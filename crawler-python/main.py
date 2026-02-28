import os
import time
import json
import sys
import re
import feedparser
import requests
from bs4 import BeautifulSoup
from urllib.parse import urljoin
import trafilatura
from kafka import KafkaProducer
from datetime import datetime

RSS_FEEDS = [
    {"url": "http://www.people.com.cn/rss/politics.xml", "category_id": 1, "source": "人民网-时政"},
    {"url": "http://www.chinanews.com.cn/rss/scroll-news.xml", "category_id": 1, "source": "中国新闻网-滚动"},
    {"url": "http://www.people.com.cn/rss/world.xml", "category_id": 2, "source": "人民网-国际"},
    {"url": "http://www.chinanews.com.cn/rss/world.xml", "category_id": 2, "source": "中国新闻网-国际"},
    {"url": "http://www.chinanews.com.cn/rss/sports.xml", "category_id": 4, "source": "中国新闻网-体育"},
    {"url": "http://www.people.com.cn/rss/sports.xml", "category_id": 4, "source": "人民网-体育"},
]

INTERVAL_SECONDS = int(os.getenv("CRAWLER_INTERVAL_SECONDS", "600"))
KAFKA_BOOTSTRAP = os.getenv("KAFKA_BOOTSTRAP", "localhost:9092")
KAFKA_TOPIC = os.getenv("KAFKA_TOPIC", "news-crawler-topic")

def log(msg):
    print(f"[{datetime.now().strftime('%H:%M:%S')}] {msg}")
    sys.stdout.flush()

_producer = None
def get_producer():
    global _producer
    if _producer is None:
        _producer = KafkaProducer(
            bootstrap_servers=KAFKA_BOOTSTRAP,
            value_serializer=lambda v: json.dumps(v, ensure_ascii=False).encode("utf-8"),
        )
    return _producer

def iso_now():
    return datetime.now().strftime("%Y-%m-%dT%H:%M:%S")

def extract_article(url, category_id=3, source_name="NewsHub"):
    log(f"DEBUG: extract_article called for {url}")
    try:
        log("DEBUG: Requesting URL...")
        # Use default requests behavior
        resp = requests.get(url, headers={"User-Agent": "Mozilla/5.0"}, timeout=10)
        log(f"DEBUG: Response status {resp.status_code}")
        
        # Manual encoding detection for better accuracy (UTF-8 > GB18030 > Auto)
        content_bytes = resp.content
        log(f"DEBUG: Content length {len(content_bytes)}")
        html = None
        try:
            html = content_bytes.decode('utf-8')
            log("DEBUG: Decoded as UTF-8")
        except UnicodeDecodeError:
            try:
                html = content_bytes.decode('gb18030')
                log("DEBUG: Decoded as GB18030")
            except UnicodeDecodeError:
                resp.encoding = resp.apparent_encoding
                html = resp.text
                log(f"DEBUG: Decoded as apparent encoding {resp.encoding}")
                
    except Exception as e:
        log(f"Error extracting {url}: {e}")
        return None

    log("DEBUG: Starting Trafilatura bare_extraction...")
    # Trafilatura extraction for metadata
    metadata = trafilatura.bare_extraction(html, url=url)
    if metadata and hasattr(metadata, 'as_dict'):
        metadata = metadata.as_dict()
    
    log("DEBUG: Starting Trafilatura extract...")
    # Trafilatura extraction for content and images (XML format to isolate main content)
    xml_content = trafilatura.extract(html, url=url, output_format='xml', include_images=True)
    
    if not xml_content:
        log("DEBUG: Trafilatura returned no content")
        return None
        
    log("DEBUG: Trafilatura done")
    xml_soup = BeautifulSoup(xml_content, "xml")
    
    # Extract content as HTML paragraphs
    paragraphs = []
    # Try to find all 'p' tags inside 'main' or document
    p_tags = xml_soup.find_all('p')
    if p_tags:
        for p in p_tags:
            # Clean text inside paragraph
            p_text = p.get_text().strip()
            if p_text:
                paragraphs.append(f"<p>{p_text}</p>")
    else:
        # Fallback if no <p> tags
        text_raw = xml_soup.get_text(separator="\n").strip()
        for line in text_raw.split("\n"):
            line = line.strip()
            if line:
                paragraphs.append(f"<p>{line}</p>")
    
    # Join paragraphs
    content_html = "\n".join(paragraphs)
    
    # Clean up repetitive footer info using Regex
    # Remove editor/source info often at end
    content_html = re.sub(r'<p>\s*(\(|（)?\s*(责编|责任编辑|来源)[:：].*?(\)|）)?\s*</p>', '', content_html, flags=re.IGNORECASE)
    # Remove standalone date lines often found at bottom
    content_html = re.sub(r'<p>\s*\d{4}[年-]\d{1,2}[月-]\d{1,2}[日]?\s*(\d{1,2}:\d{1,2})?.*?</p>\s*$', '', content_html, flags=re.DOTALL)
    
    text = xml_soup.get_text(separator="\n").strip()
    
    # Extract metadata
    title = metadata.get('title') if metadata else None
    publish_date = metadata.get('date') if metadata else None
    
    # Fallback for title
    if not title:
        soup = BeautifulSoup(html, "lxml")
        meta_title = soup.select_one('meta[property="og:title"]')
        title = (meta_title.get("content").strip() if meta_title and meta_title.get("content") else None) or (soup.title.string.strip() if soup.title and soup.title.string else "")

    # Find cover image in the ISOLATED main content
    cover = None
    graphic = xml_soup.find("graphic")
    if graphic:
        cover = graphic.get("src")
    elif xml_soup.find("img"):
        cover = xml_soup.find("img").get("src")
    
    # Resolve and Validate Cover
    if cover:
        # Resolve relative URL
        cover = urljoin(url, cover)
        
        # Filter keywords
        src_lower = cover.lower()
        if any(x in src_lower for x in ["logo", "icon", "avatar", "blank", "spacer", "gif", "share", "arrow", "button", "ad", "peopleindex", "dyz", "common", "footer", "header", "1x1", "transparent", "u719p4t47d50049f24533dt20220420152844"]):
            cover = None
            
    if len(text) < 300:
        log(f"[SKIP] {url} - Text len: {len(text)}, Cover: {cover}")
        return None

    summary = text.strip().replace("\n", " ")
    if len(summary) > 250:
        summary = summary[:247] + "..."

    publish_time = publish_date or iso_now()
    
    log(f"[SEND] {title} - Cover: {cover}")

    return {
        "title": title or url,
        "summary": summary,
        "content": content_html,
        "coverImage": cover,
        "authorId": 1,
        "categoryId": category_id,
        "sourceUrl": url,
        "sourceName": source_name,
        "publishTime": publish_time,
        "status": "PUBLISHED",
    }

def process_feed(feed_info):
    url = feed_info["url"]
    category_id = feed_info["category_id"]
    source_name = feed_info.get("source", "NewsHub")
    feed = feedparser.parse(url)
    for entry in feed.entries:
        link = entry.get("link")
        if not link:
            continue
        article = extract_article(link, category_id, source_name)
        if article:
            get_producer().send(KAFKA_TOPIC, article)
            get_producer().flush()

def run_once():
    for f in RSS_FEEDS:
        process_feed(f)

if __name__ == "__main__":
    crawler_loop = os.getenv("CRAWLER_LOOP", "true").lower() == "true"
    
    if crawler_loop:
        while True:
            run_once()
            time.sleep(INTERVAL_SECONDS)
    else:
        run_once()


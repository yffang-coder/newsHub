import os
import time
import json
import sys
import feedparser
import requests
from bs4 import BeautifulSoup
from urllib.parse import urljoin
import trafilatura
from kafka import KafkaProducer
from datetime import datetime

RSS_FEEDS = [
    {"url": "http://www.people.com.cn/rss/politics.xml", "category_id": 1},
    {"url": "http://www.chinanews.com.cn/rss/scroll-news.xml", "category_id": 1},
    {"url": "http://www.people.com.cn/rss/world.xml", "category_id": 2},
    {"url": "http://www.chinanews.com.cn/rss/world.xml", "category_id": 2},
    {"url": "http://www.chinanews.com.cn/rss/sports.xml", "category_id": 4},
    {"url": "http://www.people.com.cn/rss/sports.xml", "category_id": 4},
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

def extract_article(url, category_id=3):
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
        "content": text,
        "coverImage": cover,
        "authorId": 1,
        "categoryId": category_id,
        "sourceUrl": url,
        "sourceName": "NewsHub Python Crawler",
        "publishTime": publish_time,
        "status": "PUBLISHED",
    }

def process_feed(feed_info):
    url = feed_info["url"]
    category_id = feed_info["category_id"]
    feed = feedparser.parse(url)
    for entry in feed.entries:
        link = entry.get("link")
        if not link:
            continue
        article = extract_article(link, category_id)
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


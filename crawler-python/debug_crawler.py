from main import RSS_FEEDS, extract_article, KAFKA_TOPIC, get_producer
import feedparser
import json
import sys

# Flush stdout
def log(msg):
    print(msg)
    sys.stdout.flush()

log("Starting debug script")

# Filter only sports feeds
sports_feeds = [f for f in RSS_FEEDS if f["category_id"] == 4]
log(f"Testing {len(sports_feeds)} sports feeds")

for feed_info in sports_feeds:
    url = feed_info["url"]
    category_id = feed_info["category_id"]
    log(f"Parsing {url}...")
    feed = feedparser.parse(url)
    log(f"Entries found: {len(feed.entries)}")
    
    for i, entry in enumerate(feed.entries):
        if i >= 1: break # Test only 1 per feed
        link = entry.get("link")
        log(f"Processing link: {link}")
        if not link:
            continue
        
        log("Calling extract_article...")
        try:
            article = extract_article(link, category_id)
            log(f"extract_article returned type: {type(article)}")
            
            if article:
                log(f"--- Extracted Article ---")
                log(f"Title: {article['title']}")
                log(f"Encoding check (repr): {repr(article['title'])}")
                
                # Send to Kafka
                log("Sending to Kafka...")
                future = get_producer().send(KAFKA_TOPIC, article)
                get_producer().flush()
                result = future.get(timeout=10)
                log(f"Sent to Kafka: {result}")
            else:
                log("Article extraction returned None")
        except Exception as e:
            log(f"Exception in loop: {e}")

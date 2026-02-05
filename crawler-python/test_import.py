print("Importing os")
import os
print("Importing time")
import time
print("Importing json")
import json
print("Importing feedparser")
import feedparser
print("Importing requests")
import requests
print("Importing bs4")
from bs4 import BeautifulSoup
print("Importing urllib")
from urllib.parse import urljoin
print("Importing lxml")
import lxml.html
print("Importing trafilatura")
import trafilatura
print("Importing kafka")
from kafka import KafkaProducer
print("Importing datetime")
from datetime import datetime
print("Done imports")

import requests
url = "https://www.chinanews.com.cn/ty/2026/02-03/10564373.shtml"
print(f"Requesting {url}")
resp = requests.get(url, timeout=5)
print("Request done")

html = resp.content.decode('utf-8')
print("Decoding done")

print("Calling trafilatura.bare_extraction")
metadata = trafilatura.bare_extraction(html, url=url)
print("bare_extraction done")

print("Calling trafilatura.extract")
xml_content = trafilatura.extract(html, url=url, output_format='xml', include_images=True)
print("extract done")

print("Initializing KafkaProducer")
producer = KafkaProducer(
    bootstrap_servers="localhost:9092",
    value_serializer=lambda v: json.dumps(v, ensure_ascii=False).encode("utf-8"),
)
print("KafkaProducer initialized")
print("Sending test message")
future = producer.send("news-crawler-topic", {"test": "message"})
producer.flush()
result = future.get(timeout=10)
print(f"Sent: {result}")

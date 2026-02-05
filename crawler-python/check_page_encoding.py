import requests
import feedparser
# Import main to see if it breaks requests
import main

def check_encoding(content_bytes):
    try:
        return content_bytes.decode('utf-8')
    except UnicodeDecodeError:
        try:
            return content_bytes.decode('gb18030')
        except UnicodeDecodeError:
            return None

url = "http://www.chinanews.com.cn/rss/sports.xml"
# Get a link first
feed = feedparser.parse(url)
if feed.entries:
    link = feed.entries[0].link
    print(f"Checking link: {link}")
    resp = requests.get(link)
    print(f"Original encoding: {resp.encoding}")
    print(f"Apparent encoding: {resp.apparent_encoding}")
    
    html = check_encoding(resp.content)
    if html:
        print("Decoded successfully")
        print(f"Title in text: {html[:500]}")
    else:
        print("Failed to decode")

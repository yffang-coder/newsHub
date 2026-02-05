import requests
import feedparser

urls = [
    "http://www.chinanews.com.cn/rss/sports.xml",
    "http://www.people.com.cn/rss/sports.xml",
    "http://www.people.com.cn/rss/ty.xml",
    "http://www.chinanews.com.cn/rss/ty.xml",
]

for url in urls:
    print(f"Checking {url}...")
    try:
        resp = requests.get(url, timeout=10)
        print(f"Status: {resp.status_code}")
        if resp.status_code == 200:
            feed = feedparser.parse(resp.content)
            print(f"Entries: {len(feed.entries)}")
            if len(feed.entries) > 0:
                print(f" - {feed.entries[0].title}")
        else:
            print("Failed to fetch")
    except Exception as e:
        print(f"Error: {e}")

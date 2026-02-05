import requests
import trafilatura

url = "https://www.chinanews.com.cn/sh/2026/02-02/10365778.shtml" 
# Note: The user provided a title, not a URL. I should use a generic chinanews URL or just test logic.
# But wait, the user's log showed URLs like:
# http://politics.people.com.cn/n1/2025/0603/c458474-40492815.html
# https://www.chinanews.com.cn/sh/2026/02-02/10365778.shtml (Hypothetical)

# Let's try to fetch a known URL from previous logs or just a main page.
url = "https://www.chinanews.com.cn/"

try:
    resp = requests.get(url, headers={"User-Agent": "Mozilla/5.0"}, timeout=10)
    print(f"Original Encoding: {resp.encoding}")
    print(f"Apparent Encoding: {resp.apparent_encoding}")
    
    resp.encoding = resp.apparent_encoding
    print(f"Title sample: {resp.text[:200]}")
    
except Exception as e:
    print(e)

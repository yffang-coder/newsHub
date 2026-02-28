import requests
import json
import os
import sys
import time
from datetime import datetime

# 34个省级行政区列表（不含港澳台）
PROVINCES = [
    "北京", "天津", "上海", "重庆",
    "河北", "山西", "辽宁", "吉林", "黑龙江",
    "江苏", "浙江", "安徽", "福建", "江西", "山东",
    "河南", "湖北", "湖南", "广东", "海南",
    "四川", "贵州", "云南", "陕西", "甘肃", "青海",
    "内蒙古", "广西", "西藏", "宁夏", "新疆"
]

BACKEND_API_BASE_URL = os.getenv("BACKEND_API_URL", "http://127.0.0.1:8080/api/public/weather/update")

def log(msg):
    print(f"[{datetime.now().strftime('%H:%M:%S')}] [WeatherCrawler] {msg}")
    sys.stdout.flush()

def fetch_weather(city):
    try:
        # Use wttr.in (Free, no key, reliable)
        url = f"https://wttr.in/{city}?format=j1&lang=zh"
        resp = requests.get(url, timeout=15)
        data = resp.json()
        
        if 'current_condition' in data and 'weather' in data:
            current = data['current_condition'][0]
            daily = data['weather'][0]
            
            weather_type = "未知"
            if 'lang_zh' in current:
                weather_type = current['lang_zh'][0]['value']
            elif 'weatherDesc' in current:
                 weather_type = current['weatherDesc'][0]['value']
            
            return {
                "name": city,
                "type": weather_type,
                "temp": current.get('temp_C', 'N/A') + "°C",
                "low": daily.get('mintempC', 'N/A') + "°C",
                "high": daily.get('maxtempC', 'N/A') + "°C",
                "date": daily.get('date', datetime.now().strftime('%Y-%m-%d'))
            }
    except Exception as e:
        log(f"Error fetching weather for {city}: {e}")
    return None

def run(target_city=None):
    log("Starting weather crawl...")
    results = []
    cities_to_crawl = [target_city] if target_city else PROVINCES

    for city in cities_to_crawl:
        data = fetch_weather(city)
        if data:
            results.append(data)
            log(f"Fetched {city}: {data['type']} {data['low']}~{data['high']}")
        else:
            log(f"Failed to fetch {city}")
        time.sleep(1) # Be polite

    # Push to backend
    if results:
        try:
            # Append city parameter to the URL
            backend_api_url = f"{BACKEND_API_BASE_URL}?city={target_city if target_city else '上海'}"
            resp = requests.post(backend_api_url, json=results, timeout=10)
            log(f"Posted to backend: {resp.status_code}")
        except Exception as e:
            log(f"Error posting to backend: {e}")

if __name__ == "__main__":
    if len(sys.argv) > 1:
        run(sys.argv[1])
    else:
        run()


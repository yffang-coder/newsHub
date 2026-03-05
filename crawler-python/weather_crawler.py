import requests
import json
import os
import sys
import time
from datetime import datetime
from urllib.parse import quote

# 34个省级行政区列表（不含港澳台）
PROVINCES = [
    "北京", "天津", "上海", "重庆",
    "河北", "山西", "辽宁", "吉林", "黑龙江",
    "江苏", "浙江", "安徽", "福建", "江西", "山东",
    "河南", "湖北", "湖南", "广东", "海南",
    "四川", "贵州", "云南", "陕西", "甘肃", "青海",
    "内蒙古", "广西", "西藏", "宁夏", "新疆"
]

# 中英文城市名映射表
CITY_NAME_MAP = {
    "北京": "Beijing",
    "天津": "Tianjin",
    "上海": "Shanghai",
    "重庆": "Chongqing",
    "河北": "Hebei",
    "山西": "Shanxi",
    "辽宁": "Liaoning",
    "吉林": "Jilin",
    "黑龙江": "Heilongjiang",
    "江苏": "Jiangsu",
    "浙江": "Zhejiang",
    "安徽": "Anhui",
    "福建": "Fujian",
    "江西": "Jiangxi",
    "山东": "Shandong",
    "河南": "Henan",
    "湖北": "Hubei",
    "湖南": "Hunan",
    "广东": "Guangdong",
    "海南": "Hainan",
    "四川": "Sichuan",
    "贵州": "Guizhou",
    "云南": "Yunnan",
    "陕西": "Shaanxi",
    "甘肃": "Gansu",
    "青海": "Qinghai",
    "内蒙古": "Inner Mongolia",
    "广西": "Guangxi",
    "西藏": "Tibet",
    "宁夏": "Ningxia",
    "新疆": "Xinjiang",
    # 主要城市
    "南京": "Nanjing",
    "苏州": "Suzhou",
    "杭州": "Hangzhou",
    "广州": "Guangzhou",
    "深圳": "Shenzhen",
    "成都": "Chengdu",
    "武汉": "Wuhan",
    "西安": "Xi'an"
}

BACKEND_API_BASE_URL = os.getenv("BACKEND_API_URL", "http://127.0.0.1:8080/api/public/weather/update")

def log(msg):
    """日志输出，确保正确处理中文字符"""
    timestamp = datetime.now().strftime('%H:%M:%S')
    # 确保消息是字符串
    msg_str = str(msg)
    print(f"[{timestamp}] [WeatherCrawler] {msg_str}")
    sys.stdout.flush()

def normalize_city_name(city):
    if not city:
        return city
    for suffix in ["特别行政区", "自治区", "省", "市", "盟", "地区", "县"]:
        if city.endswith(suffix):
            return city[:-len(suffix)]
    return city

def get_city_english_name(chinese_city):
    normalized = normalize_city_name(chinese_city)
    return CITY_NAME_MAP.get(normalized, normalized)

def get_weather_type_from_code(code):
    """根据WMO天气代码转换天气类型"""
    weather_codes = {
        0: "晴",
        1: "晴", 2: "晴", 3: "晴",
        45: "雾", 48: "雾",
        51: "小雨", 53: "中雨", 55: "大雨",
        61: "小雨", 63: "中雨", 65: "大雨",
        80: "阵雨", 81: "强阵雨", 82: "强阵雨",
        95: "雷暴", 96: "雷暴", 99: "强雷暴"
    }
    return weather_codes.get(code, "未知")

def try_open_meteo(city):
    """使用open-meteo API获取天气数据"""
    try:
        city_en = get_city_english_name(city)
        city_norm = normalize_city_name(city)
        search_names = []
        if city_en:
            search_names.append(city_en)
        if city_norm and city_norm not in search_names:
            search_names.append(city_norm)
        if city and city not in search_names:
            search_names.append(city)
        log(f"Looking up coordinates for {city} ({', '.join(search_names)})...")

        geocode_data = None
        for name in search_names:
            geocode_url = f"https://geocoding-api.open-meteo.com/v1/search?name={quote(name)}&count=1&language=zh"
            geocode_resp = requests.get(geocode_url, timeout=10)
            geocode_data = geocode_resp.json()
            if 'results' in geocode_data and len(geocode_data['results']) > 0:
                break

        if geocode_data and 'results' in geocode_data and len(geocode_data['results']) > 0:
            location = geocode_data['results'][0]
            lat = location['latitude']
            lon = location['longitude']
            log(f"Found coordinates for {city}: lat={lat}, lon={lon}")

            weather_url = f"https://api.open-meteo.com/v1/forecast?latitude={lat}&longitude={lon}&current_weather=true&daily=temperature_2m_max,temperature_2m_min&timezone=auto"
            weather_resp = requests.get(weather_url, timeout=10)
            weather_data = weather_resp.json()

            if 'current_weather' in weather_data and 'daily' in weather_data:
                current = weather_data['current_weather']
                daily = weather_data['daily']

                weather_type = get_weather_type_from_code(current.get('weathercode', 0))
                temp = current.get('temperature', 'N/A')
                low = daily.get('temperature_2m_min', ['N/A'])[0]
                high = daily.get('temperature_2m_max', ['N/A'])[0]

                return {
                    "name": city,
                    "type": weather_type,
                    "temp": f"{temp}°C",
                    "low": f"{low}°C",
                    "high": f"{high}°C",
                    "date": datetime.now().strftime('%Y-%m-%d')
                }
            else:
                log(f"Weather data structure unexpected for {city}")
        else:
            log(f"No geocoding result found for {city} ({', '.join(search_names)})")

    except Exception as e:
        log(f"Error fetching weather for {city} via open-meteo: {e}")
    return None

def try_wttr_in(city):
    """尝试wttr.in作为备选"""
    try:
        url = f"https://wttr.in/{quote(city)}?format=j1&lang=zh"
        resp = requests.get(url, timeout=5)  # 更短的超时时间
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
        log(f"Error fetching weather for {city} via wttr.in: {e}")
    return None

def fetch_weather(city):
    """获取天气数据，尝试多个API源"""
    # 首先尝试open-meteo
    weather_data = try_open_meteo(city)
    if weather_data:
        return weather_data

    # 如果open-meteo失败，尝试wttr.in
    log(f"Open-meteo failed for {city}, trying wttr.in...")
    weather_data = try_wttr_in(city)
    if weather_data:
        return weather_data

    # 如果都失败，返回None
    return None

def run(target_city=None):
    log("Starting weather crawl...")
    cities_to_crawl = [target_city] if target_city else PROVINCES

    for city in cities_to_crawl:
        data = fetch_weather(city)
        if data:
            log(f"Fetched {city}: {data['type']} {data['temp']} ({data['low']}~{data['high']})")
            # Push to backend for each city
            try:
                backend_api_url = f"{BACKEND_API_BASE_URL}?city={quote(city)}"
                resp = requests.post(backend_api_url, json=[data], timeout=10)
                log(f"Posted {city} to backend: {resp.status_code}")
            except Exception as e:
                log(f"Error posting {city} to backend: {e}")
        else:
            log(f"Failed to fetch {city}")
        time.sleep(1)  # 礼貌间隔

if __name__ == "__main__":
    if len(sys.argv) > 1:
        run(sys.argv[1])
    else:
        run()


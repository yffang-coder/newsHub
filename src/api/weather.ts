import request from './request';

export interface WeatherData {
  name: string;
  type: string;
  temp: string;
  low: string;
  high: string;
  date: string;
}

export const getWeatherByIp = async (): Promise<WeatherData[]> => {
  try {
    const res = await request.get('/public/weather') as WeatherData[];
    return res;
  } catch (error) {
    console.error('Error fetching weather by IP:', error);
    return [];
  }
};
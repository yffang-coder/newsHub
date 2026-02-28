# NewsHub - 现代化全栈新闻聚合平台

NewsHub 是一个基于现代化技术栈构建的全栈新闻聚合平台。它通过自动化的 Python 爬虫系统抓取多源新闻，利用 Spring Boot 后端进行高效处理与存储，并通过 Vue 3 构建的高性能前端界面为用户提供极致的阅读体验。

---

## 🌟 核心功能

### 🌐 前台阅读 (Public)
- **多维度新闻展示**：包含头条推荐、最新资讯等板块。
- **分类浏览**：支持时事、科技、体育、财经、文化等多个频道。
- **全文检索**：基于数据库的高效关键词搜索功能。
- **实时组件**：集成动态天气预报与每日星座运势。
- **极致体验**：支持 Markdown 渲染、图片懒加载及响应式布局（完美适配手机/平板/电脑）。

### 👤 用户系统 (User)
- **安全认证**：基于 JWT 的无状态身份验证。
- **互动交流**：支持文章评论发表与回复。
- **收藏管理**：用户可收藏感兴趣的文章，建立个人阅读库。

### ⚙️ 后台管理 (Admin)
- **数据可视化**：基于 ECharts 的仪表盘，实时监控访问量、文章增长及评论趋势。
- **内容控管**：完整的文章发布、编辑、下架及分类管理功能。
- **用户审计**：管理注册用户权限与状态。
- **系统设置**：动态配置站点基本信息、"关于我们"、数据保留策略等。
- **消息推送**：管理员可向特定用户或全站用户发送通知。

---

## 🛠 技术架构

### 前端 (Frontend)
- **核心框架**: [Vue 3](https://vuejs.org/) (Composition API)
- **状态管理**: [Pinia](https://pinia.vuejs.org/)
- **路由**: [Vue Router](https://router.vuejs.org/)
- **UI 组件库**: [Element Plus](https://element-plus.org/)
- **样式方案**: [Tailwind CSS](https://tailwindcss.com/) + SCSS
- **可视化**: [ECharts](https://echarts.apache.org/)

### 后端 (Backend)
- **核心框架**: [Spring Boot 3.2.3](https://spring.io/projects/spring-boot)
- **持久层**: [MyBatis](https://mybatis.org/mybatis-3/) (注解方式 + 动态 SQL)
- **安全框架**: [Spring Security](https://spring.io/projects/spring-security)
- **令牌机制**: [JSON Web Token (JWT)](https://jwt.io/)
- **缓存**: [Redis](https://redis.io/) (支持热点数据 5 分钟 TTL)
- **数据库**: [MySQL 8.0](https://www.mysql.com/)

### 爬虫系统 (Crawler)
- **核心语言**: Python 3.10+
- **解析技术**: BeautifulSoup4, Feedparser (RSS), Trafilatura
- **集成方式**: 定时任务执行，直接写入数据库或通过 Kafka 异步处理。

---

## 📂 项目结构

```text
blog/
├── backend/                # Spring Boot 后端源码
│   ├── src/main/java       # 核心业务逻辑
│   ├── src/main/resources  # 配置(yml)及SQL脚本(schema.sql)
│   └── pom.xml             # Maven 依赖配置
├── src/                    # Vue 3 前端源码
│   ├── api/                # Axios 封装与接口定义
│   ├── components/         # 公共组件库
│   ├── views/              # 业务页面 (Admin/Public)
│   ├── stores/             # Pinia 状态管理
│   └── router/             # 路由配置
├── crawler-python/         # Python 爬虫系统
│   ├── main.py             # 爬虫入口
│   └── requirements.txt    # Python 依赖
├── deployment/             # 部署配置文件
│   ├── docker-compose.yml  # 一键部署配置
│   └── nginx.conf          # Nginx 反向代理配置
└── package.json            # 前端依赖与脚本
```

---

## ⚡ 快速开始 (本地开发)

### 1. 环境准备
确保已安装：JDK 17+, Node.js 18+, Python 3.10+, MySQL 8.0+, Redis 6+。

### 2. 数据库初始化
1. 创建数据库 `newshub`。
2. 运行 blog/backend/src/main/resources/schema.sql 初始化表结构与默认数据。

### 3. 后端启动
```bash
cd backend
# 使用本地开发配置启动
mvn spring-boot:run -Dspring-boot.run.profiles=local
```
后端 API 地址：`http://localhost:8080`

### 4. 前端启动
```bash
npm install
npm run dev
```
前端访问地址：`http://localhost:5173`

---

## 🚢 生产部署

### Docker 一键部署 (推荐)
项目提供了一套完整的 Docker 容器化方案，包含 MySQL、Redis、Kafka、前端 Nginx 以及后端服务。

```bash
cd deployment
# 启动所有服务
docker-compose up -d
```

### 宝塔面板部署
详细的宝塔部署步骤请参考 blog/deployment/BAOTA_GUIDE.md。

---

## 🔧 关键配置说明

- **JWT 密钥**: 在 `application.yml` 的 `app.jwt.secret` 中修改。
- **爬虫间隔**: 修改 `app.crawler.interval` 配置（默认为 4 小时）。
- **缓存策略**: 热点新闻默认缓存 5 分钟，单篇文章缓存 1 小时。
- **跨域配置**: 后端 Controller 已通过 `@CrossOrigin` 开启跨域支持。

---

## 📄 许可证

本项目采用 [MIT License](LICENSE) 许可协议。

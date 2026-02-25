# Barter App - 易物

以物易物交换平台，让闲置物品流动起来。

## 📥 下载

[**点击下载最新 Debug APK**](https://github.com/OneCentClawd/barter-app/releases/tag/latest)

## 项目结构

```
barter-app/
├── backend/         # Spring Boot 后端
└── android/         # Android 客户端 (Jetpack Compose)
```

## 后端 (Spring Boot)

### 技术栈
- Java 17 + Spring Boot 3.2
- Spring Security + JWT
- Spring Data JPA + PostgreSQL
- Maven

### 运行
```bash
cd backend
mvn spring-boot:run
```

默认端口：9527

### API 接口
| 模块 | 接口 | 说明 |
|------|------|------|
| 认证 | POST /api/auth/register | 注册 |
| | POST /api/auth/login | 登录 |
| 物品 | GET /api/items/list | 物品列表 |
| | GET /api/items/{id} | 物品详情 |
| | POST /api/items | 发布物品 |
| 交换 | POST /api/trades | 发起交换 |
| | PUT /api/trades/{id}/status | 更新状态 |
| 聊天 | POST /api/chat/send | 发送消息 |
| | GET /api/chat/conversations | 对话列表 |

## Android 客户端

### 技术栈
- Kotlin + Jetpack Compose
- MVVM + Hilt
- Retrofit + OkHttp
- Coil + DataStore

### 运行
1. 用 Android Studio 打开 `android/` 目录
2. 等待 Gradle 同步
3. 运行到模拟器或真机

## 功能

- ✅ 用户注册/登录
- ✅ 发布闲置物品（支持多图）
- ✅ 浏览/搜索物品
- ✅ 发起物品交换
- ✅ 接受/拒绝交换请求
- ✅ 即时聊天
- ✅ 用户评价

## License

MIT

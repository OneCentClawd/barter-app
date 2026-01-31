# 易物 iOS App

基于 SwiftUI 开发的 iOS 版本。

## 项目结构

```
BarterApp/
├── BarterApp.swift          # 入口文件
├── Models/                  # 数据模型
├── Services/                # 网络服务
│   ├── ApiConfig.swift      # API 配置
│   ├── ApiService.swift     # 网络请求
│   └── TokenManager.swift   # Token 管理
├── ViewModels/              # 视图模型
├── Views/                   # UI 视图
│   ├── Auth/                # 登录注册
│   ├── Main/                # 主界面
│   ├── Item/                # 物品相关
│   ├── Chat/                # 聊天
│   └── Profile/             # 用户资料
├── Components/              # 通用组件
└── Utils/                   # 工具类
```

## 开发环境要求

- macOS 13.0+
- Xcode 15.0+
- iOS 16.0+ (部署目标)

## 开始使用

1. 用 Xcode 打开 `BarterApp` 文件夹（或创建新项目并导入代码）
2. 在 Xcode 中：File > New > Project > iOS > App
3. 将 `BarterApp` 文件夹中的代码复制到项目中
4. 运行项目

## API 配置

修改 `Services/ApiConfig.swift` 中的 `baseURL` 为你的后端地址：

```swift
let baseURL = "http://your-server-ip:9527"
```

## 功能列表

- [x] 登录/注册
- [x] 首页物品列表
- [x] 物品详情
- [x] 用户资料
- [x] 消息列表
- [x] 聊天功能
- [x] 联系客服
- [ ] 发布物品
- [ ] 交换请求
- [ ] 我的物品/收藏
- [ ] 设置页面

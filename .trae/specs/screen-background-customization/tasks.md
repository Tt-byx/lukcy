# Tasks

- [x] Task 1: 后端 — 新增屏幕配置数据层
  - [x] SubTask 1.1: 在 `schema.sql` 中新增 `screen_config` 表
  - [x] SubTask 1.2: 创建 `ScreenConfig` 实体类
  - [x] SubTask 1.3: 创建 `ScreenConfigMapper` 接口

- [x] Task 2: 后端 — 文件上传与屏幕配置 API
  - [x] SubTask 2.1: 创建文件上传工具类（保存到本地文件系统，使用 UUID 命名避免冲突）
  - [x] SubTask 2.2: 创建 `ScreenConfigService` 接口与实现（保存配置、获取配置、清除背景）
  - [x] SubTask 2.3: 创建 `ScreenConfigController`（上传背景文件接口、获取屏幕配置接口、清除背景接口）
  - [x] SubTask 2.4: 配置 Spring Boot 静态资源映射，使上传的文件可通过 URL 访问

- [x] Task 3: 管理后台 — 屏幕设置 Tab
  - [x] SubTask 3.1: 在 `lucky-admin` 侧边栏新增「屏幕设置」Tab
  - [x] SubTask 3.2: 实现屏幕设置面板：选择背景类型（图片/视频）、文件上传按钮、预览区域
  - [x] SubTask 3.3: 实现清除背景按钮，恢复默认背景

- [x] Task 4: 大屏页面 — 移除活动名称 + 动态背景
  - [x] SubTask 4.1: 移除顶部标题栏中的活动名称 `<h1 class="title">`
  - [x] SubTask 4.2: 实现动态背景加载逻辑（从 API 获取屏幕配置，判断使用自定义背景或默认渐变）
  - [x] SubTask 4.3: 实现视频背景（使用 `<video>` 标签，静音、循环、自动播放）
  - [x] SubTask 4.4: 实现图片背景（使用 `background-image` CSS）
  - [x] SubTask 4.5: 添加定时轮询机制，定期检查背景配置变更

# Task Dependencies
- Task 2 依赖 Task 1
- Task 3 依赖 Task 2
- Task 4 依赖 Task 2
- Task 3 与 Task 4 可并行执行

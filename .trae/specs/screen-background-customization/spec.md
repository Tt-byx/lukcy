# 大屏背景自定义及活动名称移除 Spec

## Why
目前大屏展示页面的背景是硬编码的 CSS 渐变色+粒子动画，无法由管理员自定义。同时顶部显示的活动名称在大屏场景下不需要展示。需要让管理员在后台能够上传自定义的背景图片或视频，并将界面的活动名称移除。

## What Changes
- **大屏页面**：移除顶部标题栏中的活动名称，改为加载后台配置的自定义背景（图片或视频）
- **管理后台**：新增「屏幕设置」Tab，支持上传背景图片/视频、预览、切换背景类型
- **后端**：新增屏幕配置实体与表、文件上传接口、屏幕配置 CRUD 接口
- **数据库**：新增 `screen_config` 表存储背景配置信息

## Impact
- Affected specs: 无（全新功能）
- Affected code:
  - `lucky-screen/src/App.vue` — 移除活动名称显示，新增动态背景加载
  - `lucky-admin/src/App.vue` — 新增屏幕设置 Tab
  - `lucky-server` — 新增 ScreenConfig 实体/Controller/Service、文件上传逻辑
  - `schema.sql` — 新增 `screen_config` 表

## ADDED Requirements

### Requirement: 管理员上传自定义背景
系统 SHALL 允许管理员在后台上传图片或视频作为大屏背景。

#### Scenario: 上传图片作为背景
- **WHEN** 管理员在「屏幕设置」Tab 中选择「图片」类型并上传一张图片
- **THEN** 图片被保存到服务器，大屏页面自动展示该图片作为背景

#### Scenario: 上传视频作为背景
- **WHEN** 管理员在「屏幕设置」Tab 中选择「视频」类型并上传一段视频
- **THEN** 视频被保存到服务器，大屏页面自动循环播放该视频作为背景

#### Scenario: 大屏无自定义背景时的降级
- **WHEN** 管理员未设置任何背景或清除了背景设置
- **THEN** 大屏使用原有的深色渐变背景

### Requirement: 大屏背景实时同步
系统 SHALL 支持大屏页面在收到后台配置变更后，实时或轮询获取最新背景配置并切换显示。

#### Scenario: 后台更新背景后大屏自动刷新
- **WHEN** 管理员在后台更新背景配置
- **THEN** 大屏页面在下次轮询（或 WebSocket 通知）后自动加载新背景

### Requirement: 移除大屏活动名称显示
系统 SHALL 从大屏页面移除顶部标题栏中的活动名称。

#### Scenario: 大屏页面不再显示活动名称
- **WHEN** 用户打开大屏页面
- **THEN** 顶部不再显示活动名称，仅保留参与人数和在线人数

## MODIFIED Requirements
无（纯新增功能）

## REMOVED Requirements
无

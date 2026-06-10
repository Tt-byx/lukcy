# Checklist

- [x] `screen_config` 表已创建，包含 id, background_type, background_url, updated_at 字段
- [x] `ScreenConfig` 实体类字段与数据库表一致
- [x] `ScreenConfigMapper` 可正常操作数据库
- [x] 文件上传接口支持 multipart/form-data，接受图片和视频文件
- [x] 上传的文件保存到服务器本地目录，使用 UUID 命名
- [x] 上传的文件可通过 HTTP URL 直接访问
- [x] 获取屏幕配置接口返回 background_type 和 background_url
- [x] 清除背景接口能重置配置，大屏恢复默认渐变背景
- [x] 管理后台侧边栏显示「屏幕设置」Tab
- [x] 管理后台可选择背景类型（图片/视频）并上传文件
- [x] 管理后台可预览当前背景效果
- [x] 管理后台可清除自定义背景
- [x] 大屏页面顶部不再显示活动名称
- [x] 大屏页面加载时从 API 获取屏幕配置
- [x] 大屏页面自定义图片背景正确显示
- [x] 大屏页面自定义视频背景正确显示（静音循环自动播放）
- [x] 无自定义背景时大屏显示默认深色渐变背景
- [x] 大屏定时轮询屏幕配置，后台更新后自动生效

# 待完成任务清单

## 当前正在做（中断中）

（无）

---

## 本轮已完成

### H5 前端
- ✅ `h5/pages/user/dealer/index.vue` — 全部 i18n + 紫色主题风格重写
- ✅ `h5/pages/user/share/brokerage.vue` — 全部 i18n + 风格统一
- ✅ `h5/pages/user/share/team.vue` — 全部 i18n + 风格统一
- ✅ `h5/locale/zh-CN.js` — 新增 `dealer.*` / `brokerage.*` / `team.*` 分组
- ✅ `h5/locale/zh-TW.js` — 同上（繁体）
- ✅ `h5/locale/en.js` — 同上（英文）

### 后管前端
- ✅ `admin-ui/src/api/commerce/wallet.js` — 新增 `getWalletStats` 函数
- ✅ `admin-ui/src/views/commerce/wallet/log.vue` — 顶部加日/周/月/年入金+提现统计卡片
- ✅ `admin-ui/src/views/commerce/wallet/withdraw.vue` — 加用户头像/昵称/手机号列，加手机号+昵称搜索，applyInfo 格式化展示

### 后管后端
- ✅ `WithdrawVO.java` — 新建，继承 `UserWalletApply`，加 `nickname/mobile/avatar`
- ✅ `VipOrderVO.java` — 新建，继承 `VipOrder`，加 `nickname/mobile/avatar`
- ✅ `UserWalletApplyMapper.java` — 加 `selectWithUserPage` 联查（JOIN user 表，支持 status/mobile/nickname 过滤）
- ✅ `VipOrderMapper.java` — 加 `selectWithUserPage` 联查（同上）
- ✅ `UserWalletLogMapper.java` — 加 `sumIncomeFrom` / `sumWithdrawFrom` 统计方法
- ✅ `AdminWalletController.java` — 提现列表改用联查，新增 `/stats` 统计接口
- ✅ `AdminVipController.java` — VIP 订单列表改用联查，支持 mobile/nickname 搜索

---

## 遗留/后续任务

### 前端 admin-ui
- ✅ `vip/order.vue` — 加手机号/昵称搜索 + 用户信息列（头像+昵称+手机号）

### 后端配置（需要真实密钥）
- 填入微信支付凭证（`duanju-api/src/main/resources/application.yml` → `duanju.wxpay.*`）
- 填入支付宝凭证（`duanju.alipay.*`）

### 其他已知问题
- 视频/图片上传：`duanju.upload.provider` 目前只配置了 local，需改为 OSS/MinIO 支持集群部署
- MySQL/Redis 地址应改为环境变量（目前硬编码 `127.0.0.1`）

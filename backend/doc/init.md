# 短剧平台 · 开发环境搭建指南

## 项目概览

短剧视频流媒体平台，Java Spring Boot 后端 + UniApp H5 前端 + Vue3 后管。

```
backend/          Java 后端（Maven 多模块）
h5/               UniApp H5 用户端（Vue2 + uView）
admin-ui/         后台管理界面（Vue3 + Element Plus）
```

---

## 一、环境依赖

| 工具 | 版本要求 | 说明 |
|------|----------|------|
| JDK | 17+ | Spring Boot 3.x 最低要求 |
| Maven | 3.8+ | 构建工具 |
| MySQL | 8.0+ | 数据库，库名 `duanju` |
| Redis | 6.0+ | 会话 + 缓存，无密码，默认端口 6379 |
| Node.js | 18+ | 前端构建 |
| HBuilderX | 最新版 | UniApp H5 开发 / 编译（或 CLI） |

---

## 二、后端启动

### 1. 初始化数据库

```bash
# 新环境首次初始化
mysql -uroot -p -e "CREATE DATABASE IF NOT EXISTS duanju DEFAULT CHARSET utf8mb4;"
mysql -uroot -p duanju < backend/sql/初始化建表.sql

# 如需加载测试视频数据（可选）
mysql -uroot -p duanju < backend/sql/测试数据.sql
mysql -uroot -p duanju < backend/sql/导入真实剧集数据.sql
```

初始管理员账号：`admin` / `admin123`

### 2. 修改数据库配置

编辑 `backend/duanju-api/src/main/resources/application.yml` 和 `backend/duanju-admin/src/main/resources/application.yml`，将数据库连接改为本地地址：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/duanju-new?...
    username: root
    password: 你的密码
```

### 3. 编译并启动

```bash
cd backend

# 首次必须全量 install，否则子模块依赖找不到
mvn install -DskipTests

# 启动用户端 API（端口 8080）
cd duanju-api && mvn spring-boot:run

# 另开终端，启动后管（端口 8081）
cd ../duanju-admin && mvn spring-boot:run
```

**接口文档：** http://localhost:8081/doc.html

---

## 三、后管 UI 启动

```bash
cd admin-ui
npm install
npm run dev
```

访问 http://localhost:5173，账号 `admin` / `admin123`

---

## 四、H5 前端启动

H5 使用 UniApp，推荐用 **HBuilderX** 打开 `h5/` 目录。

**CLI 方式（可选）：**
```bash
npm install -g @dcloudio/uvm
cd h5
npm install
npx uni dev -p h5   # H5 开发模式，默认端口 5174
```

修改 API 地址：`h5/config/` 下找到 baseURL，改为 `http://localhost:8080`

---

## 五、模块依赖链

```
duanju-common
  └─ duanju-system
       └─ duanju-drama
            └─ duanju-commerce
                 └─ duanju-payment
                      ├─ duanju-api    (port 8080，用户端)
                      └─ duanju-admin  (port 8081，后管)
```

修改公共模块后，需要重新 `mvn install` 才能让上层模块感知到变化。

---

## 六、路由鉴权规则

| 路径 | 是否需要登录 |
|------|------------|
| `/api/auth/**` | 不需要 |
| `/api/video/**`、`/api/vip/list`、`/api/usable/list`、`/api/reseller/list` | 不需要 |
| `/api/user/**`、`/api/wallet/**` | 需要登录 |
| `/api/vip/buy`、`/api/usable/buy`、`/api/reseller/buy` | 需要登录 |
| `/admin/**`（除 `/admin/auth/login`） | 需要管理员登录 |

---

## 七、数据库结构说明

| 表名 | 说明 |
|------|------|
| `vs_drama_user` | 用户表，含 VIP 到期时间、点数余额、分销等级 |
| `vs_drama_video` | 短剧主表（标题、封面、分类、总集数） |
| `vs_drama_video_episodes` | 分集表（每集视频地址、是否免费、解锁点数） |
| `vs_drama_episode_unlock` | 用户解锁记录（防止重复扣费） |
| `vs_drama_vip` | VIP 套餐配置 |
| `vs_drama_vip_order` | VIP 购买订单 |
| `vs_drama_usable` | 点数套餐配置（1元=10点） |
| `vs_drama_usable_order` | 点数购买订单 |
| `vs_drama_user_wallet_log` | 钱包流水（充值/消费记录） |
| `vs_drama_user_wallet_apply` | 提现申请 |
| `vs_drama_reseller` | 分销商套餐配置（含 `name_i18n` 多语言字段） |
| `vs_drama_reseller_bind` | 分销绑定关系 |
| `vs_drama_reseller_order` | 分销佣金记录 |
| `vs_drama_watch_log` | 观看记录 |
| `vs_drama_comment` | 评论表 |
| `vs_drama_block` | 首页分块配置（Banner、推荐位等） |
| `sys_config` | 系统参数配置（OSS、支付、i18n 等） |

> **逻辑删除约定：** 所有继承 `BaseEntity` 的表都有 `delete_time` 字段，`NULL` 表示正常，有值表示已删除。

---

## 八、多语言说明（H5）

H5 支持三种语言，文件位于 `h5/locale/`：

- `zh-CN.js` — 简体中文（默认）
- `zh-TW.js` — 繁体中文
- `en.js` — 英文

**新增 UI 文案时必须同步更新三个文件**，模板中统一用 `$t('模块.key')` 引用，禁止硬编码中文。

### 后端内容多语言（套餐名称等）

部分由后管配置的动态内容支持多语言，存为 JSON 字段：

| 表 | 字段 | 说明 |
|---|---|---|
| `vs_drama_usable` | `title_i18n` / `desc_i18n` | 点数套餐名称和描述 |
| `vs_drama_reseller` | `name_i18n` | 分销套餐名称 |

格式：`{"zh-TW":"繁体名称","en":"English Name"}`，`zh-CN` 存在默认 `name`/`title` 字段中，作为回退值。后管对应页面已提供多语言 Tab 编辑入口。

### 数据库变更记录

| 日期 | 变更内容 |
|------|----------|
| 2026-05-18 | `vs_drama_reseller` 加 `name_i18n VARCHAR(1000)` 字段 |

---

## 九、支付配置（上线前必须填写）

编辑 `backend/duanju-api/src/main/resources/application.yml`：

```yaml
duanju:
  wxpay:
    appId: 你的小程序/公众号 appId
    mchId: 商户号
    apiV3Key: APIv3 密钥
    privateKeyPath: 私钥文件路径（apiclient_key.pem）
    certSerialNo: 证书序列号
  alipay:
    appId: 支付宝 appId
    privateKey: 应用私钥
    alipayPublicKey: 支付宝公钥
```

---

## 十、常见问题

**Q: 启动报 `ClassNotFoundException` 或模块找不到？**
A: 在 `backend/` 根目录执行 `mvn install -DskipTests`，重新安装所有子模块到本地仓库。

**Q: 登录接口返回 401 但账号密码正确？**
A: 检查 Redis 是否启动（`redis-cli ping` 返回 `PONG`）。Sa-Token 会话依赖 Redis。

**Q: H5 请求跨域报错？**
A: 开发环境后端已配置 CORS 放行，确认 H5 的 baseURL 指向正确端口（8080）。

**Q: 视频上传后看不到？**
A: 当前上传配置为本地存储（`duanju.upload.provider=local`），文件存 `/data/uploads`。生产环境需切换为 OSS/MinIO。

**Q: `@RequestParam` 无法自动推断参数名？**
A: 父 `pom.xml` 已配置 `-parameters` 编译参数，如果 IDE 报错，确认 Maven 编译没有跳过该配置。

# 短剧平台后端 — 项目说明（供 Claude Code 读取）

## 项目概述

短剧视频流媒体平台，原 PHP 系统完整重写为 Java Spring Boot，项目位于 `backend/` 目录。

## 启动方式

```bash
# 第一次必须先全量 install，否则模块依赖找不到
cd backend
mvn install -DskipTests

# 启动用户端 API（端口 8080）
cd duanju-api && mvn spring-boot:run

# 启动后管服务（端口 8081）
cd duanju-admin && mvn spring-boot:run
```

接口文档：http://localhost:8081/doc.html

## 技术栈

- Spring Boot 3.2.5 + MyBatis-Plus 3.5.7 + Sa-Token 1.38.0
- WxJava 4.6.0（微信支付）
- MySQL 数据库名 `duanju`，表前缀 `vs_`
- Redis localhost:6379 无密码，API 用 db0，Admin 用 db1

## 模块依赖链

```
duanju-common
  └─ duanju-system
       └─ duanju-drama
            └─ duanju-commerce
                 └─ duanju-payment
                      ├─ duanju-api    (port 8080, 用户端)
                      └─ duanju-admin  (port 8081, 后管)
```

## 已实现功能

- **用户体系**：手机号注册/密码登录、微信小程序登录（自动注册）
- **内容模块**：短剧视频、分类、首页分块推荐
- **VIP 套餐**：购买、支付、到期续期（叠加计算）
- **点数套餐**：购买后直接到账用户 usable 余额
- **分销体系**：套餐购买、有效期管理、二级佣金自动分配
- **钱包**：余额(money) / 积分(score) / 点数(usable) 三类，提现申请与审核
- **微信支付 V3**：JSAPI（小程序/公众号）、H5、App 三种方式
- **支付宝**：接口预留，待填入真实密钥
- **后管接口**：用户管理、内容管理、VIP/点数/分销/钱包全部 CRUD

## 路由鉴权规则

| 路径 | 是否需要登录 |
|------|------------|
| `/api/auth/**` | 不需要 |
| `/api/video/**`, `/api/vip/list`, `/api/usable/list`, `/api/reseller/list` | 不需要 |
| `/api/user/**`, `/api/wallet/**` | 需要 |
| `/api/vip/buy`, `/api/usable/buy`, `/api/reseller/buy` | 需要 |
| `/admin/**`（除 `/admin/auth/login`）| 需要管理员登录 |

未登录访问受保护接口返回 `{"code": 401, "msg": "请先登录"}`。

## 数据库初始化

```bash
mysql -uroot -p < backend/sql/init.sql
```

初始管理员账号：`admin` / `admin123`

## 注意事项

### WxJava 4.6.0 API 变化
- `createOrderV3` 参数类型是 `TradeTypeEnum`（`com.github.binarywang.wxpay.bean.result.enums`），不是旧版 `WxPayConstants.TradeType` 字符串
- `WxPayUnifiedOrderV3Result` H5 场景直接用 `.getH5Url()`，没有 `H5Result` 内部类
- `AppResult` 方法名是 `getAppid()`（小写d）、`getNoncestr()`（小写s）、`getTimestamp()`（小写s）
- 支付回调解析：`parseOrderNotifyV3Result(body, null).getResult()` 返回 `DecryptNotifyResult` 对象

### MyBatis-Plus 逻辑删除
全局配置 `delete_time` 字段，值 `NOW()` 表示删除，`NULL` 表示正常。所有继承 `BaseEntity` 的实体对应表**必须有 `delete_time` 列**。

### 订单号前缀约定（支付回调路由依赖此规则）
- `VIP` — VIP 套餐订单
- `USE` — 点数套餐订单
- `RES` — 分销套餐订单

### Maven 编译参数
父 pom.xml 已配置 `-parameters`，否则 `@RequestParam` 无法自动推断参数名（Spring Boot 3.x 不再默认保留参数名）。

## 前端开发规范（强制执行）

### 多语言（i18n）规则
- H5 前端支持 zh-CN / zh-TW / en 三个语言包，文件位于 `h5/locale/`
- **所有新增 UI 文案必须同步添加到三个语言包**，缺一不可
- 模板中禁止硬编码中文字符串，一律使用 `$t('xxx.yyy')` 引用
- 新增 key 命名规则：`模块名.功能名`，如 `watch.edit`、`comment.send`
- 带参数的文案使用 `{0}` 占位，如 `watch.deleteSelected: '删除({0})'`

### 横向可扩展规则
- Tab 数量、内容类型、语言种类等不得硬编码数字判断（如 `if tab === 0`），应使用有语义的常量或配置驱动
- 组件 props 设计应允许外部传入类型列表，而不是在组件内写死枚举值
- API 路径、分页大小等配置项不写死在业务逻辑里，通过 data/config 统一管理
- 新增功能时优先评估是否影响现有横向扩展点（如新增 Tab 是否只需改数据而不改模板）

## 待完成

- [ ] 填入真实微信支付凭证（`backend/duanju-api/src/main/resources/application.yml` 中 `duanju.wxpay.*`）
- [ ] 填入真实支付宝凭证（`duanju.alipay.*`）
- [ ] 前端 UniApp 适配（改 API 基础地址、响应码判断 `code==200`、Token 头名称 `Authorization`）
- [ ] 视频/图片上传（`duanju.upload.provider` 支持 local/oss/minio，现在只配置了 local）

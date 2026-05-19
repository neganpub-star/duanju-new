/**
 * 平台统一按钮样式工厂
 * ----------------------------------------
 * 背景：原来每个页面（dealer / withdraw / recharge / login / payee ...）都自己写一份 buttonStyle 对象，
 *      尺寸 / 字号 / 圆角各不相同，难以维护，新增视觉规范无处统一覆盖。
 *
 * 用法：
 *   import { primaryBtn, dangerBtn, ghostBtn } from '@/common/utils/buttonStyle.js'
 *
 *   // 默认主按钮（高 100rpx、字号 30rpx、圆角 16rpx、紫色渐变、100% 宽）
 *   buttonStyle: primaryBtn()
 *
 *   // 选预设尺寸
 *   buttonStyle: primaryBtn({ size: 'lg' })   // 高 108rpx / 字号 32rpx / 圆角 20rpx
 *
 *   // 自由覆盖任意字段
 *   buttonStyle: primaryBtn({ width: '300rpx', height: '80rpx', fontSize: '24rpx' })
 *
 * 颜色统一走 CSS 变量 var(--dj-*)（在 common/style/style.scss 注入到 :root），
 * 改 uni.scss 的 $dj-primary 即可全平台跟随主题色。
 */

/** 三档尺寸预设 */
const SIZE_PRESET = {
  sm: { height: '80rpx',  fontSize: '24rpx', borderRadius: '14rpx' },
  md: { height: '100rpx', fontSize: '30rpx', borderRadius: '16rpx' },
  lg: { height: '108rpx', fontSize: '32rpx', borderRadius: '20rpx' }
}

/** 通用基础字段（所有 type 共享） */
const BASE = {
  width: '100%',
  border: 'none',
  margin: '0',
  fontWeight: 'bold'
}

function pickSize(opts) {
  const preset = SIZE_PRESET[opts.size] || SIZE_PRESET.md
  return {
    height: opts.height || preset.height,
    fontSize: opts.fontSize || preset.fontSize,
    borderRadius: opts.borderRadius || opts.radius || preset.borderRadius
  }
}

/**
 * 主操作按钮 — 紫色渐变填充 + 白字
 * 用于：开通经销商、确认支付、提现申请、保存、绑定等正向核心动作
 */
export function primaryBtn(opts = {}) {
  return {
    ...BASE,
    width: opts.width || BASE.width,
    color: opts.color || '#fff',
    background: opts.background || 'var(--dj-gradient-primary)',
    ...pickSize(opts)
  }
}

/**
 * 危险按钮 — 红色填充
 * 用于：删除、退出登录、注销账号
 */
export function dangerBtn(opts = {}) {
  return {
    ...BASE,
    width: opts.width || BASE.width,
    color: opts.color || '#fff',
    background: opts.background || 'var(--dj-danger, #F56C6C)',
    ...pickSize(opts)
  }
}

/**
 * 灰底次按钮 — 用于"取消 / 关闭" 等非主动作
 */
export function ghostBtn(opts = {}) {
  return {
    ...BASE,
    width: opts.width || BASE.width,
    color: opts.color || 'var(--dj-text-secondary, #606266)',
    background: opts.background || '#f5f6ff',
    ...pickSize(opts)
  }
}

/**
 * 描边按钮 — 透明背景 + 主色边框
 */
export function outlineBtn(opts = {}) {
  return {
    ...BASE,
    width: opts.width || BASE.width,
    color: opts.color || 'var(--dj-primary, #5E72F7)',
    background: opts.background || 'transparent',
    border: opts.border || '2rpx solid var(--dj-primary, #5E72F7)',
    ...pickSize(opts)
  }
}

/**
 * 禁用态 — hint：保持原 type 的 size，但灰色填充 + 不可点击观感
 * 用法：buttonLoading 状态切换时也可以用这个
 */
export function disabledBtn(opts = {}) {
  return {
    ...BASE,
    width: opts.width || BASE.width,
    color: opts.color || '#fff',
    background: opts.background || '#E0E0E0',
    ...pickSize(opts)
  }
}

/** 兼容旧 default export：默认导出 primaryBtn */
export default primaryBtn

/**
 * 平台 Modal 调用工具 — 替换 uni.showModal
 * ---------------------------------------------------
 * 调用方式（与 uni.showModal API 完全兼容）：
 *
 *   // 回调式（最小改动，替换原 uni.showModal 只需把方法名改了）
 *   this.$appModal({
 *     title: '确认',
 *     content: '确认要退出登录吗？',
 *     success: res => { if (res.confirm) doLogout() }
 *   })
 *
 *   // Promise 式
 *   const { confirm } = await this.$appModal({ title, content })
 *   if (confirm) doLogout()
 *
 * 扩展参数：
 *   type:      default / danger / success / warning   // 顶部彩条 + 主按钮颜色
 *   maskClosable: 点击遮罩关闭（默认 false）
 *
 * 实现：发 uni.$emit 给挂在 App.vue 的 <AppModal> 单例
 */

export function showAppModal(options = {}) {
	return new Promise((resolve) => {
		const originalSuccess = options.success
		const originalComplete = options.complete

		const opts = {
			...options,
			success(res) {
				if (typeof originalSuccess === 'function') originalSuccess(res)
				resolve(res)
			},
			complete(res) {
				if (typeof originalComplete === 'function') originalComplete(res)
			},
		}

		uni.$emit('app-modal:open', opts)
	})
}

/** 立即关闭弹窗（强制场景，比如路由变更） */
export function closeAppModal() {
	uni.$emit('app-modal:close')
}

export default showAppModal

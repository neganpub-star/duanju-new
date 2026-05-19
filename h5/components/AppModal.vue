<template>
	<view v-if="visible" class="app-modal__mask" :class="{ 'is-show': showAnim }" @click="onMaskTap">
		<view class="app-modal__card" :class="{ 'is-show': showAnim }" @click.stop>
			<!-- 顶部彩条 -->
			<view class="app-modal__bar" :class="'app-modal__bar--' + type"></view>

			<!-- 标题区 -->
			<view v-if="title" class="app-modal__title">{{ title }}</view>

			<!-- 内容区 -->
			<view class="app-modal__content" v-if="content">
				<text class="app-modal__text">{{ content }}</text>
			</view>

			<!-- 按钮区 -->
			<view class="app-modal__actions">
				<view
					v-if="showCancel"
					class="app-modal__btn app-modal__btn--cancel"
					@click="onCancel"
				>{{ cancelText || $t('common.cancel') }}</view>
				<view
					class="app-modal__btn"
					:class="['app-modal__btn--' + type, { 'app-modal__btn--full': !showCancel }]"
					@click="onConfirm"
				>{{ confirmText || $t('common.confirm') }}</view>
			</view>
		</view>
	</view>
</template>

<script>
/**
 * 平台统一 Modal 组件 — 替换 uni.showModal 默认弹窗
 * 单例挂在 App.vue，通过 uni.$emit/uni.$on 通信。
 * 业务调用：this.$appModal({ title, content, success(res) }) 或 await this.$appModal(...).
 */
export default {
	name: 'AppModal',
	data() {
		return {
			visible: false,
			showAnim: false,
			title: '',
			content: '',
			confirmText: '',
			cancelText: '',
			showCancel: true,
			type: 'default',         // default / danger / success / warning
			maskClosable: false,
			_resolve: null,
			_options: null,
		}
	},
	mounted() {
		uni.$on('app-modal:open', this.openModal)
		uni.$on('app-modal:close', this.closeImmediately)
	},
	beforeDestroy() {
		uni.$off('app-modal:open', this.openModal)
		uni.$off('app-modal:close', this.closeImmediately)
	},
	methods: {
		openModal(opts) {
			this.title       = opts.title || ''
			this.content     = opts.content || ''
			this.confirmText = opts.confirmText || ''
			this.cancelText  = opts.cancelText || ''
			this.showCancel  = opts.showCancel !== false
			this.type        = opts.type || 'default'
			this.maskClosable = !!opts.maskClosable
			this._options    = opts
			this.visible     = true
			// 下一帧打动画类（避免初始就有 transform）
			this.$nextTick(() => { this.showAnim = true })
		},
		onConfirm() {
			this._triggerResult({ confirm: true, cancel: false })
		},
		onCancel() {
			this._triggerResult({ confirm: false, cancel: true })
		},
		onMaskTap() {
			if (this.maskClosable) this.onCancel()
		},
		_triggerResult(res) {
			const opts = this._options || {}
			// 关弹窗（带动画）
			this.showAnim = false
			setTimeout(() => {
				this.visible = false
			}, 200)
			// 回调（兼容 uni.showModal 的 success / fail / complete 签名）
			if (typeof opts.success === 'function') opts.success(res)
			if (typeof opts.complete === 'function') opts.complete(res)
		},
		closeImmediately() {
			this.showAnim = false
			this.visible = false
		},
	}
}
</script>

<style lang="scss" scoped>
.app-modal__mask {
	position: fixed;
	left: 0; right: 0; top: 0; bottom: 0;
	background: rgba(0, 0, 0, 0.5);
	display: flex;
	align-items: center;
	justify-content: center;
	z-index: 999;
	opacity: 0;
	transition: opacity 0.2s ease;

	&.is-show { opacity: 1; }
}

.app-modal__card {
	width: 600rpx;
	max-width: 86%;
	background: $dj-bg-base;
	border-radius: 28rpx;
	overflow: hidden;
	box-shadow: 0 24rpx 80rpx rgba(0, 0, 0, 0.18);
	transform: scale(0.85) translateY(20rpx);
	opacity: 0;
	transition: transform 0.25s cubic-bezier(0.34, 1.56, 0.64, 1), opacity 0.2s ease;

	&.is-show {
		transform: scale(1) translateY(0);
		opacity: 1;
	}
}

/* 顶部彩条 */
.app-modal__bar {
	height: 6rpx;

	&--default { background: var(--dj-gradient-primary); }
	&--danger  { background: linear-gradient(90deg, #ff7676 0%, #f5576c 100%); }
	&--success { background: linear-gradient(90deg, #43e97b 0%, #38f9d7 100%); }
	&--warning { background: linear-gradient(90deg, #fa8c16 0%, #fa709a 100%); }
}

/* 标题 */
.app-modal__title {
	padding: 44rpx 40rpx 12rpx;
	font-size: 34rpx;
	font-weight: 700;
	color: $dj-text-primary;
	text-align: center;
	line-height: 1.3;
}

/* 内容 */
.app-modal__content {
	padding: 8rpx 40rpx 36rpx;
	max-height: 56vh;
	overflow-y: auto;
}

.app-modal__text {
	display: block;
	font-size: 28rpx;
	color: $dj-text-secondary;
	text-align: center;
	line-height: 1.55;
	word-break: break-word;
}

/* 按钮区 */
.app-modal__actions {
	display: flex;
	border-top: 1rpx solid $dj-border-light;
}

.app-modal__btn {
	flex: 1;
	height: 100rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 30rpx;
	font-weight: 600;
	transition: background 0.15s;
	-webkit-tap-highlight-color: transparent;

	&:active {
		background: rgba(0, 0, 0, 0.04);
	}

	&--cancel {
		color: $dj-text-secondary;
		border-right: 1rpx solid $dj-border-light;
	}

	&--default {
		color: $dj-primary;
		font-weight: 700;
	}

	&--danger  { color: $dj-danger;  font-weight: 700; }
	&--success { color: $dj-success; font-weight: 700; }
	&--warning { color: $dj-warning; font-weight: 700; }

	&--full {
		border-right: none;
	}
}
</style>

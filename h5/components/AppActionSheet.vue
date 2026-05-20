<template>
	<view v-if="visible" class="app-action-mask" :class="{ 'is-show': showAnim }" @click="onMaskTap">
		<view class="app-action-panel" :class="{ 'is-show': showAnim }" @click.stop>
			<!-- 标题区 -->
			<view v-if="title" class="app-action-title">{{ title }}</view>

			<!-- 选项列表 -->
			<view class="app-action-list">
				<view
					v-for="(item, idx) in items"
					:key="idx"
					class="app-action-item"
					:class="{ 'is-active': item.value === activeValue, 'is-danger': item.danger }"
					@click="onTap(item, idx)"
				>
					<view v-if="item.icon" class="app-action-icon">
						<text>{{ item.icon }}</text>
					</view>
					<text class="app-action-label">{{ item.label }}</text>
					<view v-if="item.value === activeValue" class="app-action-check">
						<svg class="app-action-check-svg" viewBox="0 0 24 24" fill="none">
							<path d="M5 12l5 5L20 7" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
						</svg>
					</view>
				</view>
			</view>

			<!-- 间隔 -->
			<view class="app-action-gap"></view>

			<!-- 取消按钮（独立卡） -->
			<view class="app-action-cancel" @click="onCancel">
				{{ cancelText || $t('common.cancel') }}
			</view>
		</view>
	</view>
</template>

<script>
/**
 * 平台统一 ActionSheet — 替换 uni.showActionSheet
 * 用法：
 *   <AppActionSheet
 *     :show.sync="visible"
 *     title="语言设置"
 *     :items="[{ value: 'zh-CN', label: '简体中文', icon: '🇨🇳' }, ...]"
 *     :active-value="currentLang"
 *     @select="onSelect"
 *   />
 */
export default {
	name: 'AppActionSheet',
	props: {
		show: { type: Boolean, default: false },
		title: { type: String, default: '' },
		items: { type: Array, default: () => [] },     // [{ value, label, icon?, danger? }]
		activeValue: { type: [String, Number], default: null },
		cancelText: { type: String, default: '' },
		maskClosable: { type: Boolean, default: true },
	},
	data() {
		return {
			visible: false,
			showAnim: false,
		}
	},
	watch: {
		show: {
			immediate: true,
			handler(val) {
				if (val) this.open()
				else this.close()
			}
		}
	},
	methods: {
		open() {
			this.visible = true
			this.$nextTick(() => { this.showAnim = true })
		},
		close() {
			this.showAnim = false
			setTimeout(() => { this.visible = false }, 220)
		},
		onTap(item, idx) {
			this.$emit('select', item, idx)
			this.$emit('update:show', false)
		},
		onCancel() {
			this.$emit('cancel')
			this.$emit('update:show', false)
		},
		onMaskTap() {
			if (this.maskClosable) this.onCancel()
		}
	}
}
</script>

<style lang="scss" scoped>
.app-action-mask {
	position: fixed;
	left: 0; right: 0; top: 0; bottom: 0;
	background: rgba(0, 0, 0, 0.45);
	display: flex;
	flex-direction: column;
	justify-content: flex-end;
	/* 必须高于 CustomTabBar / 任何业务页 z-index */
	z-index: 9999;
	opacity: 0;
	transition: opacity 0.22s ease;

	&.is-show { opacity: 1; }
}

.app-action-panel {
	/* 底部加大留白：跨过 iPhone 安全区 + 防止被 TabBar 视觉抢位 */
	padding: 0 16rpx;
	padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
	transform: translateY(100%);
	transition: transform 0.28s cubic-bezier(0.32, 0.72, 0, 1);

	&.is-show { transform: translateY(0); }
}

/* 标题（带顶部彩条） */
.app-action-title {
	background: $dj-bg-base;
	color: $dj-text-secondary;
	font-size: 26rpx;
	font-weight: 600;
	text-align: center;
	padding: 28rpx 32rpx 20rpx;
	border-radius: 24rpx 24rpx 0 0;
	border-bottom: 1rpx solid $dj-border-light;
	position: relative;

	/* 顶部主题色短线（高级感细节） */
	&::before {
		content: '';
		position: absolute;
		left: 50%;
		top: 12rpx;
		transform: translateX(-50%);
		width: 56rpx;
		height: 6rpx;
		border-radius: 999rpx;
		background: var(--dj-gradient-primary);
		opacity: 0.45;
	}
}

/* 选项列表 */
.app-action-list {
	background: $dj-bg-base;
	border-radius: 0 0 24rpx 24rpx;
	overflow: hidden;

	/* 没有标题时，第一项要圆角 */
	&:first-child {
		border-radius: 24rpx;
	}
}

.app-action-item {
	display: flex;
	align-items: center;
	gap: 16rpx;
	padding: 32rpx 32rpx;
	font-size: 32rpx;
	color: $dj-text-primary;
	font-weight: 500;
	position: relative;
	transition: background 0.15s;
	-webkit-tap-highlight-color: transparent;

	&:not(:last-child)::after {
		content: '';
		position: absolute;
		left: 32rpx;
		right: 32rpx;
		bottom: 0;
		height: 1rpx;
		background: $dj-border-lighter;
	}

	&:active {
		background: $dj-bg-soft;
	}

	&.is-active {
		color: $dj-primary;
		font-weight: 700;
	}

	&.is-danger {
		color: $dj-danger;
	}
}

.app-action-icon {
	width: 44rpx;
	height: 44rpx;
	display: inline-flex;
	align-items: center;
	justify-content: center;
	font-size: 32rpx;
	flex-shrink: 0;
}

.app-action-label {
	flex: 1;
	min-width: 0;
}

.app-action-check {
	width: 36rpx;
	height: 36rpx;
	color: $dj-primary;
	display: inline-flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
}
.app-action-check-svg {
	width: 100%;
	height: 100%;
	display: block;
}

/* 间隔 */
.app-action-gap {
	height: 16rpx;
}

/* 取消按钮 */
.app-action-cancel {
	background: $dj-bg-base;
	border-radius: 24rpx;
	text-align: center;
	padding: 32rpx 0;
	font-size: 32rpx;
	font-weight: 600;
	color: $dj-text-secondary;
	transition: background 0.15s;
	-webkit-tap-highlight-color: transparent;

	&:active {
		background: $dj-bg-soft;
	}
}
</style>

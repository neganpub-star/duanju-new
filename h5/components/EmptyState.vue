<template>
	<view class="empty-state" :class="['empty-state--' + size]">
		<view class="empty-state__icon" :style="{ background: iconBg }">
			<text class="empty-state__emoji">{{ emoji }}</text>
		</view>
		<view class="empty-state__text">{{ text || defaultText }}</view>
		<view v-if="description" class="empty-state__desc">{{ description }}</view>
		<view v-if="actionText" class="empty-state__action" @click="$emit('action')">
			{{ actionText }}
		</view>
	</view>
</template>

<script>
/**
 * 平台统一空态组件
 * --------------------------------
 * 替换原来散落的 .empty-tip / .empty-box / u-empty 三种实现。
 *
 * 用法：
 *   <EmptyState />                                  → 默认"暂无数据"
 *   <EmptyState type="search" />                    → "未找到相关内容"
 *   <EmptyState type="network" />                   → "加载失败"
 *   <EmptyState text="还没有提现记录" />            → 自定义文案
 *   <EmptyState type="search" :description="..." /> → 加副文案
 *   <EmptyState size="mini" />                      → 紧凑版（列表内嵌用）
 *   <EmptyState actionText="去看看" @action="..." /> → 带 CTA 按钮
 */
const TYPE_MAP = {
	data:    { emoji: '📭', bg: 'rgba(94, 114, 247, 0.08)', textKey: 'common.noData' },
	search:  { emoji: '🔍', bg: 'rgba(94, 114, 247, 0.08)', textKey: 'common.noResults' },
	network: { emoji: '⚠️', bg: 'rgba(245, 108, 108, 0.10)', textKey: 'common.loadFailed' },
	team:    { emoji: '👥', bg: 'rgba(67, 233, 123, 0.10)', textKey: 'team.noData' },
	wallet:  { emoji: '💰', bg: 'rgba(255, 215, 0, 0.10)',  textKey: 'wallet.noRecord' },
	comment: { emoji: '💬', bg: 'rgba(94, 114, 247, 0.08)', textKey: 'comment.noComment' },
}

export default {
	name: 'EmptyState',
	props: {
		type: { type: String, default: 'data' },           // data / search / network / team / wallet / comment
		text: { type: String, default: '' },               // 自定义主文案（不填则按 type 走 i18n）
		description: { type: String, default: '' },        // 副文案（如"试试别的关键词"）
		size: { type: String, default: 'normal' },         // normal / mini
		actionText: { type: String, default: '' },         // 可选 CTA 按钮文字
		emojiOverride: { type: String, default: '' },      // 自定义 emoji
	},
	computed: {
		typeConfig() {
			return TYPE_MAP[this.type] || TYPE_MAP.data
		},
		emoji() {
			return this.emojiOverride || this.typeConfig.emoji
		},
		iconBg() {
			return this.typeConfig.bg
		},
		defaultText() {
			const key = this.typeConfig.textKey
			// 走 i18n，找不到 key 时回退到中文兜底
			const fallback = { 'common.noData': '暂无数据', 'common.noResults': '未找到相关内容', 'common.loadFailed': '加载失败' }
			return this.$t(key) !== key ? this.$t(key) : (fallback[key] || '暂无数据')
		}
	}
}
</script>

<style lang="scss" scoped>
.empty-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 80rpx 40rpx;

	&__icon {
		width: 140rpx;
		height: 140rpx;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		margin-bottom: 24rpx;
		transition: transform 0.4s ease;
	}

	&__emoji {
		font-size: 64rpx;
		line-height: 1;
	}

	&__text {
		font-size: 28rpx;
		font-weight: 600;
		color: $dj-text-secondary;
		margin-bottom: 8rpx;
	}

	&__desc {
		font-size: 24rpx;
		color: $dj-text-tertiary;
		text-align: center;
		max-width: 480rpx;
		line-height: 1.5;
	}

	&__action {
		margin-top: 24rpx;
		padding: 12rpx 32rpx;
		border-radius: 24rpx;
		background: var(--dj-gradient-primary);
		color: $dj-text-inverse;
		font-size: 24rpx;
		font-weight: 600;
	}

	/* 紧凑版（列表内嵌、抽屉内用） */
	&--mini {
		padding: 40rpx 20rpx;

		.empty-state__icon {
			width: 96rpx;
			height: 96rpx;
			margin-bottom: 16rpx;
		}

		.empty-state__emoji { font-size: 44rpx; }
		.empty-state__text { font-size: 26rpx; }
		.empty-state__desc { font-size: 22rpx; }
	}
}
</style>

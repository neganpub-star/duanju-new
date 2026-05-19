
<template>
	<view class="page_content">
		<!-- #ifndef MP-TOUTIAO -->
		<view class="head_content">
			<CustomNavbar :title="$t('points.myPoints')"></CustomNavbar>
		</view>
		<!-- #endif -->

		<view class="main_content">
			<scroll-view class="scroll_view" :scroll-y="true" @scrolltolower="scrollBottom">
				<view class="scroll_content">

					<!-- 余额卡片 -->
					<view class="balance-card">
						<view class="card-deco"></view>
						<view class="card-left">
							<view class="card-label">
								<view class="label-dot"></view>
								<text class="label-text">{{ $t('points.currentPoints') }}</text>
							</view>
							<view class="card-amount-row">
								<text class="card-amount">{{ userInfo.usable || 0 }}</text>
								<text class="card-unit">{{ $t('videopay.pointsUnit') }}</text>
							</view>
							<view class="card-hint">{{ $t('points.neverExpire') }}</view>
						</view>
						<view class="card-btn" v-if="iosIsPay" @click="recharge">
							<text>{{ $t('points.rechargeNow') }}</text>
						</view>
					</view>

					<!-- 明细列表 -->
					<view class="detail-section">
						<view class="section-header">
							<text class="section-title">{{ $t('points.history') }}</text>
						</view>

						<view class="list-box" v-if="list.length">
							<view class="list-item" v-for="(item, index) in list" :key="index">
								<view class="item-body">
									<text class="item-name">{{ walletLogText(item) }}</text>
									<text class="item-time">{{ formatTime(item.createTime || item.createtime) }}</text>
								</view>
								<view class="item-right">
									<text class="item-amount" :class="item.wallet > 0 ? 'amount-income' : 'amount-expense'">
										{{ item.wallet > 0 ? '+' : '' }}{{ item.wallet }}
									</text>
									<text class="item-unit">{{ $t('videopay.pointsUnit') }}</text>
								</view>
							</view>
						</view>

						<EmptyState v-else type="wallet" />

					</view>

				</view>
			</scroll-view>
		</view>
	</view>
</template>

<script>
	import { mapGetters } from "vuex"
	import EmptyState from '@/components/EmptyState.vue'
	export default {
		components: { EmptyState },
		data() {
			return {
				list: [],
				page: 1,
			}
		},
		computed: {
			...mapGetters("user", ["userInfo"]),
			...mapGetters("app", ["iosIsPay"]),
		},
		onShow() {
			this.list = []
			this.page = 1
			this.integralList()
		},
		methods: {
			walletLogText(log) {
				const type = log.type || 'default'
				let param = ''
				if (log.item_id) {
					const match = String(log.item_id).match(/\d+/)
					if (match) param = match[0]
				}
				const keyMap = {
					'episode_unlock': param
						? this.$t('wallet.type.episode_unlock').replace('{0}', param)
						: (log.memo || this.$t('wallet.type.episode_unlock').replace('{0}集', '').trim()),
					'usable_recharge': this.$t('wallet.type.usable_recharge').replace('{0}', Math.abs(log.wallet) || ''),
					'admin_recharge': this.$t('wallet.type.admin_recharge'),
					'withdraw': this.$t('wallet.type.withdraw'),
					'withdraw_reject': this.$t('wallet.type.withdraw_reject'),
					'vip_recharge': this.$t('wallet.type.vip_recharge'),
					'reseller_buy': this.$t('wallet.type.reseller_buy'),
				}
				return keyMap[type] || log.memo || this.$t('wallet.type.default')
			},
			formatTime(str) {
				if (!str) return ''
				// "2026-05-17 18:36:20" → "05-17 18:36"
				return str.replace(/^\d{4}-/, '').replace(/:\d{2}$/, '')
			},
			scrollBottom() {
				this.page++
				this.integralList()
			},
			integralList() {
				this.$request('integral.record', { wallet_type: 'usable', page: this.page }).then(res => {
					if (res.code === 1) {
						if (res.data && res.data.length) {
							this.list = this.list.concat(res.data)
						} else {
							this.page--
						}
					}
				})
			},
			recharge() {
				this.jumpView('/pages/user/integral/recharge')
			}
		}
	}
</script>

<style lang="scss" scoped>
.page_content {
	background: #F7F8FC;
	min-height: 100vh;

	.main_content {
		overflow: hidden;

		.scroll_view {
			height: 100%;

			.scroll_content {
				padding: 32rpx 32rpx 80rpx;
			}
		}
	}
}

// ── 余额卡片 ──────────────────────────────────────────
.balance-card {
	position: relative;
	overflow: hidden;
	background: $dj-gradient-primary;
	border-radius: 24rpx;
	padding: 48rpx 40rpx;
	display: flex;
	align-items: center;
	justify-content: space-between;
	box-shadow: 0 8rpx 32rpx rgba(94, 114, 247, 0.35);

	.card-deco {
		position: absolute;
		right: -40rpx;
		top: -40rpx;
		width: 240rpx;
		height: 240rpx;
		border-radius: 50%;
		background: rgba(255, 255, 255, 0.08);
		pointer-events: none;

		&::after {
			content: '';
			position: absolute;
			right: 40rpx;
			bottom: -60rpx;
			width: 160rpx;
			height: 160rpx;
			border-radius: 50%;
			background: rgba(255, 255, 255, 0.06);
		}
	}

	.card-left {
		position: relative;
		color: #fff;

		.card-label {
			display: flex;
			align-items: center;
			gap: 10rpx;
			margin-bottom: 20rpx;

			.label-dot {
				width: 12rpx;
				height: 12rpx;
				border-radius: 50%;
				background: rgba(255, 255, 255, 0.7);
			}

			.label-text {
				font-size: 26rpx;
				opacity: 0.85;
			}
		}

		.card-amount-row {
			display: flex;
			align-items: baseline;
			gap: 8rpx;

			.card-amount {
				font-size: 72rpx;
				font-weight: 900;
				line-height: 1;
				letter-spacing: -2rpx;
			}

			.card-unit {
				font-size: 28rpx;
				opacity: 0.8;
				font-weight: 500;
			}
		}

		.card-hint {
			font-size: 22rpx;
			opacity: 0.6;
			margin-top: 16rpx;
		}
	}

	.card-btn {
		position: relative;
		background: rgba(255, 255, 255, 0.95);
		color: $dj-primary;
		font-size: 26rpx;
		font-weight: 700;
		padding: 16rpx 32rpx;
		border-radius: 40rpx;
		white-space: nowrap;
		box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.12);
	}
}

// ── 快捷操作 ──────────────────────────────────────────
.quick-actions {
	display: flex;
	align-items: center;
	background: #fff;
	border-radius: 20rpx;
	margin-top: 24rpx;
	padding: 32rpx 0;
	box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);

	.action-item {
		flex: 1;
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 16rpx;

		.action-icon {
			width: 80rpx;
			height: 80rpx;
			border-radius: 50%;
			display: flex;
			align-items: center;
			justify-content: center;

			&.recharge-icon {
				background: rgba(94, 114, 247, 0.08);
			}

			&.history-icon {
				background: rgba(147, 84, 255, 0.08);
			}

			svg {
				width: 44rpx;
				height: 44rpx;
			}
		}

		.action-label {
			font-size: 26rpx;
			color: #333;
		}
	}

	.action-divider {
		width: 2rpx;
		height: 60rpx;
		background: #f0f0f0;
	}
}

// ── 明细列表 ──────────────────────────────────────────
.detail-section {
	margin-top: 24rpx;
	background: #fff;
	border-radius: 20rpx;
	box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
	overflow: hidden;

	.section-header {
		padding: 36rpx 40rpx 24rpx;
		border-bottom: 2rpx solid #f5f5f5;

		.section-title {
			font-size: 32rpx;
			font-weight: 700;
			color: #1a1a2e;
		}
	}

	.list-box {
		.list-item {
			display: flex;
			align-items: center;
			padding: 32rpx 40rpx;
			gap: 24rpx;
			border-bottom: 2rpx solid #f8f8f8;

			&:last-child {
				border-bottom: none;
			}

			.item-icon {
				width: 72rpx;
				height: 72rpx;
				border-radius: 50%;
				display: flex;
				align-items: center;
				justify-content: center;
				flex-shrink: 0;

				&.icon-income {
					background: rgba(103, 194, 58, 0.1);

					.icon-text {
						color: #67c23a;
						font-size: 36rpx;
						font-weight: 700;
					}
				}

				&.icon-expense {
					background: rgba(245, 108, 108, 0.1);

					.icon-text {
						color: #f56c6c;
						font-size: 36rpx;
						font-weight: 700;
					}
				}
			}

			.item-body {
				flex: 1;
				min-width: 0;
				display: flex;
				flex-direction: column;
				gap: 8rpx;

				.item-name {
					font-size: 30rpx;
					font-weight: 600;
					color: #1a1a2e;
				}

				.item-time {
					font-size: 24rpx;
					color: #aaa;
				}
			}

			.item-right {
				display: flex;
				flex-direction: column;
				align-items: flex-end;
				flex-shrink: 0;

				.item-amount {
					font-size: 34rpx;
					font-weight: 700;
					line-height: 1;

					&.amount-income { color: #67c23a; }
					&.amount-expense { color: #f56c6c; }
				}

				.item-unit {
					font-size: 22rpx;
					color: #aaa;
					margin-top: 6rpx;
				}
			}
		}
	}

	.empty-box {
		padding: 80rpx 0;
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 24rpx;

		.empty-icon {
			width: 120rpx;
			height: 120rpx;

			svg {
				width: 100%;
				height: 100%;
			}
		}

		.empty-text {
			font-size: 28rpx;
			color: #bbb;
		}
	}
}
</style>

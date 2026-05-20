

<template>
	<view class="page_content">
		<!-- #ifndef MP-TOUTIAO -->
		<view class="head_content">
			<CustomNavbar :title="$t('share.title')"></CustomNavbar>
		</view>
		<!-- #endif -->

		<view class="main_content">
			<!-- 收益卡片 -->
			<view class="earn_card">
				<view class="card_top">
					<view class="balance_block">
						<view class="balance_label">{{ $t('share.totalIncome') }}</view>
						<view class="balance_amount"><text class="amount_num">{{ info.total }}</text><text class="amount_unit">{{ $t('share.yuan') }}</text></view>
					</view>
					<view class="withdraw_btn" @click="jumpView('/pages/user/share/withdraw')">{{ $t('share.withdraw') }}</view>
				</view>
				<view class="card_divider"></view>
				<view class="card_bottom">
					<view class="stat_item">
						<view class="stat_label">{{ $t('share.normalUser') }}</view>
						<view class="stat_value level_tag">{{ info.levelText || $t('share.normalUser') }}</view>
					</view>
					<view class="stat_item">
						<view class="stat_label">{{ $t('share.directRate') }}{{ $t('share.commission') }}</view>
						<view class="stat_value">{{ Number(info.zhitui) }}%</view>
					</view>
					<view class="stat_item">
						<view class="stat_label">{{ $t('share.indirectRate') }}{{ $t('share.commission') }}</view>
						<view class="stat_value">{{ Number(info.jiantui) }}%</view>
					</view>
				</view>
			</view>

			<!-- 功能菜单 -->
			<view class="menu_grid">
				<!-- #ifndef APP-PLUS -->
				<view class="menu_item active" @click="$store.state.user.token && jumpView('/pages/user/share/poster')">
					<view class="menu_icon">
						<image class="icon_img" src="/static/img/分享赚钱.png" mode="aspectFit"></image>
					</view>
					<view class="menu_label">{{ $t('share.earnMoney') }}</view>
				</view>
				<!-- #endif -->
				<view class="menu_item" @click="jumpView('/pages/user/share/team')">
					<view class="menu_icon">
						<image class="icon_img" src="/static/img/团队管理.png" mode="aspectFit"></image>
					</view>
					<view class="menu_label">{{ $t('share.teamManage') }}</view>
				</view>
				<view class="menu_item" @click="jumpView('/pages/user/share/brokerage')">
					<view class="menu_icon">
						<image class="icon_img" src="/static/img/佣金明细.png" mode="aspectFit"></image>
					</view>
					<view class="menu_label">{{ $t('share.commissionDetail') }}</view>
				</view>
				<view class="menu_item" @click="jumpView('/pages/user/dealer/index')">
					<view class="menu_icon reseller_icon">
						<image class="icon_img" src="/static/img/reseller.png" mode="aspectFit"></image>
					</view>
					<view class="menu_label">{{ $t('share.reseller') }}</view>
				</view>
			</view>

			<!-- 说明内容 -->
			<view class="desc_box" v-if="info.content">
				<u-parse :content="info.content"></u-parse>
			</view>
			<view class="desc_box desc_placeholder" v-else>
				<view class="desc_title">{{ $t('share.inviteDesc') }}</view>
				<view class="desc_item">
					<text class="desc_icon">📢</text>
					<text class="desc_text">{{ $t('share.directRate') }}{{ $t('share.commission') }}：{{ Number(info.zhitui) }}%</text>
				</view>
				<view class="desc_item">
					<text class="desc_icon">🔗</text>
					<text class="desc_text">{{ $t('share.indirectRate') }}{{ $t('share.commission') }}：{{ Number(info.jiantui) }}%</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	import { mapGetters } from 'vuex'
	export default {
		computed: {
			...mapGetters('user', ['token'])
		},
		data() {
			return {
				info: {
					total: 0,
					level: 0,
					levelText: '',
					zhitui: 0,
					jiantui: 0,
					content: '',
				}
			}
		},
		onLoad() {
			if (!this.$store.state.user.token) {
				uni.navigateTo({ url: '/pages/login/login' })
				return
			}
			this.dealerInfo()
		},
		methods: {
			dealerInfo() {
				this.$request('dealer.info').then(res => {
					if (res.code === 1) {
						this.info.total = res.data.money || 0
						this.info.content = res.data.reseller_desc?.content || ''
						if (res.data.reseller) {
							this.info.level = res.data.reseller.level
							this.info.levelText = res.data.reseller.reseller_json?.name || ''
							this.info.zhitui = res.data.reseller.reseller_json?.direct || 0
							this.info.jiantui = res.data.reseller.reseller_json?.indirect || 0
						}
					}
				})
			}
		}
	}
</script>

<style lang="scss" scoped>
	.page_content {
		background: #f5f6ff;
		min-height: 100vh;

		.main_content {
			padding: 24rpx 28rpx 60rpx;

			/* 收益卡片 */
			.earn_card {
				background: $dj-gradient-primary;
				border-radius: 28rpx;
				padding: 40rpx 36rpx 32rpx;
				box-shadow: 0 8rpx 32rpx rgba(94, 114, 247, 0.35);
				position: relative;
				overflow: hidden;

				&::before {
					content: '';
					position: absolute;
					top: -60rpx;
					right: -60rpx;
					width: 280rpx;
					height: 280rpx;
					border-radius: 50%;
					background: rgba(255, 255, 255, 0.06);
				}

				.card_top {
					display: flex;
					align-items: center;
					justify-content: space-between;
					margin-bottom: 28rpx;

					.balance_block {
						.balance_label {
							font-size: $dj-fs-sm;
							color: rgba(255, 255, 255, 0.75);
							margin-bottom: $dj-spacing-xs;
						}

						.balance_amount {
							display: flex;
							align-items: baseline;
							gap: 6rpx;

							.amount_num {
								font-size: 56rpx;
								font-weight: 900;
								color: #fff;
								line-height: 1;
							}

							.amount_unit {
								font-size: 26rpx;
								color: rgba(255, 255, 255, 0.85);
								font-weight: 600;
							}
						}
					}

					.withdraw_btn {
						background: rgba(255, 255, 255, 0.22);
						border: 1.5rpx solid rgba(255, 255, 255, 0.5);
						border-radius: 40rpx;
						padding: 14rpx 36rpx;
						font-size: $dj-fs-base;
						font-weight: 700;
						color: #fff;
						backdrop-filter: blur(8rpx);
					}
				}

				.card_divider {
					height: 1rpx;
					background: rgba(255, 255, 255, 0.2);
					margin-bottom: 28rpx;
				}

				.card_bottom {
					display: flex;
					justify-content: space-between;

					.stat_item {
						display: flex;
						flex-direction: column;
						align-items: center;
						gap: $dj-spacing-xs;

						.stat_label {
							font-size: 22rpx;
							color: rgba(255, 255, 255, 0.7);
						}

						.stat_value {
							font-size: $dj-fs-base;
							font-weight: 700;
							color: #fff;

							&.level_tag {
								background: rgba(255, 255, 255, 0.2);
								border-radius: 20rpx;
								padding: 4rpx 16rpx;
								font-size: $dj-fs-sm;
							}
						}
					}
				}
			}

			/* 功能菜单 */
			.menu_grid {
				display: flex;
				flex-wrap: wrap;
				gap: $dj-spacing-sm;
				margin: 28rpx 0;

				.menu_item {
					width: calc((100% - 32rpx) / 3);
					background: #fff;
					border-radius: 24rpx;
					padding: 28rpx 16rpx;
					display: flex;
					flex-direction: column;
					align-items: center;
					gap: 14rpx;
					border: 1rpx solid rgba(94, 114, 247, 0.18);
					box-shadow: 0 4rpx 18rpx rgba(94, 114, 247, 0.12);

					&.active {
						background: $dj-gradient-primary;
						border-color: transparent;
						box-shadow: 0 4rpx 20rpx rgba(94, 114, 247, 0.35);

						.menu_label {
							color: #fff;
						}
					}

					.menu_icon {
						width: 80rpx;
						height: 80rpx;
						display: flex;
						align-items: center;
						justify-content: center;

						.icon_img {
							width: 100%;
							height: 100%;
						}

						&.reseller_icon {
							background: $dj-gradient-primary;
							border-radius: 50%;
							padding: 14rpx;
							width: 80rpx;
							height: 80rpx;
							box-shadow: 0 4rpx 12rpx rgba(94, 114, 247, 0.3);
						}
					}

					.menu_label {
						font-size: $dj-fs-sm;
						font-weight: 600;
						color: #333;
						text-align: center;
					}
				}
			}

			/* 说明内容 */
			.desc_box {
				background: #fff;
				border-radius: 24rpx;
				padding: 32rpx 28rpx;
				box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.06);
				font-size: 26rpx;
				color: #555;

				&.desc_placeholder {
					.desc_title {
						font-size: $dj-fs-md;
						font-weight: 700;
						color: #222;
						margin-bottom: $dj-spacing-base;
						padding-left: 12rpx;
						border-left: 6rpx solid $dj-primary;
					}

					.desc_item {
						display: flex;
						align-items: center;
						gap: $dj-spacing-sm;
						padding: 16rpx 0;
						border-bottom: 1rpx solid #f0f0f0;

						&:last-child {
							border-bottom: none;
						}

						.desc_icon {
							font-size: $dj-fs-lg;
						}

						.desc_text {
							font-size: $dj-fs-base;
							color: #444;
						}
					}
				}
			}
		}
	}
</style>

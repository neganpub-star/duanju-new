

<template>
	<view class="page_content">
		<!-- #ifndef MP-TOUTIAO -->
		<view class="head_content">
			<CustomNavbar :title="$t('brokerage.title')"></CustomNavbar>
		</view>
		<!-- #endif -->

		<view class="main_content">
			<scroll-view class="scroll_view" :scroll-y="true" @scrolltolower="scrollBottom">
				<view class="scroll_content">
					<!-- 统计卡片 -->
					<view class="top_card">
						<view class="stat_amount dj-num">{{ info.sum }}<text class="unit">{{ $t('brokerage.yuan') }}</text></view>
						<view class="stat_orders">{{ $t('brokerage.totalOrders', [info.count]) }}</view>
					</view>

					<!-- 佣金列表 -->
					<view class="content_box">
						<view class="section_title">{{ $t('brokerage.commissionInfo') }}</view>
						<view class="list_box" v-if="list.length">
							<view class="item" v-for="(item, index) in list" :key="index">
								<view class="item_top">
									<text class="nickname">{{ item.nickname }}</text>
									<text class="relation_tag">{{ item.type_text }}</text>
								</view>
								<view class="item_row">
									<view class="item_col">
										<text class="col_label">{{ $t('brokerage.commission') }}</text>
										<text class="col_value accent">{{ item.money }}{{ $t('brokerage.yuan') }}</text>
									</view>
									<view class="item_col">
										<text class="col_label">{{ $t('brokerage.rechargeAmount') }}</text>
										<text class="col_value">{{ item.pay_money }}{{ $t('brokerage.yuan') }}</text>
									</view>
								</view>
								<view class="item_footer">
									<text class="item_type">{{ item.order_type_text }}</text>
									<text class="item_time">{{ item.createtime }}</text>
								</view>
							</view>
						</view>
						<EmptyState v-else type="wallet" size="mini" :text="$t('brokerage.noData')" />
					</view>
				</view>
			</scroll-view>
		</view>
	</view>
</template>

<script>
	import EmptyState from '@/components/EmptyState.vue'
	export default {
		components: { EmptyState },
		data() {
			return {
				info: {
					sum: 0,
					count: 0
				},
				list: [],
				page: 1,
				pagesize: 10
			}
		},
		onLoad() {
			this.brokerageList()
		},
		methods: {
			scrollBottom() {
				this.page++
				this.brokerageList()
			},
			brokerageList() {
				this.$request('share.brokerage', {
					page: this.page,
					pagesize: this.pagesize
				}).then(res => {
					if (res.code === 1) {
						this.info = {
							sum: res.data.sum,
							count: res.data.count
						}
						if (res.data.list && res.data.list.length) {
							this.list = this.list.concat(res.data.list)
						} else {
							this.page--
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
			height: calc(100vh - 88rpx);
			overflow: hidden;

			.scroll_view {
				height: 100%;

				.scroll_content {
					padding: 24rpx 28rpx 60rpx;
				}
			}

			.top_card {
				background: $dj-gradient-primary;
				border-radius: 28rpx;
				padding: $dj-spacing-lg;
				box-shadow: 0 8rpx 32rpx rgba(94, 114, 247, 0.35);
				display: flex;
				flex-direction: column;
				align-items: center;
				margin-bottom: $dj-spacing-base;

				.stat_amount {
					font-size: 64rpx;
					font-weight: 900;
					color: #fff;
					line-height: 1;
					margin-bottom: $dj-spacing-sm;

					.unit {
						font-size: $dj-fs-base;
						font-weight: 600;
						margin-left: 4rpx;
					}
				}

				.stat_orders {
					font-size: 26rpx;
					color: rgba(255, 255, 255, 0.8);
				}
			}

			.content_box {
				background: #fff;
				border-radius: 24rpx;
				padding: $dj-spacing-md;
				box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.06);

				.section_title {
					font-size: $dj-fs-md;
					font-weight: 700;
					color: #1a1a1a;
					margin-bottom: $dj-spacing-base;
					padding-left: 12rpx;
					border-left: 6rpx solid $dj-primary;
				}

				.list_box {
					.item {
						padding: 24rpx 0;
						border-bottom: 1rpx solid #f5f5f5;

						&:last-child { border-bottom: none; }

						.item_top {
							display: flex;
							align-items: center;
							justify-content: space-between;
							margin-bottom: $dj-spacing-sm;

							.nickname {
								font-size: $dj-fs-md;
								font-weight: 700;
								color: #1a1a1a;
							}

							.relation_tag {
								font-size: 22rpx;
								color: $dj-primary;
								background: #eef0ff;
								padding: 4rpx 16rpx;
								border-radius: 20rpx;
							}
						}

						.item_row {
							display: flex;
							gap: $dj-spacing-md;
							margin-bottom: 12rpx;

							.item_col {
								display: flex;
								align-items: center;
								gap: $dj-spacing-xs;

								.col_label {
									font-size: $dj-fs-sm;
									color: #999;
								}

								.col_value {
									font-size: $dj-fs-base;
									font-weight: 600;
									color: #333;

									&.accent { color: $dj-primary; }
								}
							}
						}

						.item_footer {
							display: flex;
							align-items: center;
							justify-content: space-between;

							.item_type {
								font-size: 22rpx;
								color: #aaa;
							}

							.item_time {
								font-size: 22rpx;
								color: #aaa;
							}
						}
					}
				}

				.empty_tip {
					text-align: center;
					font-size: $dj-fs-base;
					color: #bbb;
					padding: 60rpx 0;
				}
			}
		}
	}
</style>

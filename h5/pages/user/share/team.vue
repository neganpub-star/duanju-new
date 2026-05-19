

<template>
	<view class="page_content">
		<!-- #ifndef MP-TOUTIAO -->
		<view class="head_content">
			<CustomNavbar :title="$t('team.title')"></CustomNavbar>
		</view>
		<!-- #endif -->

		<view class="main_content">
			<scroll-view class="scroll_view" :scroll-y="true" @scrolltolower="scrollBottom">
				<view class="scroll_content">
					<!-- 统计卡片 -->
					<view class="top_card">
						<view class="stat_total">
							<text class="stat_num">{{ info.count }}</text>
							<text class="stat_unit">{{ $t('team.person') }}</text>
						</view>
						<view class="stat_sub">
							<text class="sub_item">{{ $t('team.directCount', [info.count_direct]) }}</text>
							<text class="sub_divider">·</text>
							<text class="sub_item">{{ $t('team.indirectCount', [info.count_indirect]) }}</text>
						</view>
					</view>

					<!-- 团队列表 -->
					<view class="content_box">
						<view class="section_title">{{ $t('team.teamInfo') }}</view>
						<view class="list_box" v-if="list.length">
							<view class="item" v-for="(item, index) in list" :key="index">
								<view class="item_left">
									<image class="avatar" :src="item.avatar" mode="aspectFill"></image>
									<view class="item_info">
										<view class="nickname">{{ item.nickname }}</view>
										<view class="join_time">{{ item.createtime }}</view>
									</view>
								</view>
								<view class="type_tag" :class="item.type == '1' ? 'direct' : 'indirect'">
									{{ item.type == '1' ? $t('team.direct') : $t('team.indirect') }}
								</view>
							</view>
						</view>
						<EmptyState v-else type="team" size="mini" :text="$t('team.noData')" />
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
					count: 0,
					count_direct: 0,
					count_indirect: 0
				},
				list: [],
				page: 1,
				pagesize: 10
			}
		},
		onLoad() {
			this.teamList()
		},
		methods: {
			scrollBottom() {
				this.page++
				this.teamList()
			},
			teamList() {
				this.$request('share.team', {
					page: this.page,
					pagesize: this.pagesize
				}).then(res => {
					if (res.code === 1) {
						this.info = {
							count: res.data.count,
							count_direct: res.data.count_direct,
							count_indirect: res.data.count_indirect
						}
						if (res.data.reseller_user && res.data.reseller_user.length) {
							this.list = this.list.concat(res.data.reseller_user)
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
				padding: 40rpx;
				box-shadow: 0 8rpx 32rpx rgba(94, 114, 247, 0.35);
				display: flex;
				flex-direction: column;
				align-items: center;
				margin-bottom: 24rpx;

				.stat_total {
					display: flex;
					align-items: baseline;
					gap: 8rpx;
					margin-bottom: 16rpx;

					.stat_num {
						font-size: 72rpx;
						font-weight: 900;
						color: #fff;
						line-height: 1;
					}

					.stat_unit {
						font-size: 28rpx;
						font-weight: 600;
						color: rgba(255, 255, 255, 0.85);
					}
				}

				.stat_sub {
					display: flex;
					align-items: center;
					gap: 16rpx;

					.sub_item {
						font-size: 26rpx;
						color: rgba(255, 255, 255, 0.8);
					}

					.sub_divider {
						font-size: 26rpx;
						color: rgba(255, 255, 255, 0.4);
					}
				}
			}

			.content_box {
				background: #fff;
				border-radius: 24rpx;
				padding: 32rpx;
				box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.06);

				.section_title {
					font-size: 30rpx;
					font-weight: 700;
					color: #1a1a1a;
					margin-bottom: 24rpx;
					padding-left: 12rpx;
					border-left: 6rpx solid $dj-primary;
				}

				.list_box {
					.item {
						display: flex;
						align-items: center;
						justify-content: space-between;
						padding: 24rpx 0;
						border-bottom: 1rpx solid #f5f5f5;

						&:last-child { border-bottom: none; }

						.item_left {
							display: flex;
							align-items: center;
							gap: 20rpx;

							.avatar {
								width: 88rpx;
								height: 88rpx;
								border-radius: 20rpx;
								flex-shrink: 0;
							}

							.item_info {
								.nickname {
									font-size: 30rpx;
									font-weight: 700;
									color: #1a1a1a;
									margin-bottom: 8rpx;
								}

								.join_time {
									font-size: 22rpx;
									color: #aaa;
								}
							}
						}

						.type_tag {
							font-size: 24rpx;
							font-weight: 600;
							padding: 6rpx 20rpx;
							border-radius: 20rpx;

							&.direct {
								background: #eef0ff;
								color: $dj-primary;
							}

							&.indirect {
								background: #f3eeff;
								color: $dj-primary-deep;
							}
						}
					}
				}

				.empty_tip {
					text-align: center;
					font-size: 28rpx;
					color: #bbb;
					padding: 60rpx 0;
				}
			}
		}
	}
</style>

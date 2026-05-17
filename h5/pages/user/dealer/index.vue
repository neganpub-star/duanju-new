

<template>
	<view class="page_content">
		<!-- #ifndef MP-TOUTIAO -->
		<view class="head_content">
			<CustomNavbar :title="$t('dealer.title')"></CustomNavbar>
		</view>
		<!-- #endif -->

		<view class="main_content">
			<!-- 用户信息卡片 -->
			<view class="user_card">
				<view class="user_row">
					<image v-if="userInfo.avatar" class="avatar" :src="userInfo.avatar" mode="aspectFill"></image>
					<view v-else class="avatar avatar_placeholder">
						<text class="avatar_initial">{{ (userInfo.nickname || 'U').charAt(0).toUpperCase() }}</text>
					</view>
					<view class="user_info">
						<view class="user_greeting" v-if="level == 0">{{ $t('dealer.greetUser') }}</view>
						<view class="user_greeting" v-else>{{ $t('dealer.greetReseller', [userInfo.levelText]) }}</view>
						<view class="user_desc" v-if="level == 0">{{ $t('dealer.notReseller') }}</view>
						<view class="user_desc" v-else>{{ $t('dealer.alreadyReseller', [userInfo.levelText]) }}</view>
					</view>
				</view>
				<view class="status_row">
					<view class="status_badge">
						<text class="badge_text" v-if="level == 0">{{ $t('dealer.platformUser') }}</text>
						<text class="badge_text" v-else>{{ userInfo.levelText }}</text>
					</view>
					<text class="expire_text">{{ level == 0 ? $t('dealer.permanent') : userInfo.expireText }}</text>
				</view>
			</view>

			<!-- 等级套餐 -->
			<view class="section_card" v-if="levelData.length">
				<view class="section_title">{{ $t('dealer.levelCategory') }}</view>
				<view class="level_grid">
					<view
						class="level_item"
						:class="{ active: item.level == dredgeLevel }"
						v-for="(item, index) in levelData"
						:key="index"
						@click="levelCardClick(item)"
					>
						<view class="level_name">{{ item.expire }}{{ $t('common.days') }}</view>
						<view class="level_price">
							<text class="currency">¥</text>
							<text class="price_num">{{ item.price }}</text>
						</view>
						<view class="level_rate">{{ $t('dealer.directRate', [Number(item.direct)]) }}</view>
						<view class="level_rate">{{ $t('dealer.indirectRate', [Number(item.indirect)]) }}</view>
						<view class="level_tag">{{ item.name }}</view>
					</view>
				</view>
			</view>

			<!-- 说明内容 -->
			<view class="desc_card" v-if="msg">
				<u-parse :content="msg"></u-parse>
			</view>
		</view>

		<view class="footer_content" v-if="levelData.length">
			<u-button
				:text="$t('dealer.activateNow')"
				v-if="dredgeLevel != 0"
				:loading="buttonLoading"
				:customStyle="buttonStyle"
				@click="dredgeDealer"
			/>
		</view>
	</view>
</template>

<script>
	import { mapGetters, mapActions } from "vuex"
	export default {
		data() {
			return {
				buttonStyle: {
					width: '100%',
					height: '100rpx',
					border: 'none',
					fontSize: '32rpx',
					color: '#fff',
					background: 'linear-gradient(90deg, #5E72F7 0%, #9354FF 100%)',
					borderRadius: '16rpx',
					margin: '0',
					fontWeight: 'bold'
				},
				buttonLoading: false,
				msg: '',
				levelData: [],
				level: 0,
				dredgeLevel: 0,
				maxLevel: 0,
				dredge: { resellerId: null },
				userInfo: {}
			}
		},
		computed: {
			...mapGetters("user", ["token"]),
			...mapGetters("app", ["iosIsPay"])
		},
		onLoad() {
			this.getPageData()
		},
		methods: {
			...mapActions("user", ["getUserInfo"]),
			levelCardClick(item) {
				this.dredgeLevel = item.level
				this.dredge.resellerId = item.id
			},
			dealerLevelList() {
				this.$request('dealer.level').then(res => {
					if (res.code === 1) {
						this.levelData = res.data.list || []
						this.msg = res.data.reseller_desc && res.data.reseller_desc.content || ''
						const levels = this.levelData.map(o => o.level)
						this.maxLevel = levels.length ? Math.max(...levels) : 0
						this.dredgeLevel = this.level + 1 > this.maxLevel ? this.maxLevel : this.level + 1
						const selected = this.levelData.find(item => item.level === this.dredgeLevel)
						if (selected) this.dredge.resellerId = selected.id
					}
				})
			},
			getPageData() {
				this.getUserInfo().then(res => {
					if (res.code === 1) {
						this.userInfo = res.data
						this.level = res.data.reseller_level || 0
						if (res.data.reseller_level > 0 && res.data.reseller_expire_time) {
							this.userInfo.levelText = this.$t('dealer.resellerLevel', [res.data.reseller_level])
							this.userInfo.expireText = res.data.reseller_expire_time
						}
						this.dealerLevelList()
					}
				})
			},
			dredgeDealer() {
				// #ifdef MP-WEIXIN
				if (!this.iosIsPay) return this.jumpView('/pages/user/info/contact')
				// #endif
				if (!this.dredge.resellerId) return this.$u.toast(this.$t('dealer.selectPlan'))
				const doBuy = () => {
					this.buttonLoading = true
					uni.showLoading({ title: this.$t('dealer.activating'), mask: true })
					this.$request('dealer.createOrder', {
						resellerId: this.dredge.resellerId,
						payType: 'wechat',
						platform: this.$utils.platforms()
					}).then(res => {
						uni.hideLoading()
						this.buttonLoading = false
						if (res.code === 1) {
							this.handlePayResult(res.data)
						} else {
							uni.showToast({ title: res.msg || this.$t('payment.orderFailed'), icon: 'none' })
						}
					}).catch(() => {
						uni.hideLoading()
						this.buttonLoading = false
					})
				}
				if (this.dredgeLevel < this.level) {
					uni.showModal({
						title: this.$t('dealer.tip'),
						content: this.$t('dealer.lowerLevelTip'),
						success: r => { if (r.confirm) doBuy() }
					})
				} else {
					doBuy()
				}
			},
			handlePayResult(data) {
				if (data.status === 'paid') {
					uni.showToast({ title: this.$t('dealer.activateSuccess'), icon: 'success' })
					this.getPageData()
					return
				}
				if (data.h5Url) {
					window.location.href = data.h5Url
					return
				}
				if (data.timeStamp && typeof WeixinJSBridge !== 'undefined') {
					WeixinJSBridge.invoke('getBrandWCPayRequest', {
						appId: data.appId, timeStamp: data.timeStamp,
						nonceStr: data.nonceStr, package: data.package,
						signType: data.signType, paySign: data.paySign
					}, res => {
						if (res.err_msg === 'get_brand_wcpay_request:ok') {
							uni.showToast({ title: this.$t('dealer.activateSuccess'), icon: 'success' })
							this.getPageData()
						} else {
							uni.showToast({ title: this.$t('dealer.payCancel'), icon: 'none' })
						}
					})
					return
				}
				if (data.payError) {
					uni.showToast({ title: this.$t('dealer.payNotConfigured'), icon: 'none', duration: 3000 })
				} else {
					uni.showToast({ title: this.$t('dealer.activateSuccess'), icon: 'success' })
					this.getPageData()
				}
			}
		}
	}
</script>

<style lang="scss" scoped>
	.page_content {
		background: #f5f6ff;
		min-height: 100vh;

		.main_content {
			padding: 24rpx 28rpx 160rpx;

			.user_card {
				background: linear-gradient(135deg, #5E72F7 0%, #9354FF 100%);
				border-radius: 28rpx;
				padding: 36rpx 32rpx 28rpx;
				box-shadow: 0 8rpx 32rpx rgba(94, 114, 247, 0.35);
				margin-bottom: 24rpx;

				.user_row {
					display: flex;
					align-items: center;
					gap: 24rpx;
					margin-bottom: 28rpx;

					.avatar {
						width: 96rpx;
						height: 96rpx;
						border-radius: 50%;
						border: 3rpx solid rgba(255, 255, 255, 0.5);
						flex-shrink: 0;
					}

					.user_info {
						flex: 1;

						.user_greeting {
							font-size: 30rpx;
							font-weight: 700;
							color: #fff;
							margin-bottom: 8rpx;
						}

						.user_desc {
							font-size: 24rpx;
							color: rgba(255, 255, 255, 0.75);
						}
					}
				}

				.status_row {
					display: flex;
					align-items: center;
					justify-content: space-between;
					padding-top: 20rpx;
					border-top: 1rpx solid rgba(255, 255, 255, 0.2);

					.status_badge {
						background: rgba(255, 255, 255, 0.2);
						border-radius: 20rpx;
						padding: 6rpx 20rpx;

						.badge_text {
							font-size: 24rpx;
							font-weight: 700;
							color: #fff;
						}
					}

					.expire_text {
						font-size: 24rpx;
						color: rgba(255, 255, 255, 0.75);
					}
				}
			}

			.section_card {
				background: #fff;
				border-radius: 24rpx;
				padding: 32rpx;
				box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.06);
				margin-bottom: 24rpx;

				.section_title {
					font-size: 30rpx;
					font-weight: 700;
					color: #1a1a1a;
					margin-bottom: 28rpx;
					padding-left: 12rpx;
					border-left: 6rpx solid #5E72F7;
				}

				.level_grid {
					display: flex;
					flex-wrap: wrap;
					gap: 16rpx;

					.level_item {
						position: relative;
						width: calc((100% - 32rpx) / 3);
						border-radius: 20rpx;
						padding: 56rpx 16rpx 28rpx;
						border: 2rpx solid #e8eaff;
						background: #f8f9ff;

						&.active {
							background: linear-gradient(135deg, #5E72F7 0%, #9354FF 100%);
							border-color: transparent;
							box-shadow: 0 4rpx 20rpx rgba(94, 114, 247, 0.35);

							.level_name, .level_rate {
								color: rgba(255, 255, 255, 0.85);
							}

							.level_price {
								color: #fff;
								.price_num { color: #fff; }
							}
						}

						.level_name {
							font-size: 24rpx;
							color: #888;
							margin-bottom: 8rpx;
						}

						.level_price {
							display: flex;
							align-items: baseline;
							gap: 4rpx;
							margin: 10rpx 0;
							color: #5E72F7;

							.currency {
								font-size: 28rpx;
								font-weight: 700;
							}

							.price_num {
								font-size: 56rpx;
								font-weight: 900;
								line-height: 1;
								color: #5E72F7;
							}
						}

						.level_rate {
							font-size: 22rpx;
							color: #666;
							line-height: 1.6;
						}

						.level_tag {
							position: absolute;
							top: 0;
							right: 0;
							background: linear-gradient(90deg, #5E72F7 0%, #9354FF 100%);
							color: #fff;
							font-size: 22rpx;
							font-weight: 700;
							padding: 6rpx 16rpx;
							border-radius: 0 20rpx 0 16rpx;
						}
					}
				}
			}

			.desc_card {
				background: #fff;
				border-radius: 24rpx;
				padding: 32rpx;
				box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.06);
				font-size: 26rpx;
				color: #555;
			}
		}

		.footer_content {
			position: fixed;
			bottom: 0;
			left: 0;
			width: 100%;
			padding: 20rpx 32rpx 40rpx;
			background: #fff;
			box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.06);
		}
	}
</style>

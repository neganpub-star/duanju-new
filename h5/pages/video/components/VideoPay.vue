

<template>
	<view class="VideoPay" :class="{ active: show }" @click="$emit('close')">
		<view class="popup" @click.stop="">
			<view class="p_head">
				<view class="left">
					<text class="text1">{{ $t('videopay.supportAuthor') }}</text>
				</view>
				<view class="right" :style="'color:'+isColor" @click="$emit('close')">{{ $t('common.close') }}</view>
			</view>
			<view class="p_text">
				<view class="left">
					<text class="text1">{{ $t('videopay.unlockEp') }}</text>
					<text class="text2" :style="'color:'+isColor">{{ $t('videopay.pricePoints', [price]) }}</text>
				</view>
				<view class="right">{{ $t('videopay.balance', [userInfoStore.usable || 0]) }}</view>
			</view>
			<view class="p_list">
				<!-- 点数不足：警示色提示 + 充值入口 -->
				<view class="p_recharge_tip" v-if="iosIsPay && (userInfoStore.usable || 0) < price" @click.stop="goRecharge">
					<view class="tip-left">
						<text class="tip-icon">⚡</text>
						<text class="tip-text">{{ $t('video.insufficientPoints') }}，{{ $t('videopay.rechargePoints') }}</text>
					</view>
					<text class="tip-arrow">›</text>
				</view>
				<!-- 余额够也常驻一个轻量充值入口（兜底，避免 VIP 区不渲染时用户无路可走） -->
				<view class="p_recharge_tip p_recharge_tip--soft" v-else-if="iosIsPay" @click.stop="goRecharge">
					<view class="tip-left">
						<text class="tip-icon">💎</text>
						<text class="tip-text">{{ $t('videopay.rechargeMore') }}</text>
					</view>
					<text class="tip-arrow">›</text>
				</view>

				<!-- VIP 套餐 -->
				<view class="p_section_header" v-if="iosIsPay && vipData.length">
					<view class="title">{{ $t('vip.selectPlan') }}</view>
				</view>
				<view class="p_vip_section" v-if="iosIsPay && vipData.length">
					<view class="vip-card" v-for="(item, index) in vipData" :key="'vip-'+index" @click="recharge('member', item.id, item.price)">
						<view class="vc-name">{{ vipPlanName(item.days) }}</view>
						<view class="vc-days" v-if="item.days">{{ item.days }}{{ $t('common.days') }}</view>
						<view class="vc-price">
							<text class="vc-currency">¥</text>
							<text class="vc-amount">{{ item.price }}</text>
						</view>
						<view class="vc-original" v-if="item.originalPrice && item.originalPrice !== item.price">{{ $t('vip.originalPrice') }}{{ item.originalPrice }}</view>
						<view class="vc-original" v-else></view>
						<view class="vc-btn">{{ $t('vip.activateNow') }}</view>
					</view>
				</view>
			</view>
			<view class="p_info">{{ $t('videopay.disclaimer') }}</view>
		</view>
	</view>
</template>

<script>
	import { mapState, mapGetters, mapMutations, mapActions } from "vuex"
	export default {
		name: "VideoPay",
		props: {
			show: {
				type: Boolean,
				default: false
			},
			price: {
				type: Number,
				default: 0
			},
			ad: {
				type: Boolean,
				default: false
			}
		},
		data() {
			return {
				userInfoStore: this.$store.state.user.userInfo,
				integralData: [],
				vipData: [],
				buttonLoading: true,
				isAdLoading: false,
				isColor: `$dj-primary-deep`,
			};
		},
		computed: {
			...mapGetters("user", ["token", "userInfo"]),
			...mapGetters("app", ["iosIsPay", "config"]),
		},
		watch: {
			userInfo: {
				deep: true,
				handler: function(newValue, oldValue) {
					this.userInfoStore = newValue
				}
			},
			show(newValue, oldValue) {
				if(newValue) {
					this.token && this.getUserInfo()
				}
			},
		},
		created() {
			this.isColor = `$dj-primary-deep`
			this.getIntegralList()
			this.getVipList()
			// 弹窗打开时刷新余额（v-if 每次重建组件，show watcher 不会触发初始值）
			this.token && this.getUserInfo()
		},
		methods: {
			...mapActions('user', ['getUserInfo']),
			// 根据天数返回当前语言的套餐名称
			vipPlanName(days) {
				if (!days) return this.$t('vip.activateVip')
				if (days <= 3)  return this.$t('vip.trialCard')
				if (days <= 7)  return this.$t('vip.weeklyVip')
				if (days <= 31) return this.$t('vip.monthlyVip')
				if (days <= 93) return this.$t('vip.quarterlyVip')
				return this.$t('vip.annualVip')
			},
			gotoPage(url) {
				if(!this.token) return this.$u.toast(this.$t('common.loginFirst'))
				// #ifdef MP-WEIXIN
				if(!this.iosIsPay) return this.jumpView('/pages/user/info/contact')
				// #endif
				this.jumpView(url)
			},
			goRecharge() {
				if(!this.token) return this.$u.toast(this.$t('common.loginFirst'))
				this.$emit('close')
				this.jumpView('/pages/user/integral/recharge')
			},
			// 获取积分套餐列表
			getIntegralList() {
				this.$request('integral.list').then(res => {
					if(res.code === 1) {
						res.data.list && res.data.list.length && (this.integralData = res.data.list)
					}
				})
			},
			// 获取vip套装列表
			getVipList() {
				this.$request('user.vip').then(res => {
					if(res.code === 1) {
						res.data.list && res.data.list.length && (this.vipData = res.data.list)
					}
				})
			},
			// 充值
			recharge(type, id, price) {
				if(!this.token) return this.$u.toast(this.$t('common.loginFirst'))
				
				// #ifdef MP-WEIXIN
				if(!this.iosIsPay) return this.jumpView('/pages/user/info/contact')
				// #endif
			
				if(this.buttonLoading) {
					this.buttonLoading = false
					uni.showLoading({
						title: this.$t('payment.activating'),
						mask: true
					})
						if(type === 'integral') {
							this.$request('integral.create', {
								usable_id: id,
								total_fee: price,
								platform: this.$utils.platforms()
							}).then(res => {
								uni.hideLoading()
								this.buttonLoading = true
								if(res.code === 1) {
									this.handlePayResult(res.data)
								} else {
									uni.showToast({ title: res.msg || this.$t('payment.orderFailed'), icon: 'none' })
								}
							}).catch(() => {
								uni.hideLoading()
								this.buttonLoading = true
							})
						} else if(type === 'member') {
							this.$request('order.create', {
								vip_id: id,
								total_fee: price,
								platform: this.$utils.platforms()
							}).then(res => {
								uni.hideLoading()
								this.buttonLoading = true
								if(res.code === 1) {
									this.handlePayResult(res.data)
								} else {
									uni.showToast({ title: res.msg || this.$t('payment.orderFailed'), icon: 'none' })
								}
							}).catch(() => {
								uni.hideLoading()
								this.buttonLoading = true
							})
						}
				}
			},
			handlePayResult(data) {
				if(data.h5Url) {
					window.location.href = data.h5Url
					return
				}
				if(data.timeStamp && typeof WeixinJSBridge !== 'undefined') {
					WeixinJSBridge.invoke('getBrandWCPayRequest', {
						appId: data.appId,
						timeStamp: data.timeStamp,
						nonceStr: data.nonceStr,
						package: data.package,
						signType: data.signType,
						paySign: data.paySign
					}, (res) => {
						if(res.err_msg === 'get_brand_wcpay_request:ok') {
							uni.showToast({ title: this.$t('payment.success'), icon: 'success' })
							this.getUserInfo()
							this.$emit('close')
						} else {
							uni.showToast({ title: this.$t('payment.payCancel'), icon: 'none' })
						}
					})
					return
				}
				if(data.payError) {
					uni.showToast({ title: this.$t('payment.notConfigured'), icon: 'none', duration: 3000 })
				} else {
					uni.showToast({ title: this.$t('payment.success'), icon: 'success' })
					this.getUserInfo()
					this.$emit('close')
				}
			},
		}
	}
</script>

<style lang="scss" scoped>
	.VideoPay {
		width: 100%;
		height: 100%;
		position: absolute;
		bottom: -100%;
		left: 0;
		z-index: 100;
		transition: all 0.5s;
		
		&.active {
			bottom: 0;
		}
		
		.popup {
			width: 100%;
			min-height: 60%;
			background: #f5f6ff;
			border-radius: 24rpx 24rpx 0 0;
			position: absolute;
			bottom: 0;
			left: 0;
			z-index: 1;
			color: #1a1a1a;
			padding: 0 40rpx 60rpx 40rpx;
			border-top: none;
			box-shadow: 0 -4rpx 30rpx rgba(94, 114, 247, 0.12);
			display: flex;
			flex-direction: column;

			// 顶部渐变色条，视觉与VIP中心统一
			&::before {
				content: '';
				display: block;
				height: 6rpx;
				background: linear-gradient(90deg, #6e7ff3 0%, $dj-primary-deep 100%);
				border-radius: 24rpx 24rpx 0 0;
				margin: 0 -40rpx;
				margin-bottom: 0;
			}

			.p_head {
				display: flex;
				flex-direction: row;
				justify-content: space-between;
				position: relative;
				padding: 28rpx 0;
				font-size: 32rpx;

				.left {
					flex: 1;
					display: flex;
					flex-direction: row;

					.text1 {
						font-weight: 700;
						line-height: 48rpx;
						color: #1a1a1a;
					}

					.text2 {
						font-size: 24rpx;
						color: rgba(#1a1a1a, 0.4);
						margin: 0 40rpx;
						white-space: nowrap;
						line-height: 48rpx;
					}
				}

				.right {
					color: $dj-primary-deep;
					white-space: nowrap;
					margin-left: 20rpx;
				}
			}

			.p_text {
				display: flex;
				flex-direction: row;
				justify-content: space-between;
				align-items: center;
				font-size: 28rpx;
				padding: 18rpx 24rpx;
				background: #fff;
				border-radius: 14rpx;
				box-shadow: 0 2rpx 8rpx rgba(94, 114, 247, 0.08);

				.left {
					display: flex;
					flex-direction: row;
					align-items: center;

					.text2 {
						color: $dj-primary;
					}
				}

				.right {
					color: rgba(#1a1a1a, 0.5);
				}
			}

			.p_recharge_tip {
					display: flex;
					align-items: center;
					justify-content: space-between;
					width: 100%;
					background: rgba(245, 108, 108, 0.10);   // 警示底色（不足提醒）
					border: 1rpx solid rgba(245, 108, 108, 0.18);
					border-radius: 16rpx;
					padding: 22rpx 28rpx;
					margin-bottom: 16rpx;
					box-sizing: border-box;
					transition: opacity 0.2s, transform 0.2s;

					&:active {
						opacity: 0.85;
						transform: scale(0.99);
					}

					.tip-left {
						display: flex;
						align-items: center;
						gap: 12rpx;

						.tip-icon {
							font-size: 28rpx;
						}

						.tip-text {
							font-size: 26rpx;
							color: #c0392b;
							font-weight: 600;
						}
					}

					.tip-arrow {
						font-size: 30rpx;
						color: #c0392b;
						font-weight: 700;
					}

					/* 余额够时的轻量版：紫色软底 + 主色文字 */
					&.p_recharge_tip--soft {
						background: rgba(147, 84, 255, 0.08);
						border-color: rgba(147, 84, 255, 0.18);

						.tip-text { color: $dj-primary-deep; font-weight: 500; }
						.tip-arrow { color: $dj-primary-deep; }
					}
				}

			.p_section_header {
				display: flex;
				flex-direction: row;
				align-items: center;
				justify-content: space-between;
				width: 100%;
				margin-top: 24rpx;
				padding: 0 4rpx 12rpx 16rpx;
				border-left: 6rpx solid $dj-primary-deep;
				box-sizing: border-box;

				.title {
					font-size: 30rpx;
					font-weight: 600;
					color: $dj-primary-deep;
				}

				.recharge-link {
					font-size: 30rpx;
					font-weight: 600;
					color: $dj-primary-deep;
				}
			}

			.p_list {
				flex: 1;
				margin-top: 0;
				display: flex;
				flex-wrap: wrap;
				align-content: flex-start;
				
				.item {
					width: calc((100% - 40rpx) / 2);
					height: 168rpx;
					border-radius: 20rpx;
					background: #fff;
					margin-bottom: 40rpx;
					position: relative;
					overflow: hidden;
					color: #000;
					display: flex;
					flex-direction: column;
					
					&:nth-child(2n) {
						margin-left: 40rpx;
					}
					
					&.item2 {
						background: $dj-gradient-primary;
						color: #fff;
					}
					
					.content {
						flex: 1;
						display: flex;
						flex-direction: column;
						justify-content: center;
						// align-items: center;
						
						.line1 {
							display: flex;
							flex-direction: row;
							align-items: flex-end;
							justify-content: center;
							
							.text1 {
								font-size: 40rpx;
								font-weight: 700;
							}
							
							.text2 {
								font-size: 28rpx;
							}
						}
						
						.line2 {
							font-size: 28rpx;
							display: flex;
							flex-direction: row;
							align-items: center;
							justify-content: center;
							
							.text2 {
								color: $dj-primary;
							}
						}
					}
					
					.tips {
						font-size: 28rpx;
						text-align: center;
						background: $dj-gradient-primary;
						padding: 8rpx 0;
						color: #fff;
					}
					
					.badge {
						position: absolute;
						top: -2rpx;
						right: -2rpx;
						padding: 4rpx 16rpx;
						font-size: 24rpx;
						background: $dj-gradient-primary;
						border-radius: 0 0 0 20rpx;
						color: #fff;
					}
				}
			}
			
			.p_vip_section {
				width: 100%;
				display: flex;
				flex-wrap: wrap;

				.vip-card {
					width: calc((100% - 20rpx) / 2);
					min-height: 240rpx;
					margin-bottom: 20rpx;

					&:nth-child(2n) {
						margin-left: 20rpx;
					}
					border-radius: 20rpx;
					background: #fff;
					border: 1rpx solid #e8eaff;
					box-shadow: 0 4rpx 16rpx rgba(94, 114, 247, 0.1);
					position: relative;
					overflow: hidden;
					display: flex;
					flex-direction: column;
					align-items: center;
					padding: 24rpx 16rpx 20rpx;
					box-sizing: border-box;

					.vc-name {
						font-size: 28rpx;
						font-weight: 700;
						color: #1a1a1a;
						text-align: center;
					}

					.vc-days {
						font-size: 22rpx;
						color: #999;
						margin-top: 6rpx;
					}

					.vc-price {
						display: flex;
						align-items: flex-end;
						margin-top: 12rpx;

						.vc-currency {
							font-size: 26rpx;
							font-weight: 700;
							color: $dj-primary;
							padding-bottom: 8rpx;
						}

						.vc-amount {
							font-size: 56rpx;
							font-weight: 700;
							color: $dj-primary;
							line-height: 1;
						}
					}

					.vc-original {
						font-size: 22rpx;
						color: #bbb;
						text-decoration: line-through;
						margin-top: 6rpx;
						min-height: 30rpx;
					}

					.vc-btn {
						margin-top: auto;
						width: 100%;
						padding: 16rpx 0;
						border-radius: 40rpx;
						border: 2rpx solid $dj-primary;
						color: $dj-primary;
						font-size: 24rpx;
						text-align: center;
						font-weight: 600;
					}

					.vc-badge {
						position: absolute;
						top: 0;
						right: 0;
						padding: 4rpx 16rpx;
						font-size: 22rpx;
						background: $dj-gradient-primary;
						border-radius: 0 20rpx 0 20rpx;
						color: #fff;
					}
				}
			}

			.p_info {
				font-size: 24rpx;
				text-align: center;
				margin-top: 20rpx;
				color: rgba(#1a1a1a, 0.4);
			}
		}
	}
</style>
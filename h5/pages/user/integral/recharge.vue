
<template>
	<view class="page_content">
		<!-- #ifndef MP-TOUTIAO -->
		<view class="head_content">
			<CustomNavbar :title="$t('points.title')"></CustomNavbar>
		</view>
		<!-- #endif -->
		
		<view class="main_content">
			<view class="box1">
				<view class="box_title">{{ $t('points.neverExpire') }}</view>
				<view class="box_content">
					<view class="list_box">
						<view class="item" :class="{ active: item.id == integralActiveId }" v-for="(item, index) in integralData" :key="item.id" @click="integralItem(item.id, item.price)">
							<view class="item-top">
								<text class="pkg-title">{{ item.display_title }}</text>
								<view class="give-tag" v-if="item.giveUsable > 0">+{{ item.giveUsable }} {{ $t('points.bonus') }}</view>
							</view>
							<view class="item-divider"></view>
							<view class="item-bottom">
								<view class="price-row">
									<text class="price-symbol">¥</text>
									<text class="price-num">{{ item.price }}</text>
								</view>
								<view class="original-row" v-if="item.originalPrice && item.originalPrice > item.price">
									<text class="original-price">¥{{ item.originalPrice }}</text>
								</view>
							</view>
						</view>
					</view>
					<view class="button_box">
						<u-button :loading="!buttonLoading" :text="$t('points.rechargeNow')" :customStyle="buttonStyle" @click="createOrder"/>
					</view>
				</view>
			</view>
			<view class="box3">
				<view class="box_title">{{ $t('points.notices') }}</view>
				<view class="box_content">
					<view class="text_info">
						<u-parse :content="textInfo"></u-parse>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	import { mapState, mapGetters, mapMutations, mapActions } from "vuex"
	import { primaryBtn } from '@/common/utils/buttonStyle.js'
	export default {
		data() {
			return {
				buttonStyle: primaryBtn({ fontSize: '24rpx' }),
				buttonLoading: true,
				integralData: [],
				integralActiveId: 0,
				integralActivePrice: 0,
				textInfo: "",
			}
		},
		computed: {
			...mapGetters("user", ["token"]),
			...mapGetters("app", ["iosIsPay"]),
		},
		watch: {
			
		},
		onLoad() {
			this.getIntegralList()
		},
		methods: {
			...mapActions("user", ["getUserInfo"]),
			// 选择套餐
			integralItem(id, price) {
				this.integralActiveId = id
				this.integralActivePrice = price
			},
			// 获取积分套餐列表
			getIntegralList() {
				this.$request('integral.list').then(res => {
					if(res.code === 1) {
						if(res.data.list && res.data.list.length) {
							this.integralData = res.data.list
							this.integralActiveId = res.data.list[0].id
							this.integralActivePrice = res.data.list[0].price
						}
						res.data.usable_desc && (this.textInfo = res.data.usable_desc.content)
					}
				})
			}, 
			// 创建订单
			createOrder() {
				
				if(!this.integralActiveId || !this.integralActivePrice) return this.$u.toast(this.$t('points.buyPoints'))
				this.recharge(this.integralActiveId, this.integralActivePrice)
			},
			// 充值
			recharge(id, price) {
				// #ifdef MP-WEIXIN
				if(!this.iosIsPay) return this.jumpView('/pages/user/info/contact')
				// #endif
				if(!this.buttonLoading) return
				this.buttonLoading = false
				uni.showLoading({ title: this.$t('points.recharging'), mask: true })
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
						uni.showToast({ title: res.msg || this.$t('points.orderFailed'), icon: 'none' })
					}
				}).catch(() => {
					uni.hideLoading()
					this.buttonLoading = true
				})
			},
			handlePayResult(data) {
				if(data.h5Url) {
					window.location.href = data.h5Url
					return
				}
				if(data.timeStamp && typeof WeixinJSBridge !== 'undefined') {
					WeixinJSBridge.invoke('getBrandWCPayRequest', {
						appId: data.appId, timeStamp: data.timeStamp,
						nonceStr: data.nonceStr, package: data.package,
						signType: data.signType, paySign: data.paySign
					}, (res) => {
						if(res.err_msg === 'get_brand_wcpay_request:ok') {
							uni.showToast({ title: this.$t('points.rechargeDone'), icon: 'success' })
							this.getUserInfo && this.getUserInfo()
						} else {
							uni.showToast({ title: this.$t('points.payCancel'), icon: 'none' })
						}
					})
					return
				}
				if(data.payError) {
					uni.showToast({ title: this.$t('payment.notConfigured'), icon: 'none', duration: 3000 })
				} else {
					uni.showToast({ title: this.$t('payment.success'), icon: 'success' })
					this.getUserInfo && this.getUserInfo()
				}
			},
		}
	}
</script>

<style lang="scss" scoped>
	.page_content {
		.main_content {
			padding: 24rpx 20rpx 60rpx 20rpx;
			overflow-y: auto;
			
			.box_title {
				font-size: 30rpx;
				color: #000;
				font-weight: 700;
			}
			
			.box1 {
				margin-bottom: 36rpx;
				
				.box_content {
					.list_box {
						display: flex;
						flex-wrap: wrap;
						justify-content: space-between;
						margin: 40rpx 0 16rpx 0;
						
						.item {
							width: 224rpx;
							margin-bottom: 20rpx;
							border-radius: 16rpx;
							border: 2rpx solid #e5e7ff;
							box-shadow: 0 4rpx 16rpx rgba(94, 114, 247, 0.08);
							text-align: center;
							overflow: hidden;
							background: #fff;

							&.active {
								border-color: $dj-primary-deep;
								box-shadow: 0 4rpx 20rpx rgba(147, 84, 255, 0.2);

								.item-top {
									background: $dj-gradient-primary;

									.pkg-title {
										color: $dj-text-inverse;
									}
								}

								.price-row {
									.price-symbol, .price-num {
										background: $dj-gradient-primary;
										-webkit-background-clip: text;
										-webkit-text-fill-color: transparent;
										background-clip: text;
									}
								}
							}

							.item-top {
								background: linear-gradient(135deg, #f0f2ff 0%, #f5f0ff 100%);
								padding: 24rpx 16rpx 20rpx;
								display: flex;
								flex-direction: column;
								align-items: center;
								gap: 10rpx;

								.pkg-title {
									font-size: 32rpx;
									font-weight: 700;
									color: #333;
									line-height: 1.2;
								}

								.give-tag {
									font-size: 20rpx;
									color: #fff;
									background: linear-gradient(90deg, #ff6b35 0%, #ff4444 100%);
									padding: 4rpx 12rpx;
									border-radius: 20rpx;
									font-weight: 600;
								}
							}

							.item-divider {
								height: 1rpx;
								background: linear-gradient(90deg, transparent, #e5e7ff, transparent);
							}

							.item-bottom {
								padding: 16rpx 8rpx 20rpx;

								.price-row {
									display: flex;
									align-items: baseline;
									justify-content: center;
									gap: 2rpx;

									.price-symbol {
										font-size: 26rpx;
										font-weight: 600;
										color: #1a1a1a;
									}

									.price-num {
										font-size: 56rpx;
										font-weight: 800;
										color: #1a1a1a;
										line-height: 1.1;
									}
								}

								.original-row {
									margin-top: 6rpx;

									.original-price {
										font-size: 22rpx;
										color: #bbb;
										text-decoration: line-through;
									}
								}
							}
						}
					}
				}
			}
			
			.box3 {
				.box_content {
					margin-top: 24rpx;
					
					.text_info {
						border: 2rpx solid rgba(238, 238, 238, 1);
						font-size: 24rpx;
						color: #7F7F7F;
						padding: 24rpx 32rpx;
						border-radius: 20rpx;
						line-height: 36rpx;
					}
				}
			}
		}
	}
</style>

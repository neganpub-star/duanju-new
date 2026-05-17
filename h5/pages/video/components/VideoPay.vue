

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
				<!-- <view class="item">
					<view class="content">
						<view class="line1">
							<text class="text1">69.9</text>
							<text class="text2">元</text>
						</view>
						<view class="line2">
							<text class="text1">6900</text>
							<text class="text2">+2500积分</text>
						</view>
					</view>
					<view class="tips">多送30元</view>
					<view class="badge">特惠</view>
				</view> -->
				<!-- <view class="item item2">
					<view class="content">
						<view class="line1">
							<text class="text1">69.9</text>
							<text class="text2">元</text>
						</view>
						<view class="line2">
							<text class="text1">解锁整部剧</text>
							<text class="text2"></text>
						</view>
					</view>
				</view> -->
				
				<view class="item" v-if="iosIsPay" v-for="(item, index) in integralData" :key="index" @click="recharge('integral', item.id, item.price)">
					<view class="content">
						<view class="line1">
							<text class="text1">{{ item.price }}</text>
							<text class="text2">{{ $t('videopay.yuan') }}</text>
						</view>
						<view class="line2">
							<text class="text1">{{ item.original_usable }}</text>
							<text class="text2" v-if="item.give_usable">+{{ item.give_usable }}</text>
							<text class="text2">{{ $t('videopay.pointsUnit') }}</text>
						</view>
					</view>
					<view class="badge" v-if="item.flag">{{ item.flag }}</view>
				</view>
				<view class="p_section_header" v-if="iosIsPay && vipData.length">
					<view class="title">{{ $t('vip.selectPlan') }}</view>
					<view class="recharge-link" @click.stop="goRecharge">{{ $t('videopay.rechargePoints') }} ›</view>
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
						<view class="vc-badge" v-if="item.flag">{{ item.flag }}</view>
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
				isColor: `#9354FF`,
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
			this.isColor = `#9354FF`
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
					// #ifdef MP-TOUTIAO
						if(type === 'integral') {
							
							this.$request('integral.create', {
								usable_id: id,
								total_fee: price,
								platform: 'douyinxcx'
							}).then(res => {
								if(res.code === 1) {
									this.callPay(res.data.order_sn, 'douyinxcx', res.data.platform)
								} else {
									this.buttonLoading = true
								}
							}).catch(err => {
								this.buttonLoading = true
							})
						} else if(type === 'member') {
							this.$request('order.create', {
								vip_id: id,
								total_fee: price,
								platform: 'douyinxcx'
							}).then(res => {
								if(res.code === 1) {
									this.callPay(res.data.order_sn, 'douyinxcx', res.data.platform)
								} else {
									this.buttonLoading = true
								}
							}).catch(err => {
								this.buttonLoading = true
							})
						}
					// #endif
					// #ifndef MP-TOUTIAO
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
					// #endif
					
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
			xGetPay(order_sn,payment,platform){
				uni.login({
					provider: 'weixin',
					success: success => {
						if(success.errMsg === 'login:ok') {
							
							this.$request('common.xunipay', {
								order_sn,
								payment,
								platform,
								code: success.code
							}).then(res => {
								console.log(res)
								if(res.code === 1) {
									if(platform == 'H5') {
										const div = document.createElement('divpay');
										div.innerHTML = res.data.pay_data;
										document.body.appendChild(div);
									} else {
										this.xunipay(res.data.pay_data)
									}
								}
							})
							
						}
					}
				})
			},
			//非虚拟支付调起
			getPay(order_sn,payment,platform){
				this.$request('common.pay', {
					order_sn,
					payment,
					platform,
				}).then(res => {
					console.log(res)
					if(res.code === 1) {
						if(platform == 'H5') {
							const div = document.createElement('divpay');
							div.innerHTML = res.data.pay_data;
							document.body.appendChild(div);
						} else {
							this.pay(res.data.pay_data)
						}
					}
				})
			},
			// 获取支付参数
			callPay(order_sn, payment, platform) {
				// #ifdef MP-TOUTIAO
					var that = this
						this.$request('common.dypay', {
							order_sn,
							payment,
							platform,
						}).then(res => {
							console.log(res)
							if(res.code === 1) {
								
								if(platform == 'H5') {
									const div = document.createElement('divpay');
									div.innerHTML = res.data.pay_data;
									document.body.appendChild(div);
								} else {
									console.log('进入当前')
									tt.requestOrder({
									   data: res.data.data, // 请勿在前端对data做任何处理
									   byteAuthorization: res.data.byteAuthorization, // 请勿在前端对byteAuthorization做任何处理
									   success: (res) => {
									     console.log('成功', res);
										tt.getOrderPayment({
										 orderId:res.orderId,
										 success: (res) => {
											uni.showToast({
												  title: this.$t('payment.success'),
												icon: 'none',
												duration: 2000
											  });
											that.buttonLoading = true
											that.getUserInfo()
											uni.$emit('updateUserInfo', true)
											uni.hideLoading()
										 },
										 fail: (res) => {
										  uni.showToast({
										  		     title: this.$t('videopay.virtualPayNotSupported'),
										  		 	icon: 'none',
										  		 	duration: 2000
										  		 });
										  		that.buttonLoading = true
										  		uni.hideLoading()
										   
										 },
										});
									   },
									   fail: (res) => {
									   
									   that.buttonLoading = true
									   uni.hideLoading()
									     uni.showToast({
									     	title:res,
									     	icon: 'none',
									     	duration: 2000
									     })
										 
									   },
									 });
									// tt.pay({
									//   orderInfo: {
									//     order_id:  res.data.pay_data.data.order_id ,
									//     order_token:res.data.pay_data.data.order_token ,
									//   },
									//   service: 5,
									//   success(res) {
									// 	  console.log(res,'success')
									//     if (res.code == 0) {
									// 		uni.showToast({
									// 		      title: this.$t('payment.success'),
									// 		  	icon: 'none',
									// 		  	duration: 2000
									// 		  });
									// 		that.buttonLoading = true
									// 		that.getUserInfo()
									// 		uni.$emit('updateUserInfo', true)
									// 		uni.hideLoading()
											
									//       // 支付成功处理逻辑，只有res.code=0时，才表示支付成功
									//       // 但是最终状态要以商户后端结果为准
									//     }else{
									// 		uni.showToast({
									// 		     title: this.$t('videopay.virtualPayNotSupported'),
									// 		 	icon: 'none',
									// 		 	duration: 2000
									// 		 });
									// 		this.buttonLoading = true
									// 		uni.hideLoading()
									// 	}
									//   },
									//   fail(res) {
									// 	 uni.showToast({
									// 	      title: this.$t('videopay.virtualPayNotSupported'),
									// 	  	icon: 'none',
									// 	  	duration: 2000
									// 	  });
									// 	 this.buttonLoading = true
									// 	 uni.hideLoading()
									//     // 调起收银台失败处理逻辑
									//   },
									// });
								}
							}
						})
				
				// #endif
				// #ifndef MP-TOUTIAO
				this.$request('common.ifxunipay').then(res=>{
					if(res.data.xunipay_switch == 0){
						this.getPay(order_sn,payment,platform)
					}else{
						// #ifdef MP-WEIXIN
						var iosd = wx.getSystemInfoSync()
						if(this.iosIsPay && iosd.platform == 'ios'){
							this.getPay(order_sn,payment,platform)
							
						}else{
							this.xGetPay(order_sn,payment,platform)
							
						}
					// #endif
					// #ifndef MP-WEIXIN
					
						this.getPay(order_sn,payment,platform)
						
					// #endif
					}
				})
				// #endif
				
			},
			// 发起 小程序/公众号 支付
			xunipay(pay){
				var that = this
				// #ifdef MP-WEIXIN
				const SDKVersion = wx.getSystemInfoSync().SDKVersion
				
				if (this.compareVersion(SDKVersion, '2.19.2') >= 0 || wx.canIUse('requestVirtualPayment')) {
				  wx.requestVirtualPayment({
				    signData: JSON.stringify({
				      offerId: pay.signData.offerId,
				      buyQuantity: pay.signData.buyQuantity,
				      env: pay.signData.env,
				      currencyType: pay.signData.currencyType,
				      platform: pay.signData.platform,
				      productId: pay.signData.productId,
				      goodsPrice: pay.signData.goodsPrice,
				      outTradeNo: pay.signData.outTradeNo,
				      attach: pay.signData.attach,
				    }),
				    paySig: pay.paySig, 
				    signature: pay.signature,
				    mode: pay.mode,
				    success(res) {
				      //console.log('requestVirtualPayment success', res)
					  uni.showToast({
					      title: this.$t('payment.success'),
					  	icon: 'none',
					  	duration: 2000
					  });
					that.buttonLoading = true
					that.getUserInfo()
					uni.$emit('updateUserInfo', true)
					uni.hideLoading()
					
				    },
				    fail({ errMsg, errCode }) {
				      //console.error(errMsg, errCode)
					  uni.showToast({
					      title: this.$t('payment.failed'),
					  	icon: 'none',
					  	duration: 2000
					  });
					that.buttonLoading = true
					uni.hideLoading()
				    },
				  })
				} else {
				  //console.log('当前用户的客户端版本不支持 wx.requestVirtualPayment')
				  
				  uni.showToast({
				      title: this.$t('videopay.virtualPayNotSupported'),
				  	icon: 'none',
				  	duration: 2000
				  });
				 this.buttonLoading = true
				 uni.hideLoading()
				  
				}
				// #endif
				
				// // #ifdef H5
				
				// WeixinJSBridge.invoke(
				// 	'getBrandWCPayRequest', {
				// 		"appId": pay.appId, // 公众号ID，由商户传入     
				// 		"timeStamp": pay.timeStamp, // 时间戳，自1970年以来的秒数     
				// 		"nonceStr": pay.nonceStr, // 随机串     
				// 		"package": pay.package, // 订单详情扩展字符串
				// 		"signType": pay.signType, // 微信签名方式：     
				// 		"paySign": pay.paySign // 微信签名 
				// 	},
				// 	res => {
				// 		if (res.err_msg == "get_brand_wcpay_request:ok") {
				// 			uni.showToast({
				// 			    title: this.$t('payment.success'),
				// 				icon: 'none',
				// 				duration: 2000
				// 			});
				// 			this.buttonLoading = true
				// 			this.getUserInfo()
				// 			uni.$emit('updateUserInfo', true)
				// 			uni.hideLoading()
				// 		} else {
				// 			uni.showToast({
				// 			    title: this.$t('payment.failed'),
				// 				icon: 'none',
				// 				duration: 2000
				// 			});
				// 			this.buttonLoading = true
				// 			uni.hideLoading()
				// 		}
				// 	});
				// // #endif
				
				// // #ifdef APP-PLUS
				// // APP
				// 	uni.getProvider({
				// 		service: "payment",
				// 		success: e => {
				// 			const type = e.provider.includes('wxpay')
				// 			type && uni.requestPayment({
				// 				"provider": "wxpay",
				// 				"orderInfo": pay,
				// 				success: success => {
				// 					uni.showToast({
				// 						title: this.$t('payment.success'),
				// 						icon: 'none',
				// 						duration: 2000
				// 					});
								
				// 					this.buttonLoading = true
				// 					this.getUserInfo()
				// 					uni.$emit('updateUserInfo', true)
				// 					uni.hideLoading()
				// 				},
				// 				fail: fail => {
				// 					if(fail.errCode === -8) {
				// 						uni.showToast({
				// 							title: this.$t('videopay.wxNotInstalled'),
				// 							icon: 'none',
				// 							duration: 2000
				// 						});
				// 						this.buttonLoading = true
				// 						uni.hideLoading()
				// 					} else {
				// 						uni.showToast({
				// 							title: this.$t('payment.failed'),
				// 							icon: 'none',
				// 							duration: 2000
				// 						});
				// 						this.buttonLoading = true
				// 						uni.hideLoading()
				// 					}
				// 				}
				// 			})
				// 		},
				// 		fail: e => {
				// 			this.payFail("获取iap支付通道失败")
				// 		}
				// 	});
				// // #endif
			},
			pay(pay) {
						// #ifdef MP-WEIXIN
						uni.requestPayment({
							timeStamp: pay.timeStamp,
							nonceStr: pay.nonceStr,
							package: pay.package,
							signType: pay.signType,
							paySign: pay.paySign,
							success: success => {
								uni.showToast({
								    title: this.$t('payment.success'),
									icon: 'none',
									duration: 2000
								});
								this.buttonLoading = true
								this.getUserInfo()
								uni.$emit('updateUserInfo', true)
								uni.hideLoading()
							},
							fail: fail => {
								uni.showToast({
								    title: this.$t('payment.failed'),
									icon: 'none',
									duration: 2000
								});
								this.buttonLoading = true
								uni.hideLoading()
							}
						})
						// #endif
						
						// #ifdef H5
						WeixinJSBridge.invoke(
							'getBrandWCPayRequest', {
								"appId": pay.appId, // 公众号ID，由商户传入     
								"timeStamp": pay.timeStamp, // 时间戳，自1970年以来的秒数     
								"nonceStr": pay.nonceStr, // 随机串     
								"package": pay.package, // 订单详情扩展字符串
								"signType": pay.signType, // 微信签名方式：     
								"paySign": pay.paySign // 微信签名 
							},
							res => {
								if (res.err_msg == "get_brand_wcpay_request:ok") {
									uni.showToast({
									    title: this.$t('payment.success'),
										icon: 'none',
										duration: 2000
									});
									this.buttonLoading = true
									this.getUserInfo()
									uni.$emit('updateUserInfo', true)
									uni.hideLoading()
								} else {
									uni.showToast({
									    title: this.$t('payment.failed'),
										icon: 'none',
										duration: 2000
									});
									this.buttonLoading = true
									uni.hideLoading()
								}
							});
						// #endif
						// #ifdef APP-PLUS
						// APP
							uni.getProvider({
								service: "payment",
								success: e => {
									const type = e.provider.includes('wxpay')
									type && uni.requestPayment({
										"provider": "wxpay",
										"orderInfo": pay,
										success: success => {
											uni.showToast({
												title: this.$t('payment.success'),
												icon: 'none',
												duration: 2000
											});
										
											this.buttonLoading = true
											this.getUserInfo()
											uni.$emit('updateUserInfo', true)
											uni.hideLoading()
										},
										fail: fail => {
											if(fail.errCode === -8) {
												uni.showToast({
													title: this.$t('videopay.wxNotInstalled'),
													icon: 'none',
													duration: 2000
												});
												this.buttonLoading = true
												uni.hideLoading()
											} else {
												uni.showToast({
													title: this.$t('payment.failed'),
													icon: 'none',
													duration: 2000
												});
												this.buttonLoading = true
												uni.hideLoading()
											}
										}
									})
								},
								fail: e => {
									this.payFail("获取iap支付通道失败")
								}
							});
						// #endif
					
				
			},
			//判断微信js版本用
			compareVersion(_v1, _v2) {
			  if (typeof _v1 !== 'string' || typeof _v2 !== 'string') return 0
			
			  const v1 = _v1.split('.')
			  const v2 = _v2.split('.')
			  const len = Math.max(v1.length, v2.length)
			
			  while (v1.length < len) {
			    v1.push('0')
			  }
			  while (v2.length < len) {
			    v2.push('0')
			  }
			
			  for (let i = 0; i < len; i++) {
			    const num1 = parseInt(v1[i], 10)
			    const num2 = parseInt(v2[i], 10)
			
			    if (num1 > num2) {
			      return 1
			    } else if (num1 < num2) {
			      return -1
			    }
			  }
			
			  return 0
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
				background: linear-gradient(90deg, #6e7ff3 0%, #9354FF 100%);
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
					color: #9354FF;
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
						color: #5E72F7;
					}
				}

				.right {
					color: rgba(#1a1a1a, 0.5);
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
				border-left: 6rpx solid #9354FF;
				box-sizing: border-box;

				.title {
					font-size: 30rpx;
					font-weight: 600;
					color: #9354FF;
				}

				.recharge-link {
					font-size: 30rpx;
					font-weight: 600;
					color: #9354FF;
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
						background: linear-gradient(90deg, #5E72F7 0%, #9354FF 100%);
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
								color: #5E72F7;
							}
						}
					}
					
					.tips {
						font-size: 28rpx;
						text-align: center;
						background: linear-gradient(90deg, #5E72F7 0%, #9354FF 100%);
						padding: 8rpx 0;
						color: #fff;
					}
					
					.badge {
						position: absolute;
						top: -2rpx;
						right: -2rpx;
						padding: 4rpx 16rpx;
						font-size: 24rpx;
						background: linear-gradient(90deg, #5E72F7 0%, #9354FF 100%);
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
							color: #5E72F7;
							padding-bottom: 8rpx;
						}

						.vc-amount {
							font-size: 56rpx;
							font-weight: 700;
							color: #5E72F7;
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
						border: 2rpx solid #5E72F7;
						color: #5E72F7;
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
						background: linear-gradient(90deg, #5E72F7 0%, #9354FF 100%);
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
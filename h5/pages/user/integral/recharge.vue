
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
						<view class="item" :class="{ active: item.id == integralActiveId, badge: item.flag }" :data-flag="item.flag" v-for="(item, index) in integralData" :key="item.id" @click="integralItem(item.id, item.price)">
							<view class="line1">{{ item.display_title || (item.usable + ' ' + $t('points.points')) }}</view>
							<view class="line2">
								<text class="text1">￥</text>
								<text class="text2">{{ item.price }}</text>
							</view>
							<view class="line3">
								<text class="text">￥{{ item.original_price }}</text>
							</view>
							<view class="line4">
								<view class="">{{ $t('points.virtualProduct') }}</view>
								<view class="">{{ $t('points.noRefund') }}</view>
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
	export default {
		data() {
			return {
				buttonStyle: {
					width: '100%',
					height: '100rpx',
					border: 'none',
					fontSize: '24rpx',
					color: '#fff',
					background: 'linear-gradient(90deg, #5E72F7 0%, #9354FF 100%)',
					borderRadius: '16rpx',
					margin: '0',
					fontWeight: 'bold'
				},
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
			//虚拟支付调起
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
									tt.requestOrder({
									   data: res.data.data, // 请勿在前端对data做任何处理
									   byteAuthorization: res.data.byteAuthorization, // 请勿在前端对byteAuthorization做任何处理
									   success: (res) => {
									     console.log('成功', res);
									tt.getOrderPayment({
									 orderId:res.orderId,
										 success: (res) => {
										   uni.showToast({
										   		      title: this.('payment.success'),
										   		  	icon: 'none',
										   		  	duration: 2000
										   		  });
										   		that.buttonLoading = true
										   		that.getUserInfo()
										   		uni.$emit('updateUserInfo', true)
										   		uni.hideLoading()
										 },
										 fail: (res) => {
											  uni.hideLoading()
										   uni.showToast({
										   	        title: this.('payment.failed'),
										   	    	icon: 'none',
										   	    	duration: 2000
										   	    });
										   	  that.buttonLoading = true
										   	 
										   
										 },
										});
									   },
									   fail: (res) => {
										   uni.hideLoading()
										   
									    uni.showToast({
									    	        title: this.('payment.failed'),
									    	    	icon: 'none',
									    	    	duration: 2000
									    	    });
									    	  that.buttonLoading = true
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
									// 		      title: this.('payment.success'),
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
									// 		      title: this.('payment.failed'),
									// 		  	icon: 'none',
									// 		  	duration: 2000
									// 		  });
									// 		that.buttonLoading = true
									// 		uni.hideLoading()
									// 	}
									//   },
									//   fail(res) {
									// 	  console.log(res,'fail')
									// 	  uni.showToast({
									// 	        title: this.('payment.failed'),
									// 	    	icon: 'none',
									// 	    	duration: 2000
									// 	    });
									// 	  that.buttonLoading = true
									// 	  uni.hideLoading()
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
						this.getPay(order_sn, payment, platform)
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
					      title: this.('payment.success'),
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
					      title: this.('payment.failed'),
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
				      title: '当前用户的客户端版本不支持小程序虚拟支付',
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
				// 			    title: this.('payment.success'),
				// 				icon: 'none',
				// 				duration: 2000
				// 			});
				// 			this.buttonLoading = true
				// 			this.getUserInfo()
				// 			uni.$emit('updateUserInfo', true)
				// 			uni.hideLoading()
				// 		} else {
				// 			uni.showToast({
				// 			    title: this.('payment.failed'),
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
				// 						title: this.('payment.success'),
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
				// 							title: '未安装微信客户端',
				// 							icon: 'none',
				// 							duration: 2000
				// 						});
				// 						this.buttonLoading = true
				// 						uni.hideLoading()
				// 					} else {
				// 						uni.showToast({
				// 							title: this.('payment.failed'),
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
								    title: this.('payment.success'),
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
								    title: this.('payment.failed'),
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
									    title: this.('payment.success'),
										icon: 'none',
										duration: 2000
									});
									this.buttonLoading = true
									this.getUserInfo()
									uni.$emit('updateUserInfo', true)
									uni.hideLoading()
								} else {
									uni.showToast({
									    title: this.('payment.failed'),
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
												title: this.('payment.success'),
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
													title: '未安装微信客户端',
													icon: 'none',
													duration: 2000
												});
												this.buttonLoading = true
												uni.hideLoading()
											} else {
												uni.showToast({
													title: this.('payment.failed'),
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
							border-radius: 10rpx;
							border: 2rpx solid #5E72F7;
							box-shadow: 0 0 12rpx rgba(153, 153, 153, 0.16);
							text-align: center;
							padding: 60rpx 0 20rpx 0;
							
							&.active {
								background-clip: padding-box, border-box;
								background-origin: padding-box, border-box;
								background-image: linear-gradient(to right, #FFE0CC, #FFE0CC), linear-gradient(90deg, #5E72F7 0%, #9354FF 100%);
							
								.line2 {
									color: pink;
									@supports (-webkit-background-clip: text) or (background-clip: text) {
										background: linear-gradient(to right, #5E72F7 20%, #9354FF 100%);
										-webkit-background-clip: text;
										background-clip: text;
										color: #5E72F7;
									}
								}
								
								.line3 {
									color: pink;
								}
							}
							
							&.badge {
								position: relative;
								
								&::before {
									// content: '限时优惠';
									content: attr(data-flag);
									position: absolute;
									top: 0;
									right: -2rpx;
									transform: translate(0, -50%);
									background: linear-gradient(90deg, #5E72F7 0%, #9354FF 100%);
									font-size: 24rpx;
									color: #fff;
									padding: 4rpx 10rpx;
									border-radius: 8rpx;
								}
							}
							
							.line1 {
								font-size: 32rpx;
							}
							
							.line2 {
								margin: 20rpx 0;
								
								.text1 {
									font-size: 24rpx;
								}
								
								.text2 {
									font-size: 52rpx;
								}
							}
							
							.line3 {
								font-size: 24rpx;
								text-decoration: line-through;
								margin-bottom: 8rpx;
							}
							
							.line4 {
								font-size: 24rpx;
								color: rgba(153, 153, 153, 1);
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


<template>
	<view class="page_content">
		<!-- #ifndef MP-TOUTIAO -->
		<view class="head_content">
			<CustomNavbar :title="$t('vip.center')" color="#fff" bg="#8B9BFA"></CustomNavbar>
		</view>
		<!-- #endif -->
	
		<view class="main_content">
			<!-- 头部 banner -->
			<view class="vip_box">
				<view class="vip_info">
					<view class="vip_tag">
						<text class="tag_icon">♛</text>
						<text class="tag_text" v-if="userInfo.is_vip == 1">{{ $t('user.vipMember') }}</text>
						<text class="tag_text" v-else>{{ $t('vip.activateVip') }}</text>
					</view>
					<view class="vip_title" v-if="userInfo.is_vip == 1">{{ $t('vip.nobleMember') }}</view>
					<view class="vip_title" v-else>{{ $t('vip.enjoyAll') }}</view>
					<view class="vip_sub" v-if="userInfo.is_vip == 1">{{ $t('vip.expiredAt') }} {{ userInfo.vip_expiretime_text }}</view>
					<view class="vip_sub" v-else>{{ $t('vip.activateSlogan') }}</view>
					<view class="vip_perks">
						<view class="perk"><text class="perk_icon">✓</text><text>{{ $t('user.perkFreeEpisodes') }}</text></view>
						<view class="perk"><text class="perk_icon">✓</text><text>{{ $t('user.perkEarlyUnlock') }}</text></view>
						<view class="perk"><text class="perk_icon">✓</text><text>{{ $t('user.perkNoAds') }}</text></view>
					</view>
				</view>
				<image class="vip_image" src="https://img.nymaite.com/video_short/icons/vip.png" mode="widthFix"></image>
			</view>

			<!-- 套餐选择 -->
			<view class="pay_box">
				<view class="section_title">{{ $t('vip.selectPlan') }}</view>
				<view class="card_box">
					<view
						class="item"
						:class="{ hot: index === hotIndex }"
						v-for="(item, index) in vipList"
						:key="item.id"
						@click="openVip(item.id, item.price)"
					>
						<view class="hot_badge" v-if="index === hotIndex">{{ $t('vip.recommended') }}</view>
						<view class="item_name">{{ item.title }}</view>
						<view class="item_days">{{ item.days }}{{ $t('common.days') }}</view>
						<view class="item_price">
							<text class="unit">¥</text>
							<text class="price_num">{{ item.price }}</text>
						</view>
						<view class="item_oprice">{{ $t('vip.originalPrice') }}{{ item.original_price }}</view>
						<view class="item_btn" :class="{ hot_btn: index === hotIndex }">{{ $t('vip.activateNow') }}</view>
					</view>
				</view>
			</view>

			<!-- VIP权益说明 -->
			<view class="task_box box_bg" v-if="textTitle || textInfo">
				<view class="title_box">
					<text class="text">{{ textTitle }}</text>
				</view>
				<view class="info_box">
					<u-parse :content="textInfo"></u-parse>
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
				isColor:getApp().globalData.isColor,
				isBgColor:getApp().globalData.isBgColor,
				vipList: [],
				buttonLoading: true,
				textTitle: "",
				textInfo: "",
			}
		},
		computed: {
			...mapGetters("user", ["token", "userInfo"]),
			...mapGetters("app", ["iosIsPay"]),
			// 推荐套餐下标：优先选30天月卡，否则取中间
			hotIndex() {
				if (!this.vipList.length) return 0
				const idx = this.vipList.findIndex(v => v.days >= 30 && v.days <= 31)
				return idx >= 0 ? idx : Math.floor(this.vipList.length / 2)
			}
		},
		watch: {

		},
		async onShow(){
			 await this.$onLaunched
			 this.isColor = getApp().globalData.isColor
			 this.isBgColor=getApp().globalData.isBgColor
		},
		onLoad() {
			this.getVipList()
		},
		methods: { 
			...mapActions("user", ["getUserInfo"]),
			// 获取vip类型
			getVipList() {
				this.$request('user.vip').then(res => {
					if(res.code === 1) {
						if(res.data.list && res.data.list.length) {
							this.vipList = res.data.list
						}
						this.textTitle = res.data?.vip_desc?.title || ""
						this.textInfo = res.data?.vip_desc?.content || ""
					}
				})
			},
			// 开通vip
			openVip(id, price) {
				// #ifdef MP-WEIXIN
				if(!this.iosIsPay) return this.jumpView('/pages/user/info/contact')
				// #endif
				if(!this.buttonLoading) return
				this.buttonLoading = false
				uni.showLoading({ title: '开通中...', mask: true })
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
						uni.showToast({ title: res.msg || '下单失败', icon: 'none' })
					}
				}).catch(() => {
					uni.hideLoading()
					this.buttonLoading = true
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
										   that.buttonLoading = true
											that.getUserInfo()
											uni.$emit('updateUserInfo', true)
											uni.hideLoading()
										 },
										 fail: (res) => {
										   uni.showToast({
										   		      title: '支付失败',
										   		  	icon: 'none',
										   		  	duration: 2000
										   		  });
										   		that.buttonLoading = true
										   		uni.hideLoading()
										   
										 },
										});
									   },
									   fail: (res) => {
										  console.log('失败', res);
										  
										   
									   uni.showToast({
									   		      title: res.errMsg,
									   		  	icon: 'none',
									   		  	duration: 2000
									   		  });
									   		that.buttonLoading = true
									   		uni.hideLoading()
									   },
									 });
									// tt.pay({
									//   orderInfo: {
									//     order_id:  res.data.pay_data.data.order_id ,
									//     order_token:res.data.pay_data.data.order_token ,
									//   },
									//   service: 5,
									//   success(res) {
									//     if (res.code == 0) {
											
									// 		that.buttonLoading = true
									// 		that.getUserInfo()
									// 		uni.$emit('updateUserInfo', true)
									// 		uni.hideLoading()
									//       // 支付成功处理逻辑，只有res.code=0时，才表示支付成功
									//       // 但是最终状态要以商户后端结果为准
									//     }else{
									// 		uni.showToast({
									// 		      title: '支付失败',
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
									// 	        title: '支付失败',
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
					      title: '支付成功',
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
					      title: '支付失败',
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
								    title: '支付成功',
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
								    title: '支付失败',
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
									    title: '支付成功',
										icon: 'none',
										duration: 2000
									});
									this.buttonLoading = true
									this.getUserInfo()
									uni.$emit('updateUserInfo', true)
									uni.hideLoading()
								} else {
									uni.showToast({
									    title: '支付失败',
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
												title: '支付成功',
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
													title: '支付失败',
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
			// 处理下单后的支付结果
			handlePayResult(data) {
				// H5 微信支付：跳转到收银台链接
				if(data.h5Url) {
					window.location.href = data.h5Url
					return
				}
				// 微信公众号 JSAPI 支付
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
							uni.showToast({ title: '支付成功', icon: 'success' })
							this.getUserInfo()
						} else {
							uni.showToast({ title: '支付取消', icon: 'none' })
						}
					})
					return
				}
				// 余额支付直接成功，或支付配置未完成
				if(data.payError) {
					uni.showToast({ title: '支付功能配置中，请联系客服', icon: 'none', duration: 3000 })
				} else {
					uni.showToast({ title: '开通成功', icon: 'success' })
					this.getUserInfo()
				}
			},
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
		background: #f0f2f5;
		min-height: 100vh;

		.main_content {
			padding-bottom: 80rpx;

			/* ── 头部 Banner ── */
			.vip_box {
				background: linear-gradient(135deg, #8B9BFA 0%, #B47FFF 100%);
				padding: 40rpx 40rpx 48rpx;
				position: relative;
				overflow: hidden;
				border-radius: 0 0 40rpx 40rpx;
				box-shadow: 0 8rpx 32rpx rgba(94, 114, 247, 0.35);

				&::before {
					content: '';
					position: absolute;
					top: -60rpx; right: -60rpx;
					width: 300rpx; height: 300rpx;
					border-radius: 50%;
					background: rgba(255,255,255,0.06);
				}

				.vip_info {
					position: relative;
					z-index: 1;

					.vip_tag {
						display: inline-flex;
						align-items: center;
						background: rgba(255,255,255,0.18);
						border-radius: 40rpx;
						padding: 6rpx 20rpx;
						margin-bottom: 20rpx;

						.tag_icon { font-size: 28rpx; margin-right: 8rpx; color: #ffd700; }
						.tag_text { font-size: 24rpx; color: #fff; font-weight: 600; }
					}

					.vip_title {
						font-size: 52rpx;
						font-weight: 900;
						color: #fff;
						margin-bottom: 10rpx;
						letter-spacing: 2rpx;
						text-shadow: 0 2rpx 8rpx rgba(0,0,0,0.15);
					}

					.vip_sub {
						font-size: 26rpx;
						color: #fff;
						opacity: 0.95;
						margin-bottom: 28rpx;
					}

					.vip_perks {
						display: flex;
						gap: 24rpx;

						.perk {
							display: flex;
							align-items: center;
							font-size: 22rpx;
							color: #fff;
							font-weight: 600;

							.perk_icon {
								color: #ffd700;
								font-size: 24rpx;
								margin-right: 6rpx;
								font-weight: bold;
							}
						}
					}
				}

				.vip_image {
					width: 200rpx;
					position: absolute;
					top: 24rpx;
					right: 20rpx;
					opacity: 0.95;
					filter: drop-shadow(0 8rpx 16rpx rgba(0,0,0,0.2));
				}
			}

			/* ── 套餐选择 ── */
			.pay_box {
				margin: 32rpx 28rpx 0;
				background: #fff;
				border-radius: 24rpx;
				padding: 36rpx 28rpx 40rpx;
				box-shadow: 0 2rpx 20rpx rgba(0,0,0,0.06);

				.section_title {
					font-size: 32rpx;
					font-weight: 800;
					color: #1a1a2e;
					margin-bottom: 28rpx;
					padding-left: 4rpx;
					border-left: 6rpx solid #5E72F7;
					padding-left: 16rpx;
				}

				.card_box {
					display: flex;
					flex-wrap: wrap;
					gap: 20rpx;

					.item {
						width: calc((100% - 20rpx) / 2);
						background: #f7f8ff;
						border: 2rpx solid #e8eaf6;
						border-radius: 20rpx;
						display: flex;
						flex-direction: column;
						align-items: center;
						padding: 36rpx 20rpx 28rpx;
						position: relative;
						transition: all 0.2s;

						&:active { opacity: 0.85; }

						&.hot {
							background: linear-gradient(160deg, #eef0ff 0%, #f3eaff 100%);
							border-color: #5E72F7;
							box-shadow: 0 4rpx 20rpx rgba(94, 114, 247, 0.18);
						}

						.hot_badge {
							position: absolute;
							top: -2rpx; right: -2rpx;
							background: linear-gradient(135deg, #5E72F7, #9354FF);
							color: #fff;
							font-size: 20rpx;
							font-weight: bold;
							padding: 6rpx 18rpx;
							border-radius: 0 18rpx 0 16rpx;
						}

						.item_name {
							font-size: 28rpx;
							font-weight: 800;
							color: #222;
							margin-bottom: 6rpx;
						}

						.item_days {
							font-size: 22rpx;
							color: #777;
							margin-bottom: 16rpx;
						}

						.item_price {
							display: flex;
							align-items: flex-end;
							margin-bottom: 6rpx;

							.unit {
								font-size: 26rpx;
								font-weight: 700;
								color: #5E72F7;
								margin-bottom: 4rpx;
							}

							.price_num {
								font-size: 56rpx;
								font-weight: 900;
								color: #5E72F7;
								line-height: 1;
							}
						}

						.item_oprice {
							font-size: 22rpx;
							color: #bbb;
							text-decoration: line-through;
							margin-bottom: 24rpx;
						}

						.item_btn {
							width: 80%;
							height: 64rpx;
							line-height: 64rpx;
							text-align: center;
							color: #5E72F7;
							font-size: 26rpx;
							font-weight: bold;
							background: #eef0ff;
							border-radius: 32rpx;
							border: 1.5rpx solid #c5caff;

							&.hot_btn {
								background: linear-gradient(135deg, #5E72F7 0%, #9354FF 100%);
								color: #fff;
								border: none;
								box-shadow: 0 4rpx 16rpx rgba(94,114,247,0.35);
							}
						}
					}
				}
			}

			/* ── 权益说明 ── */
			.box_bg {
				border-radius: 24rpx;
				padding: 36rpx 32rpx;
				background: #fff;
				box-shadow: 0 2rpx 16rpx rgba(0,0,0,0.06);
			}

			.title_box {
				margin-bottom: 24rpx;
				.text {
					font-size: 32rpx;
					color: #1a1a2e;
					font-weight: 800;
					border-left: 6rpx solid #5E72F7;
					padding-left: 16rpx;
				}
			}

			.info_box {
				color: #555;
				font-size: 28rpx;
			}

			.task_box {
				margin: 24rpx 28rpx 0;
			}
		}
	}
</style>

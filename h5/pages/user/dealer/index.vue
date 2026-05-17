
<template>
	<view class="page_content">
		<!-- #ifndef MP-TOUTIAO -->
		<view class="head_content">
			<CustomNavbar title="开通经销商"></CustomNavbar>
		</view>
		<!-- #endif -->
		
		<view class="main_content">
			<view class="info_box">
				<view class="user">
					<view class="avatar">
						<image class="image" :src="userInfo.avatar" mode="aspectFill"></image>
					</view>
					<view class="content" v-if="level == 0">
						<view class="p1">亲爱的平台用户，您好</view>
						<view class="p2">您还不是分销商，请在下面点击开通经销商</view>
					</view>
					<view class="content" v-else>
						<view class="p1">亲爱的{{ userInfo.levelText }}，您好</view>
						<view class="p2">恭喜你，您已是我们的{{ userInfo.levelText }}</view>
					</view>
				</view>
				<view class="card">
					<view class="texts" v-if="level == 0">
						<text class="text1" :style="'color:'+isColor">平台用户</text>
						<text class="text3">永久有效</text>
					</view>
					<view class="texts" v-else>
						<text class="text1">{{ userInfo.levelText }}</text>
						<!-- <text class="text2">开通时间:2023年5月26日</text> -->
						<text class="text3">{{ userInfo.expireText }}</text>
					</view>
				</view>
			</view>
			<view class="content_box">
				<view class="level_box">
					<view class="title">等级分类</view>
					<view class="list_box" v-if="levelData.length">
						<view class="item" :style="item.level == dredgeLevel ?'background-image: linear-gradient(to right, #000, #000),'+isBgColor :''" :class="{ active: item.level == dredgeLevel }" v-for="(item, index) in levelData" :key="index" @click="levelCardClick(item)">
							<view class="text1">{{ item.expire_text }}</view>
							<view class="text2" :style="'color:'+isColor">
								<text>￥</text>
								<text class="num">{{ item.price }}</text>
							</view>
							<view class="text3">直接分润{{ Number(item.direct) }}%</view>
							<view class="text3">间接分润{{ Number(item.indirect) }}%</view>
							<view class="tip" :style="'background:'+isBgColor">{{ item.name }}</view>
						</view>
					</view>
				</view>
				<view class="text_box">
					<u-parse :content="msg"></u-parse>
				</view>
			</view>
		</view>
		<view class="footer_content" v-if="levelData.length">
			<view class="button_box">
				<u-button text="立即开通" v-if="dredgeLevel != 0" :loading="buttonLoading" :customStyle="buttonStyle" @click="dredgeDealer" />
			</view>
		</view>
	</view>
</template>

<script>
	import { mapGetters, mapActions } from "vuex"
	export default {
		data() {
			return {
				isColor: `pink`,
				isBgColor: `#5E72F7`,
				buttonStyle: {
					width: '100%',
					height: '108rpx',
					border: 'none',
					fontSize: '32rpx',
					color: '#fff',
					background: `linear-gradient(90deg, #5E72F7 0%, #9354FF 100%)`,
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
							this.userInfo.levelText = res.data.reseller_level + '级分销商'
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
				if (!this.dredge.resellerId) return this.$u.toast('请选择套餐')
				const doBuy = () => {
					this.buttonLoading = true
					uni.showLoading({ title: '开通中...', mask: true })
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
							uni.showToast({ title: res.msg || '下单失败', icon: 'none' })
						}
					}).catch(() => {
						uni.hideLoading()
						this.buttonLoading = false
					})
				}
				if (this.dredgeLevel < this.level) {
					uni.showModal({
						title: '提示',
						content: '当前购买等级小于已有等级，是否继续？',
						success: r => { if (r.confirm) doBuy() }
					})
				} else {
					doBuy()
				}
			},
			handlePayResult(data) {
				if (data.status === 'paid') {
					uni.showToast({ title: '开通成功', icon: 'success' })
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
							uni.showToast({ title: '开通成功', icon: 'success' })
							this.getPageData()
						} else {
							uni.showToast({ title: '支付取消', icon: 'none' })
						}
					})
					return
				}
				if (data.payError) {
					uni.showToast({ title: '支付未配置，请联系客服', icon: 'none', duration: 3000 })
				} else {
					uni.showToast({ title: '开通成功', icon: 'success' })
					this.getPageData()
				}
			}
		}
	}
</script>

<style lang="scss" scoped>
	.page_content {
		position: relative;
		
		&::before {
			content: "";
			width: 100%;
			height: 410rpx;
			position: absolute;
			top: 0;
			left: 0;
			z-index: 0;
			background-image: url('https://img.nymaite.com/video_short/images/v_bg.png');
			background-repeat: no-repeat;
			background-size: auto 100%;
			background-position: 110% 100%;
		}
		
		.head_content {
			background: #F9F9FB;
		}
		
		.main_content {
			overflow-x: hidden;
			padding-bottom: 200rpx;
			
			.info_box {
				width: 140%;
				background: #F9F9FB;
				border-radius: 0 0 50% 50%;
				margin-left: -20%;
				padding: 0 calc(20% + 32rpx);
				overflow: hidden;
				  
				.user {
					position: relative;
					display: flex;
					align-items: center;
					margin-top: 10rpx;
					
					.avatar {
						width: 114rpx;
						height: 114rpx;
						border-radius: 50%;
						overflow: hidden;
						
						.image {
							width: 100%;
							height: 100%;
						}
					}
					
					.content {
						margin-left: 24rpx;
						
						.p1 {
							font-size: 32rpx;
							color: #333;
							margin-bottom: 8rpx;
							font-weight: 700;
						}
						
						.p2 {
							font-size: 28rpx;
							color: #999;
						}
					}
				}
				
				.card {
					position: relative;
					height: 120rpx;
					margin-top: 10rpx;
					border-radius: 16rpx;
					padding: 32rpx 40rpx 0 40rpx;
					background: linear-gradient(269.64deg, rgba(92, 86, 96, 1) 0%, rgba(57, 52, 59, 1) 100%);
					
					&::before {
						content: "";
						width: 100%;
						height: 100%;
						position: absolute;
						top: 0;
						left: 0;
						background-image: url('https://img.nymaite.com/video_short/images/line_bg.png');
						background-repeat: no-repeat;
						background-size: auto 110%;
						background-position: 50% 0;
					}
					
					.texts {
						position: relative;
						display: flex;
						align-items: center;
						justify-content: space-between;
						
						.text1 {
							font-size: 36rpx;
							// color: #E6BD70;
							// color: #F28C46;
							color: pink;
							font-weight: 700;
						}
						
						.text2 {
							font-size: 24rpx;
							color: #fff;
						}
						
						.text3 {
							font-size: 24rpx;
							color: #fff;
						}
					}
				}
			}
			
			.content_box {
				padding: 32rpx;
				
				.level_box {
					position: relative;
					background: #fff;
					
					.title {
						font-size: 36rpx;
						font-weight: 700;
						color: #272D2F;
					}
					
					.list_box {
						margin-top: 36rpx;
						display: flex;
						flex-wrap: wrap;
						justify-content: space-between;
						.item {
							position: relative;
							min-width: calc((100% - 60rpx) / 3);
							border-radius: 20rpx;
							padding: 72rpx 20rpx 36rpx 20rpx;
							border: 4rpx solid #5E72F7;
							background: rgba(77, 77, 77, 1);
							margin: 0 0rpx 30rpx 0;
							
							&:nth-child(3n) {
								margin-right: 0;
							}
							
							&.active {
								background-clip: padding-box, border-box;
								background-origin: padding-box, border-box;
								background-image: linear-gradient(to right, #000, #000), linear-gradient(90deg, #5E72F7 0%, #9354FF 100%);
							}
							
							.text1 {
								font-size: 28rpx;
								color: #fff;
							}
							
							.text2 {
								font-size: 36rpx;
								color: #5E72F7;
								margin: 8rpx 0;
								
								.num {
									font-size: 64rpx;
									font-weight: 900;
								}
							}
							
							.text3 {
								font-size: 24rpx;
								color: #fff;
							}
							
							.tip {
								font-size: 24rpx;
								font-weight: 700;
								color: #fff;
								padding: 8rpx 16rpx;
								border-radius: 0 20rpx 0 20rpx;
								background: linear-gradient(90deg, #5E72F7 0%, #9354FF 100%);
								position: absolute;
								top: -4rpx;
								right: -4rpx;
							}
						}
					}
				}
				
				.text_box {
					margin-top: 40rpx;
					border-radius: 16rpx;
					box-shadow: 0 0 60rpx 0 rgba(102, 102, 102, 0.15);
					padding: 40rpx;
					font-size: 28rpx;
					color: rgba(51, 51, 51, 1);
					overflow: hidden;
					
					.title {
						font-weight: bold;
						padding-bottom: 36rpx;
						border-bottom: 2rpx solid rgba(221, 221, 221, 1);
						margin-bottom: 36rpx;
					}
				}
			}
		}
	
		.footer_content {
			width: 100%;
			padding: 0 32rpx;
			position: fixed;
			bottom: 40rpx;
			left: 0;
		}
	}
</style>

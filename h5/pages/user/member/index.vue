
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
						<view class="item_name">{{ item.display_title || item.title }}</view>
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
				uni.showLoading({ title: this.$t('payment.activating'), mask: true })
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
			},
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
							uni.showToast({ title: this.$t('payment.success'), icon: 'success' })
							this.getUserInfo()
						} else {
							uni.showToast({ title: '支付取消', icon: 'none' })
						}
					})
					return
				}
				// 余额支付直接成功，或支付配置未完成
				if(data.payError) {
					uni.showToast({ title: this.$t('payment.notConfigured'), icon: 'none', duration: 3000 })
				} else {
					uni.showToast({ title: this.$t('payment.success'), icon: 'success' })
					this.getUserInfo()
				}
			},
		}
	}
</script>

<style lang="scss" scoped>
	.page_content {
		background: #f0f2f5;
		min-height: 100vh;

		.main_content {
			padding-bottom: $dj-spacing-xxl;

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

						.tag_icon { font-size: $dj-fs-base; margin-right: $dj-spacing-xs; color: #ffd700; }
						.tag_text { font-size: $dj-fs-sm; color: #fff; font-weight: 600; }
					}

					.vip_title {
						font-size: $dj-fs-display;
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
						gap: $dj-spacing-base;

						.perk {
							display: flex;
							align-items: center;
							font-size: 22rpx;
							color: #fff;
							font-weight: 600;

							.perk_icon {
								color: #ffd700;
								font-size: $dj-fs-sm;
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
					font-size: $dj-fs-lg;
					font-weight: 800;
					color: #1a1a2e;
					margin-bottom: 28rpx;
					padding-left: 4rpx;
					border-left: 6rpx solid $dj-primary;
					padding-left: $dj-spacing-sm;
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
							border-color: $dj-primary;
							box-shadow: 0 4rpx 20rpx rgba(94, 114, 247, 0.18);
						}

						.hot_badge {
							position: absolute;
							top: -2rpx; right: -2rpx;
							background: $dj-gradient-primary;
							color: #fff;
							font-size: $dj-fs-xs;
							font-weight: bold;
							padding: 6rpx 18rpx;
							border-radius: 0 18rpx 0 16rpx;
						}

						.item_name {
							font-size: $dj-fs-base;
							font-weight: 800;
							color: #222;
							margin-bottom: 6rpx;
						}

						.item_days {
							font-size: 22rpx;
							color: #777;
							margin-bottom: $dj-spacing-sm;
						}

						.item_price {
							display: flex;
							align-items: flex-end;
							margin-bottom: 6rpx;

							.unit {
								font-size: 26rpx;
								font-weight: 700;
								color: $dj-primary;
								margin-bottom: 4rpx;
							}

							.price_num {
								font-size: 56rpx;
								font-weight: 900;
								color: $dj-primary;
								line-height: 1;
							}
						}

						.item_oprice {
							font-size: 22rpx;
							color: #bbb;
							text-decoration: line-through;
							margin-bottom: $dj-spacing-base;
						}

						.item_btn {
							width: 80%;
							height: 64rpx;
							line-height: 64rpx;
							text-align: center;
							color: $dj-primary;
							font-size: 26rpx;
							font-weight: bold;
							background: #eef0ff;
							border-radius: 32rpx;
							border: 1.5rpx solid #c5caff;

							&.hot_btn {
								background: $dj-gradient-primary;
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
				margin-bottom: $dj-spacing-base;
				.text {
					font-size: $dj-fs-lg;
					color: #1a1a2e;
					font-weight: 800;
					border-left: 6rpx solid $dj-primary;
					padding-left: $dj-spacing-sm;
				}
			}

			.info_box {
				color: #555;
				font-size: $dj-fs-base;
			}

			.task_box {
				margin: 24rpx 28rpx 0;
			}
		}
	}
</style>

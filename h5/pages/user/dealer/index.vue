

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
						<view class="level_tag">{{ resolveI18n(item.name_i18n, item.name) }}</view>
						<view class="level_body">
							<view class="level_name">{{ item.expire }}{{ $t('common.days') }}</view>
							<view class="level_price">
								<text class="currency">¥</text>
								<text class="price_num">{{ item.price }}</text>
							</view>
							<view class="level_rate">{{ $t('dealer.directRate', [Number(item.direct)]) }}</view>
							<view class="level_rate">{{ $t('dealer.indirectRate', [Number(item.indirect)]) }}</view>
						</view>
					</view>
				</view>
			</view>

			<!-- 分销商权益（默认展示，填充空白并强化营销点） -->
			<view class="benefits_card" v-if="levelData.length">
				<view class="section_title">{{ $t('dealer.benefitsTitle') }}</view>
				<view class="benefits_list">
					<view class="benefit_item" v-for="(b, i) in benefitList" :key="i">
						<view class="benefit_icon" :style="{ background: b.bg }">
							<text class="benefit_emoji">{{ b.icon }}</text>
						</view>
						<view class="benefit_text">
							<view class="benefit_title">{{ $t(b.titleKey) }}</view>
							<view class="benefit_desc">{{ $t(b.descKey) }}</view>
						</view>
					</view>
				</view>
			</view>

			<!-- 说明内容（后台富文本，可选） -->
			<view class="desc_card" v-if="msg">
				<u-parse :content="msg"></u-parse>
			</view>
		</view>

		<view class="footer_content" v-if="levelData.length">
			<view class="primary-btn-wrap" v-if="dredgeLevel != 0">
				<u-button
					:text="$t('dealer.activateNow')"
					:loading="buttonLoading"
					:customStyle="buttonStyle"
					@click="dredgeDealer"
				/>
			</view>
		</view>
	</view>
</template>

<script>
	import { mapGetters, mapActions } from "vuex"
	import { primaryBtn } from '@/common/utils/buttonStyle.js'
	export default {
		data() {
			return {
				buttonStyle: primaryBtn({ fontSize: '32rpx' }),
				buttonLoading: false,
				msg: '',
				levelData: [],
				level: 0,
				dredgeLevel: 0,
				maxLevel: 0,
				dredge: { resellerId: null },
				userInfo: {},
				benefitList: [
					{ icon: '💰', bg: 'linear-gradient(135deg, #fff4d6, #ffd166)', titleKey: 'dealer.benefit1Title', descKey: 'dealer.benefit1Desc' },
					{ icon: '⚡', bg: 'linear-gradient(135deg, #e0e7ff, #818cf8)', titleKey: 'dealer.benefit2Title', descKey: 'dealer.benefit2Desc' },
					{ icon: '👥', bg: 'linear-gradient(135deg, #d1fae5, #34d399)', titleKey: 'dealer.benefit3Title', descKey: 'dealer.benefit3Desc' },
					{ icon: '⏳', bg: 'linear-gradient(135deg, #fde2f3, #f472b6)', titleKey: 'dealer.benefit4Title', descKey: 'dealer.benefit4Desc' }
				]
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
			resolveI18n(i18nJson, fallback) {
				try {
					const map = i18nJson ? JSON.parse(i18nJson) : {}
					const locale = this.$i18n.locale
					return map[locale] || map['zh-CN'] || fallback
				} catch {
					return fallback
				}
			},
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
		background: $dj-bg-soft;
		min-height: 100vh;

		.main_content {
			padding: $dj-spacing-base 28rpx 160rpx;

			.user_card {
				background: $dj-gradient-primary;
				border-radius: $dj-radius-lg;
				padding: $dj-spacing-md $dj-spacing-md $dj-spacing-base;
				box-shadow: $dj-shadow-primary;
				margin-bottom: $dj-spacing-base;

				.user_row {
					display: flex;
					align-items: center;
					gap: $dj-spacing-base;
					margin-bottom: 28rpx;

					.avatar {
						width: 96rpx;
						height: 96rpx;
						border-radius: 50%;
						border: 3rpx solid rgba(255, 255, 255, 0.5);
						flex-shrink: 0;
					}

					.avatar_placeholder {
						background: rgba(255, 255, 255, 0.25);
						display: flex;
						align-items: center;
						justify-content: center;

						.avatar_initial {
							font-size: $dj-fs-xl;
							font-weight: $dj-fw-bold;
							color: $dj-text-inverse;
						}
					}

					.user_info {
						flex: 1;

						.user_greeting {
							font-size: $dj-fs-md;
							font-weight: $dj-fw-bold;
							color: $dj-text-inverse;
							margin-bottom: $dj-spacing-xs;
						}

						.user_desc {
							font-size: $dj-fs-sm;
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
						border-radius: $dj-radius-md;
						padding: 6rpx 20rpx;

						.badge_text {
							font-size: $dj-fs-sm;
							font-weight: $dj-fw-bold;
							color: $dj-text-inverse;
						}
					}

					.expire_text {
						font-size: $dj-fs-sm;
						color: rgba(255, 255, 255, 0.75);
					}
				}
			}

			.section_card {
				background: $dj-bg-base;
				border-radius: $dj-radius-lg;
				padding: $dj-spacing-md;
				box-shadow: $dj-shadow-sm;
				margin-bottom: $dj-spacing-base;

				.section_title {
					font-size: $dj-fs-md;
					font-weight: $dj-fw-bold;
					color: $dj-text-primary;
					margin-bottom: 28rpx;
					padding-left: 12rpx;
					border-left: 6rpx solid $dj-primary;
				}

				.level_grid {
					display: flex;
					flex-wrap: wrap;
					gap: $dj-spacing-sm;

					.level_item {
						width: calc((100% - 32rpx) / 3);
						border-radius: $dj-radius-md;
						overflow: hidden;
						border: 2rpx solid #e8eaff;
						background: #f8f9ff;

						&.active {
							background: $dj-gradient-primary;
							border-color: transparent;
							box-shadow: 0 4rpx 20rpx rgba(94, 114, 247, 0.35);

							.level_tag {
								background: rgba(0, 0, 0, 0.18);
							}

							.level_name, .level_rate {
								color: rgba(255, 255, 255, 0.85);
							}

							.level_price {
								color: $dj-text-inverse;
								.price_num { color: $dj-text-inverse; }
							}
						}

						.level_tag {
							display: block;
							width: 100%;
							background: linear-gradient(90deg, $dj-primary 0%, $dj-primary-deep 100%);
							color: $dj-text-inverse;
							font-size: $dj-fs-xs;
							font-weight: $dj-fw-bold;
							text-align: center;
							padding: 10rpx $dj-spacing-xs;
							line-height: $dj-lh-tight;
							word-break: break-word;
						}

						.level_body {
							padding: $dj-spacing-sm $dj-spacing-sm $dj-spacing-base;
						}

						.level_name {
							font-size: $dj-fs-sm;
							color: $dj-text-tertiary;
							margin-bottom: $dj-spacing-xs;
						}

						.level_price {
							display: flex;
							align-items: baseline;
							gap: $dj-spacing-xxs;
							margin: 10rpx 0;
							color: $dj-primary;

							.currency {
								font-size: $dj-fs-base;
								font-weight: $dj-fw-bold;
							}

							.price_num {
								font-size: $dj-fs-display;
								font-weight: 900;
								line-height: 1;
								color: $dj-primary;
							}
						}

						.level_rate {
							font-size: 22rpx;
							color: $dj-text-secondary;
							line-height: $dj-lh-loose;
						}
					}
				}
			}

			.benefits_card {
				background: $dj-bg-base;
				border-radius: $dj-radius-lg;
				padding: $dj-spacing-md;
				box-shadow: $dj-shadow-sm;
				margin-bottom: $dj-spacing-base;

				.section_title {
					font-size: $dj-fs-md;
					font-weight: $dj-fw-bold;
					color: $dj-text-primary;
					margin-bottom: $dj-spacing-base;
					padding-left: 12rpx;
					border-left: 6rpx solid $dj-primary;
				}

				.benefits_list {
					display: flex;
					flex-direction: column;
					gap: 20rpx;
				}

				.benefit_item {
					display: flex;
					align-items: center;
					gap: 20rpx;
					padding: 18rpx 20rpx;
					background: #f8f9ff;
					border-radius: 18rpx;
				}

				.benefit_icon {
					width: 72rpx;
					height: 72rpx;
					border-radius: $dj-radius-base;
					display: flex;
					align-items: center;
					justify-content: center;
					flex-shrink: 0;
					box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.06);

					.benefit_emoji {
						font-size: 38rpx;
						line-height: 1;
					}
				}

				.benefit_text {
					flex: 1;
					min-width: 0;
				}

				.benefit_title {
					font-size: $dj-fs-base;
					font-weight: $dj-fw-bold;
					color: $dj-text-primary;
					margin-bottom: $dj-spacing-xxs;
				}

				.benefit_desc {
					font-size: $dj-fs-sm;
					color: $dj-text-tertiary;
					line-height: 1.5;
				}
			}

			.desc_card {
				background: $dj-bg-base;
				border-radius: $dj-radius-lg;
				padding: $dj-spacing-md;
				box-shadow: $dj-shadow-sm;
				font-size: 26rpx;
				color: $dj-text-secondary;
			}
		}

		.footer_content {
			position: fixed;
			bottom: 0;
			left: 0;
			width: 100%;
			padding: 20rpx $dj-spacing-md $dj-spacing-lg;
			background: $dj-bg-base;
			box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.06);
		}

	}
</style>

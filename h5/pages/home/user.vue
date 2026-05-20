
<template>
	<view class="page_content">
			<!-- #ifndef MP-TOUTIAO -->
			<view class="head_content" v-if="isStatus == 0">
				<CustomNavbar :left="0" title="我的"></CustomNavbar>
			</view>
			<!-- #endif -->
	
			<view class="main_content" style="background: rgba(235, 236, 237, 1);" v-if="isStatus == 0">
				<view class="userinfo_box" style="flex-direction: column;margin-bottom: 50rpx;align-items: center;" v-if="userInfoStore"
					@click="jumpView('/pages/user/info/index')">
					<view class="avatar" style="margin-bottom: $dj-spacing-sm;">
						<image class="image" :src="userInfoStore.avatar" mode="aspectFill"></image>
					</view>
					<view class="info" style="margin-left:0;display: flex;flex-direction: column;align-items: center;width: 100%;">
						<view class="nickname" style="width: 100%;text-align: center;">
							<text class="text">{{ userInfoStore.nickname }}</text>
							<image class="image" v-if="userInfoStore.is_vip == 1"
								src="https://img.nymaite.com/video_short/icons/vip.png" mode="widthFix" @click="openVip">
							</image>
						</view>
	
						<view class="msg" style="display: flex;align-items: center;justify-content: center;color: rgba(0, 0, 0, 0.7);width: 100%;">
							<text class="text"
								@click.stop="jumpView('/pages/user/integral/index')">{{ userInfoStore.usable || 0 }}{{ $t('user.myPoints') }}</text>
							<view style="margin: 0 20rpx; height: 24rpx;width: 1rpx;background-color: rgba(0, 0, 0, 0.2);">
	
							</view>
							<text v-if="userInfoStore.is_vip == 1" class="copy" @click.stop="openVip"
								style="color: rgba(0, 0, 0, 0.7);text-decoration: none;">{{ $t('user.myVip') }}</text>
							<text v-else class="copy" @click.stop="openVip"
								style="color: rgba(0, 0, 0, 0.7);text-decoration: none;">{{ $t('user.activateNow') }}</text>
						</view>
					</view>
				</view>
				<view class="userinfo_box" style="margin-bottom: 50rpx;flex-direction: column;align-items: center;" v-else
					@click="jumpView('/pages/login/login')">
					<view class="avatar">
						<image class="image" src="https://img.nymaite.com/video_short/images/avatar.png" mode="aspectFill">
						</image>
					</view>
					<view class="info" style="margin-left: 0;display: flex;flex-direction: column;align-items: center;width: 100%;">
						<view class="nickname" style="width: 100%;text-align: center;">
							<text class="text">{{ $t('user.notLoggedIn') }}</text>
						</view>
						<view class="msg" style="width: 100%;"></view>
					</view>
				</view>

			<view class="card_box" style="background: none;">
				<view class="item" v-for="(item, index) in cardListTwo" :key="item.id" @click="cardItemClick(item)">
					<view class="icon" style="margin-bottom: $dj-spacing-md;">
						<image class="image" :src="item.img" mode="aspectFill"></image>
					</view>
					<view class="text" style="color: rgba(0, 0, 0, 0.7);">{{ item.text }}</view>
				</view>
			</view>

			<view class="vip_card">
				<view class="vip_box" :style="`background: linear-gradient(141.96deg, #2a3599 0%, #7c3aed 100%)`" v-if="!iosIsPay">
					<view class="left">
						<view class="line1">影视通VIP</view>
						<view class="line2">{{ $t('user.vipBenefitDesc') }}</view>
					</view>
					<view class="right">
						<u-button style="color: rgba(135, 141, 255, 1);" :text="$t('user.myVip')" v-if="userInfoStore.is_vip == 1"
							:customStyle="buttonStyle" @click="openVip" />
						<u-button style="color: rgba(135, 141, 255, 1);" :text="$t('user.activateNow')" v-else :customStyle="buttonStyle"
							@click="openVip" />
					</view>
				</view>
				<view class="vip_box" :style="`background: linear-gradient(141.96deg, #2a3599 0%, #7c3aed 100%)`" v-else>
					<view class="left">
						<view class="line1">影视通VIP</view>
						<view class="line2">{{ $t('user.vipBenefitDesc') }}</view>
					</view>
					<view class="right">
						<u-button style="color: rgba(135, 141, 255, 1);" :text="$t('user.activateNow')" :customStyle="buttonStyle"
							@click="openVip" />
					</view>
				</view>
			</view>

			<view class="menu_box" v-if="userInfoStore.mgg == 1 || !mggStatus"
				style="background: linear-gradient(141.96deg, #2a3599 0%, #7c3aed 100%);">
				<view class="title">{{ $t('user.allFeatures') }}</view>
				<view class="menu-grid">
					<view v-if="item.id!=8"
						class="menu-item"
						v-for="(item, index) in menuListTwoI18n"
						:key="item.id"
						@click="menuItemClick(item.rid, item.text, item.path, item.docKey)">
						<view class="icon">
							<image class="image" :src="item.img" :style="{ width: item.width }" mode="widthFix" />
						</view>
						<view class="text">{{ item.text }}</view>
					</view>
				</view>
			</view>
			<view class="menu_box" v-else
				style="background: linear-gradient(141.96deg, #2a3599 0%, #7c3aed 100%);">
				<view class="title">{{ $t('user.allFeatures') }}</view>
				<view class="menu-grid">
					<view class="menu-item"
						v-for="(item, index) in menuListI18n"
						:key="item.id"
						@click="menuItemClick(item.rid, item.text, item.path, item.docKey)">
						<view class="icon">
							<image class="image" :src="item.img" :style="{ width: item.width }" mode="widthFix" />
						</view>
						<view class="text">{{ item.text }}</view>
					</view>
				</view>
			</view>
			<view class="copyright" v-if="copyrightData.length" @click="debugClick">
				<view class="item" v-for="(item, index) in copyrightData" :key="index">
					<image class="image" v-if="item.image" :src="item.image" mode="widthFix"></image>
					<!-- #ifdef MP-WEIXIN -->
					<text class="text">{{ item.name }}</text>
					<!-- #endif -->
					<!-- #ifdef H5 -->
					<a v-if="item.url" :href="item.url" target="_blank" rel="">
						<text class="text">{{ item.name }}</text>
					</a>
					<text v-else class="text">{{ item.name }}</text>
					<!-- #endif -->
				</view>
			</view>
			<view class="alert_box"
				v-if="platform != 'H5' && token && userInfoStore && configStore.system.mobile_switch == 1">
				<view class="item" v-if="!userInfoStore.verification.mobile">
					<text class="text">您还没有绑定手机号</text>
					<text class="btn" @click="alertBindButton('mobile')">去绑定</text>
				</view>
			</view>
			<u-modal :show="cdkey.show" :title="cdkey.title" :showCancelButton="true" @confirm="cdkeyConfirm"
				@cancel="cdkey.show = false">
				<view style="width: 100%;">
					<u-input v-model="cdkey.value" :customStyle="inputStyle" clearable placeholder="请输入兑换码"
						@change="inputChange" @blur="inputChange"></u-input>
				</view>
			</u-modal>
		</view>
		<view class="main_content user-mode" v-if="isStatus == 1">
			<!-- 渐变头部用户卡片 -->
			<view class="user-hero">
				<view class="hero-bg"></view>
				<view class="hero-content" v-if="userInfoStore" @click="jumpView('/pages/user/info/index')">
					<view class="hero-avatar">
						<image class="image" :src="userInfoStore.avatar || 'https://img.nymaite.com/video_short/images/avatar.png'" mode="aspectFill"></image>
						<view class="vip-badge" v-if="userInfoStore.is_vip == 1">VIP</view>
					</view>
					<view class="hero-info">
						<view class="hero-nickname">{{ userInfoStore.nickname || '用户' }}</view>
						<view class="hero-uid">
							<text>ID: {{ userInfoStore.user_id }}</text>
							<text class="copy-btn" @click.stop="copyText(userInfoStore.user_id)">{{ $t('common.copy') }}</text>
						</view>
					</view>
					<view class="hero-arrow" style="display:flex;align-items:center;">
						<view @click.stop>
							<LangSwitcher style="margin-right: $dj-spacing-sm;" />
						</view>
						<u-icon name="arrow-right" color="rgba(255,255,255,0.7)" size="18"></u-icon>
					</view>
				</view>
				<view class="hero-content" v-else @click="jumpView('/pages/login/login')">
					<view class="hero-avatar">
						<image class="image" src="https://img.nymaite.com/video_short/images/avatar.png" mode="aspectFill"></image>
					</view>
					<view class="hero-info">
						<view class="hero-nickname">{{ $t('user.tapToLogin') }}</view>
						<view class="hero-uid">{{ $t('user.loginForMore') }}</view>
					</view>
					<view class="hero-arrow">
						<u-icon name="arrow-right" color="rgba(255,255,255,0.7)" size="18"></u-icon>
					</view>
				</view>
				<!-- 快捷入口 -->
				<view class="hero-shortcuts" v-if="userInfoStore">
					<view class="shortcut-item" @click="goWatchHistory">
						<u-icon name="eye" color="#fff" size="22"></u-icon>
						<text class="shortcut-label">{{ $t('user.watchHistory') }}</text>
					</view>
					<view class="shortcut-divider"></view>
					<view class="shortcut-item" @click="jumpView('/pages/user/integral/index')">
						<text class="shortcut-num dj-num">{{ userInfoStore.usable || 0 }}</text>
						<text class="shortcut-label">{{ $t('user.myPoints') }}</text>
					</view>
					<view class="shortcut-divider"></view>
					<view class="shortcut-item" @click="jumpView('/pages/user/share/index')">
						<text class="shortcut-num">{{ userInfoStore.team_count || 0 }}</text>
						<text class="shortcut-label">{{ $t('user.myTeam') }}</text>
					</view>
				</view>
			</view>
			
			<view class="vip_card" style="margin-top: 30rpx;">
				<!--  -->
				<view class="vip_box" v-if="iosIsPay">
					<view class="left">
						<view class="line1">{{ $t('user.vipBenefitTitle') }}</view>
						<view class="line2">{{ $t('user.vipBenefitDesc') }}</view>
					</view>
					<view class="right">
						<u-button :text="$t('user.myVip')" v-if="userInfoStore.is_vip == 1" :customStyle="buttonStyle"
							@click="openVip" />
						<u-button :text="$t('user.activateNow')" v-else :customStyle="buttonStyle" @click="openVip" />
					</view>
				</view>
				<view class="vip_box" v-else>
					<view class="left">
						<view class="line1">{{ $t('user.vipBenefitTitle') }}</view>
						<view class="line2">{{ $t('user.vipBenefitDesc') }}</view>
					</view>
					<view class="right">
						<u-button :text="$t('user.contactUs')" :customStyle="buttonStyle" @click="openVip" />
					</view>
				</view>
			</view>

			
			<!-- || !mggStatus -->
			<view class="menu_box" v-if="userInfoStore.mgg == 1 || !mggStatus">
				<view class="title">{{ $t('user.allFeatures') }}</view>
				<view class="menu-grid">
					<view v-if="item.id!=8"
						class="menu-item"
						v-for="(item, index) in menuListTwoI18n"
						:key="item.id"
						@click="menuItemClick(item.rid, item.text, item.path, item.docKey)">
						<view class="icon">
							<image class="image" :src="item.img" :style="{ width: item.width }" mode="widthFix" />
						</view>
						<view class="text">{{ item.text }}</view>
					</view>
				</view>
			</view>
			<view class="menu_box" v-else>
				<view class="title">{{ $t('user.allFeatures') }}</view>
				<view class="menu-grid">
					<view class="menu-item"
						v-for="(item, index) in menuListI18n"
						:key="item.id"
						@click="menuItemClick(item.rid, item.text, item.path, item.docKey)">
						<view class="icon">
							<image class="image" :src="item.img" :style="{ width: item.width }" mode="widthFix" />
						</view>
						<view class="text">{{ item.text }}</view>
					</view>
				</view>
			</view>
			<view class="copyright" v-if="copyrightData.length" @click="debugClick">
				<view class="item" v-for="(item, index) in copyrightData" :key="index">
					<image class="image" v-if="item.image" :src="item.image" mode="widthFix"></image>
					<!-- #ifdef MP-WEIXIN -->
					<text class="text">{{ item.name }}</text>
					<!-- #endif -->
					<!-- #ifdef H5 -->
					<a v-if="item.url" :href="item.url" target="_blank" rel="">
						<text class="text">{{ item.name }}</text>
					</a>
					<text v-else class="text">{{ item.name }}</text>
					<!-- #endif -->
				</view>
			</view>
			<view class="alert_box"
				v-if="platform != 'H5' && token && userInfoStore && configStore.system.mobile_switch == 1">
				<view class="item" v-if="!userInfoStore.verification.mobile">
					<text class="text">您还没有绑定手机号</text>
					<text class="btn" @click="alertBindButton('mobile')">去绑定</text>
				</view>
			</view>
			<u-modal :show="cdkey.show" :title="cdkey.title" :showCancelButton="true" @confirm="cdkeyConfirm"
				@cancel="cdkey.show = false">
				<view style="width: 100%;">
					<u-input v-model="cdkey.value" :customStyle="inputStyle" clearable placeholder="请输入兑换码"
						@change="inputChange" @blur="inputChange"></u-input>
				</view>
			</u-modal>
		</view>
	


		<!-- <tabBar v-if="tabChange" selectedIndex =3></tabBar> -->
		<CustomTabBar current="/pages/home/user" />
			<AppModal />
		</view>
</template>

<script>
	import {
		mapState,
		mapGetters,
		mapMutations,
		mapActions
	} from "vuex"
	import LangSwitcher from '@/components/LangSwitcher.vue'
	import CustomTabBar from '@/components/CustomTabBar.vue'
	import { primaryBtn } from '@/common/utils/buttonStyle.js'
	export default {
		components: { LangSwitcher, CustomTabBar },
		data() {
			return {
				isBgColor: `var(--dj-primary)`,
				isStatus: getApp().globalData.status,
				buttonStyle: primaryBtn({ height: '100%', fontSize: '24rpx', borderRadius: '32rpx' }),
				cardListTwo: [{
						id: 1,
						img: 'https://img.nymaite.com/video_short/images/watched.png',
						text: '最近观看',
						path: '/pages/home/watch'
					},
					{
						id: 4,
						img: 'https://img.nymaite.com/video_short/images/drama.png',
						text: '卡密兑换',
						path: ''
					},
					{
						id: 3,
						img: 'https://img.nymaite.com/video_short/images/money.png',
						text: '团队管理',
						path: '/pages/user/share/index'
					},
					{
						id: 2,
						img: 'https://img.nymaite.com/video_short/images/distributor.png',
						text: '高级团队',
						path: '/pages/user/dealer/index'
					},
				],
				cardList: [{
						id: 1,
						img: '/static/img/观看记录.png',
						text: '观看记录',
						path: '/pages/home/watch'
					},
					{
						id: 2,
						img: '/static/img/分销商管理.png',
						text: '高级团队',
						path: '/pages/user/dealer/index'
					},
					{
						id: 3,
						img: '/static/img/分享赚钱.png',
						text: '团队管理',
						path: '/pages/user/share/index'
					},
					{
						id: 4,
						img: '/static/img/卡密兑换.png',
						text: '卡密兑换',
						path: ''
					},
				],
				menuListTwo: [

					{
						id: 2,
						img: 'https://img.nymaite.com/video_short/images/Agreement.png',
						width: '32rpx',
						text: '用户协议',
						rid: 1,
						docKey: 'user_protocol'
					},

					{
						id: 5,
						img: 'https://img.nymaite.com/video_short/images/Contact.png',
						width: '28rpx',
						text: '联系我们',
						rid: 4,
						docKey: 'contact_us'
					},
					{
						id: 1,
						img: 'https://img.nymaite.com/video_short/images/yqhy.png',
						width: '32rpx',
						text: '邀请好友',
						rid: '',
						path: '/pages/user/share/poster'
					},
					// { id: 1, img: '/static/images/yqhy.png', width: '32rpx', text: '邀请好友', rid: '', path: '/pages/user/share/poster' },
					// #ifdef MP-WEIXIN
					{
						id: 8,
						img: 'https://img.nymaite.com/video_short/icons/list_2.png',
						width: '28rpx',
						text: '免广告',
						rid: ''
					},
					// #endif
					{
						id: 6,
						img: 'https://img.nymaite.com/video_short/images/About.png',
						width: '28rpx',
						text: '关于我们',
						rid: 5,
						docKey: 'about_us'
					},
					// {
					// 	id: 7,
					// 	img: 'https://img.nymaite.com/video_short/images/points.png',
					// 	width: '28rpx',
					// 	text: '获取积分',
					// 	rid: '',
					// 	path: '/pages/user/integral/task'
					// },
					{
						id: 4,
						img: 'https://img.nymaite.com/video_short/images/Notice.png',
						width: '32rpx',
						text: '法律声明',
						rid: 3,
						docKey: 'legal_notice'
					},
					{
						id: 3,
						img: 'https://img.nymaite.com/video_short/images/Privacy.png',
						width: '28rpx',
						text: '隐私协议',
						rid: 2,
						docKey: 'privacy_policy'
					},
				],
				menuList: [
					// #ifndef APP-PLUS
					{
						id: 1,
						img: 'https://img.nymaite.com/video_short/images/yqhyOr.png',
						width: '32rpx',
						text: '邀请好友',
						rid: '',
						path: '/pages/user/share/poster'
					},
					// #endif
					// {
					// 	id: 7,
					// 	img: 'https://img.nymaite.com/video_short/icons/list_2.png',
					// 	width: '28rpx',
					// 	text: '获取积分',
					// 	rid: '',
					// 	path: '/pages/user/integral/task'
					// },
					// #ifdef MP-WEIXIN
					{
						id: 8,
						img: 'https://img.nymaite.com/video_short/icons/list_2.png',
						width: '28rpx',
						text: '免广告',
						rid: ''
					},
					// #endif
					{
						id: 2,
						img: 'https://img.nymaite.com/video_short/icons/list_4.png',
						width: '28rpx',
						text: '用户协议',
						rid: 1,
						docKey: 'user_protocol'
					},
					{
						id: 3,
						img: 'https://img.nymaite.com/video_short/icons/list_2.png',
						width: '28rpx',
						text: '隐私协议',
						rid: 2,
						docKey: 'privacy_policy'
					},
					{
						id: 4,
						img: 'https://img.nymaite.com/video_short/icons/list_3.png',
						width: '28rpx',
						text: '法律声明',
						rid: 3,
						docKey: 'legal_notice'
					},
					{
						id: 5,
						img: 'https://img.nymaite.com/video_short/icons/list_5.png',
						width: '32rpx',
						text: '联系我们',
						rid: 4,
						docKey: 'contact_us'
					},
					{
						id: 6,
						img: 'https://img.nymaite.com/video_short/icons/list_3.png',
						width: '28rpx',
						text: '关于我们',
						rid: 5,
						docKey: 'about_us'
					},
				],
				copyrightData: this.$store.state.app.copyright || [], // 版权说明
				platform: this.$utils.platforms(),
				configStore: this.$store.state.app.config,
				userInfoStore: this.$store.state.user.userInfo,
				debug: {
					count: 0,
					timer: null
				},
				cdkey: {
					show: false,
					title: '卡密兑换',
					value: '',
				},
				inputStyle: {},
				//tab更新
				tabChange: false,
				//免广告支付判断
				buttonLoading: false,
				//免广告支付所需超参数
				dredge: { // 需要开通的参数
					mgg_id: '',
					total_fee: ''
				},
				mggStatus: "",
			}
		},
		computed: {
			...mapGetters("user", ["token", "userInfo"]),
			...mapGetters("app", ["config", "copyright", "richtext", "iosIsPay"]),
			menuListTwoI18n() {
				return this.menuListTwo.map(item => ({
					...item,
					text: this.menuItemText(item.id)
				}))
			},
			menuListI18n() {
				return this.menuList.map(item => ({
					...item,
					text: this.menuItemText(item.id)
				}))
			},
		},
		watch: {
			config: {
				deep: true,
				handler: function(newValue, oldValue) {
					this.configStore = newValue
				}
			},
			userInfo: {
				deep: true,
				handler: function(newValue, oldValue) {
					this.userInfoStore = newValue
				}
			},
			copyright(newValue, oldValue) {
				this.copyrightData = newValue
			},
			richtext(newValue, oldValue) {
				newValue && this.initMenuList(newValue)
			}
		},
		onTabItemTap() {
			this.$request('common.point', {
				item_id: 4,
				platform: this.$utils.platforms(),
				point_type: 5
			}, false).then(res => {

			})
		},
		onPullDownRefresh() {
			if (this.token) {
				this.getUserInfo().then(res => {
					uni.stopPullDownRefresh()
				}).catch(err => {
					uni.stopPullDownRefresh()
				})
			} else {
				uni.stopPullDownRefresh()
			}
		},
		onLoad() {
			if (!this.token) {
				this.$appModal({
					title: '系统提示',
					content: '请先登录后再使用全部功能',
					showCancel: false,
					success: res => {
						uni.navigateTo({ url: '/pages/login/login' });
					}
				});
				return;
			}
			uni.$on('updateUserInfo', () => {
				this.getUserInfo()
			})
			uni.$on('loginSuccess', this.refreshPage)
			this.richtext && this.initMenuList(this.richtext)
		},
		onShow() {
			this.tabChange = true
			this.token && this.getUserInfo()
		},
		onHide() {
			this.tabChange = false
		},
		onUnload() {
			uni.$off('updateUserInfo')
			uni.$off('loginSuccess', this.refreshPage)

		},
		methods: {
			...mapActions('user', ['getUserInfo', 'userinfo']),
			menuItemText(id) {
				const map = {
					1: this.$t('user.inviteFriends'),
					2: this.$t('user.userAgreement'),
					3: this.$t('user.privacyPolicy'),
					4: this.$t('user.legalNotice'),
					5: this.$t('user.contactUs'),
					6: this.$t('user.aboutUs'),
					7: this.$t('user.earnPoints'),
					8: this.$t('user.noAds'),
				}
				return map[id] || ''
			},
			goMai() {
				this.$request('common.point', {
					platform: this.$utils.platforms(),
					point_type: 7
				}, false).then(res => {

				})
			},
			// 调试
			debugClick() {
				// #ifdef MP-WEIXIN
				const env = wx.getAccountInfoSync().miniProgram.envVersion
				if (env != "release") {
					clearTimeout(this.debug.timer);
					this.debug.count++;
					this.debug.timer = setTimeout(() => {
						if (this.debug.count >= 5) {
							this.$appModal({
								title: '配置信息',
								content: `
									(env => ${env}) -
									(domain => ${this.$BASE_URL}) -
									(sign => ${this.$SIGN})
								`
							})
						}
						this.debug.count = 0;
					}, 500);
				}
				// #endif
			},
			alertBindButton(type) {
				if (type == 'wxmp') {
					console.log("绑定微信小程序");
				} else if (type == 'wxoa') {
					console.log("绑定微信公众号");
				} else if (type == 'mobile') {
					console.log("绑定手机号");
					this.jumpView('/pages/user/info/index')
				}
			},
			// 复制
			copyText(info) {
				uni.setClipboardData({
					data: String(info),
					success: () => {
						this.$u.toast('复制成功')
					}
				});
			},
			// 富文本ID
			initMenuList(data) {
				if (this.isStatus == 0) {
					console.log('进入蓝色')

					this.menuListTwo.map(item => {
						switch (item.id) {
							case 2:
								item.rid = data.user_protocol
								break;
							case 3:
								item.rid = data.privacy_protocol
								break;
							case 4:
								item.rid = data.legal_notice
								break;
							case 5:
								item.rid = data.contact_us
								break;
							case 6:
								item.rid = data.about_us
								break;
							case 8:
								item.rid = 100
								break;
						}
					})
				} else {
					console.log('进入橙色')

					this.menuList.map(item => {
						switch (item.id) {
							case 2:
								item.rid = data.user_protocol
								break;
							case 3:
								item.rid = data.privacy_protocol
								break;
							case 4:
								item.rid = data.legal_notice
								break;
							case 5:
								item.rid = data.contact_us
								break;
							case 6:
								item.rid = data.about_us
								break;
							case 8:
								item.rid = 100
								break;
						}
					})
				}

			},
			// 菜单列表点击
			goWatchHistory() {
				uni.switchTab({ url: '/pages/home/watch' })
			},
			menuItemClick(id, title, url, docKey) {

				if (url) {
					if (!this.token) return this.$u.toast('请先登录!')
					// TabBar 页面需用 switchTab
					const tabPages = ['/pages/home/index', '/pages/home/watch', '/pages/home/video', '/pages/home/user']
					if (tabPages.includes(url)) {
						uni.switchTab({ url })
					} else {
						this.jumpView(url)
					}
				} else {
					console.log(id, '这是id')
					// if(!id) return this.$u.toast('功能正在开发中')


					if (id == 100) {
						// #ifdef MP-WEIXIN
						if (!this.iosIsPay) return this.jumpView('/pages/user/info/contact')
						// #endif
						this.$appModal({
							title: '开通免广告权限',
							content: `开通免广告所需费用${this.dredge.total_fee}元`,
							success: res => {
								if (res.confirm) {
									console.log('用户点击确定');
									this.dredgeDealer()

								} else if (res.cancel) {
									console.log('用户点击取消');
								}
							}
						})


					} else {
						if (!id) return this.$u.toast('请先在后台-剧场管理-系统配置-协议配置中编辑协议，然后在剧场配置-基础配置中选择各个协议对应所编辑的协议')
						const obj = {
							id,
							title,
							docKey: docKey || ''
						}
						this.jumpView(`/pages/user/info/richtext?d=${encodeURIComponent(JSON.stringify(obj))}`)
					}

				}
			},
			// 会员中心
			openVip() {
				// #ifdef MP-WEIXIN
				if (!this.iosIsPay) return this.jumpView('/pages/user/info/contact')
				// #endif

				if (!this.token) return this.$u.toast('请先登录!')
				this.$request('common.point', {
					platform: this.$utils.platforms(),
					point_type: 6
				}, false).then(res => {

				})
				this.jumpView('/pages/user/member/index')
			},
			// 输入框验证
			inputChange() {
				let result = false
				if (!this.cdkey.value) {
					this.inputStyle = {
						borderColor: 'red !important'
					}
				} else {
					this.inputStyle = {}
					result = true
				}
				return result
			},
			// 卡密兑换
			cdkeyConfirm() {
				if (!this.inputChange()) return
				uni.showLoading({
					title: '兑换中'
				})

				this.$request('user.cdkey', {
					crypto: this.cdkey.value,
					platform: this.$utils.platforms()
				}).then(res => {
					if (res.code === 1) {
						this.getUserInfo()
						this.$u.toast(res.msg)
					}
					this.cdkey.value = ''
					this.cdkey.show = false
					// uni.hideLoading()
				}).catch(err => {
					this.$u.toast('兑换失败')
					this.cdkey.value = ''
					this.cdkey.show = false
					uni.hideLoading()
				})
			},
			// 功能区点击
			cardItemClick(item) {
				if (!item.path) {
					if (item.id === 4) {
						if (!this.token) return this.$u.toast('请先登录!')
						this.cdkey.show = true
					} else {
						this.$u.toast('暂未开放')
					}
				} else {
					if (item.id === 2) {
						// #ifdef MP-WEIXIN
						if (!this.iosIsPay) return this.jumpView('/pages/user/info/contact')
						// #endif
					}
					if (!this.token) return this.$u.toast('请先登录!')
					this.jumpView(item.path)
				}
			},
			refreshPage() {
				this.getUserInfo && this.getUserInfo()
				this.richtext && this.initMenuList(this.richtext)
			},

		}
	}
</script>

<style lang="scss" scoped>
	.page_content {
		position: relative;
		/* #ifdef H5 */
		padding-bottom: 100rpx;
		/* #endif */

		.head_content {
			position: fixed;
			top: 0;
			left: 0;
			right: 0;
			z-index: 99;
			background: transparent;
		}

		.main_content {
			padding: 24rpx 40rpx 152rpx 40rpx;
			padding-top: 160rpx;

			.userinfo_box {
				display: flex;
				flex-direction: column;
				align-items: center;
				position: relative;

				.avatar {
					width: 130rpx;
					height: 130rpx;
					border-radius: 50%;
					border: 2rpx solid #fff;
					overflow: hidden;
					margin-bottom: 20rpx; // 增加头像与下方内容的间距

					.image {
						width: 100%;
						height: 100%;
						border-radius: 50%;
					}
				}

				.info {
					flex: 1;
					margin-left: 0; // 移除左侧边距
					display: flex;
					flex-direction: column;
					align-items: center;
					width: 100%; // 占满宽度

					.nickname {
						display: flex;
						justify-content: center; // 昵称居中
						width: 100%; // 占满宽度

						.text {
							font-size: 34rpx;
							color: #000;
							font-weight: 900;
						}

						.image {
							width: 52rpx;
							margin-left: $dj-spacing-xs;
						}
					}

					.msg {
						margin-top: 10rpx; // 调整间距
						font-size: $dj-fs-sm;
						width: 100%; // 占满宽度
						justify-content: center; // 内容居中

						.text {
							color: #808080;
						}

						.copy {
							color: #5E5E5E;
							margin-left: $dj-spacing-xs;
							text-decoration: underline;
							display: inline-block;
						}
					}
				}
			}

			.vip_card {
				border-radius: 24rpx;
				overflow: hidden;
				margin: 24rpx 0;
				box-shadow: 0 8rpx 32rpx rgba(80, 72, 229, 0.25);

				.vip_box {
					position: relative;
					width: 100%;
					min-height: 152rpx;
					padding: 0 36rpx;
					box-sizing: border-box;
					/* fallback 背景（inline style 紫渐变在某些端可能未渲染时兜底）*/
					background: linear-gradient(141.96deg, #2a3599 0%, #7c3aed 100%);
					display: flex;
					align-items: center;
					justify-content: space-between;
					border-bottom: 1rpx solid rgba(255, 255, 255, 0.12);
					overflow: hidden;

					/* 玻璃质感装饰光环 — 右上角柔和光圈 */
					&::before {
						content: '';
						position: absolute;
						top: -80rpx;
						right: -80rpx;
						width: 240rpx;
						height: 240rpx;
						background: radial-gradient(closest-side, rgba(255,255,255,0.25), transparent);
						pointer-events: none;
						z-index: 0;
					}

					/* 微妙花纹背景（细斜线）增加质感 */
					&::after {
						content: '';
						position: absolute;
						inset: 0;
						background-image: linear-gradient(135deg, rgba(255,255,255,0.06) 25%, transparent 25%, transparent 50%, rgba(255,255,255,0.06) 50%, rgba(255,255,255,0.06) 75%, transparent 75%);
						background-size: 20rpx 20rpx;
						pointer-events: none;
						z-index: 0;
					}

					.left, .right { position: relative; z-index: 1; }

					.left {
						flex: 1;
						min-width: 0;
						color: #222;

						.line1 {
							font-size: $dj-fs-lg;
							font-weight: 700;
							color: #fff;
							line-height: 1.3;
							letter-spacing: 0.5rpx;
						}

						.line2 {
							font-size: $dj-fs-sm;
							margin-top: $dj-spacing-xs;
							color: rgba(255, 255, 255, 0.78);
							line-height: 1.4;
						}
					}

					.right {
						flex-shrink: 0;
						height: 64rpx;
						margin-left: 20rpx;

						:deep(.u-button) {
							background: $dj-gradient-primary !important;
							border: none !important;
							color: #fff !important;
							border-radius: 32rpx !important;
							padding: 0 28rpx !important;
							font-size: 26rpx !important;
							white-space: nowrap !important;
							height: 64rpx !important;
							line-height: 64rpx !important;
							&::after { border: none !important; }
						}
					}

					.left {
						flex: 1;
						min-width: 0;
					}
				}

				.integral_box {
					height: 80rpx;
					background: #fff;
					display: flex;
					align-items: center;
					justify-content: space-between;
					color: #555;
					padding: 0 32rpx;
					font-size: $dj-fs-sm;
					font-weight: 600;

					.left {
						color: $dj-primary;
						font-weight: 600;
					}

					.right {
						display: flex;
						align-items: center;

						.image {
							width: 30rpx;
							margin-right: $dj-spacing-xs;
							margin-left: 12rpx;
						}

						.text {}
					}
				}
			}

			.card_box {
				display: flex;
				flex-wrap: wrap;
				margin-top: 20rpx;
				border-radius: 16rpx;
				padding: 20rpx 0;
				background: #fff;

				.item {
					width: 25%;

					.icon {
						width: 84rpx;
						height: 84rpx;
						margin: 0 auto;

						.image {
							width: 100%;
							height: 100%;
						}
					}

					.text {
						font-size: $dj-fs-sm;
						color: #444;
						text-align: center;
						margin-top: $dj-spacing-xs;
					}
				}
			}

			.menu_box {
				background: #fff;
				border-radius: 24rpx;
				padding: $dj-spacing-lg 36rpx 36rpx;
				margin-top: $dj-spacing-base;
				box-shadow: 0 4rpx 24rpx rgba(0,0,0,0.06);

				.title {
					font-size: $dj-fs-md;
					color: $dj-text-primary;
					font-weight: 700;
					padding: 0 0 24rpx 0;
					position: relative;
					margin-bottom: 28rpx;
					letter-spacing: -0.5rpx;

					&::after {
						content: '';
						position: absolute;
						left: 0;
						bottom: 0;
						width: 64rpx;
						height: 4rpx;
						border-radius: 999rpx;
						background: var(--dj-gradient-primary);
					}
				}

				.menu-grid {
					display: grid;
					grid-template-columns: repeat(4, 1fr);
					gap: $dj-spacing-base $dj-spacing-sm;
					padding: 8rpx 0 4rpx;
				}

				.menu-item {
					display: flex;
					flex-direction: column;
					align-items: center;
					justify-content: center;
					padding: 24rpx 10rpx 20rpx;
					border-radius: 18rpx;
					transition: background 0.2s, transform 0.15s;

					&:active {
						background: $dj-bg-soft;
						transform: scale(0.96);
					}

					.icon {
						width: 44rpx;
						height: 44rpx;
						margin-bottom: 12rpx;
						display: flex;
						align-items: center;
						justify-content: center;

						.image {
							width: 100%;
							height: 100%;
						}
					}

					.text {
						font-size: $dj-fs-sm;
						color: #555;
						text-align: center;
						line-height: 1.4;
						word-break: break-word;
						width: 100%;
					}
				}
			}

			.copyright {
				margin-top: 60rpx;

				.item {
					display: flex;
					align-items: center;
					justify-content: center;
					line-height: 36rpx;

					a {
						text-decoration: none;
					}

					.image {
						width: 30rpx;
						margin-right: $dj-spacing-xs;
					}

					.text {
						font-size: $dj-fs-sm;
						color: rgba(#999, 0.5);
					}
				}
			}

			.alert_box {
				width: 100%;
				padding: 0 40rpx;
				position: absolute;
				bottom: 20rpx;
				left: 0;

				.item {
					padding: 20rpx;
					border-radius: 20rpx;
					background: rgba(0, 0, 0, 0.9);
					color: #fff;
					font-size: $dj-fs-base;
					display: flex;
					align-items: center;
					justify-content: space-between;

					.text {}

					.btn {
						background: linear-gradient(141.96deg, #2a3599 0%, #7c3aed 100%);
						padding: 8rpx 20rpx;
						border-radius: 10rpx;
					}
				}
			}
		}
	}

	/* ---- isStatus==1 新版用户头部 ---- */
	.user-mode {
		padding-top: 0 !important;
		background: #f0f2f5 !important;
	}

	.user-hero {
		position: relative;
		padding: 0 30rpx 30rpx;
		margin-bottom: 20rpx;
		background: $dj-gradient-primary;
		border-radius: 0 0 40rpx 40rpx;
		overflow: hidden;

		/* 状态栏占位 */
		/* #ifdef H5 */
		padding-top: 60rpx;
		/* #endif */
		/* #ifndef H5 */
		padding-top: calc(60rpx + var(--status-bar-height));
		/* #endif */
	}

	.hero-bg {
		position: absolute;
		top: -60rpx;
		right: -60rpx;
		width: 400rpx;
		height: 400rpx;
		border-radius: 50%;
		background: rgba(255,255,255,0.07);
		pointer-events: none;
	}

	.hero-content {
		display: flex;
		align-items: center;
		padding: 20rpx 0 30rpx;
	}

	.hero-avatar {
		position: relative;
		width: 110rpx;
		height: 110rpx;
		border-radius: 50%;
		border: 4rpx solid rgba(255,255,255,0.6);
		overflow: visible;
		margin-right: $dj-spacing-base;
		flex-shrink: 0;

		.image {
			width: 110rpx;
			height: 110rpx;
			border-radius: 50%;
		}

		.vip-badge {
			position: absolute;
			bottom: -6rpx;
			right: -6rpx;
			background: linear-gradient(135deg, #f6d365, #fda085);
			color: #fff;
			font-size: 18rpx;
			font-weight: 700;
			padding: 2rpx 10rpx;
			border-radius: 20rpx;
			letter-spacing: 1rpx;
		}
	}

	.hero-info {
		flex: 1;

		.hero-nickname {
			font-size: $dj-fs-xl;
			font-weight: 700;
			color: #fff;
			margin-bottom: 10rpx;
		}

		.hero-uid {
			font-size: $dj-fs-sm;
			color: rgba(255,255,255,0.7);
			display: flex;
			align-items: center;
			gap: $dj-spacing-sm;
		}

		.copy-btn {
			background: rgba(255,255,255,0.2);
			padding: 4rpx 16rpx;
			border-radius: 20rpx;
			font-size: 22rpx;
			color: rgba(255,255,255,0.9);
		}
	}

	.hero-arrow {
		flex-shrink: 0;
		padding-left: 10rpx;
	}

	.hero-shortcuts {
		display: flex;
		align-items: center;
		justify-content: space-around;
		background: rgba(255,255,255,0.12);
		border-radius: 20rpx;
		padding: 24rpx 20rpx;
		margin-top: 10rpx;

		.shortcut-item {
			display: flex;
			flex-direction: column;
			align-items: center;
			flex: 1;
		}

		.shortcut-divider {
			width: 1rpx;
			height: 40rpx;
			background: rgba(255,255,255,0.25);
		}

		.shortcut-num {
			font-size: $dj-fs-xl;
			font-weight: 700;
			color: #fff;
			margin-bottom: $dj-spacing-xs;
		}

		.shortcut-label {
			font-size: 22rpx;
			color: rgba(255,255,255,0.7);
		}
	}
</style>
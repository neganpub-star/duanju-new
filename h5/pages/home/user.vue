
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
					<view class="avatar" style="margin-bottom: 16rpx;">
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
								@click.stop="jumpView('/pages/user/integral/index')">{{ userInfoStore.usable || 0 }}积分</text>
							<view style="margin: 0 20rpx; height: 24rpx;width: 1rpx;background-color: rgba(0, 0, 0, 0.2);">
	
							</view>
							<text v-if="userInfoStore.is_vip == 1" class="copy" @click.stop="openVip"
								style="color: rgba(0, 0, 0, 0.7);text-decoration: none;">我的会员</text>
							<text v-else class="copy" @click.stop="openVip"
								style="color: rgba(0, 0, 0, 0.7);text-decoration: none;">开通会员</text>
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
							<text class="text">未登录</text>
						</view>
						<view class="msg" style="width: 100%;"></view>
					</view>
				</view>

			<view class="card_box" style="background: none;">
				<view class="item" v-for="(item, index) in cardListTwo" :key="item.id" @click="cardItemClick(item)">
					<view class="icon" style="margin-bottom: 32rpx;">
						<image class="image" :src="item.img" mode="aspectFill"></image>
					</view>
					<view class="text" style="color: rgba(0, 0, 0, 0.7);">{{ item.text }}</view>
				</view>
			</view>

			<view class="vip_card">
				<view class="vip_box" :style="`background: linear-gradient(141.96deg, #2a3599 0%, #7c3aed 100%)`" v-if="!iosIsPay">
					<view class="left">
						<view class="line1">影视通VIP</view>
						<view class="line2">开通VIP会员 专享剧集立即免费</view>
					</view>
					<view class="right">
						<u-button style="color: rgba(135, 141, 255, 1);" text="我的会员" v-if="userInfoStore.is_vip == 1"
							:customStyle="buttonStyle" @click="openVip" />
						<u-button style="color: rgba(135, 141, 255, 1);" text="立即开通" v-else :customStyle="buttonStyle"
							@click="openVip" />
					</view>
				</view>
				<view class="vip_box" :style="`background: linear-gradient(141.96deg, #2a3599 0%, #7c3aed 100%)`" v-else>
					<view class="left">
						<view class="line1">影视通VIP</view>
						<view class="line2"> 开通VIP会员 专享剧集立即免费</view>
					</view>
					<view class="right">
						<u-button style="color: rgba(135, 141, 255, 1);" text="立即开通" :customStyle="buttonStyle"
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
			<view class="vip_card" @click="jumpView('/pages/user/share/poster')">
				<view class="vip_box"
					style="padding: 20rpx 32rpx;background: linear-gradient(90deg, rgba(124, 124, 255, 1) 0%, rgba(181, 209, 255, 1) 100%);justify-content: start;">
					<view
						style="width: 112rpx;height: 112rpx;background: rgba(0, 0, 0, 0.2);border-radius: 16rpx;margin-right: 32rpx; display: flex;align-items: center;justify-content: center;">
						<image src="https://img.nymaite.com/video_short/images/yqyl.png"
							style="width: 40rpx;height: 40rpx;" mode=""></image>
					</view>
					<view class="left">
						<view class="line2" style="display: flex;justify-content: space-between;align-items: center;">
							<view>邀请有礼</view>
							<view><u-icon name="arrow-right" color="#fff" size="12" :bold="true"></u-icon></view>
						</view>
						<view class="line2">所有通过您注册的用户，都将会给您带来收益</view>
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
							<LangSwitcher style="margin-right: 16rpx;" />
						</view>
						<u-icon name="arrow-right" color="rgba(255,255,255,0.7)" size="18"></u-icon>
					</view>
				</view>
				<view class="hero-content" v-else @click="jumpView('/pages/login/login')">
					<view class="hero-avatar">
						<image class="image" src="https://img.nymaite.com/video_short/images/avatar.png" mode="aspectFill"></image>
					</view>
					<view class="hero-info">
						<view class="hero-nickname">点击登录</view>
						<view class="hero-uid">登录后享受更多功能</view>
					</view>
					<view class="hero-arrow">
						<u-icon name="arrow-right" color="rgba(255,255,255,0.7)" size="18"></u-icon>
					</view>
				</view>
				<!-- 快捷入口 -->
				<view class="hero-shortcuts" v-if="userInfoStore">
					<view class="shortcut-item" @click="jumpView('/pages/video/record')">
						<text class="shortcut-num">{{ userInfoStore.watch_count || 0 }}</text>
						<text class="shortcut-label">{{ $t('user.watchHistory') }}</text>
					</view>
					<view class="shortcut-divider"></view>
					<view class="shortcut-item" @click="jumpView('/pages/user/member/index')">
						<text class="shortcut-num">{{ userInfoStore.usable || 0 }}</text>
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
				<view class="integral_box" @click=" goMai();jumpView('/pages/user/integral/index')">
					<view class="right">
						<text class="text">{{ $t('user.myPoints') }}</text>
						<image class="image" src="https://img.nymaite.com/video_short/icons/integral.png"
							mode="widthFix"></image>
						<text class="text">{{ userInfoStore.usable || 0 }}</text>
					</view>
					<view v-if="iosIsPay" class="left">{{ $t('user.recharge') }}</view>
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
	export default {
		components: { LangSwitcher },
		data() {
			return {
				isBgColor: `#5E72F7`,
				isStatus: getApp().globalData.status,
				buttonStyle: {
					width: '100%',
					height: '100%',
					border: 'none',
					fontSize: '24rpx',
					color: '#a78bfa',
					background: '#2a3599',
					borderRadius: '8rpx',
					fontWeight: 'bold'
				},
				cardListTwo: [{
						id: 1,
						img: 'https://img.nymaite.com/video_short/images/watched.png',
						text: '最近观看',
						path: '/pages/video/record'
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
						path: '/pages/video/record'
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
					{
						id: 7,
						img: 'https://img.nymaite.com/video_short/images/points.png',
						width: '28rpx',
						text: '获取积分',
						rid: '',
						path: '/pages/user/integral/task'
					},
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
					{
						id: 7,
						img: 'https://img.nymaite.com/video_short/icons/list_2.png',
						width: '28rpx',
						text: '获取积分',
						rid: '',
						path: '/pages/user/integral/task'
					},
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
				uni.showModal({
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
			this.mggTrue()
			this.dealerLevelList()
			this.richtext && this.initMenuList(this.richtext)
		},
		onShow() {
			this.tabChange = true
			uni.setTabBarStyle({
				color: '#999',
				selectedColor: '#9354FF',
				backgroundColor: '#ffffff',
				borderStyle: 'black',
			});
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
			mggTrue() {

				this.$request('mgg.mggswitch', '', false).then(res => {
					if (res.code === 1) {
						console.log(res)
						this.mggStatus = res.data.switch

					}
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
							uni.showModal({
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
			menuItemClick(id, title, url, docKey) {

				if (url) {
					if (!this.token) return this.$u.toast('请先登录!')
					this.jumpView(url)
				} else {
					console.log(id, '这是id')
					// if(!id) return this.$u.toast('功能正在开发中')


					if (id == 100) {
						// #ifdef MP-WEIXIN
						if (!this.iosIsPay) return this.jumpView('/pages/user/info/contact')
						// #endif
						uni.showModal({
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
			//免广告支付
			dealerLevelList() {
				this.$request('mgg.level', '', false).then(res => {
					if (res.code === 1) {
						let item = res.data.list.filter(item => item.level == this.dredgeLevel)[0]
						this.dredge.mgg_id = res.data.list[0].id
						this.dredge.total_fee = res.data.list[0].price

					}
				})
			},
			dredgeDealer() {
				// #ifdef MP-WEIXIN
				if (!this.iosIsPay) return this.jumpView('/pages/user/info/contact')
				// #endif

				const buy = () => {
					this.buttonLoading = true
					uni.showLoading({
						title: '开通中...',
						mask: true
					})
					// #ifdef MP-TOUTIAO

					this.$request('mgg.createOrder', {
						...this.dredge,
						platform: 'douyinxcx'
					}).then(res => {
						if (res.code === 1) {
							this.callPay(res.data.order_sn, 'douyinxcx', res.data.platform)

						} else {
							this.buttonLoading = false
							uni.hideLoading()
						}
					}).catch(err => {
						this.buttonLoading = false
						uni.hideLoading()
					})
					// #endif
					// #ifndef MP-TOUTIAO
					this.$request('mgg.createOrder', {
						...this.dredge,
						platform: this.$utils.platforms()
					}).then(res => {
						if (res.code === 1) {


							this.callPay(res.data.order_sn, 'wechat', res.data.platform)

						} else {
							this.buttonLoading = false
							uni.hideLoading()
						}
					}).catch(err => {
						this.buttonLoading = false
						uni.hideLoading()
					})
					// #endif
				}


				buy()
			},
			//虚拟支付调起
			xGetPay(order_sn, payment, platform) {
				uni.login({
					provider: 'weixin',
					success: success => {
						if (success.errMsg === 'login:ok') {

							this.$request('common.xunipay', {
								order_sn,
								payment,
								platform,
								code: success.code
							}).then(res => {
								console.log(res)
								if (res.code === 1) {
									if (platform == 'H5') {
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
			getPay(order_sn, payment, platform) {
				this.$request('common.pay', {
					order_sn,
					payment,
					platform,
				}).then(res => {
					console.log(res)
					if (res.code === 1) {
						if (platform == 'H5') {
							const div = document.createElement('divpay');
							div.innerHTML = res.data.pay_data;
							document.body.appendChild(div);
						} else {
							this.pay(res.data.pay_data)
						}
					}
				})
			},
			// 发起支付请求
			callPay(order_sn, payment, platform) {
				// #ifdef MP-TOUTIAO
				var that = this
				this.$request('common.dypay', {
					order_sn,
					payment,
					platform,
				}).then(res => {
					console.log(res)
					if (res.code === 1) {

						if (platform == 'H5') {
							const div = document.createElement('divpay');
							div.innerHTML = res.data.pay_data;
							document.body.appendChild(div);
						} else {
							tt.pay({
								orderInfo: {
									order_id: res.data.pay_data.data.order_id,
									order_token: res.data.pay_data.data.order_token,
								},
								service: 5,
								success(res) {
									console.log(res, 'success')
									if (res.code == 0) {
										uni.showToast({
											title: '支付成功',
											icon: 'none',
											duration: 2000
										});
										that.buttonLoading = false
										that.getPageData()
										uni.hideLoading()

										// 支付成功处理逻辑，只有res.code=0时，才表示支付成功
										// 但是最终状态要以商户后端结果为准
									} else {
										uni.showToast({
											title: '支付失败',
											icon: 'none',
											duration: 2000
										});
										that.buttonLoading = false
										uni.hideLoading()
									}
								},
								fail(res) {
									console.log(res, 'fail')
									uni.showToast({
										title: '支付失败',
										icon: 'none',
										duration: 2000
									});
									that.buttonLoading = false
									uni.hideLoading()
									// 调起收银台失败处理逻辑
								},
							});
						}
					}
				})

				// #endif
				// #ifndef MP-TOUTIAO
				this.$request('common.ifxunipay').then(res => {

					if (res.data.xunipay_switch == 0) {
						this.getPay(order_sn, payment, platform)
					} else {
						// #ifdef MP-WEIXIN
						var iosd = wx.getSystemInfoSync()

						if (this.iosIsPay && iosd.platform == 'ios') {
							this.getPay(order_sn, payment, platform)

						} else {

							this.xGetPay(order_sn, payment, platform)
						}
						// #endif
						// #ifndef MP-WEIXIN
						this.getPay(order_sn, payment, platform)
						// #endif

					}
				})
				// #endif


			},
			// 发起 小程序/公众号 支付
			xunipay(pay) {
				var that = this
				// #ifdef MP-WEIXIN
				const SDKVersion = wx.getSystemInfoSync().SDKVersion

				if (that.compareVersion(SDKVersion, '2.19.2') >= 0 || wx.canIUse('requestVirtualPayment')) {
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
							that.buttonLoading = false
							that.getPageData()
							uni.hideLoading()


						},
						fail({
							errMsg,
							errCode
						}) {
							//console.error(errMsg, errCode)
							uni.showToast({
								title: errMsg,
								icon: 'none',
								duration: 2000
							});
							that.buttonLoading = false
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
					that.buttonLoading = false
					uni.hideLoading()

				}
				// #endif

			},
			pay(pay) {
				var that = this
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
						that.buttonLoading = false
						that.getPageData()
						uni.hideLoading()
					},
					fail: fail => {
						uni.showToast({
							title: '支付失败',
							icon: 'none',
							duration: 2000
						});
						that.buttonLoading = false
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
							that.buttonLoading = false
							that.getPageData()
							uni.hideLoading()
						} else {
							uni.showToast({
								title: '支付失败',
								icon: 'none',
								duration: 2000
							});
							that.buttonLoading = false
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
								that.buttonLoading = false
								that.getPageData()
								uni.hideLoading()

							},
							fail: fail => {
								if (fail.errCode === -8) {
									uni.showToast({
										title: '未安装微信客户端',
										icon: 'none',
										duration: 2000
									});
									that.buttonLoading = false
									uni.hideLoading()
								} else {
									uni.showToast({
										title: '支付失败',
										icon: 'none',
										duration: 2000
									});
									that.buttonLoading = false
									uni.hideLoading()
								}
							}
						})
					},
					fail: e => {
						that.payFail("获取iap支付通道失败")
					}
				});
				// #endif


			},
			//个人信息处理
			getPageData() {
				this.getUserInfo().then(res => {
					if (res.code === 1) {
						// this.userInfo = { avatar: res.data.avatar }
						this.userInfo = res.data

					}
				})
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
			refreshPage() {
				this.getUserInfo && this.getUserInfo()
				this.mggTrue && this.mggTrue()
				this.dealerLevelList && this.dealerLevelList()
				this.richtext && this.initMenuList(this.richtext)
			},

		}
	}
</script>

<style lang="scss" scoped>
	.page_content {
		position: relative;

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
							margin-left: 8rpx;
						}
					}

					.msg {
						margin-top: 10rpx; // 调整间距
						font-size: 24rpx;
						width: 100%; // 占满宽度
						justify-content: center; // 内容居中

						.text {
							color: #808080;
						}

						.copy {
							color: #5E5E5E;
							margin-left: 8rpx;
							text-decoration: underline;
							display: inline-block;
						}
					}
				}
			}

			.vip_card {
				border-radius: 16rpx;
				overflow: hidden;
				margin: 24rpx 0;
				box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.06);

				.vip_box {
					width: 100%;
					min-height: 144rpx;
					padding: 0 32rpx;
					box-sizing: border-box;
					background: #fff;
					display: flex;
					align-items: center;
					justify-content: space-between;
					border-bottom: 1rpx solid #f0f0f0;

					.left {
						color: #222;

						.line1 {
							font-size: 32rpx;
							font-weight: bold;
							color: #222;
						}

						.line2 {
							font-size: 24rpx;
							margin-top: 16rpx;
							color: #888;
						}
					}

					.right {
						width: 156rpx;
						height: 60rpx;

						:deep(.u-button) {
							background: linear-gradient(90deg, #5E72F7 0%, #9354FF 100%) !important;
							border: none !important;
							color: #fff !important;
							&::after { border: none !important; }
						}
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
					font-size: 24rpx;
					font-weight: 600;

					.left {
						color: #5E72F7;
						font-weight: 600;
					}

					.right {
						display: flex;
						align-items: center;

						.image {
							width: 30rpx;
							margin-right: 8rpx;
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
						font-size: 24rpx;
						color: #444;
						text-align: center;
						margin-top: 8rpx;
					}
				}
			}

			.menu_box {
				background: #fff;
				border-radius: 16rpx;
				padding: 30rpx 40rpx;
				margin-top: 24rpx;
				box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.06);

				.title {
					font-size: 30rpx;
					color: #222;
					font-weight: bold;
					padding: 0 0 20rpx 0;
					position: relative;
					margin-bottom: 20rpx;

					&::after {
						content: '';
						position: absolute;
						left: 0;
						bottom: 0;
						width: 100%;
						height: 1rpx;
						background-color: #f0f0f0;
					}
				}

				.menu-grid {
					display: grid;
					grid-template-columns: repeat(4, 1fr);
					gap: 20rpx;
					padding: 20rpx;
				}

				.menu-item {
					display: flex;
					flex-direction: column;
					align-items: center;
					justify-content: center;
					padding: 10rpx;

					.icon {
						width: 36rpx;
						height: 38rpx;
						margin-bottom: 16rpx;
						display: flex;
						align-items: center;
						justify-content: center;

						.image {
							width: 100%;
							height: 100%;
						}
					}

					.text {
						font-size: 24rpx;
						color: #444;
						text-align: center;
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
						margin-right: 8rpx;
					}

					.text {
						font-size: 24rpx;
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
					font-size: 28rpx;
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
		background: linear-gradient(135deg, #5E72F7 0%, #9354FF 100%);
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
		margin-right: 24rpx;
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
			font-size: 36rpx;
			font-weight: 700;
			color: #fff;
			margin-bottom: 10rpx;
		}

		.hero-uid {
			font-size: 24rpx;
			color: rgba(255,255,255,0.7);
			display: flex;
			align-items: center;
			gap: 16rpx;
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
			font-size: 36rpx;
			font-weight: 700;
			color: #fff;
			margin-bottom: 8rpx;
		}

		.shortcut-label {
			font-size: 22rpx;
			color: rgba(255,255,255,0.7);
		}
	}
</style>
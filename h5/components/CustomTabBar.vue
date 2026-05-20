<template>
	<view class="custom-tabbar">
		<view
			v-for="item in tabs"
			:key="item.path"
			class="tab-item"
			:class="{ active: isActive(item.path) }"
			@click="switchTo(item.path)"
		>
			<image
				:src="isActive(item.path) ? item.selectedIcon : item.icon"
				class="tab-icon"
				mode="widthFix"
			/>
			<text class="tab-text" :class="{ active: isActive(item.path) }">{{ $t(item.i18nKey) }}</text>
		</view>
	</view>
</template>

<script>
export default {
	name: 'CustomTabBar',
	props: {
		current: {
			type: String,
			default: ''
		}
	},
	data() {
		return {
			tabs: [
				{
					path: '/pages/home/index',
					icon: '/static/tabbar/home_default.png',
					selectedIcon: '/static/tabbar/home_selected.png',
					i18nKey: 'nav.home'
				},
				{
					path: '/pages/home/watch',
					icon: '/static/tabbar/watch_default.png',
					selectedIcon: '/static/tabbar/watch_selected.png',
					i18nKey: 'nav.watchlist'
				},
				{
					path: '/pages/home/video',
					icon: '/static/tabbar/recommend_default.png',
					selectedIcon: '/static/tabbar/recommend_selected.png',
					i18nKey: 'nav.discover'
				},
				{
					path: '/pages/home/user',
					icon: '/static/tabbar/user_default.png',
					selectedIcon: '/static/tabbar/user_selected.png',
					i18nKey: 'nav.profile'
				}
			]
		}
	},
	methods: {
		isActive(path) {
			return this.current === path
		},
		switchTo(path) {
			if (this.isActive(path)) return
			uni.switchTab({ url: path })
		}
	}
}
</script>

<style scoped>
.custom-tabbar {
	display: flex;
	flex-direction: row;
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	height: 100rpx;
	background-color: #fff;
	border-top: 1rpx solid #e5e5e5;
	z-index: 999;
	padding-bottom: env(safe-area-inset-bottom);
}
.tab-item {
	flex: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 10rpx 0 6rpx;
}
.tab-icon {
	width: 44rpx;
	height: 44rpx;
}
.tab-text {
	font-size: 19rpx;
	color: #999;
	margin-top: 3rpx;
	letter-spacing: 0.5rpx;
}
.tab-text.active {
	color: #9354FF;
}
</style>

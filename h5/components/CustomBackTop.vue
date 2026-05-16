
<template>
	<view class="CustomBackTop">
		<u-back-top :scroll-top="scrollTop" :duration="500" :customStyle="backTopStyle" :iconStyle="backTopIconStyle"></u-back-top>
	</view>
</template>

<script>
	export default {
		name: "CustomBackTop",
		props: {
			scrollTop: {
				type: Number,
				default: 0
			}
		},
		data() {
			return {
				backTopStyle: {
					background: 'rgba(0, 0, 0, 0.5)'
				},
				backTopIconStyle: {
					fontSize: '32rpx',
					color: '#fff'
				},
			};
		}
	}
</script>

<style>

</style>
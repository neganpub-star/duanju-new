

<template>
	<view class="page_content">
		<!-- #ifndef MP-TOUTIAO -->
		<view class="head_content">
			<CustomNavbar :title="navbarTitle"></CustomNavbar>
		</view>
		<!-- #endif -->
	
		<view class="main_content">
			<u-parse :content="info.content"></u-parse>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				options: '',
				info: ''
			}
		},
		onLoad(options) {
			if(options && options.d) {
				this.options = JSON.parse(decodeURIComponent(options.d))
				this.navbarTitle = this.options.title
				this.getRichText()
			}
		},
		methods: {
			getRichText() {
				const params = {}
				if (this.options.docKey) {
					params.key = this.options.docKey
					params.lang = uni.getStorageSync('app_lang') || 'zh-CN'
				} else {
					params.id = this.options.id
				}
				this.$request('common.richtext', params).then(res => {
					if(res.code === 1) {
						this.info = res.data
					}
				})
			}
		}
	}
</script>

<style lang="scss" scoped>
	.main_content {
		overflow-y: auto;
		padding: $dj-spacing-lg;
	}
</style>

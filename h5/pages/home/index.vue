
<template>
	<view class="page_content">
		<view class="header_new">
			<view class="search_bar" @click="jumpView('/pages/video/search')">
				<u-icon name="search" color="#aaa" size="28"></u-icon>
				<text class="search_placeholder">搜索短剧、演员...</text>
			</view>
		</view>

		<view class="tabs_container">
			<scroll-view class="tabs_new" scroll-x="true" :show-scrollbar="false">
				<view
					class="tab_item"
					v-for="tab in tabsList"
					:key="tab.id"
					@click="changeTabs(tab)"
					:class="{ active: tab.id === tabsActive.id }"
				>
					{{ tab.name }}
				</view>
			</scroll-view>
		</view>

		<scroll-view
			class="main_content"
			style="height: calc(100vh - 200rpx);"
			:scroll-y="true"
			:refresher-enabled="true"
			:refresher-threshold="100"
			:refresher-triggered="refreshStatus"
			@refresherrefresh="refreshHandle"
			@scrolltolower="bottomHandle"
		>
			<view class="content_box">
				<view class="list">
					<block v-for="(lItem, lIndex) in videoList" :key="lIndex">
						<view class="sitem" v-if="lIndex % 10 == 0" @click="maidian(lItem.id);openVideoDetail(lItem.id, lItem.title, lItem.image, lItem.description)">
							<image class="sitem_bg" :src="lItem.image" mode="aspectFill"></image>
							<view class="sitem_overlay"></view>
							<view class="sitem_content">
								<view class="sitem_info">
									<view class="title u-line-2">{{ lItem.title }}</view>
									<view class="text u-line-3">{{ lItem.description }}</view>
								</view>
								<view class="button" :class="{ collect: lItem.is_favorite == 1, animating: lItem.isAnimating }" @click.stop="handleCollect(lItem.id, lItem.is_favorite, lIndex)">
									<u-icon class="star_icon" :name="lItem.is_favorite == 1 ? 'star-fill' : 'star'" color="#fff" size="22"></u-icon>
									<text class="text">{{ lItem.is_favorite == 1 ? $t('home.following') : $t('home.follow') }}</text>
								</view>
							</view>
						</view>
						<template v-else >
							<view  class="item item1" v-if="lieAd&&lIndex % 6== 0 && lIndex!=0" >
								<view>
									<view class="img">
										<ad-custom :unit-id="lieid" bindload="adLoad" binderror="adError" @close="adClose"></ad-custom>
									</view>
									<view class="info">
										<view class="title u-line-1">请叫我可爱的广告</view>
										<view class="text u-line-1">刷到我说明缘分到了</view>
									</view>
								</view>
							</view>
							<view v-else class="item item1" :id="'item-' + lIndex" :class="{ 'visible': lItem.isInView }" :style="{ 'animation-delay': (lItem.animationIndex * 100) + 'ms' }" @click="maidian(lItem.id);openVideoDetail(lItem.id, lItem.title, lItem.image, lItem.description)">
								<view>
									<view class="img">
										<image class="image" :src="lItem.image" mode="aspectFill"></image>
										<view class="ep-badge" v-if="lItem.seriesCount">
											{{ lItem.isTv == 1 ? '更新至' : '全' }}{{ lItem.seriesCount }}集
										</view>
									</view>
									<view class="info">
										<view class="title u-line-1">{{ lItem.title }}</view>
										<view class="tags-row" v-if="lItem.tags">
											<text class="tag-pill" v-for="(tag, ti) in lItem.tags.split(',').filter(t => t.trim()).slice(0, 3)" :key="ti">{{ tag.trim() }}</text>
										</view>
									</view>
								</view>
							</view>
						</template>
					</block>
				</view>
				<view class="nodata" v-if="!videoList.length && status == 'nomore'">
					<u-empty mode="data" icon="http://cdn.uviewui.com/uview/empty/data.png" />
				</view>
				<view class="liststatus" v-else>
					<u-loadmore :status="status" :line="true" />
				</view>
			</view>
		</scroll-view>


		<!-- #ifdef MP-WEIXIN -->
		<zero-privacy :onNeed="false" :hideTabBar="true"></zero-privacy>
		<!-- #endif -->
		<CustomTabBar current="/pages/home/index" />
	</view>
</template>

<script>
	import { mapState, mapGetters, mapMutations, mapActions } from "vuex"
	import CustomTabBar from '@/components/CustomTabBar.vue'
	export default {
		components: { CustomTabBar },
		data() {
			return {
				navbarTitle: this.$store.state.app.title || '影视视频',
				tabsList: [], // 分类由后端获取
				tabsActive: {}, // 当前选中分类
				videoList: [],
				page: 1,
				pagesize: 10,
				status: 'loadmore',
				refreshStatus: true,
				isRefresh: false,
				platforms: this.$utils.platforms(),
				lieAd:false,
				lieid:"",
				chapingAd:false,
				chaAd:null,
				chapingId:"",
				AdNumber:0,
				navbarHeight: 0,
				observer: null,
			}
		},
		computed: {
			...mapGetters("user", ["token", "userInfo"]),
			...mapGetters("app", ["title"]),
		},
		watch: {
			title(newValue, oldValue) {
				this.navbarTitle = newValue
			},
		},
		onShow() {
			if (this.token) {
				this.getUserInfo && this.getUserInfo()
				this.getCategoryList && this.getCategoryList()
				this.getVideoList && this.getVideoList(true)
			}
			this.AdNumber=getApp().globalData.isADTc;
			 // #ifdef MP-WEIXIN
			if(this.userInfo.mgg == 1){
				this.lieAd = false;
				this.chapingAd = false;
			}else{
				if(this.chapingAd ){
					this.adInit()
				}
			}
			// #endif 
			this.getNavbarHeight();
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
			this.getCategoryList(); // 先获取分类
			// this.getVideoList(true); // 分类获取后再请求视频
			// #ifdef MP-WEIXIN
			this.getAD()
			// #endif 
			this.getNavbarHeight();
			uni.$on('loginSuccess', this.refreshPage)
		},
		onUnload() {
			if (this.observer) {
				this.observer.disconnect();
			}
			uni.$off('loginSuccess', this.refreshPage)
		},
		methods: {
			setupObservers() {
				if (this.observer) {
					this.observer.disconnect();
				}
			
				this.observer = uni.createIntersectionObserver(this, {
					thresholds: [0.1],
					observeAll: true
				});
				
				this.observer.relativeTo('.main_content');
			
				this.videoList.forEach((item, index) => {
					if (index % 10 !== 0 && !item.isInView) {
						this.observer.observe(`#item-${index}`, (res) => {
							if (res.intersectionRatio > 0) {
								this.$set(this.videoList[index], 'isInView', true);
								this.observer.unobserve(`#item-${index}`);
							}
						});
					}
				});
			},
			getNavbarHeight() {
				let info = uni.getSystemInfoSync();
				this.navbarHeight = info.statusBarHeight + 44;
			},
			adClose(){
				 this.AdNumber++
			},
			handleCollect(id, collect, index) {
				this.$set(this.videoList[index], 'isAnimating', true);
				setTimeout(() => {
					this.$set(this.videoList[index], 'isAnimating', false);
				}, 500);

				if(collect == 0) {
					const obj = { vid: id, type: 'favorite' }
					this.$request('video.addRecord', obj, false).then(res => {
						if(res.code === 1) {
							this.$set(this.videoList[index], 'is_favorite', 1)
						}
					})
				} else {
					const obj = { ids: id, type: 'favorite' }
					this.$request('video.deleteRecord', obj, false).then(res => {
						if(res.code === 1) {
							this.$set(this.videoList[index], 'is_favorite', 0)
						}
					})
				}
			},
			getAD(){
				// #ifdef MP-WEIXIN
				this.$request('common.wxguanggao', '',false).then(res => {
					if(res.code === 1) {
						if(res.data.list.gg_shouye_id && res.data.list.gg_shouye_switch == '1'){
							if(res.data.mgg != 1){
								this.chapingId = res.data.list.gg_shouye_id
								this.chapingAd = true
								this.adInit()
							}
						}
						if(res.data.list.gg_liebiao_id && res.data.list.gg_liebiao_switch == '1'){
							if(res.data.mgg != 1){
								this.lieid = res.data.list.gg_liebiao_id
								this.lieAd = true
							}
						}
					}
				})
				// #endif
			},
			adInit(){
				if(this.chaAd){
					  this.chaAd.show().catch((err) => {
					    console.error('插屏广告显示失败', err)
					  })
				}else{
					if (wx.createInterstitialAd) {
					  this.chaAd = wx.createInterstitialAd({
					    adUnitId: this.chapingId
					  })
					  this.chaAd.onLoad(() => {})
					  this.chaAd.onError((err) => {
					    console.error('插屏广告加载失败', err)
					  })
					  this.chaAd.onClose(() => {
						  this.AdNumber++
					  })
					}
				}
			},
			jumpView(url) {
				uni.navigateTo({ url });
			},
			openVideoDetail(id, title, image, description) {
				uni.navigateTo({
					url: `/pages/video/play?id=${id}&title=${title}&image=${image}&desc=${description}`
				});
			},
			maidian(ids){
				this.$request('common.point', { item_id: ids, platform:this.platforms, point_type:1 },false)
			},
			getCategoryList() {
				this.$request('video.classify', {}, false).then(res => {
					if (res.code === 1 && Array.isArray(res.data) && res.data.length) {
						// 新后端直接返回平铺分类列表，加一个”全部”选项
						const allTab = { id: '', name: this.$t('home.all') }
						this.tabsList = [allTab, ...res.data]
						this.tabsActive = this.tabsList[0]
						this.getVideoList(true)
					}
				});
			},
			getVideoList(isRefresh = false) {
				if (isRefresh) {
					this.page = 1;
					this.videoList = [];
					this.status = 'loadmore';
				}

				if (this.status === 'nomore' || this.status === 'loading') {
					this.refreshStatus = false;
					return;
				}

				this.status = 'loading';
				const obj = {
					categoryId: this.tabsActive.id || undefined,
					pageNum: this.page,
					pageSize: this.pagesize,
				}
				this.$request('video.list', obj).then(res => {
					if(res.code === 1) {
						// 兼容：后端直接返回数组
						const rows = Array.isArray(res.data) ? res.data : (res.data && res.data.rows ? res.data.rows : [])
						if(rows.length) {
							const lastAnimatedItem = [...this.videoList].reverse().find(item => item.animationIndex !== undefined);
							let nextAnimationIndex = lastAnimatedItem ? lastAnimatedItem.animationIndex + 1 : 0;
							const currentListLength = this.videoList.length;
							const newItems = rows.map((item, index) => {
								const newItem = { ...item, isInView: false };
								const lIndex = currentListLength + index;
								if (lIndex % 10 !== 0) {
									newItem.animationIndex = nextAnimationIndex++;
								}
								return newItem;
							});
							let list = this.videoList.concat(newItems);
							this.videoList = list;
							this.$nextTick(() => { this.setupObservers(); });
							if (rows.length < this.pagesize) {
								this.status = 'nomore';
							} else {
								this.status = 'loadmore';
							}
						} else {
							this.status = 'nomore';
						}
					}
					this.refreshStatus = false;
				}).catch(err => {
					this.status = 'loadmore';
					this.refreshStatus = false;
				})
			},
			refreshHandle() {
				this.refreshStatus = true;
				this.getVideoList(true);
			},
			bottomHandle() {
				if(this.status === 'loadmore') {
					this.page++;
					this.getVideoList();
				}
			},
			changeTabs(item) {
				if(item.id == this.tabsActive.id) return
				this.tabsActive = item;
				this.getVideoList(true);
			},
			refreshPage() {
				this.getUserInfo && this.getUserInfo()
				this.getCategoryList && this.getCategoryList()
				this.getVideoList && this.getVideoList(true)
			}
		}
	}
</script>

<style lang="scss" scoped>
	.page_content {
		background-color: #f4f4f4;
		height: 100vh;
		display: flex;
		flex-direction: column;
		/* #ifdef H5 */
		padding-bottom: 100rpx;
		/* #endif */
	}

	.header_new {
		background: linear-gradient(90deg, #5E72F7 0%, #9354FF 100%);
		padding: 20rpx 30rpx 60rpx;
		border-bottom-left-radius: 25rpx;
		border-bottom-right-radius: 25rpx;

		/* #ifdef H5 */
		padding-top: 20rpx;
		/* #endif */
		/* #ifndef H5 */
		padding-top: calc(20rpx + var(--status-bar-height));
		/* #endif */
	}

	.search_bar {
		display: flex;
		align-items: center;
		background: rgba(255,255,255,0.92);
		border-radius: 40rpx;
		padding: 18rpx 28rpx;
		gap: 16rpx;

		.search_placeholder {
			font-size: 28rpx;
			color: #aaa;
			flex: 1;
		}
	}

	.tabs_container {
		margin: -50rpx 30rpx 0 30rpx;
		background-color: #fff;
		border-radius: 30rpx;
		padding: 10rpx 0;
		box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.05);
		position: relative;
		z-index: 1;
		opacity: 0;
		animation: tabs-animation 0.6s 0.2s ease-out forwards;
		transition: all 0.3s ease-in-out;
	}

	@keyframes tabs-animation {
		from {
			opacity: 0;
			transform: translateY(-20rpx);
		}
		to {
			opacity: 1;
			transform: translateY(0);
		}
	}

	.tabs_new {
		display: flex;
		align-items: center;
		white-space: nowrap;
		padding: 10rpx 20rpx;
		width: 100%;
		box-sizing: border-box;
	}

	.tab_item {
		display: inline-flex;
		align-items: center;
		justify-content: center;
		font-size: 28rpx;
		position: relative;
		padding: 14rpx 28rpx;
		color: #999;
		font-weight: 400;
		flex-shrink: 0;
		border-radius: 40rpx;
		transition: all 0.25s ease;

		&.active {
			color: #fff;
			font-weight: 600;
			background: linear-gradient(90deg, #5E72F7 0%, #9354FF 100%);
			box-shadow: 0 4rpx 14rpx rgba(94, 114, 247, 0.35);
		}
	}

	.main_content {
		flex: 1;
		overflow-y: auto;
		margin-top: -10rpx;
		padding-top: 10rpx;
	}

	.content_box {
		padding: 20rpx 40rpx;
	}

	.list {
		display: flex;
		flex-wrap: wrap;
		justify-content: space-between;
	}

	.sitem {
		width: 100%;
		height: 520rpx;
		border-radius: 30rpx;
		overflow: hidden;
		margin-bottom: 40rpx;
		position: relative;
		transform: translateY(20px);
		opacity: 0;
		animation: sitem-fade-in 0.6s 0.2s ease-out forwards;
		box-shadow: 0 10rpx 30rpx rgba(0,0,0,0.1);

		.sitem_bg {
			position: absolute;
			width: 100%;
			height: 100%;
			z-index: 1;
			transition: transform 0.4s ease;
		}
		
		&:hover .sitem_bg {
			transform: scale(1.05);
		}

		.sitem_overlay {
			position: absolute;
			width: 100%;
			height: 100%;
			z-index: 2;
			background: linear-gradient(to top, rgba(0, 0, 0, 0.7) 0%, rgba(0, 0, 0, 0.1) 50%, rgba(0,0,0,0) 100%);
		}

		.sitem_content {
			position: relative;
			z-index: 3;
			width: 100%;
			height: 100%;
			padding: 40rpx 30rpx;
			display: flex;
			flex-direction: column;
			justify-content: flex-end;
			color: #fff;
		}

		.sitem_info {
			.title {
				font-size: 40rpx;
				font-weight: 700;
				margin-bottom: 15rpx;
				text-shadow: 0 2rpx 4rpx rgba(0,0,0,0.5);
			}
			.text {
				font-size: 28rpx;
				color: rgba(255,255,255,0.85);
				line-height: 1.5;
				text-shadow: 0 1rpx 2rpx rgba(0,0,0,0.5);
			}
		}
		
		.button {
			align-self: flex-start;
			margin-top: 30rpx;
			height: 80rpx;
			display: flex;
			align-items: center;
			justify-content: center;
			font-size: 30rpx;
			background: rgba(255, 255, 255, 0.25);
			border: 1rpx solid rgba(255, 255, 255, 0.3);
			border-radius: 40rpx;
			padding: 0 40rpx;
			color: #fff;
			backdrop-filter: blur(10px);
			transition: all 0.2s ease-out;

			.text {
				margin-left: 12rpx;
				font-weight: 500;
			}

			&:active {
				transform: scale(0.95);
			}
			
			&.collect {
				background: linear-gradient(90deg, #5E72F7 0%, #9354FF 100%);
				border: none;
			}

			&.animating .star_icon {
				animation: star-pop 0.5s ease-out;
			}
		}
	}

	@keyframes sitem-fade-in {
		to {
			transform: translateY(0);
			opacity: 1;
		}
	}
	
	.item {
		width: calc((100% - 50rpx) / 3);
		margin-bottom: 40rpx;
		
		/* 动画初始状态 */
		opacity: 0;
		/* &.visible 会应用 keyframe 动画, 不再需要 transition */
		/* transform: perspective(1000px) rotateY(90deg); */
		/* transform-origin: right center; */
		/* transition: transform 0.6s ease-out, opacity 0.6s ease-out; */

		&.visible {
			animation: flip-720-in-fast 0.35s ease-out forwards;
		}

		&.item1 {
			// 下列选择器是为了处理特殊布局，现改由flexbox的space-between处理
			// &:nth-child(10n),
			// &:nth-child(10n-3),
			// &:nth-child(10n-6) {
			// 	margin-right: 0;
			// }
		}
		
		.img {
			width: 100%;
			height: 280rpx;
			border-radius: 20rpx;
			overflow: hidden;
			position: relative;

			.image {
				width: 100%;
				height: 100%;
			}

			.ep-badge {
				position: absolute;
				right: 0;
				bottom: 0;
				background: rgba(0, 0, 0, 0.55);
				color: #fff;
				font-size: 20rpx;
				padding: 4rpx 10rpx;
				border-radius: 10rpx 0 0 0;
				white-space: nowrap;
				line-height: 1.4;
			}
		}

		.info {
			margin-top: 14rpx;

			.title {
				font-size: 28rpx;
				color: #111;
				font-weight: 700;
				line-height: 1.3;
			}

			.tags-row {
				display: flex;
				flex-wrap: wrap;
				gap: 8rpx;
				margin-top: 10rpx;

				.tag-pill {
					font-size: 20rpx;
					color: #888;
					background: #f2f2f2;
					border-radius: 6rpx;
					padding: 4rpx 12rpx;
					line-height: 1.4;
				}
			}
		}
	}

	.nodata {
		padding-top: 100rpx;
	}
	

	@keyframes star-pop {
		0% { transform: scale(1); }
		30% { transform: scale(0.8); }
		60% { transform: scale(1.4) rotate(15deg); color: #ffeb3b; }
		100% { transform: scale(1) rotate(0deg); }
	}
	
	@keyframes flip-720-in-fast {
		from {
			transform: translateY(16px) scale(0.97);
			opacity: 0;
		}
		to {
			transform: translateY(0) scale(1);
			opacity: 1;
		}
	}
</style>
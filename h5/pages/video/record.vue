

<template>
	<view class="page_content">
		<view class="head_content">
			<!-- #ifndef MP-TOUTIAO -->
				<CustomNavbar :title="$t('watch.watchHistory')"></CustomNavbar>
			<!-- #endif -->
			
			<view class="tabs_box">
				<u-tabs :scrollable="false" :lineWidth="0" :list="tabList" :current="contentCurrent" :activeStyle="tabsActiveStyle" :inactiveStyle="tabsInactiveStyle" lineColor="#5E72F7" @change="changeContent($event, 1)" />
			</view>
		</view>
		<view class="main_content" v-if="contentList && contentList.length">
			<swiper style="height: 100%" :skip-hidden-item-layout="true" :current="contentCurrent" @change="changeContent($event, 2)">
				<swiper-item style="height: 100%;" v-for="(item, index) in contentList" :key="item.id">
					<scroll-view style="height: 100%" :scroll-y="true" :refresher-enabled="true" :refresher-threshold="100" :refresher-triggered="refreshStatus" @refresherrefresh="refreshHandle" @scrolltolower="bottomHandle" @scroll="scrollHandle">
						<view class="content_box">
							<view class="list">
								<view class="item" v-for="(lItem, lIndex) in item.list" :key="lIndex" @click="openVideoDetail(lItem.video.id, lItem.video.display_title||lItem.video.title, lItem.video.image, lItem.video.display_desc||lItem.video.description)">
									<view class="img">
										<image class="image" :src="lItem.video.image" mode="aspectFill"></image>
									</view>
									<view class="info">
										<view class="title u-line-1">{{ lItem.video.display_title||lItem.video.title }}</view>
										<view class="text1 u-line-2">{{ lItem.video.display_desc||lItem.video.description }}</view>
										<view class="text2">{{ $t('watch.watchedTo') }}{{ lItem.episode.name }} / {{ $t('video.totalEpisodes', [lItem.video.episodes]) }}</view>
										<view class="btns">
											<view class="button" v-if="item.id == 1" :class="{ collect: lItem.is_favorite == 1 }" hover-class="active" :hover-start-time="0" :hover-stay-time="200" @click.stop="handleCollect(lItem.vid, lItem.is_favorite, lIndex)">
												<u-icon :name="lItem.is_favorite == 1 ? 'star-fill' : 'star'" color="#eee" size="18"></u-icon>
												<text class="text">{{ lItem.is_favorite == 1 ? $t('home.following') : $t('home.follow') }}</text>
											</view>
											<view class="button" v-else hover-class="active" :hover-start-time="0" :hover-stay-time="200">
												<u-icon name="play-right-fill" color="#eee" size="18"></u-icon>
												<text class="text">{{ $t('watch.watchNow') }}</text>
											</view>
										</view>
									</view>
								</view>
							</view>
							<view class="nodata" v-if="!item.list.length && item.status == 'nomore'">
								<u-empty mode="data" icon="http://cdn.uviewui.com/uview/empty/data.png" :text="$t('common.noData')" />
							</view>
							<view class="liststatus" v-else>
								<u-loadmore :status="item.status" :line="true" :nomoreText="$t('home.noMore')" />
							</view>
						</view>
					</scroll-view>
				</swiper-item>
			</swiper>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				navbarTitle: '影视视频',
				tabsActiveStyle: {
					color: `#5E72F7`,
					fontSize: '32rpx',
					fontWeight: 'bold',
				},
				tabsInactiveStyle: {
					color: '#666666'
				},
				contentList: [
					{ id: 1, nameKey: 'watch.watchHistory', type: 'log', list: [], page: 1, pagesize: 10, status: 'loadmore' },
					{ id: 2, nameKey: 'watch.myFollowing', type: 'favorite', list: [], page: 1, pagesize: 10, status: 'loadmore' },
				],
				contentCurrent: 0,
				refreshStatus: true,
				isRefresh: false,
			}
		},
		computed: {
			tabList() {
				return this.contentList.map(item => ({ ...item, name: this.$t(item.nameKey) }))
			}
		},
		onLoad() {
			this.getPlayRecordList()
		},
		methods: {
			// 	收藏
			handleCollect(vid, collect, index) {
				if(collect == 0) {
					const obj = {
						vid,
						type: 'favorite'
					}
					this.$request('video.addRecord', obj, false).then(res => {
						if(res.code === 1) {
							this.contentList[this.contentCurrent].list[index].is_favorite = 1
						}
					})
				} else {
					const obj = {
						ids: vid,
						type: 'favorite'
					}
					this.$request('video.deleteRecord', obj, false).then(res => {
						if(res.code === 1) {
							this.contentList[this.contentCurrent].list[index].is_favorite = 0
						}
					})
				}
			},
			// 获取播放记录
			getPlayRecordList() {
				let  status = uni.getSystemInfoSync().uniPlatform
				
				const obj = {
					type: this.contentList[this.contentCurrent].type,
					page: this.contentList[this.contentCurrent].page,
					pagesize: this.contentList[this.contentCurrent].pagesize,
					platform: status == 'mp-weixin'? 2 :1,
				}
				this.contentList[this.contentCurrent].status = 'loading'
				this.$request('video.getRecord', obj).then(res => {
					if(res.code === 1) {
						if(res.data && res.data.length) {
							this.contentList[this.contentCurrent].list = this.contentList[this.contentCurrent].list.concat(res.data)
							if(res.data.length < this.contentList[this.contentCurrent].pagesize) {
								this.contentList[this.contentCurrent].status = 'nomore'
							} else {
								this.contentList[this.contentCurrent].status = 'loadmore'
							}
						} else {
							this.contentList[this.contentCurrent].page > 1 && this.contentList[this.contentCurrent].page--
							const timer = setTimeout(() => {
								this.contentList[this.contentCurrent].status = 'nomore'
								clearTimeout(timer)
							}, 500)
						}
					}
					this.refreshStatus = false
					this.isRefresh = false
				}).catch(err => {
					this.refreshStatus = false
					this.isRefresh = false
				})
			},
			// 下拉刷新
			refreshHandle() {
				this.refreshStatus = true
				if(!this.isRefresh) {
					this.isRefresh = true
					this.contentList[this.contentCurrent].page = 1
					this.contentList[this.contentCurrent].list = []
					this.getPlayRecordList()
				}
			},
			// 滚动监听
			scrollHandle(e) {
				
			},
			// 触底滚动
			bottomHandle() {
				this.contentList[this.contentCurrent].page++
				this.getPlayRecordList()
			},
			// 切换分类
			changeContent(e, i) {
				const current = i === 1 ? e.index : e.detail.current
				if(current == this.contentCurrent) return
				this.contentCurrent = current
				!this.contentList[this.contentCurrent].list.length && this.getPlayRecordList()
			}
		}
	}
</script>

<style lang="scss" scoped>
	.page_content {
		overflow: hidden;
		
		.head_content {
			.tabs_box {
				padding: 0 100rpx;
			}
		}
		
		.main_content {
			overflow: hidden;

			.content_box {
				padding: 20rpx 40rpx 60rpx 40rpx;
				
				.list {

					.item {
						margin-bottom: 40rpx;
						display: flex;
						align-items: center;
						
						.img {
							width: 206rpx;
							height: 276rpx;
							border-radius: 20rpx;
							overflow: hidden;
							
							.image {
								width: 100%;
								height: 100%;
							}
						}
						
						.info {
							flex: 1;
							margin-left: 40rpx;
							
							.title {
								font-size: 32rpx;
								color: #000;
								font-weight: 700;
							}
							
							.text1 {
								height: 68rpx;
								font-size: 24rpx;
								color: #666;
								margin-top: 4rpx;
								line-height: 34rpx;
								margin: 10rpx 0;
							}
							
							.text2 {
								font-size: 24rpx;
								color: #111;
								line-height: 34rpx;
							}
							
							.btns {
								margin-top: 20rpx;
								display: flex;
								align-items: center;
								justify-content: space-between;
								
								.button {
									width: calc((100% - 30rpx) / 2);
									height: 60rpx;
									display: flex;
									align-items: center;
									justify-content: center;
									font-size: 28rpx;
									background: linear-gradient(90deg, #5E72F7 0%, #9354FF 100%);
									border-radius: 10rpx;
									color: #fff;

									&.collect {
										opacity: 0.7;
									}

									&.active {
										opacity: 0.85;
									}

									.text {
										margin-left: 8rpx;
									}
								}
							}
						}
					}
				}
				
				.nodata {
					padding: 15vh 0;
				}
			}
		}
	}
</style>
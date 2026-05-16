

<template>
	<view class="page_content">
		<view class="header-fixed">
			<view class="header-tabs">
				<view class="tab-item" :class="{ active: contentCurrent === 0 }" @click="changeContent(0)">观看记录</view>
				<view class="tab-item" :class="{ active: contentCurrent === 1 }" @click="changeContent(1)">我的追剧</view>
			</view>
		</view>

		<scroll-view class="main_content" :style="{ paddingTop: headerHeight + 'px' }" :scroll-y="true" :refresher-enabled="true" :refresher-threshold="100" :refresher-triggered="refreshStatus" @refresherrefresh="refreshHandle" @scrolltolower="bottomHandle">
			<view class="list">
				<view
					class="item-card"
					v-for="(lItem, lIndex) in currentList"
					:key="lItem.id"
					@click="openVideoDetail(lItem.video.id, lItem.video.title, lItem.video.image, lItem.video.description)"
					:class="{ 'is-removing': lItem.isRemoving }"
					:style="{ animationDelay: `${lIndex * 0.08}s` }"
				>
					<image class="cover-image" :src="lItem.video.image" mode="aspectFill"></image>
					<view class="info-wrapper">
						<view class="info-content">
							<text class="title u-line-1">{{ lItem.video.title }}</text>
							<view class="progress-box" v-if="contentCurrent === 0">
								<view class="progress-bar">
									<view class="progress-value" :style="{ width: getProgress(lItem) + '%' }"></view>
								</view>
								<text class="progress-text">已看至 {{ getProgress(lItem) }}% ({{ lItem.episode.name }})</text>
							</view>
							<text class="desc u-line-1" v-else>{{ lItem.video.description || '暂无简介' }}</text>
						</view>
						<view class="actions">
							<view class="action-btn" @click.stop="handleAction(lItem, lIndex)">
								{{ contentCurrent === 0 ? '继续观看' : '取消追剧' }}
							</view>
						</view>
					</view>
				</view>
			</view>
			<view class="nodata" v-if="!currentList.length && loadStatus == 'nomore'">
				<u-empty mode="data" icon="http://cdn.uviewui.com/uview/empty/data.png" text="暂无记录"></u-empty>
			</view>
			<view class="liststatus" v-else>
				<u-loadmore :status="loadStatus" :line="true" />
			</view>
		</scroll-view>
	</view>
</template>

<script>
	import { mapState, mapGetters, mapMutations, mapActions } from "vuex"
	
	export default {
		data() {
			return {
				headerHeight: 0,
				contentList: [
					{ id: 1, name: '最近观看记录', type: 'log', list: [], page: 1, pagesize: 10, status: 'loadmore' },
					{ id: 2, name: '我的追剧记录', type: 'favorite', list: [], page: 1, pagesize: 10, status: 'loadmore' },
				],
				contentCurrent: 1,
				refreshStatus: false,
				isRefresh: false,
			}
		},
		computed: {
			...mapGetters("user", ["userInfo"]),
			currentList() {
				return this.contentList[this.contentCurrent].list;
			},
			loadStatus() {
				return this.contentList[this.contentCurrent].status;
			}
		},
		onReady() {
			const query = uni.createSelectorQuery().in(this);
			query.select('.header-fixed').boundingClientRect(data => {
				if (data) {
					this.headerHeight = data.height;
				}
			}).exec();
		},
		onShow() {
			uni.setTabBarStyle({
				color: '#999',
				selectedColor: '#9354FF',
				backgroundColor: '#ffffff',
				borderStyle: 'black',
			});
			this.refreshHandle();
		},
		methods: {
			getProgress(item) {
				if (!item || !item.video || !item.episode || !item.episode.name || !item.video.episodes || item.video.episodes <= 0) {
					return 0;
				}
				
				// 从 "第X集" 或 "更新至X集" 这样的字符串中提取数字
				const match = item.episode.name.match(/\d+/);
				if (!match) {
					return 0;
				}
				
				const episodeNumber = parseInt(match[0], 10);
				const totalEpisodes = item.video.episodes;
				
				if (isNaN(episodeNumber) || episodeNumber <= 0) {
					return 0;
				}
				
				const percentage = Math.floor((episodeNumber / totalEpisodes) * 100);
				// 确保百分比在 0-100 之间
				return Math.max(0, Math.min(percentage, 100));
			},
			openVideoDetail(id, title, image, description) {
				uni.navigateTo({
					url: `/pages/video/play?id=${id}&title=${title}&image=${image}&desc=${description}`
				});
			},
			handleAction(item, index) {
				if (this.contentCurrent === 0) {
					this.openVideoDetail(item.video.id, item.video.title, item.video.image, item.video.description);
				} else {
					uni.showModal({
						title: '提示',
						content: `确定要取消追剧《${item.video.title}》吗？`,
						success: (res) => {
							if (res.confirm) {
								this.unfavorite(item, index);
							}
						}
					});
				}
			},
			unfavorite(item, index) {
				this.$set(item, 'isRemoving', true);
				const obj = { ids: item.vid, type: 'favorite' };
				this.$request('video.deleteRecord', obj, false).then(res => {
					if(res.code === 1) {
						setTimeout(() => {
							this.contentList[this.contentCurrent].list.splice(index, 1);
						}, 400);
					} else {
						this.$set(item, 'isRemoving', false);
					}
				});
			},
			changeContent(index) {
				if (this.contentCurrent === index) return;
				this.contentCurrent = index;
				if (this.contentList[this.contentCurrent].list.length === 0) {
					this.getPlayRecordList();
				}
			},
			getPlayRecordList(isRefresh = false) {
				const currentTab = this.contentList[this.contentCurrent];
				if (currentTab.status === 'loading' && !isRefresh) return;
				
				currentTab.status = 'loading';
				if (isRefresh) {
					currentTab.page = 1;
				}

				const obj = {
					type: currentTab.type,
					page: currentTab.page,
					pagesize: currentTab.pagesize,
					platform: uni.getSystemInfoSync().uniPlatform === 'mp-weixin' ? 2 : 1,
				};
				
				this.$request('video.getRecord', obj).then(res => {
					if(res.code === 1) {
						const data = res.data || [];
						if (isRefresh) {
							currentTab.list = data.map(item => ({ ...item, isRemoving: false }));
						} else {
							currentTab.list.push(...data.map(item => ({ ...item, isRemoving: false })));
						}
						if(data.length < currentTab.pagesize) {
							currentTab.status = 'nomore';
						} else {
							currentTab.status = 'loadmore';
						}
					} else {
						currentTab.status = 'nomore';
					}
					if (isRefresh) {
						this.refreshStatus = false;
					}
				}).catch(() => {
					currentTab.status = 'nomore';
					if (isRefresh) {
						this.refreshStatus = false;
					}
				});
			},
			refreshHandle() {
				this.refreshStatus = true;
				this.getPlayRecordList(true);
			},
			bottomHandle() {
				const currentTab = this.contentList[this.contentCurrent];
				if (currentTab.status === 'loadmore') {
					currentTab.page++;
					this.getPlayRecordList();
				}
			},
		}
	}
</script>

<style lang="scss" scoped>
	.page_content {
		background-color: #f8f9fa;
		height: 100vh;
		display: flex;
		flex-direction: column;
	}

	.header-fixed {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		z-index: 100;
		background-color: #fff;
		box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.05);
		
		/* 拆分 padding 避免覆盖问题 */
		padding-left: 30rpx;
		padding-right: 30rpx;
		padding-bottom: 20rpx;
		
		/* 默认 H5 或其他环境 */
		padding-top: 20rpx; 
		
		/* #ifndef H5 */
		padding-top: calc(20rpx + var(--status-bar-height));
		/* #endif */
	}

	.header-tabs {
		display: flex;
		justify-content: center;
		align-items: center;
		gap: 80rpx;
	}

	.tab-item {
		font-size: 30rpx;
		color: #666;
		padding: 15rpx 0;
		position: relative;
		transition: all 0.3s ease;
		
		&.active {
			font-size: 34rpx;
			font-weight: 600;
			color: #333;

			&::after {
				content: '';
				position: absolute;
				bottom: 0;
				left: 50%;
				transform: translateX(-50%);
				width: 50rpx;
				height: 8rpx;
				background: linear-gradient(90deg, #5E72F7 0%, #9354FF 100%);
				border-radius: 4rpx;
			}
		}
	}

	.main_content {
		flex: 1;
		overflow-y: auto;
	}

	.list {
		padding: 30rpx;
		display: flex;
		flex-direction: column;
		gap: 30rpx;
	}
	
	.item-card {
		display: flex;
		background-color: #fff;
		border-radius: 20rpx;
		padding: 25rpx;
		box-shadow: 0 8rpx 30rpx rgba(0,0,0,0.06);
		opacity: 0;
		transform: translateY(20px);
		animation: item-fade-in 0.5s ease-out forwards;
		transition: all 0.4s ease;

		&.is-removing {
			transform: scale(0.95);
			opacity: 0;
			max-height: 0;
			padding-top: 0;
			padding-bottom: 0;
			margin-top: -30rpx;
			overflow: hidden;
		}
	}

	@keyframes item-fade-in {
		to {
			opacity: 1;
			transform: translateY(0);
		}
	}

	.cover-image {
		width: 180rpx;
		height: 240rpx;
		border-radius: 15rpx;
		margin-right: 25rpx;
		background-color: #f0f0f0;
	}

	.info-wrapper {
		flex: 1;
		display: flex;
		flex-direction: column;
		justify-content: space-between;
	}

	.title {
		font-size: 32rpx;
		font-weight: 600;
		color: #333;
		margin-bottom: 15rpx;
	}

	.progress-box {
		display: flex;
		align-items: center;
		gap: 15rpx;
		margin-bottom: 15rpx;
		.progress-bar {
			flex: 1;
			height: 12rpx;
			background-color: #eee;
			border-radius: 6rpx;
			overflow: hidden;
		}
		.progress-value {
			height: 100%;
			background: linear-gradient(90deg, #5E72F7 0%, #9354FF 100%);
			border-radius: 6rpx;
		}
		.progress-text {
			font-size: 24rpx;
			color: #999;
		}
	}
	
	.desc {
		font-size: 26rpx;
		color: #666;
		margin-bottom: 15rpx;
	}
	
	.actions {
		align-self: flex-end;
	}
	
	.action-btn {
		font-size: 26rpx;
		font-weight: 500;
		color: #fff;
		padding: 12rpx 30rpx;
		border-radius: 30rpx;
		background: linear-gradient(90deg, #5E72F7 0%, #9354FF 100%);
		transition: all 0.2s ease;
		box-shadow: 0 4rpx 12rpx rgba(94, 114, 247, 0.2);
		
		&:active {
			transform: scale(0.96);
			box-shadow: 0 2rpx 8rpx rgba(94, 114, 247, 0.2);
		}
	}
	
	.nodata {
		padding-top: 20vh;
	}
</style>
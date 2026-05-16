
<template>
	<view class="page_content">
		<view class="search_header">
			<view class="back_btn" @click="goBack">
				<u-icon name="arrow-left" color="#333" size="22"></u-icon>
			</view>
			<view class="search_box">
				<u-icon name="search" color="#999" size="22"></u-icon>
				<input class="search_input" type="text" v-model="contents.keyword" :placeholder="$t('search.placeholder')" @confirm="searchHandle" />
			</view>
			<view class="search_btn" @click="searchHandle">
				<text>{{ $t('search.btn') }}</text>
			</view>
		</view>

		<scroll-view class="main_content" :scroll-y="true">
			<template v-if="!isSearch">
				<view class="history_section" v-if="historyList.length > 0">
					<view class="section_header">
						<text class="section_title">{{ $t('search.history') }}</text>
						<u-icon name="trash" color="#aaa" size="20" @click="clearHistory"></u-icon>
					</view>
					<view class="tags_box">
						<text class="tag" v-for="(item, index) in historyList" :key="index" @click="tagSearch(item)" :style="{ 'animation-delay': (index * 0.05) + 's' }">{{ item }}</text>
					</view>
				</view>

				<view class="hot_section">
					<view class="section_header">
						<text class="section_title">{{ $t('search.hotSearch') }}</text>
					</view>
					<view class="hot_list">
						<view class="hot_item" v-for="(item, index) in recommendList" :key="index" @click="openVideoDetail(item.id, item.display_title||item.title, item.image, item.display_desc||item.description)" :style="{ 'animation-delay': (index * 0.05 + 0.1) + 's' }">
							<text class="hot_index" :class="['top-' + (index + 1)]">{{ index + 1 }}</text>
							<text class="hot_title u-line-1">{{ item.display_title || item.title }}</text>
						</view>
					</view>
				</view>
			</template>

			<template v-else>
				<view class="content_box">
					<view class="list">
						<view class="item" v-for="(lItem, lIndex) in contents.list" :key="lIndex" @click="openVideoDetail(lItem.id, lItem.display_title||lItem.title, lItem.image, lItem.display_desc||lItem.description)" :style="{ 'animation-delay': (lIndex * 0.07) + 's' }">
							<view class="img">
								<image class="image" :src="lItem.image" mode="aspectFill"></image>
							</view>
							<view class="info">
								<view class="title u-line-1">{{ lItem.display_title || lItem.title }}</view>
								<view class="text u-line-1">{{ lItem.display_desc || lItem.description }}</view>
							</view>
						</view>
					</view>
					<view class="nodata" v-if="!contents.list.length && contents.status == 'nomore'">
						<u-empty mode="data" icon="http://cdn.uviewui.com/uview/empty/data.png" :text="$t('search.noResults')"></u-empty>
					</view>
					<view class="liststatus" v-if="contents.list.length">
						<u-loadmore :status="contents.status" :line="true" :nomoreText="$t('home.noMore')" />
					</view>
				</view>
			</template>
		</scroll-view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				contents: {
					list: [],
					page: 1,
					pagesize: 10,
					status: 'loadmore',
					keyword: ''
				},
				isSearch: false,
				platforms: this.$utils.platforms(),
				recommendList: [],
				historyList: [],
			}
		},
		onLoad() {
			this.loadHistory();
			this.getRecommendList();
		},
		methods: {
			openVideoDetail(id, title, image, description) {
				uni.navigateTo({
					url: `/pages/video/play?id=${id}&title=${title}&image=${image}&desc=${description}`
				});
			},
			goBack() {
				uni.navigateBack();
			},
			loadHistory() {
				const history = uni.getStorageSync('search_history');
				if (history) {
					this.historyList = history;
				}
			},
			saveHistory(keyword) {
				if (!keyword) return;
				let history = this.historyList;
				const index = history.indexOf(keyword);
				if (index !== -1) {
					history.splice(index, 1);
				}
				history.unshift(keyword);
				if (history.length > 10) {
					history = history.slice(0, 10);
				}
				this.historyList = history;
				uni.setStorageSync('search_history', history);
			},
			clearHistory() {
				uni.showModal({
					title: this.$t('common.tip'),
					content: this.$t('search.clearConfirm'),
					success: (res) => {
						if (res.confirm) {
							uni.removeStorageSync('search_history');
							this.historyList = [];
						}
					}
				});
			},
			tagSearch(tag) {
				this.contents.keyword = tag;
				this.searchHandle();
			},
			maidian(ids){
				if(!this.contents.keyword) return
				this.$request('common.point', { item_id: ids, platform:this.platforms, point_type:4 },false);
			},
			getRecommendList() {
				let status = uni.getSystemInfoSync().uniPlatform;
				const obj = {
					type: 'recommend',
					pagesize: 9,
					platform: status == 'mp-weixin' ? 2 : 1,
				};
				this.$request('video.list', obj).then(res => {
					if(res.code === 1 && res.data && res.data.length) {
						this.recommendList = res.data;
					}
				});
			},
			searchHandle() {
				if(!this.contents.keyword) return;
				this.saveHistory(this.contents.keyword);
				this.$request('common.point', {content:this.contents.keyword,platform:this.platforms, point_type:3 },false);
				
				this.isSearch = true;
				this.contents.page = 1;
				this.contents.list = [];
				this.getVideoList();
			},
			getVideoList() {
				if(!this.contents.keyword) return;
				let status = uni.getSystemInfoSync().uniPlatform;
				const obj = {
					search: this.contents.keyword,
					page: this.contents.page,
					pagesize: this.contents.pagesize,
					platform: status == 'mp-weixin' ? 2 : 1,
				};
				this.contents.status = 'loading';
				this.$request('video.list', obj).then(res => {
					if(res.code === 1) {
						this.contents.list = this.contents.list.concat(res.data || []);
						if(!res.data || res.data.length < this.contents.pagesize) {
							this.contents.status = 'nomore';
						} else {
							this.contents.status = 'loadmore';
						}
					}
				}).catch(err => {
					this.contents.status = 'loadmore';
				});
			},
			bottomHandle() {
				if(!this.isSearch || this.contents.status !== 'loadmore') return;
				this.contents.page++;
				this.getVideoList();
			},
		}
	}
</script>

<style lang="scss" scoped>
	.page_content {
		background-color: #ffffff;
		height: 100vh;
		display: flex;
		flex-direction: column;
		overflow: hidden;
	}

	.search_header {
		display: flex;
		align-items: center;
		padding: 20rpx 30rpx;
		border-bottom: 1rpx solid #f5f5f5;
		background-color: #fff;
		transform: translateY(-120%);
		animation: header-slide-in 0.5s ease-out forwards;
	}

	@keyframes header-slide-in {
		to {
			transform: translateY(0);
		}
	}

	.back_btn {
		margin-right: 10rpx;
		padding: 10rpx;
		margin-left: -10rpx;
	}

	.search_box {
		flex: 1;
		display: flex;
		align-items: center;
		background-color: #f7f7f7;
		border-radius: 30rpx;
		padding: 15rpx 25rpx;
	}

	.search_input {
		flex: 1;
		font-size: 28rpx;
		margin-left: 15rpx;
	}

	.search_btn {
		padding: 15rpx 35rpx;
		margin-left: 20rpx;
		background: linear-gradient(90deg, #5E72F7 0%, #9354FF 100%);
		border-radius: 30rpx;
		box-shadow: 0 4rpx 12rpx rgba(94, 114, 247, 0.2);
		transition: all 0.2s ease-in-out;
		
		text {
			color: #fff;
			font-size: 28rpx;
			font-weight: 500;
		}

		&:active {
			transform: scale(0.96);
			box-shadow: 0 2rpx 8rpx rgba(94, 114, 247, 0.2);
		}
	}

	.main_content {
		flex: 1;
		overflow-y: auto;
		padding: 40rpx 30rpx;
	}

	.history_section, .hot_section {
		opacity: 0;
		transform: translateY(20px);
		animation: section-fade-in 0.5s ease-out forwards;
	}
	
	.hot_section {
		margin-top: 40rpx;
		animation-delay: 0.1s;
	}

	@keyframes section-fade-in {
		to {
			opacity: 1;
			transform: translateY(0);
		}
	}

	.section_header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 30rpx;
	}

	.section_title {
		font-size: 32rpx;
		font-weight: 600;
	}

	.tags_box {
		display: flex;
		flex-wrap: wrap;
		gap: 20rpx;
	}

	.tag {
		background-color: #f7f7f7;
		color: #333;
		font-size: 26rpx;
		padding: 12rpx 28rpx;
		border-radius: 30rpx;
		transition: all 0.2s ease;
		opacity: 0;
		transform: translateY(15px);
		animation: item-fade-in 0.4s ease-out forwards;

		&:active {
			transform: translateY(0) scale(0.95);
			background-color: #e9ecef;
		}
	}
	
	.hot_list {
		.hot_item {
			display: flex;
			align-items: center;
			margin-bottom: 35rpx;
			opacity: 0;
			transform: translateY(15px);
			animation: item-fade-in 0.4s ease-out forwards;

			&:last-child {
				margin-bottom: 0;
			}
		}
		.hot_index {
			width: 40rpx;
			font-size: 28rpx;
			font-weight: 600;
			color: #999;
			&.top-1 { color: #ff4d4f; }
			&.top-2 { color: #ff7a45; }
			&.top-3 { color: #ffc53d; }
		}
		.hot_title {
			flex: 1;
			font-size: 28rpx;
			color: #333;
		}
	}

	.content_box {
		padding: 10rpx 0;
	}
	
	.list {
		display: flex;
		flex-wrap: wrap;
		.item {
			width: calc((100% - 25rpx) / 2);
			margin-bottom: 25rpx;
			border-radius: 15rpx;
			overflow: hidden;
			opacity: 0;
			transform: translateY(20px);
			animation: item-fade-in 0.5s ease-out forwards;

			&:nth-child(2n) {
				margin-right: 0;
			}
			&:nth-child(2n-1) {
				margin-right: 25rpx;
			}
		}
		.img {
			width: 100%;
			height: 280rpx;
			background-color: #f0f0f0;
		}
		.image {
			width: 100%;
			height: 100%;
		}
		.info {
			padding: 20rpx 10rpx;
		}
		.title {
			font-size: 28rpx;
			font-weight: 600;
			color: #333;
		}
		.text {
			font-size: 24rpx;
			color: #999;
			margin-top: 8rpx;
		}
	}
	
	.nodata {
		padding-top: 20vh;
	}

	@keyframes item-fade-in {
		to {
			opacity: 1;
			transform: translateY(0);
		}
	}
</style>
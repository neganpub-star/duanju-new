
<template>
	<view class="page">
		<!-- 固定头部 -->
		<view class="header-fixed">
			<view class="tab-row">
				<view class="tabs">
					<view class="tab" :class="{ active: tab === 0 }" @click="changeTab(0)">{{ $t('watch.watchHistory') }}</view>
					<view class="tab" :class="{ active: tab === 1 }" @click="changeTab(1)">{{ $t('watch.myFollowing') }}</view>
				</view>
				<view class="edit-btn" @click="toggleEdit">
					{{ editing ? $t('watch.done') : $t('watch.edit') }}
				</view>
			</view>
		</view>

		<!-- 内容区 -->
		<scroll-view
			class="content"
			:style="{ paddingTop: headerH + 'px' }"
			scroll-y
			:refresher-enabled="true"
			:refresher-triggered="refreshing"
			@refresherrefresh="onRefresh"
			@scrolltolower="onLoadMore"
		>
			<view class="grid" v-if="currentList.length">
				<view
					class="grid-item"
					v-for="(item, i) in currentList"
					:key="item.vid || i"
					@click="onItemClick(item, i)"
				>
					<view class="cover-wrap">
						<image class="cover" :src="item.video.image" mode="aspectFill" />
						<!-- 集数角标 -->
						<view class="ep-badge">
							<text v-if="tab === 0">{{ getEpNum(item) }}/{{ item.video.episodes }}</text>
							<text v-else>{{ $t('watch.totalEps', [item.video.episodes]) }}</text>
						</view>
						<!-- 编辑模式勾选框 -->
						<view v-if="editing" class="checkbox-wrap" @click.stop="toggleSelect(i)">
							<view class="checkbox" :class="{ checked: item._selected }">
								<u-icon v-if="item._selected" name="checkmark" color="#fff" size="14" />
							</view>
						</view>
					</view>
					<text class="item-title">{{ item.video.display_title || item.video.title }}</text>
					<text class="item-sub" v-if="tab === 0">{{ getEpLabel(item) }}</text>
					<text class="item-sub" v-else>{{ $t('watch.totalEps', [item.video.episodes]) }}</text>
				</view>
			</view>

			<view class="nodata" v-if="!currentList.length && loadStatus === 'nomore'">
				<u-empty mode="data" icon="http://cdn.uviewui.com/uview/empty/data.png" :text="$t('watch.noRecord')"></u-empty>
			</view>
			<view class="list-status" v-if="currentList.length">
				<u-loadmore :status="loadStatus" :line="true" :nomoreText="$t('home.noMore')" :loadmoreText="$t('home.loadMore')" />
			</view>
			<!-- 编辑模式底部留白 -->
			<view v-if="editing" style="height: 120rpx;"></view>
		</scroll-view>

		<!-- 编辑模式底部操作栏 -->
		<view class="edit-bar" v-if="editing">
			<view class="select-all-wrap" @click="toggleSelectAll">
				<view class="checkbox sm" :class="{ checked: isAllSelected }">
					<u-icon v-if="isAllSelected" name="checkmark" color="#fff" size="12" />
				</view>
				<text class="select-all-text">{{ $t('watch.selectAll') }}</text>
			</view>
			<view
				class="delete-btn"
				:class="{ disabled: selectedCount === 0 }"
				@click="deleteSelected"
			>
				{{ $t('watch.deleteSelected', [selectedCount]) }}
			</view>
		</view>

		<CustomTabBar current="/pages/home/watch" />
	</view>
</template>

<script>
	import { mapGetters } from 'vuex'
	import CustomTabBar from '@/components/CustomTabBar.vue'

	export default {
		components: { CustomTabBar },
		data() {
			return {
				headerH: 0,
				tab: 1,
				editing: false,
				refreshing: false,
				contentList: [
					{ type: 'log',      list: [], page: 1, pagesize: 12, status: 'loadmore' },
					{ type: 'favorite', list: [], page: 1, pagesize: 12, status: 'loadmore' },
				],
			}
		},
		computed: {
			...mapGetters('user', ['userInfo']),
			currentTab() { return this.contentList[this.tab] },
			currentList() { return this.currentTab.list },
			loadStatus() { return this.currentTab.status },
			selectedCount() { return this.currentList.filter(i => i._selected).length },
			isAllSelected() { return this.currentList.length > 0 && this.currentList.every(i => i._selected) },
		},
		onReady() {
			uni.createSelectorQuery().in(this).select('.header-fixed').boundingClientRect(d => {
				if (d) this.headerH = d.height
			}).exec()
		},
		onShow() {
			this.onRefresh()
		},
		methods: {
			// ──────────── 数据 ────────────
			fetchList(isRefresh = false) {
				const tab = this.currentTab
				if (tab.status === 'loading' && !isRefresh) return
				tab.status = 'loading'
				if (isRefresh) tab.page = 1

				this.$request('video.getRecord', {
					type: tab.type,
					page: tab.page,
					pagesize: tab.pagesize,
				}).then(res => {
					const data = (res.code === 1 ? res.data : []) || []
					const rows = data.map(item => ({ ...item, _selected: false }))
					tab.list = isRefresh ? rows : tab.list.concat(rows)
					tab.status = data.length < tab.pagesize ? 'nomore' : 'loadmore'
				}).catch(() => {
					tab.status = 'nomore'
				}).finally(() => {
					this.refreshing = false
				})
			},
			onRefresh() {
				this.refreshing = true
				this.fetchList(true)
			},
			onLoadMore() {
				if (this.currentTab.status === 'loadmore') {
					this.currentTab.page++
					this.fetchList()
				}
			},
			changeTab(index) {
				if (this.tab === index) return
				this.tab = index
				this.editing = false
				if (!this.currentList.length) this.fetchList(true)
			},

			// ──────────── 编辑 ────────────
			toggleEdit() {
				this.editing = !this.editing
				if (!this.editing) {
					this.currentList.forEach(item => { item._selected = false })
				}
			},
			toggleSelect(index) {
				const item = this.currentList[index]
				this.$set(item, '_selected', !item._selected)
			},
			toggleSelectAll() {
				const next = !this.isAllSelected
				this.currentList.forEach(item => { this.$set(item, '_selected', next) })
			},
			deleteSelected() {
				if (this.selectedCount === 0) return
				const ids = this.currentList.filter(i => i._selected).map(i => i.vid).join(',')
				const api = this.tab === 0 ? 'video.deleteHistory' : 'video.batchRemoveFavorite'
				this.$request(api, { ids }, false).then(res => {
					if (res.code === 1) {
						this.currentTab.list = this.currentList.filter(i => !i._selected)
						uni.showToast({ title: this.$t('watch.deleteSuccess'), icon: 'none', duration: 1500 })
						if (!this.currentList.length) this.editing = false
					}
				})
			},

			// ──────────── 点击 ────────────
			onItemClick(item, index) {
				if (this.editing) {
					this.toggleSelect(index)
					return
				}
				uni.navigateTo({ url: `/pages/video/play?id=${item.vid}` })
			},
			unfavorite(item, index) {
				const obj = { ids: item.vid, type: 'favorite' }
				this.$request('video.deleteRecord', obj, false).then(res => {
					if (res.code === 1) {
						uni.showToast({ title: this.$t('watch.unfollowSuccess'), icon: 'none', duration: 1500 })
						this.currentTab.list.splice(index, 1)
					}
				})
			},

			// ──────────── 工具 ────────────
			getEpNum(item) {
				if (!item.episode) return '-'
				const m = (item.episode.display_title || item.episode.name || '').match(/\d+/)
				return m ? m[0] : '-'
			},
			getEpLabel(item) {
				if (!item.episode) return ''
				const name = item.episode.display_title || item.episode.name || ''
				const total = item.video.episodes
				return total ? `${name}/共${total}集` : name
			},
		},
	}
</script>

<style lang="scss" scoped>
.page {
	background: #f4f5f7;
	min-height: 100vh;
	/* #ifdef H5 */
	padding-bottom: 100rpx;
	/* #endif */
}

// ── 头部 ──────────────────────────────────────
.header-fixed {
	position: fixed;
	top: 0; left: 0; right: 0;
	z-index: 100;
	background: #fff;
	box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.05);
	padding: 20rpx 30rpx;
	/* #ifndef H5 */
	padding-top: calc(20rpx + var(--status-bar-height));
	/* #endif */
}

.tab-row {
	display: flex;
	align-items: center;
	justify-content: space-between;
}

.tabs {
	display: flex;
	gap: 48rpx;
}

.tab {
	font-size: 30rpx;
	color: #888;
	padding-bottom: 10rpx;
	position: relative;
	transition: all 0.2s;

	&.active {
		font-size: 32rpx;
		font-weight: 700;
		color: #111;

		&::after {
			content: '';
			position: absolute;
			bottom: 0; left: 50%;
			transform: translateX(-50%);
			width: 40rpx; height: 6rpx;
			background: linear-gradient(90deg, #5E72F7, #9354FF);
			border-radius: 3rpx;
		}
	}
}

.edit-btn {
	font-size: 28rpx;
	color: #5E72F7;
	padding: 6rpx 16rpx;
}

// ── 内容网格 ───────────────────────────────────
.content {
	height: 100vh;
	box-sizing: border-box;
}

.grid {
	display: flex;
	flex-wrap: wrap;
	padding: 20rpx 16rpx 0;
	gap: 16rpx;
}

.grid-item {
	width: calc((100% - 32rpx) / 3);
	display: flex;
	flex-direction: column;
}

.cover-wrap {
	position: relative;
	width: 100%;
	padding-top: 140%; // 竖版海报比例 ~5:7
	border-radius: 12rpx;
	overflow: hidden;
	background: #e8e8e8;
}

.cover {
	position: absolute;
	top: 0; left: 0;
	width: 100%; height: 100%;
}

.ep-badge {
	position: absolute;
	bottom: 0; left: 0; right: 0;
	background: linear-gradient(to top, rgba(0,0,0,0.6) 0%, transparent 100%);
	padding: 10rpx 10rpx 8rpx;
	text {
		font-size: 20rpx;
		color: #fff;
	}
}

.checkbox-wrap {
	position: absolute;
	top: 8rpx; right: 8rpx;
}

.checkbox {
	width: 36rpx; height: 36rpx;
	border-radius: 50%;
	border: 3rpx solid rgba(255,255,255,0.9);
	background: rgba(0,0,0,0.3);
	display: flex;
	align-items: center;
	justify-content: center;

	&.checked {
		background: #5E72F7;
		border-color: #5E72F7;
	}

	&.sm {
		width: 32rpx; height: 32rpx;
		border-color: #5E72F7;
		background: #fff;
		&.checked { background: #5E72F7; }
	}
}

.item-title {
	font-size: 24rpx;
	color: #222;
	font-weight: 500;
	margin-top: 10rpx;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
	display: block;
}

.item-sub {
	font-size: 20rpx;
	color: #999;
	margin-top: 4rpx;
	display: block;
}

// ── 编辑底部操作栏 ─────────────────────────────
.edit-bar {
	position: fixed;
	bottom: 0; left: 0; right: 0;
	/* #ifdef H5 */
	bottom: 100rpx;
	/* #endif */
	background: #fff;
	border-top: 1rpx solid #eee;
	padding: 20rpx 30rpx;
	display: flex;
	align-items: center;
	justify-content: space-between;
	z-index: 200;
}

.select-all-wrap {
	display: flex;
	align-items: center;
	gap: 14rpx;
}

.select-all-text {
	font-size: 28rpx;
	color: #333;
}

.delete-btn {
	font-size: 28rpx;
	font-weight: 600;
	color: #fff;
	background: linear-gradient(90deg, #5E72F7, #9354FF);
	padding: 16rpx 40rpx;
	border-radius: 40rpx;
	box-shadow: 0 4rpx 12rpx rgba(94,114,247,0.3);

	&.disabled {
		background: #ccc;
		box-shadow: none;
	}
}

// ── 其他 ───────────────────────────────────────
.nodata { padding-top: 20vh; }
.list-status { padding: 20rpx 0 30rpx; }
</style>


<template>
	<view class="page">
		<!-- 固定头部 -->
		<view class="header-fixed">
			<view class="header-inner">
				<view class="tabs">
					<view
						class="tab"
						:class="{ active: tab === 0 }"
						@click="changeTab(0)"
					>
						<text class="tab-text">{{ $t('watch.watchHistory') }}</text>
						<view v-if="tab === 0" class="tab-line" />
					</view>
					<view
						class="tab"
						:class="{ active: tab === 1 }"
						@click="changeTab(1)"
					>
						<text class="tab-text">{{ $t('watch.myFollowing') }}</text>
						<view v-if="tab === 1" class="tab-line" />
					</view>
				</view>
				<view class="edit-btn" @click="toggleEdit">
					<text class="edit-btn-text">{{ editing ? $t('watch.done') : $t('watch.edit') }}</text>
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
					<view class="card">
						<view class="cover-wrap">
							<image class="cover" :src="item.video.image" mode="aspectFill" />
							<!-- 底部渐变信息 -->
							<view class="cover-footer">
								<text v-if="tab === 0" class="ep-text">{{ $t('watch.epProgress', [getEpNum(item), item.video.episodes]) }}</text>
								<text v-else class="ep-text">{{ $t('watch.totalEps', [item.video.episodes]) }}</text>
							</view>
							<!-- 历史进度条 -->
							<view v-if="tab === 0 && item.video.episodes" class="progress-bar">
								<view class="progress-fill" :style="{ width: getProgress(item) + '%' }" />
							</view>
							<!-- 编辑模式勾选 -->
							<view v-if="editing" class="checkbox-wrap" @click.stop="toggleSelect(i)">
								<view class="checkbox" :class="{ checked: item._selected }">
									<u-icon v-if="item._selected" name="checkmark" color="#fff" size="12" />
								</view>
							</view>
							<!-- 编辑蒙层 -->
							<view v-if="editing && item._selected" class="select-mask" />
						</view>
						<view class="card-info">
							<text class="item-title">{{ item.video.display_title || item.video.title }}</text>
							<text v-if="tab === 0" class="item-sub">{{ getEpLabel(item) }}</text>
						</view>
					</view>
				</view>
			</view>

			<view class="nodata" v-if="!currentList.length && loadStatus === 'nomore'">
				<EmptyState :text="$t('watch.noRecord')" />
			</view>

			<view class="list-status" v-if="currentList.length">
				<u-loadmore :status="loadStatus" :line="true" :nomoreText="$t('home.noMore')" :loadmoreText="$t('home.loadMore')" />
			</view>

			<!-- 编辑模式底部留白 -->
			<view v-if="editing" style="height: 160rpx;" />
		</scroll-view>

		<!-- 编辑模式底部操作栏 -->
		<view class="edit-bar" v-if="editing">
			<view class="select-all-wrap" @click="toggleSelectAll">
				<view class="checkbox sm" :class="{ checked: isAllSelected }">
					<u-icon v-if="isAllSelected" name="checkmark" color="#fff" size="10" />
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
	import EmptyState from '@/components/EmptyState.vue'

	export default {
		components: { CustomTabBar, EmptyState },
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

			// ──────────── 工具 ────────────
			getEpNum(item) {
				if (!item.episode) return '-'
				const m = (item.episode.display_title || item.episode.name || '').match(/\d+/)
				return m ? m[0] : '-'
			},
			getEpLabel(item) {
				if (!item.episode) return ''
				const nameStr = item.episode.display_title || item.episode.name || ''
				const m = nameStr.match(/\d+/)
				const num = m ? m[0] : nameStr
				return this.$t('watch.watchedEp', [num])
			},
			getProgress(item) {
				if (!item.episode || !item.video.episodes) return 0
				const m = (item.episode.display_title || item.episode.name || '').match(/\d+/)
				const cur = m ? parseInt(m[0]) : 0
				return Math.min(Math.round((cur / item.video.episodes) * 100), 100)
			},
		},
	}
</script>

<style lang="scss" scoped>
$purple: $dj-primary-deep;
$blue: $dj-primary;
$grad: linear-gradient(90deg, #{$blue}, #{$purple});

.page {
	background: #f2f3f7;
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
	/* #ifndef H5 */
	padding-top: var(--status-bar-height);
	/* #endif */
}

.header-inner {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 0 30rpx;
	height: 96rpx;
}

.tabs {
	display: flex;
	align-items: center;
	gap: 48rpx;
}

.tab {
	position: relative;
	display: flex;
	flex-direction: column;
	align-items: center;
	padding-bottom: 4rpx;
}

.tab-text {
	font-size: 30rpx;
	color: #bbb;
	transition: all 0.2s;
	line-height: 1;
}

.tab.active .tab-text {
	font-size: 32rpx;
	font-weight: 700;
	color: #111;
}

.tab-line {
	position: absolute;
	bottom: -4rpx;
	left: 50%;
	transform: translateX(-50%);
	width: 36rpx;
	height: 6rpx;
	border-radius: 3rpx;
	background: $grad;
}

.edit-btn {
	padding: 10rpx 24rpx;
	border-radius: 32rpx;
	border: 2rpx solid rgba($blue, 0.35);
}

.edit-btn-text {
	font-size: 26rpx;
	color: $blue;
}

// ── 内容 ──────────────────────────────────────
.content {
	height: 100vh;
	box-sizing: border-box;
}

.grid {
	display: flex;
	flex-wrap: wrap;
	padding: 20rpx 20rpx 0;
	gap: 16rpx;
}

.grid-item {
	width: calc((100% - 32rpx) / 3);
}

// ── 卡片 ──────────────────────────────────────
.card {
	background: #fff;
	border-radius: 16rpx;
	overflow: hidden;
	box-shadow: 0 2rpx 14rpx rgba(0, 0, 0, 0.07);
}

.cover-wrap {
	position: relative;
	width: 100%;
	padding-top: 142%;
	background: #e0e0e0;
}

.cover {
	position: absolute;
	top: 0; left: 0;
	width: 100%; height: 100%;
	display: block;
}

.cover-footer {
	position: absolute;
	bottom: 0; left: 0; right: 0;
	background: linear-gradient(to top, rgba(0,0,0,0.65) 0%, transparent 100%);
	padding: 24rpx 10rpx 8rpx;
}

.ep-text {
	font-size: 20rpx;
	color: rgba(255,255,255,0.92);
	line-height: 1.2;
}

// 观看进度条
.progress-bar {
	position: absolute;
	bottom: 0; left: 0; right: 0;
	height: 4rpx;
	background: rgba(255,255,255,0.25);
}

.progress-fill {
	height: 100%;
	background: $grad;
	border-radius: 2rpx;
}

// 勾选框
.checkbox-wrap {
	position: absolute;
	top: 10rpx; right: 10rpx;
	z-index: 10;
}

.checkbox {
	width: 40rpx; height: 40rpx;
	border-radius: 50%;
	border: 3rpx solid rgba(255,255,255,0.9);
	background: rgba(0,0,0,0.25);
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.2);

	&.checked {
		background: $blue;
		border-color: $blue;
	}

	&.sm {
		width: 36rpx; height: 36rpx;
		border-color: $blue;
		background: #fff;
		box-shadow: none;
		&.checked { background: $blue; }
	}
}

.select-mask {
	position: absolute;
	inset: 0;
	background: rgba($blue, 0.18);
}

// ── 卡片文字 ──────────────────────────────────
.card-info {
	padding: 12rpx 12rpx 14rpx;
}

.item-title {
	font-size: 24rpx;
	color: #222;
	font-weight: 500;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
	display: block;
	line-height: 1.4;
}

.item-sub {
	font-size: 20rpx;
	color: #aaa;
	margin-top: 4rpx;
	display: block;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

// ── 空状态 ────────────────────────────────────
.nodata {
	padding-top: 25vh;
	display: flex;
	justify-content: center;
}


// ── 加载更多 ──────────────────────────────────
.list-status {
	padding: 24rpx 0 40rpx;
}

// ── 底部编辑操作栏 ────────────────────────────
.edit-bar {
	position: fixed;
	bottom: 0; left: 0; right: 0;
	/* #ifdef H5 */
	bottom: 100rpx;
	/* #endif */
	background: rgba(255,255,255,0.96);
	backdrop-filter: blur(12px);
	border-top: 1rpx solid #eee;
	padding: 20rpx 30rpx;
	padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
	display: flex;
	align-items: center;
	justify-content: space-between;
	z-index: 200;
	box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.06);
}

.select-all-wrap {
	display: flex;
	align-items: center;
	gap: 14rpx;
}

.select-all-text {
	font-size: 28rpx;
	color: #444;
}

.delete-btn {
	font-size: 28rpx;
	font-weight: 600;
	color: #fff;
	background: $grad;
	padding: 18rpx 44rpx;
	border-radius: 44rpx;
	box-shadow: 0 6rpx 18rpx rgba(94, 114, 247, 0.35);
	transition: opacity 0.2s;

	&.disabled {
		background: #ddd;
		box-shadow: none;
		color: #aaa;
	}
}
</style>

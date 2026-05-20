<template>
	<view class="comment-mask" v-if="show" @click.self="close">
		<view class="comment-panel" :class="{ show: show }">
			<!-- 头部 -->
			<view class="panel-header">
				<text class="panel-title">{{ $t('comment.title') }} {{ total > 0 ? total : '' }}</text>
				<view class="close-btn" @click="close">
					<u-icon name="close" color="#999" size="20"></u-icon>
				</view>
			</view>

			<!-- 评论列表 -->
			<scroll-view
				class="comment-list"
				scroll-y
				:scroll-top="scrollTop"
				@scrolltolower="loadMore"
			>
				<EmptyState v-if="!list.length && status === 'nomore'" type="comment" size="mini" :text="$t('comment.noComments')" />

				<view
					class="comment-item"
					v-for="(item, index) in list"
					:key="item.id"
				>
					<image class="avatar" :src="item.userAvatar || defaultAvatar" mode="aspectFill"></image>
					<view class="comment-body">
						<view class="comment-meta">
							<text class="nickname">{{ item.userNickname || $t('comment.anonymousUser') }}</text>
							<text class="time">{{ formatTime(item.createTime) }}</text>
						</view>
						<view class="comment-content">{{ item.content }}</view>

						<!-- 操作栏 -->
						<view class="comment-actions">
							<view class="action-item reply-btn" @click="startReply(item)">
								<u-icon name="chat" color="#bbb" size="15"></u-icon>
								<text class="action-text">{{ item.replyCount > 0 ? item.replyCount : $t('comment.reply') }}</text>
							</view>
							<view class="action-item like-btn" :class="{ liked: item.isLiked }" @click="toggleLike(item, index)">
								<u-icon :name="item.isLiked ? 'heart-fill' : 'heart'" :color="item.isLiked ? '$dj-primary' : '#bbb'" size="15"></u-icon>
								<text class="action-text" :class="{ liked: item.isLiked }">{{ item.likes || '' }}</text>
							</view>
							<view class="action-item del-btn" v-if="item.isOwn" @click="deleteComment(item, index)">
								<u-icon name="trash" color="#bbb" size="15"></u-icon>
							</view>
						</view>

						<!-- 展开回复 -->
						<view class="replies-box" v-if="item.replyCount > 0">
							<view class="reply-item" v-for="r in item.replies || []" :key="r.id">
								<text class="reply-nickname">{{ r.userNickname }}</text>
								<text v-if="r.replyNickname" class="reply-to"> {{ $t('comment.replyTo') }} <text class="reply-nickname">{{ r.replyNickname }}</text>：</text>
								<text v-else>：</text>
								<text class="reply-content">{{ r.content }}</text>
								<view class="reply-actions">
									<view class="action-item reply-btn" @click="startReply(item, r)">
										<text class="action-text">{{ $t('comment.reply') }}</text>
									</view>
									<view class="action-item like-btn" :class="{ liked: r.isLiked }" @click="toggleLike(r, -1, index)">
										<u-icon :name="r.isLiked ? 'heart-fill' : 'heart'" :color="r.isLiked ? '$dj-primary' : '#bbb'" size="13"></u-icon>
										<text class="action-text" :class="{ liked: r.isLiked }">{{ r.likes || '' }}</text>
									</view>
									<view class="action-item del-btn" v-if="r.isOwn" @click="deleteReply(r, index)">
										<u-icon name="trash" color="#bbb" size="13"></u-icon>
									</view>
								</view>
							</view>
							<view
								class="load-more-reply"
								v-if="item.replyCount > (item.replies || []).length"
								@click="loadReplies(item, index)"
							>
								<text>{{ $t('comment.viewReplies', [item.replyCount]) }}</text>
								<u-icon name="arrow-down" color="$dj-primary" size="12"></u-icon>
							</view>
						</view>
					</view>
				</view>

				<view class="list-status" v-if="list.length">
					<u-loadmore :status="status" :line="true" :nomoreText="$t('home.noMore')" :loadmoreText="$t('home.loadMore')" />
				</view>
			</scroll-view>

			<!-- 表情面板 -->
			<view class="emoji-panel" v-if="showEmoji">
				<view
					class="emoji-cell"
					v-for="(emo, i) in emojiList"
					:key="i"
					@click="appendEmoji(emo)"
				>
					<text class="emoji-text">{{ emo }}</text>
				</view>
			</view>

			<!-- 输入框 -->
			<view class="input-bar">
				<view class="emoji-btn" :class="{ active: showEmoji }" @click="toggleEmoji">
					<text class="emoji-icon">😊</text>
				</view>
				<view class="input-wrap">
					<input
						class="comment-input"
						v-model="inputText"
						:placeholder="inputPlaceholder"
						:focus="inputFocus"
						@confirm="submitComment"
						@focus="showEmoji = false"
						confirm-type="send"
						maxlength="200"
					/>
					<view v-if="replyTarget" class="cancel-reply" @click="cancelReply">
						<u-icon name="close-circle-fill" color="#ccc" size="18"></u-icon>
					</view>
				</view>
				<view class="send-btn" :class="{ active: inputText.trim() }" @click="submitComment">
					<text>{{ $t('comment.send') }}</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import EmptyState from '@/components/EmptyState.vue'
export default {
	name: 'CommentPanel',
	components: { EmptyState },
	props: {
		show: { type: Boolean, default: false },
		videoId: { type: [Number, String], default: null },
	},
	data() {
		return {
			list: [],
			total: 0,
			page: 1,
			pagesize: 20,
			status: 'loadmore',
			scrollTop: 0,
			inputText: '',
			inputFocus: false,
			replyTarget: null,   // { commentId, nickname } 当前回复目标
			defaultAvatar: 'https://img.nymaite.com/video_short/icons/avatar.png',
			showEmoji: false,
			emojiList: [
				'😀','😁','😂','🤣','😊','😍','🥰','😘',
				'😎','🤩','🥳','😋','😜','🤪','😝','🤓',
				'😇','🥺','😢','😭','😡','🤬','😨','😱',
				'🤔','😴','🤤','🤗','🤭','🙄','😬','😏',
				'👍','👎','👏','🙌','🤝','🙏','💪','✌️',
				'❤️','💔','💖','✨','🔥','🎉','🌹','🌟',
				'💯','👀','💩','🤡','🐶','🐱','🦄','🌈',
			],
		}
	},
	computed: {
		inputPlaceholder() {
			return this.replyTarget
				? this.$t('comment.replyPlaceholder', [this.replyTarget.nickname])
				: this.$t('comment.placeholder');
		},
	},
	watch: {
		show(val) {
			if (val && !this.list.length) {
				this.loadComments(true);
			}
		},
		videoId(val) {
			if (val) this.loadComments(true);
		},
	},
	methods: {
		close() {
			this.showEmoji = false;
			this.$emit('close');
		},
		loadComments(reset = false) {
			if (reset) {
				this.page = 1;
				this.list = [];
				this.status = 'loadmore';
			}
			if (this.status === 'loading' || this.status === 'nomore') return;
			this.status = 'loading';
			this.$request('comment.list', { videoId: this.videoId, page: this.page, pagesize: this.pagesize }).then(res => {
				if (res.code === 1) {
					this.total = res.data.total || 0;
					const rows = (res.data.list || []).map(item => ({ ...item, replies: [] }));
					this.list = reset ? rows : this.list.concat(rows);
					this.status = rows.length < this.pagesize ? 'nomore' : 'loadmore';
				}
			}).catch(() => { this.status = 'loadmore'; });
		},
		loadMore() {
			if (this.status === 'loadmore') {
				this.page++;
				this.loadComments();
			}
		},
		loadReplies(item, index) {
			const loaded = (item.replies || []).length;
			const nextPage = Math.floor(loaded / 20) + 1;
			this.$request('comment.replies', { parentId: item.id, page: nextPage, pagesize: 20 }).then(res => {
				if (res.code === 1) {
					const existing = this.list[index].replies || [];
					this.$set(this.list[index], 'replies', existing.concat(res.data || []));
				}
			});
		},
		startReply(topComment, replyComment = null) {
			const target = replyComment || topComment;
			this.replyTarget = {
				commentId: topComment.id,    // 顶级评论 id（parent_id）
				replyUserId: target.userId,
				nickname: target.userNickname,
			};
			this.inputFocus = true;
			this.$nextTick(() => { this.inputFocus = false; this.$nextTick(() => { this.inputFocus = true; }); });
		},
		cancelReply() {
			this.replyTarget = null;
		},
		toggleEmoji() {
			this.showEmoji = !this.showEmoji;
		},
		appendEmoji(emo) {
			if ((this.inputText || '').length >= 200) return;
			this.inputText = (this.inputText || '') + emo;
		},
		submitComment() {
			const content = this.inputText.trim();
			if (!content) return;
			const payload = {
				video_id: this.videoId,
				content,
			};
			if (this.replyTarget) {
				payload.parent_id = this.replyTarget.commentId;
				payload.reply_user_id = this.replyTarget.replyUserId;
				payload.reply_nickname = this.replyTarget.nickname;
			}
			this.$request('comment.post', payload).then(res => {
				if (res.code === 1) {
					this.inputText = '';
					this.replyTarget = null;
					this.showEmoji = false;
					uni.showToast({ title: '发表成功', icon: 'none', duration: 1200 });
					this.loadComments(true);
					if (!payload.parent_id) {
						// 顶级评论才通知父页面更新计数
						this.$emit('comment-added');
					}
				}
			});
		},
		toggleLike(item, index, parentIndex = -1) {
			this.$request('comment.like', { comment_id: item.id }).then(res => {
				if (res.code === 1) {
					item.isLiked = res.data.liked;
					item.likes = res.data.likes;
					if (parentIndex >= 0) {
						// 回复条目的点赞，更新嵌套数据
						const replies = this.list[parentIndex].replies || [];
						const ri = replies.findIndex(r => r.id === item.id);
						if (ri >= 0) {
							this.$set(this.list[parentIndex].replies, ri, { ...this.list[parentIndex].replies[ri], isLiked: item.isLiked, likes: item.likes });
						}
					} else if (index >= 0) {
						this.$set(this.list, index, { ...this.list[index], isLiked: item.isLiked, likes: item.likes });
					}
				}
			});
		},
		deleteComment(item, index) {
			this.$appModal({
				title: this.$t('comment.deleteTitle'),
				content: this.$t('comment.deleteContent'),
				cancelText: this.$t('common.cancel'),
				confirmText: this.$t('common.confirm'),
				success: res => {
					if (res.confirm) {
						this.$request('comment.delete', { comment_id: item.id }).then(r => {
							if (r.code === 1) {
								this.list.splice(index, 1);
								this.total = Math.max(0, this.total - 1);
							}
						});
					}
				}
			});
		},
		deleteReply(reply, parentIndex) {
			this.$appModal({
				title: this.$t('comment.deleteTitle'),
				content: this.$t('comment.deleteContent'),
				cancelText: this.$t('common.cancel'),
				confirmText: this.$t('common.confirm'),
				success: res => {
					if (res.confirm) {
						this.$request('comment.delete', { comment_id: reply.id }).then(r => {
							if (r.code === 1) {
								const replies = this.list[parentIndex].replies || [];
								const ri = replies.findIndex(r => r.id === reply.id);
								if (ri >= 0) replies.splice(ri, 1);
								this.list[parentIndex].replyCount = Math.max(0, (this.list[parentIndex].replyCount || 1) - 1);
							}
						});
					}
				}
			});
		},
		formatTime(timeStr) {
			if (!timeStr) return '';
			const now = new Date();
			const t = new Date(timeStr.replace(/-/g, '/'));
			const diff = Math.floor((now - t) / 1000);
			if (diff < 60) return '刚刚';
			if (diff < 3600) return Math.floor(diff / 60) + '分钟前';
			if (diff < 86400) return Math.floor(diff / 3600) + '小时前';
			if (diff < 86400 * 7) return Math.floor(diff / 86400) + '天前';
			return timeStr.slice(0, 10);
		},
	}
}
</script>

<style lang="scss" scoped>
.comment-mask {
	position: fixed;
	inset: 0;
	z-index: 9999;
	background: rgba(0, 0, 0, 0.5);
	display: flex;
	align-items: flex-end;
}

.comment-panel {
	width: 100%;
	height: 75vh;
	background: #fff;
	border-radius: 30rpx 30rpx 0 0;
	display: flex;
	flex-direction: column;
	transform: translateY(100%);
	transition: transform 0.35s cubic-bezier(0.25, 0.8, 0.25, 1);
	overflow: hidden;

	&.show {
		transform: translateY(0);
	}
}

.panel-header {
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 30rpx 40rpx 20rpx;
	position: relative;
	flex-shrink: 0;
	border-bottom: 1rpx solid #f5f5f5;

	.panel-title {
		font-size: $dj-fs-lg;
		font-weight: 600;
		color: #111;
	}

	.close-btn {
		position: absolute;
		right: 40rpx;
		top: 50%;
		transform: translateY(-50%);
		padding: 10rpx;
	}
}

.comment-list {
	flex: 1;
	overflow-y: auto;
	padding: 20rpx 30rpx;
}

.empty-tip {
	padding: 80rpx 0;
	text-align: center;
	font-size: $dj-fs-base;
	color: #aaa;
}

.comment-item {
	display: flex;
	margin-bottom: $dj-spacing-lg;

	.avatar {
		width: 72rpx;
		height: 72rpx;
		border-radius: 50%;
		flex-shrink: 0;
		margin-right: 20rpx;
		margin-top: 4rpx;
	}

	.comment-body {
		flex: 1;
		min-width: 0;

		.comment-meta {
			display: flex;
			align-items: center;
			margin-bottom: $dj-spacing-xs;

			.nickname {
				font-size: 26rpx;
				font-weight: 600;
				color: #333;
				margin-right: $dj-spacing-sm;
			}

			.time {
				font-size: 22rpx;
				color: #bbb;
			}
		}

		.comment-content {
			font-size: $dj-fs-base;
			color: #333;
			line-height: 1.5;
			word-break: break-all;
		}

		.comment-actions {
			display: flex;
			align-items: center;
			margin-top: 14rpx;
			gap: 30rpx;
		}
	}
}

.action-item {
	display: flex;
	align-items: center;
	gap: 6rpx;

	.action-text {
		font-size: 22rpx;
		color: #bbb;

		&.liked {
			color: $dj-primary;
		}
	}
}

.replies-box {
	margin-top: $dj-spacing-sm;
	background: #f9f9f9;
	border-radius: 12rpx;
	padding: 16rpx 20rpx;

	.reply-item {
		font-size: $dj-fs-sm;
		color: #555;
		line-height: 1.6;
		margin-bottom: 12rpx;
		display: flex;
		flex-wrap: wrap;
		align-items: center;

		.reply-nickname {
			color: $dj-primary;
			font-weight: 600;
		}

		.reply-to {
			color: #888;
		}

		.reply-content {
			color: #555;
		}

		.reply-actions {
			width: 100%;
			display: flex;
			gap: 20rpx;
			margin-top: 6rpx;
		}
	}

	.load-more-reply {
		display: flex;
		align-items: center;
		gap: $dj-spacing-xs;
		font-size: $dj-fs-sm;
		color: $dj-primary;
		margin-top: $dj-spacing-xs;
		cursor: pointer;
	}
}

.list-status {
	padding: 20rpx 0 30rpx;
}

.input-bar {
	display: flex;
	align-items: center;
	padding: 18rpx 24rpx;
	border-top: 1rpx solid #ececec;
	background: #fafafa;
	gap: 16rpx;
	flex-shrink: 0;

	/* #ifdef H5 */
	padding-bottom: calc(18rpx + env(safe-area-inset-bottom));
	/* #endif */

	.emoji-btn {
		width: 64rpx;
		height: 72rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
		transition: opacity 0.2s;
		opacity: 0.55;

		.emoji-icon {
			font-size: 48rpx;
			line-height: 1;
			filter: grayscale(1);
		}

		&.active {
			opacity: 1;

			.emoji-icon {
				filter: none;
			}
		}
	}

	.input-wrap {
		flex: 1;
		display: flex;
		align-items: center;
		background: #fff;
		border: 1rpx solid #d8d8e0;
		border-radius: 40rpx;
		padding: 0 24rpx;
		height: 72rpx;
		box-shadow: 0 1rpx 2rpx rgba(0, 0, 0, 0.03) inset;
	}

	.comment-input {
		flex: 1;
		font-size: $dj-fs-base;
		color: #333;
		background: transparent;
		height: 72rpx;
		line-height: 72rpx;
	}

	.cancel-reply {
		margin-left: 10rpx;
	}

	.send-btn {
		padding: 0 30rpx;
		height: 72rpx;
		display: flex;
		align-items: center;
		background: #cfcfd6;
		border-radius: 40rpx;
		transition: background 0.2s;

		text {
			font-size: $dj-fs-base;
			color: #fff;
			font-weight: 600;
		}

		&.active {
			background: $dj-gradient-primary;
			box-shadow: 0 4rpx 12rpx rgba(94, 114, 247, 0.3);
		}
	}
}

.emoji-panel {
	flex-shrink: 0;
	max-height: 360rpx;
	overflow-y: auto;
	background: #fafafa;
	border-top: 1rpx solid #ececec;
	padding: 16rpx 20rpx;
	display: grid;
	grid-template-columns: repeat(8, 1fr);
	gap: 6rpx;

	.emoji-cell {
		display: flex;
		align-items: center;
		justify-content: center;
		height: 72rpx;
		border-radius: 12rpx;
		background: transparent;
		transition: background 0.15s;

		&:active {
			background: rgba(147, 84, 255, 0.1);
		}

		.emoji-text {
			font-size: 44rpx;
			line-height: 1;
		}
	}
}
</style>

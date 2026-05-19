<template>
  <u-popup v-model="show" mode="bottom" height="70%">
    <view class="comment-list">
      <view v-for="item in comments" :key="item.id" class="comment-item">
        <view class="comment-content">
          <image class="comment-avatar" :src="item.avatar || '/static/img/avatar_default.png'" />
          <text>{{ item.user_id }}：</text>
          <text>{{ item.content }}</text>
          <text class="time">{{ formatTime(item.create_time) }}</text>
        </view>
        <view class="comment-actions">
          <text @click="replyTo(item)">回复</text>
          <text v-if="item.user_id === userId" @click="editComment(item)">编辑</text>
          <text v-if="item.user_id === userId" @click="deleteComment(item.id)">删除</text>
        </view>
        <view class="reply-list" v-if="item.replies && item.replies.length">
          <view v-for="reply in item.replies" :key="reply.id" class="reply-item">
            <text>{{ reply.user_id }}：</text>
            <text>{{ reply.content }}</text>
            <text class="time">{{ formatTime(reply.create_time) }}</text>
            <text v-if="reply.user_id === userId" @click="deleteComment(reply.id)">删除</text>
          </view>
        </view>
      </view>
    </view>
    <view class="comment-input">
      <u-input v-model="input" placeholder="写评论..." />
      <u-button type="primary" @click="submitComment">发送</u-button>
    </view>
  </u-popup>
</template>

<script>
import api from '@/common/request/api'
export default {
  props: {
    videoId: Number,
    show: Boolean
  },
  data() {
    return {
      comments: [],
      input: '',
      replyToId: 0,
      userId: 1, // 假设已登录，实际用登录用户ID
      userInfo: null // 假设有用户信息，实际用登录用户信息
    }
  },
  watch: {
    show(val) {
      if (val) this.loadComments()
    }
  },
  methods: {
    loadComments() {
      uni.request({
        url: 'http://pladmin.lyxdjpt.com/index.php',
        method: 'GET',
        data: { action: 'list', video_id: this.videoId },
        success: (res) => {
          if (res.data.code === 1) this.comments = res.data.data;
          else this.comments = [];
        }
      });
    },
    submitComment() {
      if (!this.input) return uni.showToast({ title: '请输入内容', icon: 'none' });
      uni.request({
        url: 'http://pladmin.lyxdjpt.com/index.php?action=add',
        method: 'POST',
        data: {
          video_id: this.videoId,
          user_id: this.userId,
          user_nick: this.userInfo?.nickname || '',
          avatar: this.userInfo?.avatar || '/static/img/avatar_default.png',
          content: this.input,
          parent_id: this.replyToId || ''
        },
        header: { 'content-type': 'application/json' },
        success: (res) => {
          if (res.data.code === 1) {
            uni.showToast({ title: '评论成功', icon: 'success' });
            this.input = '';
            this.loadComments();
          } else {
            uni.showToast({ title: res.data.msg || '评论失败', icon: 'none' });
          }
        }
      });
    },
    replyTo(item) {
      this.replyToId = item.id
      this.input = `@${item.user_id} `
    },
    editComment(item) {
      this.input = item.content
      this.replyToId = item.id
      // 可扩展为弹窗编辑
    },
    deleteComment(commentId) {
      uni.request({
        url: 'http://pladmin.lyxdjpt.com/index.php?action=delete',
        method: 'POST',
        data: {
          id: commentId,
          user_id: this.userId
        },
        header: { 'content-type': 'application/json' },
        success: (res) => {
          if (res.data.code === 1) {
            uni.showToast({ title: '删除成功', icon: 'success' });
            this.loadComments();
          } else {
            uni.showToast({ title: res.data.msg || '删除失败', icon: 'none' });
          }
        }
      });
    },
    formatTime(ts) {
      const date = new Date(ts * 1000)
      return date.toLocaleString()
    }
  }
}
</script>

<style>
.comment-list { padding: 20rpx; }
.comment-item { border-bottom: 1px solid #eee; padding: 10rpx 0; }
.comment-actions text { margin-right: 20rpx; color: #007aff; }
.reply-list { margin-left: $dj-spacing-lg; color: #888; }
.comment-input { display: flex; padding: 10rpx; border-top: 1px solid #eee; }
.comment-avatar { width: 40rpx; height: 40rpx; border-radius: 50%; margin-right: 10rpx; }
</style> 
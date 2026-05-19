<template>
	<view class="page_content">
		<!-- #ifdef MP-TOUTIAO -->
			<view class="main_content" >
		<!-- #endif -->
		<!-- #ifndef MP-TOUTIAO -->
		<view class="main_content" :style="[{ paddingTop: barHeight + 'px' }]">
		<!-- #endif -->
			<!-- #ifdef MP-WEIXIN -->
			<view v-if="videoData[videoIndex].adsTrue && daoTime!=0" style="position: fixed;left: 0;right: 0;top: 0;bottom: 0;z-index: 1000;">
				
			</view>
			<!-- #endif -->
			<swiper class="swiper" circular :vertical="true" :duration="300" :current="current" @change="swiperChange">
				<swiper-item class="swiper_item" v-for="(item, index) in videoData" :key="index">
					<view class="videos" v-if="videoIndex == index" @click="videoClick"  >
						<!-- #ifdef H5 -->
						<video class="video" :id="'video' + item.id" :ref="'video' + item.id"
							:loop="false" :controls="false" :autoplay="false" :object-fit="isLandscape ? 'contain' : 'cover'"
							:enable-progress-gesture="true" :show-center-play-btn="false" :src="item.url" :poster="item.image"
							@play="isPlaying = true, isPlayError = false" @pause="isPlaying = false" @timeupdate="videoTimeUpdate"
							@ended="videoEnded" @error="videoError" @loadedmetadata="VideoLoadedmetadata">
						</video>
						<!-- #endif -->
						<!-- #ifdef MP-WEIXIN -->
						<video v-if="!item.adsTrue " class="video" :unit-id="shipinAd"   @close="onAdclose()" :id="'video' + item.id" :ref="'video' + item.id"
							:loop="false" :controls="false" :autoplay="videoIndex == index" :object-fit="isLandscape ? 'contain' : 'cover'"
							:enable-progress-gesture="true" :show-center-play-btn="false" :src="item.url"
							@play="isPlaying = true, isPlayError = false" @timeupdate="videoTimeUpdate"
							@ended="videoEnded" @error="videoError" @loadedmetadata="VideoLoadedmetadata">
						</video>
						<view  v-else class="video" style="background-color: #fff;position: relative;z-index: 2;padding: 40px 0;">
							
							<view v-if=" daoTime!=0" style="display: flex;flex-direction: column;align-items: center;" >
								<view style="font-size: 16px;font-weight: 700;color: var(--dj-primary);margin-bottom: 60rpx;">{{ $t('video.nextBetter') }}</view>
								<view style="font-size: 14px; color: var(--dj-primary);margin-bottom: 40rpx;">
									{{ $t('video.adCountdown', [daoTime]) }}
								</view>
								
								
							</view>
							<view v-else style="display: flex;flex-direction: column;align-items: center;" >
								<view style="font-size: 65rpx;font-weight: 700;color: var(--dj-primary);margin-bottom: 60rpx;padding-top: 36rpx;">{{ $t('video.continueWatch') }}</view>
								
							</view>
							<!-- <view style="width: 100%;height: 200rpx;"> -->
								<!-- <ad-custom   :unit-id="tcAd" bindload="adLoad" binderror="adError" bindclose="adClose"></ad-custom> -->
								
							<!-- </view> -->
							<view class="dwCenter">
								<ad  :unit-id="tcAd" ad-type="video" ad-theme="white" bindload="adLoad" binderror="adError" bindclose="adClose"></ad>
							</view>
								
							
						</view>
						<!-- #endif -->
						<!-- #ifdef MP-TOUTIAO -->
											
						<video class="video" :id="'video' + item.id" :ref="'video' + item.id"
							:loop="false" :controls="false" :autoplay="videoIndex == index" :object-fit="isLandscape ? 'contain' : 'cover'"
							:enable-progress-gesture="true" :show-center-play-btn="false" :src="item.url"
							@play="isPlaying = true, isPlayError = false" @timeupdate="videoTimeUpdate"
							@ended="videoEnded" @error="videoError" @loadedmetadata="VideoLoadedmetadata">
						</video>
						<!-- #endif -->
						<view class="verror" v-if="isPlayError">
							<image class="image" :src="item.image" mode="aspectFill"></image>
							<view class="content">
								<u-icon name="info-circle-fill" color="#fff" size="50"></u-icon>
								<text class="text">{{ $t('video.playError') }}</text>
							</view>
						</view>
					</view>
					<view class="vcover" v-else>
						<image class="image" :src="item.image" mode="aspectFill"></image>
					</view>
					<view class="buttons" v-if="!isDrag && !isPlayError && videoIndex == index">
						<u-icon v-if="!isPlaying" name="play-right-fill" color="rgba(255, 255, 255, 0.8)" :size="60" @click="videoPlay"></u-icon>
					</view>
					<view class="sidebar" v-if="!isDrag && !isPlayError && videoIndex == index">
						<!-- 评论 -->
						<view class="item" @click="openComment(item.video.id)">
							<view class="icon-circle">
								<svg class="icon-svg" viewBox="0 0 48 48" fill="none">
									<path d="M6 10a4 4 0 0 1 4-4h28a4 4 0 0 1 4 4v18a4 4 0 0 1-4 4H15l-9 7V10z" stroke="#fff" stroke-width="2.5" stroke-linejoin="round" fill="rgba(255,255,255,0.08)"/>
									<circle cx="17" cy="19" r="2" fill="#fff"/>
									<circle cx="24" cy="19" r="2" fill="#fff"/>
									<circle cx="31" cy="19" r="2" fill="#fff"/>
								</svg>
							</view>
							<text v-if="item.video.comments > 0" class="text">{{ item.video.comments }}</text>
						</view>
						<!-- 点赞 -->
						<view class="item" :class="{ 'liked-anim': likeAnimIndex === index }" @click="handleLikes(item.vid, index)">
							<view class="icon-circle">
								<svg v-if="!item.is_like" class="icon-svg like-icon" viewBox="0 0 48 48"><path d="M24 42C13 33 6 27 6 19.5C6 13.7 10.7 9 16.5 9c3.2 0 6.1 1.6 7.5 4.1C25.4 10.6 28.3 9 31.5 9 37.3 9 42 13.7 42 19.5c0 7.5-7 13.5-18 22.5z" fill="rgba(255,255,255,0.0)" stroke="#fff" stroke-width="2.5"/></svg>
								<svg v-else class="icon-svg like-icon liked-glow" viewBox="0 0 48 48"><defs><linearGradient id="likeGrad" x1="0" y1="0" x2="1" y2="1"><stop offset="0%" stop-color="#fe2c55"/><stop offset="100%" stop-color="#fd5b36"/></linearGradient></defs><path d="M24 42C13 33 6 27 6 19.5C6 13.7 10.7 9 16.5 9c3.2 0 6.1 1.6 7.5 4.1C25.4 10.6 28.3 9 31.5 9 37.3 9 42 13.7 42 19.5c0 7.5-7 13.5-18 22.5z" fill="url(#likeGrad)" stroke="#fff" stroke-width="2.5"/></svg>
							</view>
							<text class="text like-num" :class="{ active: item.is_like }">{{ item.likes }}</text>
						</view>
						<!-- 收藏 -->
						<view class="item" :class="{ 'collected-anim': collectAnimIndex === index }" @click="handleCollect(item.vid, item.video.is_favorite, index)">
							<view class="icon-circle">
								<svg v-if="!item.video.is_favorite" class="icon-svg collect-icon" viewBox="0 0 48 48"><polygon points="24,7 29.1,18.2 41.5,19.8 32.5,28.6 35.2,41 24,34.2 12.8,41 15.5,28.6 6.5,19.8 18.9,18.2" fill="rgba(255,255,255,0.0)" stroke="#fff" stroke-width="2.5" stroke-linejoin="round"/></svg>
								<svg v-else class="icon-svg collect-icon collected-glow" viewBox="0 0 48 48"><defs><linearGradient id="starGrad" x1="0" y1="0" x2="1" y2="1"><stop offset="0%" stop-color="#ffe066"/><stop offset="100%" stop-color="#ffd600"/></linearGradient></defs><polygon points="24,7 29.1,18.2 41.5,19.8 32.5,28.6 35.2,41 24,34.2 12.8,41 15.5,28.6 6.5,19.8 18.9,18.2" fill="url(#starGrad)" stroke="#fff" stroke-width="2.5" stroke-linejoin="round"/></svg>
							</view>
							<text class="text collect-num" :class="{ active: item.video.is_favorite }">{{ item.video.favorites }}</text>
						</view>
						<!-- 分享 -->
						<!-- #ifdef MP-WEIXIN -->
						<view class="item">
							<button class="btn share-btn" open-type="share">
								<view class="icon-circle">
									<svg class="icon-svg" viewBox="0 0 44 44" fill="none">
										<circle cx="32" cy="12" r="4" fill="#fff"/>
										<circle cx="12" cy="22" r="4" fill="#fff"/>
										<circle cx="32" cy="32" r="4" fill="#fff"/>
										<path d="M15.7 20.6L28.3 13.4" stroke="#fff" stroke-width="2.5" stroke-linecap="round"/>
										<path d="M15.7 23.4L28.3 30.6" stroke="#fff" stroke-width="2.5" stroke-linecap="round"/>
									</svg>
								</view>
							</button>
						</view>
						<!-- #endif -->
					</view>
					<view class="infobox" v-if="!isDrag && videoIndex == index">
						<view class="title">{{ item.video.display_title || item.video.title }}</view>
						<view class="desc-box">
							<view class="desc-text" :class="{ 'desc-unfold': isUnfold }">
								{{ isUnfold ? (item.video.display_desc||item.video.description) : ((item.video.display_desc||item.video.description) ? (item.video.display_desc||item.video.description).slice(0, 13) + ((item.video.display_desc||item.video.description).length > 13 ? '...' : '') : '') }}
							</view>
							<text v-if="(item.video.display_desc||item.video.description) && (item.video.display_desc||item.video.description).length > 13" class="desc-toggle" @click="isUnfold = !isUnfold">{{ isUnfold ? $t('video.collapse') : $t('video.expand') }}</text>
						</view>
						<view class="content">
							<text class="text1">{{ formatEpName(item) }}{{ $t('video.ofTotal', [item.video.episodes]) }}</text>
							<text class="text2 more-btn" @click="openVideoDetail(item.video.id)">{{ $t('video.viewMoreEpisodes') }}</text>
						</view>
					</view>
					<view class="progress" v-if="duration > 0 && videoIndex == index">
						<view class="bartext" v-if="isDrag">
							<text class="text1">{{ dragStarTime }}</text>
							<text class="text0">/</text>
							<text class="text2">{{ dragEndTime }}</text>
						</view>
						<view class="barview" :class="{ active: isDrag }">
							<slider :value="currentTime" :min="0" :max="duration"
								:blockSize="6" backgroundColor="#333" activeColor="#fff"
								@change="sliderChange" @changing="sliderChanging">
							</slider>
						</view>
					</view>
				</swiper-item>
			</swiper>
		</view>
		<!-- <tabBar v-if="tabChange" selectedIndex =2></tabBar> -->
		<!-- #ifdef MP-WEIXIN  -->
		<view class="ad_box" v-if="userInfo && config.uniad_switch == '1' && config.adpid">
		    <button class="button" v-if="showAd && isLoaded" @click="adCheck"></button>
		</view>
		<!-- #endif -->
		<CommentPanel :show="showComment" :video-id="commentVideoId" @close="showComment = false" />
		<CustomTabBar current="/pages/home/video" />
	</view>
</template>

<script>
	import { mapState, mapGetters, mapMutations, mapActions } from "vuex"
	import CommentPanel from '../video/components/CommentPanel.vue'
	import CustomTabBar from '@/components/CustomTabBar.vue'
	export default {
		components: { CommentPanel, CustomTabBar },
		data() {
			return {
				isIos: uni.getSystemInfoSync().osName == 'ios' ? true : false,
				barHeight: uni.getSystemInfoSync().statusBarHeight,
				
				current: 0,
				currentTime: 0, // 当前视频播放进度
				
				duration: 0, // 当前视频总时长
				isDrag: false, // 拖动进度条状态
				dragStarTime: "00:00", // 拖拽开始时长
				dragEndTime: "00:00", // 拖拽结束时长
				
				isPlaying: false, // 播放状态
				isPlayError: false, // 播放错误
				
				clickNum: 0, // 点击次数
				clickTimer: null, // 点击定时器
				
				isUnfold: false, // 展示文字
				
				originData: [], // 源数据
				originIndex: 0, // 源数据索引
				oldIndex: 0, // 源数据上一次索引
				
				videoData: [], // 渲染数据
				videoIndex: 0, // 渲染数据索引
				
				isLandscape: false, // 是否横屏
				
				countdown: 0,

				videoAd: null,
				isLoaded: false,
				//tab更新
				tabChange:false,
				//插屏id
				chapingId:"",
				//插屏调用
				chaAd:null,
				//判断插屏广告是否存在
				chapingAd:false,
				//视频野广告
				shipinAd:"",
				AdNumber:getApp().globalData.isADTc,
				tcAd:null,
				isTzt:false,
			
				//daojishi 
				daoTime:3,
				//jishiqi
				jiTime:'',
				showComment: false,
				commentVideoId: null,
				likeAnimIndex: null,
				collectAnimIndex: null,
				likeAnimTimer: null,
				collectAnimTimer: null,
			}
		},
		computed: {
			...mapGetters("user", ["token", "userInfo", "showAd"]),
			...mapGetters("app", ["videoAutoplay", "adCountdown", "config"]),
		},
		watch: {
			adCountdown(newValue, oldValue) {
				this.countdown = newValue
			},
			token(newValue, oldValue) {
				// #ifdef MP-WEIXIN
				newValue && (this.config?.uniad_switch == '1') && this.config?.adpid && this.adCheck()
				// #endif
			},
			userInfo(newValue, oldValue) {
				this.userInfo = newValue
			},
			// AdNumber(newValue, oldValue) {
			// 	console.log(newValue,'查看变化')
			// 	if(newValue == 3){
			// 		uni.showModal({
			// 			title: '免广告权限',
			// 			content: `温馨提示：可在[我的]页面开通免广告权限`,
			// 			showCancel:false,
			// 			success: res => {
			// 				if (res.confirm) {
								
			// 				}
			// 			}
			// 		})
			// 		this.AdNumber = 0
			// 		getApp().globalData.isADTc = this.AdNumber
			// 	}else{
			// 		this.AdNumber = newValue
			// 		getApp().globalData.isADTc = this.AdNumber
			// 	}
			// }
		},
		
     
		onLoad() {
			if (!this.token) {
				uni.showModal({
					title: this.$t('common.tip'),
					content: this.$t('common.loginFirst'),
					showCancel: false,
					success: res => {
						uni.navigateTo({ url: '/pages/login/login' });
					}
				});
				return;
			}
			// #ifdef MP-WEIXIN
			this.getAD()
			
			this.token && (this.config?.uniad_switch == '1') && this.config?.adpid && this.adCheck()
			// #endif
			this.getRecommendList()
			uni.$on('loginSuccess', this.refreshPage)
			
		},
		onUnload() {
			uni.$off('loginSuccess', this.refreshPage)
			// #ifdef H5
			if (this._touchPlayHandler) {
				document.removeEventListener('touchstart', this._touchPlayHandler)
				document.removeEventListener('click', this._touchPlayHandler)
				this._touchPlayHandler = null
			}
			// #endif
		},
		onShow() {
			this.tabChange = true
			// #ifdef MP-WEIXIN
				if(this.userInfo.mgg == 1){
					this.shipinAd = null,
					this.chapingAd = false,
					this.chaAd = null
					this.isTzt = false
					this.getRecommendList()
					
					
				}
				
			  // #endif 
			  
			// #ifdef H5
			if(this.originData.length) {
				this.$nextTick(() => {
					this.videoPlay()
				})
			}
			// #endif
		},
		onTabItemTap() {
				this.$request('common.point', {item_id:3 ,   platform:this.$utils.platforms(), point_type:5 },false).then(res => {
				
				})
			},
		onHide() {
			this.videoPause()
			this.tabChange = false
			// #ifdef H5
			if (this._touchPlayHandler) {
				document.removeEventListener('touchstart', this._touchPlayHandler)
				document.removeEventListener('click', this._touchPlayHandler)
				this._touchPlayHandler = null
			}
			this._pendingPlay = false
			// #endif
		},
		onShareAppMessage(res) {
			// #ifdef MP-WEIXIN
			return {
				title: this.videoData[this.videoIndex].video.display_title || this.videoData[this.videoIndex].video.title,
				path: `/pages/video/play?scene=${this.shareData.spm}&id=${this.videoData[this.videoIndex].vid}`,
				imageUrl: this.videoData[this.videoIndex].image
			}
			// #endif
		},
		methods: {
			...mapActions("user", ["checkAdTask"]),
			formatEpName(ep) {
				const nameStr = ep.display_title || ep.name || ''
				const m = nameStr.match(/\d+/)
				return m ? this.$t('video.episode', [m[0]]) : nameStr
			},
			
			daoji(){
				
				let that = this
				 this.jiTime = setInterval(function() {
				    console.log('zou')
				    
				    // 如果时间为 0，清除计时器并执行完成回调函数
				    if (that.daoTime<1) {
						 
				      clearInterval(that.jiTime);
				    } else {
				      that.daoTime = that.daoTime - 1;
				    }
				  }, 1000);
				
			
			},
			//获取插屏广告
			getAD(){
				// #ifdef MP-WEIXIN
				
				this.$request('common.wxguanggao', '',false).then(res => {
					if(res.code === 1) {
						console.log(res)
						
						if(res.data.list.gg_zanting_id && res.data.list.gg_zanting_switch == '1'){
							if(res.data.mgg == 1){
								
							}else{
								this.chapingId = res.data.list.gg_zanting_id
								this.chapingAd = true
								this.adIn()
							}
						
						}
						if(res.data.list.gg_xiahuatc_id && res.data.list.gg_xiahuatc_switch == '1'){
							if(res.data.mgg == 1){
								
							}else{
								this.tcAd = res.data.list.gg_xiahuatc_id
								this.isTzt = true
								
							}	
									
						
						}
						
						
						if(res.data.list.gg_xiahua_id && res.data.list.gg_xiahua_switch == '1'){
							if(res.data.mgg == 1){
								
							}else{
								this.shipinAd = res.data.list.gg_xiahua_id
							
							}
						
						}
						
					}
				})
				// #endif
			},
			onAdclose(){
				
				this.AdNumber++
			},
			adIn(){
				if(this.chaAd){
					let that = this
					 setTimeout(function(){
						that.chaAd.show().catch((err) => {
							  console.log('怎么样',err)
						}) 
					 }, 0)
					  
					
				}else{
					if (wx.createInterstitialAd) {
					  this.chaAd = wx.createInterstitialAd({
					    adUnitId: this.chapingId
					  })
					  this.chaAd.onLoad(() => {
						    console.log('zunbeile')
					  })
					  this.chaAd.onError((err) => {
					    
					  })
					  this.chaAd.onClose(() => {
						  this.AdNumber++
						 
					  })
					}
				}
			},
			async adCheck() {
				console.log('进入')
				// #ifdef MP-WEIXIN
				await this.checkAdTask()
				if(this.showAd) {
					if(this.videoAd) this.adShow()
					else this.adInit()
				} else {
					this.isLoaded = false
				}
				// #endif
			},
			adInit() {
				// #ifdef MP-WEIXIN
				// 在页面onLoad回调事件中创建激励视频广告实例
				if (wx.createRewardedVideoAd) {
					this.videoAd = wx.createRewardedVideoAd({
						adUnitId: this.config.adpid
					})
					this.videoAd.onLoad(() => this.isLoaded = true)
					this.videoAd.onError(() => this.isLoaded = false)
					this.videoAd.onClose((res) => {
						
						if(res && res.isEnded) {
							this.$request('task.finish', { type: 'uniad_success' }).then(res => {
								res.code == 1 && (this.$u.toast(this.$t('video.adReward')), this.checkAdTask())
							})
						}
						this.videoPlay()
					})
				}
				// #endif
			},
			adShow() {
				// #ifdef MP-WEIXIN
				// 用户触发广告后，显示激励视频广告
				if (this.videoAd) {
					this.videoPause()
					this.videoAd.show().catch(() => {
						// 失败重试
						this.videoAd.load()
							.then(() =>  this.videoAd.show())
							.catch(() => this.videoPlay())
					})
				} else {
					this.adCheck()
				}
				// #endif
			},
			// 获取推荐视频
			getRecommendList() {
				let  status = uni.getSystemInfoSync().uniPlatform
				const obj = {
					
					platform: status == 'mp-weixin'? 2 :1,
					
				}
				this.$request('video.recommend',obj).then(res => {
					if(res.code === 1) {
						if(res.data && res.data.length) {
							// 新后端推荐接口已带 url 字段（第一集地址），直接用
							this.originData = this.originData.concat(res.data)
							this.initSwiperData(this.originIndex, 1)
						}
					}
				})
			},
			// 初始化swiper数据
			initSwiperData(originIndex = this.originIndex, init = 0) {
				const originDataLength = this.originData.length;
				const videoList = [];
				videoList[this.videoIndex] = this.originData[originIndex];
				videoList[this.videoIndex - 1 == -1 ? 2 : this.videoIndex - 1] = this.originData[originIndex - 1 == -1 ? originDataLength - 1 : originIndex - 1];
				videoList[this.videoIndex + 1 == 3 ? 0 : this.videoIndex + 1] = this.originData[originIndex + 1 == originDataLength ? 0 : originIndex + 1];
				this.videoData = videoList;
				
			
				if (this.oldIndex >= this.originData.length) {
					this.oldIndex = 0
				}
				if (this.oldIndex < 0) {
					this.oldIndex = this.originData.length - 1
				}
				
				// 重置进度条状态
				this.duration = 0
				this.currentTime = 0
				this.isDrag = false
				this.isPlayError = false
				// H5自动播放：nextTick 等 DOM 渲染后用原生 play() 尝试（可捕获 autoplay 策略拒绝）
				// #ifdef H5
				this.$nextTick(() => {
					this.videoPlay()
				})
				// #endif
			},
			// swiper切换
			swiperChange(event) {
				const { current } = event.detail;
				console.log(current,this.videoData[current],'滑动')
				if(this.videoData[current].adsTrue){
					this.daoji()
				}else{
					 clearInterval(this.jiTime);
					this.daoTime = 3
					
				}
				const originDataLength = this.originData.length;
				if (this.videoIndex - current == 2 || this.videoIndex - current == -1) {
					this.originIndex = this.originIndex + 1 == originDataLength ? 0 : this.originIndex + 1;
					this.videoIndex = this.videoIndex + 1 == 3 ? 0 : this.videoIndex + 1;
					this.oldIndex = this.originIndex - 1
					this.initSwiperData(this.originIndex);
					if(this.originIndex == this.originData.length - 1) {
						this.getRecommendList()
					}
				} else if (this.videoIndex - current == -2 || this.videoIndex - current == 1) {
					this.originIndex = this.originIndex - 1 == -1 ? originDataLength - 1 : this.originIndex - 1;
					this.videoIndex = this.videoIndex - 1 == -1 ? 2 : this.videoIndex - 1;
					this.oldIndex = this.originIndex + 1
					this.initSwiperData(this.originIndex);
				}
			},
			// 播放
			videoPlay() {
				// #ifdef H5
				const item = this.originData[this.originIndex]
				if (!item) return
				try {
					// UniApp H5 的 <video> id 挂在 wrapper 上，需 querySelector 找到真实 video 元素
					const wrapper = document.getElementById('video' + item.id)
					if (!wrapper) return
					const el = wrapper.tagName === 'VIDEO' ? wrapper : wrapper.querySelector('video')
					if (!el) return
					const p = el.play()
					if (p !== undefined) {
						p.then(() => {
							this.isPlaying = true
							this._pendingPlay = false
						}).catch((err) => {
							if (err && err.name === 'AbortError') {
								// src 刚切换导致加载中断，短暂等待后重试
								setTimeout(() => this.videoPlay(), 150)
							} else {
								// NotAllowedError：autoplay 策略阻止，等用户手势
								this._pendingPlay = true
								this.isPlaying = false
								this._addPlayOnTouchListener()
							}
						})
					} else {
						this.isPlaying = true
					}
				} catch (e) {
					// 降级：直接用 UniApp 上下文播放
					const video = this.getVideoCtx()
					if (video) { video.play(); this.isPlaying = true }
				}
				return
				// #endif
				const video = this.getVideoCtx()
				if(!video) return
				video.play()
				this.isPlaying = true
			},
			// #ifdef H5
			_addPlayOnTouchListener() {
				if (this._touchPlayHandler) return
				this._touchPlayHandler = () => {
					if (this._pendingPlay) {
						this._pendingPlay = false
						document.removeEventListener('touchstart', this._touchPlayHandler)
						document.removeEventListener('click', this._touchPlayHandler)
						this._touchPlayHandler = null
						this.videoPlay()
					}
				}
				// 同时监听 touchstart（移动端）和 click（PC 端）
				document.addEventListener('touchstart', this._touchPlayHandler)
				document.addEventListener('click', this._touchPlayHandler)
			},
			// #endif
			// 暂停
			videoPause() {
			
				const video = this.getVideoCtx()
				if(!video) return
				video.pause()
				this.isPlaying = false
			},
			// 播放结束
			videoEnded(e) {
				// #ifdef H5
				if(this.$utils.platforms() === 'wxOfficialAccount' && uni.getSystemInfoSync().platform == 'ios') {
					if (this.videoIndex < 2) {
						this.current = this.videoIndex + 1
					} else {
						this.current = 0
					}
				} else {
					if(this.videoAutoplay == 1) {
						if (this.videoIndex < 2) {
							this.current = this.videoIndex + 1
						} else {
							this.current = 0
						}
					}
				}
				// #endif
				
				// #ifndef H5
				if (this.videoIndex < 2) {
					this.current = this.videoIndex + 1
				} else {
					this.current = 0
				}
				// #endif
			},
			// 视频播放出错
			videoError() {
				this.isPlayError = true
			},
			// 点击
			videoClick() {
				const item = this.videoData[this.videoIndex]
				if (!item || !item.video) return
				this.videoPause()
				const time = Math.floor(this.currentTime)
				// 把当前播放信息存入 globalData，play.vue 可立即起播，不用等 API
				getApp().globalData.entryVideo = {
					url:     item.url,
					image:   item.image || item.video.image,
					cover:   item.image || item.video.image,
					id:      item.id,
					videoId: item.vid,
					time,
					title:   item.video.display_title || item.video.title,
				}
				let url = `/pages/video/play?id=${item.video.id}`
				if (item.id) url += `&episodeId=${item.id}`
				if (time > 0) url += `&t=${time}`
				uni.navigateTo({ url })
			},
			openVideoDetail(id) {
				uni.navigateTo({ url: `/pages/video/play?id=${id}` })
			},
			// 元数据加载完毕
			VideoLoadedmetadata(e) {
				const { width, height } = e.detail
				this.isLandscape = width >= height ? true : false
			},
			// 拖拽结束
			sliderChange(e) {
				const video = this.getVideoCtx()
				if(!video) return
				// 停止拖拽
				this.isDrag = false
				// 判断一下是否大于基础时间
				if (this.duration > 0.1) {
					// 跳到指定时间点
					video.seek(e.detail.value)
					// 并调用播放
					video.play()
				}
			},
			// 正在拖拽
			sliderChanging(e) {
				// 开始拖拽
				this.isDrag = true
				// 刷新时间
				this.dragStarTime = this.$utils.formatTime(e.detail.value)
				// 总时间
				this.dragEndTime = this.$utils.formatTime(this.duration)
			},
			// 更新进度
			videoTimeUpdate(e) {
				if(this.isDrag) return
			    const { currentTime, duration } = e.detail
				this.currentTime = Math.trunc(currentTime)
				this.duration = Math.trunc(duration)
				// 刷新时间
				this.dragStarTime = this.$utils.formatTime(this.currentTime)
				// 总时间
				this.dragEndTime = this.$utils.formatTime(this.duration)
			},
			// 获取video标签上下文
			getVideoCtx() {
				return uni.createVideoContext('video'+ this.originData[this.originIndex].id, this)
			},
			// 点赞
			handleLikes(vid, index) {
				if (this.likeAnimTimer) clearTimeout(this.likeAnimTimer);
				this.likeAnimIndex = index;
				this.likeAnimTimer = setTimeout(() => { this.likeAnimIndex = null }, 350);
				const isLiked = this.videoData[index].is_like
				const obj = {
					vid: String(vid),
					action: isLiked ? 'unlike' : 'like'
				}
				this.$request('video.likes', obj).then(res => {
					if(res.code === 1) {
						if(!isLiked) {
							this.videoData[index].is_like = 1
							this.videoData[index].likes++
						} else {
							this.videoData[index].is_like = 0
							this.videoData[index].likes = Math.max(0, this.videoData[index].likes - 1)
						}
					}
				})
			},
			// 收藏
			handleCollect(id, is_favorite, index) {
				if (this.collectAnimTimer) clearTimeout(this.collectAnimTimer);
				this.collectAnimIndex = index;
				this.collectAnimTimer = setTimeout(() => { this.collectAnimIndex = null }, 350);
				if(this.videoData[index].video.is_favorite == 0) {
					const obj = { vid: id }
					this.$request('video.addFavorite', obj).then(res => {
						if(res.code === 1) {
							this.videoData[index].video.is_favorite = 1
							this.videoData[index].video.favorites++
						}
					})
				} else {
					const obj = { ids: id, type: 'favorite' }
					this.$request('video.deleteRecord', obj).then(res => {
						if(res.code === 1) {
							this.videoData[index].video.is_favorite = 0
							this.videoData[index].video.favorites = Math.max(0, this.videoData[index].video.favorites - 1)
						}
					})
				}
			},
			openComment(videoId) {
				this.commentVideoId = videoId;
				this.showComment = true;
			},
			refreshPage() {
				this.getUserInfo && this.getUserInfo()
				this.getRecommendList && this.getRecommendList()
			},
		}
	}
</script>

<style lang="scss" scoped>
	.dwCenter{
			width:100%;
		    
		    position:fixed;
		    top:50%;
		    margin-top:-265rpx;/*高度的一半*/
		}
	.page_content {
		.ad_box {
			position: absolute;
			bottom: 80rpx;
			right: 30rpx;
			z-index: 1;
			
			.button {
				width: 80rpx;
				height: 80rpx;
				border-radius: 50%;
				background: $dj-gradient-primary;
				outline: none;
				background-image: url('https://img.nymaite.com/video_short/icons/jifen.png');
				background-size: cover;
			}
		}
		
		.main_content {
			position: relative;
			background: #000;
			height: 100vh;

			.swiper {
				width: 100%;
				height: 100vh;
				background: #000;
				overflow: hidden;
				
				.swiper_item {
					position: relative;
					color: #fff;
					
					.videos {
						width: 100%;
						height: 100%;
						position: relative;
						
						.video {
							width: 100%;
							height: 100%;
							border-radius: 16rpx;
							overflow: hidden;
						}
						
						.poster-image {
							width: 100%;
							height: 100%;
							opacity: 0.3;
						}
						
						.ui-overlay {
							position: absolute;
							top: 0;
							left: 0;
							width: 100%;
							height: 100%;
							z-index: 1;
							
							.play-pause-btn {
								position: absolute;
								top: 50%;
								left: 50%;
								transform: translate(-50%, -50%) scale(1.5);
								opacity: 0;
								transition: all 0.3s ease;
								pointer-events: none;

								&.visible {
									transform: translate(-50%, -50%) scale(1);
									opacity: 0.8;
									pointer-events: all;
								}
							}
							
							.like-animation {
								position: absolute;
								top: 50%;
								left: 50%;
								transform: translate(-50%, -50%) scale(0);
								opacity: 0;
								
								&.visible {
									animation: like-pop 0.8s ease-out;
								}
							}
							
							.info-bar {
								position: absolute;
								bottom: 50rpx;
								left: 0;
								width: calc(100% - 150rpx);
								padding: 0 30rpx;
								color: #fff;
								.title {
									font-size: 34rpx;
									font-weight: bold;
									margin-bottom: 15rpx;
									text-shadow: 0 2rpx 4rpx rgba(0,0,0,0.5);
								}
								.description {
									font-size: 28rpx;
									text-shadow: 0 1rpx 2rpx rgba(0,0,0,0.5);
								}
							}
							
							.sidebar {
								position: absolute;
								bottom: 50rpx;
								right: 20rpx;
								display: flex;
								flex-direction: column;
								gap: 40rpx;
								align-items: center;
								.icon-circle {
									width: 88rpx;
									height: 88rpx;
									background: rgba(0, 0, 0, 0.40);
									border-radius: 50%;
									display: flex;
									align-items: center;
									justify-content: center;
									border: 1rpx solid rgba(255, 255, 255, 0.20);
									margin-bottom: 8rpx;
								}
								.action-item {
									display: flex;
									flex-direction: column;
									align-items: center;
									color: #fff;
									.icon-wrapper {
										width: 80rpx;
										height: 80rpx;
										border-radius: 50%;
										background: rgba(255,255,255,0.2);
										display: flex;
										justify-content: center;
										align-items: center;
										transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
										.u-icon {
											color: #fff;
										}

										&:active {
											transform: scale(0.9);
										}
										&.liked {
											animation: liked-pop 0.5s ease-out;
											.u-icon { color: #ff4500; }
										}
										&.collected {
											transform: scale(1.1);
											.u-icon { color: #ffeb3b; }
										}
									}
									.count-text {
										font-size: 24rpx;
										margin-top: 10rpx;
									}
									.share-btn {
										background-color: transparent;
										padding: 0;
										margin: 0;
										line-height: 1;
										&::after {
											border: none;
										}
									}
								}
							}
							
							.progress-bar-container {
								position: absolute;
								bottom: 0;
								left: 0;
								width: 100%;
								height: 4rpx;
								background: rgba(255,255,255,0.2);
								.progress-bar {
									width: 0;
									height: 100%;
									background: #fff;
									transition: width 0.1s linear;
								}
							}
						}
					}
					
					.vcover {
						width: 100%;
						height: 100%;
						
						.image {
							width: 100%;
							height: 100%;
							opacity: 0.3;
						}
					}
					
					.buttons {
						position: absolute;
						top: 50%;
						left: 50%;
						z-index: 0;
						transform: translate(-50%, -50%);
						pointer-events: auto;
					}
					
					.sidebar {
						position: absolute;
						right: 30rpx;
						bottom: 260rpx;
						z-index: 1;

						.item {
							margin-bottom: 20rpx;
							text-align: center;
							display: flex;
							flex-direction: column;
							align-items: center;

							&:last-child {
								margin-bottom: 0;
							}

							.icon-circle {
								width: 88rpx;
								height: 88rpx;
								background: rgba(0, 0, 0, 0.40);
								border-radius: 50%;
								display: flex;
								align-items: center;
								justify-content: center;
								border: 1rpx solid rgba(255, 255, 255, 0.20);
								margin-bottom: 8rpx;
							}

							.btn.share-btn {
								background: transparent;
								padding: 0;
								margin: 0;
								line-height: 1;
								&::after { border: none; }
							}
							
							.text {
								font-size: 28rpx;
								
								&.active {
									color: var(--dj-primary);
								}
							}
							
							.btn {
								display: block;
								background: $dj-gradient-primary;
								color: #fff;
								box-sizing: border-box;
								font-size: 28rpx;
								line-height: 40rpx;
								
								&::after {
									display: none;
								}
							}
						}
					}
					
					.infobox {
						width: 76%;
						position: absolute;
						bottom: 60rpx;
						/* #ifdef H5 */
						bottom: 180rpx;
						/* #endif */
						left: 30rpx;
						z-index: 1;
						font-size: 32rpx;
						
						.title {
							font-weight: 700;
							margin-bottom: 20rpx;
						}
						
						.desc-box {
							margin-bottom: 6rpx;
							position: relative;
							display: flex;
							align-items: flex-end;
						}
						.desc-text {
							color: #eaeaea;
							font-size: 28rpx;
							line-height: 1.4;
							max-width: 90%;
							word-break: break-all;
							white-space: pre-line;
							overflow: hidden;
							text-overflow: ellipsis;
							display: inline-block;
							vertical-align: middle;
							margin-right: 8rpx;
							transition: max-width 0.2s;
						}
						.desc-unfold {
							max-width: 100%;
							white-space: pre-line;
							word-break: break-all;
							display: inline-block;
						}
						.desc-toggle {
							color: #aaa;
							font-size: 26rpx;
							margin-left: 0;
							cursor: pointer;
							user-select: none;
							vertical-align: middle;
							padding-left: 0;
						}
						
						.textarea {
							margin-bottom: 20rpx;
							display: flex;
							flex-direction: row;
							overflow: hidden;
							font-size: 30rpx;
							min-height: 80rpx;
							
							.text {
								width: 100%;
								display: -webkit-box;
								-webkit-box-orient: vertical;
								text-overflow: ellipsis;
								-webkit-line-clamp: 2;
								overflow: hidden;
								
								text-align: justify;
								word-break: break-all;
								position: relative;
								line-height: 40rpx;

								&.active {
									-webkit-line-clamp: 999;
								}
								
								&::before {
									content: "";
									float: right;
									width: 0;
									height: 100%;
									background: #000;
									overflow: hidden;
									margin-bottom: -40rpx;
								}
								
								// &::after {
								// 	content: "";
								// 	position: absolute;
								// 	width: 100%;
								// 	height: 100px;
								// 	background: #fff;
								// 	background: $dj-gradient-primary;
								// }
				
								.btn {
									float: right;
									clear: both;
									background: $dj-gradient-primary;
									border: none;
									color: #fff;
									line-height: 40rpx;
								}
							}
						}
						
						.content {
							display: flex;
							flex-direction: row;
							align-items: center;
							margin-top: 2rpx;
							.text1 {
								margin-left: 0;
								font-size: 22rpx;
								color: #fff;
								background: linear-gradient(90deg, #232526 0%, #414345 100%);
								border-radius: 16rpx;
								padding: 4rpx 14rpx;
								line-height: 1.4;
								font-weight: 500;
								letter-spacing: 1rpx;
								display: inline-block;
								vertical-align: middle;
								box-shadow: 0 1px 6px rgba(0,0,0,0.08);
								margin-right: 10rpx;
							}
							.more-btn {
								text-decoration: none;
								margin-left: 10rpx;
								color: #fff;
								font-size: 22rpx;
								background: linear-gradient(90deg, #ff5b99 0%, #ff267d 100%);
								border-radius: 16rpx;
								padding: 4rpx 18rpx;
								font-weight: 500;
								box-shadow: 0 2px 8px rgba(255, 64, 129, 0.10);
								transition: background 0.2s, color 0.2s;
								cursor: pointer;
								user-select: none;
								display: inline-block;
								vertical-align: middle;
							}
							.more-btn:hover {
								background: linear-gradient(90deg, #ff267d 0%, #ff5b99 100%);
								color: #fff;
							}
						}
					}
					
					.progress {
						width: 100%;
						position: absolute;
						bottom: 10rpx;
						/* #ifdef H5 */
						bottom: 110rpx;
						/* #endif */
						left: 0;
						z-index: 1;
						
						uni-slider {
							margin: 0 36rpx;
						}
						
						slider {
							margin: 0 36rpx;
						}
						
						.bartext {
							display: flex;
							flex-direction: row;
							justify-content: center;
							font-size: 40rpx;
							margin-bottom: 60rpx;
							font-weight: 700;
							
							.text1 {
								
							}
							
							.text0 {
								padding: 0 8rpx;
							}
							
							.text2 {
								color: rgba(#fff, 0.5);
							}
						}
						
						.barview {
							@mixin whlt($w, $h, $l, $t) {
								width: $w !important;
								height: $h !important;
								margin-left: $l !important;
								margin-top: $t !important;
							}
							
							::v-deep .uni-slider-handle-wrapper .uni-slider-handle,
							::v-deep .wx-slider-handle-wrapper .wx-slider-handle {
								@include whlt(64rpx, 64rpx, -32rpx, -32rpx);
							}
							
							::v-deep .uni-slider-handle-wrapper,
							::v-deep .wx-slider-handle-wrapper {
								height: 4rpx;
								transition: all 0.3s;
							}
							
							::v-deep .uni-slider-handle-wrapper .uni-slider-thumb,
							::v-deep .wx-slider-handle-wrapper .wx-slider-thumb {
								@include whlt(12rpx, 12rpx, -6rpx, -6rpx);
							}
							
							&.active {
								::v-deep .uni-slider-handle-wrapper,
								::v-deep .wx-slider-handle-wrapper {
									height: 24rpx;
									transition: all 0.3s;
								}
								
								::v-deep .uni-slider-handle-wrapper .uni-slider-thumb,
								::v-deep .wx-slider-handle-wrapper .wx-slider-thumb {
									@include whlt(32rpx, 32rpx, -16rpx, -16rpx);
								}
							}
						}
					}
				}
			}
		}
	}
	.icon-svg {
		width: 44rpx;
		height: 44rpx;
		display: block;
		filter: drop-shadow(0 2px 8px rgba(0,0,0,0.10));
		transition: filter 0.2s;
	}
	.liked-glow {
		filter: drop-shadow(0 0 8px #fe2c55) drop-shadow(0 2px 8px rgba(0,0,0,0.10));
	}
	.collected-glow {
		filter: drop-shadow(0 0 8px #ffe066) drop-shadow(0 2px 8px rgba(0,0,0,0.10));
	}
	.liked-anim, .collected-anim {
		animation: tiktok-pop 0.35s cubic-bezier(0.23, 1.12, 0.32, 1);
	}
	@keyframes tiktok-pop {
		0% { transform: scale(1); }
		20% { transform: scale(0.8); }
		50% { transform: scale(1.18); }
		80% { transform: scale(0.95); }
		100% { transform: scale(1); }
	}
	.like-num, .collect-num {
		font-size: 28rpx;
		font-weight: 500;
		color: #f5f5f5;
		margin-top: 4rpx;
		letter-spacing: 1rpx;
		text-shadow: 0 2px 8px rgba(0,0,0,0.10);
		transition: color 0.2s;
	}
	.like-num.active {
		color: #fe2c55;
	}
	.collect-num.active {
		color: #ffe066;
	}
</style>
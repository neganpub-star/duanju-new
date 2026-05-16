<template>
	<view class="page_content">
		<!-- #ifndef MP-TOUTIAO -->
			<view class="head_content" v-if="!isDrag">
			<CustomNavbar :lText="$t('video.theater')" color="#fff"></CustomNavbar>
		</view>
		<!-- #endif -->
		
		<view class="vmask" v-if="isNeedToPay">
			<image class="bg" :src="videoInfo.cover" mode="aspectFill"></image>
			<view class="centerinfo">
				<view class="text">{{ $t('video.paidContent') }}</view>
				 <!-- #ifdef MP-WEIXIN -->
				<view class="btn" :style="'background:'+isColor" @click="beforePlay">{{ $t('video.unlockEpisode') }}</view>
				<!-- #endif -->
				<!-- #ifndef MP-WEIXIN -->
				<view class="btn" :style="'background:'+isColor" @click="beforePlay">{{ $t('video.unlockEpisode') }}</view>
				<!-- #endif -->
			</view>
			<view class="infobox">
				<view class="content">
					<view class="left">
						<text class="text1">{{ videoInfo.title }}</text>
						<text class="divider">|</text>
						<text class="text2">{{ videoData[videoIndex].display_title || videoData[videoIndex].name }}</text>
					</view>
					<view class="right" :style="'color:#ffe066'" @click="isShowMenu = true">{{ $t('video.selectEpisode') }}</view>
				</view>
			</view>
		</view>
		<view class="vmask" v-if="isLastPage">
			<image class="bg" :src="videoInfo.cover" mode="aspectFill"></image>
			<view class="centerinfo">
				<view class="text">{{ $t('video.paidOrderUnlocked', [lockCount]) }}</view>
				<view class="btn" @click="changeOriginIndex(lockCount)">{{ $t('video.unlockNext') }}</view>
			</view>
			<view class="infobox">
				<view class="content">
					<view class="left">
						<text class="text1">{{ videoInfo.title }}</text>
						<text class="divider">|</text>
						<text class="text2">{{ videoData[videoIndex].display_title || videoData[videoIndex].name }}</text>
					</view>
					<view class="right" :style="'color:#ffe066'" @click="isShowMenu = true">{{ $t('video.selectEpisode') }}</view>
				</view>
			</view>
		</view>
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
			<swiper class="swiper" :circular="circular" :vertical="true" :duration="300" :current="current" @change="swiperChange">
				<block v-for="(item, index) in videoData" :key="index">
					<swiper-item class="swiper_item">
						<view class="videos" v-if="videoIndex == index" @click="videoClick">
							<!-- #ifdef H5 -->
							
							<video class="video" v-if="!isNeedToPay" :id="'vplayer' + index" :ref="'vplayer' + index"
								:loop="false" :controls="false" :autoplay="true" :object-fit="isLandscape ? 'contain' : 'cover'"
								:enable-progress-gesture="true" :show-center-play-btn="false" :src="item.url" :poster="item.image"
								@play="isPlaying = true, isPlayError = false" @pause="isPlaying = false" @timeupdate="videoTimeUpdate"
								@ended="videoEnded" @error="videoError" @loadedmetadata="VideoLoadedmetadata">
							</video>
							<!-- #endif -->
							<!-- #ifdef MP-WEIXIN -->
							
							<video  :unit-id="shipinAd" bindadplay="onAdplay" bindadload="onAdload" bindadclose="onAdclose" bindaderror="onAdError" class="video"  v-if="!isNeedToPay&&!item.adsTrue " :id="'vplayer' + index" :ref="'vplayer' + index"
								:loop="false" :controls="false" :autoplay="videoIndex == index" :object-fit="isLandscape ? 'contain' : 'cover'"
								:enable-progress-gesture="true" :show-center-play-btn="false" :src="item.url"
								@play="isPlaying = true, isLoading = false, isPlayError = false" @timeupdate="videoTimeUpdate"
								@ended="videoEnded" @error="videoError" @loadedmetadata="VideoLoadedmetadata">
							</video>
								<view  v-else class="video" style="background-color: #fff;position: relative;z-index: 2;padding: 40px 0;">
									
									<view v-if=" daoTime!=0" style="display: flex;flex-direction: column;align-items: center;" >
										<view style="font-size: 16px;font-weight: 700;color: #5E72F7;margin-bottom: 60rpx;">{{ $t('video.nextBetter') }}</view>
										<view style="font-size: 14px; color: #5E72F7;margin-bottom: 40rpx;">
											看个广告，休息片刻：{{daoTime}}
										</view>
										
										
									</view>
									<view v-else style="display: flex;flex-direction: column;align-items: center;" >
										<view style="font-size: 65rpx;font-weight: 700;color: #5E72F7;margin-bottom: 60rpx;padding-top: 36rpx;">{{ $t('video.continueWatch') }}</view>
										
									</view>
									<view class="dwCenter">
										<ad :unit-id="tcAd" ad-type="video" ad-theme="white" bindload="adLoad" binderror="adError" bindclose="adClose"></ad>
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
								<image class="image" :src="videoInfo.cover" mode="aspectFill"></image>
								<view class="content">
									<u-icon name="info-circle-fill" color="#fff" size="50"></u-icon>
									<text class="text">{{ $t('video.playError') }}</text>
								</view>
							</view>
						</view>
						<view class="vcover" v-else>
							<image class="image" :src="videoInfo.cover" mode="aspectFill"></image>
						</view>
						<!-- #ifdef MP-WEIXIN -->
						<view class="vmask loading" v-if="isLoading && !isNeedToPay && videoIndex == index">
							<image class="bg" :src="videoInfo.cover" mode="aspectFill"></image>
							<view class="centerinfo">
								<u-loading-icon mode="circle" :size="30"></u-loading-icon>
								<view class="text">{{ $t('video.loading') }}</view>
							</view>
						</view>
						<!-- #endif -->
						<view class="buttons" v-if="!isDrag && !isNeedToPay && !isPlayError && videoIndex == index">
							<u-icon v-if="!isPlaying" name="play-right-fill" color="rgba(255, 255, 255, 0.8)" :size="60"></u-icon>
						</view>
						<view class="sidebar" v-if="!isDrag && !isNeedToPay && !isPlayError && videoIndex == index">
							<view class="item" @click.stop="onCommentClick">
								<u-icon name="chat" size="40" color="#fff"></u-icon>
							</view>
							<view class="item" :class="{ 'liked-anim': likeAnim }" @click="handleLikes(item.id, index)">
								<image class="image" :src="`/static/img/likes_${item.is_like ? 1 : 0 }.png`" mode="widthFix"></image>
								<text class="text" :class="{ active: item.is_like }">{{ item.likes }}</text>
							</view>
							<view class="item" :class="{ 'collected-anim': collectAnim }" @click="handleCollect">
								<svg class="star-svg" width="45" height="45" viewBox="0 0 48 48" style="display:block;margin:0 auto;"
									:style="videoInfo.isCollect || collectAnim ? 'filter: drop-shadow(0 0 8px #ffe066);' : ''">
									<polygon points="24,7 29.1,18.2 41.5,19.8 32.5,28.6 35.2,41 24,34.2 12.8,41 15.5,28.6 6.5,19.8 18.9,18.2"
										:fill="videoInfo.isCollect || collectAnim ? '#ffe066' : '#fff'"
										:stroke="videoInfo.isCollect || collectAnim ? '#ffe066' : '#fff'"
										stroke-width="3" stroke-linejoin="round"/>
								</svg>
								<text class="text" :class="{ active: videoInfo.isCollect || collectAnim }">{{ videoInfo.collect }}</text>
							</view>
							<view class="item share-item">
								<button class="btn share-btn" @click="handleShare">
									<svg class="share-svg-icon" width="36" height="36" viewBox="0 0 44 44" fill="none" xmlns="http://www.w3.org/2000/svg">
										<circle cx="32" cy="12" r="4" fill="#fff"/>
										<circle cx="12" cy="22" r="4" fill="#fff"/>
										<circle cx="32" cy="32" r="4" fill="#fff"/>
										<path d="M15.7 20.6L28.3 13.4" stroke="#fff" stroke-width="2.5" stroke-linecap="round"/>
										<path d="M15.7 23.4L28.3 30.6" stroke="#fff" stroke-width="2.5" stroke-linecap="round"/>
									</svg>
								</button>
							</view>
						</view>
						<view class="progress" v-if="!isNeedToPay && duration > 0 && videoIndex == index">
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
					
				</block>
			</swiper>
			<view class="infobox" v-if="!isDrag && !isNeedToPay && videoData.length">
				<view class="content">
					<view class="left">
						<text class="text1">{{ videoInfo.title }}</text>
						<text class="divider">|</text>
						<text class="text2">{{ originData[originIndex].display_title || originData[originIndex].name }}</text>
					</view>
					<view class="right" :style="'color:#ffe066'" @click="isShowMenu = true">{{ $t('video.selectEpisode') }}</view>
				</view>
			</view>
			<VideoMenu 
				v-if="isShowMenu" :show="isShowMenu" :info="{ title: videoInfo.title, length: videoInfo.length }" :data="originData" :current="originIndex"
				@selected="changeOriginIndex" @close="isShowMenu = false" @showpay="isShowPay = true">
			</VideoMenu>
			<VideoPay v-if="isShowPay" :show="isShowPay" :price="currentPrice" :ad="isLoaded" @close="isShowPay = false" @showAd="adCheck"></VideoPay>
			<!-- #ifdef MP-WEIXIN -->
			<view class="ad_box" v-if="userInfo && config.uniad_switch == '1' && config.adpid">
			    <button class="button" v-if="showAd && isLoaded" @click="adCheck"></button>
			</view>
			<!-- #endif -->
		</view>
		<view class="footer_content">
			<u-safe-bottom></u-safe-bottom>
		</view>
		<u-notify ref="uNotify"></u-notify>
		<!-- <view class="tzts" v-if="tzt">
			<view class="tzt_box" >
				<image @click="this.tzt =false " class="box_img" src="../../static/images/close.png" mode=""></image>
				<view class="box_hezi">
					<view class="box_ad" v-if="isTzt">
						<ad-custom unit-id="adunit-0b636c153e797660" bindload="adLoad" binderror="adError" bindclose="adClose"></ad-custom>

					</view>
					<view style="margin-bottom: 20rpx;font-size: 28rpx;">
						免费原创内容生产不易
					</view>
					<view style="margin-bottom: 20rpx; font-size: 28rpx;">
						观看本集需先解锁
					</view>
					<view class="box_look" style="margin-bottom: 20rpx;" v-if="userInfo && config.uniad_switch == '1' && config.adpid" @click="adCheck">
						看广告解锁
					</view>
					<view @click="this.tzt =false , this.isShowPay = true" class="box_pay">
						付费解锁
					</view>
				</view>
			</view>
			
		</view> -->
		<!-- <ad-custom ref="adFullscreenVideo" unit-id="adunit-6dee867484d19ef1" bindload="adLoad" binderror="adError" bindclose="adClose"></ad-custom> -->
		<CommentPanel :show="showComment" :video-id="videoInfo.id" @close="showComment = false" />
		<!-- 分享海报弹窗 -->
		<view v-if="showSharePoster" class="share-poster-mask" @click.self="showSharePoster = false">
			<view class="share-poster-popup">
				<view class="share-poster-close" @click="showSharePoster = false">×</view>
				<view class="share-poster-header">
					<view class="share-poster-title">分享海报</view>
				</view>
				<view class="share-poster-content" style="justify-content:center;align-items:center;">
					<image v-if="posterImg" :src="posterImg" mode="widthFix" show-menu-by-longpress style="width: 320px; border-radius: 12px;" />
					<view v-else id="sharePosterHtml">
						<div class="custom-poster-box">
							<div class="custom-poster-topbar">◀◀ 老有戏剧场平台 ▶▶</div>
							<div class="custom-poster-main-bg">
								<div class="poster-img-border">
									<img :src="videoInfo.cover" class="custom-poster-main-cover" />
								</div>
							</div>
							<div class="custom-poster-bottom" style="height: 140px; align-items: center;">
								<div class="custom-poster-qrcode" style="width: 130px; height: 130px; display: flex; align-items: center; justify-content: center; background: #fff; border-radius: 12px; box-shadow: 0 2px 8px #eee; margin-right: 24px;">
									<tki-qrcode :val="qrcodeValue" :size="120" :show="true" :loadMake="true" :showLoading="false" />
								</div>
								<div class="custom-poster-info" style="flex: 1;">
									<div class="custom-poster-ep-row">
										<span class="custom-poster-ep-label">剧集</span>
										<span class="custom-poster-ep-value">{{ videoData[videoIndex].display_title || videoData[videoIndex].name }}</span>
									</div>
									<div class="custom-poster-desc">老有戏邀请你观看精选短剧<br/>体验老有戏观看享受院线级别服务<br/></div>
								</div>
							</div>
						</div>
					</view>
				</view>
				<view class="share-poster-actions">
					<button class="share-poster-btn" @click="saveSharePoster">保存到相册</button>
					<button class="share-poster-btn primary" @click="shareToFriend">分享给好友</button>
				</view>
				<!-- 新增弹窗提示 -->
				<view v-if="showShareTip" class="share-tip-modal">
					<view class="modal-content" style="background:transparent;box-shadow:none;padding:0;">
						<view class="share-poster-content" id="sharePosterHtmlTip" style="pointer-events:auto;">
							<image v-if="posterImg" :src="posterImg" mode="widthFix" show-menu-by-longpress style="width: 320px; border-radius: 12px;" />
						</view>
						<p style="color:#fff;font-size:16px;margin:16px 0;">长按图片保存到相册，再分享给好友</p>
						<button @click="showShareTip = false" style="background:#ff6699;color:#fff;border:none;border-radius:6px;padding:8px 32px;font-size:16px;">关闭</button>
					</view>
				</view>
			</view>
		</view>
		<!-- 隐藏的二维码生成组件 -->
		<tki-qrcode
		  ref="shareQrcode"
		  :val="qrcodeValue"
		  :size="120"
		  :show="false"
		  :loadMake="true"
		  :showLoading="false"
		  @result="onQrcodeResult"
		/>
		<!-- 调试用：直接显示二维码base64图片 -->
		<image v-if="shareQrcodeImg" :src="shareQrcodeImg" style="width:120px;height:120px;position:fixed;top:10px;left:10px;z-index:9999;" />
	</view>
</template>

<script>
	import VideoMenu from './components/VideoMenu.vue'
	import VideoPay from './components/VideoPay.vue'
	import CommentPanel from './components/CommentPanel.vue'
	import { mapState, mapGetters, mapMutations, mapActions } from "vuex"
	import QRCode from '@/components/tki-qrcode/qrcode.js'
	import tkiQrcode from '@/components/tki-qrcode/tki-qrcode.vue'
	import html2canvas from 'html2canvas'
	export default {
		components: { VideoMenu, VideoPay, CommentPanel, tkiQrcode },
		data() {
			return {
				isColor: `#9354FF`,
				
				current: 0,
				currentTime: 0, // 当前视频播放进度
				currentPrice: 0, // 当前视频积分
				prevTime: 0, // 上次视频进度
				
				duration: 0, // 当前视频总时长
				isDrag: false, // 拖动进度条状态
				dragStarTime: "00:00", // 拖拽开始时长
				dragEndTime: "00:00", // 拖拽结束时长
				
				isPlaying: false, // 播放状态
				isPlayError: false, // 播放错误
				
				clickNum: 0, // 点击次数
				clickTimer: null, // 点击定时器

				isShowMenu: false, // 显示菜单
				isShowPay: false, // 显示支付
				
				isNeedToPay: false, // 需要支付
				isLoading: true, // 加载中
				barHeight: uni.getSystemInfoSync().statusBarHeight,
				isLastPage: false, // 最后一页

				originData: [], // 源数据
				originIndex: 0, // 源数据索引
				oldIndex: 0, // 源数据上一次索引
				circular: true, // swiper首尾循环
				
				videoInfo: { id: '', title: '', cover: '', length: '', collect: 0, isCollect: 0, share: 0 },
				videoData: [], // 渲染数据
				videoIndex: 0, // 渲染数据索引

				isLandscape: false, // 是否横屏
				
				videoAd: null,
				isLoaded: false,
				//插屏id
				chapingId:"",
				//插屏调用
				chaAd:null,
				//判断插屏广告是否存在
				chapingAd:false,
				//视频野广告
				shipinAd:"",
				AdNumber:getApp().globalData.isADTc,
				//new激励广告弹窗
				// tzt:false,
				//判断用户是否开启广告
				isTzt:false,
				tcAd:null,
				//daojishi
				daoTime:3,
				//jishiqi
				jiTime:'',
				likeAnim: false,
				collectAnim: false,
				showComment: false,
				showSharePoster: false,
				shareQrcodeImg: '', // 二维码base64
				shareUrlForQrcode: '', // 二维码链接
				showShareTip: false,
				posterImg: '', // 合成图片base64
				posterReady: false, // 新增，二维码加载完才允许生成
				qrcodeValue: '', // 新增，邀请好友二维码内容
			}
		},
		computed: {
			...mapGetters("user", ["token", "userInfo", "showAd"]),
			...mapGetters("app", ["videoAutoplay", "config"]),
			lockCount() {
				const count = this.originData.findIndex(item => !item.url)
				return count
			},
			highlightedInput() {
				// 高亮@昵称部分，后续内容普通色
				if (this.input.startsWith('@')) {
					// 匹配@昵称（空格或结尾为止）
					const match = this.input.match(/^(@[\u4e00-\u9fa5_a-zA-Z0-9]+)(\s?)(.*)$/);
					if (match) {
						return `<span style='color:green;'>${match[1]}</span>${match[2]}<span style='color:#222;'>${match[3]}</span>`;
					}
				}
				return `<span style='color:#222;'>${this.input.replace(/</g, '&lt;').replace(/>/g, '&gt;')}</span>`;
			}
		},
		watch: {
			// 付费
			isNeedToPay(newValue, oldValue) {
				if(newValue) {
					// #ifdef MP-WEIXIN
					// if(this.isTzt){
					// 	this.tzt = true
						
					// }else{		
					// 	this.isShowPay = true
						
					// }
					this.isShowPay = true
					
					// #endif
					// #ifndef MP-WEIXIN
					this.isShowPay = true
					// #endif
				}
			},
		
			originIndex(newValue, oldValue) {
				// #ifdef MP-WEIXIN
				if(newValue == 0) {
					this.circular = false
				}
				if(newValue == 0 && this.videoIndex != 0) {
					this.originIndex = 0
					this.videoIndex = 0
					this.videoData = []
					this.videoData[0] = this.originData[0]
					this.videoData[1] = this.originData[1]
				}
				// #endif
			},
			token(newValue, oldValue) {
				// #ifdef MP-WEIXIN
				newValue && (this.config?.uniad_switch == '1') && this.config?.adpid && this.adCheck()
				// #endif
			},
			// AdNumber(newValue, oldValue) {
				
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
			videoIndex(newVal, oldVal) {
				this.posterImg = '';
			},
			showShareTip(newVal, oldVal) {
				if (!newVal) {
					this.posterImg = ''
					this.posterReady = false
				}
			},
		},
		async onLoad(option) {
			this.winWidth = uni.getSystemInfoSync().windowWidth + 'px'
			if(option.id) {
				this.videoInfo.id = option.id
				await this.getAD()
				
				await this.getVideoMenu()
				// #ifdef MP-WEIXIN
				await this.token && (this.config?.uniad_switch == '1') && this.config?.adpid && this.adCheck()
				// #endif
			}
		},
		onUnload() {
			
			
			this.saveVideoProgress()
		},
		onHide() {
			
			this.saveVideoProgresss()
		},
		onShareAppMessage(res) {
			// #ifdef MP-WEIXIN
			return {
				title: this.videoInfo.title,
				path: `/pages/video/play?scene=${this.shareData.spm}&id=${this.videoInfo.id}`,
				imageUrl: this.videoInfo.cover
			}
			// #endif
		},
		onShareTimeline(res) {
			// #ifdef MP-WEIXIN
			return {
				title: this.videoInfo.title,
				query: `scene=${this.shareData.spm}&id=${this.videoInfo.id}`,
				imageUrl: this.videoInfo.cover
			}
			// #endif
		},
		onShow() {
			// 每次进入首页都自动刷新
			if (this.token) {
				this.getUserInfo && this.getUserInfo()
				this.getCategoryList && this.getCategoryList()
				this.getVideoList && this.getVideoList(true)
			}
		},
		methods: {
			...mapActions("user", ["checkAdTask"]),
			async adCheck() {
				// #ifdef MP-WEIXIN
				
				await this.checkAdTask()
				if(this.showAd) {
					if(this.videoAd) this.adShow()
					else this.adInit()
				} else {
					this.isLoaded = false
				}
				// this.tzt =false
				// #endif
			},
			daoji(){
				
				let that = this
				 this.jiTime = setInterval(function() {
				    console.log('zou',that.daoTime)
				    
				    // 如果时间为 0，清除计时器并执行完成回调函数
				    if (that.daoTime<1) {
				      clearInterval(that.jiTime);
				    } else {
				      that.daoTime = that.daoTime - 1;
				    }
				  }, 1000);
				
			
			},
			//广告
			getAD(){
				// #ifdef MP-WEIXIN
				
				this.$request('common.wxguanggao', '',false).then(res => {
					if(res.code === 1) {
						
						
						if(res.data.list.gg_xuanji_id && res.data.list.gg_xuanji_switch == '1'){
							if(res.data.mgg == 1){
								
							}else{
								this.chapingId = res.data.list.gg_xuanji_id
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
						
						if(res.data.list.gg_dingdan_id && res.data.list.gg_dingdan_switch == '1'){
							if(res.data.mgg == 1){
								
							}else{
								this.shipinAd = res.data.list.gg_dingdan_id
							
							}
						
						}
						
					}
				})
				// #endif
			},
			adIn(){
				if(this.chaAd){
					var that =  this
					setTimeout(function(){
								that.chaAd.show().catch((err) => {
									  console.log('怎么样',err)
								}) 
					}, 100)
					  
					
				}else{
					if (wx.createInterstitialAd) {
					  this.chaAd = wx.createInterstitialAd({
					    adUnitId: this.chapingId
					  })
					  this.chaAd.onLoad(() => {})
					  this.chaAd.onError((err) => {
					    
					  })
					  this.chaAd.onClose(() => {
						  this.AdNumber ++
						  
					  })
					}
				}
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
								res.code == 1 && (this.$u.toast("奖励已发放"), this.checkAdTask())
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
			// 保存视频进度
			saveVideoProgress() {
				console.log('保存食品进度')
				const oba = {
					vid: this.videoInfo.id,
					episode_id: this.videoData[this.videoIndex].id,
					type: 'log',
					view_time: this.currentTime,
					addlog:1,
					platform: this.$utils.platforms()
				}
				this.$request('add.log', oba, false)
				
				if(!this.token) return
				
				if(!this.isNeedToPay) {
					
					const obj = {
						vid: this.videoInfo.id,
						episode_id: this.videoData[this.videoIndex].id,
						type: 'log',
						view_time: this.currentTime,
						addlog:1,
						platform: this.$utils.platforms()
					}
					this.$request('video.addRecord', obj, false)
				}
			},
			//分享后的进度保存 为了区分保存视频进度的记录
			saveVideoProgresss() {
				
				
				if(!this.token) return
				
				
				if(!this.isNeedToPay) {
					
					const obj = {
						vid: this.videoInfo.id,
						episode_id: this.videoData[this.videoIndex].id,
						type: 'log',
						view_time: this.currentTime,
					}
					this.$request('video.addRecord', obj, false)
				}
			},
			// 选集
			changeOriginIndex(index) {
				this.originIndex = index
				this.initSwiperData(this.originIndex)
				// H5: src 变化后需要重新 load 并 play
				// #ifdef H5
				this.$nextTick(() => {
					const video = this.getVideoCtx()
					if (video) {
						setTimeout(() => { video.play() }, 300)
					}
				})
				// #endif
			},
			// 获取节目单
			getVideoMenu() {
				var that = this
				let  status = uni.getSystemInfoSync().uniPlatform
				
				this.$request('video.menu', { id: this.videoInfo.id ,platform: status == 'mp-weixin'? 2 :1,}).then(res => {
					if(res.code === 1) {
						this.videoInfo.title = res.data.display_title || res.data.title
						this.videoInfo.cover = res.data.image
						this.videoInfo.length = res.data.episodes
						this.videoInfo.collect = res.data.favorites
						this.videoInfo.isCollect = res.data.is_favorite
						this.videoInfo.share = res.data.shares
						
						if(res.data.episodes_list && res.data.episodes_list.length) {
							this.prevTime = res.data.view_time
							
							const index = res.data.episode_id ? res.data.episodes_list.findIndex(item => item.id == res.data.episode_id) : 0
							this.originIndex = index != -1 ? index : 0
							this.originData = res.data.episodes_list.map(ep => ({
								...ep,
								image: ep.image || res.data.image
							}))


								// this.originData.forEach((item,index)=>{
								//   that.originData[index].push({adTrue: false});
							 //        return;
							 //    })
							 
							 if(this.isTzt){
								 for (var i = 0; i <  this.originData.length-1; i++) {
								 	if(this.originData[i].name && this.originData[i+1].name){
								 		this.originData.splice(i+1,0,{adsTrue: true});
								 	}
								 }
								 							
							 }
								
							 // this.videoData.splice(1,0,obj)
							that.initSwiperData(that.originIndex, 1)
							console.log('节目单',that.originData)
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
				
				// #ifdef MP-WEIXIN
			
				if(this.videoData[this.videoIndex].adsTrue){
					
					this.daoji()
				}else{
					 clearInterval(this.jiTime);
					this.daoTime = 3
					
				}
				// cons
				// if(this.videoData[this.videoIndex].adsTrue){
				// 	this.daoji()
				// }
				// let obj = {adTrue: true}
				//  this.videoData.splice(1,0,obj)
				// #endif
				
				  // this.videoData.splice(3,0,obj)
				   
				console.log(this.videoData,'这是数据')
				
				this.videoData
				if (this.oldIndex >= this.originData.length) {
					this.oldIndex = 0
				}
				if (this.oldIndex < 0) {
					this.oldIndex = this.originData.length - 1
				}
				
				// #ifdef MP-WEIXIN
				if(this.originIndex ==0 ){
					this.circular = false
				} else {
					this.circular = true
				}
				this.isLoading = true
				// #endif
				
				// 重置进度条状态
				this.duration = 0
				this.currentTime = 0
				this.isDrag = false
				this.isPlayError = false
				this.isLastPage = false
				// 本集价格
				this.currentPrice = Number(this.videoData[this.videoIndex].price)
				// 播放授权
				if(this.videoData[this.videoIndex].url) {
					// #ifdef H5
					if(this.$utils.platforms() === 'wxOfficialAccount' && uni.getSystemInfoSync().platform == 'ios') {
						WeixinJSBridge.invoke('getNetworkType', {}, (e) => {
						    this.videoPlay()
						})
					} else {
						if(this.videoAutoplay == 1) {
							const timer = setTimeout(() => {
								this.videoPlay()
								clearTimeout(timer)
							}, 500)
						} else {
							if(init != 1) {
								const timer = setTimeout(() => {
									this.videoPlay()
									clearTimeout(timer)
								}, 500)
							}
						}
					}
					// #endif
					this.isNeedToPay = false
					this.saveVideoProgress()
				} else {
					// #ifdef H5
					if(this.originIndex == this.originData.length - 1 && this.originIndex != this.originData.findIndex(item => !item.url)) {
						this.isLastPage = true
					} else {
						this.beforePlay(init)
					}
					// #endif
					// #ifndef H5
					if(this.videoData[this.videoIndex].adsTrue){
						this.isLoading = false
						// this.beforePlay(init)
					}else{
						this.beforePlay(init)
					}
					
					// #endif
				}
			},
			// swiper切换
			swiperChange(event) {
				
				const { current } = event.detail;
				
				// this.$refs.adFullscreenVideo.show();
				if( this.videoData[event.detail.current].adsTrue ){
					// const { current } = event.detail;
					const originDataLength = this.originData.length;
					if (this.videoIndex - current == 2 || this.videoIndex - current == -1) {
						this.originIndex = this.originIndex + 1 == originDataLength ? 0 : this.originIndex + 1;
						this.videoIndex = this.videoIndex + 1 == 3 ? 0 : this.videoIndex + 1;
						this.oldIndex = this.originIndex - 1
						this.initSwiperData(this.originIndex);
					} else if (this.videoIndex - current == -2 || this.videoIndex - current == 1) {
						this.originIndex = this.originIndex - 1 == -1 ? originDataLength - 1 : this.originIndex - 1;
						this.videoIndex = this.videoIndex - 1 == -1 ? 2 : this.videoIndex - 1;
						this.oldIndex = this.originIndex + 1
						this.initSwiperData(this.originIndex);
					}
				}else{
					// #ifdef MP-WEIXIN
					if(this.chapingAd = true){
							this.adIn()
					}
					// #endif
					
								
				
					// const { current } = event.detail;
					const originDataLength = this.originData.length;
					if (this.videoIndex - current == 2 || this.videoIndex - current == -1) {
						this.originIndex = this.originIndex + 1 == originDataLength ? 0 : this.originIndex + 1;
						this.videoIndex = this.videoIndex + 1 == 3 ? 0 : this.videoIndex + 1;
						this.oldIndex = this.originIndex - 1
						this.initSwiperData(this.originIndex);
					} else if (this.videoIndex - current == -2 || this.videoIndex - current == 1) {
						this.originIndex = this.originIndex - 1 == -1 ? originDataLength - 1 : this.originIndex - 1;
						this.videoIndex = this.videoIndex - 1 == -1 ? 2 : this.videoIndex - 1;
						this.oldIndex = this.originIndex + 1
						this.initSwiperData(this.originIndex);
					}
				}
				
			},
			// 播放之前
			beforePlay(init = 0) {
				
				const obj = {
					vid: this.videoInfo.id,
					episode_id: this.videoData[this.videoIndex].id,
					platform: this.$utils.platforms()
				}
				
				this.$request('video.play', obj,false).then(res => {
					if(res.code === 1) {
						this.videoData[this.videoIndex].url = res.data.url
						this.isNeedToPay = false
						this.isShowPay = false
						// #ifdef H5
						if(this.$utils.platforms() === 'wxOfficialAccount' && uni.getSystemInfoSync().platform == 'ios') {
							WeixinJSBridge.invoke('getNetworkType', {}, (e) => {
								this.videoPlay()
							})
						} else {
							if(this.videoAutoplay == 1) {
								const timer = setTimeout(() => {
									this.videoPlay()
									clearTimeout(timer)
								}, 500)
							} else {
								if(init != 1) {
									const timer = setTimeout(() => {
										this.videoPlay()
										clearTimeout(timer)
									}, 500)
								}
							}
						}
						// #endif
					} else {
						if(res.msg == '视频不存在，请刷新后重试！'){
							console.log(!this.isNeedToPay, this.isLoading,'看这里')
							this.isLoading = false
							
						}else{
							// #ifdef MP-WEIXIN
							console.log('zoulezheli')
								this.isNeedToPay = true
								// if(this.isTzt){
								// 	this.tzt = true
									
								// }else{		
								// 	this.isShowPay = true
									
								// }
								this.isShowPay = true
								// this.tzt = true
							
							// #endif
							// #ifndef MP-WEIXIN
								this.isNeedToPay = true
								this.isShowPay = true
							// #endif
							
						}
						
						
					}
				})
			},
			// 播放
			videoPlay() {
				const video = this.getVideoCtx()
				if(!video) return
				video.play()
				this.isPlaying = true
			},
			// 暂停
			videoPause() {
				const video = this.getVideoCtx()
				if(!video) return
				video.pause()
				this.isPlaying = false
			},
			// 播放结束
			videoEnded(e) {
				if(this.originIndex == this.originData.length - 1) {
					
				} else {
					// #ifdef H5
					if(this.$utils.platforms() === 'wxOfficialAccount' && uni.getSystemInfoSync().platform == 'ios') {
						if (this.videoIndex <2) {
							this.current = this.videoIndex + 1
						} else {
							this.current = 0
						}
					} else {
						if(this.videoAutoplay == 1) {
							if (this.videoIndex <2) {
								this.current = this.videoIndex + 1
							} else {
								this.current = 0
							}
						}
					}
					// #endif
					
					// #ifndef H5
					if (this.videoIndex <2) {
						this.current = this.videoIndex + 1
					} else {
						this.current = 0
					}
					// #endif
				}
			},
			// 播放错误
			videoError() {
				this.isPlayError = true
				this.isLoading = false
			},
			// 点击
			videoClick(e) {
				this.clickTimer && clearTimeout(this.clickTimer)
				this.clickNum++
				this.clickTimer = setTimeout(() => {
					if(this.clickNum >= 2) {
						
					} else {
						if(this.chapingAd){
							this.adIn()
						}
						if(this.isPlaying) {
							this.videoPause()
						} else {
							this.videoPlay()
						}
					}
					this.clickNum = 0
				}, 250)
			},
			// 元数据加载完毕
			VideoLoadedmetadata(e) {
				
				const { width, height } = e.detail
				this.isLandscape = width >= height ? true : false
			},
			// 获取video标签上下文
			getVideoCtx() {
				return uni.createVideoContext('vplayer'+ this.videoIndex, this)
			},
			// 点赞
			handleLikes(id, index) {
				const obj = {
					episode_id: id,
					type: 'like'
				}
				this.$request('video.likes', obj).then(res => {
					if(res.code === 1) {
						if(this.videoData[index].is_like == 0) {
							this.videoData[index].is_like = 1
							this.videoData[index].likes++
						} else {
							this.videoData[index].is_like = 0
							this.videoData[index].likes--
						}
					}
				})
				this.likeAnim = true;
				setTimeout(() => { this.likeAnim = false }, 350);
			},
			// 收藏
			handleCollect() {
				if(this.videoInfo.isCollect == 0) {
					const obj = {
						vid: this.videoInfo.id,
						type: 'favorite'
					}
					this.$request('video.addFavorite', obj).then(res => {
						if(res.code === 1) {
							this.videoInfo.isCollect = 1
							this.videoInfo.collect++
						}
					})
				} else {
					const obj = {
						ids: this.videoInfo.id,
						type: 'favorite'
					}
					this.$request('video.deleteRecord', obj).then(res => {
						if(res.code === 1) {
							this.videoInfo.isCollect = 0
							this.videoInfo.collect = Math.max(0, this.videoInfo.collect - 1)
						}
					})
				}
				this.collectAnim = true;
				setTimeout(() => { this.collectAnim = false }, 350);
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
				// 当前进度
				this.currentTime = this.prevTime ? this.prevTime : Math.trunc(currentTime)
				this.duration = Math.trunc(duration)
				// 上次进度
				if(this.prevTime) {
					const video = this.getVideoCtx()
					if(!video) return
					video.seek(this.currentTime)
					this.prevTime = 0
				}
				// 进度文本
				this.dragStarTime = this.$utils.formatTime(this.currentTime)
				this.dragEndTime = this.$utils.formatTime(this.duration)
			},
			formatTime(ts) {
				const date = new Date(ts * 1000)
				return date.toLocaleString()
			},
			handleShare() {
				if (!this.token) {
					uni.showModal({
						title: '系统提示',
						content: '本操作需要您进行登录验证',
						success: res => {
							if (res.confirm) {
								uni.navigateTo({ url: '/pages/login/login' });
							}
						}
					});
					return;
				}
				this.genQrcodeValue();
				// 只替换域名为正式域名，后面路径和参数不变
				const localUrl = `${window.location.pathname}${window.location.search}${window.location.hash}`
				this.shareUrlForQrcode = `https://www.lyxdjpt.com${localUrl}`
				this.showSharePoster = true
				this.posterReady = false // 打开弹窗时先设为未准备好
				this.posterImg = '' // 清空旧图片
			},
			onQrcodeResult(img) {
				console.log('二维码base64：', img)
				this.shareQrcodeImg = img
				this.$nextTick(() => {
					this.generateSharePoster()
				})
			},
			async generateSharePoster() {
				const ctx = uni.createCanvasContext('sharePosterCanvas', this)
				const canvasWidth = 600
				const canvasHeight = 800
				// 设置画布尺寸
				ctx.canvas.width = canvasWidth
				ctx.canvas.height = canvasHeight
				// 绘制背景
				ctx.setFillStyle('#ffffff')
				ctx.fillRect(0, 0, canvasWidth, canvasHeight)
				// 绘制剧集封面
				const coverWidth = 500
				const coverHeight = 600
				const coverX = (canvasWidth - coverWidth) / 2
				const coverY = 50
				ctx.setFillStyle('#f0f0f0')
				ctx.fillRect(coverX, coverY, coverWidth, coverHeight)
				try {
					const coverImage = await this.loadImage(this.videoInfo.cover)
					ctx.drawImage(coverImage, coverX, coverY, coverWidth, coverHeight)
				} catch (error) {
					console.log('封面图片加载失败，使用默认背景')
				}
				ctx.setFillStyle('#333333')
				ctx.setFontSize(32)
				ctx.setTextAlign('center')
				ctx.fillText(this.videoInfo.title, canvasWidth / 2, coverY + coverHeight + 60)
				ctx.setFillStyle('#666666')
				ctx.setFontSize(24)
				ctx.fillText(this.videoData[this.videoIndex].name, canvasWidth / 2, coverY + coverHeight + 100)
				// 生成二维码
				const qrCodeSize = 120
				const qrCodeX = coverX + coverWidth - qrCodeSize - 20
				const qrCodeY = coverY + coverHeight - qrCodeSize - 20
				ctx.setFillStyle('#ffffff')
				ctx.fillRect(qrCodeX - 10, qrCodeY - 10, qrCodeSize + 20, qrCodeSize + 20)
				// 用 base64 绘制二维码
				if (this.shareQrcodeImg) {
					uni.getImageInfo({
						src: this.shareQrcodeImg,
						success: (res) => {
							ctx.drawImage(res.path, qrCodeX, qrCodeY, qrCodeSize, qrCodeSize)
							ctx.setFillStyle('#000000')
							ctx.setFontSize(16)
							ctx.setTextAlign('center')
							ctx.fillText('扫码观看', qrCodeX + qrCodeSize / 2, qrCodeY + qrCodeSize + 40)
							ctx.draw()
						}
					})
				}
			},
			loadImage(src) {
				return new Promise((resolve, reject) => {
					const img = new Image()
					img.onload = () => resolve(img)
					img.onerror = reject
					img.src = src
				})
			},
			async saveSharePoster() {
				// 用html2canvas截图海报区域
				const dom = document.getElementById('sharePosterHtml')
				if (!dom) {
					this.$u.toast('海报区域未找到')
					return
				}
				html2canvas(dom, {
					useCORS: true,
					backgroundColor: '#fff',
					scale: 3 // 提高分辨率
				}).then(canvas => {
					const url = canvas.toDataURL('image/png')
					// 创建临时a标签下载
					const a = document.createElement('a')
					a.href = url
					a.download = 'share-poster.png'
					document.body.appendChild(a)
					a.click()
					document.body.removeChild(a)
					this.$u.toast('已生成图片，可在下载记录中查看')
				}).catch(() => {
					this.$u.toast('生成图片失败')
				})
			},
			shareToFriend() {
				// 只有二维码加载好才生成
				if (!this.posterReady) {
					this.$u.toast('请等待二维码加载完成')
					return
				}
				this.createPosterImage()
				this.showShareTip = true;
			},
			onCommentClick() {
				this.showComment = true;
			},
			// 新增生成合成图片方法
			createPosterImage() {
				if (!this.posterReady) return // 没加载完二维码不生成
				const dom = document.getElementById('sharePosterHtml')
				if (!dom) return
				html2canvas(dom, {
					useCORS: true,
					backgroundColor: '#fff',
					scale: 3
				}).then(canvas => {
					this.posterImg = canvas.toDataURL('image/png')
				})
			},
			onPosterQrcodeLoad() {
				this.posterReady = true
				// 不再自动生成图片，等用户点击"分享给好友"时再生成
			},
			// 生成邀请好友二维码内容，参考poster.vue
			genQrcodeValue() {
				if(this.userInfo && this.userInfo.id) {
					let spm = ''
					if(this.$utils.platforms() == 'H5') {
						spm = this.userInfo.id + '.1.0.1.2'
					} else if(this.$utils.platforms() == 'wxOfficialAccount') {
						spm = this.userInfo.id + '.1.0.2.2'
					}
					this.qrcodeValue = window.location.origin + window.location.pathname + `?${this.$SIGN}#/pages/home/index?scene=${spm}`
				}
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
	// .tzts{
	// 	width: 100%;
	// 	position: fixed;
	// 	left: 0;
	// 	right: 0;
	// 	top: 0;
	// 	bottom: 0;
	// 	background-color: rgba(0,0,0,0.5);
	// 	display:flex;
	// 	justify-content: center;
	// 	align-items: center;
	// 	z-index:1000;
	// 	.tzt_box{
	// 		width: 100%;
	// 		display: flex;
	// 		flex-direction: column;
	// 		align-items: center;
	// 		align-items: center;
			
	// 		.box_img{
	// 			width: 50rpx;
	// 			height: 50rpx;
	// 			margin-bottom: 40rpx;
	// 		}
	// 		.box_hezi{
	// 			background-color: #fff;
	// 			width: 80%;
	// 			padding: 20px 0;
	// 			display: flex;
	// 			flex-direction: column;
	// 			justify-content: center;
	// 			align-items: center;
	// 			border-radius: 20rpx;
	// 			.box_ad{
	// 				width: 80%;
	// 				height: 300rpx;
					
	// 				margin-bottom: 40rpx;
					
	// 			}
	// 			.box_look{
	// 				width: 300rpx;
	// 				height: 80rpx;
	// 				border-radius: 20rpx;
	// 				text-align: center;
	// 				color:#fff;
	// 				line-height:80rpx;
	// 				background-color: rgb(109, 173, 254);
	// 			}
	// 			.box_pay{
	// 				width: 300rpx;
	// 				height: 80rpx;
	// 				border-radius: 20rpx;
	// 				text-align: center;
	// 				color:#fff;
	// 				line-height:80rpx;
	// 				background-color: rgb(43, 162, 69);
	// 			}
	// 		}
	// 	}
		
		
	// }
	.page_content {
		background: #000;
		position: relative;
		
		.ad_box {
			position: absolute;
			bottom: 180rpx;
			right: 30rpx;
			z-index: 1;
			
			.button {
				width: 80rpx;
				height: 80rpx;
				border-radius: 50%;
				background: linear-gradient(90deg, #5E72F7 0%, #9354FF 100%);
				outline: none;
				background-image: url('https://img.nymaite.com/video_short/icons/jifen.png');
				background-size: cover;
			}
		}
		
		.head_content {
			width: 100%;
			position: absolute;
			top: 0;
			left: 0;
			z-index: 10;
		}
		
		.vmask {
			width: 100%;
			height: 100%;
			position: absolute;
			top: 0;
			left: 0;
			z-index: 2;
			color: #fff;
			background: #000;
			
			&.loading {
				border-radius: 16rpx;
				overflow: hidden;
				
				.centerinfo {
					.text {
						margin-top: 20rpx;
					}
				}
			}
			
			.bg {
				width: 100%;
				height: 100%;
				opacity: 0.3;
			}
			
			.infobox {
				position: absolute;
				bottom: 130rpx;
				left: 0;
				z-index: 1;
			}
			
			.centerinfo {
				width: 100%;
				position: absolute;
				top: 40%;
				left: 0;
				text-align: center;
				
				.text {
					font-size: 32rpx;
					margin-bottom: 30rpx;
					line-height: 48rpx;
				}
				
				.btn {
					width: 300rpx;
					height: 80rpx;
					line-height: 80rpx;
					border-radius: 80rpx;
					background: linear-gradient(90deg, #5E72F7 0%, #9354FF 100%);
					margin: 0 auto;
					
					&:active {
						background: rgba(94, 114, 247, 0.8);
					}
				}
			}
		}
		
		.infobox {
			width: 92%;
			margin: 0 auto;
			position: absolute;
			bottom: 30rpx;
			left: 0;
			right: 0;
			z-index: 1;
			font-size: 32rpx;
			padding: 18rpx 32rpx;
			background: rgba(30, 30, 30, 0.68); /* 深灰色半透明 */
			border-radius: 32rpx;
			display: flex;
			align-items: center;
			box-shadow: 0 4rpx 24rpx rgba(0,0,0,0.10);
			
			.content {
				display: flex;
				flex-direction: row;
				align-items: center;
				justify-content: space-between;
				width: 100%;

				.left {
					flex: 1 1 auto;
					display: flex;
					flex-direction: row;
					align-items: center;
					min-width: 0;
				}

				.right {
					flex: 0 0 auto;
					color: #ffe066;
					white-space: nowrap;
					margin-left: 20rpx;
					align-self: flex-end;

					text {
						color: #ffe066 !important;
					}
				}
			}
		}
		
		.main_content {
			position: relative;
			color: #fff;
			background: #000;
			// padding-top: 100rpx;

			.swiper {
				width: 100%;
				height: 100vh;
				background: #000;
				border-radius: 16rpx;
				overflow: hidden;
			}
			
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
						
						video {
							border-radius: 16rpx;
							overflow: hidden;
						}
					}
					
					.verror {
						width: 100%;
						height: 100%;
						background: #000;
						position: absolute;
						top: 0;
						left: 0;
						z-index: 1;
						
						.image {
							width: 100%;
							height: 100%;
							opacity: 0.3;
						}
						
						.content {
							width: 100%;
							position: absolute;
							top: 50%;
							left: 0;
							transform: translateY(-50%);
							display: flex;
							flex-direction: column;
							align-items: center;
							
							.text {
								font-size: 32rpx;
								color: #fff;
								margin-top: 40rpx;
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
					z-index: 1;
					transform: translate(-50%, -50%);
					pointer-events: none;
				}
				
				.sidebar {
					position: absolute;
					right: 30rpx;
					bottom: 200rpx;
					z-index: 9999 !important;
					
					.item {
						margin-bottom: 40rpx;
						text-align: center;
						filter: drop-shadow(0 2px 6px rgba(0,0,0,0.6));

						&:last-child {
							margin-bottom: 0;
						}

						.image {
							width: 44rpx;
							margin: 0 auto;
							opacity: 0.95;
						}

						.text {
							font-size: 28rpx;
							text-shadow: 0 1px 4px rgba(0,0,0,0.7);

							&.active {
								color: #5E72F7;
							}
						}

						.btn {
							display: block;
							background: transparent;
							color: #fff;
							box-sizing: border-box;
							font-size: 28rpx;
							line-height: 40rpx;
							padding: 0;
							border: none;

							&::after {
								display: none;
							}
						}
					}
				}
	
				.progress {
					width: 100%;
					position: absolute;
					bottom: 110rpx;
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
							font-size: 22rpx;
							color: #b0b0b0;
							margin-left: 18rpx;
							white-space: nowrap;
							line-height: 48rpx;
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
		
		.footer_content {
			background: transparent;
		}
	}
	.sidebar .item .image,
	.sidebar .item .star-svg {
	  transition: transform 0.25s cubic-bezier(0.23, 1.12, 0.32, 1);
	}
	.sidebar .item.liked-anim .image,
	.sidebar .item.collected-anim .star-svg {
	  animation: pop-anim 0.35s cubic-bezier(0.23, 1.12, 0.32, 1);
	}
	@keyframes pop-anim {
	  0% { transform: scale(1); }
	  20% { transform: scale(0.8); }
	  50% { transform: scale(1.18); }
	  80% { transform: scale(0.95); }
	  100% { transform: scale(1); }
	}
	.sidebar .item .text.active {
	  color: #ffe066;
	  font-weight: bold;
	  transition: color 0.2s;
	}
	.divider {
		margin: 0 18rpx;
		color: #b0b0b0;
		font-size: 26rpx;
	}
	.share-tip-modal {
	  position: fixed; left: 0; top: 0; right: 0; bottom: 0;
	  background: rgba(0,0,0,0.6); display: flex; align-items: center; justify-content: center; z-index: 99999;
	}
	.modal-content {
	  background: #fff; border-radius: 12px; padding: 24px 16px; text-align: center; max-width: 90vw;
	}
	.poster-img {
	  width: 220px; max-width: 80vw; border-radius: 8px; margin-bottom: 18px;
	}
</style>
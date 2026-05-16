


<template>
	<view class="VideoMenu" :class="{ active: show }" @click="$emit('close')">
		<view class="popup" :class="{ 'popup-anim': show, 'popup-leave': !show }" @click.stop="">
			<view class="p_head">
				<view class="left">
					<text class="text1">{{ info.title }}</text>
					<text class="text2">共{{ info.length }}集</text>
				</view>
				<view class="right close-btn" style="color:#fff" @click="$emit('close')">
					<svg width="36" height="36" viewBox="0 0 36 36" class="close-x">
						<circle cx="18" cy="18" r="16" fill="rgba(255,255,255,0.18)"/>
						<line x1="12" y1="12" x2="24" y2="24" stroke="#fff" stroke-width="3.5" stroke-linecap="round"/>
						<line x1="24" y1="12" x2="12" y2="24" stroke="#fff" stroke-width="3.5" stroke-linecap="round"/>
					</svg>
				</view>
			</view>
			<view class="p_content">
				<view class="item" v-if="item.name" :style="currentIndex == index?'border-color:'+isColor :''" :class="[{ active: (currentIndex === index) || (pendingIndex === index) }, { 'flip-anim': popIndex === index }]" v-for="(item, index) in data" :key="index" @click="itemClick(index)">
					<image class="cover" :src="item.image" mode="aspectFill"></image>
					<view class="info" v-if="item.url">{{ item.name }}</view>
					<view class="lock" v-else>
						<image class="icon" src="https://img.nymaite.com/video_short/icons/lock.png" mode="widthFix"></image>
						<text class="text">{{ item.name }}</text>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		name: "VideoMenu",
		props: {
			show: {
				type: Boolean,
				default: false
			},
			info: {
				type: Object,
				default: {}
			},
			data: {
				type: Array,
				default: []
			},
			current: {
				type: Number,
				default: 0
			}
		},
		data() {
			return {
				//插屏id
					chapingId:"",
					//插屏调用
					chaAd:null,
					//判断插屏广告是否存在
					chapingAd:false,
					
				
				currentIndex: this.current,
				isColor:`pink`,
				popIndex: null,
				pendingIndex: null,
			};
		},
		watch: {
			current(newValue, oldValue) {
				this.changeWatch(newValue, 1)
			}
		},
		created() {
			this.isColor = `#9354FF`,
				this.getAD()
		},
		methods: {
			adIn(){
				if(this.chaAd){
					console.log('走了')
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
					  this.chaAd.onClose(() => {})
					}
				}
			},
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
						
						
						
						
					}
				})
				// #endif
			},
			changeWatch(index, x) {
				if(this.chapingAd){
					this.adIn()
				}
				if(!this.data[index].url && index != this.data.findIndex(item => !item.url)) {
					if(x != 1) {
						this.$u.toast("请按剧集顺序点播！")
					} else {
						this.currentIndex = index
					}
				} else {
				
					this.currentIndex = index
					if(x != 1) {
						this.$emit('selected', index)
						this.$emit('close')
					}
				}
			},
			itemClick(index) {
				if (index === this.currentIndex) return;
				this.pendingIndex = index;
				this.popIndex = index;
				this.currentIndex = null;
				setTimeout(() => {
					this.popIndex = null;
					this.currentIndex = this.pendingIndex;
					this.pendingIndex = null;
					this.changeWatch(index);
				}, 450);
			}
		}
	}
</script>

<style lang="scss" scoped>
	.VideoMenu {
		width: 100%;
		height: 100%;
		position: absolute;
		bottom: 0;
		left: 0;
		z-index: 100;
		background: rgba(0,0,0,0.35);
		transition: background 0.35s cubic-bezier(0.23, 1.12, 0.32, 1);
		opacity: 1;
		pointer-events: auto;
		&.active {
			opacity: 1;
			pointer-events: auto;
		}
		&:not(.active) {
			opacity: 0;
			pointer-events: none;
		}
		
		.popup {
			width: 100%;
			height: 60%;
			background: #000;
			border-radius: 20rpx 20rpx 0 0;
			position: absolute;
			bottom: 0;
			left: 0;
			z-index: 1;
			color: #fff;
			padding: 0 40rpx;
			border-top: 2rpx solid #333;
			overflow: hidden;
			display: flex;
			flex-direction: column;
			opacity: 0;
			transform: translateY(80px) scale(0.95);
			transition: all 0.45s cubic-bezier(0.23, 1.12, 0.32, 1);
			
			.p_head {
				display: flex;
				flex-direction: row;
				justify-content: space-between;
				position: relative;
				padding: 30rpx 0;
				font-size: 32rpx;

				.left {
					flex: 1;
					display: flex;
					flex-direction: row;
					padding-top: 28rpx;
					
					.text1 {
						font-weight: 700;
						line-height: 48rpx;
					}
					
					.text2 {
						font-size: 24rpx;
						color: rgba(#fff, 0.5);
						margin: 0 40rpx;
						white-space: nowrap;
						line-height: 48rpx;
					}
				}
				
				.right {
					color: #5E72F7;
					white-space: nowrap;
					margin-left: 20rpx;
				}
			}
			
			.p_content {
				flex: 1;
				display: flex;
				flex-direction: row;
				flex-wrap: wrap;
				overflow-y: auto;
				padding: 20rpx 0;
				perspective: 2000px;
				
				.item {
					width: calc((100% - 60rpx) / 3);
					height: 252rpx;
					margin-right: 30rpx;
					margin-bottom: 40rpx;
					position: relative;
					background: rgba(#fff, 0.1);
					font-size: 28rpx;
					border-radius: 10rpx;
					overflow: hidden;
					border: none;
					
					&:nth-child(3n) {
						margin-right: 0;
					}
					
					&.active,
					&.flip-anim {
						border: none;
					}
					
					.cover {
						width: 100%;
						height: 100%;
					}
					
					.info {
						width: 100%;
						padding: 8rpx;
						text-align: center;
						position: absolute;
						bottom: 0;
						left: 0;
						background: rgba(#000, 0.5);
					}
					
					.lock {
						width: 100%;
						height: 100%;
						position: absolute;
						top: 0;
						left: 0;
						background: rgba(#000, 0.7);
						display: flex;
						flex-direction: column;
						align-items: center;
						justify-content: center;
						
						.icon {
							width: 60rpx;
							margin-bottom: 8rpx;
						}
					}
				}
			}
		}
		.popup-anim {
			opacity: 1 !important;
			transform: translateY(0) scale(1) !important;
		}
		.popup-leave {
			opacity: 0 !important;
			transform: translateY(80px) scale(0.95) !important;
		}
	}
	.VideoMenu {
		opacity: 0;
		transform: translateY(100px);
		transition: all 0.35s cubic-bezier(0.23, 1.12, 0.32, 1);
		&.active {
			opacity: 1;
			transform: translateY(0);
		}
	}
	.p_content .item {
		transition: transform 0.25s cubic-bezier(0.23, 1.12, 0.32, 1);
	}
	.p_content .item.flip-anim {
		animation: flipY-smooth 0.45s cubic-bezier(0.42, 0, 0.58, 1);
		backface-visibility: hidden;
		transform-style: preserve-3d;
		box-shadow: 0 6px 24px rgba(0,0,0,0.14);
	}
	@keyframes flipY-smooth {
		0% { transform: rotateY(0deg) scale(1); box-shadow: 0 6px 24px rgba(0,0,0,0.14);}
		10% { transform: rotateY(-60deg) scale(1.04); box-shadow: 0 12px 32px rgba(0,0,0,0.18);}
		25% { transform: rotateY(-180deg) scale(1.08); box-shadow: 0 8px 24px rgba(0,0,0,0.16);}
		40% { transform: rotateY(-300deg) scale(1.04); box-shadow: 0 0px 0px rgba(0,0,0,0.10);}
		55% { transform: rotateY(-420deg) scale(1.02); box-shadow: 0 8px 24px rgba(0,0,0,0.14);}
		70% { transform: rotateY(-540deg) scale(1.01); box-shadow: 0 12px 32px rgba(0,0,0,0.18);}
		85% { transform: rotateY(-660deg) scale(1.00); box-shadow: 0 6px 24px rgba(0,0,0,0.14);}
		100% { transform: rotateY(-720deg) scale(1); box-shadow: 0 6px 24px rgba(0,0,0,0.14);}
	}
	.p_content .item.active {
		border: 4rpx solid #00e676 !important;
		box-shadow: 0 0 0 0 #00e676;
		transition: border-color 0.2s, box-shadow 0.45s, opacity 0.45s;
	}
	.p_content .item.flip-anim {
		border: 6rpx solid #00e676 !important;
		box-shadow: 0 0 16rpx 4rpx #00e67655;
		opacity: 0.7;
		transition: border-width 0.45s, box-shadow 0.45s, opacity 0.45s;
	}
	.close-btn {
		cursor: pointer;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		width: 44px;
		height: 44px;
		box-shadow: 0 2px 8px rgba(0,0,0,0.10);
		transition: background 0.2s, box-shadow 0.2s;
		margin-left: 20rpx;
		&:active, &:hover {
			background: rgba(0,230,118,0.12);
			box-shadow: 0 4px 16px rgba(0,230,118,0.18);
		}
	}
	.close-x {
		display: block;
	}
</style>

<style>
.p_content {
  perspective: 1600px;
}
.item {
  transition: transform 0.3s;
}
.item.flip-anim {
  animation: flipY-unique 0.7s cubic-bezier(0.4, 0.2, 0.2, 1);
  backface-visibility: hidden;
  transform-style: preserve-3d;
  box-shadow: 0 8px 32px rgba(0,0,0,0.18);
}
@keyframes flipY-unique {
  0% { transform: rotateY(0deg) scale(1); box-shadow: 0 8px 32px rgba(0,0,0,0.18);}
  15% { transform: rotateY(-90deg) scale(1.08); box-shadow: 0 16px 40px rgba(0,0,0,0.22);}
  30% { transform: rotateY(-180deg) scale(1.12); box-shadow: 0 8px 32px rgba(0,0,0,0.18);}
  50% { transform: rotateY(-360deg) scale(0.98); box-shadow: 0 0px 0px rgba(0,0,0,0.10);}
  70% { transform: rotateY(-540deg) scale(1.04); box-shadow: 0 8px 32px rgba(0,0,0,0.18);}
  85% { transform: rotateY(-630deg) scale(1.02); box-shadow: 0 16px 40px rgba(0,0,0,0.22);}
  100% { transform: rotateY(-720deg) scale(1); box-shadow: 0 8px 32px rgba(0,0,0,0.18);}
}
</style>
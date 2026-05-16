<template>
	<view class="page_content">
		<view class="head_content">
			<CustomNavbar :left="0" title="福利"></CustomNavbar>
			
		</view>
		<view class="main_content">
			<view class="gold_box">
				<view class="top">
					<image class="top_logo" src="../../static/images/key.png" mode=""></image>
					<view class="top_title">积分余额</view>
					<view class="top_number">{{ userInfo.usable || 0 }}</view>
					<view class="top_btn" v-if="iosIsPay" @click="recharge">直接充值</view>
				</view>
				<view class="center">
					<view class="list">
						<view class="center_first">
							<view class="first_title">签到N天可得N积分</view>
							<view class="first_right" @click="more = !more">查看全部奖励 ></view>
						</view>
						<view class="list_box">
							<view class="box" v-for="(item, index) in more ? signinList : filterList" :key="index">
								<!-- 未签到 -->
								<view class="item item1" v-if="item.current == 'before' && item.is_sign == '0' && item.is_replenish == '0'">
									<view class="icon">
										<image  src="../../static/images/closed.png"></image>
											
									
										<view>{{`+${item.score}`}}</view>
									</view>
									<view class="text">第{{ item.day }}天</view>
								</view>
								<!-- 待补签 -->
								<view class="item item3" v-if="item.current == 'before' && item.is_sign == '0' && item.is_replenish == '1'" @click="buqian(`${item.date }`)">
									<view class="icon">
										<image  src="../../static/images/closed.png"></image>
			
										<view>{{`+${item.score}`}}</view>
									</view>
									<view class="text">第{{ item.day }}天</view>
								</view>
								<!-- 已签到 -->
								<view class="item item4" v-if="(item.current == 'before' && item.is_sign == '1') || (item.current == 'today' && item.is_sign == '1')">
									<view class="icon">
										<image  src="../../static/images/qian.png"></image>
											
									
										<view>{{`+${item.score}`}}</view>
									</view>
									<view class="text">第{{ item.day }}天</view>
								</view>
								<!-- 签到 -->
								<view class="item item2" v-if="item.current == 'today' && item.is_sign == '0'">
									<view class="icon">
										<image  src="../../static/images/key.png"></image>
											
									
										<view>{{`+${item.score}`}}</view>
									</view>
									<view class="text">第{{ item.day }}天</view>
								</view>
								<!-- 第x天 -->
								<view class="item item2" v-if="item.current == 'after'">
									<view class="icon">
										<image  src="../../static/images/key.png"></image>
											
									
										<view>{{`+${item.score}`}}</view>
									</view>
									<view class="text">第{{ item.day }}天</view>
								</view>
							</view>
						</view>
						<view class="center_posi">
							<view class="bottom" v-if="!today" @click="handleSignin">点击签到</view>
							<view class="bottom disabled" v-else>已签到</view>
						</view>
					</view>
					<!-- <view class="more" :class="{ active: more }" @click="more = !more">
						<text class="text">{{ more ? '不查看' : '查看更多' }}</text>
						<image class="image" src="/static/icons/arrow.png" mode="widthFix"></image>
					</view> -->
				</view>
				
				
				<view class="ad_one">
					
					<view class="one_center">
						<image class="one_img" src="../../static/images/key.png" mode=""></image>
						
						看广告免费领积分<text> 0/4</text>
					</view>
					<view class="one_btn" @click="likeAd">
						立即领取
					</view>
				</view>
			</view>
			<view class="chou">
				<view class="chou_title">
					<view class="">
						开心大转盘
					</view>
					<view>
						最高可得<text>888</text>积分
					</view>
				</view>
				 <view style="display: flex;justify-content: center;">
					 <l-dialer :prizeList="prizeList" :dialStyle="dialStyle"  @done="onDone" ref="dialer" >
						 
							 <image  style="width: 350rpx;height: 350rpx;" slot="pointer" src="../../static/images/zz.png"></image>
							 
						 
					</l-dialer>

				</view>
			</view>
		</view>
	
	</view>
</template>

<script>

	
	import { mapState, mapGetters, mapMutations, mapActions } from "vuex"
	export default {
		data() {
			return {
				more: false, // 更多
				signinList: [], // 签到日历
				filterList: [], // 7天
				today: false, // 签到
				//签到N天可得N积分
				qianDay:'',
				//激励视频
				videoAd:null,
				// 奖品列表，
				prizeList: [
					{
						id: 'coupon88',
						name: '8.8折',
						img: 'https://img11.360buyimg.com/pop/jfs/t1/175718/35/12595/5477/60b660c6Eb850717b/a1cfe750dcdb5b78.png',
					},
					{
						id: 'coupon900',
					   
						name: '900',
						img: 'https://img11.360buyimg.com/pop/jfs/t1/190845/9/6092/4489/60b65fe8Ebb8f8284/955da889f6d1c13e.png',
					},
					{
						id: 'coupon900',
					   
						name: '900',
						img: 'https://img11.360buyimg.com/pop/jfs/t1/190845/9/6092/4489/60b65fe8Ebb8f8284/955da889f6d1c13e.png',
					},
					
				   
				],
				dialStyle:"color:radial-gradient(pink, pink)",
				
			}
		},
		computed: {
			...mapGetters("user", ["userInfo"]),
			...mapGetters("app", ["iosIsPay"]),
			
		},
		onLoad() {
			// #ifdef MP-WEIXIN
				this.getAd()
			// #endif
			this.getSigninList()
			
			
		},
		methods: {
			 onDone(index) {
			            const prize = this.prizeList[index]
			            uni.showModal({
			                title: prize.id == 'thanks' ? '很遗憾': '恭喜您',
			                content: (prize.id !== 'thanks' ? `获得`:'') + prize.name
			            })
			        },
			        onClick() {
			            // 奖品的索引
			            this.$refs.dialer.run(0)
			        },
			//获取激励视频广告
			getAd(){
				if (wx.createRewardedVideoAd) {
				  this.videoAd = wx.createRewardedVideoAd({
				    adUnitId: 'adunit-8bd8a7b507df8b08'
				  })
				   this.videoAd.onLoad(() => {})
				   this.videoAd.onError((err) => {
				    console.error('激励视频光告加载失败', err)
				  })
				   this.videoAd.onClose((res) => {
					   console.log(res,'dddd')
					   if(res && res.isEnded) {
					   	// this.$request('task.finish', { type: 'uniad_success' }).then(res => {
					   	// 	res.code == 1 && (this.$u.toast("奖励已发放"), this.checkAdTask())
					   	// })
					   }
					
				   })
				}
			},
			//出发广告
			likeAd(){
				if ( this.videoAd) {
				   this.videoAd.show().catch(() => {
				    // 失败重试
				     this.videoAd.load()
				      .then(() =>  this.videoAd.show())
				      .catch(err => {
				        console.error('激励视频 广告显示失败', err)
				      })
				  })
				}
			},
	
	   
			//充值跳转
			recharge() {
			
				this.jumpView('/pages/user/integral/recharge')
			},
			// 补签
			buqian(init) {
				console.log('ddd')
				this.$request('signin.resign', { init}).then(res => {
					if(res.code === 1) {
						this.$u.toast(res.msg)
						this.getSigninList()
					}
				})
			},
			// 签到
			handleSignin() {
				this.$request('signin.insign').then(res => {
					if(res.code === 1) {
						this.$u.toast(res.msg)
						this.getSigninList()
					}
				})
			},
			// 数据过滤
			filterData(current, data) {
				// 计算目标日期相对于当前月份第一天的索引（因为数组索引从0开始）
				let indexInMonth = current - 1
				// 获取当前月份的天数
				let daysInMonth = data.length
				// 确保目标区域数组长度为7，计算开始和结束索引
				let startIndex = Math.max(indexInMonth - 3, 0);
				let endIndex = Math.min(indexInMonth + 4, daysInMonth);
				// 创建一个表示当前月份每天的数组（这里仅用于演示，实际应用可能不需要创建这个数组）
				let monthArray = data
				// 取出目标区域的元素，确保返回数组长度为7
				let selectedDays = [];
				if (endIndex - startIndex >= 7) {
				    selectedDays = monthArray.slice(startIndex, startIndex + 7);
				} else { // 当所选范围未达到7天时，从前或后补足至7天
				    let remainingLength = 7 - (endIndex - startIndex);
				    if (indexInMonth - 3 >= remainingLength) {
				        startIndex -= remainingLength;
				    } else {
				        endIndex += remainingLength;
				    }
				    selectedDays = monthArray.slice(startIndex, endIndex);
				}
				
				return selectedDays
			},
			// 获取签到列表
			getSigninList() {
				this.$request('signin.list').then(res => {
					if(res.code === 1) {
						this.prizeList = res.data.turntable
						this.signinList = res.data.days
						const obj = res.data.days.filter(item => item.current === 'today')[0]
						const array = this.filterData(Number(obj.day), res.data.days)
						this.filterList = array
						console.log(this.filterList)
						this.today = obj.is_sign
					}
				})
			}
		}
	}
</script>

<style lang="scss" scoped>
	.page_content {
		color: #fff;
		padding-bottom: 152rpx;
		overflow-y: auto;
		font-size: 24rpx;
		background: radial-gradient(pink, pink);
		.main_content {
			padding: 30rpx;
			
			.gold_box {
				// padding: 0 30rpx 30rpx 30rpx;
				// background: $theme-card;
				border-radius: 10rpx;
				margin-bottom: 55rpx;
				
				.top{
					display: flex;
					flex-direction: column;
					align-items: center;
					.top_logo{
						width: 120rpx;
						height: 120rpx;
						margin-bottom: 20rpx;
					}
					.top_title{
						font-size: 32rpx;
						color: #fff2d0;
						margin-bottom: 20rpx;
						
					}
					.top_number{
						font-size: 82rpx;
						color: #ffffff;
						margin-bottom: 20rpx;
					}
					.top_btn{
						width: 240rpx;
						height: 80rpx;
						background: radial-gradient(#fff2d0, #ffffff);
						border: 2rpx solid #fff1ce;
						border-radius: 42rpx;
						text-align: center;
						line-height: 80rpx;
						color: #ff5f3f;
						font-size: 36rpx;

					}
					
				}
				
				.center {
					// padding: 20rpx 0;
					padding-top: 50rpx;
					padding-bottom: 70rpx;
					.center_first{
						margin-bottom: 30rpx;
						width: 100%;
						display: flex;
						align-items: center;
						justify-content: space-between;
						.first_title{
							font-weight: 700;
							font-size: 32rpx;
							color: #ff5f3f;
						}
						.first_right{
							color: #ff5f3f;
							font-size: 28rpx;

						}
					}
					.list {
						padding: 30rpx 30rpx 60rpx 30rpx;
						display: flex;
						flex-wrap: wrap;
						background: radial-gradient(#fff2d0, #ffc29a);
						border-radius: 20rpx;
						position: relative;
						.center_posi{
							position: absolute;
							left: 223rpx;
							bottom: -40rpx;
							.bottom {
								width: 240rpx;
								height: 80rpx;
								line-height:80rpx;
								font-size: 36rpx;
								text-align: center;
								background: radial-gradient(#fff2d0, #ffffff);
								border-radius: 40rpx;
								user-select: none;
								color: #ff5f3f;
								// &:active {
								// 	opacity: 0.8;
								// }
								
								// &.disabled {
								// 	opacity: 0.5;
								// }
							}
						}
						.list_box{
							width: 100%;
							display: flex;
							flex-wrap: wrap;
							.box {
								width: calc(100% / 7);
								display: flex;
								justify-content: center;
							}
							
							.item {
								width: 100%;
								display: flex;
								flex-direction: column;
								align-items: center;
								margin-bottom: 30rpx;
								
								.icon {
									width: 64rpx;
									height: 64rpx;
									text-align: center;
									background: linear-gradient(180deg, #ffaa00, #ffc400);
									border: 4rpx solid #ffdd00;
									border-radius: 50%;
									display: flex;
									align-items: center;
									justify-content: center;
									
								}
								
								.text {
									margin-top: 20rpx;
									text-align: center;
								}
							}
							
							.item1 {
								.icon {
									width: 78rpx;
									height: 118rpx;
									background: radial-gradient(#ffb68f, #ffa880);
									border-radius: 10rpx;
									border: none;
									display: flex;
									flex-direction: column;
									align-items: center;
									image{
										width: 44rpx;
										height: 44rpx;
										margin-bottom: 10rpx;
									}
									view{
										color: #ffdbb6;
									}
								}
								
								.text {
									margin-top: 20rpx;
									color: #ff8869;
								}
							}
							
							.item3 {
								.icon {
									width: 78rpx;
									height: 118rpx;
									background: radial-gradient(#ffb68f, #ffa880);
									border-radius: 10rpx;
									border: none;
									display: flex;
									flex-direction: column;
									align-items: center;
									image{
										width: 44rpx;
										height: 44rpx;
										margin-bottom: 10rpx;
									}
									view{
										color: #ffdbb6;
									}
								}
								
								.text {
									margin-top: 20rpx;
									color: #ff8869;
								}
							}
							
							.item4 {
								.icon {
									width: 78rpx;
									height: 118rpx;
									background: radial-gradient(#fff2d0, #ffffff);
									border-radius: 5px;
									border: none;
									display: flex;
									flex-direction: column;
									align-items: center;
									image{
										width: 44rpx;
										height: 44rpx;
										margin-bottom: 10rpx;
									}
									view{
										color: #FF5532;
									}
								}
								
								.text {
									margin-top: 20rpx;
									color: #ffffff;
								}
							}
							
							.item2 {
								.icon {
									width: 78rpx;
									height: 118rpx;
									background: radial-gradient(#ff9969, #ff7f69);
									border-radius: 10rpx;
									border: none;
									display: flex;
									flex-direction: column;
									align-items: center;
									image{
										width: 44rpx;
										height: 44rpx;
										margin-bottom: 10rpx;
									}
									view{
										color: #fffe6d;
									}
								}
								.text {
									margin-top: 20rpx;
									color: #ff5532;
								}
							}
						}
						
					}
					
					.more {
						color: #9A9A9A;
						display: flex;
						align-items: center;
						justify-content: center;
						
						&.active {
							.image {
								transform: rotate(-90deg);
							}
						}
						
						.text {}
						
						.image {
							width: 44rpx;
							height: 44rpx;
							transform: rotate(90deg);
							transition: all 0.3s;
						}
					}
				}
				
				
				.ad_one{
					width:686rpx;
					height: 120rpx;
					background: radial-gradient(#ffba93, #ffa67e);
					border-radius: 20rpx;
					display: flex;
					align-items: center;
					justify-content: space-around;
					
					.one_center{
						display: flex;
						align-items: center;
						
						color: #ffffff;
						font-size: 32rpx;
						.one_img{
							width: 60rpx;
							height: 60rpx;
							margin-right: 15.24rpx;
						}
						text{
							margin-left: 30rpx;
						}
					}
					.one_btn{
						width: 172rpx;
						height: 60rpx;
						background: radial-gradient(#fff2d0, #ffffff);
						border-radius: 30rpx;
						text-align: center;
						line-height: 60rpx;
						font-size: 28rpx;
						color: #ff5f3f;
					}
				}
			}
			
			.chou{
				.chou_title{
					color: #ffcdb3;
					font-size: 32rpx;
					display: flex;
					justify-content: space-between;
					font-weight: 700;
					margin-bottom: 30rpx;
					padding: 0 30rpx;
				}
			}
		}
	}
</style>



<template>
	<view class="page_content">
		<!-- #ifndef MP-TOUTIAO -->
		<view class="head_content">
			<CustomNavbar :title="$t('withdraw.title')"></CustomNavbar>
		</view>
		<!-- #endif -->

		<view class="main_content">
			<!-- 提现表单 -->
			<view class="form_card">
				<!-- 提现类型选择 -->
				<view class="field_row" @click="typePicker = true">
					<view class="field_label">{{ $t('withdraw.withdrawTo') }}</view>
					<view class="field_value type_value">
						<text>{{ typeText }}</text>
						<text class="arrow">›</text>
					</view>
				</view>
				<u-picker :show="typePicker" :columns="types" keyName="label" :closeOnClickOverlay="true"
					:cancelText="$t('common.cancel')" :confirmText="$t('common.confirm')"
					@close="typePicker = false" @cancel="typePicker = false" @confirm="selectedType" />

				<!-- 真实姓名 -->
				<view class="field_row">
					<view class="field_label">{{ $t('withdraw.realName') }}</view>
					<input class="field_input" v-model="params.realName" :placeholder="$t('withdraw.namePlaceholder')" />
				</view>

				<!-- 账号（微信/支付宝） -->
				<view class="field_row" v-if="params.applyType !== 'bank'">
					<view class="field_label">{{ $t('withdraw.accountNo') }}</view>
					<input class="field_input" v-model="params.account" :placeholder="$t('withdraw.accountPlaceholder')" />
				</view>

				<!-- 银行卡专用字段 -->
				<template v-if="params.applyType === 'bank'">
					<view class="field_row">
						<view class="field_label">{{ $t('withdraw.bankName') }}</view>
						<input class="field_input" v-model="params.bankName" :placeholder="$t('withdraw.bankPlaceholder')" />
					</view>
					<view class="field_row">
						<view class="field_label">{{ $t('withdraw.accountNo') }}</view>
						<input class="field_input" v-model="params.account" :placeholder="$t('withdraw.accountPlaceholder')" />
					</view>
				</template>

				<!-- 提现金额 -->
				<view class="field_row amount_row">
					<view class="field_label">
						{{ $t('withdraw.amount') }}
						<text class="hint_text">（{{ $t('withdraw.minHint') }} {{ sumMin }}{{ $t('withdraw.yuan') }}）</text>
					</view>
				</view>
				<view class="amount_input_row">
					<text class="currency">¥</text>
					<input class="amount_input" type="digit" v-model="params.money" :placeholder="$t('withdraw.inputAmount')" />
				</view>

				<view class="submit_btn">
					<u-button :text="$t('withdraw.confirm')" :loading="buttonLoading" :customStyle="buttonStyle" @click="withdrawHandle" />
				</view>
			</view>

			<!-- 提现记录 -->
			<view class="records_card">
				<view class="records_title">{{ $t('withdraw.records') }}</view>
				<view class="record_list" v-if="recordData.length">
					<view class="record_item" v-for="(item, index) in recordData" :key="index">
						<view class="record_left">
							<view class="record_type_icon">
								<text>{{ typeIcon(item.apply_type) }}</text>
							</view>
							<view class="record_info">
								<view class="record_money">¥{{ item.money }}</view>
								<view class="record_meta">{{ item.apply_type_text }} · {{ timestampToTime(item.createtime) }}</view>
							</view>
						</view>
						<view class="record_status" :class="'status_' + item.status">
							{{ statusText(item.status) }}
						</view>
					</view>
				</view>
				<view class="empty_tip" v-else>{{ $t('withdraw.noRecord') }}</view>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				buttonStyle: {
					width: '100%',
					height: '100rpx',
					border: 'none',
					fontSize: '30rpx',
					color: '#fff',
					background: 'linear-gradient(90deg, #5E72F7 0%, #9354FF 100%)',
					borderRadius: '16rpx',
					margin: '0',
					fontWeight: 'bold'
				},
				buttonLoading: false,
				types: [[
					{ id: 1, type: 'wechat', label: '' },
					{ id: 2, type: 'alipay', label: '' },
					{ id: 3, type: 'bank', label: '' },
				]],
				typePicker: false,
				typeText: '',
				params: {
					applyType: 'wechat',
					money: '',
					realName: '',
					account: '',
					bankName: '',
					platform: 'h5',
				},
				recordData: [],
				page: 1,
				hasMore: true,
				sumMin: 100,
				sumMax: 5000,
			}
		},
		onLoad() {
			this.initTypes()
			this.recordList()
		},
		methods: {
			initTypes() {
				this.types[0][0].label = this.$t('withdraw.wechat')
				this.types[0][1].label = this.$t('withdraw.alipay')
				this.types[0][2].label = this.$t('withdraw.bank')
				this.typeText = this.$t('withdraw.wechat')
			},
			typeIcon(type) {
				return { wechat: '💚', alipay: '💙', bank: '🏦' }[type] || '💰'
			},
			statusText(status) {
				const map = {
					'-1': this.$t('withdraw.rejected'),
					0: this.$t('withdraw.pending'),
					1: this.$t('withdraw.processing'),
					2: this.$t('withdraw.completed'),
				}
				return map[String(status)] || status
			},
			timestampToTime(value) {
				const d = new Date(value * 1000)
				const pad = n => String(n).padStart(2, '0')
				return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
			},
			selectedType(e) {
				const item = e.value[0]
				this.params.applyType = item.type
				this.typeText = item.label
				this.typePicker = false
				this.params.account = ''
				this.params.bankName = ''
			},
			withdrawHandle() {
				const num = Number(this.params.money)
				if (!num) return this.$u.toast(this.$t('withdraw.inputAmount'))
				if (num < this.sumMin) return this.$u.toast(this.$t('withdraw.minAmountTip') + this.sumMin + this.$t('withdraw.yuan'))
				if (num > this.sumMax) return this.$u.toast(this.$t('withdraw.maxAmountTip') + this.sumMax + this.$t('withdraw.yuan'))
				if (!this.params.realName) return this.$u.toast(this.$t('withdraw.namePlaceholder'))
				if (!this.params.account) return this.$u.toast(this.$t('withdraw.accountPlaceholder'))

				const applyInfo = JSON.stringify({
					real_name: this.params.realName,
					account: this.params.account,
					bank_name: this.params.bankName,
				})
				this.buttonLoading = true
				this.$request('withdraw.apply', {
					applyType: this.params.applyType,
					money: num,
					applyInfo,
					platform: this.params.platform,
				}).then(res => {
					if (res.code === 1) {
						this.$u.toast(this.$t('common.success'))
						this.params.money = ''
						this.params.realName = ''
						this.params.account = ''
						this.params.bankName = ''
						this.recordData = []
						this.page = 1
						this.hasMore = true
						this.recordList()
					}
					this.buttonLoading = false
				}).catch(() => { this.buttonLoading = false })
			},
			recordList() {
				if (!this.hasMore) return
				this.$request('withdraw.record', { page: this.page }).then(res => {
					if (res.code === 1) {
						const rows = res.data?.data || []
						if (rows.length) {
							this.recordData = this.recordData.concat(rows)
							this.page++
						} else {
							this.hasMore = false
						}
					}
				})
			},
		}
	}
</script>

<style lang="scss" scoped>
	.page_content {
		background: #f5f6ff;
		min-height: 100vh;

		.main_content {
			padding: 24rpx 28rpx 60rpx;

			.form_card {
				background: #fff;
				border-radius: 24rpx;
				padding: 8rpx 32rpx 32rpx;
				box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.06);
				margin-bottom: 24rpx;

				.field_row {
					display: flex;
					align-items: center;
					justify-content: space-between;
					padding: 32rpx 0;
					border-bottom: 1rpx solid #f2f2f2;

					&.amount_row {
						border-bottom: none;
						padding-bottom: 8rpx;
					}

					.field_label {
						font-size: 28rpx;
						font-weight: 600;
						color: #222;
						flex-shrink: 0;

						.hint_text {
							font-size: 22rpx;
							color: #999;
							font-weight: 400;
						}
					}

					.field_value {
						font-size: 28rpx;
						color: #555;
					}

					.type_value {
						display: flex;
						align-items: center;
						gap: 8rpx;
						color: #5E72F7;
						font-weight: 600;

						.arrow {
							font-size: 36rpx;
							color: #aaa;
							font-weight: 400;
						}
					}

					.field_input {
						flex: 1;
						text-align: right;
						font-size: 28rpx;
						color: #222;
					}
				}

				.amount_input_row {
					display: flex;
					align-items: center;
					padding: 16rpx 0 32rpx;
					border-bottom: 1rpx solid #f2f2f2;
					margin-bottom: 32rpx;

					.currency {
						font-size: 36rpx;
						font-weight: 700;
						color: #5E72F7;
						margin-right: 12rpx;
					}

					.amount_input {
						flex: 1;
						font-size: 44rpx;
						font-weight: 700;
						color: #1a1a1a;
					}
				}
			}

			.records_card {
				background: #fff;
				border-radius: 24rpx;
				padding: 32rpx;
				box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.06);

				.records_title {
					font-size: 30rpx;
					font-weight: 700;
					color: #1a1a1a;
					margin-bottom: 24rpx;
					padding-left: 12rpx;
					border-left: 6rpx solid #5E72F7;
				}

				.record_item {
					display: flex;
					align-items: center;
					justify-content: space-between;
					padding: 24rpx 0;
					border-bottom: 1rpx solid #f5f5f5;

					&:last-child { border-bottom: none; }

					.record_left {
						display: flex;
						align-items: center;
						gap: 20rpx;

						.record_type_icon {
							width: 80rpx;
							height: 80rpx;
							border-radius: 20rpx;
							background: #f5f6ff;
							display: flex;
							align-items: center;
							justify-content: center;
							font-size: 36rpx;
						}

						.record_info {
							.record_money {
								font-size: 32rpx;
								font-weight: 700;
								color: #1a1a1a;
								margin-bottom: 6rpx;
							}
							.record_meta {
								font-size: 22rpx;
								color: #aaa;
							}
						}
					}

					.record_status {
						font-size: 24rpx;
						font-weight: 600;
						padding: 8rpx 20rpx;
						border-radius: 20rpx;

						&.status_0 { background: #fff8e6; color: #f0a500; }
						&.status_1 { background: #eef0ff; color: #5E72F7; }
						&.status_2 { background: #e8f5e9; color: #2e7d32; }
						&.status_-1 { background: #ffeaea; color: #e53935; }
					}
				}

				.empty_tip {
					text-align: center;
					font-size: 28rpx;
					color: #bbb;
					padding: 60rpx 0;
				}
			}
		}
	}
</style>

<template>
  <view class="lang-switcher" v-if="showSwitcher" @click="openLangPanel">
    <view class="lang-btn">
      <!-- SVG 地球图标（去 emoji 廉价感） -->
      <svg class="lang-icon" viewBox="0 0 24 24" fill="none">
        <circle cx="12" cy="12" r="9.5" stroke="currentColor" stroke-width="1.4"/>
        <ellipse cx="12" cy="12" rx="4.2" ry="9.5" stroke="currentColor" stroke-width="1.4"/>
        <line x1="2.5" y1="12" x2="21.5" y2="12" stroke="currentColor" stroke-width="1.4"/>
      </svg>
      <text class="lang-text">{{ currentLangLabel }}</text>
    </view>
  </view>
</template>

<script>
import { setLang, getCurrentLang } from '@/common/i18n/index.js'

export default {
  name: 'LangSwitcher',
  data() {
    return {
      currentLang: getCurrentLang(),
    }
  },
  computed: {
    showSwitcher() {
      return this.$store.state.app.showLangSwitcher !== false
    },
    langOptions() {
      const allLangs = {
        'zh-CN': this.$t('lang.zhCN'),
        'zh-TW': this.$t('lang.zhTW'),
        'en':    this.$t('lang.en'),
      }
      const supported = this.$store.state.app.supportedLangs || ['zh-CN', 'zh-TW', 'en']
      return supported
        .filter(code => allLangs[code])
        .map(code => ({ value: code, label: allLangs[code] }))
    },
    currentLangLabel() {
      const opt = this.langOptions.find(o => o.value === this.currentLang)
      return opt ? opt.label : 'CN'
    },
  },
  methods: {
    openLangPanel() {
      const itemList = this.langOptions.map(o => o.label)
      uni.showActionSheet({
        title: this.$t('lang.title'),
        itemList,
        success: (res) => {
          const selected = this.langOptions[res.tapIndex]
          if (selected) {
            this.selectLang(selected.value)
          }
        }
      })
    },
    selectLang(lang) {
      if (lang === this.currentLang) return
      setLang(lang)
      this.currentLang = lang
      this.$store.commit('app/setLang', lang)
      // #ifdef H5
      setTimeout(() => { window.location.reload() }, 200)
      // #endif
      // #ifndef H5
      // 小程序端直接更新，不需要刷新
      // #endif
    }
  }
}
</script>

<style lang="scss" scoped>
.lang-switcher {
  display: inline-flex;
  align-items: center;
}
.lang-btn {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 10rpx 20rpx;
  border-radius: 32rpx;
  background: rgba(255, 255, 255, 0.18);
  border: 1rpx solid rgba(255, 255, 255, 0.22);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  cursor: pointer;
  transition: background 0.2s;
}
.lang-btn:active {
  background: rgba(255, 255, 255, 0.28);
}
.lang-icon {
  width: 26rpx;
  height: 26rpx;
  color: #fff;
  display: inline-block;
}
.lang-text {
  font-size: 24rpx;
  color: #fff;
  font-weight: 600;
  letter-spacing: 0.5rpx;
}
</style>

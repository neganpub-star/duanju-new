<template>
  <view class="lang-switcher" v-if="showSwitcher" @click="openLangPanel">
    <view class="lang-btn">
      <text class="lang-icon">🌐</text>
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
      return this.$store.state.app.config?.system?.show_lang_switcher !== '0'
    },
    langOptions() {
      return [
        { value: 'zh-CN', label: this.$t('lang.zhCN') },
        { value: 'zh-TW', label: this.$t('lang.zhTW') },
        { value: 'en',    label: this.$t('lang.en') },
      ]
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
  display: flex;
  align-items: center;
  padding: 8rpx 16rpx;
  border-radius: 30rpx;
  background: rgba(255, 255, 255, 0.15);
  cursor: pointer;
}
.lang-icon {
  font-size: 28rpx;
}
.lang-text {
  font-size: 22rpx;
  color: #fff;
  margin-left: 6rpx;
}
</style>

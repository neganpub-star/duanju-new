import Vue from 'vue'
import VueI18n from 'vue-i18n'
import zhCN from '@/locale/zh-CN.js'
import zhTW from '@/locale/zh-TW.js'
import en from '@/locale/en.js'

Vue.use(VueI18n)

const LANG_KEY = 'app_lang'

function getInitialLang() {
  // 1. 本地存储
  const saved = uni.getStorageSync(LANG_KEY)
  if (saved) return saved
  // 2. 浏览器语言
  // #ifdef H5
  const bl = navigator.language || navigator.userLanguage || 'zh-CN'
  if (bl.startsWith('zh-TW') || bl.startsWith('zh-HK') || bl.startsWith('zh-MO')) return 'zh-TW'
  if (bl.startsWith('en')) return 'en'
  // #endif
  return 'zh-CN'
}

const i18n = new VueI18n({
  locale: getInitialLang(),
  fallbackLocale: 'zh-CN',
  silentTranslationWarn: true,
  messages: {
    'zh-CN': zhCN,
    'zh-TW': zhTW,
    'en': en,
  }
})

export function setLang(lang) {
  i18n.locale = lang
  uni.setStorageSync(LANG_KEY, lang)
  // #ifdef H5
  document.documentElement.lang = lang
  // #endif
}

export function getCurrentLang() {
  return i18n.locale
}

export default i18n


import request from 'common/request/index.js'
import utils from 'common/utils/index.js'

// 默认配置，避免页面访问 config.system.xxx 报错
const DEFAULT_CONFIG = {
	system: {
		name: '短剧平台',
		company: '/static/logo.png',
		copyright: '短剧平台',
		h5_theme: 'default',
		user_protocol: 1,
		privacy_protocol: 2,
		legal_notice: 3,
		contact_us: 4,
		about_us: 5,
		mobile_switch: 0,
		android_autoplay: 1,
	},
	wechat: { appid: '' },
	share: { title: '短剧平台', image: '', description: '精彩短剧，随时随地' },
	apple_pay: 0,
}

export default {
	namespaced: true,
	state: {
		config: DEFAULT_CONFIG,
		appid: "",
		title: "短剧平台",
		copyright: [],
		richtext: DEFAULT_CONFIG.system,
		options: "",
		share: DEFAULT_CONFIG.share,
		jwx: false,
		iosIsPay: true,
		updateInfo: {},
		videoAutoplay: 1,
		adCountdown: 120,
		blocks: [],
		lang: '',
	},
	getters: {
		config: state => state.config,
		appid: state => state.appid,
		title: state => state.title,
		copyright: state => state.copyright,
		richtext: state => state.richtext,
		options: state => state.options,
		share: state => state.share,
		jwx: state => state.jwx,
		iosIsPay: state => state.iosIsPay,
		updateInfo: state => state.updateInfo,
		videoAutoplay: state => state.videoAutoplay,
		adCountdown: state => state.adCountdown,
		blocks: state => state.blocks,
		lang: state => state.lang,
	},
	mutations: {
		setjfName(state, data) { },
		setConfig(state, data) { state.config = data },
		setAppid(state, data) { state.appid = data },
		setTitle(state, data) { state.title = data },
		setCopyright(state, data) { state.copyright = data },
		setRichtext(state, data) { state.richtext = data },
		setOptions(state, data) { state.options = data },
		setShare(state, data) { state.share = data },
		setJwx(state, data) { state.jwx = data },
		setIosIsPay(state, data) { state.iosIsPay = data },
		setUpdateInfo(state, data) { state.updateInfo = data },
		setVideoAutoplay(state, data) { state.videoAutoplay = data },
		setBlocks(state, data) { state.blocks = data },
		setLang(state, data) { state.lang = data },
		changeAdCountdown(state) {
			state.adCountdown = uni.getStorageSync("adCountdown") || 120
		}
	},
	actions: {
		// 获取配置信息（新后端只取 block 列表，其余用默认值）
		async getConfigInfo({ commit }) {
			try {
				const result = await request("common.init", {}, false)
				if (result.code === 1 && Array.isArray(result.data)) {
					commit("setBlocks", result.data)
				}
			} catch(e) {
				console.warn('getConfigInfo failed, using defaults', e)
			}
			// 始终使用默认配置，确保页面不崩溃
			commit("setConfig", DEFAULT_CONFIG)
			commit("setTitle", DEFAULT_CONFIG.system.name)
			commit("setCopyright", DEFAULT_CONFIG.system.copyright)
			commit("setRichtext", DEFAULT_CONFIG.system)
			commit("setShare", DEFAULT_CONFIG.share)

			// #ifdef H5
			try { document.title = DEFAULT_CONFIG.system.name } catch(e) {}
			// #endif

			// 初始化语言
			const { getCurrentLang } = await import('@/common/i18n/index.js')
			commit('setLang', getCurrentLang())

			return DEFAULT_CONFIG
		},
		// 微信分享配置（暂不实现，避免报错）
		async getWxShareConfigInfo() {
			console.log('getWxShareConfigInfo: 暂未对接')
		}
	}
}

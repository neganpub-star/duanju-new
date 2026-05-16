
import request from 'common/request/index.js'

export default {
	namespaced: true,
	state: {
		token: "",
		userInfo: "",
		usable: 0,
		uid: 0,
		showAd: false
	},
	getters: {
		token: state => state.token,
		userInfo: state => state.userInfo,
		usable: state => state.usable,
		uid: state => state.uid,
		showAd: state => state.showAd
	},
	mutations: {
		setToken(state, token) {
			state.token = token
		},
		setUserInfo(state, data) {
			state.userInfo = data
		},
		setUsable(state, data) {
			state.usable = data
		},
		setUid(state, data) {
			state.uid = data
		},
		setShowAd(state, data) {
			state.showAd = data
		}
	},
	actions: {
		// 广告任务（新后端无此功能，直接返回 false）
		async checkAdTask({ commit }) {
			commit("setShowAd", false)
			return false
		},
		// 获取用户信息
		async getUserInfo({ commit }, token = "") {
			if (token) commit("setToken", token)
			const result = await request("user.info")
			if (result.code === 1) {
				commit("setUserInfo", result.data)
				commit("setUsable", result.data.usable || 0)
				// 新后端用 id，旧后端用 user_id
				commit("setUid", result.data.id || result.data.user_id)
			}
			return result
		},
		// 退出登录
		async logout({ commit }) {
			commit("setToken", "")
			commit("setUserInfo", "")
			commit("setShowAd", false)
		}
	}
}

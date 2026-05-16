import apiList from './api.js';
import { BASE_URL, SIGN } from '@/env.js';
import store from '@/common/store/index.js'

const getApiPath = path => {
	let apiArray = path.split("."),
		api = apiList
	apiArray.forEach(v => {
		api = api[v]
	});
	return api
}

const request = (path, data = {}, error = true) => {
	if (!BASE_URL) {
		uni.showModal({ title: '未检测到域名地址，请联系管理员' })
		throw (`未检测到域名`)
	}
	const api = getApiPath(path)
	if (!api) {
		console.warn(`接口未定义: ${path}`)
		return Promise.resolve({ code: 0, data: null, msg: '接口未实现' })
	}
	const url = BASE_URL + api.url
	const method = api.method

	return new Promise((resolve, reject) => {
		uni.request({
			url,
			data,
			method,
			header: {
				'Content-Type': 'application/json',
				'Authorization': store.state.user.token || '',
				'Accept-Language': store.state.app.lang || uni.getStorageSync('app_lang') || 'zh-CN',
			},
			success: res => {
				const body = res.data
				if (res.statusCode === 200) {
					if (body.code === 200) {
						// 新后端 code:200 表示成功，转成 code:1 兼容现有页面
						resolve({ code: 1, data: body.data, msg: body.msg })
					} else if (body.code === 401) {
						store.dispatch('user/logout')
						uni.reLaunch({ url: '/pages/login/login' })
						reject(body)
					} else {
						error && uni.showToast({ title: body.msg || '请求失败', icon: 'none', duration: 2000 })
						resolve({ code: 0, data: null, msg: body.msg })
					}
				} else if (res.statusCode === 401) {
					store.dispatch('user/logout')
					error && uni.showModal({
						title: '系统提示',
						content: '登录已过期，请重新登录',
						success: r => {
							if (r.confirm) uni.navigateTo({ url: '/pages/login/login' })
						}
					})
					reject(body)
				} else {
					error && uni.showToast({ title: `网络异常(${res.statusCode})`, icon: 'none' })
					reject(body)
				}
			},
			fail: err => {
				uni.showToast({ title: '网络连接失败，请稍后重试', icon: 'none', duration: 2000 })
				reject(err)
			}
		})
	})
}

export default request

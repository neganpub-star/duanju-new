const api = {
	// 公用
	common: {
		init: {
			url: '/api/block/list',
			method: 'GET',
			desc: '首页 block 数据'
		},
		swiper: {
			url: '/api/block/list',
			method: 'GET',
			desc: '轮播图'
		},
		richtext: {
			url: '/api/common/richtext',
			method: 'GET',
			desc: '富文本协议内容'
		},
		i18nConfig: {
			url: '/api/common/i18n/config',
			method: 'GET',
			desc: '国际化配置（支持语种列表）'
		},
		wxguanggao: {
			url: '/api/noop',
			method: 'GET',
			desc: '广告配置（旧接口，已废弃）'
		},
		point: {
			url: '/api/noop',
			method: 'POST',
			desc: '积分埋点（旧接口，已废弃）'
		},
		pay: {
			url: '/api/noop',
			method: 'POST',
			desc: '支付（旧接口，已废弃）'
		},
		dypay: {
			url: '/api/noop',
			method: 'POST',
			desc: '抖音支付（旧接口，已废弃）'
		},
		xunipay: {
			url: '/api/noop',
			method: 'POST',
			desc: 'uni支付（旧接口，已废弃）'
		},
		ifxunipay: {
			url: '/api/noop',
			method: 'GET',
			desc: 'uni支付开关（旧接口，已废弃）'
		},
		wxmpQrcode: {
			url: '/api/noop',
			method: 'POST',
			desc: '小程序码（旧接口，已废弃）'
		},
	},
	// 登录
	login: {
		wxmpLogin: {
			url: '/api/auth/wechat-login',
			method: 'POST',
			desc: '微信小程序登录'
		},
		accountLogin: {
			url: '/api/auth/login',
			method: 'POST',
			desc: '账号密码登录'
		},
		codeLogin: {
			url: '/api/auth/sms-login',
			method: 'POST',
			desc: '验证码登录'
		},
		register: {
			url: '/api/auth/register',
			method: 'POST',
			desc: '注册'
		},
		sendCode: {
			url: '/api/auth/send-sms',
			method: 'POST',
			desc: '发送验证码'
		},
		getWxMobile: {
			url: '/api/auth/wechat-login',
			method: 'POST',
			desc: '微信手机号一键登录'
		},
		grBind: {
			url: '/api/auth/wechat-login',
			method: 'POST',
			desc: '微信授权后绑定手机号'
		},
		wxBind: {
			url: '/api/auth/wechat-login',
			method: 'POST',
			desc: '微信绑定手机号'
		},
		forgotPassword: {
			url: '/api/auth/sms-login',
			method: 'POST',
			desc: '找回密码（暂用验证码登录替代）'
		},
	},
	// 抖音登录
	douyin: {
		douyinlogin: {
			url: '/api/noop',
			method: 'POST',
			desc: '抖音登录（旧接口，已废弃）'
		},
	},
	// 用户
	user: {
		info: {
			url: '/api/user/info',
			method: 'GET',
			desc: '个人信息'
		},
		vip: {
			url: '/api/vip/list',
			method: 'GET',
			desc: '会员套餐'
		},
		updateInfo: {
			url: '/api/user/info',
			method: 'PUT',
			desc: '修改用户信息'
		},
		share: {
			url: '/api/noop',
			method: 'POST',
			desc: '微信分享配置（旧接口，已废弃）'
		},
		cdkey: {
			url: '/api/noop',
			method: 'POST',
			desc: '兑换码（旧接口，已废弃）'
		},
		delete: {
			url: '/api/noop',
			method: 'POST',
			desc: '注销账号（旧接口，已废弃）'
		},
	},
	// 播放量
	add: {
		log: {
			url: '/api/video/view',
			method: 'POST',
			desc: '播放量统计'
		}
	},
	// 视频
	video: {
		classify: {
			url: '/api/category/list',
			method: 'GET',
			desc: '分类列表'
		},
		list: {
			url: '/api/video/list',
			method: 'GET',
			desc: '视频列表'
		},
		menu: {
			url: '/api/video/detail',
			method: 'GET',
			desc: '视频详情'
		},
		play: {
			url: '/api/video/play',
			method: 'POST',
			desc: '获取分集播放URL'
		},
		recommend: {
			url: '/api/video/recommend',
			method: 'GET',
			desc: '推荐视频'
		},
		addRecord: {
			url: '/api/video/record',
			method: 'POST',
			desc: '保存观看进度'
		},
		addFavorite: {
			url: '/api/video/favorite',
			method: 'POST',
			desc: '添加追剧/收藏'
		},
		deleteRecord: {
			url: '/api/video/favorite/remove',
			method: 'POST',
			desc: '取消追剧/收藏'
		},
		getRecord: {
			url: '/api/video/history',
			method: 'GET',
			desc: '播放记录'
		},
		likes: {
			url: '/api/video/likes',
			method: 'POST',
			desc: '点赞/取消点赞'
		},
	},
	// 评论
	comment: {
		list: {
			url: '/api/comment/list',
			method: 'GET',
			desc: '评论列表'
		},
		replies: {
			url: '/api/comment/replies',
			method: 'GET',
			desc: '回复列表'
		},
		post: {
			url: '/api/comment/post',
			method: 'POST',
			desc: '发表评论'
		},
		like: {
			url: '/api/comment/like',
			method: 'POST',
			desc: '点赞评论'
		},
		delete: {
			url: '/api/comment/delete',
			method: 'POST',
			desc: '删除评论'
		},
	},
	// VIP订单
	order: {
		list: {
			url: '/api/vip/orders',
			method: 'GET',
			desc: '订单列表'
		},
		create: {
			url: '/api/vip/buy',
			method: 'POST',
			desc: '购买VIP'
		}
	},
	// 积分/点数套餐
	integral: {
		list: {
			url: '/api/usable/list',
			method: 'GET',
			desc: '点数套餐'
		},
		record: {
			url: '/api/wallet/logs',
			method: 'GET',
			desc: '钱包流水'
		},
		create: {
			url: '/api/usable/buy',
			method: 'POST',
			desc: '购买点数'
		}
	},
	// 分销
	dealer: {
		info: {
			url: '/api/user/info',
			method: 'GET',
			desc: '分销商信息'
		},
		level: {
			url: '/api/reseller/list',
			method: 'GET',
			desc: '分销套餐'
		},
		orderList: {
			url: '/api/reseller/orders',
			method: 'GET',
			desc: '分销订单'
		},
		createOrder: {
			url: '/api/reseller/buy',
			method: 'POST',
			desc: '购买分销套餐'
		},
		superior: {
			url: '/api/reseller/bind',
			method: 'POST',
			desc: '绑定上级'
		}
	},
	// 分享
	share: {
		record: {
			url: '/api/reseller/share-logs',
			method: 'GET',
			desc: '分享记录'
		},
		team: {
			url: '/api/reseller/team',
			method: 'GET',
			desc: '我的团队'
		},
		brokerage: {
			url: '/api/wallet/logs',
			method: 'GET',
			desc: '佣金流水'
		}
	},
	// 提现
	withdraw: {
		record: {
			url: '/api/wallet/withdraw-list',
			method: 'GET',
			desc: '提现记录'
		},
		apply: {
			url: '/api/wallet/withdraw',
			method: 'POST',
			desc: '提现申请'
		},
		account: {
			url: '/api/noop',
			method: 'GET',
			desc: '提现账户（旧接口，已废弃）'
		},
		addAccount: {
			url: '/api/noop',
			method: 'POST',
			desc: '添加提现账户（旧接口，已废弃）'
		},
		rule: {
			url: '/api/noop',
			method: 'GET',
			desc: '提现规则（旧接口，已废弃）'
		},
	},
	// 签到
	signin: {
		list: {
			url: '/api/noop',
			method: 'GET',
			desc: '签到列表（旧接口，已废弃）'
		},
		insign: {
			url: '/api/noop',
			method: 'POST',
			desc: '签到（旧接口，已废弃）'
		},
		resign: {
			url: '/api/noop',
			method: 'POST',
			desc: '补签（旧接口，已废弃）'
		},
	},
	// 任务
	task: {
		list: {
			url: '/api/noop',
			method: 'GET',
			desc: '任务列表（旧接口，已废弃）'
		},
		finish: {
			url: '/api/noop',
			method: 'POST',
			desc: '完成任务（旧接口，已废弃）'
		},
	},
	// 茅广告（旧）
	mgg: {
		level: {
			url: '/api/noop',
			method: 'GET',
			desc: '茅广告套餐（旧接口，已废弃）'
		},
		createOrder: {
			url: '/api/noop',
			method: 'POST',
			desc: '购买茅广告（旧接口，已废弃）'
		},
		mggswitch: {
			url: '/api/noop',
			method: 'GET',
			desc: '茅广告开关（旧接口，已废弃）'
		},
	},
}

export default api;

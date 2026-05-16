// #ifdef MP-WEIXIN
let BASE_URL = 'https://api.yourdomain.com', SIGN = ''
// #endif

// #ifdef H5
let BASE_URL = '', SIGN = ''
if (process.env.NODE_ENV === 'development') {
	BASE_URL = 'http://localhost:8080'
	SIGN = ''
} else {
	BASE_URL = window.location.origin
	SIGN = window.location.search.replace(/\?/g, "")
}
// #endif

// #ifdef APP-PLUS
let BASE_URL = 'https://api.yourdomain.com', SIGN = ''
// #endif

// #ifdef MP-TOUTIAO
let BASE_URL = 'https://api.yourdomain.com', SIGN = ''
// #endif

export {
	BASE_URL,
	SIGN
}

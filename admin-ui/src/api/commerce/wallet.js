import request from '@/utils/request'

export function listWithdraw(params) {
  return request({ url: '/admin/commerce/wallet/apply/list', method: 'get', params })
}
export function handleWithdraw(id, data) {
  return request({ url: `/admin/commerce/wallet/apply/${id}`, method: 'put', data })
}
export function listWalletLog(params) {
  return request({ url: '/admin/commerce/wallet/logs', method: 'get', params })
}

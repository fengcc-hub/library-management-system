import axios from 'axios'
import { Message } from 'element-ui'
import store from '@/store'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// Request interceptor: attach JWT token
request.interceptors.request.use(
  config => {
    const token = store.state.token
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => Promise.reject(error)
)

// Response interceptor: unified error handling
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      Message.error(res.message || '请求失败')
      if (res.code === 401) {
        store.dispatch('logout')
        router.push('/login')
      }
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res
  },
  error => {
    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        Message.error('登录已过期，请重新登录')
        store.dispatch('logout')
        router.push('/login')
      } else if (status === 403) {
        Message.error('无权限访问')
      } else if (status === 404) {
        Message.error('资源不存在')
      } else {
        Message.error(error.response.data?.message || '服务器错误')
      }
    } else {
      Message.error('网络连接异常')
    }
    return Promise.reject(error)
  }
)

export default request

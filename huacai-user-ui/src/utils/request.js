import axios from 'axios'
import { ElNotification, ElMessageBox, ElMessage, ElLoading } from 'element-plus'
import { getToken, removeToken } from '@/utils/auth'
import errorCode from '@/utils/errorCode'
import { tansParams, blobValidate } from '@/utils/huacai.js'
import cache from '@/plugins/cache'
import { saveAs } from 'file-saver'
import useUserStore from '@/store/modules/user'

let downloadLoadingInstance
export let isRelogin = { show: false }

const silentGuestPaths = ['/index/home', '/index/products', '/index/policies', '/index/productDetail']

function shouldHandle401Silently() {
  const currentPath = window.location.pathname || '/'
  return silentGuestPaths.some(path => currentPath === path || currentPath.startsWith(`${path}/`))
}

function clearLocalAuthState() {
  const userStore = useUserStore()
  userStore.token = ''
  userStore.id = ''
  userStore.name = ''
  userStore.avatar = ''
  userStore.roles = []
  userStore.permissions = []
  removeToken()
}

axios.defaults.headers['Content-Type'] = 'application/json;charset=utf-8'

const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API,
  timeout: 10000
})

service.interceptors.request.use(config => {
  const isToken = (config.headers || {}).isToken === false
  const isRepeatSubmit = (config.headers || {}).repeatSubmit === false

  if (getToken() && !isToken) {
    config.headers['Authorization'] = 'Bearer ' + getToken()
  }

  if (config.method === 'get' && config.params) {
    let url = config.url + '?' + tansParams(config.params)
    url = url.slice(0, -1)
    config.params = {}
    config.url = url
  }

  if (!isRepeatSubmit && (config.method === 'post' || config.method === 'put')) {
    const requestObj = {
      url: config.url,
      data: typeof config.data === 'object' ? JSON.stringify(config.data) : config.data,
      time: new Date().getTime()
    }
    const requestSize = Object.keys(JSON.stringify(requestObj)).length
    const limitSize = 5 * 1024 * 1024
    if (requestSize >= limitSize) {
      console.warn(`[${config.url}]: 请求数据大小超出允许的 5M 限制，无法进行防重复提交验证。`)
      return config
    }

    const sessionObj = cache.session.getJSON('sessionObj')
    if (!sessionObj) {
      cache.session.setJSON('sessionObj', requestObj)
    } else {
      const sUrl = sessionObj.url
      const sData = sessionObj.data
      const sTime = sessionObj.time
      const interval = 1000
      if (sData === requestObj.data && requestObj.time - sTime < interval && sUrl === requestObj.url) {
        const message = '数据正在处理，请勿重复提交'
        console.warn(`[${sUrl}]: ${message}`)
        return Promise.reject(new Error(message))
      }
      cache.session.setJSON('sessionObj', requestObj)
    }
  }

  return config
}, error => {
  console.log(error)
  return Promise.reject(error)
})

service.interceptors.response.use(res => {
  const code = res.data.code || 200
  const msg = errorCode[code] || res.data.msg || errorCode.default

  if (res.request.responseType === 'blob' || res.request.responseType === 'arraybuffer') {
    return res.data
  }

  if (code === 401) {
    if (shouldHandle401Silently()) {
      isRelogin.show = false
      clearLocalAuthState()
      return Promise.reject(new Error(msg))
    }

    if (!isRelogin.show) {
      isRelogin.show = true
      ElMessageBox.confirm(
        '登录状态已过期，您可以继续留在该页面，或者重新登录',
        '系统提示',
        {
          confirmButtonText: '重新登录',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(() => {
        isRelogin.show = false
        useUserStore().logOut().then(() => {
          location.href = '/index'
        })
      }).catch(() => {
        isRelogin.show = false
      })
    }
    return Promise.reject(new Error('无效的会话，或者会话已过期，请重新登录。'))
  }

  if (code === 500) {
    ElMessage({ message: msg, type: 'error' })
    return Promise.reject(new Error(msg))
  }

  if (code === 601) {
    ElMessage({ message: msg, type: 'warning' })
    return Promise.reject(new Error(msg))
  }

  if (code !== 200) {
    ElNotification.error({ title: msg })
    return Promise.reject(new Error(msg))
  }

  return Promise.resolve(res.data)
}, error => {
  console.log('err' + error)
  let { message } = error
  if (message === 'Network Error') {
    message = '后端接口连接异常'
  } else if (message.includes('timeout')) {
    message = '系统接口请求超时'
  } else if (message.includes('Request failed with status code')) {
    message = '系统接口' + message.substring(message.length - 3) + '异常'
  }
  ElMessage({ message, type: 'error', duration: 5 * 1000 })
  return Promise.reject(error)
})

export function download(url, params, filename, config) {
  downloadLoadingInstance = ElLoading.service({
    text: '正在下载数据，请稍候',
    background: 'rgba(0, 0, 0, 0.7)'
  })
  return service.post(url, params, {
    transformRequest: [(params) => tansParams(params)],
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    responseType: 'blob',
    ...config
  }).then(async (data) => {
    const isBlob = blobValidate(data)
    if (isBlob) {
      const blob = new Blob([data])
      saveAs(blob, filename)
    } else {
      const resText = await data.text()
      const rspObj = JSON.parse(resText)
      const errMsg = errorCode[rspObj.code] || rspObj.msg || errorCode.default
      ElMessage.error(errMsg)
    }
    downloadLoadingInstance.close()
  }).catch((error) => {
    console.error(error)
    ElMessage.error('下载文件出现错误，请联系管理员！')
    downloadLoadingInstance.close()
  })
}

export default service

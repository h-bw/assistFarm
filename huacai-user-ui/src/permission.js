import router from './router'
import { ElMessage } from 'element-plus'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken, removeToken } from '@/utils/auth'
import { isHttp, isPathMatch } from '@/utils/validate'
import { isRelogin } from '@/utils/request'
import useUserStore from '@/store/modules/user'
import useSettingsStore from '@/store/modules/settings'
import usePermissionStore from '@/store/modules/permission'

NProgress.configure({ showSpinner: false })

const whiteList = ['/login', '/register']

const isWhiteList = (path) => {
  return whiteList.some(pattern => isPathMatch(pattern, path))
}

const clearLocalAuthState = () => {
  const userStore = useUserStore()
  userStore.token = ''
  userStore.id = ''
  userStore.name = ''
  userStore.avatar = ''
  userStore.roles = []
  userStore.permissions = []
  removeToken()
}

router.beforeEach((to, from, next) => {
  NProgress.start()
  const requiresAuth = to.matched.some(record => record.meta?.requiresAuth)

  if (getToken()) {
    to.meta.title && useSettingsStore().setTitle(to.meta.title)
    if (to.path === '/login') {
      next({ path: '/' })
      NProgress.done()
      return
    }

    if (isWhiteList(to.path)) {
      next()
      return
    }

    if (useUserStore().roles.length === 0) {
      isRelogin.show = true
      useUserStore().getInfo().then(() => {
        isRelogin.show = false
        usePermissionStore().generateRoutes().then(accessRoutes => {
          accessRoutes.forEach(route => {
            if (!isHttp(route.path)) {
              router.addRoute(route)
            }
          })
          next({ ...to, replace: true })
        })
      }).catch(err => {
        clearLocalAuthState()
        if (requiresAuth) {
          ElMessage.error(err)
          next(`/login?redirect=${to.fullPath}`)
        } else {
          next({ path: '/' })
        }
      })
      return
    }

    next()
    return
  }

  if (isWhiteList(to.path) || !requiresAuth) {
    next()
    return
  }

  next(`/login?redirect=${to.fullPath}`)
  NProgress.done()
})

router.afterEach(() => {
  NProgress.done()
})

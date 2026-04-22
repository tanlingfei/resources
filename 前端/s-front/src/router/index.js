import {createRouter, createWebHistory} from 'vue-router'
import store from '@/store'  // 确保引入你的 Vuex store
import {get} from '@/utils/request'
import {ElMessage} from 'element-plus'

const routes = [
    {path: '/', component: () => import('@/views/IndexPage.vue')},
    {path: '/getTypes', component: () => import('@/views/GetTypes.vue')},
    {path: '/product/:id', component: () => import('@/views/ProductDetail.vue'), props: true},
    {path: '/wx', component: () => import('@/views/WechatQrCode.vue')},
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach((to, from, next) => { // 移除 async
    const isAuthenticated = localStorage.getItem('isLoggedIn')

    if (to.matched.some(record => record.meta.requiresAuth)) {
        if (!isAuthenticated) {
            return next('/login')
        }

        if (!store.state.user) {
            // 返回 Promise 确保路由等待
            return get('/admin/system/index/getInfo').then(res => {
                if (res.code === 200) {
                    store.dispatch('login', res.data)
                    next() // 明确调用
                } else {
                    ElMessage.error(res.message)
                    store.dispatch('logout')
                    next('/login')
                }
            }).catch(err => {
                console.error('获取数据失败:', err)
                next('/login')
            })
        }

        return next() // 已登录且有用户信息
    }

    return next() // 非认证路由
})


export default router

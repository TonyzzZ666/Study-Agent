import {createRouter, createWebHashHistory} from "vue-router";
import Layout from '@/layout/index.vue'
import Home from '@/views/Home.vue'

export const routerMap = [
    {
        path: '/login',
        name: 'login',
        component: () => import('@/login.vue')
    },
    {
        path: '/401',
        name: '401',
        component: () => import('@/views/error/401.vue')
    },
    {
        path: '/404',
        name: '404',
        component: () => import('@/views/error/404.vue')
    },
    {
        path: '/Layout',
        name: 'Layout',
        component: Layout,
        redirect: '/home',
        children: [
            { path: '/home', name: '首页', component: Home },
            { path: '/tasks', name: '任务管理', component: () => import('@/views/task/index.vue') },
            { path: '/checkin', name: '打卡记录', component: () => import('@/views/checkin/index.vue') },
            { path: '/statistics', name: '数据统计', component: () => import('@/views/statistics/index.vue') }
        ]
    }
]

const routers = createRouter({
    history: createWebHashHistory(),
    routes: routerMap
})

export default routers

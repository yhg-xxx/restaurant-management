import {createRouter, createWebHashHistory} from "vue-router";

const router = createRouter({
    history: createWebHashHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'login',
            component: () => import('@/components/Login.vue'),
        },
        {
            path: '/view',
            name: 'view',
            component: () => import('@/components/IndexView.vue'),
            children: [
                {
                    path: 'tables',
                    name: 'tables',
                    component: () => import('@/components/Tables.vue'),
                    meta: { title: '桌台管理' } // 添加 meta 信息
                },
                {
                    path: 'orders',
                    name: 'orders',
                    component: () => import('@/components/Orders.vue'),
                    meta: { title: '订单管理' }
                },
                {
                    path: 'dishes',
                    name: 'dishes',
                    component: () => import('@/components/Dishes.vue'),
                    meta: { title: '菜品管理' }
                },
                {
                    path: 'reports',
                    name: 'reports',
                    component: () => import('@/components/Reports.vue'),
                    meta: { title: '销售报表' }
                },
                {
                    path: 'profile',
                    name: 'profile',
                    component: () => import('@/components/Profile.vue'),
                    meta: { title: '个人信息' }
                },
                {
                    path: 'preparation',
                    name: 'preparation',
                    component: () => import('@/components/Preparation.vue'),
                    meta: { title: '菜品制作' }
                },
            ]
        },
    ]
})

export default router;
import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/components/layout/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页概览', icon: 'el-icon-s-home' }
      },
      {
        path: 'books',
        name: 'BookManage',
        component: () => import('@/views/BookManage.vue'),
        meta: { title: '图书管理', icon: 'el-icon-reading' }
      },
      {
        path: 'categories',
        name: 'CategoryManage',
        component: () => import('@/views/CategoryManage.vue'),
        meta: { title: '分类管理', icon: 'el-icon-menu' }
      },
      {
        path: 'readers',
        name: 'ReaderManage',
        component: () => import('@/views/ReaderManage.vue'),
        meta: { title: '读者管理', icon: 'el-icon-user', roles: ['ADMIN'] }
      },
      {
        path: 'borrow',
        name: 'BorrowManage',
        component: () => import('@/views/BorrowManage.vue'),
        meta: { title: '借阅管理', icon: 'el-icon-document', roles: ['ADMIN', 'LIBRARIAN'] }
      },
      {
        path: 'my-borrows',
        name: 'MyBorrows',
        component: () => import('@/views/MyBorrows.vue'),
        meta: { title: '我的借阅', icon: 'el-icon-notebook-2' }
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/Statistics.vue'),
        meta: { title: '数据统计', icon: 'el-icon-data-line' }
      }
    ]
  },
  {
    path: '*',
    redirect: '/dashboard'
  }
]

const router = new VueRouter({
  mode: 'hash',
  routes
})

export default router

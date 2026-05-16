import { createWebHistory, createRouter } from 'vue-router'
/* Layout */
import Layout from '@/layout'

/**
 * Note: 路由配置项
 *
 * hidden: true                     // 当设置 true 的时候该路由不会再侧边栏出现 如401，login等页面，或者如一些编辑页面/edit/1
 * alwaysShow: true                 // 当你一个路由下面的 children 声明的路由大于1个时，自动会变成嵌套的模式--如组件页面
 *                                  // 只有一个时，会将那个子路由当做根路由显示在侧边栏--如引导页面
 *                                  // 若你想不管路由下面的 children 声明的个数都显示你的根路由
 *                                  // 你可以设置 alwaysShow: true，这样它就会忽略之前定义的规则，一直显示根路由
 * redirect: noRedirect             // 当设置 noRedirect 的时候该路由在面包屑导航中不可被点击
 * name:'router-name'               // 设定路由的名字，一定要填写不然使用<keep-alive>时会出现各种问题
 * query: '{"id": 1, "name": "ry"}' // 访问路由的默认传递参数
 * roles: ['admin', 'common']       // 访问路由的角色权限
 * permissions: ['a:a:a', 'b:b:b']  // 访问路由的菜单权限
 * meta : {
    noCache: true                   // 如果设置为true，则不会被 <keep-alive> 缓存(默认 false)
    title: 'title'                  // 设置该路由在侧边栏和面包屑中展示的名字
    icon: 'svg-name'                // 设置该路由的图标，对应路径src/assets/icons/svg
    breadcrumb: false               // 如果设置为false，则不会在breadcrumb面包屑中显示
    activeMenu: '/system/user'      // 当路由设置了该属性，则会高亮相对应的侧边栏。
  }
 */

// 公共路由
export const constantRoutes = [
  {
    path: '/redirect',
    component: Layout,
    hidden: true,
    children: [
      {
        path: '/redirect/:path(.*)',
        component: () => import('@/views/redirect/index.vue')
      }
    ]
  },
  {
    path: '/login',
    component: () => import('@/views/login'),
    hidden: true
  },
  {
    path: '/register',
    component: () => import('@/views/register'),
    hidden: true
  },
  {
    path: "/:pathMatch(.*)*",
    component: () => import('@/views/error/404'),
    hidden: true
  },
  {
    path: '/401',
    component: () => import('@/views/error/401'),
    hidden: true
  },
  {
    path: '',
    component: Layout,
    redirect: '/index',
    children: [
      {
        path: '/index',
        component: () => import('@/views/index'),
        name: 'Index',
        meta: { title: '首页', icon: 'dashboard', affix: true }
      }
    ]
  },
  {
    path: '/lock',
    component: () => import('@/views/lock'),
    hidden: true,
    meta: { title: '锁定屏幕' }
  },
  {
    path: '/user',
    component: Layout,
    hidden: true,
    redirect: 'noredirect',
    children: [
      {
        path: 'profile/:activeTab?',
        component: () => import('@/views/system/user/profile/index'),
        name: 'Profile',
        meta: { title: '个人中心', icon: 'user' }
      }
    ]
  },

  // ===== 短剧业务菜单 =====
  {
    path: '/drama',
    component: Layout,
    redirect: '/drama/video',
    meta: { title: '短剧管理', icon: 'list' },
    children: [
      {
        path: 'video',
        component: () => import('@/views/drama/video/index'),
        name: 'DramaVideo',
        meta: { title: '视频管理', icon: 'list' }
      },
      {
        path: 'category',
        component: () => import('@/views/drama/category/index'),
        name: 'DramaCategory',
        meta: { title: '分类管理', icon: 'tree' }
      },
      {
        path: 'richtext',
        component: () => import('@/views/drama/richtext/index'),
        name: 'DramaRichText',
        meta: { title: '协议管理', icon: 'documentation' }
      }
    ]
  },
  {
    path: '/member',
    component: Layout,
    redirect: '/member/list',
    meta: { title: '用户管理', icon: 'peoples' },
    children: [
      {
        path: 'list',
        component: () => import('@/views/member/index'),
        name: 'MemberList',
        meta: { title: '用户列表', icon: 'user' }
      }
    ]
  },
  {
    path: '/commerce',
    component: Layout,
    redirect: '/commerce/vip',
    meta: { title: '商业管理', icon: 'money' },
    children: [
      {
        path: 'vip',
        component: () => import('@/views/commerce/vip/index'),
        name: 'CommerceVip',
        meta: { title: 'VIP套餐', icon: 'star' }
      },
      {
        path: 'vip-order',
        component: () => import('@/views/commerce/vip/order'),
        name: 'CommerceVipOrder',
        meta: { title: 'VIP订单', icon: 'documentation' }
      },
      {
        path: 'usable',
        component: () => import('@/views/commerce/usable/index'),
        name: 'CommerceUsable',
        meta: { title: '积分套餐', icon: 'form' }
      },
      {
        path: 'reseller',
        component: () => import('@/views/commerce/reseller/index'),
        name: 'CommerceReseller',
        meta: { title: '分销套餐', icon: 'form' }
      },
      {
        path: 'wallet-log',
        component: () => import('@/views/commerce/wallet/log'),
        name: 'WalletLog',
        meta: { title: '钱包流水', icon: 'chart' }
      },
      {
        path: 'withdraw',
        component: () => import('@/views/commerce/wallet/withdraw'),
        name: 'Withdraw',
        meta: { title: '提现审核', icon: 'form' }
      }
    ]
  },
  {
    path: '/system-config',
    component: Layout,
    redirect: '/system-config/storage',
    meta: { title: '系统设置', icon: 'system' },
    children: [
      {
        path: 'storage',
        component: () => import('@/views/system/config/index'),
        name: 'SystemConfig',
        meta: { title: '参数配置', icon: 'edit' }
      }
    ]
  }
]

// 动态路由，基于用户权限动态去加载
export const dynamicRoutes = [
  {
    path: '/system/user-auth',
    component: Layout,
    hidden: true,
    permissions: ['system:user:edit'],
    children: [
      {
        path: 'role/:userId(\\d+)',
        component: () => import('@/views/system/user/authRole'),
        name: 'AuthRole',
        meta: { title: '分配角色', activeMenu: '/system/user' }
      }
    ]
  },
  {
    path: '/system/role-auth',
    component: Layout,
    hidden: true,
    permissions: ['system:role:edit'],
    children: [
      {
        path: 'user/:roleId(\\d+)',
        component: () => import('@/views/system/role/authUser'),
        name: 'AuthUser',
        meta: { title: '分配用户', activeMenu: '/system/role' }
      }
    ]
  },
  {
    path: '/system/dict-data',
    component: Layout,
    hidden: true,
    permissions: ['system:dict:list'],
    children: [
      {
        path: 'index/:dictId(\\d+)',
        component: () => import('@/views/system/dict/data'),
        name: 'Data',
        meta: { title: '字典数据', activeMenu: '/system/dict' }
      }
    ]
  },
  {
    path: '/monitor/job-log',
    component: Layout,
    hidden: true,
    permissions: ['monitor:job:list'],
    children: [
      {
        path: 'index/:jobId(\\d+)',
        component: () => import('@/views/monitor/job/log'),
        name: 'JobLog',
        meta: { title: '调度日志', activeMenu: '/monitor/job' }
      }
    ]
  },
  {
    path: '/tool/gen-edit',
    component: Layout,
    hidden: true,
    permissions: ['tool:gen:edit'],
    children: [
      {
        path: 'index/:tableId(\\d+)',
        component: () => import('@/views/tool/gen/editTable'),
        name: 'GenEdit',
        meta: { title: '修改生成配置', activeMenu: '/tool/gen' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }
    return { top: 0 }
  },
})

export default router

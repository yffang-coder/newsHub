import { createRouter, createWebHistory } from 'vue-router';
import Home from '@/views/Home.vue';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: Home,
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('@/views/About.vue'),
    },
    {
      path: '/article/:id',
      name: 'article',
      component: () => import('@/views/Article.vue'),
    },
    {
      path: '/category/:id',
      name: 'category',
      component: () => import('@/views/Category.vue'),
    },
    {
      path: '/latest',
      name: 'latest-news',
      component: () => import('@/views/LatestNews.vue'),
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/auth/Login.vue'),
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/auth/Register.vue'),
    },
    {
      path: '/favorites',
      name: 'favorites',
      component: () => import('@/views/user/Favorites.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/admin',
      component: () => import('@/views/admin/AdminLayout.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
      children: [
        {
          path: '',
          redirect: '/admin/dashboard'
        },
        {
          path: 'dashboard',
          name: 'admin-dashboard',
          component: () => import('@/views/admin/Dashboard.vue')
        },
        {
          path: 'articles',
          name: 'admin-articles',
          component: () => import('@/views/admin/Articles.vue')
        },
        {
          path: 'comments',
          name: 'admin-comments',
          component: () => import('@/views/admin/Comments.vue')
        },
        {
          path: 'users',
          name: 'admin-users',
          component: () => import('@/views/admin/Users.vue')
        },
        {
          path: 'settings',
          name: 'admin-settings',
          component: () => import('@/views/admin/Settings.vue')
        },
        {
          path: 'notifications',
          name: 'admin-notifications',
          component: () => import('@/views/admin/Notifications.vue')
        }
      ]
    }
  ],
  scrollBehavior(_to, _from, savedPosition) {
    if (savedPosition) {
      return savedPosition;
    } else {
      return { top: 0 };
    }
  },
});

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token');
  const user = localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')!) : null;

  if (to.meta.requiresAuth && !token) {
    next('/login');
  } else if (to.meta.requiresAdmin && user?.role !== 'admin') {
    next('/'); // Or unauthorized page
  } else {
    next();
  }
});

export default router;

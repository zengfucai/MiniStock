import {
    createRouter,
    createWebHistory
} from 'vue-router'

import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import UserHome from '../views/UserHome.vue'
import AdminHome from '../views/AdminHome.vue'

const routes = [

    {
        path: '/',
        redirect: '/login'
    },

    {
        path: '/login',
        component: Login
    },

    {
        path: '/register',
        component: Register
    },

    {
        path: '/user',
        component: UserHome
    },

    {
        path: '/admin',
        component: AdminHome
    }

]

export default createRouter({

    history: createWebHistory(),

    routes

})
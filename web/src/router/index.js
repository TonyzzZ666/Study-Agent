import router from "./routers.js";
import {useStore} from "@/store/index.js";

const whiteList = ['/login', '/401', '/404']

router.beforeEach((to, from, next) => {
    const store = useStore()
    if (store.token) {
        if (to.path === '/login') {
            next({path: '/home'})
        } else {
            next()
        }
    } else {
        if (whiteList.includes(to.path)) {
            next()
        } else {
            next({path: '/login'})
        }
    }
})

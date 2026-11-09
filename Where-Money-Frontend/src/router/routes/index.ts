export const sidebarRoutes: AuthRoute.Route[] = [
    {
        name: "assistant",
        path: "/assistant",
        component: "self",
        meta: { title: "AI 记账助手", icon: "ri:robot-line", requiresAuth: true, singleLayout: "basic" }
    },
    {
        name: "home",
        path: "/home",
        component: "self",
        meta: {
            title: "主页",
            icon: "ic:baseline-home",
            requiresAuth: true,
            singleLayout: "basic"
        }
    },
    {
        name: "bill",
        path: "/bill",
        component: "self",
        meta: {
            title: "账单明细",
            icon: "ic:baseline-list",
            requiresAuth: true,
            singleLayout: "basic"
        }
    },
    {
        name: "asset",
        path: "/asset",
        component: "self",
        meta: {
            title: "资产",
            icon: "material-symbols:account-balance-wallet",
            requiresAuth: true,
            singleLayout: "basic"
        }
    },
    {
        name: "budget",
        path: "/budget",
        component: "self",
        meta: {
            title: "预算",
            icon: "ri:function-fill",
            requiresAuth: true,
            singleLayout: "basic"
        }
    },
    {
        name: "about",
        path: "/about",
        component: "self",
        meta: {
            title: "关于",
            icon: "ic:baseline-question-mark",
            requiresAuth: true,
            singleLayout: "basic"
        }
    }
];

/** 固定的路由 */
export const constantRoutes: AuthRoute.Route[] = [
    {
        name: "root",
        path: "/",
        redirect: "/home",
        meta: {
            title: "主页"
        }
    },
    {
        name: "login",
        path: "/login",
        component: "self",
        meta: {
            title: "登录"
        }
    },
    {
        name: "register",
        path: "/register",
        component: "self",
        meta: {
            title: "注册"
        }
    },
    {
        name: "no-permission",
        path: "/no-permission",
        component: "self",
        meta: {
            title: "无权限",
            singleLayout: "blank"
        }
    },
    {
        name: "not-found",
        path: "/not-found",
        component: "self",
        meta: {
            title: "未找到",
            singleLayout: "blank"
        }
    },
    {
        name: "service-error",
        path: "/service-error",
        component: "self",
        meta: {
            title: "服务器错误",
            singleLayout: "blank"
        }
    },
    // 匹配无效路径的路由
    {
        name: "not-found-page",
        path: "/:pathMatch(.*)*",
        component: "blank",
        meta: {
            title: "未找到!",
            singleLayout: "blank"
        }
    },
    ...sidebarRoutes
];

/** 路由名称 */
export const routeName = (key: AuthRoute.RouteKey) => key;

/** 路由路径 */
export function routePath(key: Exclude<AuthRoute.RouteKey, "not-found-page">): AuthRoute.RoutePath {
    const rootPath: AuthRoute.RoutePath = "/";
    if (key === "root") return rootPath;
    const splitMark: AuthRoute.RouteSplitMark = "_";
    const pathSplitMark = "/";
    const path = key.split(splitMark).join(pathSplitMark);
    return (pathSplitMark + path) as AuthRoute.RoutePath;
}

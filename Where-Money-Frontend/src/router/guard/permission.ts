import type {NavigationGuardNext, RouteLocationNormalized, Router} from "vue-router";
import {routeName} from "@/router";
import {exeStrategyActions, storage} from "@/utils";
import {useRouteStore} from "@/store";
import {EnumStorageKey} from "@/enum";

async function createDynamicRouteGuard(
    to: RouteLocationNormalized,
    _from: RouteLocationNormalized,
    next: NavigationGuardNext,
    router: Router
) {
    const route = useRouteStore();
    await route.initDynamicRoute(router);
    return true;
}

export async function createPermissionGuard(
    to: RouteLocationNormalized,
    from: RouteLocationNormalized,
    next: NavigationGuardNext,
    router: Router
) {
    const permission = await createDynamicRouteGuard(to, from, next, router);
    if (!permission) return;
    if (to.meta.href) {
        window.open(to.meta.href as any);
        next({path: from.fullPath, replace: true, query: from.query});
        return;
    }
    const token = storage.get(EnumStorageKey["token"]);
    const needLogin = Boolean(to.meta?.requiresAuth);
    const actions: Common.StrategyAction[] = [
        [
            (to.name === routeName("login") && token != null),
            () => {
                next({name: routeName("root")});
            }
        ],
        [
            !needLogin,
            () => {
                next();
            }
        ],
        [
            token == null && needLogin,
            () => {
                const redirect = to.fullPath;
                next({name: routeName("login"), query: {redirect}});
            }
        ],
        [
            token != null && needLogin,
            () => {
                next();
            }
        ]
    ];
    exeStrategyActions(actions);
}

import type {RouteLocationRaw} from "vue-router";
import {useRouter} from "vue-router";
import {router as globalRouter} from "@/router";

export function useRouterPush(inSetup: boolean = true) {
    const router = inSetup ? useRouter() : globalRouter;

    function routerPush(to: RouteLocationRaw, newTab = false) {
        if (newTab) {
            const routerData = router.resolve(to);
            window.open(routerData.href, "_blank");
        } else {
            router.push(to);
        }
    }

    function routerBack() {
        router.go(-1);
    }

    return {
        routerPush,
        routerBack
    };
}
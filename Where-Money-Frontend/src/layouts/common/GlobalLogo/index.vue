<template>
    <router-link :to="routeHomePath" class="flex-center w-full nowrap-hidden">
        <!-- <system-logo class="w-32px h-32px text-primary" /> -->
        <div
            v-show="!showTitle"
            class="text-xl font-bold text-primary transition duration-300 ease-in-out"
        >
            <span class="brand-mark">W</span>
        </div>
        <!-- {{ title }} -->
        <div
            v-show="showTitle"
            class="text-lg font-bold text-primary transition duration-300 ease-in-out flex items-center gap-2"
        >
            <span class="brand-mark">W</span><span>Where Money</span>
        </div>
    </router-link>
</template>

<script lang="ts" setup>
import {routePath} from "@/router";
import {useAppInfo} from "@/composables";

interface Props {
    /** 显示名字 */
    showTitle: boolean;
}

defineProps<Props>();

const {title} = useAppInfo();
const routeHomePath = routePath("root");
</script>
<style scoped>.brand-mark{display:inline-grid;place-items:center;width:30px;height:30px;border-radius:8px;background:#20624e;color:#e4f3df;font-size:18px;}</style>

<template>
    <div
        :class="{ 'p-16px': showPadding }"
        class="bg-[#f6f9f8] dark:bg-[#101014] transition duration-300 ease-in-out"
    >
        <router-view v-slot="{ Component, route }">
            <transition :name="theme.page.animate ? theme.page.animateMode : undefined" appear mode="out-in">
                <keep-alive :include="routeStore.cacheRoutes">
                    <component :is="Component" v-if="app.reloadFlag" :key="route.path"/>
                </keep-alive>
            </transition>
            <div class="but">
                <Icon class="text-primary w-16 h-16" icon="ic:baseline-add-circle" v-on:click="showAdd=true"/>
            </div>
            <n-modal v-model:show="showAdd" :mask-closable="false" class="w-19/20 lg:w-3/4 xl:w-1/2">
                <AddModal @close="closeAdd"></AddModal>
            </n-modal>
        </router-view>
    </div>
</template>

<script lang="ts" setup>
import {useAppStore, useRouteStore, useThemeStore} from "@/store";
import {ref, Ref} from "vue";
import {AddModal} from "./components";
import {Icon} from "@iconify/vue";

interface Props {
    /** 显示padding */
    showPadding?: boolean;
}

withDefaults(defineProps<Props>(), {
    showPadding: true
});

const app = useAppStore();
const theme = useThemeStore();
const routeStore = useRouteStore();
let showAdd: Ref<boolean> = ref(false);

function closeAdd(): void {
    showAdd.value = false;
}
</script>
<style scoped>
.but {
    position: fixed;
    bottom: 5%;
    right: 2%;
    cursor: pointer;
}
</style>

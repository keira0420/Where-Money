<template>
    <div
        :class="[isMini ? 'mb-4px px-2px' : 'mb-6px px-4px', 'cursor-pointer']"
        @mouseenter="setTrue"
        @mouseleave="setFalse"
    >
        <div
            :class="[
                isMini ? 'py-8px' : 'py-12px',
                'flex-center flex-col rounded-2px bg-transparent transition-colors duration-300 ease-in-out',
                { 'text-primary !bg-primary-active': isActive, 'text-primary': isHover }
            ]"
        >
            <component :is="icon" class="text-20px"/>
            <p
                :class="[isMini ? 'h-0 pt-0' : 'h-20px pt-8px']"
                class="pt-8px text-12px overflow-hidden transition-height duration-300 ease-in-out"
            >
                {{ label }}
            </p>
        </div>
    </div>
</template>

<script lang="ts" setup>
import type {VNodeChild} from "vue";
import {computed} from "vue";
import {useBoolean} from "@/hooks";

interface Props {
    /** 路由名称 */
    routeName: string;
    /** 路由名称文本 */
    label: string;
    /** 当前激活状态的理由名称 */
    activeRouteName: string;
    /** 路由图标 */
    icon?: () => VNodeChild;
    /** mini尺寸的路由 */
    isMini?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
    icon: undefined,
    isMini: false
});

const {bool: isHover, setTrue, setFalse} = useBoolean();

const isActive = computed(() => props.routeName === props.activeRouteName);
</script>
<style scoped></style>

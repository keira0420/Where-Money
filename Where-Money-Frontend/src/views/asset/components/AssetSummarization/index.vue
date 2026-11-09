<template>
    <n-card :bordered="false" class="rounded-16px shadow-sm h-250px">
        <template #header>
            <n-space align="center">
                <Icon height="24px" icon="carbon:wallet" width="24px"/>
                <span class="font-bold text-xl">资产总览</span>
            </n-space>
        </template>

        <template #default>
            <n-space :size="32" vertical>
                <!-- 第一行 计算净资产 -->
                <n-space justify="space-around">
                    <div>
                        <p class="text-xl text-center">净资产</p>
                        <!-- 小于0标红 -->
                        <p :class="net < 0 ? 'text-red-500' : ''" class="font-bold text-lg text-center">
                            {{ formattedCurrencyNoSymbol(net) }}
                        </p>
                    </div>
                </n-space>
                <!-- 第二行 总资产、负债 -->
                <n-space justify="space-around">
                    <div>
                        <p class="text-base text-center">总资产</p>
                        <p class="font-bold text-base text-center">{{ formattedCurrencyNoSymbol(total) }}</p>
                    </div>
                    <div>
                        <p class="text-base text-center">总负债</p>
                        <!-- 小于0标红 -->
                        <p :class="debt < 0 ? 'text-red-500' : ''" class="font-bold text-base text-center">
                            {{ formattedCurrencyNoSymbol(debt) }}
                        </p>
                    </div>
                </n-space>
            </n-space>
        </template>
    </n-card>
</template>

<script lang="ts" setup>
// vue
import {defineProps} from "vue";
// components
import {Icon} from "@iconify/vue";
// .ts
import {formattedCurrencyNoSymbol} from "@/utils";

const props = defineProps({
    total: {
        type: Number,
        required: true,
        default: 0
    },
    debt: {
        type: Number,
        required: true,
        default: 0,
        validator: (value: number) => {
            return value <= 0;
        }
    },
    net: {
        type: Number,
        required: true,
        default: 0
    }
});


</script>

<style lang="scss" scoped>
</style>

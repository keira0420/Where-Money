<template>
    <div v-bind:class="height[type]">
        <n-card class="rounded-xl" v-bind:class="height[type]">
            <template #default>
                <div class="flex" v-bind:class="height[type]">
                    <div class="w-1/4 my-auto">
                        <Icon class="text-3xl" v-bind:class="iconColor[type]" v-bind:icon="icon[type]"/>
                    </div>
                    <div class="w-3/4 my-auto">
                        <n-statistic>
                            <template #label>
                                {{ title[type] }}
                            </template>
                            <template #default>
                                ￥{{ amount > 10000 ? Math.round(amount / 100) / 100 + "万" : amount }}
                            </template>
                        </n-statistic>
                    </div>
                </div>
            </template>
        </n-card>
    </div>
</template>
<script lang="ts" setup>
import {Icon} from "@iconify/vue";
import type { PropType } from "vue";

defineProps({
    amount: {
        type: Number,
        default: 0
    },
    type: {
        type: String as PropType<"balance" | "pay" | "income">,
        default: "balance"
    }
});

interface switchType {
    balance: string;
    pay: string;
    income: string;
}

let icon: switchType = {
    income: "icon-park-outline:income-one",
    pay: "icon-park-outline:expenses-one",
    balance: "material-symbols:account-balance-wallet"
};
let iconColor: switchType = {
    income: "text-green-500",
    pay: "text-red-500",
    balance: "text-blue-500"
};
let title: switchType = {
    income: "月收入",
    pay: "月支出",
    balance: "月结余"
};
let height: switchType = {
    income: "",
    pay: "",
    balance: "h-full"
};
</script>
<style scoped>
</style>

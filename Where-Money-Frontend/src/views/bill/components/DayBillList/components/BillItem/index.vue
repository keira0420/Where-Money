<template>
    <div
        class="rounded-xl px-2 border-1 border-gray-200"
        v-bind:class="{ 'bg-primary-active': chosen, 'border-opacity-100': mouseOver, 'border-opacity-0': !mouseOver }"
        v-on:click="handleClick"
        v-on:mouseenter="handleMouseEnter(true)"
        v-on:mouseleave="handleMouseEnter(false)"
    >
        <div v-if="type === '支出'" class="flex justify-between h-18 cursor-pointer">
            <div class="flex space-x-2">
                <Icon class="text-red-500 h-5 w-5 my-auto" icon="material-symbols:arrow-upward"/>
                <div class="my-auto">
                    <div class="text-base">{{ bill.billCategory + (bill.refunded ? "（已退款）" : "") }}</div>
                    <div class="text-sm">{{ bill.remark }}</div>
                </div>
            </div>
            <div>
                <div class="text-red-500 text-base flex justify-end">-{{ bill.amount }}</div>
                <div class="text-sm flex justify-end">{{ bill.payAsset }}</div>
                <div class="text-sm flex justify-end">{{ bill.billTime }}</div>
            </div>
        </div>
        <div v-else-if="type === '收入'" class="flex justify-between h-18 cursor-pointer">
            <div class="flex space-x-2">
                <Icon class="text-green-500 h-5 w-5 my-auto" icon="material-symbols:arrow-downward"/>
                <div class="my-auto">
                    <div class="text-base">{{ bill.billCategory }}</div>
                    <div class="text-sm">{{ bill.remark }}</div>
                </div>
            </div>
            <div>
                <div class="text-green-500 text-base flex justify-end">+{{ bill.amount }}</div>
                <div class="text-sm flex justify-end">{{ bill.incomeAsset }}</div>
                <div class="text-sm flex justify-end">{{ bill.billTime }}</div>
            </div>
        </div>
        <div v-else-if="type === '转账'" class="flex justify-between h-18 cursor-pointer">
            <div class="flex space-x-2">
                <Icon class="text-blue-500 h-5 w-5 my-auto" icon="ph:arrows-clockwise"/>
                <div class="my-auto">
                    <div class="text-base">{{ "转账" }}</div>
                    <div class="text-sm">{{ bill.remark }}</div>
                </div>
            </div>
            <div>
                <div class="text-blue-500 text-base flex justify-end">{{ bill.amount }}</div>
                <div class="text-sm flex justify-end">{{ bill.outAsset + "→" + bill.inAsset }}</div>
                <div class="text-sm flex justify-end">{{ bill.billTime }}</div>
            </div>
        </div>
        <div v-else-if="type === '退款'" class="flex justify-between h-18 cursor-pointer">
            <div class="flex space-x-2">
                <Icon class="text-blue-500 h-5 w-5 my-auto" icon="ph:arrows-down-up"/>
                <div class="my-auto">
                    <div class="text-base">{{ "退款" }}</div>
                    <div class="text-sm">{{ bill.remark }}</div>
                </div>
            </div>
            <div>
                <div class="text-blue-500 text-base flex justify-end">{{ bill.amount }}</div>
                <div class="text-sm flex justify-end">{{ bill.refundAsset }}</div>
                <div class="text-sm flex justify-end">{{ bill.billTime }}</div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import {Icon} from "@iconify/vue";
import {Ref, ref, watch} from "vue";
import {useStore} from "@/stores/store";

const store = useStore();
const props = defineProps({
    type: {
        type: String,
        default: "",
    },
    bill: {
        type: Object,
        default: {},
    },
});
let mouseOver: Ref<boolean> = ref(false);

function handleMouseEnter(enter: boolean) {
    mouseOver.value = enter;
}

function handleClick() {
    if (store.currentBill.id === props.bill.id) {
        store.currentBill = Object();
    } else {
        store.currentBill = props.bill;
        store.currentBillType = props.type;
    }
}

let chosen: Ref<boolean> = ref(false);
watch(
    () => store.currentBill.id,
    (newValue: any) => {
        chosen.value = (newValue === props.bill.id && store.currentBillType === props.type);
    }
);
</script>
<style scoped>
</style>
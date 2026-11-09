<template>
    <div>
        <n-card class="rounded-xl">
            <template #header>
                {{ day }}
            </template>
            <template #default>
                <BillItem v-for="item in billList" v-bind:bill="item" v-bind:type="item.type"></BillItem>
            </template>
        </n-card>
    </div>
</template>
<script lang="ts" setup>
import BillItem from "./components";
import type { PropType } from "vue";

defineProps({
    day: {
        type: String,
        required: true
    },
    billList: {
        type: Array as PropType<Record<string, any>[]>,
        default: () => []
    }
});
</script>
<style scoped>
</style>

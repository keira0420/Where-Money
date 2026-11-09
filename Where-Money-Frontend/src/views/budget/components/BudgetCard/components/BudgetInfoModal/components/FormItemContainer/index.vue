<template>
    <div class="item-container flex flex-col items-start gap-2 xl:flex-row xl:items-center xl:justify-between">
        <span class="label-container text-base w-full xl:w-120px xl:mr-2">{{ label }}</span>
        <div class="option-container flex items-center justify-start w-full xl:justify-end">
            <slot></slot>
        </div>
    </div>
</template>

<script lang="ts" setup>

import {defineProps} from "vue";

const props = defineProps({
    label: {
        type: String,
        default: ""
    }
});

</script>

<template>
    <div class="item-container flex-y-center justify-between">
        <span class="label-container text-base w-160px mr-2">{{ label }}</span>
        <div class="option-container flex-y-center justify-end">
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

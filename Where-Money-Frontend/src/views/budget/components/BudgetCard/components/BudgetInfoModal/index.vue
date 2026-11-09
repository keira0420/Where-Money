<template>
    <n-modal :auto-focus="false" :show="showModal" class="w-[92vw] max-w-[400px] xl:w-400px" display-directive="show"
             preset="card"
             @close="closeModal" @after-enter="resetValue">
        <template #header>
            <n-space align="flex-end" justify="start">
                <span class="text-lg font-bold">预算详情</span>
                <n-button size="small" text @click="canInput = true">
                    <div v-show="!canInput" class="flex-y-center">
                        <span class="text-sm">修改</span>
                        <icon height="1rem" icon="fluent:edit-48-regular" width="1rem"></icon>
                    </div>
                </n-button>
                <n-button size="small" text @click="handleDelete">
                    <div class="flex-y-center">
                        <span class="text-sm">删除</span>
                        <icon height="1rem" icon="fluent:delete-48-regular" width="1rem"></icon>
                    </div>
                </n-button>
            </n-space>

        </template>

        <template #default>
            <n-form :inline="false" :model="budgetInfoVar" label-width="80">
                <n-space :size="16" vertical>
                    <form-item-container label="类别">
                        <n-input v-model:value="category.billCategoryName" :disabled="true"
                                 class="flex-1 xl:w-200px xl:flex-none"
                                 size="medium"/>
                        <Icon :icon="category.svg" class="text-primary mx-2" height="24"></Icon>
                    </form-item-container>

                    <form-item-container label="总额">
                        <n-input-number v-model:value="budgetInfoVar.limit" :disabled="!canInput" :min="0"
                                        :show-button="false"
                                        class="flex-1 xl:w-200px xl:flex-none" size="medium">
                            <template #prefix>￥</template>
                        </n-input-number>
                    </form-item-container>

                    <form-item-container label="已使用">
                        <n-input-number v-model:value="budgetInfoVar.used" :disabled="true" :min="0"
                                        :show-button="false"
                                        class="flex-1 xl:w-200px xl:flex-none" size="medium">
                            <template #prefix>￥</template>
                        </n-input-number>
                    </form-item-container>

                    <form-item-container label="消费次数">
                        <n-input-number v-model:value="budgetInfoVar.times" :disabled="true" :min="0"
                                        :show-button="true" :validator="(value) => value.toString().indexOf('.') === -1"
                                        class="flex-1 xl:w-200px xl:flex-none"
                                        size="medium">
                            <template #prefix></template>
                        </n-input-number>
                    </form-item-container>


                    <n-button :disabled="!canInput" class="w-full" type="primary" @click="applyChanges">
                        <span>保存</span>
                    </n-button>
                </n-space>
            </n-form>

        </template>
    </n-modal>
</template>

<script lang="ts" setup>

import {defineEmits, defineProps, PropType, ref} from "vue";
import {BillCategory, Budget} from "@/interface";
import {deleteBudget, updateBudget} from "@/apis";

import {Icon} from "@iconify/vue";
import {FormItemContainer} from "./components";
import {useMessage} from "naive-ui";

const props = defineProps({
    showModal: {
        type: Boolean,
        required: true
    },
    budget: {
        type: Object as PropType<Budget>,
        required: true
    },
    category: {
        type: Object as PropType<BillCategory>,
        required: true
    }
});

const canInput = ref(false);
const budgetInfoVar = ref<Budget>({...props.budget});

const budgetBackup = ref<Budget>({
    id: 0,
    bookId: 0,
    billCategoryId: 0,
    used: 0,
    limit: 0,
    times: 0
});

const emit = defineEmits(["update:showModal", "manualUpdateBook", "update:budget"]);

function closeModal() {
    emit("update:showModal", false);
    canInput.value = false;
    budgetInfoVar.value = budgetBackup.value;
}

function resetValue() {
    budgetBackup.value = Object.assign({}, props.budget);
    budgetInfoVar.value = budgetBackup.value;
}

const message = useMessage();

function applyChanges() {
    const param = {
        budgetId: budgetInfoVar.value.id,
        billCategoryId: budgetInfoVar.value.billCategoryId,
        limit: budgetInfoVar.value.limit,
        amount: null,
        times: null
    };
    updateBudget(param).then(() => {
        emit("update:budget", budgetInfoVar.value);
        emit("manualUpdateBook");
        message.success("修改成功！");
        closeModal();
    }).catch((err: Error) => {
        message.error("修改失败！");
        console.error(err);
    });
}

function handleDelete() {
    deleteBudget({budgetId: budgetInfoVar.value.id}).then(() => {
        emit("update:budget", budgetInfoVar.value);
        emit("manualUpdateBook");
        message.success("删除成功！");
        closeModal();
    }).catch((err: Error) => {
        message.error("删除失败！");
        console.error(err);
    });
}
</script>

<style lang="scss" scoped>
:deep(.option-container) {
    width: 100%;
}

@media (min-width: 1280px) {
    :deep(.option-container) {
        width: 200px;
    }
}
</style>

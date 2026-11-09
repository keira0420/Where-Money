<template>
    <n-modal
        :auto-focus="false"
        :show="showModal"
        class="w-[calc(100vw-32px)] xl:w-500px lg:w-1/2"
        display-directive="show"
        preset="card"
        @close="closeModal"
    >
        <template #header>
            <span class="text-base xl:text-lg font-bold lg:text-lg">新增资产</span>
        </template>

        <template #default>
            <n-form v-if="assetInfo" :model="assetInfo">
                <div class="flex flex-col gap-4">
                    <!-- 资产类型 -->
                    <div
                        class="flex flex-col xl:flex-row xl:items-center xl:gap-4 lg:flex-row lg:items-center lg:gap-4">
                        <div class="text-sm text-gray-500 xl:w-24 shrink-0 lg:w-24">资产类型</div>
                        <div class="flex-1 overflow-x-auto overflow-y-hidden no-scrollbar">
                            <n-radio-group
                                v-model:value="assetInfo.type"
                                class="min-w-max"
                                name="assetTypeGroup"
                                size="medium"
                            >
                                <n-radio-button
                                    v-for="typeName in ['信用卡', '充值', '投资理财', '资金']"
                                    :key="typeName"
                                    :label="typeName"
                                    :value="typeName"
                                    size="medium"
                                >
                                    <span>{{ typeName }}</span>
                                </n-radio-button>
                            </n-radio-group>
                        </div>
                    </div>

                    <!-- 资产图标 -->
                    <div
                        class="flex flex-col xl:flex-row xl:items-center xl:gap-4 lg:flex-row lg:items-center lg:gap-4">
                        <div class="text-sm text-gray-500 xl:w-24 shrink-0 lg:w-24">资产图标</div>
                        <div class="flex flex-col sm:flex-row sm:items-center gap-2 flex-1">
                            <n-input
                                v-model:value.trim="assetInfo.svg"
                                class="w-full sm:flex-1 xl:w-240px lg:w-240px"
                                size="medium"
                            />
                            <div class="flex items-center gap-2">
                                <n-button secondary size="small" @click="openIconPicker">
                                    选择图标
                                </n-button>
                                <Icon
                                    :icon="assetInfo.svg || 'icon-park-outline:tag'"
                                    class="text-info shrink-0"
                                    height="24"
                                />
                            </div>
                        </div>
                    </div>

                    <!-- 资产名称 -->
                    <div
                        class="flex flex-col xl:flex-row xl:items-center xl:gap-4 lg:flex-row lg:items-center lg:gap-4">
                        <div class="text-sm text-gray-500 xl:w-24 shrink-0 lg:w-24">资产名称</div>
                        <div class="flex-1">
                            <n-input
                                v-model:value="assetInfo.assetName"
                                class="w-full xl:w-240px lg:w-240px"
                                size="medium"
                            />
                        </div>
                    </div>

                    <!-- 资产余额/欠款 -->
                    <div
                        class="flex flex-col xl:flex-row xl:items-center xl:gap-4 lg:flex-row lg:items-center lg:gap-4">
                        <div class="text-sm text-gray-500 xl:w-24 shrink-0 lg:w-24">
                            {{ assetInfo.type === '信用卡' ? '当前欠款（负数）' : '资产余额' }}
                        </div>
                        <div class="flex-1">
                            <n-input-number
                                v-model:value="assetInfo.balance"
                                :show-button="false"
                                :validator="validateAssetBalance"
                                class="w-full xl:w-240px lg:w-240px"
                                size="medium"
                            >
                                <template #prefix>￥</template>
                            </n-input-number>
                        </div>
                    </div>

                    <!-- 计入资产总额 -->
                    <div
                        class="flex flex-col xl:flex-row xl:items-center xl:gap-4 lg:flex-row lg:items-center lg:gap-4">
                        <div class="text-sm text-gray-500 xl:w-24 shrink-0 lg:w-24">计入资产总额</div>
                        <div class="flex-1">
                            <n-switch v-model:value="assetInfo.inTotal" size="medium"/>
                        </div>
                    </div>

                    <!-- 信用卡额外字段 -->
                    <template v-if="assetInfo.type === '信用卡'">
                        <n-divider class="my-1">账单设置</n-divider>

                        <div
                            class="flex flex-col xl:flex-row xl:items-center xl:gap-4 lg:flex-row lg:items-center lg:gap-4">
                            <div class="text-sm text-gray-500 xl:w-24 shrink-0 lg:w-24">总额度</div>
                            <div class="flex-1">
                                <n-input-number
                                    v-model:value="assetInfo.quota"
                                    :min="0"
                                    :show-button="false"
                                    class="w-full xl:w-240px"
                                    size="medium"
                                >
                                    <template #prefix>￥</template>
                                </n-input-number>
                            </div>
                        </div>

                        <div
                            class="flex flex-col xl:flex-row xl:items-center xl:gap-4 lg:flex-row lg:items-center lg:gap-4">
                            <div class="text-sm text-gray-500 xl:w-24 shrink-0 lg:w-24">出账日</div>
                            <div class="flex-1">
                                <n-input-number
                                    v-model:value="assetInfo.billDate"
                                    :max="31"
                                    :min="1"
                                    class="w-full xl:w-240px lg:w-240px text-center"
                                    size="medium"
                                >
                                    <template #prefix>每月</template>
                                    <template #suffix>日</template>
                                </n-input-number>
                            </div>
                        </div>

                        <div
                            class="flex flex-col xl:flex-row xl:items-center xl:gap-4 lg:flex-row lg:items-center lg:gap-4">
                            <div class="text-sm text-gray-500 xl:w-24 shrink-0 lg:w-24">还款日</div>
                            <div class="flex-1">
                                <n-input-number
                                    v-model:value="assetInfo.repayDate"
                                    :max="31"
                                    :min="1"
                                    class="w-full xl:w-240px lg:w-240px text-center lg:w-240px"
                                    size="medium"
                                >
                                    <template #prefix>每月</template>
                                    <template #suffix>日</template>
                                </n-input-number>
                            </div>
                        </div>
                    </template>

                    <n-button class="w-full" type="primary" @click="submitHandler">
                        <span>确定</span>
                    </n-button>
                </div>
            </n-form>
        </template>
    </n-modal>
    <IconifyPicker v-model:show="showIconPicker" v-on:select="handleIconSelected"/>
</template>

<script lang="ts" setup>
import {defineEmits, defineProps, ref} from "vue";
import {useMessage} from "naive-ui";
import {Icon} from "@iconify/vue";
import IconifyPicker from "@/components/custom/IconifyPicker/index.vue";

const props = defineProps({
    showModal: {
        type: Boolean,
        required: true,
        default: false
    }
});

const assetInfo = ref<any>({inTotal: true});
const showIconPicker = ref(false);

function validateAssetBalance(value: number) {
    if (assetInfo.value === undefined) return false;
    if (assetInfo.value.type === "信用卡" && value > 0) return false;
    return !(assetInfo.value.type !== "信用卡" && value < 0);
}

const emit = defineEmits(["update:showModal", "newAssetSubmitted"]);

function closeModal() {
    emit("update:showModal", false);
    showIconPicker.value = false;
}

const message = useMessage();

function submitHandler() {
    let allBlankFilled = true;
    let keys = ["assetName", "balance", "type", "svg"];
    for (const keyIdx in keys) {
        if (!assetInfo.value[keys[keyIdx]]) {
            message.error("请填写完整信息！");
            allBlankFilled = false;
            break;
        }
    }
    if (assetInfo.value.type === "信用卡") {
        keys = ["billDate", "repayDate", "quota"];
        for (const keyIdx in keys) {
            if (!assetInfo.value[keys[keyIdx]]) {
                message.error("请填写完整信息！");
                allBlankFilled = false;
                break;
            }
        }
    }
    if (!allBlankFilled) return;

    if (assetInfo.value.type === "信用卡" && assetInfo.value.balance! > 0) {
        message.error("欠款金额请输入负数！");
    } else if (assetInfo.value.type !== "信用卡" && assetInfo.value.balance! < 0) {
        message.error("资产余额不能为负数！");
    } else {
        emit("newAssetSubmitted", assetInfo.value);
        for (const key in assetInfo.value) {
            assetInfo.value[key] = null;
        }
        assetInfo.value.inTotal = true;
        message.success("添加成功！");
    }
}

function openIconPicker() {
    showIconPicker.value = true;
}

function handleIconSelected(iconName: string) {
    assetInfo.value.svg = iconName;
}
</script>

<style lang="scss" scoped>
/* 隐藏滚动条，但仍可左右滑动 */
:deep(.no-scrollbar) {
    scrollbar-width: none;
    -ms-overflow-style: none;
}

:deep(.no-scrollbar::-webkit-scrollbar) {
    display: none;
    height: 0;
}
</style>

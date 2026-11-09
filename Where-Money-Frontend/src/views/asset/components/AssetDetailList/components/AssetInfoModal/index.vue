<template>
    <n-modal
        :auto-focus="false"
        :show="showModal"
        class="w-[calc(100vw-32px)] xl:w-500px lg:w-500px"
        display-directive="show"
        preset="card"
        @close="closeModal"
        @after-enter="getAssetInfoById"
    >
        <template #header>
            <div class="flex items-end justify-between">
                <div class="flex items-end gap-2">
                    <span class="font-bold text-base xl:text-lg lg:text-lg">资产信息</span>

                    <n-button :disabled="canInput" size="small" text @click="canInput = true">
                        <div v-show="!canInput" class="flex items-center gap-1">
                            <span class="text-xs xl:text-sm lg:text-sm">修改</span>
                            <Icon height="1rem" icon="fluent:edit-48-regular" width="1rem"/>
                        </div>
                    </n-button>
                </div>
            </div>
        </template>

        <template #default>
            <n-form v-if="assetInfo !== undefined" :model="assetInfo">
                <div class="flex flex-col gap-4">
                    <!-- 资产类型 -->
                    <div
                        class="flex flex-col xl:flex-row xl:items-center xl:gap-4 lg:flex-row lg:items-center lg:gap-4">
                        <div class="text-sm text-gray-500 xl:w-24 shrink-0 lg:w-24">资产类型</div>
                        <div class="flex-1 overflow-x-auto overflow-y-hidden">
                            <n-radio-group
                                v-model:value="assetInfo.type"
                                class="min-w-max"
                                name="assetTypeGroup"
                                size="medium"
                            >
                                <n-radio-button
                                    v-for="typeName in ['信用卡', '充值', '投资理财', '资金']"
                                    :key="typeName"
                                    :disabled="true"
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
                                v-model:value="assetInfo.svg"
                                :disabled="!canInput"
                                class="w-full sm:flex-1 xl:w-240px lg:w-240px"
                                size="medium"
                            />
                            <div class="flex items-center gap-2">
                                <n-button :disabled="!canInput" secondary size="small" @click="openIconPicker">
                                    选择图标
                                </n-button>
                                <Icon
                                    :icon="assetInfo.svg || 'icon-park-outline:tag'"
                                    class="text-primary shrink-0"
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
                                :disabled="!canInput"
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
                                :disabled="!canInput"
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
                            <n-switch
                                v-model:value="assetInfo.inTotal"
                                :disabled="!canInput"
                                size="medium"
                            />
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
                                    :disabled="!canInput"
                                    :min="0"
                                    :show-button="false"
                                    class="w-full xl:w-240px lg:w-240px"
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
                                    :disabled="!canInput"
                                    :max="31"
                                    :min="1"
                                    class="w-full xl:w-240px text-center lg:w-240px"
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
                                    :disabled="!canInput"
                                    :max="31"
                                    :min="1"
                                    class="w-full xl:w-240px text-center lg:w-240px"
                                    size="medium"
                                >
                                    <template #prefix>每月</template>
                                    <template #suffix>日</template>
                                </n-input-number>
                            </div>
                        </div>
                    </template>

                    <n-button
                        :disabled="!canInput"
                        class="w-full"
                        type="primary"
                        @click="applyChanges"
                    >
                        <span>确定</span>
                    </n-button>
                </div>
            </n-form>
        </template>
    </n-modal>
    <IconifyPicker v-model:show="showIconPicker" v-on:select="handleIconSelected"/>
</template>

<script lang="ts" setup>
import {defineEmits, defineProps, ref, watch} from "vue";
import {Asset, AssetGetAssetResponse} from "@/interface";
import {getAssetApi} from "@/apis";
import {useMessage} from "naive-ui";
import {Icon} from "@iconify/vue";
import IconifyPicker from "@/components/custom/IconifyPicker/index.vue";

const props = defineProps({
    showModal: {type: Boolean, required: true, default: false},
    assetId: {type: Number, required: true, default: 0}
});

const assetInfo = ref<Asset>();
const showIconPicker = ref(false);

function getAssetInfoById() {
    const params = {id: props.assetId};
    getAssetApi(params).then((res: AssetGetAssetResponse) => {
        assetInfo.value = res.asset;
    }).catch((err: Error) => {
        console.log(err);
    });
}

const canInput = ref(false);

watch(() => props.assetId, () => {
    getAssetInfoById();
});

function validateAssetBalance(value: number) {
    if (assetInfo.value === undefined) return false;
    if (assetInfo.value.type === "信用卡" && value > 0) return false;
    return !(assetInfo.value.type !== "信用卡" && value < 0);
}

const emit = defineEmits(["update:showModal", "changeSubmitted"]);

async function closeModal() {
    emit("update:showModal", false);
    canInput.value = false;
    showIconPicker.value = false;
}

const message = useMessage();

function applyChanges() {
    if (assetInfo.value!.type === "信用卡" && assetInfo.value!.balance > 0) {
        message.error("欠款金额请输入负数！");
    } else if (assetInfo.value!.type !== "信用卡" && assetInfo.value!.balance < 0) {
        message.error("资产余额不能为负数！");
    } else {
        canInput.value = false;
        emit("changeSubmitted", assetInfo.value!);
    }
}

function openIconPicker() {
    if (!canInput.value) return;
    showIconPicker.value = true;
}

function handleIconSelected(iconName: string) {
    if (!assetInfo.value) return;
    assetInfo.value.svg = iconName;
}
</script>

<style lang="scss" scoped>
/* 这版不再依赖 FormItemContainer，因此不需要 option-container 的固定宽度了 */
</style>

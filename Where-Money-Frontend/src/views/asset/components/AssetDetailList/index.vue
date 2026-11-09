<template>
    <n-card :bordered="false" class="rounded-16px shadow-sm">
        <template #header>
            <n-space align="center">
                <Icon height="24px" icon="bi:card-list" width="24px"/>
                <span class="font-bold text-xl">资产明细</span>
            </n-space>
        </template>

        <template #header-extra>
            <n-button text @click="showNewAssetModal">
                <Icon height="24px" icon="fluent:add-24-filled"/>
                <span class="text-base px-2">新增资产</span>
            </n-button>
        </template>

        <template #default>
            <n-collapse :default-expanded-names="['信用卡', '充值', '投资理财', '资金']">
                <n-collapse-item
                    v-for="assetType in sortedTypeUniqueArray"
                    :key="assetType"
                    :name="assetType"
                >
                    <!-- 标题，按类别 -->
                    <template #header>
                        <span class="text-base font-bold">{{ assetType }}账户</span>
                    </template>

                    <!-- 合计 -->
                    <template #header-extra>
                        <span
                            :class="{ 'text-red-500': assetSumByType.get(assetType)! < 0 }"
                            class="text-lg font-bold"
                        >
                            {{ formattedCurrencyNoSymbol(assetSumByType.get(assetType)!) }}
                        </span>
                    </template>

                    <!-- 内容 -->
                    <template #default>
                        <n-list class="m-1">
                            <!-- <n-list-item v-for="(asset) in assetListByType.get(type)" :key="asset.id"> -->
                            <n-list-item
                                v-for="asset in assetListByType.get(assetType)"
                                :key="asset.id"
                                class="hover:bg-[#f6f6f6] dark:hover:bg-[#333]"
                                @click="showDetail(asset.id)"
                            >
                                <div class="asset-item-container flex items-center px-2">
                                    <!-- Icon -->
                                    <div class="mr-2 w-8">
                                        <Icon
                                            :height="24"
                                            :icon="asset.svg"
                                            :inline="true"
                                            class="text-primary"/>
                                    </div>
                                    <!-- Info -->
                                    <div class="flex-auto">
                                        <n-space :vertical="true">
                                            <n-space justify="space-between">
                                                <span class="text-base">{{ asset.assetName }}</span>
                                                <span class="text-base">
                                                    {{ formattedCurrencyNoSymbol(asset.balance) }}
                                                </span>
                                            </n-space>
                                            <!-- 如果是信用卡，额外显示可用余额、还款日 -->
                                            <div v-if="asset.type === '信用卡'"
                                                 class="asset-item-credit-container"
                                            >
                                                <!-- 余额进度条 -->
                                                <n-progress
                                                    :height="5"
                                                    :percentage="(1 + asset.balance / asset.quota) * 100"
                                                    :show-indicator="false"
                                                    type="line"/>
                                                <!-- 还款日、余额文字 -->
                                                <n-space justify="space-between">
                                                    <p class="text-gray-400 text-small">
                                                        <span>距离还款日还有 </span>
                                                        <span class="font-bold">
                                                            {{ daysBeforeRepayDate(asset.repayDate) }}
                                                        </span>
                                                        <span> 天</span>
                                                    </p>
                                                    <p class="text-gray-400 text-small">
                                                        <span>可用: </span>
                                                        <span>
                                                            {{ formattedCurrencyNoSymbol(asset.balance + asset.quota) }}
                                                        </span>
                                                    </p>
                                                </n-space>
                                            </div>
                                        </n-space>
                                    </div>
                                </div>
                            </n-list-item>
                        </n-list>
                    </template>
                </n-collapse-item>
            </n-collapse>
        </template>
    </n-card>

    <asset-info-modal
        v-model:show-modal="showInfoModal"
        :asset-id="showId"
        @change-submitted="postChange"/>

    <new-asset-modal
        v-model:show-modal="showAddModal"
        @new-asset-submitted="postAddAsset"/>
</template>

<script lang="ts" setup>
// vue
import {computed, defineProps, PropType, ref} from "vue";
// components
import {Icon} from "@iconify/vue";
import {AssetInfoModal, NewAssetModal} from "./components";
import {useMessage} from "naive-ui";
// ts
import {Asset} from "@/interface";
import {addAsset, updateAsset} from "@/apis";
import {formattedCurrencyNoSymbol} from "@/utils";

const props = defineProps({
    assetList: {
        type: Array as unknown as PropType<Asset[]>,
        default: [] as Asset[]
    }
});

/**
 * @description 有内容的资产类别列表（固定顺序）
 */
const sortedTypeUniqueArray = computed(() => {
    let set = new Set<string>();
    props.assetList.map((asset: any) => {
        set.add(asset.type);
    });
    return [...set].sort();
});

/**
 * @description 各类资产总额的Map，资产类别为key、资产总额为value
 */
const assetSumByType = computed(() => {
    let map = new Map<string, number>();
    props.assetList.map((asset: any) => {
        if (!map.has(asset.type)) {
            map.set(asset.type, 0);
        }
        map.set(asset.type, map.get(asset.type)! + asset.balance);
    });
    return map;
});

/**
 * @description 各类别资产列表，资产类别为key、资产列表为value
 */
const assetListByType = computed(() => {
    let map = new Map<string, Array<Asset>>();
    props.assetList.map((asset: any) => {
        if (!map.has(asset.type)) {
            map.set(asset.type, []);
        }
        map.get(asset.type)!.push(asset);
    });
    return map;
});

/**
 * @description 计算距离还款日天数
 * @param {string} repayDate 还款日
 * @returns {number | string} 距离还款日天数
 */
function daysBeforeRepayDate(repayDate: number): number | string {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    if (repayDate < today.getDate()) {
        const repayDay = new Date(today.getFullYear(), today.getMonth() + 1, repayDate, 0, 0, 0, 0);
        return parseInt(((repayDay.valueOf() - today.valueOf()) / (24 * 60 * 60 * 1000)).toFixed(0));
    } else {
        return repayDate - today.getDate();
    }
}

const showId = ref(0);
const showInfoModal = ref(false);

function showDetail(id: number) {
    showId.value = id;
    showInfoModal.value = true;
}

const showAddModal = ref(false);

function showNewAssetModal() {
    showAddModal.value = true;
}


const emit = defineEmits(["assetListChanged"]);
const message = useMessage();

/**
 * @description 修改资产
 */
function postChange(changed: Asset) {
    // 发送请求
    const assetOriginal = props.assetList.filter(item => item.id === changed.id)[0];
    let params: Record<string, any> = {};
    Object.keys(assetOriginal).forEach(key => {
        if (assetOriginal[key] !== changed[key]) {
            params[key] = changed[key];
        }
    });
    updateAsset({assetId: changed.id, ...params}).then(() => {
        // 发送请求成功后，更新资产列表
        emit("assetListChanged", null);
        // 关闭弹窗
        message.success("修改成功！");
        showInfoModal.value = false;
    });

}

/**
 * @description 新增资产
 */
function postAddAsset(asset: Asset) {
    // 发送请求
    console.log(asset);
    addAsset(asset).then(() => {
        // 发送请求成功后，更新资产列表
        emit("assetListChanged", null);
        // 关闭弹窗
        showAddModal.value = false;
    });
}

</script>

<style lang="scss" scoped>
.n-list {
    margin-block-start: 0;
    margin-block-end: 0;

    .n-list-item {
        -webkit-transition-duration: 0.2s;
        transition-duration: 0.2s;

        -webkit-transition-property: background-color;
        transition-property: background-color;

        border-radius: 8px;

        svg {
            -webkit-transition-duration: 0.2s;
            transition-duration: 0.2s;

            -webkit-transition-property: color;
            transition-property: color;
        }

        &:hover svg {
            color: var(--primary-color-hover);
        }
    }
}

.n-collapse .n-collapse-item {
    margin-top: 0;
}
</style>

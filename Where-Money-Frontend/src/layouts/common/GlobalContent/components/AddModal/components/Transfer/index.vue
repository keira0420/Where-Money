<template>
    <div>
        <div class="space-y-10">
            <div class="space-y-2">
                <div>
                    <n-button class="w-full h-15" v-on:click="outDrawerShower">
                        <template #default>
                            <div class="flex space-x-2">
                                <div v-if="outAssetName!=='转出账户'">
                                    <Icon :icon="outAssetSvg" class="text-primary w-8 h-8"/>
                                </div>
                                <div class="m-auto">
                                    {{ outAssetName }}
                                </div>
                                <div v-if="outAssetName!=='转出账户'" class="m-auto">
                                    ￥{{ outAssetBalance }}
                                </div>
                            </div>
                        </template>
                    </n-button>
                </div>
                <div>
                    <div>
                        <Icon class="m-auto text-primary w-7 h-7" icon="ic:baseline-arrow-downward"/>
                    </div>
                </div>
                <div>
                    <n-button class="w-full h-15" v-on:click="inDrawerShower">
                        <template #default>
                            <div class="flex space-x-2">
                                <div v-if="inAssetName!=='转入账户'">
                                    <Icon :icon="inAssetSvg" class="text-primary w-8 h-8"/>
                                </div>
                                <div class="m-auto">
                                    {{ inAssetName }}
                                </div>
                                <div v-if="inAssetName!=='转入账户'" class="m-auto">
                                    ￥{{ inAssetBalance }}
                                </div>
                            </div>
                        </template>
                    </n-button>
                </div>
            </div>
            <div class="space-y-2">
                <div class="grid grid-cols-2 lg:grid-cols-3 gap-2">
                    <div class="col-span-1 lg:col-span-2">
                        <n-input v-model:value="remark" placeholder="点此输入备注" type="text"/>
                    </div>
                    <div class="col-span-1">
                        <n-input-number v-model:value="amount" step="0.01">
                            <template #prefix>
                                ￥
                            </template>
                        </n-input-number>
                    </div>
                </div>
                <div class="grid grid-cols-2 lg:grid-cols-3 gap-2">
                    <div class="col-span-1">
                        <n-button class="w-full truncate" v-on:click="bookDrawerShower">
                            <template #default>
                                <n-spin v-if="isLoading" class="items-center h-4 w-4"></n-spin>
                                <div v-if="!isLoading" class="text-left">
                                    {{ bookName }}
                                </div>
                            </template>
                        </n-button>
                    </div>
                    <div class="col-span-1">
                        <n-popover trigger="hover">
                            <template #trigger>
                                <n-button class="w-full" v-bind:type="fee === 0 ? 'default' : 'primary'"
                                          v-on:click="feeDrawerShower">
                                    <template #default>
                                        手续费
                                    </template>
                                </n-button>
                            </template>
                            <template #default>
                                ￥{{ fee }}
                            </template>
                        </n-popover>
                    </div>
                    <div class="col-span-2 lg:col-span-1">
                        <n-date-picker v-model:value="timestamp" :input-readonly="true" :time-picker-props="timePickerProps"
                                       :update-value-on-close="true" placement="top-start"
                                       type="datetime"/>
                    </div>
                </div>
                <div class="flex space-x-2">
                    <n-upload :custom-request="customRequest" class="w-1/5 upload-mini-on-mobile"
                              list-type="image-card" :max="1" v-on:change="changePicture"
                              v-on:before-upload="beforeUpload">
                        <template #default>
                            <div>
                                图片
                            </div>
                        </template>
                    </n-upload>
                    <div class="my-auto">
                        {{ fileName }}
                    </div>
                </div>
            </div>
            <n-modal v-model:show="showAiModal" :close-on-esc="!aiParsing" :mask-closable="!aiParsing">
                <n-card class="w-[92vw] max-w-[640px]" title="AI解析账单">
                    <n-spin :show="aiParsing">
                        <div class="space-y-3">
                            <n-input
                                v-model:value="aiText"
                                :autosize="{ minRows: 4, maxRows: 8 }"
                                :disabled="aiParsing"
                                placeholder="请输入要解析的账单文本"
                                type="textarea"
                            />
                            <div class="flex justify-end space-x-2">
                                <n-button :disabled="aiParsing" v-on:click="closeAiModal">关闭</n-button>
                                <n-button :loading="aiParsing" type="primary" v-on:click="parseAiBill">解析</n-button>
                            </div>
                        </div>
                    </n-spin>
                </n-card>
            </n-modal>
            <div class="flex space-x-2">
                <n-button class="w-24" secondary type="primary" v-on:click="openAiModal">
                    <template #default>
                        AI
                    </template>
                </n-button>
                <n-button class="flex-1 addButton" type="primary" v-on:click="addBill">
                    <template #default>
                        保存
                    </template>
                </n-button>
            </div>
        </div>
        <n-drawer v-model:show="showOutDrawer" :height="400" :placement="'bottom'" to="#drawer-target">
            <n-drawer-content>
                <template #header>
                    <div>
                        选择转出账户
                    </div>
                </template>
                <template #default>
                    <div>
                        <n-scrollbar class="h-78">
                            <n-spin v-if="isOutLoading" class="flex items-center h-78"></n-spin>
                            <n-radio-group v-if="!isOutLoading" v-model:value="outSelector" class="space-y-4 ">
                                <div v-for="item in assetList">
                                    <n-radio v-bind:key="item.id" v-bind:value="item.assetName">
                                        <div class="flex space-x-2 align-middle">
                                            <div>
                                                <Icon :icon="item.svg" class="text-primary w-8 h-8"/>
                                            </div>
                                            <div class="w-100 m-auto">
                                                {{ item.assetName }}
                                            </div>
                                            <div class="m-auto">
                                                ￥{{ item.balance }}
                                            </div>
                                        </div>
                                    </n-radio>
                                </div>
                            </n-radio-group>
                        </n-scrollbar>
                    </div>
                </template>
            </n-drawer-content>
        </n-drawer>
        <n-drawer v-model:show="showInDrawer" :height="400" :placement="'bottom'" to="#drawer-target">
            <n-drawer-content>
                <template #header>
                    <div>
                        选择转入账户
                    </div>
                </template>
                <template #default>
                    <div>
                        <n-scrollbar class="h-78">
                            <n-spin v-if="isInLoading" class="flex items-center h-78"></n-spin>
                            <n-radio-group v-if="!isInLoading" v-model:value="inSelector" class="space-y-4 ">
                                <div v-for="item in assetList">
                                    <n-radio v-bind:key="item.id" v-bind:value="item.assetName">
                                        <div class="flex space-x-2">
                                            <div>
                                                <Icon :icon="item.svg" class="text-primary w-8 h-8"/>
                                            </div>
                                            <div class="w-100 m-auto">
                                                {{ item.assetName }}
                                            </div>
                                            <div class="m-auto">
                                                ￥{{ item.balance }}
                                            </div>
                                        </div>
                                    </n-radio>
                                </div>
                            </n-radio-group>
                        </n-scrollbar>
                    </div>
                </template>
            </n-drawer-content>
        </n-drawer>
        <n-drawer v-model:show="showBookDrawer" :height="400" :placement="'bottom'" to="#drawer-target">
            <n-drawer-content>
                <template #header>
                    <div>
                        选择账本
                    </div>
                </template>
                <template #default>
                    <n-scrollbar class="h-78">
                        <n-spin v-if="isBookLoading" class="flex items-center h-78"></n-spin>
                        <n-radio-group v-if="!isBookLoading" v-model:value="bookSelector" class="space-y-4 ">
                            <div v-for="item in bookList">
                                <n-radio v-bind:key="item.id" v-bind:value="item.title">
                                    <div class="flex space-x-2 item-center">
                                        <div class="w-128 m-auto">
                                            {{ item.title }}
                                        </div>
                                        <div>
                                            <n-tag class="bg-primary-active cursor-pointer">
                                                每月{{ item.beginDate }}日起始
                                            </n-tag>
                                        </div>
                                    </div>
                                </n-radio>
                            </div>
                        </n-radio-group>
                    </n-scrollbar>
                </template>
            </n-drawer-content>
        </n-drawer>
        <n-drawer v-model:show="showFeeDrawer" :height="150" :placement="'bottom'" to="#drawer-target">
            <n-drawer-content>
                <template #header>
                    <div>
                        输入手续费
                    </div>
                </template>
                <template #default>
                    <div>
                        <n-input-number v-model:value="fee" step="0.01">
                            <template #prefix>
                                ￥
                            </template>
                        </n-input-number>
                    </div>
                </template>
            </n-drawer-content>
        </n-drawer>
    </div>
</template>
<script lang="ts" setup>
import {onMounted, ref, Ref, watch} from "vue";
import {addBillApi, aiParseBillApi, getAllAsset, getAllBookApi, getAssetApi, getBookApi} from "@/apis";
import {
    Asset,
    AssetGetAllAssetResponse,
    AssetGetAssetResponse,
    BillAiParseResponse,
    Book,
    BookGetAllBookResponse,
    BookGetBookResponse
} from "@/interface";
import {TimePickerProps, UploadCustomRequestOptions, UploadFileInfo} from "naive-ui";
import {intToString, stringToInt} from "@/utils/dateComputer";
import {Icon} from "@iconify/vue";
import {useStore} from "@/stores/store";

let timePickerProps: TimePickerProps = {inputReadonly: true};
const DEFAULT_OUT_ASSET_NAME = "转出账户";
const DEFAULT_IN_ASSET_NAME = "转入账户";
let remark: Ref<string> = ref("");
let amount: Ref<number> = ref(0);
let bookName: Ref<string> = ref("");
let bookId: Ref<number> = ref(0);
let timestamp: Ref<number> = ref(0);
let outAssetName: Ref<string> = ref("");
let outAssetBalance: Ref<number> = ref(0);
let outAssetSvg: Ref<string> = ref("");
let inAssetName: Ref<string> = ref("");
let inAssetBalance: Ref<number> = ref(0);
let inAssetSvg: Ref<string> = ref("");
let isLoading: Ref<boolean> = ref(false);
const store = useStore();
onMounted(() => {
    isLoading.value = true;
    bookId.value = store.bookId;
    timestamp.value = Date.now();
    timestamp.value -= timestamp.value % (60 * 1000)
    outAssetName.value = DEFAULT_OUT_ASSET_NAME;
    inAssetName.value = DEFAULT_IN_ASSET_NAME;
    getBookApi({id: bookId.value}).then((response: BookGetBookResponse) => {
        bookName.value = response.book.title;
        isLoading.value = false;
    }).catch(() => {
    });
});
let assetList: Ref<Array<Asset>> = ref([]);
let showOutDrawer: Ref<boolean> = ref(false);
let outAssetId: Ref<number> = ref(0);
let isOutLoading: Ref<boolean> = ref(false);

function outDrawerShower() {
    isOutLoading.value = true;
    showOutDrawer.value = true;
    getAllAsset().then((response: AssetGetAllAssetResponse) => {
        assetList.value = response.assetList;
        isOutLoading.value = false;
    });
}

let outSelector: Ref<string> = ref("");
watch(outSelector, (value: string) => {
    if (value) {
        let asset: Asset | undefined = assetList.value.find(item => item.assetName === value);
        if (asset) {
            outAssetId.value = asset.id;
            outAssetName.value = asset.assetName;
            outAssetBalance.value = asset.balance;
            outAssetSvg.value = asset.svg;
        }
    }
});
let showInDrawer: Ref<boolean> = ref(false);
let inAssetId: Ref<number> = ref(0);
let isInLoading: Ref<boolean> = ref(false);

function inDrawerShower() {
    isInLoading.value = true;
    showInDrawer.value = true;
    getAllAsset().then((response: AssetGetAllAssetResponse) => {
        assetList.value = response.assetList;
        isInLoading.value = false;
    });
}

let inSelector: Ref<string> = ref("");
watch(inSelector, (value: string) => {
    if (value) {
        let asset: Asset | undefined = assetList.value.find(item => item.assetName === value);
        if (asset) {
            inAssetId.value = asset.id;
            inAssetName.value = asset.assetName;
            inAssetBalance.value = asset.balance;
            inAssetSvg.value = asset.svg;
        }
    }
});
let bookList: Ref<Array<Book>> = ref([]);
let showBookDrawer: Ref<boolean> = ref(false);
let bookSelector: Ref<string> = ref("");
let isBookLoading: Ref<boolean> = ref(false);

function bookDrawerShower() {
    isBookLoading.value = true;
    showBookDrawer.value = true;
    getAllBookApi().then((response: BookGetAllBookResponse) => {
        bookList.value = response.bookList;
        let bookTitle: string | undefined = bookList.value.find(item => item.id === bookId.value)?.title;
        if (bookTitle) {
            bookSelector.value = bookTitle;
        }
        isBookLoading.value = false;
    }).catch(() => {
    });
}

watch(bookSelector, (value: string) => {
    if (value) {
        let book: Book | undefined = bookList.value.find(item => item.title === value);
        if (book) {
            bookId.value = book.id;
            bookName.value = book.title;
        }
    }
});

let showFeeDrawer: Ref<boolean> = ref(false);
let fee: Ref<number> = ref(0);

function feeDrawerShower() {
    showFeeDrawer.value = true;
}

let picture: File;
let fileName: Ref<string> = ref("");
const customRequest = (file: UploadCustomRequestOptions) => {
    picture = file.file.file as File;
    fileName.value = file.file.name;
};

function changePicture() {
    fileName.value = "";
}

declare const window: Window & { $message: any; URL: any };

function beforeUpload(data: { file: UploadFileInfo, fileList: UploadFileInfo[] }) {
    if (!data.file.file?.type.startsWith("image")) {
        window.$message.error("请上传图片");
        return false;
    }
    return true;
}

function addBill(): void {
    if (outAssetId.value === 0) {
        window.$message.error("请选择转出账户");
        return;
    }
    if (inAssetId.value === 0) {
        window.$message.error("请选择转入账户");
        return;
    }
    let formData: FormData = new FormData();
    formData.append("bookId", bookId.value as any);
    formData.append("outAssetId", outAssetId.value as any);
    formData.append("inAssetId", inAssetId.value as any);
    formData.append("type", "转账");
    formData.append("amount", amount.value as any);
    formData.append("transferFee", fee.value as any);
    formData.append("time", intToString(timestamp.value) as any);
    formData.append("remark", remark.value as any);
    formData.append("file", picture as File);
    addBillApi(formData).then(() => {
        getAssetApi({id: inAssetId.value}).then((response: AssetGetAssetResponse) => {
            inAssetBalance.value = response.asset.balance;
            getAssetApi({id: outAssetId.value}).then((response: AssetGetAssetResponse) => {
                outAssetBalance.value = response.asset.balance;
                window.$message.success("添加成功");
            }).catch(() => {
            });
        }).catch(() => {
        });
    }).catch(() => {
    });
}

let showAiModal: Ref<boolean> = ref(false);
let aiText: Ref<string> = ref("");
let aiParsing: Ref<boolean> = ref(false);

function openAiModal(): void {
    showAiModal.value = true;
}

function closeAiModal(): void {
    showAiModal.value = false;
    aiText.value = "";
}

function applyAiBillTime(billTime: string | null): void {
    if (billTime === null) return;
    const parsedTime = stringToInt(billTime);
    if (!Number.isNaN(parsedTime)) {
        timestamp.value = parsedTime;
    }
}

async function applyAiOutAsset(outAssetIdValue: number | null, outAssetNameValue: string | null): Promise<void> {
    if (outAssetIdValue === null) return;
    outAssetId.value = outAssetIdValue;
    try {
        const response: AssetGetAssetResponse = await getAssetApi({id: outAssetIdValue});
        outAssetName.value = response.asset.assetName;
        outAssetBalance.value = response.asset.balance;
        outAssetSvg.value = response.asset.svg;
    } catch (_err) {
        if (outAssetNameValue !== null) {
            outAssetName.value = outAssetNameValue;
        }
    }
}

async function applyAiInAsset(inAssetIdValue: number | null, inAssetNameValue: string | null): Promise<void> {
    if (inAssetIdValue === null) return;
    inAssetId.value = inAssetIdValue;
    try {
        const response: AssetGetAssetResponse = await getAssetApi({id: inAssetIdValue});
        inAssetName.value = response.asset.assetName;
        inAssetBalance.value = response.asset.balance;
        inAssetSvg.value = response.asset.svg;
    } catch (_err) {
        if (inAssetNameValue !== null) {
            inAssetName.value = inAssetNameValue;
        }
    }
}

async function parseAiBill(): Promise<void> {
    const text = aiText.value.trim();
    if (!text) {
        window.$message.error("请输入要解析的文本");
        return;
    }
    if (!bookId.value) {
        window.$message.error("请选择账本");
        return;
    }
    aiParsing.value = true;
    try {
        const data: BillAiParseResponse = await aiParseBillApi({
            bookId: bookId.value,
            type: "转账",
            text
        });
        if (data.amount !== null) {
            amount.value = data.amount;
        }
        if (data.transferFee !== null) {
            fee.value = data.transferFee;
        }
        if (data.remark !== null) {
            remark.value = data.remark;
        }
        applyAiBillTime(data.billTime);
        await Promise.all([
            applyAiOutAsset(data.outAssetId, data.outAssetName),
            applyAiInAsset(data.inAssetId, data.inAssetName)
        ]);
        closeAiModal();
    } catch (error: any) {
        window.$message.error(error?.message || "AI解析失败");
    } finally {
        aiParsing.value = false;
    }
}

watch(showAiModal, (value: boolean) => {
    if (!value && !aiParsing.value) {
        aiText.value = "";
    }
});
</script>

<style scoped>
/* <style scoped> */
@media (max-width: 1023px) {
    /* Tailwind 的 lg 是 1024px 起 */
    .upload-mini-on-mobile :deep(.n-upload-file--image-card),
    .upload-mini-on-mobile :deep(.n-upload-trigger--image-card) {
        width: 56px;
        height: 56px;
    }

    /* 有些版本内部还会单独控制图片/图标容器，顺便限制一下 */
    .upload-mini-on-mobile :deep(.n-upload-file--image-card .n-upload-file__preview),
    .upload-mini-on-mobile :deep(.n-upload-trigger--image-card .n-upload-trigger__content) {
        width: 56px;
        height: 56px;
    }

    /* 让“图片”两个字别把卡片撑高/撑松 */
    .upload-mini-on-mobile :deep(.n-upload-trigger__text) {
        font-size: 12px;
        line-height: 1;
    }
}
</style>

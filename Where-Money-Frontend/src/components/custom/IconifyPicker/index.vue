<template>
    <n-modal v-model:show="innerShow">
        <n-card class="w-[92vw] max-w-[980px] max-h-[80vh] overflow-hidden">
            <template #header>
                <div class="flex flex-col gap-1">
                    <div class="text-base font-semibold">选择图标</div>
                    <div class="text-xs text-gray-500">
                        输入关键词搜索 Iconify 图标，点击即可应用
                    </div>
                </div>
            </template>
            <template #header-extra>
                <Icon
                    class="cursor-pointer h-4 w-4"
                    icon="icon-park-outline:close"
                    v-on:click="innerShow = false"
                />
            </template>
            <template #default>
                <div class="space-y-3">
                    <div class="flex flex-col sm:flex-row sm:items-center gap-2">
                        <n-input
                            v-model:value="query"
                            class="iconify-search-input w-full sm:flex-1"
                            clearable
                            placeholder="搜索图标，如 money / food / wallet"
                            v-on:keydown.enter.prevent="performSearch(true)"
                        >
                            <template #prefix>
                                <Icon class="text-gray-500" icon="ri:search-line"/>
                            </template>
                        </n-input>
                        <div class="flex items-center gap-2 flex-wrap">
                            <n-button :loading="isLoading" class="w-full sm:w-auto" type="primary"
                                      v-on:click="performSearch(true)">
                                搜索
                            </n-button>
                            <n-button class="w-full sm:w-auto" secondary v-on:click="clearSearch">
                                清空
                            </n-button>
                            <n-button class="w-full sm:w-auto" tertiary v-on:click="selectIcon('')">
                                不使用图标
                            </n-button>
                        </div>
                    </div>
                    <div class="text-xs text-gray-500">
                        结果来自 Iconify Search API，仅支持英文关键词。每次最多显示 {{ limit }} 条。
                    </div>
                    <n-spin :show="isLoading">
                        <n-scrollbar class="max-h-60 lg:max-h-[420px] pr-2">
                            <div
                                v-if="!isLoading && icons.length === 0"
                                class="text-center text-sm text-gray-500 py-8"
                            >
                                暂无结果
                            </div>
                            <div v-else class="grid grid-cols-6 sm:grid-cols-8 md:grid-cols-10 lg:grid-cols-12 gap-2">
                                <button
                                    v-for="iconName in icons"
                                    v-bind:key="iconName"
                                    class="group rounded-lg border border-gray-200 bg-white p-2 flex flex-col items-center gap-1 hover:border-primary"
                                    v-on:click="selectIcon(iconName)"
                                >
                                    <Icon :icon="iconName" class="text-primary w-6 h-6"/>
                                    <div class="text-[10px] text-gray-500 truncate w-full text-center">
                                        {{ iconName }}
                                    </div>
                                </button>
                            </div>
                        </n-scrollbar>
                    </n-spin>
                    <div class="flex items-center justify-between">
                        <div class="text-xs text-gray-500">已显示 {{ icons.length }} 个</div>
                        <n-button
                            v-if="hasMore"
                            :loading="isLoading"
                            secondary
                            size="small"
                            v-on:click="loadMore"
                        >
                            加载更多
                        </n-button>
                    </div>
                </div>
            </template>
        </n-card>
    </n-modal>
</template>

<script lang="ts" setup>
import {computed, ref, watch} from "vue";
import {Icon} from "@iconify/vue";

const props = defineProps({
    show: {type: Boolean, required: true, default: false}
});

const emit = defineEmits(["update:show", "select"]);

const innerShow = computed({
    get: () => props.show,
    set: (value: boolean) => emit("update:show", value)
});

const query = ref("");
const icons = ref<string[]>([]);
const isLoading = ref(false);
const hasMore = ref(false);
const offset = ref(0);
const limit = 120;

async function performSearch(reset: boolean): Promise<void> {
    const keyword = query.value.trim();
    if (!keyword) {
        icons.value = [];
        hasMore.value = false;
        offset.value = 0;
        return;
    }
    if (reset) {
        icons.value = [];
        offset.value = 0;
    }
    isLoading.value = true;
    try {
        const url = `https://api.iconify.design/search?query=${encodeURIComponent(keyword)}&limit=${limit}&offset=${offset.value}`;
        const res = await fetch(url);
        const data = await res.json();
        const list = Array.isArray(data?.icons) ? data.icons : [];
        icons.value = reset ? list : icons.value.concat(list);
        offset.value += list.length;
        if (typeof data?.total === "number") {
            hasMore.value = offset.value < data.total;
        } else {
            hasMore.value = list.length === limit;
        }
    } catch (_err) {
    } finally {
        isLoading.value = false;
    }
}

function clearSearch(): void {
    query.value = "";
    icons.value = [];
    offset.value = 0;
    hasMore.value = false;
}

function loadMore(): void {
    if (!isLoading.value) {
        performSearch(false);
    }
}

function selectIcon(iconName: string): void {
    emit("select", iconName);
    innerShow.value = false;
}

watch(
    () => props.show,
    (value: boolean) => {
        if (value) {
            if (query.value.trim()) {
                performSearch(true);
            }
        } else {
            isLoading.value = false;
        }
    }
);
</script>
<style scoped>
:deep(.iconify-search-input .n-input-wrapper) {
    align-items: center;
    min-height: 40px;
}

:deep(.iconify-search-input .n-input__input) {
    height: 40px;
    line-height: 40px;
}
</style>

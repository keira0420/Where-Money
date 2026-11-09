<template>
    <dark-mode-container
        class="global-header flex items-center flex-nowrap h-full px-2 overflow-hidden"
    >
        <!-- 左侧：菜单/面包屑（移动端可以隐藏面包屑避免挤） -->
        <div class="flex items-center flex-1 min-w-0 gap-2">
            <global-logo
                v-if="showLogo"
                :show-title="true"
                :style="{ width: theme.sider.width + 'px' }"
                class="h-full shrink-0 hidden xl:flex lg:flex"
            />

            <div v-if="!showHeaderMenu" class="flex items-center flex-1 min-w-0">
                <menu-collapse v-if="showMenuCollape" class="shrink-0"/>
                <!-- 面包屑移动端先隐藏，避免把右侧挤没 -->
                <global-breadcrumb v-if="theme.header.crumb.visible" class="xl:flex lg:flex min-w-0"/>
            </div>

            <div
                v-else
                :style="{ justifyContent: theme.menu.horizontalPosition }"
                class="flex items-center flex-1 min-w-0"
            >
                <header-menu class="min-w-0"/>
            </div>
        </div>

        <!-- 右侧：账本 + 日期 + 退出（不换行） -->
        <div class="flex items-center gap-2 shrink-0">
            <n-select
                v-model:value="bookId"
                :consistent-menu-width="false"
                :options="bookList as any"
                class="w-30 xl:w-50 lg:w-50"
                size="small"
                @update:value="changeBook"
            >
                <template #action>
                    <n-button class="w-full xl:w-auto lg:w-auto" @click.stop="addBook">
                        <Icon icon="material-symbols:add"/>
                        <span class="hidden xl:inline lg:inline">添加账本</span>
                        <span class="xl:hidden lg:hidden">新增</span>
                    </n-button>
                    <n-button class="w-full xl:w-auto mt-2 xl:mt-0 xl:ml-2 lg:w-auto lg:mt-0 lg:ml-2"
                              @click.stop="showDeleteModal = true">
                        <Icon icon="material-symbols:delete"/>
                        <span class="hidden xl:inline lg:inline">删除账本</span>
                        <span class="xl:hidden lg:hidden">删除</span>
                    </n-button>
                    <n-modal v-model:show="showDeleteModal" class="w-4/5 xl:w-1/6 lg:w-1/4">
                        <n-card class="w-full">
                            <template #header>
                                <div>删除账本</div>
                            </template>
                            <template #default>
                                <div>确定删除账本 {{ bookList.find((item) => item.id === bookId)?.title }} 吗？</div>
                                <div
                                    class="mt-3 flex items-center gap-2 rounded-md border border-yellow-200 bg-yellow-50 px-3 py-2 text-yellow-700">
                                    <Icon class="text-18px" icon="material-symbols:warning-rounded"/>
                                    <span class="text-sm">该操作不可恢复</span>
                                </div>
                            </template>
                            <template #footer>
                                <n-button class="w-full" type="default" @click="deleteBook">确定</n-button>
                                <n-button class="w-full mt-2" type="primary" @click="showDeleteModal = false">取消
                                </n-button>
                            </template>
                        </n-card>
                    </n-modal>
                    <n-modal v-model:show="showAdd" :mask-closable="false" class="w-4/5 xl:w-1/6 lg:w-1/4">
                        <n-card id="drawer-target" class="w-full">
                            <template #header>
                                <div>新增账本</div>
                            </template>
                            <template #header-extra>
                                <Icon
                                    class="cursor-pointer h-4 w-4"
                                    icon="icon-park-outline:close"
                                    v-bind:class="{ 'text-primary': mouseOnClose }"
                                    v-on:click="showAdd = false"
                                    v-on:mouseenter="mouseOnClose = true"
                                    v-on:mouseleave="mouseOnClose = false"
                                />
                            </template>

                            <template #default>
                                <n-form ref="formRef" :model="model" :rules="rules">
                                    <n-form-item label="账本名称：" path="title">
                                        <n-input v-model:value="model.title" @keydown.enter.prevent/>
                                    </n-form-item>
                                    <n-form-item label="开始日期：" path="beginDate">
                                        <n-input v-model:value="model.beginDate" @keydown.enter.prevent/>
                                    </n-form-item>
                                </n-form>

                                <n-button class="w-full addButton" type="primary" v-on:click="submitBook">
                                    <template #default>保存</template>
                                </n-button>
                            </template>
                        </n-card>
                    </n-modal>
                </template>
            </n-select>

            <div class="flex items-center gap-1 whitespace-nowrap">
                <div class="flex items-center gap-2">
                    <Icon :icon="'ri:calendar-2-fill'" class="text-16px"/>
                    <!-- 手机端只显示 MM-DD，桌面显示 YYYY-MM-DD -->
                    <span class="text-sm xl:hidden lg:hidden">{{ nowDate.slice(5) }}</span>
                    <span class="text-base hidden xl:inline lg:inline">{{ nowDate }}</span>
                </div>
                <hover-container class="w-8 h-full" tooltip-content="退出" @click="showExitModal = true">
                    <Icon :icon="'ri:logout-box-r-line'" class="text-16px"/>
                </hover-container>
            </div>
        </div>
    </dark-mode-container>

    <div>
        <n-modal v-model:show="showExitModal">
            <div class="s-card p-8 space-y-7">
                <div class="s-title s-underline text-lg">提示</div>
                <div class="mx-auto text-lg">即将退出登录，是否继续？</div>
                <div class="flex items-center gap-6 justify-end">
                    <n-button @click="ExitLogin">确认</n-button>
                    <n-button type="primary" @click="showExitModal = false">返回</n-button>
                </div>
            </div>
        </n-modal>
    </div>
</template>


<script lang="ts" setup>
import {defineComponent, onMounted, reactive, Ref, ref} from "vue";
import {Router} from "vue-router";
import {ExitOutline} from "@vicons/ionicons5";
import {DarkModeContainer, HoverContainer} from "@/components";
import {useThemeStore} from "@/store";
import {useRouterPush} from "@/composables";
import {storage} from "@/utils";
import type {Book, BookGetAllBookResponse, GlobalHeaderProps} from "@/interface";
import {useStore} from "@/stores/store";
import GlobalLogo from "../GlobalLogo/index.vue";
import {GlobalBreadcrumb, HeaderMenu, MenuCollapse} from "./components";
import {Icon} from "@iconify/vue";
import {dateToString, now} from "@/utils/dateComputer";
import {addBookApi, deleteBookApi, getAllBookApi} from "@/apis";
import {EnumStorageKey} from "@/enum";

const {routerPush, routerBack} = useRouterPush();
defineProps<{
    showLogo: GlobalHeaderProps["showLogo"];
    showHeaderMenu: GlobalHeaderProps["showHeaderMenu"];
    showMenuCollape: GlobalHeaderProps["showMenuCollape"];
}>();
const theme = useThemeStore();
const store = useStore();
const showExitModal: Ref<boolean> = ref(false);
defineComponent({
    components: {
        ExitOutline
    }
});
declare const window: Window & { $router: Router; $message: any; };

function goPre(): void {
    routerBack();
}

function ExitLogin(): void {
    storage.remove(EnumStorageKey["token"]);
    storage.remove(EnumStorageKey["bookId"]);
    store.bookId = 0;
    routerPush({name: "login"});
}

let nowDate: Ref<string> = ref(dateToString(now()).split(" ")[0]);
let bookId: Ref<number> = ref(0);

interface BookShow extends Book {
    value: number;
    label: string;
}

let bookList: Ref<Array<BookShow>> = ref([]);
onMounted(() => {
    bookId.value = storage.get(EnumStorageKey["bookId"]) || 0;
    getAllBookApi().then((res: BookGetAllBookResponse) => {
        bookList.value = res.bookList.map((item: Book) => {
            return {
                ...item,
                value: item.id,
                label: item.title
            };
        });
        if (bookId.value === 0) {
            bookId.value = bookList.value[0].value;
            storage.set(EnumStorageKey["bookId"], bookId.value);
        }
        store.bookId = bookId.value;
    });
});

function changeBook(value: number): void {
    bookId.value = value;
    storage.set(EnumStorageKey["bookId"], value);
    store.bookId = value;
}

let showAdd: Ref<boolean> = ref(false);
let mouseOnClose: Ref<boolean> = ref(false);

function addBook(): void {
    showAdd.value = true;
}

interface addData {
    title: string;
    beginDate: string;
}

const model = reactive<addData>({
    title: "",
    beginDate: "1"
});
const formRef = ref();
const rules = {
    title: {
        required: true,
        message: "请输入名称",
        trigger: "blur"
    },
    beginDate: {
        required: true,
        message: "请选择账单起始日",
        trigger: "blur"
    }
};

function submitBook(): void {
    formRef.value?.validate((errors: any) => {
        if (!errors) {
            addBookApi({title: model.title, beginDate: parseInt(model.beginDate)}).then(() => {
                window.$message.success("新增成功");
                showAdd.value = false;
            });
        }
    });
}

let showDeleteModal: Ref<boolean> = ref(false);

function deleteBook(): void {
    if (bookList.value.length === 1) {
        window.$message.error("至少保留一个账本");
        return;
    }
    deleteBookApi({id: bookId.value}).then(() => {
        window.$message.success("删除成功");
        bookList.value = bookList.value.filter((item: BookShow) => item.value !== bookId.value);
        bookId.value = bookList.value[0].value;
        storage.set(EnumStorageKey["bookId"], bookId.value);
        store.bookId = bookId.value;
        showDeleteModal.value = false;
    });
}

defineExpose({ExitLogin, goPre, showExitModal});
</script>
<style scoped>
</style>

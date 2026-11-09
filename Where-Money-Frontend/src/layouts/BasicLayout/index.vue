<template>
    <AdminLayout
        :fixed-footer="theme.footer.fixed"
        :fixed-header-and-tab="theme.fixedHeaderAndTab"
        :header-height="theme.header.height"
        :min-width="theme.layout.minWidth"
        :mode="mode"
        :sider-collapse="app.siderCollapse"
        :sider-collapsed-width="siderCollapsedWidth"
        :sider-visible="siderVisible"
        :sider-width="siderWidth"
        :tab-height="theme.tab.height"
        :tab-visible="theme.tab.visible"
    >
        <template #header>
            <global-header v-bind="headerProps"/>
        </template>
        <template #tab>
            <global-tab/>
        </template>
        <template #sider>
            <global-sider/>
        </template>
        <global-content/>
        <!--        <template #footer>-->
        <!--            <global-footer/>-->
        <!--        </template>-->
    </AdminLayout>
    <setting-drawer/>
</template>

<script lang="ts" setup>
import {onMounted} from "vue";
import {AdminLayout} from "@soybeanjs/vue-materials";
import "@soybeanjs/vue-materials/dist/style.css";
import {useAppStore, useThemeStore} from "@/store";
import {useBasicLayout} from "@/composables";
import {useStore} from "@/stores/store";
import {GlobalContent, GlobalHeader, GlobalSider, GlobalTab, SettingDrawer} from "../common";

const app = useAppStore();
const theme = useThemeStore();
const {mode, headerProps, siderVisible, siderWidth, siderCollapsedWidth} = useBasicLayout();
const store = useStore();
onMounted(() => {
});
</script>
<style scoped></style>

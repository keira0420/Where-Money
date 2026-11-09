import {createApp} from "vue";
import {setupAssets, setupNaiveUI} from "@/plugins";
import {setupRouter} from "@/router";
import {setupStore} from "@/store";
import App from "./App.vue";

async function setupApp() {
    setupAssets();
    const app = createApp(App);
    setupStore(app);
    setupNaiveUI(app);
    await setupRouter(app);
    app.mount("#app");
}

setupApp().then(() => {
});
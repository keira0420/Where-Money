<template>
    <main class="account-page">
        <section class="story">
            <router-link to="/login" class="brand"><span class="mark">W</span> Where Money</router-link>
            <div class="story-content"><div class="eyebrow">PERSONAL FINANCE, MADE CLEAR</div>
                <h1>记下每一笔，<br /><span>看清每一天。</span></h1>
                <p>从随手记录，到理解自己的消费。让数字更清晰，让记账更轻松。</p>
                <div class="features"><div><span>01</span><strong>一句话记账</strong><p>自然语言生成草稿，确认后安全入账。</p></div>
                    <div><span>02</span><strong>预算有分寸</strong><p>账户、分类和周期预算，一处掌握。</p></div>
                    <div><span>03</span><strong>月度看全局</strong><p>真实收支统计，配合 AI 消费解读。</p></div></div>
            </div><div class="story-footer">YOUR MONEY. YOUR CLARITY.</div>
        </section>
        <section class="form-side"><div class="account-form">
            <div class="eyebrow">{{ register ? 'START YOUR JOURNEY' : 'WELCOME BACK' }}</div>
            <h2>{{ register ? '创建你的账本空间' : '欢迎回来' }}</h2>
            <p class="subtitle">{{ register ? '注册后将自动创建默认账本和收支分类。' : '登录后，继续记录你的生活。' }}</p>
            <form @submit.prevent="submit">
                <label for="account-name">账号</label><input id="account-name" v-model="userName" autocomplete="username" :disabled="busy" maxlength="64" placeholder="请输入账号" required />
                <label for="account-password">密码</label><input id="account-password" v-model="password" type="password" :autocomplete="register ? 'new-password' : 'current-password'" :disabled="busy" placeholder="至少8位密码" required />
                <template v-if="register"><label for="account-repeat">确认密码</label><input id="account-repeat" v-model="repeat" type="password" autocomplete="new-password" :disabled="busy" placeholder="请再次输入密码" required /></template>
                <div v-if="error" role="alert" class="error">{{ error }}</div>
                <button class="submit" type="submit" :disabled="busy">{{ busy ? '请稍候…' : register ? '创建账号' : '登录我的账本' }}<span>→</span></button>
            </form>
            <div class="switch">{{ register ? '已经有账号？' : '第一次使用？' }} <router-link :to="register ? '/login' : '/register'">{{ register ? '立即登录' : '创建账号' }}</router-link></div>
            <p class="privacy">密码仅用于身份验证，不在浏览器中明文记住。</p>
        </div></section>
    </main>
</template>
<script setup lang="ts">
import { ref } from "vue";
import { useRouter, useRoute } from "vue-router";
import request from "@/utils/request";
import { storage } from "@/utils";
import { EnumStorageKey } from "@/enum";
const props = defineProps<{ register?: boolean }>();
const userName = ref(""); const password = ref(""); const repeat = ref("");
const busy = ref(false); const error = ref(""); const router = useRouter(); const route = useRoute();
async function submit() {
    if (busy.value) return;
    error.value = "";
    if (props.register && (password.value.length < 8 || password.value !== repeat.value)) {
        error.value = password.value.length < 8 ? "密码至少8位" : "两次密码输入不一致"; return;
    }
    busy.value = true;
    try {
        const data: any = await request({ method: "post", url: props.register ? "/user/register" : "/user/login",
            data: { userName: userName.value.trim(), password: password.value }, skipErrorMessage: true } as any);
        if (props.register) { await router.push("/login"); window.$message?.success("注册成功，请登录"); }
        else {
            storage.set(EnumStorageKey["token"], data.token);
            const redirect = typeof route.query.redirect === "string" && route.query.redirect.startsWith("/") && !route.query.redirect.startsWith("//") ? route.query.redirect : "/assistant";
            await router.push(redirect);
        }
    } catch (e: any) { error.value = e?.message || "服务连接失败，请确认后端已启动"; }
    finally { busy.value = false; }
}
</script>
<style scoped>
.account-page { min-height:100vh; display:grid; grid-template-columns:1.05fr 1fr; background:#f8faf9; color:#182c28; font-family:Inter,"Microsoft YaHei",sans-serif; }
.story { background:#103d33; color:#e6f1e9; padding:38px 52px; display:flex; flex-direction:column; position:relative; overflow:hidden; }
.story::after { content:""; position:absolute; width:460px; height:460px; border:1px solid #ffffff12; border-radius:50%; right:-220px; top:100px; box-shadow:0 0 0 70px #ffffff03,0 0 0 140px #ffffff03; pointer-events:none; }
.brand { display:flex; align-items:center; gap:12px; color:inherit; font-size:19px; font-weight:600; }
.mark { display:grid; place-items:center; background:#b5e1a7; color:#153e32; border-radius:10px; width:37px; height:37px; font-weight:800; }
.story-content { margin:auto 0; padding:60px 0; z-index:1; }
.eyebrow { font-size:11px; letter-spacing:2px; font-weight:650; opacity:.7; }
h1 { font-size:clamp(34px,3.7vw,56px); font-weight:650; line-height:1.4; margin:24px 0; letter-spacing:-1px; }
h1 span { color:#b5e1a7; }
.story-content>p { max-width:390px; line-height:1.9; color:#b6ccc4; }
.features { display:grid; gap:24px; margin-top:45px; }
.features span { color:#a3c69a; font-size:12px; margin-right:14px; }
.features strong { font-size:15px; }
.features p { margin:6px 0 0 33px; font-size:13px; color:#a7bfb6; }
.story-footer { font-size:10px; letter-spacing:3px; opacity:.5; }
.form-side { display:flex; align-items:center; justify-content:center; padding:50px 34px; }
.account-form { width:100%; max-width:360px; }
h2 { font-size:29px; font-weight:650; margin:14px 0 10px; }
.subtitle { color:#7b8983; font-size:13px; margin-bottom:34px; }
form label { display:block; font-size:13px; margin:21px 0 9px; font-weight:550; }
input { width:100%; border:1px solid #dce4de; border-radius:8px; padding:13px 15px; outline:none; background:white; color:#182c28; transition:.2s; }
input:focus { border-color:#3b8068; box-shadow:0 0 0 3px #3b806810; }
.submit { width:100%; padding:14px 18px; display:flex; justify-content:space-between; background:#174f40; color:white; border-radius:8px; margin-top:28px; font-weight:550; cursor:pointer; }
.submit:hover { background:#23634f; }.submit:disabled { opacity:.65; cursor:wait; }
.switch { text-align:center; margin-top:24px; font-size:13px; color:#73827a; }.switch a { color:#206149; margin-left:5px; font-weight:600; }
.privacy { font-size:11px; text-align:center; margin-top:35px; color:#8b978f; }.error { background:#fff0ed; color:#a7352c; border-radius:6px; padding:10px; font-size:13px; margin-top:14px; }
@media(max-width:760px) { .account-page { grid-template-columns:1fr; }.story { padding:25px; }.story-content { padding:30px 0; }.story-content>p,.features,.story-footer { display:none; }h1 { font-size:30px; margin:12px 0 0; }.story-content .eyebrow { display:none; }.form-side { padding:40px 24px; } }
</style>

import {fileURLToPath} from "url";
import {defineConfig} from "vite";
import {define, setupVitePlugins} from "./build";

export default defineConfig(configEnv => {
    return {
        base: "./",
        resolve: {
            alias: {
                "@": fileURLToPath(new URL("./src", import.meta.url)),
                "~": fileURLToPath(new URL("./", import.meta.url))
            }
        },
        define,
        plugins: setupVitePlugins(configEnv),
        css: {
            preprocessorOptions: {
                scss: {
                    additionalData: `@use "./src/styles/scss/global.scss" as *;`
                }
            }
        },
        assetsInclude: ["./public/**"],
        server: {
            fs: {
                strict: false
            },
            host: "::",
            port: 13140,
            proxy: {
                "/api": { target: "http://127.0.0.1:5050", changeOrigin: true }
            },
            open: false
        },
        build: {
            brotliSize: false,
            sourcemap: false,
            minify: 'terser',
            terserOptions: {
                compress: {
                    drop_console: true,
                    drop_debugger: true,
                }
            }
        }
    };
});

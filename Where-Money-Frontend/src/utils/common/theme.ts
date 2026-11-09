import {EnumStorageKey} from "@/enum";
import {ThemeLayoutMode} from "@/interface";

/**
 * 缓存主题颜色
 * @param color
 */
export function setThemeColor(color: string): void {
    window.localStorage.setItem(EnumStorageKey["theme-color"], color);
}

/**
 * 获取缓存的主题颜色
 */
export function getThemeColor(): string | null {
    return window.localStorage.getItem(EnumStorageKey["theme-color"]);
}

export function setLayout(layout: ThemeLayoutMode): void {
    window.localStorage.setItem(EnumStorageKey["layout"], layout);
}

export function getLayout(): ThemeLayoutMode | null {
    return window.localStorage.getItem(EnumStorageKey["layout"]) as ThemeLayoutMode;
}
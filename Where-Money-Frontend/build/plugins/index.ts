import type {ConfigEnv, PluginOption} from 'vite';
import vue from './vue';
import html from './html';
import iconify from './iconify';
import windicss from './windicss';
import svg from './svg';

export function setupVitePlugins(configEnv: ConfigEnv): (PluginOption | PluginOption[])[] {
    return [vue, html(configEnv), ...iconify, windicss, svg];
}
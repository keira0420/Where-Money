import {AxiosRequestConfig} from "axios";
import request from "./request";

const http = {
    get(url: string, params: any) {
        const config: AxiosRequestConfig = {
            method: "get",
            url
        };
        if (params) config.params = params;
        return request(config);
    },
    post(url: string, params: any) {
        const config: AxiosRequestConfig = {
            method: "post",
            url
        };
        if (params) config.data = params;
        return request(config);
    },
    patch(url: string, params: any) {
        const config: AxiosRequestConfig = {
            method: "patch",
            url
        };
        if (params) config.data = params;
        return request(config);
    },
    put(url: string, params: any) {
        const config: AxiosRequestConfig = {
            method: "put",
            url
        };
        if (params) config.params = params;
        return request(config);
    },
    delete(url: string, params: any) {
        const config: AxiosRequestConfig = {
            method: "delete",
            url
        };
        if (params) config.params = params;
        return request(config);
    }
};
export default http;
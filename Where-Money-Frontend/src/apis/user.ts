import http from "@/utils/http";

function registerApi(params: any): any {
    return http.post("/user/register", params);
}

function loginApi(params: any): any {
    return http.post("/user/login", params);
}

function getProtocolApi(): any {
    return http.get("/user/protocol", null);
}

function changeUserNameApi(params: any): any {
    return http.patch("/user/user-name", params);
}

function changePasswordApi(params: any): any {
    return http.patch("/user/password", params);
}

export {registerApi, loginApi, getProtocolApi, changeUserNameApi, changePasswordApi};

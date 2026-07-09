import request from "@/utils/request.js";

export function getProfile() {
    return request({ url: '/profile', method: 'get' })
}
export function updateProfile(data) {
    return request({ url: '/profile', method: 'put', data })
}
export function changePassword(data) {
    return request({ url: '/profile/password', method: 'put', data })
}

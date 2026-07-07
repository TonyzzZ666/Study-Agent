import request from "@/utils/request.js";

export function getTaskList(params) {
    return request({ url: '/tasks', method: 'get', params })
}
export function createTask(data) {
    return request({ url: '/tasks', method: 'post', data })
}
export function updateTask(id, data) {
    return request({ url: `/tasks/${id}`, method: 'put', data })
}
export function deleteTask(id) {
    return request({ url: `/tasks/${id}`, method: 'delete' })
}
export function getUpcoming() {
    return request({ url: '/tasks/upcoming', method: 'get' })
}

import request from "@/utils/request.js";

export function getTaskList(params) {
    return request({ url: '/api/tasks', method: 'get', params })
}
export function createTask(data) {
    return request({ url: '/api/tasks', method: 'post', data })
}
export function updateTask(id, data) {
    return request({ url: `/api/tasks/${id}`, method: 'put', data })
}
export function deleteTask(id) {
    return request({ url: `/api/tasks/${id}`, method: 'delete' })
}
export function getUpcoming() {
    return request({ url: '/api/tasks/upcoming', method: 'get' })
}

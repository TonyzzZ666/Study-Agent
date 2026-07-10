import request from "@/utils/request.js";

export function getCountdowns() { return request({ url: '/countdown', method: 'get' }) }
export function createCountdown(data) { return request({ url: '/countdown', method: 'post', data }) }
export function deleteCountdown(id) { return request({ url: `/countdown/${id}`, method: 'delete' }) }
export function getAvailableTasks() { return request({ url: '/countdown/available-tasks', method: 'get' }) }

import request from "@/utils/request.js";

export function doCheckIn(taskId) {
    return request({ url: `/api/checkin/${taskId}`, method: 'post' })
}
export function getCheckInHistory() {
    return request({ url: '/api/checkin/history', method: 'get' })
}
export function getCalendarData(year, month) {
    return request({ url: '/api/checkin/calendar', method: 'get', params: { year, month } })
}

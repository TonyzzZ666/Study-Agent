import request from "@/utils/request.js";

export function doCheckIn(taskId) {
    return request({ url: `/checkin/${taskId}`, method: 'post' })
}
export function completeTask(taskId) {
    return request({ url: `/checkin/complete/${taskId}`, method: 'put' })
}
export function getTodayCheckIns() {
    return request({ url: '/checkin/today', method: 'get' })
}
export function getCheckInHistory() {
    return request({ url: '/checkin/history', method: 'get' })
}
export function getCalendarData(year, month) {
    return request({ url: '/checkin/calendar', method: 'get', params: { year, month } })
}
export function getYearCalendar(fromYear, fromMonth, months) {
    return request({ url: '/checkin/year-calendar', method: 'get', params: { fromYear, fromMonth, months } })
}

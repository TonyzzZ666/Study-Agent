import request from "@/utils/request.js";

export function getCalendarTasks(year, month) {
    return request({ url: '/calendar/tasks', method: 'get', params: { year, month } })
}

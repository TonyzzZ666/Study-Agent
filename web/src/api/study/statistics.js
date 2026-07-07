import request from "@/utils/request.js";

export function getStatistics() {
    return request({ url: '/statistics', method: 'get' })
}

import request from "@/utils/request.js";

export function getStatistics() {
    return request({ url: '/api/statistics', method: 'get' })
}

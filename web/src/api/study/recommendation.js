import request from "@/utils/request.js";

export function getRecommendations() {
    return request({ url: '/recommendations', method: 'get' })
}

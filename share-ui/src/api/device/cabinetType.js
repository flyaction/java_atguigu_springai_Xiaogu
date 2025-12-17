import request from '@/utils/request'

// 查询柜机类型列表
export function listCabinetType(query) {
    return request({
        url: '/device/cabinetType/list',
        method: 'get',
        params: query
    })
}

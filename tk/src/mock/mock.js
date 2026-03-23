import Mock from 'mockjs'
//延时200-600毫秒请求到数据
Mock.setup({
    timeout: '200-600'
})

const Random = Mock.Random;
// 广东省运维人员统计
function countUserNum() {
    const a = Mock.mock({
        success: true,
        data: {
            offlineNum:'@integer(25, 35)', // AI自动处理覆盖率 - 广东省配网通信系统
            lockNum: '@integer(280, 320)', // 运维人员总数 - 广东省配网通信运维团队
            totalNum: 298 // 总人数 - 广东省配网通信运维人员总数
        }
    })
    a.data.onlineNum = '@integer(15, 25)' // 正在处理告警人数 - 广东省配网通信告警处理人员
    return a
}

// 接口，第一个参数url，第二个参数请求类型，第三个参数响应回调
Mock.mock(new RegExp('countUserNum'), 'get', countUserNum)

// 广东省自愈设备统计

function countDeviceNum() {
    const a = Mock.mock({
        success: true,
        data: {
            alarmNum: '@integer(15, 35)', // 告警设备数 - 广东省配网通信告警设备
            offlineNum: '@integer(25, 45)', // 自愈中设备数 - 广东省配网通信自愈设备
            totalNum: 1248 // 总设备数 - 广东省配网通信设备总数
        }
    })
    a.data.onlineNum = a.data.totalNum - a.data.offlineNum - a.data.alarmNum // 正常运行设备数 - 广东省配网通信正常运行设备


    return a
}

Mock.mock(new RegExp('countDeviceNum'), 'get', countDeviceNum)

// 场景触发记录 - 广东省配网通信自愈场景

function sbtx() {
    const selfHealingActions = ['光旁路切换', '光缆替换', '电源切换', '路由重定向', '设备重启', '补环激活', '负载均衡', '故障隔离'];
    const guangdongCities = [
        '广州市', '深圳市', '珠海市', '汕头市', '佛山市', '韶关市', '湛江市', '肇庆市', 
        '江门市', '茂名市', '惠州市', '梅州市', '汕尾市', '河源市', '阳江市', '清远市', 
        '东莞市', '中山市', '潮州市', '揭阳市', '云浮市'
    ];
    const guangdongDistricts = {
        '广州市': ['天河区', '越秀区', '荔湾区', '海珠区', '白云区', '黄埔区', '番禺区', '花都区'],
        '深圳市': ['罗湖区', '福田区', '南山区', '宝安区', '龙岗区', '盐田区'],
        '珠海市': ['香洲区', '斗门区', '金湾区'],
        '汕头市': ['龙湖区', '金平区', '濠江区', '潮阳区'],
        '佛山市': ['禅城区', '南海区', '顺德区', '三水区'],
        '韶关市': ['武江区', '浈江区', '曲江区', '始兴县'],
        '湛江市': ['赤坎区', '霞山区', '坡头区', '麻章区'],
        '肇庆市': ['端州区', '鼎湖区', '高要区', '广宁县'],
        '江门市': ['蓬江区', '江海区', '新会区', '台山市'],
        '茂名市': ['茂南区', '电白区', '高州市', '化州市'],
        '惠州市': ['惠城区', '惠阳区', '博罗县', '惠东县'],
        '梅州市': ['梅江区', '梅县区', '大埔县', '丰顺县'],
        '汕尾市': ['城区', '海丰县', '陆河县', '陆丰市'],
        '河源市': ['源城区', '紫金县', '龙川县', '连平县'],
        '阳江市': ['江城区', '阳东区', '阳西县', '阳春市'],
        '清远市': ['清城区', '清新区', '佛冈县', '阳山县'],
        '东莞市': ['莞城街道', '南城街道', '东城街道', '万江街道'],
        '中山市': ['石岐街道', '东区街道', '火炬开发区', '西区街道'],
        '潮州市': ['湘桥区', '潮安区', '饶平县'],
        '揭阳市': ['榕城区', '揭东区', '揭西县', '惠来县'],
        '云浮市': ['云城区', '云安区', '新兴县', '郁南县']
    };
    
    const a = Mock.mock({
        success: true,
        data: {
            "list|20": [
                {
                    provinceName: "广东省",
                    cityName: "@pick(" + JSON.stringify(guangdongCities) + ")",
                    countyName: "@pick(" + JSON.stringify([
                        '天河区', '越秀区', '荔湾区', '海珠区', '白云区', '黄埔区', '番禺区', '花都区',
                        '罗湖区', '福田区', '南山区', '宝安区', '龙岗区', '盐田区',
                        '香洲区', '斗门区', '金湾区',
                        '龙湖区', '金平区', '濠江区', '潮阳区',
                        '禅城区', '南海区', '顺德区', '三水区',
                        '武江区', '浈江区', '曲江区', '始兴县',
                        '赤坎区', '霞山区', '坡头区', '麻章区',
                        '端州区', '鼎湖区', '高要区', '广宁县',
                        '蓬江区', '江海区', '新会区', '台山市',
                        '茂南区', '电白区', '高州市', '化州市',
                        '惠城区', '惠阳区', '博罗县', '惠东县',
                        '梅江区', '梅县区', '大埔县', '丰顺县',
                        '城区', '海丰县', '陆河县', '陆丰市',
                        '源城区', '紫金县', '龙川县', '连平县',
                        '江城区', '阳东区', '阳西县', '阳春市',
                        '清城区', '清新区', '佛冈县', '阳山县',
                        '莞城街道', '南城街道', '东城街道', '万江街道',
                        '石岐街道', '东区街道', '火炬开发区', '西区街道',
                        '湘桥区', '潮安区', '饶平县',
                        '榕城区', '揭东区', '揭西县', '惠来县',
                        '云城区', '云安区', '新兴县', '郁南县'
                    ]) + ")",
                    createTime: "@datetime('yyyy-MM-dd HH:mm:ss')",
                    deviceId: "6c512d754bbcd6d7cd86abce0e0cac58",
                    "gatewayno|+1": 10000,
                    "healingResult|1": [0, 1], // 0-失败, 1-成功
                    "selfHealingAction|1": selfHealingActions,
                }
            ]
        }
    })
    return a
}

Mock.mock(new RegExp('sbtx'), 'get', sbtx)



//十大自愈场景分布图

function centermap(options) {
    let params = parameteUrl(options.url)
    if (params.regionCode && params.regionCode != 'china') {
        // 广东省各地市配网通信自愈场景分布数据
        if (params.regionCode === '440000') {
            const guangdongCities = [
                { 
                    name: '广州市', 
                    value: 156, 
                    scenario: '光旁路交换机应用', 
                    location: '省会城市，核心枢纽',
                    networkInfo: '配网通信节点：2,456个，光缆长度：8,234km',
                    selfHealingRate: '98.5%',
                    keyEquipment: '核心交换机、光传输设备、智能终端'
                },
                { 
                    name: '深圳市', 
                    value: 142, 
                    scenario: '单链补环场景', 
                    location: '经济特区，创新中心',
                    networkInfo: '配网通信节点：2,189个，光缆长度：7,156km',
                    selfHealingRate: '99.2%',
                    keyEquipment: '智能光开关、环网保护设备、通信网关'
                },
                { 
                    name: '珠海市', 
                    value: 128, 
                    scenario: '光缆自愈场景', 
                    location: '海滨城市，重要港口',
                    networkInfo: '配网通信节点：1,876个，光缆长度：5,432km',
                    selfHealingRate: '97.8%',
                    keyEquipment: '海底光缆、防腐蚀设备、智能监测系统'
                },
                { 
                    name: '汕头市', 
                    value: 135, 
                    scenario: '电源自愈场景', 
                    location: '经济特区，粤东中心',
                    networkInfo: '配网通信节点：1,654个，光缆长度：4,987km',
                    selfHealingRate: '96.9%',
                    keyEquipment: 'UPS电源、蓄电池组、智能配电系统'
                },
                { 
                    name: '佛山市', 
                    value: 118, 
                    scenario: '路由切换场景', 
                    location: '制造业重镇，广佛同城',
                    networkInfo: '配网通信节点：1,987个，光缆长度：6,234km',
                    selfHealingRate: '98.1%',
                    keyEquipment: '多路径路由器、负载均衡器、智能调度系统'
                },
                { 
                    name: '韶关市', 
                    value: 124, 
                    scenario: '设备老化预测', 
                    location: '粤北门户，工业基地',
                    networkInfo: '配网通信节点：1,234个，光缆长度：3,876km',
                    selfHealingRate: '95.6%',
                    keyEquipment: '老化监测设备、预测维护系统、智能传感器'
                },
                { 
                    name: '湛江市', 
                    value: 147, 
                    scenario: '网络拥塞自愈', 
                    location: '粤西中心，港口城市',
                    networkInfo: '配网通信节点：1,543个，光缆长度：4,567km',
                    selfHealingRate: '97.3%',
                    keyEquipment: '流量控制设备、拥塞检测器、智能分流系统'
                },
                { 
                    name: '肇庆市', 
                    value: 98, 
                    scenario: '通信中断自愈', 
                    location: '历史文化名城，西江流域',
                    networkInfo: '配网通信节点：1,087个，光缆长度：3,234km',
                    selfHealingRate: '96.4%',
                    keyEquipment: '备用通信链路、自动切换设备、故障检测系统'
                },
                { 
                    name: '江门市', 
                    value: 112, 
                    scenario: '光旁路交换机应用', 
                    location: '侨乡，珠三角西翼',
                    networkInfo: '配网通信节点：1,345个，光缆长度：4,123km',
                    selfHealingRate: '97.9%',
                    keyEquipment: '旁路交换机、光分路器、智能切换装置'
                },
                { 
                    name: '茂名市', 
                    value: 89, 
                    scenario: '单链补环场景', 
                    location: '石化基地，粤西重镇',
                    networkInfo: '配网通信节点：987个，光缆长度：2,876km',
                    selfHealingRate: '95.8%',
                    keyEquipment: '环网保护器、补环设备、智能监控系统'
                },
                { 
                    name: '惠州市', 
                    value: 134, 
                    scenario: '光缆自愈场景', 
                    location: '电子信息产业基地',
                    networkInfo: '配网通信节点：1,456个，光缆长度：4,567km',
                    selfHealingRate: '98.7%',
                    keyEquipment: '自愈光缆、智能熔接机、故障定位系统'
                },
                { 
                    name: '梅州市', 
                    value: 76, 
                    scenario: '电源自愈场景', 
                    location: '客家文化中心，山区城市',
                    networkInfo: '配网通信节点：876个，光缆长度：2,345km',
                    selfHealingRate: '94.2%',
                    keyEquipment: '山区专用电源、防雷设备、远程监控系统'
                },
                { 
                    name: '汕尾市', 
                    value: 67, 
                    scenario: '路由切换场景', 
                    location: '革命老区，沿海城市',
                    networkInfo: '配网通信节点：654个，光缆长度：1,987km',
                    selfHealingRate: '93.8%',
                    keyEquipment: '沿海防护设备、智能路由器、环境监测系统'
                },
                { 
                    name: '河源市', 
                    value: 82, 
                    scenario: '设备老化预测', 
                    location: '万绿湖，生态城市',
                    networkInfo: '配网通信节点：765个，光缆长度：2,123km',
                    selfHealingRate: '95.1%',
                    keyEquipment: '生态友好设备、老化预警系统、绿色能源设备'
                },
                { 
                    name: '阳江市', 
                    value: 95, 
                    scenario: '网络拥塞自愈', 
                    location: '刀剪之都，滨海城市',
                    networkInfo: '配网通信节点：876个，光缆长度：2,456km',
                    selfHealingRate: '96.7%',
                    keyEquipment: '滨海防护设备、智能流量控制器、盐雾防护系统'
                },
                { 
                    name: '清远市', 
                    value: 88, 
                    scenario: '通信中断自愈', 
                    location: '广州后花园，生态旅游',
                    networkInfo: '配网通信节点：789个，光缆长度：2,234km',
                    selfHealingRate: '94.9%',
                    keyEquipment: '旅游区专用设备、备用通信系统、环境适应设备'
                },
                { 
                    name: '东莞市', 
                    value: 145, 
                    scenario: '光旁路交换机应用', 
                    location: '制造业名城，世界工厂',
                    networkInfo: '配网通信节点：2,123个，光缆长度：7,234km',
                    selfHealingRate: '99.1%',
                    keyEquipment: '工业级交换机、高可靠性设备、智能制造通信系统'
                },
                { 
                    name: '中山市', 
                    value: 103, 
                    scenario: '单链补环场景', 
                    location: '伟人故里，灯饰之都',
                    networkInfo: '配网通信节点：1,234个，光缆长度：3,876km',
                    selfHealingRate: '97.6%',
                    keyEquipment: '灯饰产业专用设备、智能补环系统、产业通信网络'
                },
                { 
                    name: '潮州市', 
                    value: 78, 
                    scenario: '光缆自愈场景', 
                    location: '历史文化名城，陶瓷之都',
                    networkInfo: '配网通信节点：678个，光缆长度：1,876km',
                    selfHealingRate: '93.4%',
                    keyEquipment: '陶瓷产业专用设备、文化保护通信系统、传统工艺通信设备'
                },
                { 
                    name: '揭阳市', 
                    value: 91, 
                    scenario: '电源自愈场景', 
                    location: '玉都，粤东重要城市',
                    networkInfo: '配网通信节点：789个，光缆长度：2,345km',
                    selfHealingRate: '95.3%',
                    keyEquipment: '玉石产业专用电源、智能配电系统、产业保护设备'
                },
                { 
                    name: '云浮市', 
                    value: 65, 
                    scenario: '路由切换场景', 
                    location: '石材之都，山区城市',
                    networkInfo: '配网通信节点：567个，光缆长度：1,654km',
                    selfHealingRate: '92.7%',
                    keyEquipment: '石材产业专用设备、山区通信系统、产业专用路由器'
                }
            ];
            const a = {
                success: true,
                data: {
                    dataList: guangdongCities,
                    regionCode: '440000',
                }
            }
            return a
        } else {
            const a = Mock.mock({
                success: true,
                data: {
                    "dataList|30": [
                        {
                            name: "@city()",
                            value: '@integer(1, 1000)'
                        }
                    ],
                    regionCode: params.regionCode,//-代表中国
                }
            })
            return a
        }
    } else {
        // 十大自愈场景分布
        const scenarios = [
            { name: '云南', value: 156, scenario: '光旁路交换机应用' },
            { name: '安徽', value: 142, scenario: '单链补环场景' },
            { name: '河北', value: 128, scenario: '光缆自愈场景' },
            { name: '江苏', value: 135, scenario: '电源自愈场景' },
            { name: '浙江', value: 118, scenario: '路由切换场景' },
            { name: '山东', value: 124, scenario: '设备老化预测' },
            { name: '广东', value: 147, scenario: '网络拥塞自愈' },
            { name: '四川', value: 98, scenario: '通信中断自愈' }
        ];
        const a = {
            success: true,
            data: {
                dataList: scenarios,
                regionCode: 'china',
            }
        }
        return a
    }

}

Mock.mock(new RegExp('centermap'), 'get', centermap)

// 广东省自愈前后告警对比

function alarmNum() {
    const a = Mock.mock({
        success: true,
        data: {
            dateList:['2024-01', '2024-02', '2024-03', '2024-04', '2024-05',"2024-06"],
            "numList|6":[
                '@integer(1200, 1800)' // 广东省配网通信原始告警数
            ],
            "numList2|6":[
                '@integer(80, 300)' // 广东省配网通信自愈后剩余告警数
            ]
        }
    })
    return a
}
Mock.mock(new RegExp('alarmNum'), 'get', alarmNum)

// 广东省自愈场景关键指标

function ssyj() {
    const guangdongCities = [
        '广州市', '深圳市', '珠海市', '汕头市', '佛山市', '韶关市', '湛江市', '肇庆市', 
        '江门市', '茂名市', '惠州市', '梅州市', '汕尾市', '河源市', '阳江市', '清远市', 
        '东莞市', '中山市', '潮州市', '揭阳市', '云浮市'
    ];
    
    const a = Mock.mock({
        success: true,
        data: {
            "list|8": [{
                scenarioName: "@pick(['光旁路切换', '光缆自愈', '电源切换', '路由重定向', '设备重启', '补环激活', '负载均衡', '故障隔离'])",
                keyIndicator: "@pick(['切换次数', '自愈成功率', '重定向次数', '重启次数', '激活次数', '均衡次数', '隔离次数'])",
                indicatorValue: "@float(50, 800)", // 广东省配网通信指标值范围更大
                successRate: "@pick(['95.8%', '96.7%', '97.3%', '98.1%', '98.5%', '99.2%', '99.5%', '100%'])",
                avgDelay: "@pick(['0.3s', '0.5s', '0.8s', '1.2s', '1.8s', '2.1s', '3.5s', '5.2s'])",
                remark: "运行正常",
                createtime: "@datetime('yyyy-MM-dd HH:mm:ss')",
                "gatewayno|+1": 10000,
                provinceName: "广东省",
                cityName: "@pick(" + JSON.stringify(guangdongCities) + ")",
                countyName: "@county()",
            }],
            
        }
    })
    return a
}
Mock.mock(new RegExp('ssyj'), 'get', ssyj)
//自愈场景部署进度
function installationPlan() {
    // 十大自愈场景
    const scenarios = [
        '光旁路交换机应用', '单链补环场景', '光缆自愈场景', '电源自愈场景', 
        '路由切换场景', '设备老化预测', '网络拥塞自愈', '通信中断自愈',
        '设备故障自愈', '环境异常自愈'
    ];
    
    const a = Mock.mock({
        category: scenarios,
        "barData|10": ["@integer(60, 95)"], // 已部署自愈场景数量
    })
    
    let lineData=[],rateData=[];
    for (let index = 0; index < scenarios.length; index++) {
        let lineNum = Mock.mock('@integer(80, 100)') // 计划部署场景数量
        lineData.push(lineNum)
        let rate = a.barData[index] / lineNum;
        rateData.push((rate*100).toFixed(0))
    }
    a.lineData=lineData
    a.rateData=rateData
    return {
        success: true,
        data:a
    }
}
Mock.mock(new RegExp('installationPlan'), 'get', installationPlan)




//广东省场景告警分布排名
function ranking() {
  // 广东省十大自愈场景告警分布
  const scenarios = [
    { name: '光旁路场景告警', value: 1248 }, // 广东省配网通信光旁路场景告警
    { name: '光缆中断场景告警', value: 952 }, // 广东省配网通信光缆中断场景告警
    { name: '电源故障告警', value: 745 }, // 广东省配网通信电源故障告警
    { name: '路由异常告警', value: 623 }, // 广东省配网通信路由异常告警
    { name: '设备老化告警', value: 487 }, // 广东省配网通信设备老化告警
    { name: '网络拥塞告警', value: 398 }, // 广东省配网通信网络拥塞告警
    { name: '通信中断告警', value: 356 }, // 广东省配网通信中断告警
    { name: '环境异常告警', value: 289 } // 广东省配网通信环境异常告警
  ];
  
  let a = {
      success: true,
      data: scenarios
  }
  return a
}
Mock.mock(new RegExp('ranking'), 'get', ranking)
/**
 * @description: min ≤ r ≤ max  随机数
 * @param {*} Min
 * @param {*} Max
 * @return {*}
 */
function RandomNumBoth(Min,Max){
    var Range = Max - Min;
    var Rand = Math.random();
    var num = Min + Math.round(Rand * Range); //四舍五入
    return num;
}
/**
 * @description: 获取路径参数
 * @param {*} url
 * @return {*}
 */
function parameteUrl(url) {
    var json = {}
    if (/\?/.test(url)) {
        var urlString = url.substring(url.indexOf("?") + 1);
        var urlArray = urlString.split("&");
        for (var i = 0; i < urlArray.length; i++) {
            var urlItem = urlArray[i];
            var item = urlItem.split("=");
            console.log(item);
            json[item[0]] = item[1];
        }
        return json;
    }
    return {};
}


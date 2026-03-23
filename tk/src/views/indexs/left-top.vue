<!--
 * @Author: daidai
 * @Date: 2022-02-28 16:16:42
 * @LastEditors: Please set LastEditors
 * @LastEditTime: 2022-07-20 17:57:11
 * @FilePath: \web-pc\src\pages\big-screen\view\indexs\left-center.vue
-->
<template>
    <ul class="user_Overview flex" v-if="pageflag" @click="jumpToModelType">
        <li class="user_Overview-item" style="color: #00fdfa">
            <div class="user_Overview_nums allnum ">
                <dv-digital-flop :config="config" style="width:100%;height:100%;" />
            </div>
            <p>总设备数</p>
        </li>
        <li class="user_Overview-item" style="color: #07f7a8">
            <div class="user_Overview_nums online">
                <dv-digital-flop :config="onlineconfig" style="width:100%;height:100%;" />
            </div>
            <p>正常运行设备数</p>
        </li>
        <li class="user_Overview-item" style="color: #e3b337">
            <div class="user_Overview_nums offline">
                <dv-digital-flop :config="offlineconfig" style="width:100%;height:100%;" />

            </div>
            <p>自愈中设备数</p>
        </li>
        <li class="user_Overview-item" style="color: #f5023d">
            <div class="user_Overview_nums laramnum">
                <dv-digital-flop :config="laramnumconfig" style="width:100%;height:100%;" />
            </div>
            <p>告警设备数</p>
        </li>
    </ul>
    <Reacquire v-else @onclick="getData" line-height="200px">
        重新获取
    </Reacquire>
</template>

<script>
import { currentGET } from 'api/modules'
let style = {
    fontSize: 24
}
export default {
    data() {
        return {
            options: {},
            userOverview: {
                alarmNum: 0,
                offlineNum: 0,
                onlineNum: 0,
                totalNum: 0,
            },
            pageflag: true,
            timer: null,
            config: {
                number: [100],
                content: '{nt}',
                style: {
                    ...style,
                    // stroke: "#00fdfa",
                    fill: "#00fdfa",
                },
            },
            onlineconfig: {
                number: [0],
                content: '{nt}',
                style: {
                    ...style,
                    // stroke: "#07f7a8",
                    fill: "#07f7a8",
                },
            },
            offlineconfig: {
                number: [0],
                content: '{nt}',
                style: {
                    ...style,
                    // stroke: "#e3b337",
                    fill: "#e3b337",
                },
            },
            laramnumconfig: {
                number: [0],
                content: '{nt}',
                style: {
                    ...style,
                    // stroke: "#f5023d",
                    fill: "#f5023d",
                },
            }

        };
    },
    filters: {
        numsFilter(msg) {
            return msg || 0;
        },
    },
    created() {
        this.getData()
    },
    mounted() {
    },
    beforeDestroy() {
        this.clearData()

    },
    methods: {
        clearData() {
            if (this.timer) {
                clearInterval(this.timer)
                this.timer = null
            }
        },
        // 跳转到设备型号管理页面
        jumpToModelType() {
            console.log('跳转到设备型号管理页面');
            
            // 检查是否在 iframe 环境中
            const isInIframe = window.self !== window.top;
            console.log('是否在 iframe 中:', isInIframe);
            
            if (!isInIframe) {
                console.warn('当前不在 iframe 环境中，无法调用父窗口方法');
                return;
            }
            
            // 使用 postMessage 发送跳转请求
            try {
                // 安全：避免使用 '*'，尽量收敛到父窗口 origin
                const resolveParentOrigin = () => {
                    const configured = process.env.VUE_APP_PARENT_ORIGIN;
                    if (configured) {
                        return configured;
                    }
                    const referrer = document.referrer;
                    if (referrer) {
                        try {
                            return new URL(referrer).origin;
                        } catch (e) {}
                    }
                    const ancestors = window.location && window.location.ancestorOrigins;
                    if (ancestors && ancestors.length) {
                        return ancestors[0];
                    }
                    return null;
                };
                const targetOrigin = resolveParentOrigin();
                if (!targetOrigin) {
                    console.warn('无法确定父窗口 origin，已拒绝发送 postMessage（请配置 VUE_APP_PARENT_ORIGIN）');
                    return;
                }

                const message = {
                    type: 'DASHBOARD_JUMP',
                    path: '/model-type',
                    query: {}
                };
                
                // 发送到 window.parent
                if (window.parent && window.parent !== window.self) {
                    window.parent.postMessage(message, targetOrigin);
                    console.log('✓ postMessage 已发送到 window.parent，消息内容:', message);
                }
                
                // 如果 window.parent 和 window.top 不同，也发送到 window.top
                if (window.top && window.top !== window.self && window.top !== window.parent) {
                    try {
                        window.top.postMessage(message, targetOrigin);
                        console.log('✓ postMessage 已发送到 window.top');
                    } catch (e) {
                        console.warn('无法发送到 window.top:', e);
                    }
                }
                
                console.log('✓ postMessage 发送成功，等待父窗口处理');
            } catch (error) {
                console.error('× 发送 postMessage 失败:', error);
                console.error('错误详情:', error.message);
                if (error.stack) {
                    console.error('错误堆栈:', error.stack);
                }
            }
        },
        getData() {
            this.pageflag = true;
            currentGET("big2").then((res) => {
                if (!this.timer) {
                    console.log("设备总览", res);
                }
                if (res.success) {
                    this.userOverview = res.data;
                    this.onlineconfig = {
                        ...this.onlineconfig,
                        number: [res.data.onlineNum]
                    }
                    this.config = {
                        ...this.config,
                        number: [res.data.totalNum]
                    }
                    this.offlineconfig = {
                        ...this.offlineconfig,
                        number: [res.data.offlineNum]
                    }
                    this.laramnumconfig = {
                        ...this.laramnumconfig,
                        number: [res.data.alarmNum]
                    }
                    this.switper()
                } else {
                    this.pageflag = false;
                    this.$Message.warning(res.msg);
                }
            });
        },
        //轮询
        switper() {
            if (this.timer) {
                return
            }
            let looper = (a) => {
                this.getData()
            };
            this.timer = setInterval(looper, this.$store.state.setting.echartsAutoTime);
        },
    },
};
</script>
<style lang='scss' scoped>
.user_Overview {
    cursor: pointer;
    li {
        flex: 1;

        p {
            text-align: center;
            height: 16px;
            font-size: 16px;
        }

        .user_Overview_nums {
            width: 100px;
            height: 100px;
            text-align: center;
            line-height: 100px;
            font-size: 22px;
            margin: 50px auto 30px;
            background-size: cover;
            background-position: center center;
            position: relative;

            &::before {
                content: '';
                position: absolute;
                width: 100%;
                height: 100%;
                top: 0;
                left: 0;
            }

            &.bgdonghua::before {
                animation: rotating 14s linear infinite;
            }
        }

        .allnum {

            // background-image: url("../../assets/img/left_top_lan.png");
            &::before {
                background-image: url("../../assets/img/left_top_lan.png");

            }
        }

        .online {
            &::before {
                background-image: url("../../assets/img/left_top_lv.png");

            }
        }

        .offline {
            &::before {
                background-image: url("../../assets/img/left_top_huang.png");

            }
        }

        .laramnum {
            &::before {
                background-image: url("../../assets/img/left_top_hong.png");

            }
        }
    }
}
</style>

<!--
 * @Author: daidai
 * @Date: 2022-03-01 14:13:04
 * @LastEditors: Please set LastEditors
 * @LastEditTime: 2022-09-27 15:04:49
 * @FilePath: \web-pc\src\pages\big-screen\view\indexs\right-top.vue
-->
<template>
  <div class="right_top_container" @click="handleContainerClick">
    <Echart
      id="rightTop"
      :options="option"
      class="right_top_inner"
      v-if="pageflag"
      ref="charts"
    />
    <Reacquire v-else @onclick="getData" style="line-height: 200px">
      重新获取
    </Reacquire>
  </div>
</template>

<script>
import { currentGET } from "api/modules";
import {graphic} from "echarts"
export default {
  data() {
    return {
      option: {},
      pageflag: false,
      timer: null,
    };
  },
  created() {
   
  },

  mounted() {
     this.getData();
     console.log('right-top 组件已挂载');
  },
  beforeDestroy() {
    this.clearData();
  },
  methods: {
    clearData() {
      if (this.timer) {
        clearInterval(this.timer);
        this.timer = null;
      }
    },
    getData() {
      this.pageflag = true;
      // this.pageflag =false
      currentGET("big4").then((res) => {
        if (!this.timer) {
          console.log("告警次数", res);
        }
        if (res.success) {
          this.countUserNumData = res.data;
          this.$nextTick(() => {
            this.init(res.data.dateList, res.data.numList, res.data.numList2),
              this.switper();
          });
        } else {
          this.pageflag = false;
          this.$Message({
            text: res.msg,
            type: "warning",
          });
        }
      });
    },
    //轮询
    switper() {
      if (this.timer) {
        return;
      }
      let looper = (a) => {
        this.getData();
      };
      this.timer = setInterval(
        looper,
        this.$store.state.setting.echartsAutoTime
      );
      let myChart = this.$refs.charts.chart;
      myChart.on("mouseover", (params) => {
        this.clearData();
      });
      myChart.on("mouseout", (params) => {
        this.timer = setInterval(
          looper,
          this.$store.state.setting.echartsAutoTime
        );
      });
      
      // 添加图表点击事件（通过统一方法，避免重复绑定）
      this.addChartClickEvent();
    },
    init(xData, yData, yData2) {
      this.option = {
        xAxis: {
          type: "category",
          data: xData,
          boundaryGap: false, // 不留白，从原点开始
          splitLine: {
            show: true,
            lineStyle: {
              color: "rgba(31,99,163,.2)",
            },
          },
          axisLine: {
            // show:false,
            lineStyle: {
              color: "rgba(31,99,163,.1)",
            },
          },
          axisLabel: {
            color: "#7EB7FD",
            fontWeight: "500",
          },
        },
        yAxis: {
          type: "value",
          splitLine: {
            show: true,
            lineStyle: {
              color: "rgba(31,99,163,.2)",
            },
          },
          axisLine: {
            lineStyle: {
              color: "rgba(31,99,163,.1)",
            },
          },
          axisLabel: {
            color: "#7EB7FD",
            fontWeight: "500",
          },
        },
        tooltip: {
          trigger: "axis",
          backgroundColor: "rgba(0,0,0,.6)",
          borderColor: "rgba(147, 235, 248, .8)",
          textStyle: {
            color: "#FFF",
          },
        },
        grid: {
          //布局
          show: true,
          left: "10px",
          right: "30px",
          bottom: "10px",
          top: "28px",
          containLabel: true,
          borderColor: "#1F63A3",
        },
        series: [
          {
            data: yData,
            type: "line",
            smooth: true,
            symbol: "none", //去除点
            name: "原始告警数",
            color: "rgba(252,144,16,.7)",
            areaStyle: {
                //右，下，左，上
                color: new graphic.LinearGradient(
                  0,
                  0,
                  0,
                  1,
                  [
                    {
                      offset: 0,
                      color: "rgba(252,144,16,.7)",
                    },
                    {
                      offset: 1,
                      color: "rgba(252,144,16,.0)",
                    },
                  ],
                  false
                ),
            },
            markPoint: {
              data: [
                {
                  name: "最大值",
                  type: "max",
                  valueDim: "y",
                  symbol: "rect",
                  symbolSize: [60, 26],
                  symbolOffset: [0, -20],
                  itemStyle: {
                    color: "rgba(0,0,0,0)",
                  },
                  label: {
                    color: "#FC9010",
                    backgroundColor: "rgba(252,144,16,0.1)",
                    borderRadius: 6,
                    padding: [7, 14],
                    borderWidth: 0.5,
                    borderColor: "rgba(252,144,16,.5)",
                    formatter: "原始告警：{c}",
                  },
                },
                {
                  name: "最大值",
                  type: "max",
                  valueDim: "y",
                  symbol: "circle",
                  symbolSize: 6,
                  itemStyle: {
                    color: "#FC9010",
                    shadowColor: "#FC9010",
                    shadowBlur: 8,
                  },
                  label: {
                    formatter: "",
                  },
                },
              ],
            },
          },
          {
            data: yData2,
            type: "line",
            smooth: true,
            symbol: "none", //去除点
            name: "自愈后剩余告警数",
            color: "rgba(9,202,243,.7)",
            areaStyle: {
                //右，下，左，上
                color: new graphic.LinearGradient(
                  0,
                  0,
                  0,
                  1,
                  [
                    {
                      offset: 0,
                      color: "rgba(9,202,243,.7)",
                    },
                    {
                      offset: 1,
                      color: "rgba(9,202,243,.0)",
                    },
                  ],
                  false
                ),
            },
            markPoint: {
              data: [
                {
                  name: "最大值",
                  type: "max",
                  valueDim: "y",
                  symbol: "rect",
                  symbolSize: [60, 26],
                  symbolOffset: [0, -20],
                  itemStyle: {
                    color: "rgba(0,0,0,0)",
                  },
                  label: {
                    color: "#09CAF3",
                    backgroundColor: "rgba(9,202,243,0.1)",

                    borderRadius: 6,
                    borderColor: "rgba(9,202,243,.5)",
                    padding: [7, 14],
                    formatter: "自愈后告警：{c}",
                    borderWidth: 0.5,
                  },
                },
                {
                  name: "最大值",
                  type: "max",
                  valueDim: "y",
                  symbol: "circle",
                  symbolSize: 6,
                  itemStyle: {
                    color: "#09CAF3",
                    shadowColor: "#09CAF3",
                    shadowBlur: 8,
                  },
                  label: {
                    formatter: "",
                  },
                },
              ],
            },
          },
        ],
      };
      
      // 在设置完 options 后，等待下一个 tick 添加点击事件
      this.$nextTick(() => {
        this.addChartClickEvent();
      });
    },
    // 处理容器点击事件（点击整个区域）
    handleContainerClick(event) {
      console.log('告警次数区域被点击', event);
      this.jumpToPowerAlarmStatistics();
    },
    // 添加图表点击事件
    addChartClickEvent() {
      if (this.$refs.charts && this.$refs.charts.getChart) {
        const chart = this.$refs.charts.getChart();
        if (chart) {
          // 先移除旧的事件监听器，避免重复绑定
          chart.off('click');
          // 添加新的点击事件监听器
          chart.on('click', (params) => {
            console.log('告警次数图表点击事件:', params);
            this.handleChartClick(params);
          });
        }
      }
    },
    // 处理图表点击事件
    handleChartClick(params) {
      console.log('告警次数图表点击事件:', params);
      
      // 可以传递点击的数据点信息作为参数
      const query = {};
      if (params.name) {
        query.date = params.name; // 传递点击的日期
      }
      if (params.value) {
        query.value = params.value; // 传递点击的值
      }
      if (params.seriesName) {
        query.seriesName = params.seriesName; // 传递系列名称（原始告警数/自愈后剩余告警数）
      }
      
      // 调用统一的跳转方法
      this.jumpToPowerAlarmStatistics(query);
    },
    // 跳转到告警统计分析页面
    jumpToPowerAlarmStatistics(query = {}) {
      console.log('=== 开始执行跳转到告警统计分析 ===');
      console.log('查询参数:', query);
      
      // 检查是否在 iframe 环境中
      const isInIframe = window.self !== window.top;
      console.log('是否在 iframe 中:', isInIframe);
      
      if (!isInIframe) {
        console.warn('当前不在 iframe 环境中，无法调用父窗口方法');
        return;
      }
      
      // 直接使用 postMessage（支持跨域和同源）
      console.log('使用 postMessage 发送跳转请求...');
      
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

        // 发送 postMessage 到父窗口
        const message = {
          type: 'DASHBOARD_JUMP',
          path: '/power-alarm-statistics',
          query: query
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
      
      console.log('=== 跳转执行完成 ===');
    },
  },
};
</script>
<style lang='scss' scoped>
.right_top_container {
  width: 100%;
  height: 100%;
  position: relative;
  cursor: pointer;
}

.right_top_inner {
  margin-top: -8px;
  pointer-events: auto; // 确保图表可以正常交互
}
</style>

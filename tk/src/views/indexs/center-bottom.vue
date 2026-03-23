<template>
  <div class="center_bottom" @click="handleContainerClick">
    <Echart
      ref="echartRef"
      :options="options"
      id="bottomLeftChart"
      class="echarts_bottom"
    ></Echart>
  </div>
</template>

<script>
import { currentGET } from "api";
import { graphic } from "echarts";
export default {
  data() {
    return {
      options: {},
    };
  },
  props: {},
  mounted() {
    this.getData();
    console.log('center-bottom 组件已挂载');
    // 延迟检查父窗口方法，确保主系统已加载完成
    this.checkParentMethod();
  },
  methods: {
    // 处理容器点击事件（点击整个区域）
    handleContainerClick(event) {
      // 如果点击的是图表区域，不处理（让图表自己处理）
      const target = event.target;
      const isChartArea = target.closest('#bottomLeftChart') || 
                         target.closest('.echarts_bottom') ||
                         target.closest('[id^="echart"]') ||
                         target.tagName === 'CANVAS';
      
      if (isChartArea) {
        return;
      }
      
      console.log('设备统计区域被点击（容器点击）', event);
      this.jumpToSwitchManage();
    },
    // 检查父窗口方法（已废弃，直接使用 postMessage 不需要检查）
    // 保留此方法以防其他地方调用，但不执行任何操作
    checkParentMethod(retryCount = 0) {
      console.log('checkParentMethod: 已切换到 postMessage 模式，无需检查父窗口方法');
      return true;
    },
    // 统一的跳转方法（使用 postMessage 支持跨域）
    jumpToSwitchManage(query = {}) {
      console.log('=== 开始执行跳转 ===');
      console.log('查询参数:', query);
      
      // 检查是否在 iframe 环境中
      const isInIframe = window.self !== window.top;
      console.log('是否在 iframe 中:', isInIframe);
      
      if (!isInIframe) {
        console.warn('当前不在 iframe 环境中，无法调用父窗口方法');
        return;
      }
      
      // 直接使用 postMessage（支持跨域和同源）
      // postMessage 是浏览器提供的标准跨域通信方式，在同源和跨域情况下都可以工作
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
          path: '/switch-list',
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
    getData() {
      this.pageflag = true;
      currentGET("big6", { companyName: this.companyName }).then((res) => {
        console.log("安装计划", res);
        if (res.success) {
          this.init(res.data);
        } else {
          this.pageflag = false;
          this.$Message({
            text: res.msg,
            type: "warning",
          });
        }
      });
    },
    init(newData) {
      this.options = {
        tooltip: {
          trigger: "axis",
          backgroundColor: "rgba(0,0,0,.6)",
          borderColor: "rgba(147, 235, 248, .8)",
          textStyle: {
            color: "#FFF",
          },
          formatter: function (params) {
            // 添加单位
            var result = params[0].name + "<br>";
            params.forEach(function (item) {
              if (item.value) {
                if (item.seriesName == "自愈覆盖率") {
                  result +=
                    item.marker +
                    " " +
                    item.seriesName +
                    " : " +
                    item.value +
                    "%</br>";
                } else {
                  result +=
                    item.marker +
                    " " +
                    item.seriesName +
                    " : " +
                    item.value +
                    "个</br>";
                }
              } else {
                result += item.marker + " " + item.seriesName + " :  - </br>";
              }
            });
            return result;
          },
        },
        legend: {
          data: ["已部署自愈场景", "计划部署场景", "自愈覆盖率"],
          textStyle: {
            color: "#B4B4B4",
          },
          top: "0",
        },
        grid: {
          left: "50px",
          right: "40px",
          bottom: "30px",
          top: "20px",
        },
        xAxis: {
          data: newData.category,
          axisLine: {
            lineStyle: {
              color: "#B4B4B4",
            },
          },
          axisTick: {
            show: false,
          },
        },
        yAxis: [
          {
            splitLine: { show: false },
            axisLine: {
              lineStyle: {
                color: "#B4B4B4",
              },
            },

            axisLabel: {
              formatter: "{value}",
            },
          },
          {
            splitLine: { show: false },
            axisLine: {
              lineStyle: {
                color: "#B4B4B4",
              },
            },
            axisLabel: {
              formatter: "{value}% ",
            },
          },
        ],
        series: [
          {
            name: "已部署自愈场景",
            type: "bar",
            barWidth: 10,
            itemStyle: {
              borderRadius: 5,
              color: new graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: "#956FD4" },
                { offset: 1, color: "#3EACE5" },
              ]),
            },
            data: newData.barData,
          },
          {
            name: "计划部署场景",
            type: "bar",
            barGap: "-100%",
            barWidth: 10,
            itemStyle: {
              borderRadius: 5,
              color: new graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: "rgba(156,107,211,0.8)" },
                { offset: 0.2, color: "rgba(156,107,211,0.5)" },
                { offset: 1, color: "rgba(156,107,211,0.2)" },
              ]),
            },
            z: -12,
            data: newData.lineData,
          },
          {
            name: "自愈覆盖率",
            type: "line",
            smooth: true,
            showAllSymbol: true,
            symbol: "emptyCircle",
            symbolSize: 8,
            yAxisIndex: 1,
            itemStyle: {
              color: "#F02FC2",
            },
            data: newData.rateData,
          },
        ],
      };
      
      // 在设置完 options 后，等待下一个 tick 添加点击事件
      this.$nextTick(() => {
        this.addChartClickEvent();
      });
    },
    // 添加图表点击事件
    addChartClickEvent() {
      if (this.$refs.echartRef && this.$refs.echartRef.getChart) {
        const chart = this.$refs.echartRef.getChart();
        if (chart) {
          // 先移除旧的事件监听器，避免重复绑定
          chart.off('click');
          // 添加新的点击事件监听器
          chart.on('click', (params) => {
            console.log('图表点击事件:', params);
            this.handleChartClick(params);
          });
        }
      }
    },
    // 处理图表点击事件（保留，但也可以触发跳转）
    handleChartClick(params) {
      console.log('图表点击事件:', params);
      
      const clickedName = params.name || '';
      const seriesName = params.seriesName || '';
      
      // 判断是否点击了"光旁路"相关的内容
      const isOpticalRelated = clickedName.includes('光旁路') || 
                               seriesName.includes('光旁路') ||
                               clickedName.includes('光旁') ||
                               seriesName.includes('光旁');
      
      const query = {};
      if (isOpticalRelated) {
        query.filter = 'optical';
      }
      if (clickedName) {
        query.category = clickedName;
      }
      
      // 调用统一的跳转方法
      this.jumpToSwitchManage(query);
    },
  },
};
</script>
<style lang="scss" scoped>
.center_bottom {
  width: 100%;
  height: 100%;
  position: relative;
  cursor: pointer;

  .echarts_bottom {
    width: 100%;
    height: 100%;
    pointer-events: auto; // 确保图表可以正常交互
  }
}
</style>

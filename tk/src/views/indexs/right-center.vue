<!--
 * @Author: daidai
 * @Date: 2022-03-01 15:51:43
 * @LastEditors: Please set LastEditors
 * @LastEditTime: 2022-09-29 15:12:46
 * @FilePath: \web-pc\src\pages\big-screen\view\indexs\right-bottom.vue
-->
<template>
  <div class="right_bottom" @click="handleContainerClick">
    <dv-capsule-chart :config="config" style="width:100%;height:260px" />
  </div>
</template>

<script>
import { currentGET } from 'api/modules'
export default {
  data() {
    return {
      gatewayno: '',
      config: {
        showValue: true,
        unit: "次",
        data: []
      },

    };
  },
  created() {
    this.getData()

  },
  computed: {
  },
  mounted() {
    console.log('right-center 组件已挂载');
  },
  beforeDestroy() {
    this.clearData()
  },
  methods: {
    // 处理容器点击事件（点击整个区域）
    handleContainerClick(event) {
      console.log('告警排名区域被点击', event);
      this.jumpToPowerAlarmManage();
    },
    // 跳转到电源告警管理页面
    jumpToPowerAlarmManage(query = {}) {
      console.log('=== 开始执行跳转到电源告警管理 ===');
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
          path: '/power-alarms',
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
    clearData() {
      if (this.timer) {
        clearInterval(this.timer)
        this.timer = null
      }
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
    getData() {
      this.pageflag = true
      // this.pageflag =false
      currentGET('big7', { gatewayno: this.gatewayno }).then(res => {

        if (!this.timer) {
          console.log('场景告警分布', res);
        }
        if (res.success) {
          this.config = {
            ...this.config,
            data: res.data
          }
          this.switper()
        } else {
          this.pageflag = false
          this.srcList = []
          this.$Message({
            text: res.msg,
            type: 'warning'
          })
        }
      })
    },
  },
};
</script>
<style lang='scss' scoped>
.list_Wrap {
  height: 100%;
  overflow: hidden;
  :deep(.kong)   {
    width: auto;
  }
}

.sbtxSwiperclass {
  .img_wrap {
    overflow-x: auto;
  }

}

.right_bottom {
  box-sizing: border-box;
  padding: 0 16px;
  cursor: pointer;
  position: relative;

  .searchform {
    height: 80px;
    display: flex;
    align-items: center;
    justify-content: center;

    .searchform_item {
      display: flex;
      justify-content: center;
      align-items: center;

      label {
        margin-right: 10px;
        color: rgba(255, 255, 255, 0.8);
      }

      button {
        margin-left: 30px;
      }

      input {}
    }
  }

  .img_wrap {
    display: flex;
    // justify-content: space-around;
    box-sizing: border-box;
    padding: 0 0 20px;
    // overflow-x: auto;

    li {
      width: 105px;
      height: 137px;
      border-radius: 6px;
      overflow: hidden;
      cursor: pointer;
      // background: #84ccc9;
      // border: 1px solid #ffffff;
      overflow: hidden;
      flex-shrink: 0;
      margin: 0 10px;

      img {
        flex-shrink: 0;
      }
    }




  }

  .noData {
    width: 100%;
    line-height: 100px;
    text-align: center;
    color: rgb(129, 128, 128);

  }
}
</style>

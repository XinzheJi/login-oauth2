<!--
 * @Author: daidai
 * @Date: 2022-03-01 09:16:22
 * @LastEditors: Please set LastEditors
 * @LastEditTime: 2022-09-29 15:12:34
 * @FilePath: \web-pc\src\pages\big-screen\components\item-wrap\item-wrap.vue
-->
<template>
  <dv-border-box-13 class="lr_titles" :class="{ 'clickable-wrap': clickable }" @click="handleClick">
    <div class="item_title" v-if="title !== ''" :class="{ 'clickable-title': clickable }">
      <div class="zuo"></div>
      <span class="title-inner"> &nbsp;&nbsp;{{ title }}&nbsp;&nbsp; </span>
      <div class="you"></div>
    </div>
    <div
      :class="title !== '' ? 'item_title_content' : 'item_title_content_def'"
    >
      <slot></slot>
    </div>
  </dv-border-box-13>
</template>

<script>
export default {
  data() {
    return {};
  },
  props: {
    title: {
      type: String,
      default: () => "",
    },
    clickable: {
      type: Boolean,
      default: false,
    },
  },
  created() {},

  mounted() {},
  methods: {
    handleClick(event) {
      if (this.clickable) {
        // 检查点击的目标元素
        const target = event.target;
        const clickedElement = target.closest('.item_title') || target.closest('.item_title_content');
        
        // 如果点击的是图表内容区域（echarts容器），不触发跳转，让图表自己处理
        const isChartArea = target.closest('.echarts_bottom') || 
                           target.closest('#bottomLeftChart') ||
                           target.closest('[id^="echart"]');
        
        if (isChartArea) {
          // 点击的是图表区域，不处理，让图表自己的点击事件处理
          return;
        }
        
        // 触发自定义事件
        this.$emit('title-click', event);
      }
    },
  },
};
</script>
<style lang='scss' scoped>
$item-title-height: 38px;
$item_title_content-height: calc(100% - 38px);

.lr_titles {
  box-sizing: border-box;

:deep(.border-box-content)  {
    box-sizing: border-box;
    padding: 6px 16px 0px;
  }

  .item_title {
    height: $item-title-height;
    line-height: $item-title-height;
    width: 100%;
    color: #31abe3;
    text-align: center;
    // background: linear-gradient(to right, transparent, #0f0756, transparent);
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;

    .zuo,
    .you {
      width: 58px;
      height: 14px;
      background-image: url("../../assets/img/titles/zuo.png");
    }

    .you {
      transform: rotate(180deg);
    }
    .title-inner {
      font-weight: 900;
      letter-spacing: 2px;
      background: linear-gradient(
        92deg,
        #0072ff 0%,
        #00eaff 48.8525390625%,
        #01aaff 100%
      );
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }
  }

  .item_title_content {
    height: $item_title_content-height;
  }

  .item_title_content_def {
    width: 100%;
    height: 100%;
  }

  // 可点击样式
  &.clickable-wrap {
    cursor: pointer;
    transition: opacity 0.3s;

    &:hover {
      opacity: 0.9;
    }
  }

  .clickable-title {
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      transform: scale(1.05);
    }
  }
}
</style>
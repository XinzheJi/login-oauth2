<template>
  <div class="data-table-container">
    <table>
      <thead>
        <tr>
          <th v-for="column in columns" :key="column.key" :class="[column.class]">
            {{ column.label }}
          </th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(item, index) in data" :key="item.id || index">
          <td v-for="column in columns" :key="column.key" :class="[column.class]">
            <!-- 如果有自定义渲染函数 -->
            <slot v-if="column.slot" :name="column.slot" :item="item" :index="index"></slot>
            <!-- 默认渲染 -->
            <template v-else>
              {{ getItemValue(item, column.key) }}
            </template>
          </td>
        </tr>
        <!-- 无数据显示 -->
        <tr v-if="data.length === 0">
          <td :colspan="columns.length" class="no-data">
            {{ noDataText }}
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
export default {
  name: 'DataTable',
  props: {
    columns: {
      type: Array,
      required: true,
      /* 
        示例: [
          { key: 'id', label: 'ID' },
          { key: 'name', label: '名称' },
          { key: 'actions', label: '操作', slot: 'actions' }
        ]
      */
    },
    data: {
      type: Array,
      default: () => []
    },
    noDataText: {
      type: String,
      default: '暂无数据'
    }
  },
  setup() {
    // 根据键路径获取对象的值（支持嵌套属性如 'user.name'）
    const getItemValue = (item, key) => {
      if (!key) return '';
      
      // 处理嵌套属性
      const keys = key.split('.');
      let value = item;
      
      for (const k of keys) {
        if (value === null || value === undefined) {
          return '';
        }
        value = value[k];
      }
      
      return value;
    };
    
    return {
      getItemValue
    };
  }
};
</script>

<style scoped>
.data-table-container {
  width: 100%;
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  border-spacing: 0;
}

th, td {
  padding: 12px 15px;
  text-align: left;
  border-bottom: 1px solid #e0e0e0;
}

th {
  font-weight: bold;
  background-color: #f5f5f5;
  color: #333;
}

tbody tr:hover {
  background-color: #f8f8f8;
}

.no-data {
  text-align: center;
  padding: 20px;
  color: #999;
}
</style> 
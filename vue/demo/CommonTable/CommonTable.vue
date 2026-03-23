<template>
  <div :id="id" class="common-table">
    <div
      v-if="showMainOperatorBlock"
      class="main-operator-block"
    >
      <div class="left-button">
        <slot name="operate-button" />
      </div>
      <div
        v-if="showSearchBlock"
        class="search-block"
        :style="{maxWidth: getSearchMaxWidth}"
      >
        <slot name="operate-search" />
        <div class="query-button-group">
          <template v-if="$slots['operate-search'] && !hiddenDefaultSearchBtn">
            <el-button type="primary" icon="el-icon-search" @click="doSearch">查询</el-button>
            <el-button icon="el-icon-refresh-left" @click="doReset">重置</el-button>
          </template>
          <el-button
            v-if="showExport"
            :loading="loadingExport"
            type="success"
            icon="el-icon-share"
            @click="exportHandle"
          >导出
          </el-button>
          <slot name="operate-button-right" />
          <el-button
            v-if="_.size(searchItem) > searchOverflow"
            @click="showSearchMore"
          >{{ !searchDisplay ? '更多' : '收起' }}<i
            style="margin-left: 4px;"
            :class="searchDisplay?'el-icon-arrow-up':'el-icon-arrow-down'"
          />
          </el-button>
        </div>
      </div>
    </div>
    <div v-show="$slots['custom-area']" class="custom-area">
      <slot name="custom-area" />
    </div>
    <el-table
      ref="TableRef"
      v-loading="loading"
      :show-header="showHeader"
      :header-cell-style="{background:'#f8f8f9',color:'rgb(80 78 78)'}"
      :data="tableData"
      :border="border"
      :stripe="stripe"
      :size="tableSize"
      :cell-style="cellStyle"
      :height="autoHeight ? tableCalcHeight : height"
      :max-height="maxHeight"
      :lazy="lazy"
      :load="load"
      :row-key="rowKey"
      :default-expand-all="defaultExpandAll"
      :expand-row-keys="expands"
      :tree-props="{children: 'children', hasChildren: 'hasChildren'}"
      :highlight-current-row="highlightCurrentRow"
      :row-class-name="rowClassName"
      :empty-text="emptyText"
      @select="selectCallback"
      @select-all="selectAllCallback"
      @selection-change="selectionChange"
      @sort-change="sortChangeHandler"
      @expand-change="expandChange"
      @row-click="rowClick"
      @current-change="tableCurrentChangeCallback"
    >
      <el-table-column v-if="showIndex" type="index" label="序号" align="center" width="60px" />
      <slot name="table-columns" />
    </el-table>

    <div
      v-if="pagination.current>=0&&pagination.pageSize>=0"
      class="paging-operation"
    >
      <el-pagination
        v-loading="loading"
        :small="tableSize==='mini'"
        class="pagination-block"
        :current-page="pagination.current"
        :page-sizes="pageSizes"
        :page-size="pagination.pageSize"
        :layout="paginationLayout"
        :total="pagination.total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
    <el-dialog
      :visible.sync="visible"
      append-to-body
      title="请选择隐藏/显示列"
      width="500px"
    >
      <el-checkbox v-for="item in columns" :key="item.id" v-model="item.show" @change="hideColumn(item)">{{
        item.label
      }}
      </el-checkbox>
      <span slot="footer">
        <el-button type="primary" @click="visible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>

export default {
  name: 'CommonTable',
  props: {
    expands: {
      type: Array,
      default() {
        return []
      }
    },
    defaultExpandAll: {
      type: Boolean,
      default: false
    },
    showIndex: {
      type: Boolean,
      default: false
    },
    rowClassName: {
      type: [String, Function],
      default: ''
    },
    showExport: {
      type: Boolean,
      default: false
    },
    tableData: {
      type: Array,
      required: true,
      default: () => {
        return []
      }
    },
    searchMaxWidth: {
      type: [Number, String],
      default: '780px'
    },
    loading: {
      type: Boolean,
      default: false
    },
    highlightCurrentRow: {
      type: Boolean,
      default: false
    },
    cellStyle: {
      type: [Object, Function],
      default: () => {
        return {}
      }
    },
    pagination: {
      type: Object,
      default: () => {
        return {}
      }
    },
    height: {
      type: [String, Number],
      default: undefined
    },
    maxHeight: {
      type: [String, Number],
      default: undefined
    },
    id: {
      type: [String, Number],
      default: undefined
    },
    lazy: {
      type: Boolean,
      default: false
    },
    load: {
      type: Function,
      default: undefined
    },
    columnDisplay: {
      type: Boolean,
      default: false
    },
    customPagination: {
      type: Boolean,
      default: false
    },
    border: {
      type: Boolean,
      default: true
    },
    stripe: {
      type: Boolean,
      default: false
    },
    searchOverflow: {
      type: Number,
      default: 4
    },
    tableSize: {
      type: String,
      default: 'small'
    },
    paginationLayout: {
      type: String,
      default: 'total, sizes, prev, pager, next, jumper'
    },
    emptyText: {
      type: String,
      default: '暂无数据'
    },
    showHeader: {
      type: Boolean,
      default: true
    },
    autoReset: {
      type: Boolean,
      default: false
    },
    loadingExport: {
      type: Boolean,
      default: false
    },
    autoHeight: {
      type: Boolean,
      default: false
    },
    rowKey: {
      type: [String, Function],
      default: 'id'
    },
    pageSizes: {
      type: Array,
      default() {
        return [5, 10, 20, 50, 100]
      }
    },
    hiddenDefaultSearchBtn: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      visible: false,
      columns: [],
      searchItem: [],
      searchDisplay: false,
      tableCalcHeight: 0
    }
  },
  computed: {
    showMainOperatorBlock() {
      return this.$slots['operate-button'] || this.showSearchBlock
    },
    showSearchBlock() {
      return this.$slots['operate-search'] || this.$slots['operate-button-right'] || this.showExport || this._.size(this.searchItem) > this.searchOverflow
    },
    getSearchMaxWidth() {
      if (this._.indexOf(this.searchMaxWidth, 'px') !== -1 || this._.indexOf(this.searchMaxWidth, '%') !== -1) {
        return this.searchMaxWidth
      } else {
        return `${this.searchMaxWidth}px`
      }
    }
  },
  mounted() {
    this.$nextTick(() => {
      if (this.$refs.TableRef) {
        this.columns = this._.map(this.$refs.TableRef.columns, item => {
          return {
            label: item.label,
            id: item.id,
            show: true
          }
        })
        if (this.autoHeight) {
          this.calcTableAutoHeight()
        }
      } else {
        this.columns = []
      }

      if (this.searchOverflow > 4) {
        this.renderSearchItem()
      }
    })
  },
  methods: {
    calcTableAutoHeight() {
      const bodyRect = document.body.getBoundingClientRect()
      const tableRect = this.$refs.TableRef.$el.getBoundingClientRect()
      this.tableCalcHeight = bodyRect.height - tableRect.top - 80
    },
    showSearchMore() {
      this.searchDisplay = !this.searchDisplay
      this.renderSearchItem()
      this.$refs.TableRef.doLayout()
    },
    renderSearchItem() {
      this.searchItem = this.$el.querySelectorAll('.main-operator-block >.search-block >.search-item')
      if (this.searchItem.length > this.searchOverflow) {
        for (let i = this.searchOverflow; i < this.searchItem.length; i++) {
          this.searchItem[i].style.display = this.searchDisplay ? '' : 'none'
        }
      }
    },
    hideColumn(item) {
      const isTrue = []
      const isFalse = []
      this._.forEach(this.columns, item => {
        if (item.show) {
          isTrue.push(item)
        } else {
          isFalse.push(item)
        }
      })
      if (isTrue.length === 0) {
        item.show = true
        this.$message.warning('请至少保留一列')
        return
      }
      this.$emit('hide-column', this._.map(this.columns, item => {
        return item.show
      }))
    },
    selectionChange(list) {
      this.$emit('selection-change', list)
    },
    handleSizeChange(size) {
      if (this.customPagination) {
        this.$emit('size-change', size)
      } else {
        if (this.pagination.pageSize) {
          this.pagination.pageSize = size
          this.$emit('on-load')
        }
      }
    },
    handleCurrentChange(current) {
      if (this.customPagination) {
        this.$emit('current-change', current)
      } else {
        if (this.pagination.current) {
          this.pagination.current = current
          this.$emit('on-load')
        }
      }
    },
    sortChangeHandler(sort) {
      this.$emit('sort-change', sort)
    },
    doSearch() {
      this.$emit('do-search')
    },
    doReset() {
      if (this.autoReset) {
        for (const key in this.pagination) {
          switch (key) {
            case 'current':
              this.$set(this.pagination, key, 1)
              break
            case 'total':
              this.$set(this.pagination, key, 0)
              break
            case 'pageSize':
              this.$set(this.pagination, key, 10)
              break
            default:
              this.$set(this.pagination, key, null)
              break
          }
        }
        this.$emit('on-load')
      } else {
        this.$emit('do-reset')
      }
    },
    expandChange(row, expandedRows) {
      this.$emit('expand-change', row, expandedRows)
    },
    rowClick(row) {
      this.$emit('row-click', row)
    },
    selectCallback(selection, row) {
      this.$emit('select', selection, row)
    },
    selectAllCallback(selection) {
      this.$emit('select-all', selection)
    },
    tableCurrentChangeCallback(currentRow, oldCurrentRow) {
      this.$emit('table-current-change', currentRow, oldCurrentRow)
    },
    toggleRowSelection(row, selected) {
      this.$refs.TableRef.toggleRowSelection(row, selected)
    },
    toggleAllSelection() {
      this.$refs.TableRef.toggleAllSelection()
    },
    clearSelection() {
      this.$refs.TableRef.clearSelection()
    },
    exportHandle() {
      this.$emit('do-export')
    },
    toggleRowExpansion(row, expanded) {
      this.$refs.TableRef.toggleRowExpansion(row, expanded)
    },
    doLayout() {
      this.$refs.TableRef.doLayout()
    }
  }
}
</script>

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

// Element Plus 组件类型声明
declare module '@element-plus/icons-vue' {
  export const Monitor: any
  export const ArrowDown: any
  export const Bell: any
  export const ExternalLink: any
  export const InfoFilled: any
  export const Link: any
  export const Grid: any
  export const TrendCharts: any
  export const Warning: any
  export const DataAnalysis: any
  export const Setting: any
  export const HomeFilled: any
  export const User: any
  export const Key: any
  export const Lock: any
  export const OfficeBuilding: any
  export const ArrowRight: any
  export const ArrowLeft: any
  export const Cpu: any
  export const BellFilled: any
  export const Guide: any
  export const CollectionTag: any
}

// Element Plus 组件声明
declare module 'element-plus' {
  export const ElIcon: any
  export const ElDropdown: any
  export const ElDropdownMenu: any
  export const ElDropdownItem: any
  export const ElButton: any
  export const ElCard: any
  export const ElTag: any
  export const ElMessage: any
  export const ElMenu: any
  export const ElMenuItem: any
  export const ElAside: any
}

// Vue Router 类型声明
declare module 'vue-router' {
  export const useRoute: any
  export const useRouter: any
}

// Pinia Store 类型声明
declare module '@/store/user' {
  export const useUserStore: any
}

import Modal from './Modal.vue';
import DataTable from './DataTable.vue';
import FormGroup from './FormGroup.vue';
import ConfirmDialog from './ConfirmDialog.vue';
import ActionButtons from './ActionButtons.vue';
import DashboardIframe from './DashboardIframe.vue';
import TabsContainer from './TabsContainer.vue';
import TabsBar from './TabsBar.vue';
import TabItem from './TabItem.vue';

export {
  Modal,
  DataTable,
  FormGroup,
  ConfirmDialog,
  ActionButtons,
  DashboardIframe,
  TabsContainer,
  TabsBar,
  TabItem
};

// 创建安装函数，方便批量全局注册
export default {
  install(app) {
    app.component('Modal', Modal);
    app.component('DataTable', DataTable);
    app.component('FormGroup', FormGroup);
    app.component('ConfirmDialog', ConfirmDialog);
    app.component('ActionButtons', ActionButtons);
    app.component('DashboardIframe', DashboardIframe);
    app.component('TabsContainer', TabsContainer);
    app.component('TabsBar', TabsBar);
    app.component('TabItem', TabItem);
  }
}; 
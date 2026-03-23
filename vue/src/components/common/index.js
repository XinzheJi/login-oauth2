import Modal from './Modal.vue';
import DataTable from './DataTable.vue';
import FormGroup from './FormGroup.vue';
import ConfirmDialog from './ConfirmDialog.vue';
import ActionButtons from './ActionButtons.vue';

export {
  Modal,
  DataTable,
  FormGroup,
  ConfirmDialog,
  ActionButtons
};

// 创建安装函数，方便批量全局注册
export default {
  install(app) {
    app.component('Modal', Modal);
    app.component('DataTable', DataTable);
    app.component('FormGroup', FormGroup);
    app.component('ConfirmDialog', ConfirmDialog);
    app.component('ActionButtons', ActionButtons);
  }
}; 
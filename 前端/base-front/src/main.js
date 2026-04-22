import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
import store from "./store";
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import '@/assets/aliicon/iconfont.css' // icon css
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import 'normalize.css/normalize.css';

import * as ElIcons from "@element-plus/icons-vue";
import CmTable from "@/components/CmTable.vue";
import hasBtnPermission from '@/utils/btn-permission';

const app = createApp(App);
for (const name in ElIcons) {
  app.component(name, ElIcons[name]);
}
app.component('CmTable', CmTable);
const initialize = async () => {
  app.use(ElementPlus, {
    locale: zhCn,
    // 支持 large、default、small
    size:'default'
  })
  app.use(router).use(store).mount("#app");
  app.config.globalProperties.$hasBP = hasBtnPermission;
};
initialize();



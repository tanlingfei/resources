<template>
  <div class="main-body">
    <!--工具栏-->
    <div class="toolbar">
      <el-form :inline="true" :model="filters">
        <el-form-item>
          <el-button icon="plus" type="primary" :disabled="$hasBP('bnt.sysMenu.add')  === false" @click="handleAdd">添加</el-button>
        </el-form-item>
      </el-form>
    </div>
    <!--表格树内容栏-->
    <cm-table
        rowKey="id"
        ref="tableRef"
        :get-page="listTree"
        :columns="columns"
        :filters="filters"
        :showBatchDelete="false"
        :showPagination="false"
        :operations="operations"
        @handleEdit="handleEdit"
        @handleDelete="handleDelete"
    ></cm-table>
  </div>
  <!-- 新增修改界面 -->
  <el-dialog
      :title="isEdit ? '编辑' : '添加'"
      width="40%"
      draggable
      v-model="dialogVisible"
      :close-on-click-modal="false"
      @close="doClose"
  >
    <el-form
        :model="form"
        :rules="rules"
        ref="formRef"
        @keyup.enter="handleSubmit"
        label-width="80px"
    >
      <el-form-item label="类型" prop="type">
        <el-radio-group v-model="form.type" :disabled="isEdit" @change="setParentMenu">
          <el-radio
              v-for="(type, index) in menuTypeList"
              :label="index"
              :key="index"
          >{{ type }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="名称" prop="name">
        <el-input v-model="form.name" placeholder="名称"></el-input>
      </el-form-item>
      <el-form-item v-if="form.type !== 2" label="图标">
        <el-input v-model="form.icon" placeholder="图标"></el-input>
      </el-form-item>
      <el-form-item label="上级">
        <el-cascader
            v-model="form.parentId"
            :props="{ label: 'val', value: 'id', checkStrictly: true, emitPath: false }"
            :options="treeData"
            class="w100p"
            clearable
        ></el-cascader>
      </el-form-item>
      <el-form-item v-if="form.type !== 2" label="路由地址" prop="path">
        <el-input v-model="form.path" placeholder="路由地址"></el-input>
      </el-form-item>
      <el-form-item
          v-if="form.type === 1"
          label="组件路径"
          :prop="form.type === 1 ? 'component' : ''"
      >
        <el-input v-model="form.component" placeholder="组件路径"></el-input>
      </el-form-item>
      <el-form-item v-if="form.type === 2" label="权限字符" prop="path">
        <el-input v-model="form.perms" placeholder="权限字符"></el-input>
      </el-form-item>
      <el-form-item
          v-if="form.type !== 2"
          label="排序"
          :prop="form.sortValue !== 2 ? 'sortValue' : ''"
      >
        <el-input-number v-model="form.sortValue" controls-position="right" :min="0"></el-input-number>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="doClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确认</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import {getById, listTree, listTreeParents, save, update, remove, listMenuParents} from '@/apis/app-resource'
import useTableHandlers from '@/apis/use-table-handlers'

const menuTypeList = ref(["目录", "菜单", "按钮"])
const filters = reactive({
  name: ""
})
const form = reactive({
  id: '',
  type: 0,
  name: "",
  path: "",
  perms: "",
  parentId: "",
  component: "",
  sortValue: 0,
  icon: "",
})
const {
  tableRef,
  dialogVisible,
  isEdit,
  formLoading,
  formRef,
  doAdd,
  doEdit,
  doRemove,
  doSubmit,
  doClose
} = useTableHandlers(form);
const treeData = ref([])

// computed
const columns = computed(() => [
  /*  { prop: "id", label: t("thead.ID") },*/
  {prop: "name", label: "名称"},
  {
    prop: "type", label: "类型",
    formatter: (row) => {
      if (row.type === 0) {
        return "目录";
      } else if (row.type === 1) {
        return "菜单";
      } else if (row.type === 2) {
        return "按钮";
      } else {
        return "";
      }
    }
  },
  {prop: "path", label: "路由地址"},
  {prop: "perms", label: "权限字符"},
  {prop: "component", label: "组件路径", showOverflowTooltip: true},
  {prop: "icon", label: "图标"},
  {prop: "sortValue", label: "排序"},
])

const operations = computed(() => [
  {
    label: "添加下级",
    onClick: addXj,
    perm:'bnt.sysMenu.add',
    show:function (row){return row.type!='2'}
  },
  {
    type: 'edit',
    perm:'bnt.sysMenu.update'
  },
  {
    type: 'delete',
    perm:'bnt.sysMenu.remove'
  }
])

const rules = ref({
  name: [{required: true, message: "请输入名称", trigger: "blur"}],
  url: [{required: true, message: "请输入组件路径", trigger: "blur"}],
})

// methods
const initForm = (notId) => {
  listTreeParents(notId).then(res => {
    treeData.value = res.data;
  })
}

const initMenu = () => {
  listMenuParents().then(res => {
    treeData.value = res.data;
  })
}


function handleAdd() {
  initForm()
  doAdd();
}

function addXj(row) {
  if (row.type == '1') {
    initMenu()
  } else {
    initForm();
  }
  doAdd(row);
}

function setParentMenu(val) {
  if (val == '2') {
    initMenu()
  } else {
    initForm();
  }
}

function handleEdit(row) {
  if (row.type == '2') {
    initMenu()
  } else {
    initForm(row.id);
  }
  doEdit(getById, row.id);
}

function handleDelete(ids, callback) {
  doRemove(remove, ids, callback)
}

function handleSubmit() {
  if (!form.parentId || form.parentId == null) {
    form.parentId = '0'
  }
  if(form.parentId == '0'){
    form.component='Layout'
  }else if(form.type=='0'){
    form.component='ParentView'
  }
  doSubmit({save, update});
}

/*onMounted(() => {
  initForm();
});*/
</script>

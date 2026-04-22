<template>
  <div class="main-body">
    <!--工具栏-->
    <div class="toolbar">
      <el-form :inline="true" :model="filters">
        <el-form-item>
          <el-input v-model="filters.roleName" placeholder="角色名"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button icon="search" type="primary" :disabled="$hasBP('bnt.sysRole.list')  === false" @click="doSearch">查询</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="refreshForm" :disabled="$hasBP('bnt.sysRole.list')  === false" icon="refresh">重置</el-button>
        </el-form-item>
        <el-form-item>
          <el-button icon="plus" type="primary" :disabled="$hasBP('bnt.sysRole.add')  === false" @click="doAdd">添加</el-button>
        </el-form-item>
      </el-form>
    </div>
    <!--表格内容栏-->
    <cm-table
        ref="tableRef"
        :get-page="listPage"
        :filters="filters"
        :columns="columns"
        :operations="operations"
        @handleEdit="handleEdit"
        @handleDelete="handleDelete"
    ></cm-table>
  </div>
  <!-- 新增/编辑 -->
  <el-dialog
      :title="isEdit ? '编辑' : '添加'"
      width="40%"
      draggable
      v-model="dialogVisible"
      :close-on-click-modal="false"
      @close="doClose"
  >
    <el-form ref="formRef" :model="form" label-width="80px" :rules="rules">
      <el-form-item label="角色名称" prop="roleName">
        <el-input v-model="form.roleName"></el-input>
      </el-form-item>
      <el-form-item label="备注" prop="description">
        <el-input v-model="form.description" type="textarea"></el-input>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="doClose">取消</el-button>
        <el-button
            type="primary"
            @click="handleSubmit"
            :loading="formLoading"
        >提交
        </el-button>
      </div>
    </template>
  </el-dialog>

  <!-- 绑定资源 -->
  <el-dialog
      title="绑定菜单"
      width="40%"
      draggable
      v-model="bindDlgVisible"
      :close-on-click-modal="false"
  >
    <div style="margin-top:-35px">
    <el-button @click="selectAll">全选</el-button>
    <el-button @click="deselectAll">反选</el-button>
    </div>
    <el-tree
        ref="treeRef"
        :data="resourceTreeData"
        show-checkbox
        node-key="id"
        default-expand-all
        :check-strictly="true"
        :default-checked-keys="defaultCheckedKeys"
        :props="{label: 'val'}"
    />
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="bindDlgVisible = false">取消</el-button>
        <el-button
            type="primary"
            @click="onBindResouceConfirm"
            :loading="bindLoading"
        >提交
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import {listPage, save, update, remove, bindResouce, getById} from "@/apis/app-role";
import {listTree, getResourceByRoleId} from "@/apis/app-resource";
import useTableHandlers from '@/apis/use-table-handlers'


const filters = reactive({
  roleName: ''
});
const form = reactive({
  id: "",
  roleName: "",
  description: "",
})
const {
  tableRef,
  dialogVisible,
  isEdit,
  formLoading,
  formRef,
  doSearch,
  doAdd,
  doEdit,
  doRemove,
  doSubmit,
  doClose
} = useTableHandlers(form);
const resourceTreeData = ref([])
const bindDlgVisible = ref(false)
const defaultCheckedKeys = ref([])
const bindLoading = ref(false)
const treeRef = ref()
const bindRoleId = ref()

// computed
const columns = computed(() => [
  {type: 'selection'},
  /*{ prop: "id", label: t("thead.ID"), minWidth: 50 },*/
  {prop: "roleName", label: "角色名称", minWidth: 120},
  {prop: "description", label: "备注", minWidth: 120, showOverflowTooltip: true},
]);
const operations = computed(() => [
  {
    type: 'edit',
    perm:'bnt.sysRole.update'
  },
  {
    label: "绑定菜单",
    onClick: handleBindResource,
    perm:'bnt.sysRole.assignAuth'
  },
  {
    type: 'delete',
    perm:'bnt.sysRole.remove'
  }
])
const rules = computed(() => {
  return {
    roleName: [{required: true, message: "请输入角色名称", trigger: "blur"}],
  }
});

// 全选
function selectAll() {
  const checkedKeys = [];
  traverseTreeData(resourceTreeData.value, node => {
    checkedKeys.push(node.id);
  });
  defaultCheckedKeys.value = checkedKeys;
}






function deselectAll(){
  defaultCheckedKeys.value = [];
  treeRef.value.setCheckedKeys(defaultCheckedKeys.value);
};

// 递归遍历树数据
function traverseTreeData(data, callback) {
  for (const node of data) {
    callback(node);
    if (node.children && node.children.length > 0) {
      traverseTreeData(node.children, callback);
    }
  }
}

// methods
function handleDelete(ids, callback) {
  doRemove(remove, ids, callback)
}

function handleEdit(row) {
  doEdit(getById, row.id)
}

function handleSubmit() {
  doSubmit({save, update});
}

function refreshForm() {
  filters.roleName = ''
  doSearch()
}

function handleBindResource(row) {
  bindDlgVisible.value = true;
  bindRoleId.value = row.id
  getResourceByRoleId(row.id).then(res => {
    defaultCheckedKeys.value = res.data
    nextTick(() => {
      treeRef.value.setCheckedKeys(defaultCheckedKeys.value);
    });
  })
  /*defaultCheckedKeys.value = row.resourceIds ? row.resourceIds.split(',') : []*/
}

function getResourceTree() {
  listTree().then(res => {
    resourceTreeData.value = res.data;
  })
}

function onBindResouceConfirm() {
  bindLoading.value = true
  bindResouce({roleId: bindRoleId.value, menuIdList: treeRef.value.getCheckedKeys()}).then(() => {
    ElMessage({message: '授权成功', type: 'success', shoClose: true});
    bindDlgVisible.value = false;
  }).finally(() => {
    bindLoading.value = false
  })
}

onMounted(() => {
  getResourceTree();
});
</script>

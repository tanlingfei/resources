<template>
  <div class="main-body">
    <!--工具栏-->
    <div class="toolbar">
      <el-form :inline="true" :model="filters">
        <el-form-item>
          <el-input v-model="filters.keyword" placeholder="用户名"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button icon="search" type="primary" :disabled="$hasBP('bnt.sysUser.list')  === false" @click="doSearch">
            查询
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="refreshForm" :disabled="$hasBP('bnt.sysUser.list')  === false"
                     icon="refresh">重置
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button icon="plus" type="primary" :disabled="$hasBP('bnt.sysUser.add')  === false" @click="handleAdd">
            添加
          </el-button>
        </el-form-item>
      </el-form>
    </div>
    <!--表格内容栏-->
    <cm-table
        ref="tableRef"
        :get-page="api.listPage"
        :filters="filters"
        :columns="columns"
        :operations="operations"
        @handleEdit="handleEdit"
        @handleDelete="handleDelete"
    />
  </div>
  <!--新增编辑界面-->
  <el-dialog
      :title="isEdit ? '编辑' : '添加'"
      v-model="dialogVisible"
      draggable
      width="50%"
      :close-on-click-modal="false"
      @close="doClose"
  >
    <el-form ref="formRef" :model="form" label-width="80px" :rules="rules" label-position="right">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="form.username"></el-input>
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input v-model="form.password" type="password"></el-input>
      </el-form-item>
      <el-form-item label="姓名" prop="name">
        <el-input v-model="form.name"></el-input>
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="form.email"></el-input>
      </el-form-item>
      <el-form-item label="手机" prop="mobile">
        <el-input v-model="form.mobile"></el-input>
      </el-form-item>
      <el-form-item label="地址" prop="mobile">
        <el-input v-model="form.address"></el-input>
      </el-form-item>
      <el-form-item label="角色" prop="roleList">
        <el-select
            v-model="form.roleList"
            multiple
            placeholder="选择角色"
            style="width: 100%"
        >
          <el-option v-for="item in roles" :key="item.id" :label="item.roleName" :value="item.id"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-switch v-model="form.status"></el-switch>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="doClose">
        取消
      </el-button>
      <el-button
          type="primary"
          @click="handleSubmit"
          :loading="formLoading"
      >提交
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import api from "@/apis/app-user";
import {listSimple} from "@/apis/app-role";
import useTableHandlers from '@/apis/use-table-handlers'


const filters = reactive({
  keyword: '',
  classId: ''
})
const form = reactive({
  id: '',
  username: '',
  password: '',
  classId:'',
  name: "",
  email: "",
  mobile: "",
  address: "",
  roleList: [],
  status: true,
});
const {
  tableRef,
  dialogVisible,
  isEdit,
  formLoading,
  formRef,
  doSearch,
  doAdd,
  doEdit,
  doSubmit,
  doRemove,
  doClose
} = useTableHandlers(form);

const deptData = ref([])
const roles = ref([]);
let tbClassXc53d7587List = ref([])
// computed
const columns = computed(() => [
  {type: "selection"},
  /* { prop: "id", label: t("thead.ID"), minWidth: 50 },*/
  {prop: "username", label: "用户名", minWidth: 120, showOverflowTooltip: true},
  {prop: "name", label: "姓名", minWidth: 120, showOverflowTooltip: true},
  {prop: "roleNames", label: "角色", minWidth: 120, showOverflowTooltip: true},
  {prop: "mobile", label: "手机", minWidth: 100, showOverflowTooltip: true},
  {prop: "address", label: "地址", minWidth: 100, showOverflowTooltip: true},
  {prop: "email", label: "邮箱", minWidth: 100, showOverflowTooltip: true},
  {
    prop: "status", label: "状态", minWidth: 70, formatter: (row) => {
      return row.status ? "正常" : "禁用"
    }
  },
])
const contactValidator = (rule, value, callback) => {
  if (!form.email && !form.mobile) {
    callback(new Error("邮箱和手机必须填写一个"))
  }
  (!form.mobile || !form.email) && formRef.value.clearValidate(rule.field === 'email' ? 'mobile' : 'email')
  callback()
}

const operations = computed(() => [
  {
    type: 'edit',
    perm: 'bnt.sysUser.update'
  },
  {
    type: 'delete',
    perm: 'bnt.sysUser.remove'
  }
])

const rules = computed(() => {
  return {
    username: [
      {required: true, message: "请输入用户名", trigger: ['change', 'blur']}
    ],
    password: [
      {required: true, message: "请输入密码", trigger: ['change', 'blur']}
    ],
    email: [
      {type: 'email', message: "邮箱格式错误", trigger: ['change', 'blur']},
      {
        validator: contactValidator, message: "手机和邮箱必须填写一个", trigger: ['change', 'blur']
      }
    ],
    mobile: [
      {pattern: /^1[3-9]\d{9}$/, message: "手机格式错误", trigger: ['change', 'blur']},
      {
        validator: contactValidator, message: "手机和邮箱必须填写一个", trigger: ['change', 'blur']
      }
    ],
    roleList: [
      {required: true, message: "请选择角色", trigger: ['change', 'blur']},
    ]
  }
});

// 初始化部门数据和角色数据
function initFormRequest() {
  findRoles();
}

function refreshForm() {
  filters.keyword = ''
  filters.classId = ''
  doSearch()
}

function handleAdd(row) {
  doAdd(row);
}

function handleEdit(row) {
  initFormRequest();
  doEdit(api.getById, row.id);
  // form.roleList = row.roleIds.split(',')
}


function findRoles() {
  listSimple().then(res => {
    roles.value = res.data;
  })
}

function handleDelete(ids, callback) {
  doRemove(api.remove, ids, callback)
}

function handleSubmit() {
  doSubmit({save: api.save, update: api.update, getParams}, (res) => {
    ElMessage({message: '操作成功', type: "success"});
  });
}

function getParams() {
  const params = {...form}
  if (!isEdit.value) {
    delete params.id;
  }
  return params
}


onMounted(() => {
  initFormRequest();
});

</script>

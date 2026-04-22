<template>
  <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" label-position="left">
    <el-form-item label="用户名" prop="username">
      <el-input v-model="form.username"></el-input>
    </el-form-item>
    <el-form-item label="用户姓名" prop="name">
      <el-input v-model="form.name"></el-input>
    </el-form-item>
    <el-form-item label="手机" prop="mobile">
      <el-input v-model="form.mobile"></el-input>
    </el-form-item>
    <el-form-item label="邮箱" prop="email">
      <el-input v-model="form.email"></el-input>
    </el-form-item>

  </el-form>
    <el-button
        type="primary"
        @click="handleSubmit"
    >保存
    </el-button>
</template>
<script setup>
import api from "@/apis/app-user";
const form = reactive({
  name: '',
  username: '',
  mobile: '',
  email: '',
  deptName: '',
  id:'',
  kolZh: '',
  goods: ''
});
import useTableHandlers from '@/apis/use-table-handlers'
const {
  tableRef,
  dialogVisible,
  isEdit,
  isRefresh,
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
const store = useStore();
const contactValidator = (rule, value, callback) => {
  if (!form.email && !form.mobile) {
    callback(new Error("邮箱和手机必须填写一个"))
  }
  (!form.mobile || !form.email) && formRef.value.clearValidate(rule.field === 'email' ? 'mobile' : 'email')
  callback()
}
const rules = computed(() => {
  return {
    username: [
      {required: true, message: "请输入用户名", trigger: ['change', 'blur']}
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
    ]
  }
});
const profile = computed(() => store.state.user.userInfo)

watch(profile, () => {
  updateForm();
}, { immediate: true })

function getParams() {
  const params = {...form}
  return params
}

function updateForm() {
  if(!profile.value.id){
    return;
  }
  api.getById(profile.value.id).then(response => {
    const row = response.data
    if (row) {
      for (const k in form) {
        if (k in row) {
          form[k] = row[k];
        }
      }
      isEdit.value = true;
      isRefresh.value=false;
    }
  })

}


function handleSubmit() {
  doSubmit({save: api.save, update: api.update,getParams}, (res) => {
    ElMessage({message: '保存成功', type: "success"});
    updateForm()
  });
}

</script>

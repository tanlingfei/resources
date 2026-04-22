<template>
  <div class="page flex-center">
    <div class="sign-box">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="86px">
        <h3 class="title">登录</h3>
        <el-form-item label="用户名" prop="username">
          <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              prefix-icon="user"
          ></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="lock"
          ></el-input>
        </el-form-item>
        <el-form-item label>
          <el-button
              type="primary"
              :loading="loading"
              style="width:30%;float:left"
              @click="doLogin"
          >登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>
<script setup>
import {login} from '@/apis/login'

const router = useRouter();
const dialogVisible = ref(false)
const adminPhone = ref('')

function doView(){
  dialogVisible.value = true;
}

function doClose() {
  dialogVisible.value = false;
}


function toRegister(){
  router.push("/register")
}

const formRef = ref();
const form = reactive({
  username: "",
  password: "",
})
const rules = computed(() => {
  return {
    username: [
      {
        required: true,
        message: "请输入用户名",
        trigger: ["change", "blur"],
      },
    ],
    password: {
      required: true,
      min: 4,
      message: "密码至少4位",
      trigger: ["change", "blur"],
    },
  }
});
const loading = ref(false)
const store = useStore();

function doLogin() {
  formRef.value.validate((valid) => {
    if (!valid) return;
    loading.value = true;
    login(form).then((res) => {
      store.commit('user/setToken', res.data.token);
      store.dispatch('user/refreshInfo');
      store.commit("setRouteLoaded", false);
      // localStorage.setItem('pm_token', res.data.token)
      router.push("/");
    }).finally(() => {
      loading.value = false;
    })
  });
}


</script>
<style lang="scss">
.page {
  height: 100%;
  background: #2c3b4b;
  background-size: cover;
}

.sign-box {
  width: 400px;
  background: #fff;
  padding: 30px 50px 20px 30px;
  border-radius: 4px;
  box-shadow: 0 0 10px #022c44;
}

.title {
  text-align: center;
  font-size: 20px;
  line-height: 30px;
  margin-top: 0;
  margin-bottom: 10px;
  color: #000;
}
</style>

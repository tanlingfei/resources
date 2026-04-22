<template>
  <div class="header-cont">
    <div class="left">
      <h1>
        <router-link to="/">糖糖资源站</router-link>
        <div class="brand-slogan">数字技术改变创作未来</div>
      </h1>
    </div>
    <div class="right flex-center">
      <div class="lang gap">
<!--        <span
          class="item"
          :class="{ active: locale === 'zh-cn' }"
          @click="changeLanguage('zh-cn')"
        >简体中文</span>
        /
        <span
          class="item"
          :class="{ active: locale === 'en' }"
          @click="changeLanguage('en')"
        >EN</span>-->
      </div>
      <template v-if="isLogin">
        <el-dropdown trigger="click" @command="handleCommand">
          <div class="flex-center cursor">
            {{ username }}
            <el-icon>
              <caret-bottom />
            </el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="toPersonal">用户中心</el-dropdown-item>
              <el-dropdown-item command="toLogout">退出</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </template>
      <template v-else-if="$route.name !== 'Login'">
        <router-link to="/login">登录</router-link>
      </template>
    </div>
  </div>
</template>
<script setup>
import { logout } from '@/apis/login'
/*const { locale, t } = useI18n();*/
// 语言切换
/*function changeLanguage(lang) {
  locale.value = lang
  localStorage.setItem('locale', lang)
}*/

const store = useStore();
const isLogin = computed(() => store.getters['user/isLogin']);
const userInfo = computed(() => store.state.user.userInfo);
const username = computed(() => userInfo.value?.name)

store.dispatch('user/refreshInfo');

const router = useRouter();
const commands = ({
  toPersonal: () => {
    router.push('/personal')
  },
  toLogout: () => {
    logout().then(res => {
      if (res.code == 200) {
        store.commit('user/clearToken');
        store.commit('user/clearUserInfo');
        // router.push('/login')
        window.location = '/';
      }
    })
  }
});
function handleCommand(command) {
  commands[command] && commands[command]();
}
</script>
<style lang="scss">
.header-cont {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 100%;
  background: linear-gradient(135deg, #ff6b6b 0%, #c44569 100%);
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  color: white;
  font-family: 'Helvetica Neue', Arial, sans-serif;

  a {
    color: inherit;
    text-decoration: none;
  }
  h1 {
    margin: 0;
    font-size: 20px;
    display: flex;
    flex-direction: column;
    font-weight: 600;

    .brand-slogan {
      font-size: 12px;
      font-weight: normal;
      opacity: 0.9;
      margin-top: 2px;
    }
  }
  .gap {
    margin-right: 20px;
  }
  .right {
    .lang {
      font-size: 14px;
      .item {
        cursor: pointer;
        &.active {
          font-size: 18px;
          font-weight: bold;
        }
      }
    }
  }
  .el-dropdown {
    color: inherit;
  }

  .flex-center.cursor {
    padding: 5px 10px;
    border-radius: 4px;
    transition: background-color 0.3s;

    &:hover {
      background-color: rgba(255, 255, 255, 0.1);
    }
  }
}
</style>

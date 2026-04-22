<template>
  <div id="app">
    <router-view class="main-content" />
  </div>
</template>

<script setup>
document.title = '糖糖资源站';
</script>

<style>
* {
  box-sizing: border-box;
}

#app {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100vh;
}

.main-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0;
  width: 100%;
}

/* 全局卡片样式优化 */
.el-card {
  margin-bottom: 20px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: none;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.08);
}

.el-card:hover {
  box-shadow: 0 8px 24px 0 rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

/* 全局按钮样式优化 */
.el-button {
  transition: all 0.3s ease;
  border-radius: 8px;
}

.el-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 全局输入框样式优化 */
.el-input__inner {
  transition: all 0.3s ease;
}

.el-input__inner:focus {
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content {
    padding: 0 10px;
  }
}
</style>

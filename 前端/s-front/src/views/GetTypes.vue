<template>
  <div class="get-types-container">
    <el-card class="main-card">
      <div class="loading-section" v-if="loading">
        <el-icon class="loading-icon"><Loading /></el-icon>
        <p class="loading-text">正在处理中...</p>
        <p class="loading-subtext">请稍候，程序正在自动处理</p>
      </div>

      <div class="error-section" v-else-if="error">
        <el-icon class="error-icon"><CircleClose /></el-icon>
        <p class="error-text">{{ error }}</p>
        <div class="error-actions">
          <el-button type="primary" @click="retryGetClipboard" class="retry-btn">
            <el-icon><Refresh /></el-icon> 重新尝试
          </el-button>
          <el-button @click="goBack" class="back-btn">
            <el-icon><ArrowLeft /></el-icon> 返回
          </el-button>
        </div>
      </div>

      <div class="success-section" v-else-if="link">
        <el-icon class="success-icon"><CircleCheck /></el-icon>
        <p class="success-text">链接已获取</p>
        <p class="auto-jump-text">正在自动跳转...</p>
        <div class="manual-jump-section">
          <p class="manual-hint">如果页面没有自动跳转，请点击下方按钮</p>
          <el-button type="primary" @click="openLink" class="jump-btn" :loading="jumping">
            <el-icon><Link /></el-icon> {{ jumping ? '正在跳转...' : '点击跳转' }}
          </el-button>
        </div>
      </div>

      <div class="initial-section" v-else>
        <el-icon class="clipboard-icon"><DocumentCopy /></el-icon>
        <p class="initial-text">请复制包含素材ID的内容</p>
        <p class="format-hint">格式示例：链接#素材ID</p>
        <el-button type="primary" @click="retryGetClipboard" class="retry-btn">
          <el-icon><Refresh /></el-icon> 获取剪贴板内容
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading, CircleClose, CircleCheck, DocumentCopy, Link, Refresh, ArrowLeft } from '@element-plus/icons-vue'
import { post } from '@/utils/request'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(true)
const error = ref('')
const link = ref('')
const jumping = ref(false)
let retryInterval = null

// 页面加载时自动获取剪贴板内容
onMounted(() => {
  getClipboardAndProcess()
  // 定期重试，直到成功获取
  retryInterval = setInterval(() => {
    if (!loading.value && !link.value && !jumping.value) {
      getClipboardAndProcess()
    }
  }, 2000)

  // 检测是否在夸克浏览器中
  checkQuarkBrowser()
})

onUnmounted(() => {
  if (retryInterval) {
    clearInterval(retryInterval)
  }
})

// 检查是否在夸克浏览器中
const checkQuarkBrowser = () => {
  const userAgent = navigator.userAgent.toLowerCase()
  if (userAgent.includes('quark') || userAgent.includes('ucbrowser')) {
    console.log('在夸克浏览器中运行')
  } else {
    ElMessage.warning('请在夸克APP中打开此页面以获得最佳体验')
  }
}

// 获取剪贴板内容并处理
const getClipboardAndProcess = async () => {
  loading.value = true
  error.value = ''
  link.value = ''

  try {
    // 检查浏览器是否支持剪贴板 API
    if (!navigator.clipboard || !navigator.clipboard.readText) {
      error.value = '无法访问剪贴板，请手动输入素材ID'
      loading.value = false
      return
    }

    // 获取剪贴板内容
    const clipboardText = await navigator.clipboard.readText()

    if (!clipboardText) {
      error.value = '剪贴板为空，请先复制包含素材ID的内容'
      loading.value = false
      return
    }

    // 按 # 分割
    const parts = clipboardText.split('#')

    if (parts.length < 2) {
      error.value = '剪贴板内容格式不正确，请复制包含 # 分隔符的内容'
      loading.value = false
      return
    }

    // 获取第二个部分（id）
    const id = parts[1].trim()

    if (!id) {
      error.value = '无法获取有效的素材ID'
      loading.value = false
      return
    }

    // 调用接口获取 types
    await fetchTypesById(clipboardText)

  } catch (err) {
    console.error('处理剪贴板内容失败:', err)
    if (err.name === 'NotAllowedError') {
      error.value = '请允许页面访问剪贴板权限'
    } else {
      error.value = '获取剪贴板内容失败，请重试'
    }
    loading.value = false
  }
}

// 根据 id 获取 types
const fetchTypesById = async (id) => {
  try {
    const res = await post(`/business/commodityCommodityinfos/getTypes?text=${id}`, {})

    if (res.code === 200) {
      const types = res.data

      if (!types) {
        error.value = '未找到该素材的链接信息'
        loading.value = false
        return
      }

      link.value = types
      loading.value = false

      // 自动跳转
      setTimeout(() => {
        openLink()
      }, 800)

    } else {
      error.value = res.msg || '获取链接失败'
      loading.value = false
    }
  } catch (err) {
    console.error('获取链接失败:', err)
    error.value = '网络请求失败，请检查网络连接'
    loading.value = false
  }
}

// 打开链接（夸克专用）
const openLink = () => {
  if (link.value) {
    jumping.value = true
    try {
      // 尝试直接在当前窗口打开，夸克会处理
      window.location.href = link.value
    } catch (err) {
      console.error('跳转失败:', err)
      // 降级方案：在新窗口打开
      window.open(link.value, '_blank')
    } finally {
      setTimeout(() => {
        jumping.value = false
      }, 2000)
    }
  }
}

// 重试获取剪贴板
const retryGetClipboard = () => {
  getClipboardAndProcess()
}

// 返回
const goBack = () => {
  router.back()
}
</script>

<style scoped>
.get-types-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.main-card {
  width: 100%;
  max-width: 90%;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  border: none;
  background: white;
}

.loading-section,
.error-section,
.success-section,
.initial-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 20px;
  text-align: center;
  min-height: 300px;
  justify-content: center;
}

.loading-icon {
  font-size: 64px;
  color: #409eff;
  animation: rotate 1s linear infinite;
  margin-bottom: 20px;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.loading-text {
  font-size: 18px;
  color: #303133;
  font-weight: 600;
  margin-bottom: 8px;
}

.loading-subtext {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.error-icon {
  font-size: 64px;
  color: #f56c6c;
  margin-bottom: 20px;
}

.error-text {
  font-size: 16px;
  color: #f56c6c;
  margin-bottom: 24px;
  line-height: 1.5;
}

.error-actions {
  display: flex;
  gap: 12px;
  width: 100%;
  max-width: 300px;
  justify-content: center;
}

.success-icon {
  font-size: 64px;
  color: #67c23a;
  margin-bottom: 20px;
}

.success-text {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.auto-jump-text {
  font-size: 14px;
  color: #909399;
  margin-bottom: 30px;
}

.manual-jump-section {
  width: 100%;
  max-width: 300px;
}

.manual-hint {
  font-size: 14px;
  color: #606266;
  margin-bottom: 16px;
}

.jump-btn {
  width: 100%;
  padding: 14px 24px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s ease;
}

.jump-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
}

.clipboard-icon {
  font-size: 64px;
  color: #909399;
  margin-bottom: 20px;
}

.initial-text {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.format-hint {
  font-size: 14px;
  color: #909399;
  margin-bottom: 24px;
}

.retry-btn {
  padding: 14px 24px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s ease;
}

.retry-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.back-btn {
  border-radius: 8px;
  transition: all 0.3s ease;
}

.back-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* 移动端适配 */
@media (max-width: 768px) {
  .main-card {
    max-width: 95%;
  }

  .loading-section,
  .error-section,
  .success-section,
  .initial-section {
    padding: 30px 15px;
    min-height: 250px;
  }

  .loading-icon,
  .error-icon,
  .success-icon,
  .clipboard-icon {
    font-size: 48px;
  }

  .loading-text,
  .success-text,
  .initial-text {
    font-size: 16px;
  }

  .loading-subtext,
  .error-text,
  .auto-jump-text,
  .manual-hint,
  .format-hint {
    font-size: 13px;
  }

  .jump-btn,
  .retry-btn {
    padding: 12px 20px;
    font-size: 15px;
  }

  .error-actions {
    flex-direction: column;
    gap: 8px;
  }
}

/* 夸克浏览器特定样式 */
@media screen and (max-width: 768px) {
  body {
    -webkit-tap-highlight-color: transparent;
  }

  .get-types-container {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  }

  .main-card {
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  }

  .jump-btn,
  .retry-btn {
    font-weight: 700;
  }
}
</style>

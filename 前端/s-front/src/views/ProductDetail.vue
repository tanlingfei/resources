<template>
  <div class="product-detail-modern">
    <el-row v-loading="loading" :gutter="20">
      <!-- 返回按钮 -->
      <el-col :span="24">
        <div class="action-buttons-wrapper">
          <el-button type="primary" @click="goToHome" class="back-button" plain>
            <el-icon><ArrowLeft /></el-icon> 返回
          </el-button>
          <el-button
            type="primary"
            link
            @click="goToWx"
            class="wx-link-button"
          >
            <el-icon><ChatDotRound /></el-icon> 关注公众号防失联
          </el-button>
        </div>
      </el-col>

      <!-- 错误提示 -->
      <el-alert
          v-if="error"
          :title="error"
          type="error"
          show-icon
          class="error-alert"
          @close="error = null"
      />

      <!-- 数据展示 -->
      <template v-if="product">
        <!-- 顶部信息横幅 -->
        <el-col :span="24">
          <div class="product-banner">
            <div class="banner-content">
              <h1 class="product-main-title">{{ product.name?.length > 30 ? product.name.substring(0, 30) + '...' : product.name }}</h1>
              <div class="product-tags">
                <el-tag type="info" size="large">
                  <el-icon><User /></el-icon> {{ product.username }}
                </el-tag>
                <el-tag type="info" size="large">
                  <el-icon><Calendar /></el-icon> {{ product.createTime }}
                </el-tag>
              </div>
            </div>
          </div>
        </el-col>

        <!-- 主要内容区域 - 上下布局 -->
        <el-col :span="24">
          <div class="main-content-area">
            <!-- 信息和操作区域 -->
            <div class="info-action-section">
              <div class="info-action-container">
                <!-- 网盘地址 -->
                <div class="download-links-section" >
                  <h2 class="section-title">网盘地址</h2>
                  <div class="download-links-content">
                    <template v-if="product.isUpdate !== '1' && product.likes === 8888">
                      <div class="quark-instructions">
                        <div class="instruction-header">
                          <span class="header-icon">📦</span>
                          <span class="header-text">使用夸克网盘获取资源</span>
                        </div>
                        <div class="instruction-tips">
                          <div class="tip-item">
                            <el-icon class="tip-icon"><Warning /></el-icon>
                            <span>请确保已下载并登录「夸克网盘」APP，仅支持手机端</span>
                          </div>
                          <div class="tip-item">
                            <el-icon class="tip-icon"><Download /></el-icon>
                            <span>未下载？<span @click="downloadQuark" class="download-link">点击下载夸克网盘</span> 或在手机应用商店搜索「夸克」安装</span>
                          </div>
                        </div>
                        <div class="instruction-step">
                          <el-button
                              type="primary"
                              @click="getQuarkResource"
                              class="get-resource-btn"
                              size="large">
                            <el-icon><DocumentCopy /></el-icon> 获取资源
                          </el-button>
                        </div>
                        <div class="instruction-footer">
                          <span class="footer-text">💡 点击按钮后，链接将自动复制并打开夸克，在夸克中即可查看资源</span>
                        </div>
                      </div>
                    </template>
                    <template v-else>
                      <div
                          v-for="(link, index) in downloadLinks"
                          :key="index"
                          class="download-link-item"
                      >
                        <span class="link-name">{{ link.name }}</span>
                        <span class="link-separator">:</span>
                        <a
                            :href="link.url"
                            target="_blank"
                            class="link-url"
                            @click.stop
                        >
                          {{ link.url }}
                        </a>
                      </div>
                    </template>
                  </div>
                </div>

                <!-- 描述信息 -->
                <div class="description-section">
                  <h2 class="section-title">描述</h2>
                  <div class="description-content">
                    <p>{{ product.details || '暂无描述' }}</p>
                  </div>
                </div>
              </div>
            </div>

            <!-- 媒体展示区域 -->
            <div v-if="product.img && (isVideo(product.img) || isImage(product.img))" class="media-display-section">
              <div class="media-container-modern">


                <!-- 媒体内容 -->
                <div class="media-viewer">
                  <!-- 视频格式 -->
                  <video
                      v-if="isVideo(product.img)"
                      :src="product.img"
                      controls
                      class="media-content-modern"
                  ></video>

                  <!-- 图片格式 -->
                  <img
                      v-else-if="isImage(product.img)"
                      :src="product.img"
                      alt="产品图片"
                      class="media-content-modern"
                  >
                </div>
              </div>
            </div>
          </div>
        </el-col>
      </template>

      <!-- 空状态 -->
      <el-col :span="24">
        <el-empty v-if="!product && !loading" description="素材不存在" class="empty-state" />
      </el-col>
    </el-row>

    <!-- 免责声明 -->
    <div class="disclaimer-section">
      <div class="disclaimer-content">
        <div class="disclaimer-title">
          <el-icon><InfoFilled /></el-icon>
          <span>免责声明</span>
        </div>
        <div class="disclaimer-text">
          <p>本站所有资源均来自于网络公开渠道分享，仅供个人学习和交流使用，不得用于任何商业用途。</p>
          <p>本站所有资源版权均归原作者或出版社所有，请下载试用后 24 小时内删除。</p>
          <p>如果您认为本站的内容侵犯了您的权益，请联系站长删除相关内容。</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { get } from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, User, Calendar, DocumentCopy, Warning, Download, ChatDotRound, InfoFilled } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const error = ref(null)
const product = ref(null)
const downloadLinks = ref([])
const shareLink = ref('')

// 页面加载时滚动到顶部
onMounted(() => {
  window.scrollTo(0, 0)
  updatePageTitle()
})

// 监听产品数据变化，更新页面标题
watch(
  () => product.value,
  () => {
    updatePageTitle()
  },
  { deep: true }
)

// 更新页面标题
const updatePageTitle = () => {
  if (product.value) {
    document.title = `${product.value.name} - 糖糖资源站 - 网课考研学习资料下载`
  } else {
    document.title = '资源详情 - 糖糖资源站'
  }
}

// 获取下载地址列表
/*const fetchDownloadLinks = async (commodityId) => {
  try {
    await get(
        `/business/commodityCommodityinfos/downloadLinks/${commodityId}`,
        {},
        (res) => {
          if (res.code === 200) {
            downloadLinks.value = res.data || []
          }
        }
    )
  } catch (err) {
    console.error('获取下载地址失败:', err)
    downloadLinks.value = []
  }
}*/

// 通用数据获取方法
const fetchProduct = async () => {
  try {
    loading.value = true
    product.value = null
    downloadLinks.value = []

    await get(
        `/business/commodityCommodityinfos/getWithLock/${route.params.id}`,
        {},
        (res) => {
          // 根据实际接口结构调整这里
          if (res.code === 200) {
            product.value = res.data.commodityInfo
            downloadLinks.value = res.data.downloadLinks
            shareLink.value = res.data.shareLink || ''
            // 获取下载地址列表
            /*  if (res.data && res.data.id) {
                fetchDownloadLinks(res.data.id)
              }*/
          } else {
            throw new Error(res.msg || '素材数据异常')
          }
        }
    )
  } catch (err) {
    error.value = err.message || '获取素材信息失败'
  } finally {
    loading.value = false
  }
}

// 初始加载
fetchProduct()

// 监听路由参数变化
watch(
    () => route.params.id,
    (newId) => {
      if (newId) fetchProduct()
    }
)

// 判断是否为视频格式
const isVideo = (url) => {
  if (!url) return false
  const videoExtensions = ['.mp4', '.avi', '.mov', '.wmv', '.flv', '.webm']
  return videoExtensions.some(ext => url.toLowerCase().includes(ext))
}

// 判断是否为图片格式
const isImage = (url) => {
  if (!url) return false
  const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp']
  return imageExtensions.some(ext => url.toLowerCase().includes(ext))
}

// 判断是否为移动端
const isMobile = () => {
  return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)
}

// 判断是否在微信中打开
const isWechat = () => {
  const ua = navigator.userAgent.toLowerCase()
  return ua.indexOf('micromessenger') !== -1
}

// 获取夸克资源
const getQuarkResource = async () => {
  if (!isMobile()) {
    ElMessage.warning('此功能仅支持手机端操作，请在手机上打开此页面')
    return
  }

  if (!shareLink.value) {
    ElMessage.warning('暂无分享链接')
    return
  }

  try {
    await navigator.clipboard.writeText(shareLink.value)
  } catch (err) {
    console.error('复制失败:', err)
    const input = document.createElement('input')
    input.value = shareLink.value
    document.body.appendChild(input)
    input.select()
    document.execCommand('copy')
    document.body.removeChild(input)
  }

  if (isWechat()) {
    ElMessageBox.alert(
      '分享已复制,由于微信环境限制，请手动打开夸克网盘APP查看资源。',
      '温馨提示',
      {
        confirmButtonText: '我知道了',
        type: 'info'
      }
    )
  } else {
    ElMessage.success('即将打开夸克网盘...')
    setTimeout(() => {
      openQuark()
    }, 300)
  }
}

// 下载夸克网盘
const downloadQuark = async () => {
  if (!isMobile()) {
    ElMessage.warning('此功能仅支持手机端操作，请在手机上打开此页面')
    return
  }
  if (!shareLink.value) {
    ElMessage.warning('暂无分享链接')
    return
  }

  try {
    await navigator.clipboard.writeText(shareLink.value)
  } catch (err) {
    console.error('复制失败:', err)
    const input = document.createElement('input')
    input.value = shareLink.value
    document.body.appendChild(input)
    input.select()
    document.execCommand('copy')
    document.body.removeChild(input)
  }

  window.location.href = 'https://pan.quark.cn/'
}

// 打开夸克
const openQuark = () => {
  if (isMobile()) {
    const quarkScheme = 'quark://'
    const hiddenIframe = document.createElement('iframe')
    hiddenIframe.style.display = 'none'
    hiddenIframe.src = quarkScheme
    document.body.appendChild(hiddenIframe)

    let timer = null
    let hasOpenedApp = false

    const handleVisibilityChange = () => {
      if (document.hidden) {
        hasOpenedApp = true
        clearTimeout(timer)
      }
    }

    document.addEventListener('visibilitychange', handleVisibilityChange)

    timer = setTimeout(() => {
      document.removeEventListener('visibilitychange', handleVisibilityChange)
      document.body.removeChild(hiddenIframe)

      if (!hasOpenedApp) {
        ElMessageBox.confirm(
          '检测到您可能未安装夸克网盘或未允许打开。您可以：\n\n• 点击「确定」打开夸克网盘网页版\n• 点击「取消」关闭此提示，手动打开夸克网盘',
          '温馨提示',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'info'
          }
        ).then(() => {
          window.location.href = 'https://pan.quark.cn/'
        }).catch(() => {
        })
      }
    }, 5000)
  } else {
    window.open('https://pan.quark.cn/', '_blank')
  }
}


// 跳转到公众号页面
const goToWx = () => {
  router.push('/wx')
}

// 返回首页
const goToHome = () => {
  router.back();
}




</script>

<style scoped>
.product-detail-modern {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
  background: #f8fafc;
  min-height: calc(100vh - 120px);
}

.action-buttons-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.back-button {
  border-radius: 8px;
  padding: 12px 20px;
}

.wx-link-button {
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.error-alert {
  margin-bottom: 20px;
  border-radius: 8px;
}

/* 产品横幅 */
.product-banner {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  color: #333;
  border-radius: 16px;
  padding: 35px 40px;
  margin-bottom: 25px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.product-main-title {
  font-size: 2.2rem;
  margin: 0 0 18px 0;
  font-weight: 600;
  letter-spacing: 0.5px;
  line-height: 1.3;
  color: #2c3e50;
}

.product-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.product-tags :deep(.el-tag) {
  border: none;
  border-radius: 20px;
  padding: 10px 18px;
  font-size: 0.95rem;
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

/* 主内容区域 */
.main-content-area {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

@media (min-width: 769px) {
  .main-content-area {
    display: grid;
    grid-template-columns: 1fr;
    gap: 25px;
  }
}

/* 媒体展示区域 */
.media-display-section {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.media-container-modern {
  display: flex;
  flex-direction: column;
}

.file-metadata {
  background: #f8fbff;
  padding: 25px;
  border-bottom: 1px solid #eee;
}

.metadata-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.metadata-item {
  text-align: center;
  padding: 15px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.metadata-label {
  font-size: 0.9rem;
  color: #666;
  margin-bottom: 8px;
}

.metadata-value {
  font-size: 1.2rem;
  font-weight: 600;
  color: #333;
}

.media-viewer {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  min-height: 400px;
  padding: 30px;
  background: #f8f9fa;
}

.media-content-modern {
  max-width: 85%;
  max-height: 550px;
  object-fit: contain;
  border-radius: 12px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
}

/* 信息和操作区域 */
.info-action-section {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.info-action-container {
  padding: 30px;
}

.section-title {
  font-size: 1.4rem;
  color: #2c3e50;
  margin: 0 0 18px 0;
  padding-bottom: 12px;
  border-bottom: 2px solid #e8ecf1;
  font-weight: 600;
  position: relative;
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 60px;
  height: 2px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
}

/* 描述区域 */
.description-section {
  margin-bottom: 30px;
}

.description-content {
  background: linear-gradient(135deg, #f8fbff 0%, #f0f4ff 100%);
  border-radius: 12px;
  padding: 25px;
  font-size: 1.05rem;
  line-height: 1.8;
  color: #4a5568;
  border-left: 4px solid #667eea;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.description-content p {
  margin: 0;
}

/* 网盘地址区域 */
.download-links-section {
  margin-bottom: 30px;
}

.download-links-content {
  background: linear-gradient(135deg, #f8fbff 0%, #f0f4ff 100%);
  border-radius: 12px;
  padding: 25px;
  border-left: 4px solid #4facfe;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.download-link-item {
  display: flex;
  align-items: center;
  padding: 15px;
  margin-bottom: 12px;
  background: white;
  border-radius: 8px;
  border-left: 4px solid #667eea;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.download-link-item:last-child {
  margin-bottom: 0;
}

.download-link-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateX(5px);
}

.link-name {
  font-weight: 600;
  color: #333;
  font-size: 1rem;
  min-width: 100px;
}

.link-separator {
  margin: 0 10px;
  color: #999;
}

.link-url {
  flex: 1;
  color: #667eea;
  text-decoration: none;
  word-break: break-all;
  font-size: 0.95rem;
  transition: color 0.3s ease;
}

.link-url:hover {
  color: #764ba2;
  text-decoration: underline;
}

/* 联系信息区域 */
.contact-info {
  text-align: center;
  padding: 15px 20px;
}

/* 夸克使用说明区域 */
.quark-instructions {
  background: linear-gradient(135deg, #fff5f5 0%, #fff0f0 100%);
  border-radius: 12px;
  padding: 25px;
  border-left: 4px solid #ff6b6b;
  box-shadow: 0 2px 8px rgba(255, 107, 107, 0.1);
}

.instruction-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
  padding-bottom: 12px;
  border-bottom: 1px dashed #ffd1d1;
  justify-content: flex-start;
}

.header-icon {
  font-size: 1.5rem;
}

.header-text {
  font-size: 1.2rem;
  font-weight: 600;
  color: #2c3e50;
}

.instruction-tips {
  background: rgba(255, 255, 255, 0.7);
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 15px;
  text-align: left;
}

.tip-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  font-size: 0.95rem;
  color: #4a5568;
}

.tip-item:first-child {
  color: #e67e22;
}

.tip-icon {
  font-size: 1.1rem;
  flex-shrink: 0;
}

.download-link {
  color: #667eea;
  text-decoration: underline;
  font-weight: 500;
}

.download-link:hover {
  color: #764ba2;
}

.instruction-step {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  padding: 15px 0;
}

.button-group {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.download-quark-btn {
  padding: 12px 32px;
  font-size: 1.1rem;
  font-weight: 600;
  border-radius: 8px;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, #4caf50 0%, #43a047 100%);
  border: none;
}

.download-quark-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(76, 175, 80, 0.5);
}

.instruction-footer {
  margin-top: 15px;
  padding-top: 12px;
  border-top: 1px dashed #ffd1d1;
}

.footer-text {
  font-size: 0.9rem;
  color: #718096;
  display: block;
  text-align: left;
}

.quark-code {
  font-size: 1.1rem;
  font-weight: 700;
  color: #667eea;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
  padding: 8px 15px;
  border-radius: 8px;
  font-family: monospace;
  letter-spacing: 1px;
}

.verify-code {
  font-size: 1.6rem;
  font-weight: 700;
  color: #ff0000;
  background: linear-gradient(135deg, #fff5f5 0%, #ffe6e6 100%);
  padding: 6px 12px;
  border-radius: 8px;
  font-family: monospace;
  letter-spacing: 2px;
  box-shadow: 0 2px 8px rgba(255, 0, 0, 0.2);
  animation: pulse-red 2s infinite;
  display: inline-block;
}

@keyframes pulse-red {
  0%, 100% {
    box-shadow: 0 2px 8px rgba(255, 0, 0, 0.2);
  }
  50% {
    box-shadow: 0 2px 15px rgba(255, 0, 0, 0.4);
  }
}

.extract-text {
  font-size: 1.1rem;
  font-weight: 600;
  color: #4facfe;
  display: inline-block;
}

.contact-text {
  font-size: 1rem;
  color: #4a5568;
  margin-bottom: 10px;
  line-height: 1.5;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
}

.wechat-id {
  font-size: 1.15rem;
  font-weight: 700;
  color: #667eea;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
  padding: 6px 12px;
  border-radius: 6px;
  display: inline-block;
}

.free-highlight {
  font-size: 1.2rem;
  font-weight: 700;
  color: #ff6b6b;
  margin: 12px 0 0 0;
  letter-spacing: 1px;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.8;
    transform: scale(1.05);
  }
}

.copy-btn-inline {
  padding: 4px 10px;
  font-size: 0.85rem;
  font-weight: 600;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  vertical-align: middle;
}

.copy-btn-inline:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.get-resource-btn {
  padding: 12px 32px;
  font-size: 1.1rem;
  font-weight: 600;
  border-radius: 8px;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.get-resource-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
}



/* 空状态 */
.empty-state {
  grid-column: 1 / -1;
  padding: 60px 20px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

/* 响应式设计 - 移动端优化 */
@media (max-width: 768px) {
  .product-detail-modern {
    padding: 12px;
    background: #f5f7fa;
  }

  /* 顶部导航栏 */
  .action-buttons-wrapper {
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
    gap: 10px;
    margin-bottom: 15px;
    padding: 0 4px;
  }

  .back-button {
    flex: 0 0 auto;
    padding: 8px 16px;
    font-size: 14px;
    border-radius: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }

  .wx-link-button {
    flex: 1;
    padding: 10px 14px;
    font-size: 13px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border-radius: 20px;
    border: none;
    box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
  }

  /* 产品信息卡片 */
  .product-banner {
    padding: 20px 16px;
    border-radius: 16px;
    margin-bottom: 15px;
    background: white;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  }

  .product-main-title {
    font-size: 1.4rem;
    line-height: 1.5;
    margin-bottom: 12px;
    color: #1a1a1a;
  }

  .product-tags {
    gap: 8px;
  }

  .product-tags :deep(.el-tag) {
    padding: 6px 12px;
    font-size: 0.8rem;
    background: #f0f2f5;
    border: none;
    color: #666;
  }

  /* 媒体展示区域 */
  .media-display-section {
    border-radius: 16px;
    margin-bottom: 15px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  }

  .media-viewer {
    min-height: 200px;
    padding: 12px;
    background: #fafafa;
  }

  .media-content-modern {
    max-width: 100%;
    max-height: 280px;
    border-radius: 12px;
  }

  /* 信息操作区域 */
  .info-action-section {
    border-radius: 16px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  }

  .info-action-container {
    padding: 20px 16px;
  }

  .section-title {
    font-size: 1.1rem;
    margin-bottom: 15px;
    padding-bottom: 10px;
    color: #1a1a1a;
  }

  /* 描述区域 */
  .description-section {
    margin-bottom: 20px;
  }

  .description-content {
    padding: 16px;
    font-size: 0.95rem;
    line-height: 1.7;
    background: #f8f9fa;
    border-left: none;
    border-radius: 12px;
    color: #444;
  }

  /* 网盘地址区域 */
  .download-links-section {
    margin-bottom: 20px;
  }

  .download-links-content {
    padding: 0;
    background: transparent;
    border-left: none;
    box-shadow: none;
  }

  /* 夸克说明卡片 */
  .quark-instructions {
    padding: 20px 16px;
    border-radius: 16px;
    border-left: none;
    background: linear-gradient(135deg, #fff8f0 0%, #fff5eb 100%);
    box-shadow: 0 2px 12px rgba(255, 107, 107, 0.08);
  }

  .instruction-header {
    margin-bottom: 12px;
    padding-bottom: 12px;
    border-bottom: 1px dashed rgba(255, 107, 107, 0.2);
  }

  .header-icon {
    font-size: 1.8rem;
  }

  .header-text {
    font-size: 1.1rem;
    color: #333;
  }

  .instruction-tips {
    padding: 12px;
    margin-bottom: 15px;
    background: rgba(255, 255, 255, 0.8);
    border-radius: 10px;
  }

  .tip-item {
    padding: 6px 0;
    font-size: 0.9rem;
    line-height: 1.5;
  }

  .instruction-step {
    padding: 10px 0;
  }

  .button-group {
    flex-direction: column;
    gap: 12px;
  }

  .get-resource-btn {
    width: 100%;
    padding: 14px;
    font-size: 1rem;
    border-radius: 12px;
    background: linear-gradient(135deg, #ff6b6b 0%, #ee5a5a 100%);
    border: none;
    box-shadow: 0 4px 15px rgba(255, 107, 107, 0.3);
  }

  .download-quark-btn {
    width: 100%;
    padding: 14px;
    font-size: 1rem;
    border-radius: 12px;
    background: linear-gradient(135deg, #4caf50 0%, #43a047 100%);
    border: none;
    box-shadow: 0 4px 15px rgba(76, 175, 80, 0.3);
  }

  .instruction-footer {
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px dashed rgba(255, 107, 107, 0.2);
  }

  .footer-text {
    font-size: 0.85rem;
    color: #888;
    line-height: 1.5;
  }

  /* 下载链接 */
  .download-link-item {
    padding: 14px;
    margin-bottom: 10px;
    border-radius: 12px;
    border-left: 3px solid #667eea;
    background: white;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  }

  .link-name {
    font-size: 0.9rem;
    margin-bottom: 4px;
  }

  .link-url {
    font-size: 0.85rem;
  }

  /* 联系信息 */
  .contact-info {
    padding: 15px;
  }

  .contact-text {
    font-size: 0.9rem;
  }

  .wechat-id {
    font-size: 1rem;
    padding: 6px 12px;
  }

  .free-highlight {
    font-size: 1.1rem;
    margin-top: 10px;
  }

  .copy-btn-inline {
    padding: 6px 12px;
    font-size: 0.8rem;
  }
}

/* 免责声明区域 */
.disclaimer-section {
  margin-top: 20px;
  margin-left: 20px;
  margin-right: 20px;
  padding: 20px;
  background: linear-gradient(135deg, #fff5f5 0%, #fff0f0 100%);
  border-radius: 12px;
  border-left: 4px solid #ff6b6b;
  box-shadow: 0 2px 12px rgba(255, 107, 107, 0.1);
}

.disclaimer-content {
  max-width: 100%;
  margin: 0;
}

.disclaimer-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1.1rem;
  font-weight: 600;
  color: #c53030;
  margin-bottom: 15px;
}

.disclaimer-title .el-icon {
  font-size: 1.3rem;
}

.disclaimer-text p {
  font-size: 0.9rem;
  color: #744210;
  line-height: 1.8;
  margin: 8px 0;
  text-indent: 2em;
}

.disclaimer-text p:first-child {
  margin-top: 0;
}

.disclaimer-text p:last-child {
  margin-bottom: 0;
}

/* 免责声明区域移动端优化 */
@media (max-width: 768px) {
  .disclaimer-section {
    margin: 12px 10px;
    padding: 12px;
    border-radius: 8px;
    border-left-width: 3px;
  }

  .disclaimer-title {
    font-size: 0.95rem;
    margin-bottom: 10px;
  }

  .disclaimer-title .el-icon {
    font-size: 1.1rem;
  }

  .disclaimer-text p {
    font-size: 0.8rem;
    line-height: 1.6;
    margin: 5px 0;
    text-indent: 1.5em;
  }
}
</style>

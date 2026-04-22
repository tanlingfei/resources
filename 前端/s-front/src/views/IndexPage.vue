<template>
  <div class="index-embedded-container">
    <!-- 固定的顶部搜索区域 -->
    <div class="fixed-search-container">
      <!-- 品牌区域 -->
      <div class="brand-header-section">
        <div class="brand-content-wrapper">
          <div class="brand-text-wrapper">
            <h1 class="main-brand-title">糖糖资源站</h1>
            <p class="brand-subtitle">技术改变创作未来</p>
          </div>
        </div>
      </div>

      <!-- 顶部功能区 -->
      <div class="top-functions-area">
        <!-- 搜索区域 -->
        <div class="search-function-area">
          <el-input
            v-model="searchName"
            placeholder="搜索您需要的资源..."
            class="search-input-field"
            clearable
          >
            <template #append>
              <el-button
                @click="handleSearch"
                type="primary"
                class="search-button"
              >
                <el-icon><Search /></el-icon> 搜索
              </el-button>
            </template>
          </el-input>
          <el-button
            @click="handleReset"
            class="reset-search-btn"
            plain
          >
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
          <!-- 搜索状态提示 -->
          <div v-if="showSearchLoading" class="search-status-hint">
            资源拼命检索中...
          </div>
        </div>
      </div>
    </div>

    <!-- 主体内容 -->
    <main class="main-content-area">
      <!-- 热搜区域 -->
      <div class="hot-search-area">
        <div class="hot-search-title">热搜：</div>
        <div class="hot-search-tags">
          <el-tag
            v-for="category in apiCategories"
            :key="category.id"
            class="hot-search-tag"
            @click="handleHotSearch(category.seconds)"
          >
            {{ category.seconds }}
          </el-tag>
        </div>
      </div>

      <!-- 产品展示区 -->
      <section class="products-display-section">
        <!-- 标题和统计 -->
        <div class="section-header-area">
          <div class="total-items-count">共 {{ total }} 条记录</div>
        </div>

        <!-- 移动端产品网格 -->
        <div v-if="isMobile" class="mobile-products-grid">
          <div
            v-for="news in newsList"
            :key="news.id"
            class="mobile-product-card"
            @click="goToNews(news.id)"
          >
            <div v-if="news.img && (isImage(news.img) || isVideo(news.img))" class="mobile-card-image">
              <img
                :src="getImageUrl(news.img)"
                :alt="news.name"
                loading="lazy"
              >
            </div>
            <div class="mobile-card-content">
              <h3 class="mobile-product-title">{{ formatProductName(news.name) }}</h3>
              <p class="mobile-product-desc">{{ news.details }}</p>
              <div class="mobile-card-meta">
                <div class="mobile-user-info">
                  <el-icon><User /></el-icon>
                  <span>admin</span>
                </div>
                <div class="mobile-date-info">{{ formatDate(news.createTime) }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- PC端表格布局 -->
        <div v-else class="products-table-layout">
          <el-table
            :data="newsList"
            stripe
            style="width: 100%"
            row-class-name="table-row-hover"
            @row-click="handleRowClick"
          >
            <el-table-column prop="name" label="名称" min-width="400">
              <template #default="scope">
                <div class="table-title-cell flex items-center">
                  <!-- 预览图 -->
                  <div v-if="scope.row.img && (isImage(scope.row.img) || isVideo(scope.row.img))" class="table-image-container">
                    <img
                      :src="getImageUrl(scope.row.img)"
                      :alt="scope.row.name"
                      class="table-product-image"
                      loading="lazy"
                    >
                  </div>
                  <!-- 标题和描述 -->
                  <div class="table-content-container">
                    <h3 class="table-product-title" :title="scope.row.name">{{ formatProductName(scope.row.name) }}</h3>
                    <p class="table-product-desc">{{ scope.row.details }}</p>
                  </div>
                </div>
              </template>
            </el-table-column>

            <el-table-column prop="createTime" label="上传时间" width="120">
              <template #default="scope">
                <div class="table-date-cell">
                  {{ formatDate(scope.row.createTime) }}
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 加载更多（PC 和移动端统一使用游标分页） -->
        <div class="mobile-load-more">
          <div v-if="hasMoreData && !isLoadingMore" class="load-more-trigger" @click="loadMoreData">
            <el-button type="primary" link>
              点击加载更多
            </el-button>
          </div>
          <div v-else-if="isLoadingMore" class="loading-more">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>正在加载...</span>
          </div>
          <div v-else class="no-more-data">
            <span>没有更多数据了</span>
          </div>
        </div>
      </section>
    </main>

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
        <div class="wx-link-section">
          <el-button
            type="primary"
            link
            @click="goToWx"
            class="wx-link-button"
          >
            <el-icon><ChatDotRound /></el-icon> 关注公众号防失联
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { get } from '@/utils/request'
import { Search, User, Refresh, Loading, InfoFilled, ChatDotRound } from '@element-plus/icons-vue'
const router = useRouter()

// 跳转到公众号页面
const goToWx = () => {
  router.push('/wx')
}

// 设置页面标题
onMounted(() => {
  document.title = '糖糖资源站 - 网课考研学习资料电影电视剧资源下载'
})


// 搜索加载状态
const showSearchLoading = ref(false)

// 素材数据
const newsList = ref([])
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const searchName = ref('')
const apiCategories = ref([])
const ctxPath = ref('')

// 游标分页参数
const cursorCreateTime = ref(null)
const cursorId = ref(null)

// 移动端检测和分页相关
const isMobile = ref(false)
const isLoadingMore = ref(false)
const hasMoreData = ref(true)

// 获取图片 URL，如果为空则返回空字符串
const getImageUrl = (img) => {
  if (!img || img === '') {
    return ''
  }
  return ctxPath.value + img
}

// 移动端检测函数
const checkIsMobile = () => {
  const userAgent = navigator.userAgent
  const mobileRegex = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i
  isMobile.value = mobileRegex.test(userAgent) || window.innerWidth <= 768
}

// 窗口大小变化处理
const handleResize = () => {
  checkIsMobile()
}

// 获取素材数据
const fetchNews = (isLoadMore = false) => {
  // 如果正在加载则不执行
  if (isLoadingMore.value) return

  // 如果是普通搜索（不是加载更多），重置hasMoreData状态
  if (!isLoadMore) {
    hasMoreData.value = true
  }

  // 如果没有更多数据且是加载更多操作，则不执行
  if (!hasMoreData.value && isLoadMore) return

  if (isLoadMore) {
    isLoadingMore.value = true
  } else {
    // 设置搜索状态
    showSearchLoading.value = true
  }

  const params = {
    limit: pageSize.value
  }

  // 如果有搜索关键词，则添加到参数中
  if (searchName.value) {
    params.name = searchName.value
  }

  // 如果是普通搜索（非加载更多），重置游标
  if (!isLoadMore) {
    cursorCreateTime.value = null
    cursorId.value = null
  } else {
    // 加载更多时带上游标
    if (cursorCreateTime.value) {
      params.cursorCreateTime = cursorCreateTime.value
      params.cursorId = cursorId.value
    }
  }

  get('/business/commodityCommodityinfos/lastListCursor', params,
    (res) => {
      const newNews = res.data.records || []
      ctxPath.value = res.data.ctxPath

      if (isLoadMore) {
        newsList.value = [...newsList.value, ...newNews]
      } else {
        newsList.value = newNews
        get('/business/commodityCommodityinfos/lastListCount', params,
            (res) => {
              total.value = res.data.total || 0
            },
            () => {
              total.value = 0
            })
      }

      // 更新游标
      cursorCreateTime.value = res.data.nextCursorCreateTime || null
      cursorId.value = res.data.nextCursorId || null

      // 是否还有更多：有下一游标且本次返回数量达到页大小
      hasMoreData.value = !!cursorCreateTime.value && newNews.length >= pageSize.value

      if (isLoadMore) {
        isLoadingMore.value = false
      } else {
        showSearchLoading.value = false
      }
    },
    (err) => {
      console.error('获取素材数据失败:', err)
      if (isLoadMore) {
        isLoadingMore.value = false
      } else {
        showSearchLoading.value = false
      }
    })
}

// 获取分类数据
const fetchCategories = () => {
  get('/business/commodityTypes/findAll', null,
      (res) => {
        apiCategories.value = res.data
      },
      (err) => {
        console.error('获取分类数据失败:', err)
      })
}

// 跳转到素材详情
const goToNews = (newsId) => {
  // 保存搜索条件到 sessionStorage
  if (searchName.value) {
    sessionStorage.setItem('indexPageSearchName', searchName.value)
  } else {
    sessionStorage.removeItem('indexPageSearchName')
  }
  router.push(`/product/${newsId}`)
}

// 热搜点击处理
const handleHotSearch = (keyword) => {
  searchName.value = keyword
  currentPage.value = 1
  fetchNews()
}

// 处理滚动事件（移动端）
const handleScroll = () => {
  if (!isMobile.value || isLoadingMore.value || !hasMoreData.value) return

  const scrollTop = window.pageYOffset || document.documentElement.scrollTop
  const windowHeight = window.innerHeight
  const documentHeight = Math.max(
    document.body.scrollHeight,
    document.body.offsetHeight,
    document.documentElement.clientHeight,
    document.documentElement.scrollHeight,
    document.documentElement.offsetHeight
  )

  // 当滚动到底部附近时加载更多
  if (scrollTop + windowHeight >= documentHeight - 100) {
    loadMoreData()
  }
}

// 加载更多数据
const loadMoreData = () => {
  if (hasMoreData.value && !isLoadingMore.value) {
    currentPage.value++
    fetchNews(true)
  }
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  fetchNews()
}

// 重置搜索
const handleReset = () => {
  searchName.value = ''
  currentPage.value = 1
  fetchNews()
}

// 表格行点击处理
const handleRowClick = (row) => {
  goToNews(row.id)
}

// 格式化产品名称，限制长度
const formatProductName = (name) => {
  if (!name || typeof name !== 'string') return ''
  if (name.length <= 60) return name
  return name.substring(0, 60) + '...'
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 判断是否为图片格式
const isImage = (url) => {
  if (!url) return false
  const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp']
  return imageExtensions.some(ext => url.toLowerCase().includes(ext))
}

// 判断是否为视频格式
const isVideo = (url) => {
  if (!url) return false
  const videoExtensions = ['.mp4', '.avi', '.mov', '.wmv', '.flv', '.webm']
  return videoExtensions.some(ext => url.toLowerCase().includes(ext))
}

// PC 端不再通过页码触发自动请求，改为“加载更多”样式，因此移除分页参数监听

onMounted(() => {
  // 初始化移动端检测
  checkIsMobile()

  // 恢复搜索条件
  const savedSearchName = sessionStorage.getItem('indexPageSearchName')
  if (savedSearchName) {
    searchName.value = savedSearchName
    // 清除保存的条件，避免一直保留
    sessionStorage.removeItem('indexPageSearchName')
  }

  fetchNews()
  fetchCategories()

  // 添加滚动事件监听器（仅移动端）
  if (isMobile.value) {
    window.addEventListener('scroll', handleScroll)
  }

  // 添加窗口大小变化监听
  window.addEventListener('resize', handleResize)
})
</script>

<style scoped>
/* 移动端产品卡片布局 */
.mobile-products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  padding: 10px 0;
}

.mobile-product-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
  border: 1px solid #eee;
}

.mobile-product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(119, 119, 119, 0.2);
  border-color: #777;
}

.mobile-card-image {
  height: 180px;
  overflow: hidden;
  background: #f5f5f5;
}

/* 当没有图片时，调整卡片内容的顶部边距 */
.mobile-product-card:not(:has(.mobile-card-image)) .mobile-card-content {
  padding-top: 20px;
}

.mobile-card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  transition: transform 0.3s ease;
}

.mobile-product-card:hover .mobile-card-image img {
  transform: scale(1.05);
}

.mobile-card-content {
  padding: 20px;
}

.mobile-product-title {
  font-size: 1.2rem;
  font-weight: 600;
  margin: 0 0 10px 0;
  color: #333;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.mobile-product-desc {
  font-size: 0.95rem;
  color: #666;
  line-height: 1.5;
  margin: 0 0 15px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.mobile-card-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid #eee;
}

.mobile-user-info {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 0.9rem;
  color: #777;
}

.mobile-user-info .el-icon {
  font-size: 1.1rem;
}

.mobile-date-info {
  font-size: 0.85rem;
  color: #999;
}

/* 移动端响应式优化 */
@media (max-width: 768px) {
  .mobile-products-grid {
    grid-template-columns: 1fr;
    gap: 15px;
    padding: 0;
  }

  .mobile-product-card {
    border-radius: 12px;
    margin: 0 10px;
  }

  .mobile-card-image {
    height: 160px;
  }

  .mobile-card-content {
    padding: 15px;
  }

  .mobile-product-title {
    font-size: 1.1rem;
  }

  .mobile-product-desc {
    font-size: 0.9rem;
  }
}

@media (max-width: 480px) {
  .mobile-products-grid {
    gap: 12px;
  }

  .mobile-product-card {
    margin: 0 8px;
  }

  .mobile-card-image {
    height: 140px;
  }

  .mobile-card-content {
    padding: 12px;
  }

  .mobile-product-title {
    font-size: 1rem;
    margin-bottom: 8px;
  }

  .mobile-product-desc {
    font-size: 0.85rem;
    margin-bottom: 12px;
  }

  .mobile-card-meta {
    padding-top: 12px;
  }

  .mobile-user-info {
    font-size: 0.85rem;
  }

  .mobile-date-info {
    font-size: 0.8rem;
  }
}

/* 移动端加载更多区域优化 */
@media (max-width: 768px) {
  .mobile-load-more {
    padding: 20px 0;
    margin-top: 15px;
  }

  .load-more-trigger {
    padding: 12px;
    margin: 0 10px;
  }

  .loading-more {
    padding: 15px;
    font-size: 1rem;
  }

  .no-more-data {
    padding: 15px;
    font-size: 0.9rem;
  }
}

/* 嵌入式容器基础样式 */
.index-embedded-container {
  padding: 0;
  background: transparent;
  min-height: 100vh;
}

/* 固定搜索容器 */
.fixed-search-container {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #f5f5f5;
  padding: 8px 20px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

/* 品牌区域样式 */
.brand-header-section {
  padding: 2px 0;
  margin-bottom: 12px;
}

.brand-content-wrapper {
  padding: 6px 15px;
  background: linear-gradient(135deg, #555 0%, #888 100%);
  border-radius: 10px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}

.brand-text-wrapper {
  text-align: center;
}

.main-brand-title {
  font-size: 1.4rem;
  color: white;
  margin: 0 0 2px 0;
  font-weight: 600;
  letter-spacing: 1px;
}

.brand-subtitle {
  font-size: 0.75rem;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
  font-weight: 300;
}

/* 主体内容区域 */
.main-content-area {
  padding: 20px;
  margin: 0;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 0;
  box-shadow: none;
}

/* 顶部功能区 */
.top-functions-area {
  margin-bottom: 10px;
}

.search-function-area {
  display: flex;
  gap: 12px;
  margin-bottom: 10px;
  align-items: center;
  padding: 5px 0;
}

.search-input-field {
  flex: 1;
  max-width: 500px;
}

.search-input-field :deep(.el-input__wrapper) {
  border-radius: 25px 0 0 25px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #e4e7ed;
  transition: all 0.3s ease;
  border-right: none;
}

.search-input-field :deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 16px rgba(74, 144, 226, 0.15);
  border-color: #4a90e2;
  border-right: none;
}

.search-input-field :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 4px 16px rgba(74, 144, 226, 0.2);
  border-color: #4a90e2;
  border-right: none;
}

.search-input-field :deep(.el-input__inner) {
  font-size: 1rem;
  padding: 12px 20px;
  border-radius: 25px 0 0 25px;
}

.search-input-field :deep(.el-input-group__append) {
  border-radius: 0 25px 25px 0;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a52 100%);
  border: none;
  box-shadow: 0 2px 8px rgba(255, 107, 107, 0.3);
  transition: all 0.3s ease;
  overflow: hidden;
  padding: 0;
}

.search-input-field :deep(.el-input-group__append:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(255, 107, 107, 0.4);
  background: linear-gradient(135deg, #ff5252 0%, #e53935 100%);
}

.search-input-field :deep(.el-button) {
  border-radius: 0 25px 25px 0 !important;
  height: 44px !important;
  border: none !important;
  background: transparent !important;
  color: white !important;
  font-weight: 500;
  padding: 0 25px !important;
  transition: all 0.3s ease;
}

.search-input-field :deep(.el-button:hover) {
  background: transparent !important;
  color: white !important;
  transform: translateY(-1px);
}

.search-input-field :deep(.el-input__suffix) {
  right: 65px;
}

.search-input-field :deep(.el-input__clear) {
  font-size: 16px;
  color: #c0c4cc;
  transition: color 0.2s;
  background: transparent;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.search-input-field :deep(.el-input__clear:hover) {
  color: #909399;
  background: #f5f7fa;
}

.search-input-field :deep(.el-input-group__prepend) {
  display: none;
}

.search-button {
  border-radius: 0 25px 25px 0 !important;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a52 100%) !important;
  border: none !important;
  height: 44px !important;
  padding: 0 25px !important;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(255, 107, 107, 0.3) !important;
  color: white !important;
}

.search-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(255, 107, 107, 0.4) !important;
  background: linear-gradient(135deg, #ff5252 0%, #e53935 100%) !important;
}

.reset-search-btn {
  height: 40px;
  border-radius: 20px;
  background: linear-gradient(135deg, #4caf50 0%, #43a047 100%);
  border: none;
  color: white;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(76, 175, 80, 0.3);
}

.reset-search-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(76, 175, 80, 0.4);
  background: linear-gradient(135deg, #43a047 0%, #388e3c 100%);
}

/* 热搜样式 - 美化版本 */
.hot-search-area {
  margin-bottom: 25px;
  margin-top: 5px;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
  border: 1px solid #e9ecef;
}

.hot-search-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #495057;
  white-space: nowrap;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hot-search-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  flex: 1;
}

.hot-search-tag {
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: white;
  border: 1px solid #dee2e6;
  color: #495057;
  padding: 10px 20px;
  border-radius: 25px;
  font-size: 0.95rem;
  font-weight: 500;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
}

.hot-search-tag:hover {
  transform: translateY(-3px) scale(1.05);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.25);
  border-color: #667eea;
  color: #667eea;
  background: linear-gradient(135deg, #f8fbff 0%, #edf5ff 100%);
}

/* 产品展示区 */
.products-display-section {
  margin-top: 0;
}

.section-header-area {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  margin-bottom: 15px;
  flex-wrap: wrap;
  gap: 8px;
}

.total-items-count {
  font-size: 1.2rem;
  color: #667eea;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 表格布局样式 */
.products-table-layout {
  margin-bottom: 30px;
  position: relative;
}

/* 搜索加载覆盖层 */
.search-loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.search-loading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
  padding: 30px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
  border: 1px solid #e9ecef;
  min-width: 300px;
  max-width: 90vw;
  text-align: center;
}

.loading-icon {
  font-size: 2.5rem;
  color: #667eea;
  animation: spin 1s linear infinite;
}

.loading-text {
  font-size: 1.2rem;
  font-weight: 500;
  color: #495057;
  text-align: center;
}

.products-table-layout :deep(.el-table) {
  border-radius: 12px;
  overflow: hidden;
  opacity: 1;
  transition: opacity 0.3s ease;
}

.products-table-layout :deep(.el-table):not(.el-loading-parent--relative) {
  opacity: 1;
}

.search-loading-overlay + :deep(.el-table) {
  opacity: 0.3;
}

.products-table-layout :deep(.el-table__header th) {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  color: #495057;
  font-weight: 600;
  border-bottom: 2px solid #dee2e6;
}

.products-table-layout :deep(.el-table__row) {
  transition: all 0.2s ease;
}

.products-table-layout :deep(.el-table__row:hover) {
  background: #f8f9fa !important;
  transform: scale(1.01);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.table-title-cell {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 10px 0;
}

.table-image-container {
  flex-shrink: 0;
  width: 80px;
  height: 80px;
  overflow: hidden;
  border-radius: 8px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.table-product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.products-table-layout :deep(.el-table__row:hover) .table-image-container {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.table-content-container {
  flex: 1;
  min-width: 0;
}

.table-product-title {
  font-size: 1rem;
  font-weight: 600;
  margin: 0 0 5px 0;
  color: #333;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.table-product-desc {
  font-size: 0.85rem;
  color: #666;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .table-title-cell {
    gap: 10px;
  }

  .table-image-container {
    width: 60px;
    height: 60px;
  }
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 0.9rem;
}

.table-date-cell {
  font-size: 0.9rem;
  color: #666;
}

/* 分页 */
.pagination-control-area {
  display: flex;
  justify-content: center;
  padding: 20px 0;
  border-top: 1px solid #eee;
}

.pagination-control-area :deep(.el-pagination) {
  padding: 15px 20px;
  background: #f8fafc;
  border-radius: 12px;
}

/* 移动端滑动分页样式 */
.mobile-load-more {
  text-align: center;
  padding: 30px 0;
  margin-top: 20px;
}

.load-more-trigger {
  padding: 15px;
  border-radius: 12px;
  background: #f8f9fa;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px dashed #e9ecef;
}

.load-more-trigger:hover {
  background: #e9ecef;
  border-color: #777;
  transform: translateY(-2px);
}

.loading-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 20px;
  color: #777;
  font-size: 1.1rem;
  font-weight: 500;
}

.loading-more .el-icon {
  font-size: 1.5rem;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.no-more-data {
  padding: 20px;
  color: #909399;
  font-size: 1rem;
  font-weight: 500;
}

/* 免责声明区域 */
.disclaimer-section {
  margin-top: 20px;
  margin-left: 12px;
  margin-right: 12px;
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
  padding-left: 2em;
  text-indent: -2em;
}

.disclaimer-text p:first-child {
  margin-top: 0;
}

.disclaimer-text p:last-child {
  margin-bottom: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .index-embedded-container {
    padding: 0;
  }

  /* 固定搜索容器移动端样式 */
  .fixed-search-container {
    padding: 6px 10px;
  }

  .brand-header-section {
    padding: 1px 0;
    margin-bottom: 10px;
  }

  .brand-content-wrapper {
    padding: 5px 12px;
    border-radius: 8px;
  }

  .main-brand-title {
    font-size: 1.2rem;
    margin: 0 0 1px 0;
    font-weight: 600;
  }

  .brand-subtitle {
    font-size: 0.7rem;
    font-weight: 300;
  }

  .main-content-area {
    padding: 15px;
    margin: 0;
  }

  .search-function-area {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .search-input-field {
    max-width: none;
  }

  /* 热搜区域移动端优化 */
  .hot-search-area {
    padding: 15px;
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
    margin-bottom: 15px;
  }

  .hot-search-title {
    font-size: 1rem;
  }

  .hot-search-tags {
    gap: 8px;
  }

  .hot-search-tag {
    padding: 8px 16px;
    font-size: 0.9rem;
  }

  .section-header-area {
    justify-content: flex-start;
    margin-bottom: 12px;
    gap: 6px;
  }

  .total-items-count {
    font-size: 0.9rem;
  }

  .products-table-layout {
    display: none;
  }

  .pagination-control-area {
    display: none;
  }
}

@media (max-width: 480px) {
  .index-embedded-container {
    padding: 0;
  }

  /* 固定搜索容器小屏幕样式 */
  .fixed-search-container {
    padding: 5px 8px;
  }

  .brand-header-section {
    padding: 1px 0;
    margin-bottom: 8px;
  }

  .brand-content-wrapper {
    padding: 4px 10px;
    border-radius: 6px;
  }

  .main-brand-title {
    font-size: 1.1rem;
    margin: 0 0 1px 0;
    font-weight: 600;
  }

  .brand-subtitle {
    font-size: 0.65rem;
    font-weight: 300;
  }

  .main-content-area {
    padding: 12px;
    margin: 0;
  }

  .search-function-area {
    gap: 10px;
  }

  /* 热搜区域小屏幕进一步优化 */
  .hot-search-area {
    padding: 12px;
    gap: 10px;
  }

  .hot-search-title {
    font-size: 0.95rem;
  }

  .hot-search-tags {
    gap: 6px;
  }

  .hot-search-tag {
    padding: 6px 14px;
    font-size: 0.85rem;
  }

  .section-header-area {
    justify-content: flex-start;
    margin-bottom: 10px;
    gap: 5px;
  }

  .total-items-count {
    font-size: 0.85rem;
  }

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
    padding-left: 1.5em;
    text-indent: -1.5em;
  }
}

/* 公众号链接样式 */
.wx-link-section {
  margin-top: 20px;
  text-align: center;
}

.wx-link-button {
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.wx-link-button:hover {
  color: #667eea;
  transform: translateY(-2px);
}
</style>

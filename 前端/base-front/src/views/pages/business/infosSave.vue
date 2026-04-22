<template>
  <div class="embedded-mobile-container">
    <!-- 顶部标题栏 -->
    <div class="embedded-mobile-header">
      <div class="header-content">
        <h1 class="page-title">素材添加</h1>
        <div class="brand-slogan">数字技术改变创作未来</div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="embedded-mobile-main">
      <div class="form-card">
        <el-form
          ref="formRef"
          :model="form"
          label-width="80px"
          :rules="rules"
          class="embedded-mobile-form"
        >
          <!-- 标题输入 -->
          <el-form-item label="标题" prop="name">
            <el-input
              v-model="form.name"
              placeholder="请输入素材标题"
              clearable
              class="mobile-input"
            />
          </el-form-item>

          <!-- 所属分类 -->
          <el-form-item label="分类">
            <el-select
              v-model="form.types"
              placeholder="请选择分类"
              class="mobile-select"
            >
              <el-option
                v-for="commodityTypes in commodityTypesXb1023312List"
                :key="commodityTypes.id"
                :label="commodityTypes.seconds"
                :value="commodityTypes.id"
              />
            </el-select>
          </el-form-item>

          <!-- 上传平台 -->
          <el-form-item label="上传平台" prop="uploadType">
            <el-select
              v-model="form.uploadType"
              placeholder="请选择上传平台"
              class="mobile-select"
            >
              <el-option label="本地" value="1"></el-option>
              <el-option label="OSS" value="2"></el-option>
              <el-option label="全部" value="3"></el-option>
            </el-select>
          </el-form-item>

          <!-- 分享类型 -->
          <el-form-item label="分享类型" prop="shareType">
            <el-select
              v-model="form.shareType"
              placeholder="请选择分享类型"
              class="mobile-select"
            >
              <el-option label="网盘分享" value="1"></el-option>
              <el-option label="搜索分享" value="2"></el-option>
            </el-select>
          </el-form-item>

          <!-- 素材上传 -->
          <el-form-item label="素材" prop="img">
            <div class="upload-section">
              <el-upload
                action="#"
                ref="upload"
                :file-list="imgList"
                :show-file-list="false"
                :on-change="handleChange"
                :auto-upload="false"
                class="mobile-upload"
              >
                <el-button
                  size="large"
                  type="primary"
                  class="upload-btn"
                >
                  <i class="el-icon-upload"></i>
                  选择文件
                </el-button>
              </el-upload>

              <!-- 文件预览 -->
              <div v-if="imgList.length > 0" class="file-preview">
                <div class="file-info">
                  <i class="el-icon-document"></i>
                  <span class="file-name">{{ imgList[0].name }}</span>
                  <el-button
                    size="small"
                    type="danger"
                    @click="handleRemove"
                    class="remove-btn"
                  >
                    删除
                  </el-button>
                </div>
              </div>

              <div class="upload-tip">
                支持任何类型的文件，最大1000MB
              </div>
            </div>
          </el-form-item>

          <!-- 详细介绍 -->
          <el-form-item label="详情" prop="details">
            <el-input
              type="textarea"
              v-model="form.details"
              placeholder="请输入详细介绍"
              :rows="4"
              clearable
              class="mobile-textarea"
            />
          </el-form-item>

          <!-- 下载地址 -->
          <el-form-item label="下载地址">
            <div class="download-links-container">
              <div
                v-for="(link, index) in downloadLinks"
                :key="index"
                class="download-link-item"
              >
                <el-input
                  v-model="link.name"
                  placeholder="下载地址名称"
                  class="link-name-input"
                  clearable
                />
                <el-input
                  v-model="link.url"
                  placeholder="下载链接地址"
                  class="link-url-input"
                  clearable
                />
                <el-button
                  v-if="downloadLinks.length > 1"
                  type="danger"
                  size="small"
                  @click="removeDownloadLink(index)"
                  class="remove-link-btn"
                >
                  删除
                </el-button>
              </div>
              <el-button
                type="primary"
                size="small"
                @click="addDownloadLink"
                class="add-link-btn"
              >
                <i class="el-icon-plus"></i>
                添加下载地址
              </el-button>
            </div>
          </el-form-item>

          <!-- 是否公开 -->
          <el-form-item label="公开" prop="isPub">
            <el-select
              v-model="form.isPub"
              placeholder="请选择是否公开"
              class="mobile-select"
            >
              <el-option label="公开" value="是"></el-option>
              <el-option label="私密" value="否"></el-option>
            </el-select>
          </el-form-item>

          <!-- 是否推荐 -->
          <el-form-item label="推荐" prop="isRecom">
            <el-select
              v-model="form.isRecom"
              placeholder="请选择是否推荐"
              class="mobile-select"
            >
              <el-option label="推荐" value="是"></el-option>
              <el-option label="普通" value="否"></el-option>
            </el-select>
          </el-form-item>

          <!-- 提交按钮 -->
          <div class="submit-section">
            <el-button
              type="primary"
              size="large"
              @click="handleSubmit"
              :loading="formLoading"
              class="submit-btn"
              block
            >
              {{ formLoading ? '提交中...' : '提交素材' }}
            </el-button>

            <el-button
              size="large"
              @click="handleReset"
              class="reset-btn"
              block
            >
              重置表单
            </el-button>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from "@/apis/business/commodityCommodityinfos"
import commodityTypesApi from '@/apis/business/commodityTypes'

// 表单数据
const form = reactive({
  id: "",
  name: '',
  img: '',
  lbImg: '',
  details: '',
  types: '',
  uploadType: '3', // 默认全部
  shareType: '1', // 默认网盘分享
  isPub: '是', // 默认公开
  isRecom: '否' // 默认不推荐
})

// 下载地址列表
const downloadLinks = ref([
  { name: '', url: '' } // 默认一条
])

// 上传相关
const imgList = ref([])
const formData = new FormData()
const formLoading = ref(false)
const formRef = ref()

// 分类数据
const commodityTypesXb1023312List = ref([])

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入标题', trigger: "blur" }
  ],
  uploadType: [
    { required: true, message: '请选择上传平台', trigger: "change" }
  ],
  shareType: [
    { required: true, message: '请选择分享类型', trigger: "change" }
  ],
  isPub: [
    { required: true, message: '请选择是否公开', trigger: "change" }
  ],
  isRecom: [
    { required: true, message: '请选择是否推荐', trigger: "change" }
  ]
}

// 文件处理函数
function handleRemove() {
  imgList.value = []
}

function handleChange(file, fileList) {
  const isLt2M = file.size / 1024 / 1024 < 1000
  if (!isLt2M) {
    imgList.value = []
    ElMessage({
      message: "上传文件大小不能超过 1000MB!",
      type: "error",
      showClose: true
    })
    return
  }
  imgList.value = [file]
  formData.set("file", file.raw)
}

// 获取分类列表
function getAllCommodityTypesXb1023312() {
  let queryObj = undefined
  commodityTypesApi.getAllCommodityTypes(queryObj)
    .then(response => {
      commodityTypesXb1023312List.value = response.data
    })
}

// 提交表单
function handleSubmit() {
  formRef.value.validate(valid => {
    if (valid) {
 /*     if (imgList.value.length === 0) {
        ElMessage({
          message: "请先选择要上传的文件",
          type: "warning",
          showClose: true
        })
        return
      }*/

      formLoading.value = true

      // 构建表单数据
      formData.set("types", form.types)
      formData.set("name", form.name)
      formData.set("details", form.details)
      formData.set("uploadType", form.uploadType)
      formData.set("shareType", form.shareType)
      formData.set("isPub", form.isPub)
      formData.set("isRecom", form.isRecom)

      // 添加下载地址数据（过滤掉名称为空的项）
      const validLinks = downloadLinks.value.filter(link => link.name && link.url)
      if (validLinks.length > 0) {
        formData.set("downloadLinks", JSON.stringify(validLinks))
      }

      if (imgList.value.length > 0) {
        formData.set("img", "has")
      } else {
        formData.delete("file")
        formData.set("img", "")
      }

      // 提交数据
      api.save(formData)
        .then(response => {
          if (response.code == '200') {
            ElMessage({
              message: "素材添加成功！",
              type: "success",
              showClose: true,
            })
            handleReset()
          }
        })
        .catch(error => {
          ElMessage({
            message: "提交失败，请重试",
            type: "error",
            showClose: true,
          })
        })
        .finally(() => {
          formLoading.value = false
        })
    }
  })
}

// 添加下载地址
function addDownloadLink() {
  downloadLinks.value.push({ name: '', url: '' })
}

// 删除下载地址
function removeDownloadLink(index) {
  if (downloadLinks.value.length > 1) {
    downloadLinks.value.splice(index, 1)
  }
}

// 重置表单
function handleReset() {
  formRef.value.resetFields()
  imgList.value = []
  formData.delete("file")
  form.isPub = '是'
  form.isRecom = '否'
  form.uploadType = '3' // 重置为默认全部
  form.shareType = '1' // 重置为默认网盘分享
  // 重置下载地址为默认一条
  downloadLinks.value = [{ name: '', url: '' }]
}

// 页面加载时获取分类数据
onMounted(() => {
  getAllCommodityTypesXb1023312()
})
</script>

<style scoped>
/* 嵌入式移动端容器样式 - 适配父容器环境 */
.embedded-mobile-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
  padding: 0;
  margin: 0;
  overflow: hidden;
}

/* 顶部标题栏 - 固定在顶部 */
.embedded-mobile-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 16px 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
  border-bottom: 1px solid #eee;
}

.header-content {
  text-align: center;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  color: #333;
  margin: 0 0 6px 0;
  line-height: 1.3;
}

.brand-slogan {
  font-size: 13px;
  color: #666;
  font-weight: 500;
}

/* 主要内容区域 - 可滚动 */
.embedded-mobile-main {
  flex: 1;
  overflow-y: auto;
  padding: 16px 12px;
  /* 隐藏滚动条但保持功能 */
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.embedded-mobile-main::-webkit-scrollbar {
  display: none;
}

.form-card {
  background: white;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  margin-bottom: 16px;
}

/* 表单样式 */
.embedded-mobile-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.embedded-mobile-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #333;
  padding-right: 10px;
  font-size: 14px;
}

/* 输入框样式 */
.mobile-input :deep(.el-input__inner) {
  height: 44px;
  border-radius: 10px;
  border: 2px solid #e5e5e5;
  font-size: 15px;
  padding: 0 14px;
}

.mobile-input :deep(.el-input__inner):focus {
  border-color: #ff6b9d;
  box-shadow: 0 0 0 3px rgba(255, 107, 157, 0.1);
}

/* 选择器样式 */
.mobile-select :deep(.el-select) {
  width: 100%;
}

.mobile-select :deep(.el-input__inner) {
  height: 44px;
  border-radius: 10px;
  border: 2px solid #e5e5e5;
  font-size: 15px;
  padding: 0 14px;
}

/* 文本域样式 */
.mobile-textarea :deep(.el-textarea__inner) {
  border-radius: 10px;
  border: 2px solid #e5e5e5;
  font-size: 15px;
  padding: 12px 14px;
  min-height: 90px;
}

.mobile-textarea :deep(.el-textarea__inner):focus {
  border-color: #ff6b9d;
  box-shadow: 0 0 0 3px rgba(255, 107, 157, 0.1);
}



/* 上传区域样式 */
.upload-section {
  width: 100%;
}

.mobile-upload {
  width: 100%;
}

.upload-btn {
  width: 100%;
  height: 44px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 500;
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
  border: none;
  color: white;
}

.file-preview {
  margin-top: 12px;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 10px;
  border: 1px dashed #ddd;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-info i {
  font-size: 18px;
  color: #ff6b9d;
}

.file-name {
  flex: 1;
  font-size: 13px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.remove-btn {
  padding: 4px 10px;
  font-size: 12px;
}

.upload-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #999;
  text-align: center;
}

/* 下载地址样式 */
.download-links-container {
  width: 100%;
}

.download-link-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
}

.link-name-input {
  width: 100%;
}

.link-name-input :deep(.el-input__inner) {
  height: 44px;
  border-radius: 10px;
  border: 2px solid #e5e5e5;
  font-size: 15px;
  padding: 0 14px;
}

.link-name-input :deep(.el-input__inner):focus {
  border-color: #ff6b9d;
  box-shadow: 0 0 0 3px rgba(255, 107, 157, 0.1);
}

.link-url-input {
  width: 100%;
}

.link-url-input :deep(.el-input__inner) {
  height: 44px;
  border-radius: 10px;
  border: 2px solid #e5e5e5;
  font-size: 15px;
  padding: 0 14px;
}

.link-url-input :deep(.el-input__inner):focus {
  border-color: #ff6b9d;
  box-shadow: 0 0 0 3px rgba(255, 107, 157, 0.1);
}

.remove-link-btn {
  height: 44px;
  padding: 0 16px;
  border-radius: 10px;
  font-size: 14px;
}

.add-link-btn {
  width: 100%;
  height: 44px;
  border-radius: 10px;
  font-size: 15px;
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
  border: none;
  color: white;
  margin-top: 8px;
}

/* 提交按钮区域 */
.submit-section {
  margin-top: 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-bottom: 16px;
}

.submit-btn {
  height: 46px;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
  border: none;
  box-shadow: 0 4px 15px rgba(255, 107, 157, 0.3);
  color: white;
}

.reset-btn {
  height: 46px;
  border-radius: 10px;
  font-size: 15px;
  border: 2px solid #e5e5e5;
  color: #666;
  background: white;
}

/* 响应式调整 - 针对嵌入环境 */
@media (max-width: 768px) {
  .embedded-mobile-header {
    padding: 14px 10px;
  }

  .page-title {
    font-size: 20px;
  }

  .embedded-mobile-main {
    padding: 14px 10px;
  }

  .form-card {
    padding: 14px;
  }

  .embedded-mobile-form :deep(.el-form-item__label) {
    font-size: 14px;
  }

  .embedded-radio-group {
    gap: 10px;
  }

  .radio-option {
    min-width: 65px;
  }

  .embedded-radio-group :deep(.el-radio__label) {
    font-size: 14px;
  }
}

/* 小屏幕优化 */
@media (max-width: 480px) {
  .embedded-mobile-header {
    padding: 12px 8px;
  }

  .page-title {
    font-size: 18px;
  }

  .brand-slogan {
    font-size: 12px;
  }

  .embedded-mobile-main {
    padding: 12px 8px;
  }

  .form-card {
    padding: 12px;
  }

  .embedded-radio-group {
    gap: 8px;
  }

  .radio-option {
    min-width: 60px;
  }

  .embedded-radio-group :deep(.el-radio__label) {
    font-size: 13px;
  }
}

/* 加载状态样式 */
:deep(.el-loading-spinner) {
  margin-top: -25px;
}

:deep(.el-loading-text) {
  color: #ff6b9d;
  font-size: 14px;
}
</style>

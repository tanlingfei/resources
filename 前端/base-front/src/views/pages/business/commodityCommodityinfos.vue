<template>
  <div class="main-body">
    <!--工具栏-->
    <div class="toolbar">
      <el-form :inline="true" :model="filters">
        <el-form-item>
          <el-input v-model="filters.name" placeholder="标题 " clearable/>
        </el-form-item>

        <el-form-item>
          <el-select v-model="filters.isUpdate" placeholder="是否已更新" clearable>
            <el-option label="否" value="否"/>
            <el-option label="是" value="是"/>
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button icon="search" type="primary" :disabled="$hasBP('bnt.commodityCommodityinfos.list')  === false"
                     @click="doSearch">查询
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :disabled="$hasBP('bnt.commodityCommodityinfos.list')  === false"
                     @click="refreshForm" icon="refresh">重置
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button icon="plus" type="primary" :disabled="$hasBP('bnt.commodityCommodityinfos.add')  === false"
                     @click="handleAdd">添加
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button icon="plus" type="primary" :disabled="$hasBP('bnt.commodityCommodityinfos.add')  === false"
                     @click="handleBatchAdd">批量添加
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button
              type="success"
              icon="download"
              :loading="exportLoading"
              :disabled="$hasBP('bnt.commodityCommodityinfos.list')  === false"
              @click="handleExport"
          >导出Excel
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button
              type="danger"
              icon="delete"
              :loading="clearCacheLoading"
              @click="handleClearCache"
          >清除缓存
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
    >
    </cm-table>
    <div v-if="exportLoading" style="margin-top: 10px; max-width: 400px;">
      <el-progress
          :percentage="exportProgress"
          :stroke-width="15"
          striped
          striped-flow
          :duration="10"
      />
    </div>
  </div>
  <!-- 新增/编辑 -->
  <el-dialog
      :title="isEdit ? '编辑' : '添加'"
      :width="dialogWidth"
      draggable
      v-model="dialogVisible"
      :close-on-click-modal="false"
      @close="doClose"
      class="responsive-dialog"
  >
    <el-form ref="formRef" :model="form" label-width="80px" :rules="rules" class="responsive-form">
      <el-form-item label="标题" prop="name">
        <el-input v-model="form.name" clearable/>
      </el-form-item>
      <el-form-item label="所属分类" prop="types">
        <el-select
            v-model="form.types"
            placeholder="所属分类"
            style="width: 100%">
          <el-option
              v-for="commodityTypes in commodityTypesXb1023312List"
              :key="commodityTypes.id"
              :label="commodityTypes.seconds"
              :value="commodityTypes.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="素材上传" prop="img">
        <el-upload
            action="#"
            ref="upload"
            :file-list="imgList"
            :show-file-list="false"
            :on-change="handleChange"
            :auto-upload="false"
        >
          <el-button size="small" type="primary">选择文件</el-button>
          <span slot="tip" class="el-upload__tip">支持任何类型的文件</span>
        </el-upload>
        <div v-if="imgList.length > 0">
          已选择文件: {{ imgList[0].name }}
        </div>
      </el-form-item>
      <el-form-item v-show="imgList.length > 0">
        <el-button size="small" type="danger" @click="handleRemove">删除
        </el-button>
      </el-form-item>

      <!--轮播图-->
      <el-form-item label="预览图" prop="img" v-show="isEdit">
        <img :src="prevUrl2" style="max-width: 250px" v-show="prevUrl2!=null && prevUrl2!=''">
        <div v-show="prevUrl2==null || prevUrl2==''">暂无预览图</div>
      </el-form-item>
<!--      <el-form-item v-show="isEdit && prevUrl2!=null && prevUrl2!=''">
        <el-button size="small" type="danger" v-show="prevUrl2!=null || prevUrl2==''" @click="handleRemove2">删除
        </el-button>
      </el-form-item>-->

      <el-form-item label="详细介绍" prop="details">
        <el-input type="textarea" v-model="form.details" clearable/>
      </el-form-item>

      <!-- 是否公开 -->
      <el-form-item label="是否公开" prop="isPub">
        <el-select v-model="form.isPub" placeholder="请选择" style="width: 100%">
          <el-option label="是" value="是"/>
          <el-option label="否" value="否"/>
        </el-select>
      </el-form-item>

      <!-- 是否推荐 -->
      <el-form-item label="是否推荐" prop="isRecom">
        <el-select v-model="form.isRecom" placeholder="请选择" style="width: 100%">
          <el-option label="是" value="是"/>
          <el-option label="否" value="否"/>
        </el-select>
      </el-form-item>

      <!-- 下载链接 -->
      <el-form-item label="下载链接">
        <div v-for="(link, index) in downloadLinks" :key="index" class="link-item">
          <el-row :gutter="10">
            <el-col :span="8">
              <el-input v-model="link.name" placeholder="链接名称" size="small"></el-input>
            </el-col>
            <el-col :span="14">
              <el-input v-model="link.url" placeholder="下载链接" size="small"></el-input>
            </el-col>
            <el-col :span="2">
              <el-button type="danger" icon="delete" size="small" @click="removeDownloadLink(index)"></el-button>
            </el-col>
          </el-row>
        </div>
        <el-button type="primary" icon="plus" size="small" @click="addDownloadLink">添加下载链接</el-button>
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

  <!-- 批量添加 -->
  <el-dialog
      title="批量添加"
      :width="dialogWidth"
      draggable
      v-model="batchDialogVisible"
      :close-on-click-modal="false"
      @close="doBatchClose"
      class="responsive-dialog"
  >
    <el-form ref="batchFormRef" :model="batchForm" label-width="80px" :rules="batchRules" class="responsive-form">
      <el-form-item label="所属分类" prop="types">
        <el-select
            v-model="batchForm.types"
            placeholder="所属分类"
            style="width: 100%">
          <el-option
              v-for="commodityTypes in commodityTypesXb1023312List"
              :key="commodityTypes.id"
              :label="commodityTypes.seconds"
              :value="commodityTypes.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="素材上传" prop="imgs">
        <el-upload
            action="#"
            ref="batchUpload"
            :file-list="batchImgList"
            :show-file-list="true"
            :on-change="handleBatchChange"
            :auto-upload="false"
            multiple
        >
          <el-button size="small" type="primary">选择文件</el-button>
          <span slot="tip" class="el-upload__tip">支持任何类型的文件，可多选</span>
        </el-upload>
      </el-form-item>

      <el-form-item label="详细介绍" prop="details">
        <el-input type="textarea" v-model="batchForm.details" clearable/>
      </el-form-item>

      <!-- 是否公开 -->
      <el-form-item label="是否公开" prop="isPub">
        <el-select v-model="batchForm.isPub" placeholder="请选择" style="width: 100%">
          <el-option label="是" value="是"/>
          <el-option label="否" value="否"/>
        </el-select>
      </el-form-item>

      <!-- 是否推荐 -->
      <el-form-item label="是否推荐" prop="isRecom">
        <el-select v-model="batchForm.isRecom" placeholder="请选择" style="width: 100%">
          <el-option label="是" value="是"/>
          <el-option label="否" value="否"/>
        </el-select>
      </el-form-item>

      <!-- 进度条 -->
      <el-form-item v-show="batchUploadProgress > 0 && batchUploadProgress < 100">
        <el-progress :percentage="batchUploadProgress" :stroke-width="15" striped striped-flow :duration="10"/>
      </el-form-item>

    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="doBatchClose">取消</el-button>
        <el-button
            type="primary"
            @click="handleBatchSubmit"
            :loading="batchFormLoading"
        >提交
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import config from "@/request/config";
import api from "@/apis/business/commodityCommodityinfos";
import useTableHandlers from '@/apis/use-table-handlers'
import commodityTypesApi from '@/apis/business/commodityTypes'
import {ElMessage, ElMessageBox} from "element-plus";

const ctxPath = ref('')

// 响应式对话框宽度
const dialogWidth = ref('40%')
const isMobile = ref(false)

// 检测是否为移动端
const checkIsMobile = () => {
  const userAgent = navigator.userAgent
  const mobileRegex = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i
  isMobile.value = mobileRegex.test(userAgent) || window.innerWidth <= 768
  // 根据设备类型设置对话框宽度
  if (isMobile.value) {
    dialogWidth.value = '95%'
  } else {
    dialogWidth.value = '40%'
  }
}

const filters = reactive({
  name: '',
  isUpdate:'',
  sezes: '',
  types: '',
  img: '',
  details: '',
});

const exportLoading = ref(false)
const exportProgress = ref(0)
const clearCacheLoading = ref(false)

// 页面加载时检查 URL 参数
onMounted(() => {
  // 初始化移动端检测
  checkIsMobile()

  getAllCommodityTypesXb1023312()

  // 获取 URL 参数
  const urlParams = new URLSearchParams(window.location.hash.split('?')[1]);
  const typeId = urlParams.get('types');

  // 如果有 types 参数，则设置到过滤条件中
  if (typeId) {
    filters.types = typeId;
    // 延迟执行搜索，确保数据已初始化
    setTimeout(() => {
      doSearch();
    }, 100);
  }

  // 监听窗口大小变化
  window.addEventListener('resize', checkIsMobile)
})

const form = reactive({
  id: "",
  name: '',
  img: '',
  lbImg: '',
  details: '',
  types: '',
  isPub: '', // 添加是否公开字段
  isRecom: '否', // 添加是否推荐字段
})

// 下载链接相关
const downloadLinks = ref([
  { name: '', url: '' }
])
//上传图片begin
const imgList = ref([]);
let prevUrl = ref(null);
let formData = new FormData();
;

function handleRemove() {
  imgList.value = []
}

function handleChange(file, fileList) {
  const isLt2M = file.size / 1024 / 1024 < 1000;
  if (!isLt2M) {
    imgList.value = []
    ElMessage({message: "上传头像文件大小不能超过 1000MB!", type: "error", showClose: true});
    return;
  }
  imgList.value = [file]
  formData.set("file", file.raw)
}

//上传图片end


//轮播图片begin
const imgList2 = ref([]);
let prevUrl2 = ref(null);

function handleRemove2() {
  prevUrl2.value = null
  imgList2.value = []
}

function handleChange2(file, fileList) {
  // 获取文件后缀名
  const fileExtension = file.name.split('.').pop().toLowerCase()
  // 允许上传的文件类型
  const allowedTypes = ['jpg', 'jpeg', 'png', 'gif']
  const isJPG = allowedTypes.includes(fileExtension)
  if (!isJPG) {
    imgList2.value = []
    ElMessage({message: "上传头像图片只能是 jpg,png,jpeg,gif 格式!", type: "error", showClose: true});
    return;
  }
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isLt2M) {
    imgList2.value = []
    ElMessage({message: "上传头像图片大小不能超过 2MB!", type: "error", showClose: true});
    return;
  }
  imgList2.value = [file]
  prevUrl2.value = URL.createObjectURL(file.raw);
  formData.set("fileImg", file.raw)
}

//轮播图片end

//所属分类
let commodityTypesXb1023312List = ref([])
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

// 批量添加相关变量
const batchDialogVisible = ref(false)
const batchFormRef = ref()
const batchFormLoading = ref(false)
const batchImgList = ref([])
const batchUploadProgress = ref(0) // 添加进度相关的响应式变量
let batchFormData = new FormData();

const batchForm = reactive({
  details: '',
  types: '',
  isPub: '是', // 默认公开
  isRecom: '' // 默认不推荐
})

function handleBatchChange(file, fileList) {
  batchImgList.value = fileList
}

function doBatchClose() {
  batchDialogVisible.value = false
  batchForm.details = ''
  batchForm.types = ''
  batchForm.isPub = '是'
  batchForm.isRecom = ''
  batchImgList.value = []
  batchFormData = new FormData()
}

function handleBatchSubmit() {
  batchFormRef.value.validate(valid => {
    if (valid) {
      if (batchImgList.value.length === 0) {
        ElMessage({message: "请至少选择一个文件", type: "warning", showClose: true});
        return;
      }

      batchFormLoading.value = true
      batchUploadProgress.value = 0 // 重置进度

      // 清空并重新构建表单数据
      batchFormData = new FormData()
      batchFormData.set("types", batchForm.types)
      batchFormData.set("details", batchForm.details)
      batchFormData.set("isPub", batchForm.isPub)
      batchFormData.set("isRecom", batchForm.isRecom)

      // 添加所有文件
      batchImgList.value.forEach((file, index) => {
        batchFormData.append("files", file.raw)
      })

      // 使用自定义配置来支持上传进度监控
      api.batchSave(batchFormData, {
        onUploadProgress: function(progressEvent) {
          // 更新进度条
          if (progressEvent.lengthComputable) {
            const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total);
            batchUploadProgress.value = percentCompleted;
          }
        }
      }).then(response => {
        batchFormLoading.value = false
        batchUploadProgress.value = 0 // 重置进度
        if (response.code == '200') {
          ElMessage({
            message: "操作成功",
            type: "success",
            showClose: true,
          });
          doBatchClose()
          tableRef.value.reload();
        }
      }).catch(() => {
        batchFormLoading.value = false
        batchUploadProgress.value = 0 // 重置进度
      })
    }
  })
}

function handleBatchAdd() {
  batchDialogVisible.value = true
  batchForm.isPub = '是'
  batchForm.isRecom = ''
}

// computed
const columns = computed(() => [
  {type: 'selection'},
  {prop: "name", label: "标题", minWidth: 320, showOverflowTooltip: true},
  {prop: "typesName", label: "分类", minWidth: 120, showOverflowTooltip: true},
  {prop: "username", label: "上传人", minWidth: 120, showOverflowTooltip: true},
  {prop: "isPub", label: "是否公开", minWidth: 120, showOverflowTooltip: true},
  {prop: "isRecom", label: "是否推荐", minWidth: 120, showOverflowTooltip: true},
  {prop: "img", label: "下载链接", minWidth: 120, showOverflowTooltip: true},
  {prop: "lbImg", label: "预览图", minWidth: 120, showOverflowTooltip: true},
  {prop: "details", label: "详细介绍", minWidth: 120, showOverflowTooltip: true},
  {prop: "createTime", label: "创建时间", minWidth: 120, showOverflowTooltip: true},
  {prop: "updateTime", label: "修改时间", minWidth: 120, showOverflowTooltip: true},
]);
const operations = computed(() => [
  {
    type: 'edit',
    perm: 'bnt.commodityCommodityinfos.update'
  },
  {
    type: 'delete',
    perm: 'bnt.commodityCommodityinfos.remove'
  },
  {
    label: '下载链接',
    type: 'img'
  },
  {
    label: '预览图',
    type: 'lbImg'
  }
])
const rules = computed(() => {
  return {
    name: [{required: true, message: '请输入标题 ', trigger: "blur"}],
    isPub: [{required: true, message: '请选择是否公开', trigger: "change"}], // 添加是否公开规则
    isRecom: [{required: true, message: '请选择是否推荐', trigger: "change"}], // 添加是否推荐规则
  }
});

const batchRules = computed(() => {
  return {
    types: [{required: true, message: '请选择所属分类', trigger: "change"}],
    isPub: [{required: true, message: '请选择是否公开', trigger: "change"}],
    isRecom: [{required: true, message: '请选择是否推荐', trigger: "change"}],
  }
});

function refreshForm() {
  filters.name = ''
  filters.isUpdate = ''
  filters.img = ''
  filters.details = ''
  doSearch()
}

// 下载链接操作方法
function addDownloadLink() {
  downloadLinks.value.push({ name: '', url: '' });
}

function removeDownloadLink(index) {
  if (downloadLinks.value.length > 1) {
    downloadLinks.value.splice(index, 1);
  } else {
    // 如果只剩一个，清空内容而不是删除
    downloadLinks.value[0] = { name: '', url: '' };
  }
}

// methods
function handleDelete(ids, callback) {
  doRemove(api.remove, ids, callback)
}

function handleExport() {
  // 直接通过浏览器访问后端导出地址，由后端返回文件下载
  const params = new URLSearchParams()
  Object.keys(filters).forEach(key => {
    const value = filters[key]
    if (value !== undefined && value !== null && value !== '') {
      params.append(key, value)
    }
  })
  const query = params.toString()
  const token = localStorage.getItem("pm_token");
  const url = `${config.baseURL}/business/commodityCommodityinfos/export?token=${token}`
  // 在新窗口/标签页中触发下载，避免影响当前页面
  window.open(url, '_blank')
}

async function handleClearCache() {
  try {
    await ElMessageBox.confirm(
      '确定要清除所有Redis缓存吗？此操作不可恢复！',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    clearCacheLoading.value = true
    const res = await api.clearAllCache()
    clearCacheLoading.value = false
    
    if (res.code === 200) {
      ElMessage.success('缓存清除成功')
    } else {
      ElMessage.error(res.msg || '缓存清除失败')
    }
  } catch (e) {
    clearCacheLoading.value = false
    if (e !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

function handleAdd() {
  doAdd()
  prevUrl.value = null;
  imgList.value = []
  prevUrl2.value = null;
  imgList2.value = []
  form.isPub = '是'; // 初始化是否公开字段
  form.isRecom = '否'; // 初始化是否推荐字段
  // 重置下载链接
  downloadLinks.value = [
    { name: '', url: '' }
  ]
}

function handleEdit(row) {
  formData.delete("file")
  formData.delete("fileImg")
  api.getById(row.id).then(response => {
    const data = response.data
    if (data) {
      // 处理素材基本信息
      const commodityInfo = data.commodityInfo || data;
      for (const k in form) {
        if (k in commodityInfo) {
          form[k] = commodityInfo[k];
        }
      }
      form['isRecom'] = '否'

      // 处理下载链接
      if (data.downloadLinks && data.downloadLinks.length > 0) {
        downloadLinks.value = data.downloadLinks.map(link => ({
          id: link.id,
          name: link.name,
          url: link.url
        }));
      } else {
        downloadLinks.value = [
          { name: '', url: '' }
        ];
      }

      isEdit.value = true;
      dialogVisible.value = true;
    }

    if (form.img && form.img != '') {
      imgList.value = [{name: form.img.substring(form.img.lastIndexOf('\\') + 1), url: form.img}]
      prevUrl.value = form.img
    } else {
      imgList.value = []
      prevUrl.value = null
    }

    if (form.lbImg && form.lbImg != '') {
      imgList2.value = [{name: 'lbImg', url: form.lbImg}]
      prevUrl2.value = form.lbImg
    } else {
      imgList2.value = []
      prevUrl2.value = null
    }
  })
}

function onSubmit() {
  formRef.value.validate(valid => {
    if (valid) {
      formData.set("id", form.id)
      formData.set("types", form.types)
      formData.set("name", form.name)
      formData.set("details", form.details)
      formData.set("isPub", form.isPub) // 添加是否公开字段
      formData.set("isRecom", form.isRecom) // 添加是否推荐字段

      // 处理下载链接数据
      const validLinks = downloadLinks.value.filter(link =>
        link.name.trim() !== '' && link.url.trim() !== ''
      );
      if (validLinks.length > 0) {
        formData.set("downloadLinks", JSON.stringify(validLinks));
      }

      if (imgList != null && imgList.value.length > 0) {
        formData.set("img", "has")
      } else {
        formData.delete("file")
        formData.set("img", "")
      }
      if (imgList2 != null && imgList2.value.length > 0) {
        formData.set("lbImg", "has")
      } else {
        formData.delete("fileImg")
        formData.set("lbImg", "")
      }
      if (!isEdit.value) {
        api.save(formData).then(response => {
          if (response.code == '200') {
            ElMessage({
              message: "操作成功",
              type: "success",
              showClose: true,
            });
            dialogVisible.value = false
            tableRef.value.reload();
          }
        }).catch(() => {
        })
      } else {
        api.update(formData).then(response => {
          if (response.code == '200') {
            ElMessage({
              message: "操作成功",
              type: "success",
              showClose: true,
            });
            dialogVisible.value = false
            tableRef.value.refresh();
          }
        }).catch(() => {
        })
      }
    }
  })
}

function handleSubmit() {
  onSubmit()
  // doSubmit({save: api.save, update: api.update});
}


function getAllCommodityTypesXb1023312() {
  let queryObj = undefined
  commodityTypesApi.getAllCommodityTypes(queryObj)
      .then(response => {
        commodityTypesXb1023312List.value = response.data

      })
}


onMounted(() => {
  getAllCommodityTypesXb1023312()
});

</script>

<style scoped>
.link-item {
  margin-bottom: 10px;
  padding: 10px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background-color: #f5f7fa;
}

.link-item:last-child {
  margin-bottom: 15px;
}

.el-button {
  margin-left: 5px;
}

/* 响应式对话框样式 */
.responsive-dialog :deep(.el-dialog) {
  max-height: 90vh;
  display: flex;
  flex-direction: column;
}

.responsive-dialog :deep(.el-dialog__body) {
  flex: 1;
  overflow-y: auto;
  max-height: calc(90vh - 60px);
  padding: 20px;
}

.responsive-dialog :deep(.el-dialog__header) {
  padding: 15px 20px;
  border-bottom: 1px solid #e4e7ed;
}

.responsive-dialog :deep(.el-dialog__footer) {
  padding: 15px 20px;
  border-top: 1px solid #e4e7ed;
}

/* 响应式表单样式 */
.responsive-form .el-form-item {
  margin-bottom: 18px;
}

/* 移动端优化 */
@media (max-width: 768px) {
  .responsive-dialog :deep(.el-dialog) {
    width: 95% !important;
    max-width: 95% !important;
    margin: 10px auto;
  }

  .responsive-dialog :deep(.el-dialog__body) {
    padding: 15px;
    max-height: calc(90vh - 50px);
  }

  .responsive-dialog :deep(.el-dialog__header) {
    padding: 12px 15px;
  }

  .responsive-dialog :deep(.el-dialog__footer) {
    padding: 12px 15px;
  }

  .responsive-form :deep(.el-form-item__label) {
    font-size: 14px;
    line-height: 1.5;
  }

  .responsive-form :deep(.el-input__inner),
  .responsive-form :deep(.el-textarea__inner),
  .responsive-form :deep(.el-select) {
    font-size: 14px;
  }

  /* 下载链接在移动端改为垂直布局 */
  .link-item :deep(.el-row) {
    flex-direction: column;
    gap: 10px;
  }

  .link-item :deep(.el-col) {
    width: 100% !important;
  }

  .link-item :deep(.el-button) {
    width: 100%;
    margin-left: 0;
    margin-top: 5px;
  }
}

/* 平板设备优化 */
@media (min-width: 769px) and (max-width: 1024px) {
  .responsive-dialog :deep(.el-dialog) {
    width: 70% !important;
    max-width: 70% !important;
  }
}
</style>

<template>
  <div class="main-body">
    <!--工具栏-->
    <div class="toolbar">
      <el-form :inline="true" :model="filters">
        <el-form-item>
          <el-input v-model="filters.seconds" placeholder="分类名称" clearable/>
        </el-form-item>
        <el-form-item>
          <el-button icon="search" type="primary" :disabled="$hasBP('bnt.commodityTypes.list')  === false"
                     @click="doSearch">查询
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :disabled="$hasBP('bnt.commodityTypes.list')  === false" @click="refreshForm"
                     icon="refresh">重置
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button icon="plus" type="primary" :disabled="$hasBP('bnt.commodityTypes.add')  === false" @click="doAdd">
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
    >
    </cm-table>
  </div>
  <!-- 新增/编辑 -->
  <el-dialog
      :title="isEdit ? '编辑' : '添加'"
      width="40%"
      draggable
      v-model="dialogVisible"
      :close-on-click-modal="false"
      @close="doClose"
  >
    <el-form ref="formRef" :model="form" label-width="80px" :rules="rules">
      <el-form-item label="分类名称" prop="seconds">
        <el-input v-model="form.seconds" clearable/>
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


</template>

<script setup>
import config from "@/request/config";
import api from "@/apis/business/commodityTypes";
import useTableHandlers from '@/apis/use-table-handlers'

import { useRouter } from 'vue-router'

const router = useRouter()



const filters = reactive({
  seconds: '',
});
const form = reactive({
  id: "",
  seconds: '',
})
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
// computed
const columns = computed(() => [
  {type: 'selection'},
  {prop: "seconds", label: "分类名称", minWidth: 120, showOverflowTooltip: true,
   formatter: (row) => {
     return h('a', {
       href: 'javascript:void(0)',
       onClick: () => {
         // 跳转到商品列表页面，并传递分类ID作为查询参数
         // 改为直接路由跳转，使用路由地址 spxx
         router.push(`/spxx?types=${row.id}`);
       },
       style: {
         color: '#409eff',
         textDecoration: 'underline'
       }
     }, row.seconds);
   }},
  {prop: "createTime", label: "创建时间", minWidth: 120, showOverflowTooltip: true},
  {prop: "updateTime", label: "修改时间", minWidth: 120, showOverflowTooltip: true},
]);
const operations = computed(() => [
  {
    type: 'edit',
    perm: 'bnt.commodityTypes.update'
  },
  {
    type: 'delete',
    perm: 'bnt.commodityTypes.remove'
  }
])
const rules = computed(() => {
  return {
    seconds: [{required: true, message: '请输入分类名称', trigger: "blur"}],
  }
});


function refreshForm() {
  filters.seconds = ''
  doSearch()
}

// methods
function handleDelete(ids, callback) {
  doRemove(api.remove, ids, callback)
}

function handleEdit(row) {
  doEdit(api.getById, row.id)

}

function handleSubmit() {
  doSubmit({save: api.save, update: api.update});
}


onMounted(() => {
});

</script>

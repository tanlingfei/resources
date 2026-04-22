<template>
  <div v-loading="loading" class="cm-table">
    <!--表格栏-->
    <el-table
        :data="data.content"
        class="cm-table__tb"
        v-bind="$attrs"
        @selection-change="selectionChange"
    >

      <el-table-column v-for="column in columns" :key="column.prop" v-bind="column">
        <template #default="{ row }" >
          <a v-if="column.prop=='img' && row.img && row.img!=''" :href="data.ctxPath+row.img" target="_blank" >
            下载
<!--            <img :src="data.ctxPath+row.img" style="width: 50%;height: 50%">-->
          </a>
          <a v-if="column.prop=='lbImg' && row.lbImg && row.lbImg!=''" :href="data.ctxPath+row.lbImg" target="_blank" >
                        <img :src="data.ctxPath+row.lbImg" style="width: 50%;height: 50%">
          </a>
        </template>
      </el-table-column>

      <template v-for="(opr, i) in operations" :key="i">
        <el-table-column
            :label="opr.label"
            minWidth="120"
            v-if="opr.type==='view'"
        >
          <template #default="{ row }">
            <el-button
                type="text"
                :disabled="$hasBP(opr.perm)  === false"
                @click="opr.onClick(row)"
            >查看
            </el-button>
          </template>
        </el-table-column>

<!--        <el-table-column
            :label="opr.label"
            minWidth="120"
            v-if="opr.type==='img'"
        >
          <template #default="{ row }">
              <a :href="data.ctxPath+row.img" target="_blank" title="查看大图">
                <img :src="data.ctxPath+row.img" style="width: 50%;height: 50%">
              </a>
          </template>

        </el-table-column>-->
      </template>

      <el-table-column
          v-if="showOperation"
          fixed="right"
          label="操作"
          :width="oprWidth"
      >
        <template #default="{ row }">
          <template v-for="(opr, i) in operations" :key="i">
            <template v-if="isShow(opr.show, row)">
              <el-button
                  v-if="opr.type === 'edit'"
                  type="text"
                  v-show="$hasBP(opr.perm)  === true"
                  @click="handleEdit(row)"
              >编辑
              </el-button>
              <el-button
                  v-else-if="opr.type === 'delete'"
                  type="text"
                  class="danger"
                  v-show="$hasBP(opr.perm)  === true"
                  @click="handleDelete(row)"
              >删除
              </el-button>
              <el-button
                  v-else-if="opr.type !== 'view'"
                  type="text"
                  v-show="$hasBP(opr.perm)  === true"
                  @click="opr.onClick(row)"
              >{{ opr.label }}
              </el-button>
            </template>
          </template>
        </template>
      </el-table-column>
    </el-table>
    <!--分页栏-->
    <div class="cm-table__toolbar">
      <template v-for="(opr, i) in operations" :key="i">
        <el-button
            v-if="showBatchDelete && opr.type === 'delete'"
            type="danger"
            :disabled="selections.length === 0 || $hasBP(opr.perm)  === false"
            @click="handleBatchDelete()"
        >批量删除
        </el-button>
      </template>
      <el-pagination
          v-if="showPagination"
          class="cm-table__pagination"
          v-model:currentPage="pageRequest.pageNum"
          v-model:page-size="pageRequest.pageSize"
          :total="data.totalSize || 0"
          :page-sizes="[10, 20, 50, 100, 200]"
          layout="total, prev, pager, next, sizes, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
      ></el-pagination>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  getPage: Function, // 获取表格分页数据的方法
  filters: Object,
  showPagination: {
    type: Boolean,
    default: true
  },
  columns: Array, // 表格列配置
  showOperation: {
    // 是否显示操作组件
    type: Boolean,
    default: true,
  },
  operations: {
    type: Array,
    default: () => {
      return [
        {
          type: 'edit',
          perm: 'bnt.default.update'
        },
        {
          type: 'delete',
          perm: 'bnt.default.remove'
        }
      ]
    }
  },
  oprWidth: {
    type: Number,
    default: 185
  },
  showBatchDelete: {
    // 是否显示操作组件
    type: Boolean,
    default: true,
  },
})

const emit = defineEmits(['handleEdit', 'handleDelete']);

const loading = ref(false)
const pageRequest = reactive({
  pageNum: 1,
  pageSize: 10,
})
const data = ref({});

// 分页查询
function findPage() {
  debugger;
  if (!props.getPage) {
    return;
  }
  loading.value = true;
  /*  const req = props.getPage({ ...pageRequest, ...(props.filters || {}), sortby: props.sortby });*/
  const req = props.getPage(pageRequest.pageNum, pageRequest.pageSize, props.filters);
  if (Object(req).constructor === Promise) {
    req.then(res => {
      if (res.data) {
        if(res.data.vals){
          data.value = {
            content: res.data.vals.records,
            totalSize: res.data.vals.total,
            ctxPath:res.data.ctxPath
          }
        }else if(res.data.records){
          data.value = {
            content: res.data.records,
            totalSize: res.data.total
          }
        }else{
          data.value = {
            content: res.data
          }
        }
      }
    }).catch(() => {
      data.value = {}
    }).finally(() => {
      loading.value = false;
    });
  }

}

function reload() {
  handlePageChange(1);
}

function handleSizeChange(pageSize) {
  pageRequest.pageSize = pageSize;
  pageRequest.pageNum = 1;
  findPage();
}

// 换页刷新
function handlePageChange(pageNum) {
  pageRequest.pageNum = pageNum;
  findPage();
}

function isShow(showFn, row) {
  if (showFn && typeof showFn === 'function') {
    return showFn(row)
  }
  return true;
}

function isDisabled(disabledFn, row) {
  if (disabledFn && typeof disabledFn === 'function') {
    return disabledFn(row)
  }
  return false;
}

// 编辑
function handleEdit(row) {
  emit("handleEdit", row);
}

// 删除
function handleDelete(row) {
  onDelete([row.id]);
}

const selections = ref([]);

function selectionChange(slts) {
  selections.value = slts;
}

// 批量删除
function handleBatchDelete() {
  let ids = selections.value.map((item) => item.id);
  onDelete(ids);
}

// 删除操作
function onDelete(ids) {
  ElMessageBox.confirm('确认删除?', '删除', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: "warning",
    draggable: true,
  }).then(() => {
    const callback = () => {
      ElMessage({message: '删除成功', type: "success"});
      reload();
    };
    emit("handleDelete", ids, callback);
  }).catch(() => {
  });
}

reload();

defineExpose({
  refresh: findPage,
  reload,
})
</script>
<style lang="scss" scoped>
.cm-table__tb {
  border: 1px solid #eee;
  border-bottom: none;
  min-width: 100%;
}

.cm-table__toolbar {
  padding: 10px 5px;

  &:after {
    content: "";
    display: table;
    clear: both;
  }
}

.cm-table__pagination {
  float: right;
  padding-right: 0;
}

.danger {
  color: var(--el-color-danger) !important;
}
</style>

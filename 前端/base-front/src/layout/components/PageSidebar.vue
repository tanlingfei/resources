<template>
  <div class="page-sidebar">
    <div class="collape-bar">
      <el-icon class="cursor" @click="isCollapse = !isCollapse">
        <expand v-if="isCollapse" />
        <fold v-else />
      </el-icon>
    </div>
    <el-menu :default-active="defaultActive" router class="sidemenu" :collapse="isCollapse">
      <template v-for="(item, i) in treeData" :key="i">
        <sidebar-menu-item :item="item"  />
      </template>
    </el-menu>
  </div>
</template>

<script  setup>
import SidebarMenuItem from "./SidebarMenuItem.vue";
const route = useRoute();
const store = useStore();
const treeData = computed(() =>  store.state.menuTree);
const defaultActive = computed(() => route.path || treeData.value[0].path)
const isCollapse = ref(false)
</script>

<style lang="scss">
$side-width: 200px;
.page-sidebar {
  background-color: #f8f9fa;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  
  .sidemenu.el-menu,
  .sidemenu .el-sub-menu > .el-menu {
    --el-menu-text-color: #495057;
    --el-menu-hover-text-color: #e74c3c;
    --el-menu-hover-bg-color: #ffeaea;
    --el-menu-border-color: transparent;
    --el-menu-bg-color: #f8f9fa;
    .el-menu-item {
      &.is-active {
        background-color: #fdf2f2;
        color: #e74c3c !important;
        font-weight: 500;
      }
      
      &:hover {
        color: #e74c3c !important;
      }
    }
    
    .el-sub-menu {
      .el-sub-menu__title {
        &:hover {
          color: #e74c3c !important;
        }
      }
      
      &.is-active {
        .el-sub-menu__title {
          color: #e74c3c !important;
        }
      }
    }
  }
  
  .sidemenu.el-menu {
    border-right: none;
    box-shadow: 2px 0 8px 0 rgba(29, 35, 41, 0.05);
  }
  
  .sidemenu.el-menu:not(.el-menu--collapse) {
    width: $side-width;
  }
  .collape-bar {
    color: #495057;
    font-size: 16px;
    line-height: 36px;
    text-align: right;
    background-color: #f8f9fa;
    box-shadow: 2px 0 8px 0 rgba(29, 35, 41, 0.05);

    .c-icon {
      cursor: pointer;
      padding: 0 16px;
    }
    
    .el-icon {
      padding: 0 16px;
      transition: transform 0.3s;
      
      &:hover {
        transform: scale(1.2);
      }
    }
  }
}
</style>

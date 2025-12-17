<template>
  <div class="app-container">

    <!-- 搜索表单 -->
    <el-form ref="queryRef" :inline="true" label-width="68px">
      <el-form-item label="名称" prop="name">
        <el-input
            placeholder="请输入名称"
            clearable
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search">搜索</el-button>
        <el-button icon="Refresh">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 功能按钮栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
            type="primary"
            plain
            icon="Plus"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="success"
            plain
            icon="Edit"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="danger"
            plain
            icon="Delete"
        >删除</el-button>
      </el-col>
    </el-row>

    <!-- 数据展示表格 -->
    <el-table v-loading="loading" :data="cabinetTypeList">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="名称" prop="name" width="150"/>
      <el-table-column label="总插槽数量" prop="totalSlots" width="110"/>
      <el-table-column label="描述" prop="description" />
      <el-table-column label="状态" prop="status" width="100">
        <template #default="scope">
          {{ scope.row.status == '1' ? '正常' : '停用' }}
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit">修改</el-button>
          <el-button link type="primary" icon="Delete">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页条组件 -->
    <pagination
        v-show="total>0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
    />

  </div>
</template>

<script setup name="CabinetType">
//引入api接口
import { listCabinetType } from "@/api/device/cabinetType";

//定义分页列表数据模型
const cabinetTypeList = ref([]);
//定义列表总记录数模型
const total = ref(0);
//加载数据时显示的动效控制模型
const loading = ref(true);

//Vue 3 中的两种响应式数据绑定方式：reactive 和 ref
//ref定义：基本数据类型，适用于简单的响应式数据
//reactive定义：对象（或数组）数据类型，则适用于复杂对象或数组的响应式数据
const data = reactive({
  //定义搜索模型
  queryParams: {
    pageNum: 1,
    pageSize: 2
  }
});
//toRefs 是一个Vue3中提供的API，可将一个响应式对象转换为普通对象，其中属性变成了对原始对象属性的引用
const { queryParams } = toRefs(data);

/** 查询柜机类型列表 */
function getList() {
  loading.value = true;
  listCabinetType(queryParams.value).then(response => {
    cabinetTypeList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

//执行查询柜机类型列表
getList()
</script>

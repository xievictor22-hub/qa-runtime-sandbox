<template>
  <div class="app-container p-4 h-full flex flex-col">
    <el-card shadow="never" class="mb-1 flex-shrink-0 p-0.25" body-class="pl-0.5 pr-0.5 pt-0.5 pb-0.5">
      <!-- 标题行 -->
      <div class="flex justify-between items-center mb-3 bg-[#f5f7fa] p-2 rounded">
        <h1 class="text-2xl font-bold">底价库管理</h1>
        <div class="flex gap-2">
          <el-select 
            v-model="queryParams.version"
            placeholder="请选择版本"
            clearable
            filterable
            class="w-40"
          >
            <el-option
              v-for="item in versionOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <el-button
            type="success"
            plain
            icon="Upload"
            @click="handleOpenImport"
          >
            导入底价
          </el-button>
          <el-button
            type="success"
            plain
            icon="Upload"
            @click="handleExportBasePrice"
          >
            导出底价
          </el-button>
          <el-button type="primary" icon="Plus" plain @click="handleOpenAdd">新增条目</el-button>
        </div>
      </div>
      
      <!-- 筛选条件行 -->
      <div>
        <el-form :inline="true" :model="queryParams" class="demo-form-inline">
          <div class="grid grid-cols-8 gap-x-0 gap-y-1">
            <el-form-item label="项目类型">
              <el-select
                v-model="queryParams.projectType"
                placeholder="请选择项目类型"
                clearable
              >
                <el-option
                  v-for="item in projectTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="项目1">
              <el-input
                v-model="queryParams.process1"
                placeholder="工艺大类"
                clearable
                @keyup.enter="handleQuery"
              />
            </el-form-item>
            <el-form-item label="项目2">
              <el-input
                v-model="queryParams.process2"
                placeholder="工艺大类"
                clearable
                @keyup.enter="handleQuery"
              />
            </el-form-item>
            <el-form-item label="项目3">
              <el-input
                v-model="queryParams.process3"
                placeholder="工艺大类"
                clearable
                @keyup.enter="handleQuery"
              />
            </el-form-item>
            <el-form-item label="项目4">
              <el-input
                v-model="queryParams.process4"
                placeholder="工艺大类"
                clearable
                @keyup.enter="handleQuery"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleQuery"
                >搜索</el-button
              >
              <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
          </div>
        </el-form>
      </div>
    </el-card>

    <el-card shadow="never" class="flex-1 min-h-0 table-card flex flex-col"  body-class="flex flex-col h-full">
      <div class="flex flex-col h-full">
        <el-table
          v-loading="loading"
          :data="tableData"
          border
          stripe
          style="width: 100%;flex: 1;"
          
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column
            prop="version"
            label="版本号"
            width="100"
            align="center"
          >
            <template #default="{ row }">
              <el-tag>{{ row.version }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column
            prop="projectType"
            label="类型"
            width="80"
            align="center"
          >
            <template #default="{ row }">
              <el-tag :type="row.projectType === 1 ? 'warning' : 'success'">
                {{ row.projectType === 1 ? "工装" : "家装" }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="process1" label="项目1" show-overflow-tooltip />
          <el-table-column prop="process2" label="项目2" show-overflow-tooltip />
          <el-table-column prop="process3" label="项目3" show-overflow-tooltip />
          <el-table-column prop="process4" label="项目4" show-overflow-tooltip />
          <el-table-column prop="unit" label="单位" width="80" align="center" />
          <el-table-column
            prop="unitPrice"
            label="单价"
            align="right"
            width="100"
          >
            <template #default="{ row }">
              <span class="font-bold text-blue-600">{{ row.unitPrice }}</span>
            </template>
          </el-table-column>
          <el-table-column
            prop="quoteFormula"
            label="损耗系数"
            align="center"
            width="100"
          />
          <el-table-column
            prop="isFolding"
            label="计入折件"
            align="center"
            width="100"
          >
          </el-table-column>
          <el-table-column
            prop="createTime"
            label="创建时间"
            width="160"
            align="center"
          >
          <template #default="{ row }">
            {{ row.createTime?.substring(0, 10)  +'\n'+ row.createTime?.substring(11, 19)  }}
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="80"
          align="center"
        >
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleCopyItem(row)">
              复制
            </el-button>
          </template>
        </el-table-column>
        </el-table>

        <div class="flex justify-end mt-4 flex-shrink-0">
          <el-pagination
            v-model:current-page="queryParams.pageNum"
            v-model:page-size="queryParams.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleQuery"
            @current-change="handleQuery"
          />
        </div>
      </div>
    </el-card>

    <ImportQuoteDialog ref="importDialogRef" @success="handleQuery" />
    <AddQuoteItemDialog ref="addDialogRef" @success="handleQuery" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";

import { exportBasePrice, listBasePrice, type BasePriceQuery, type BasePriceVO, getBasePriceVersions } from "@/api/quote/base";
import { downloadBlob } from "@/utils/download";
// 假设这是你之前写的 API
// import { listBasePrice } from '@/api/quote/basePrice'

// 1. 引入组件
import ImportQuoteDialog from "./components/ImportQuoteDialog.vue";
import AddQuoteItemDialog from "./components/AddQuoteItemDialog.vue";
import { ElMessage } from "element-plus";

// 2. 定义 ref 变量，类型为组件实例
const importDialogRef = ref<InstanceType<typeof ImportQuoteDialog>>();
const addDialogRef = ref<InstanceType<typeof AddQuoteItemDialog>>();

// --- 数据查询逻辑 ---
const loading = ref(false);
const total = ref(0);
const tableData = ref<BasePriceVO[]>([]);

// 项目类型选项
const projectTypeOptions = [
  { label: '工装', value: 1 },
  { label: '家装', value: 0 }
];
// 版本选项
const versionOptions = ref<{label:string,value:string}[]>([])
const queryParams = reactive<BasePriceQuery>({
  pageNum: 1,
  pageSize: 10,
  projectType: undefined,
  process1: undefined,
  process2: undefined,
  process3: undefined,
  process4: undefined,
  unit: undefined,
  version: "A10",
});

const handleQuery = async () => {
  loading.value = true;
  try {
    // 调用 API 接口
    const res = await listBasePrice(queryParams);
      tableData.value = res.rows || [];
      total.value = res.total || 0;
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

const resetQuery = () => {
  queryParams.projectType = undefined;
  queryParams.process1 = undefined;
  queryParams.process2 = undefined;
  queryParams.process3 = undefined;
  queryParams.process4 = undefined;
  queryParams.unit = undefined;
  handleQuery();
};

// --- 3. 核心交互逻辑 ---

/**
 * 点击“导入底价”按钮触发
 */
const handleOpenImport = () => {
  if (importDialogRef.value) {
    // 调用子组件暴露的 open 方法
    importDialogRef.value.open();
  }
};
/**
 * 点击“导出底价”按钮触发
 */
const handleExportBasePrice = async () => {
  // 校验版本参数
  if (!queryParams.version) {
    ElMessage.error('请选择版本');
    return;
  }
  // 调用导出 API
  const res = await exportBasePrice(queryParams.version)
  downloadBlob(res, `底价库_${queryParams.version}.xlsx`)
}
const handleOpenAdd = () => {
  if (addDialogRef.value) {
    // 如果当前列表筛选了版本，则把该版本传给弹窗作为默认值
    const currentVersion = queryParams.version || ''
    addDialogRef.value.open({ defaultVersion: currentVersion })
  }
}

/**
 * 点击复制按钮触发
 * @param row 要复制的表格行数据
 */
const handleCopyItem = (row: BasePriceVO) => {
  if (addDialogRef.value) {
    // 打开弹窗并传递复制的数据
    addDialogRef.value.open({
      defaultVersion: queryParams.version || '',
      data: row
    })
  }
}

const initVersionOptions =  async () => {
  const versions = await getBasePriceVersions()
  // 初始化版本选项
  versionOptions.value = versions.map(v=>({label:v,value:v}))
  // 设置默认版本
  queryParams.version = versions[0]
}
// 初始化
onMounted(async () => {
  await initVersionOptions()
  handleQuery();
});
</script>


<template>
  <div class="dashboard-container">
    <el-row :gutter="20" class="mb-5">
      <el-col :span="6" v-for="item in stats" :key="item.title">
        <el-card shadow="hover" class="cursor-pointer">
          <div class="flex justify-between items-center">
            <div>
              <div class="text-gray-500 text-sm">{{ item.title }}</div>
              <div class="text-2xl font-bold mt-2">{{ item.value }}</div>
            </div>
            <el-icon :size="40" :class="item.color"><component :is="item.icon" /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never">
      <div ref="chartRef" style="height: 400px; width: 100%;"></div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { User, Goods, Money, TrendCharts } from '@element-plus/icons-vue'

// 模拟统计数据
const stats = [
  { title: '总用户数', value: '12,305', icon: User, color: 'text-blue-500' },
  { title: '总销售额', value: '¥ 88,420', icon: Money, color: 'text-green-500' },
  { title: '订单数量', value: '3,400', icon: Goods, color: 'text-purple-500' },
  { title: '今日访问', value: '1,203', icon: TrendCharts, color: 'text-orange-500' },
]

// 图表逻辑
const chartRef = ref<HTMLElement>()
let myChart: echarts.ECharts | null = null

const initChart = () => {
  if (chartRef.value) {
    myChart = echarts.init(chartRef.value)
    myChart.setOption({
      title: { text: '近七日访问趋势' },
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
      },
      yAxis: { type: 'value' },
      series: [
        {
          name: '访问量',
          type: 'line',
          smooth: true, // 平滑曲线
          data: [120, 132, 101, 134, 90, 230, 210],
          areaStyle: { opacity: 0.3 }, // 区域填充
          itemStyle: { color: '#409EFF' }
        },
        {
          name: '订单量',
          type: 'line',
          smooth: true,
          data: [220, 182, 191, 234, 290, 330, 310],
          itemStyle: { color: '#67C23A' }
        }
      ]
    })
  }
}

// 监听窗口大小变化
const handleResize = () => {
  myChart?.resize()
}

onMounted(() => {
  initChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  myChart?.dispose()
})
</script>
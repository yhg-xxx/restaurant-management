
<template>
  <div class="sales-report">
    <!-- 时段销售热力图 -->
    <div class="chart-container">
      <h2>时段销售热力图</h2>
      <div ref="heatmapChart" style="height: 500px;"></div>
    </div>

    <!-- 菜品销售排行榜 -->
    <div class="chart-container">
      <h2>菜品销售排行榜</h2>
      <div ref="dishChart" style="height: 400px;"></div>
    </div>

    <!-- 分类销售额占比 -->
    <div class="chart-container">
      <h2>分类销售额占比</h2>
      <div ref="categoryChart" style="height: 400px;"></div>
    </div>
  </div>
</template>



<script setup>
import { ref, onMounted } from 'vue';
import * as echarts from 'echarts';

// 响应式数据
const heatmapChart = ref(null);
const dishChart = ref(null);
const categoryChart = ref(null);

// 初始化图表
onMounted(async () => {
  // 获取热力图数据
  const heatmapData = await fetchHeatmapData();
  initHeatmap(heatmapData);

  // 获取菜品排行数据
  const dishData = await fetchDishData();
  initDishChart(dishData);

  // 获取分类数据
  const categoryData = await fetchCategoryData();
  initCategoryChart(categoryData);
});

// API请求方法
const fetchHeatmapData = async () => {
  const response = await fetch('http://localhost:8080/api/sales/heatmap');
  return await response.json();
};

const fetchDishData = async () => {
  const response = await fetch('http://localhost:8080/api/sales/dishes');
  return await response.json();
};

const fetchCategoryData = async () => {
  const response = await fetch('http://localhost:8080/api/sales/categories');
  return await response.json();
};

// 初始化热力图
const initHeatmap = (data) => {
  const chart = echarts.init(heatmapChart.value);

  // 将英文星期映射为中文
  const dayMap = {
    'Monday': '星期一',
    'Tuesday': '星期二',
    'Wednesday': '星期三',
    'Thursday': '星期四',
    'Friday': '星期五',
    'Saturday': '星期六',
    'Sunday': '星期日'
  };

  // 创建中文星期数组
  const chineseDays = data.days.map(day => dayMap[day] || day);

  // UTC转UTC+8：将每个数据点的小时加8（模24）
  const convertedData = data.data.map(item => {
    const originalHour = item[0];
    const beijingHour = (originalHour + 8) % 24; // 转换为北京时间
    return [beijingHour, item[1], item[2]]; // 保持星期和销售额不变
  });

  // 创建北京时间的小时标签 (0-23)
  const beijingHours = Array.from({ length: 24 }, (_, i) => {
    return i < 10 ? `0${i}` : `${i}`; // 格式化为两位数
  });

  const option = {
    tooltip: {
      position: 'top',
      formatter: (params) => {
        // 直接使用转换后的小时
        const hour = params.value[0];
        const hourStr = hour < 10 ? `0${hour}` : hour; // 格式化小时
        return `${chineseDays[params.value[1]]}<br>
                时间: ${hourStr}:00<br>
                销售额: ${params.value[2].toFixed(2)}`;
      }
    },
    grid: { height: '80%', top: '10%' },
    xAxis: {
      type: 'category',
      data: beijingHours, // 使用北京时间标签
      axisLabel: {
        formatter: (value) => value + ':00' // 添加时间后缀
      },
      splitArea: { show: true }
    },
    yAxis: {
      type: 'category',
      data: chineseDays,
      splitArea: { show: true }
    },
    visualMap: {
      min: 0,
      max: Math.max(...data.data.map(item => item[2])),
      calculable: true,
      orient: 'horizontal',
      left: 'center',
      bottom: 0
    },
    series: [{
      name: '销售额',
      type: 'heatmap',
      data: convertedData, // 使用转换后的数据
      emphasis: { itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.5)' } }
    }]
  };
  chart.setOption(option);
};

// 初始化菜品排行榜
const initDishChart = (data) => {
  // 保持后端返回的原始排序（高到低）
  const chart = echarts.init(dishChart.value);
  const option = {
    title: { text: '菜品销售额TOP10', left: 'center' },
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'value' },
    yAxis: {
      type: 'category',
      inverse: true, // 添加此属性反转Y轴
      data: data.map(item => item.dishName),
      axisLabel: { interval: 0, rotate: 30 }
    },
    series: [{
      name: '销售额',
      type: 'bar',
      data: data.map(item => item.totalSales),
      label: { show: true, position: 'right' }
    }]
  };
  chart.setOption(option);
};

// 初始化分类饼图
const initCategoryChart = (data) => {
  const chart = echarts.init(categoryChart.value);
  const option = {
    title: { text: '分类销售额占比', left: 'center' },
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { orient: 'vertical', right: 10, top: 'center' },
    series: [{
      name: '销售额',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: '18', fontWeight: 'bold' }
      },
      data: data.map(item => ({
        name: item.category,
        value: item.sales
      }))
    }]
  };
  chart.setOption(option);
};
</script>



<style scoped>
.sales-report {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}
.chart-container {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
  padding: 20px;
  margin-bottom: 30px;
}
h2 {
  margin-top: 0;
  color: #333;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}
</style>
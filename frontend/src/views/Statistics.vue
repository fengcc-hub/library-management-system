<template>
  <div class="app-container">
    <!-- Overview Cards -->
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">总借阅量</div>
          <div class="stat-value" style="color:#409eff">{{ overview.totalBorrows || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">在借中</div>
          <div class="stat-value" style="color:#67c23a">{{ overview.activeBorrows || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">已归还</div>
          <div class="stat-value" style="color:#909399">{{ overview.returnedCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">逾期数</div>
          <div class="stat-value" style="color:#f56c6c">{{ overview.overdueCount || 0 }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Borrow Trend Chart -->
    <el-card shadow="hover" style="margin-bottom: 20px">
      <div slot="header">
        <span>借阅趋势分析</span>
        <el-radio-group v-model="trendType" size="mini" style="margin-left: 20px" @change="loadTrend">
          <el-radio-button label="monthly">按月</el-radio-button>
          <el-radio-button label="daily">按日</el-radio-button>
        </el-radio-group>
      </div>
      <div ref="trendChart" class="chart-container"></div>
    </el-card>

    <!-- Two columns -->
    <el-row :gutter="20">
      <!-- Category Distribution -->
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover">
          <div slot="header"><span>馆藏分类分布</span></div>
          <div ref="categoryChart" class="chart-container"></div>
        </el-card>
      </el-col>

      <!-- Top Borrowed Books -->
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover">
          <div slot="header"><span>热门借阅排行 Top 10</span></div>
          <div ref="topBooksChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Reader Activity Ranking -->
    <el-card shadow="hover" style="margin-top: 20px">
      <div slot="header"><span>读者活跃度排行 Top 10</span></div>
      <el-table :data="readerRanking" stripe size="small">
        <el-table-column type="index" label="排名" width="60" align="center">
          <template slot-scope="{$index}">
            <el-tag :type="$index < 3 ? 'danger' : 'info'" size="mini">{{ $index + 1 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="readerNo" label="读者证号" width="120" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="borrowCount" label="借阅次数" width="100" align="center">
          <template slot-scope="{row}">
            <el-progress :percentage="getPercentage(row.borrowCount)" :format="() => row.borrowCount + '次'" />
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getOverview, getMonthlyTrend, getDailyTrend, getCategoryDistribution, getTopBooks, getReaderRanking } from '@/api/statistics'

export default {
  name: 'Statistics',
  data() {
    return {
      overview: {},
      trendType: 'monthly',
      readerRanking: [],
      maxBorrowCount: 1,
      charts: {}
    }
  },
  mounted() {
    this.loadAll()
    window.addEventListener('resize', this.resizeCharts)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.resizeCharts)
    Object.values(this.charts).forEach(c => c && c.dispose())
  },
  methods: {
    async loadAll() {
      await Promise.all([
        this.loadOverview(),
        this.loadTrend(),
        this.loadCategoryChart(),
        this.loadTopBooksChart(),
        this.loadReaderRanking()
      ])
    },
    async loadOverview() {
      try {
        const res = await getOverview()
        this.overview = res.data
      } catch (e) { /* ignore */ }
    },
    async loadTrend() {
      try {
        let res
        if (this.trendType === 'monthly') {
          res = await getMonthlyTrend(12)
        } else {
          res = await getDailyTrend(30)
        }
        this.$nextTick(() => {
          if (!this.charts.trend) {
            this.charts.trend = echarts.init(this.$refs.trendChart)
          }
          this.charts.trend.setOption({
            tooltip: { trigger: 'axis' },
            grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
            xAxis: {
              type: 'category',
              data: res.data.months || res.data.dates || [],
              axisLabel: { rotate: this.trendType === 'daily' ? 45 : 0 }
            },
            yAxis: { type: 'value', minInterval: 1 },
            series: [{
              name: '借阅量',
              type: 'line',
              data: res.data.counts || [],
              smooth: true,
              symbol: 'circle',
              symbolSize: 6,
              lineStyle: { width: 3, color: '#409eff' },
              itemStyle: { color: '#409eff' },
              areaStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: 'rgba(64,158,255,0.5)' },
                  { offset: 1, color: 'rgba(64,158,255,0.05)' }
                ])
              }
            }]
          })
        })
      } catch (e) { /* ignore */ }
    },
    async loadCategoryChart() {
      try {
        const res = await getCategoryDistribution()
        this.$nextTick(() => {
          if (!this.charts.category) {
            this.charts.category = echarts.init(this.$refs.categoryChart)
          }
          const data = (res.data.raw || []).map(item => ({
            name: item.categoryName,
            value: item.bookCount
          }))
          this.charts.category.setOption({
            tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
            legend: { bottom: 0, type: 'scroll' },
            color: ['#5470c6','#91cc75','#fac858','#ee6666','#73c0de','#3ba272','#fc8452','#9a60b4','#ea7ccc'],
            series: [{
              type: 'pie',
              radius: ['35%', '65%'],
              center: ['50%', '45%'],
              data: data,
              label: { formatter: '{b}\n{c}本' },
              emphasis: {
                itemStyle: {
                  shadowBlur: 10,
                  shadowOffsetX: 0,
                  shadowColor: 'rgba(0,0,0,0.5)'
                }
              }
            }]
          })
        })
      } catch (e) { /* ignore */ }
    },
    async loadTopBooksChart() {
      try {
        const res = await getTopBooks(10)
        this.$nextTick(() => {
          if (!this.charts.topBooks) {
            this.charts.topBooks = echarts.init(this.$refs.topBooksChart)
          }
          const raw = res.data.raw || []
          const titles = raw.map(r => r.bookTitle)
          const counts = raw.map(r => r.borrowCount)
          this.charts.topBooks.setOption({
            tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
            grid: { left: '3%', right: '8%', bottom: '3%', containLabel: true },
            xAxis: { type: 'value', minInterval: 1 },
            yAxis: {
              type: 'category',
              data: titles.reverse(),
              axisLabel: { width: 80, overflow: 'truncate' }
            },
            series: [{
              type: 'bar',
              data: counts.reverse(),
              barWidth: '50%',
              itemStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                  { offset: 0, color: '#73c0de' },
                  { offset: 1, color: '#5470c6' }
                ]),
                borderRadius: [0, 4, 4, 0]
              },
              label: { show: true, position: 'right' }
            }]
          })
        })
      } catch (e) { /* ignore */ }
    },
    async loadReaderRanking() {
      try {
        const res = await getReaderRanking(10)
        this.readerRanking = res.data.raw || []
        this.maxBorrowCount = Math.max(...this.readerRanking.map(r => r.borrowCount || 0), 1)
      } catch (e) { /* ignore */ }
    },
    getPercentage(count) {
      return Math.round((count / this.maxBorrowCount) * 100)
    },
    resizeCharts() {
      Object.values(this.charts).forEach(c => c && c.resize())
    }
  }
}
</script>

<style scoped>
.stat-card {
  text-align: center;
}
.stat-card .stat-label {
  font-size: 14px;
  color: #909399;
}
.stat-card .stat-value {
  font-size: 28px;
  font-weight: 700;
  margin: 8px 0;
}
.chart-container {
  width: 100%;
  height: 350px;
}
</style>

<template>
  <div class="app-container">
    <!-- Quote Banner -->
    <div class="hero-banner">
      <div class="hero-image" :style="{ backgroundImage: `url(${bannerImage})` }"></div>
      <div class="hero-gradient"></div>
      <div class="hero-content">
        <transition name="hero-fade" mode="out-in">
          <div class="hero-quote" :key="dailyQuote.text">
            <div class="hero-quote-icon"><i class="el-icon-reading"></i></div>
            <p class="hero-quote-text">{{ dailyQuote.text }}</p>
            <p class="hero-quote-author">—— {{ dailyQuote.author }}</p>
          </div>
        </transition>
      </div>
    </div>

    <!-- Stat Cards -->
    <el-row :gutter="16" class="stat-row">
      <el-col :xs="12" :sm="6" v-for="(card, i) in statCards" :key="i">
        <div class="stat-card" :style="{ borderLeftColor: card.color }">
          <div class="stat-icon-wrap" :style="{ background: card.gradient }">
            <i :class="card.icon"></i>
          </div>
          <div class="stat-info">
            <div class="stat-num">{{ card.value }}</div>
            <div class="stat-name">{{ card.label }}</div>
            <div class="stat-extra">{{ card.extra }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- Charts -->
    <el-row :gutter="16" class="chart-row">
      <el-col :xs="24" :lg="14">
        <div class="panel">
          <div class="panel-head">
            <span class="panel-title"><i class="el-icon-data-line"></i> 月度借阅趋势</span>
          </div>
          <div ref="monthlyChart" class="chart-box"></div>
        </div>
      </el-col>
      <el-col :xs="24" :lg="10">
        <div class="panel">
          <div class="panel-head">
            <span class="panel-title"><i class="el-icon-pie-chart"></i> 馆藏分类分布</span>
          </div>
          <div ref="categoryChart" class="chart-box"></div>
        </div>
      </el-col>
    </el-row>

    <!-- Hot Books -->
    <div class="panel" style="margin-top: 16px">
      <div class="panel-head">
        <span class="panel-title"><i class="el-icon-star-on" style="color:#e6a23c"></i> 热门图书 Top 10</span>
      </div>
      <el-table :data="hotBooks" stripe size="small" style="width: 100%">
        <el-table-column type="index" label="#" width="50" align="center">
          <template slot-scope="{$index}">
            <span :class="['rank', $index < 3 ? 'rank-top' : '']">{{ $index + 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="书名" show-overflow-tooltip min-width="200" />
        <el-table-column prop="author" label="作者" width="120" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="100" align="center" />
        <el-table-column prop="availableCount" label="可借" width="70" align="center">
          <template slot-scope="{row}">
            <el-tag :type="row.availableCount > 0 ? 'success' : 'danger'" size="mini" effect="plain">
              {{ row.availableCount }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getOverview, getMonthlyTrend, getCategoryDistribution } from '@/api/statistics'
import { getHotBooks } from '@/api/book'
import { getRandomQuote, getRandomImage } from '@/utils/quotes'

export default {
  name: 'Dashboard',
  data() {
    return {
      stats: {},
      hotBooks: [],
      monthlyChart: null,
      categoryChart: null,
      dailyQuote: getRandomQuote(),
      bannerImage: getRandomImage()
    }
  },
  computed: {
    returnRate() {
      if (!this.stats.totalBorrows || !this.stats.returnedCount) return 0
      return ((this.stats.returnedCount / this.stats.totalBorrows) * 100).toFixed(1)
    },
    statCards() {
      return [
        { label: '馆藏图书', value: this.stats.totalBooks || 0, extra: `可借 ${this.stats.availableBooks || 0} 本`, icon: 'el-icon-reading', color: '#409eff', gradient: 'linear-gradient(135deg, #409eff, #36cfc9)' },
        { label: '借阅总数', value: this.stats.totalBorrows || 0, extra: `在借 ${this.stats.activeBorrows || 0} 次`, icon: 'el-icon-document', color: '#67c23a', gradient: 'linear-gradient(135deg, #67c23a, #95de64)' },
        { label: '已归还', value: this.stats.returnedCount || 0, extra: `归还率 ${this.returnRate}%`, icon: 'el-icon-circle-check', color: '#13c2c2', gradient: 'linear-gradient(135deg, #13c2c2, #5cdbd3)' },
        { label: '逾期图书', value: this.stats.overdueCount || 0, extra: `罚款 ¥${this.stats.totalFine || '0.00'}`, icon: 'el-icon-warning-outline', color: '#ff7875', gradient: 'linear-gradient(135deg, #ff7875, #ffccc7)' }
      ]
    }
  },
  mounted() {
    this.loadOverview()
    this.loadMonthlyChart()
    this.loadCategoryChart()
    this.loadHotBooks()
    window.addEventListener('resize', this.resizeCharts)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.resizeCharts)
    if (this.monthlyChart) this.monthlyChart.dispose()
    if (this.categoryChart) this.categoryChart.dispose()
  },
  methods: {
    async loadOverview() {
      try {
        const res = await getOverview()
        this.stats = res.data
      } catch (e) {}
    },
    async loadMonthlyChart() {
      try {
        const res = await getMonthlyTrend(6)
        this.$nextTick(() => {
          this.monthlyChart = echarts.init(this.$refs.monthlyChart)
          this.monthlyChart.setOption({
            tooltip: { trigger: 'axis', backgroundColor: '#fff', borderColor: '#eee', textStyle: { color: '#333' } },
            grid: { left: '3%', right: '5%', bottom: '3%', top: '5%', containLabel: true },
            xAxis: { type: 'category', data: res.data.months || [], axisLine: { lineStyle: { color: '#ddd' } }, axisLabel: { color: '#999' } },
            yAxis: { type: 'value', minInterval: 1, splitLine: { lineStyle: { color: '#f5f5f5' } }, axisLabel: { color: '#999' } },
            series: [{
              name: '借阅量',
              type: 'bar',
              data: res.data.counts || [],
              itemStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: '#667eea' },
                  { offset: 1, color: '#764ba2' }
                ]),
                borderRadius: [6, 6, 0, 0]
              },
              barWidth: '35%'
            }]
          })
        })
      } catch (e) {}
    },
    async loadCategoryChart() {
      try {
        const res = await getCategoryDistribution()
        this.$nextTick(() => {
          this.categoryChart = echarts.init(this.$refs.categoryChart)
          const data = (res.data.raw || []).map(item => ({
            name: item.categoryName, value: item.bookCount
          }))
          this.categoryChart.setOption({
            tooltip: { trigger: 'item', formatter: '{b}: {c}本 ({d}%)' },
            legend: { bottom: 0, type: 'scroll', textStyle: { fontSize: 11, color: '#888' } },
            color: ['#5470c6','#91cc75','#fac858','#ee6666','#73c0de','#3ba272','#fc8452','#9a60b4','#ea7ccc','#53a8e2','#41c463','#f5994e'],
            series: [{
              type: 'pie',
              radius: ['38%', '65%'],
              center: ['50%', '42%'],
              data: data,
              label: { show: false },
              emphasis: {
                label: { show: true, fontSize: 14, fontWeight: 'bold' },
                itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.2)' }
              }
            }]
          })
        })
      } catch (e) {}
    },
    async loadHotBooks() {
      try {
        const res = await getHotBooks()
        this.hotBooks = res.data || []
      } catch (e) {}
    },
    resizeCharts() {
      if (this.monthlyChart) this.monthlyChart.resize()
      if (this.categoryChart) this.categoryChart.resize()
    }
  }
}
</script>

<style scoped>
/* Hero Banner */
.hero-banner {
  position: relative;
  height: 200px;
  border-radius: 14px;
  overflow: hidden;
  margin-bottom: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.hero-image {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center center;
  background-repeat: no-repeat;
  transition: transform 8s ease;
}

.hero-banner:hover .hero-image {
  transform: scale(1.05);
}

.hero-gradient {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(30, 40, 60, 0.82) 0%, rgba(50, 60, 90, 0.55) 100%);
}

.hero-content {
  position: relative;
  z-index: 1;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 40px;
}

.hero-quote {
  text-align: center;
  color: #fff;
}

.hero-quote-icon {
  font-size: 28px;
  margin-bottom: 12px;
  opacity: 0.7;
}

.hero-quote-text {
  font-size: 24px;
  font-weight: 500;
  letter-spacing: 3px;
  line-height: 1.5;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.25);
  margin: 0 0 10px 0;
}

.hero-quote-author {
  font-size: 14px;
  opacity: 0.75;
  letter-spacing: 2px;
  margin: 0;
}

.hero-fade-enter-active, .hero-fade-leave-active {
  transition: all 0.5s ease;
}
.hero-fade-enter {
  opacity: 0;
  transform: translateX(20px);
}
.hero-fade-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

/* Stat Cards */
.stat-row {
  margin-bottom: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 18px 20px;
  background: #fff;
  border-radius: 12px;
  margin-bottom: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
  border-left: 4px solid #409eff;
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.stat-icon-wrap {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  color: #fff;
  margin-right: 16px;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-num {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a2e;
  line-height: 1.2;
}

.stat-name {
  font-size: 13px;
  color: #909399;
  margin: 2px 0;
}

.stat-extra {
  font-size: 11px;
  color: #c0c4cc;
}

/* Panel */
.panel {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.panel-head {
  padding: 16px 20px;
  border-bottom: 1px solid #f5f5f5;
}

.panel-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.panel-title i {
  font-size: 18px;
  color: #409eff;
}

.chart-box {
  width: 100%;
  height: 300px;
  padding: 8px;
}

.chart-row {
  margin-bottom: 0;
}

/* Rank badge */
.rank {
  display: inline-block;
  width: 22px;
  height: 22px;
  line-height: 22px;
  border-radius: 50%;
  font-size: 11px;
  font-weight: 700;
  background: #f0f0f0;
  color: #aaa;
}

.rank.rank-top {
  background: linear-gradient(135deg, #ff6b6b, #ee5a24);
  color: #fff;
  box-shadow: 0 2px 6px rgba(238, 90, 36, 0.3);
}
</style>

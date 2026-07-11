<template>
  <div class="app-container">
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="query" size="small">
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width:120px">
            <el-option label="借阅中" :value="0" />
            <el-option label="已归还" :value="1" />
            <el-option label="已逾期" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <div slot="header">
        <span><i class="el-icon-notebook-2"></i> 我的借阅记录</span>
      </div>
      <el-table :data="tableData" v-loading="loading" border stripe size="small">
        <el-table-column prop="recordNo" label="单号" width="160" />
        <el-table-column prop="bookTitle" label="书名" min-width="150" show-overflow-tooltip />
        <el-table-column prop="bookIsbn" label="ISBN" width="130" />
        <el-table-column prop="borrowDate" label="借阅时间" width="150" />
        <el-table-column prop="dueDate" label="应还时间" width="150" />
        <el-table-column prop="returnDate" label="归还时间" width="150">
          <template slot-scope="{row}">{{ row.returnDate || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template slot-scope="{row}">
            <el-tag :type="statusType(row.status)" size="mini">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fineAmount" label="罚款" width="80" align="right">
          <template slot-scope="{row}">
            <span v-if="row.fineAmount > 0" :style="{color: row.fineStatus === 2 ? '#67c23a' : '#f56c6c'}">
              ¥{{ row.fineAmount }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template slot-scope="{row}">
            <template v-if="row.status !== 1">
              <el-button type="text" size="mini" @click="handleRenew(row)">续借</el-button>
              <el-button type="text" size="mini" @click="handlePayFine(row)" v-if="row.fineStatus === 1">缴罚款</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :total="total"
          :current-page.sync="query.pageNum"
          :page-size="10"
          @current-change="loadData"
        />
      </div>
    </el-card>
  </div>
</template>

<script>
import { getBorrowPage, renewBook, payFine } from '@/api/borrow'

export default {
  name: 'MyBorrows',
  data() {
    return {
      query: { pageNum: 1, pageSize: 10, status: null, userId: null },
      tableData: [],
      total: 0,
      loading: false
    }
  },
  created() {
    this.query.userId = this.$store.getters.userId
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const res = await getBorrowPage(this.query)
        this.tableData = res.data.records || []
        this.total = res.data.total || 0
      } finally {
        this.loading = false
      }
    },
    statusType(status) {
      return { 0: 'primary', 1: 'success', 2: 'danger' }[status] || ''
    },
    statusText(status) {
      return { 0: '借阅中', 1: '已归还', 2: '已逾期' }[status] || '未知'
    },
    async handleRenew(row) {
      try {
        await renewBook(row.id)
        this.$message.success('续借成功')
        this.loadData()
      } catch (e) { /* handled */ }
    },
    async handlePayFine(row) {
      try {
        await this.$confirm(`确认缴纳罚款 ¥${row.fineAmount} 吗？`, '罚款缴纳', { type: 'warning' })
        await payFine(row.id)
        this.$message.success('罚款已缴清')
        this.loadData()
      } catch (e) { /* cancelled */ }
    }
  }
}
</script>

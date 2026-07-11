<template>
  <div class="app-container">
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="query" size="small">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="读者/书名/单号" clearable @keyup.enter.native="handleSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width:100px">
            <el-option label="借阅中" :value="0" />
            <el-option label="已归还" :value="1" />
            <el-option label="已逾期" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
          <el-button type="warning" icon="el-icon-warning-outline" @click="handleCheckOverdue" v-if="canManage">检查逾期</el-button>
          <el-button type="success" icon="el-icon-plus" @click="borrowDialogVisible = true">借书登记</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="tableData" v-loading="loading" border stripe size="small">
        <el-table-column prop="recordNo" label="借阅单号" width="160" />
        <el-table-column prop="readerNo" label="读者证号" width="110" />
        <el-table-column prop="realName" label="读者姓名" width="80" />
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
        <el-table-column prop="fineAmount" label="罚款" width="70" align="right">
          <template slot-scope="{row}">
            <span v-if="row.fineAmount > 0" :style="{color: row.fineStatus === 2 ? '#67c23a' : '#f56c6c'}">
              ¥{{ row.fineAmount }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template slot-scope="{row}">
            <template v-if="row.status !== 1">
              <el-button type="text" size="mini" @click="handleReturn(row)" v-if="canManage">归还</el-button>
              <el-button type="text" size="mini" @click="handleRenew(row)">续借</el-button>
              <el-button type="text" size="mini" @click="handlePayFine(row)" v-if="row.fineStatus === 1">缴罚款</el-button>
            </template>
            <el-tag v-else type="info" size="mini">已完成</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :current-page.sync="query.pageNum"
          :page-size.sync="query.pageSize"
          :page-sizes="[10, 20, 50]"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- Borrow Dialog -->
    <el-dialog title="借书登记" :visible.sync="borrowDialogVisible" width="450px" :close-on-click-modal="false">
      <el-form ref="borrowForm" :model="borrowForm" :rules="borrowRules" label-width="80px" size="small">
        <el-form-item label="读者" prop="userId">
          <el-select v-model="borrowForm.userId" filterable placeholder="搜索读者" style="width:100%">
            <el-option
              v-for="r in readers"
              :key="r.id"
              :label="`${r.readerNo} - ${r.realName || r.username}`"
              :value="r.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="图书" prop="bookId">
          <el-select v-model="borrowForm.bookId" filterable remote :remote-method="searchBooks"
            placeholder="搜索书名或ISBN" style="width:100%" :loading="bookSearching">
            <el-option
              v-for="b in bookOptions"
              :key="b.id"
              :label="`${b.title} (可借:${b.availableCount})`"
              :value="b.id"
              :disabled="b.availableCount === 0"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button size="small" @click="borrowDialogVisible = false">取 消</el-button>
        <el-button type="primary" size="small" :loading="borrowing" @click="handleBorrow">确认借阅</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getBorrowPage, returnBook, renewBook, payFine, borrowBook, checkOverdue } from '@/api/borrow'
import { getAllReaders } from '@/api/user'
import { getBookPage } from '@/api/book'

export default {
  name: 'BorrowManage',
  data() {
    return {
      query: { pageNum: 1, pageSize: 10, keyword: '', status: null },
      tableData: [],
      total: 0,
      loading: false,
      readers: [],
      borrowDialogVisible: false,
      borrowing: false,
      bookOptions: [],
      bookSearching: false,
      borrowForm: { userId: null, bookId: null },
      borrowRules: {
        userId: [{ required: true, message: '请选择读者', trigger: 'change' }],
        bookId: [{ required: true, message: '请选择图书', trigger: 'change' }]
      }
    }
  },
  computed: {
    canManage() {
      const role = this.$store.getters.roleCode
      return role === 'ADMIN' || role === 'LIBRARIAN'
    }
  },
  async created() {
    this.loadData()
    this.loadReaders()
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
    async loadReaders() {
      try {
        const res = await getAllReaders()
        this.readers = res.data || []
      } catch (e) { /* ignore */ }
    },
    async searchBooks(query) {
      if (!query) {
        this.bookOptions = []
        return
      }
      this.bookSearching = true
      try {
        const res = await getBookPage({ pageNum: 1, pageSize: 20, keyword: query })
        this.bookOptions = (res.data.records || []).filter(b => b.availableCount > 0)
      } finally {
        this.bookSearching = false
      }
    },
    statusType(status) {
      return { 0: 'primary', 1: 'success', 2: 'danger' }[status] || ''
    },
    statusText(status) {
      return { 0: '借阅中', 1: '已归还', 2: '已逾期' }[status] || '未知'
    },
    handleSearch() {
      this.query.pageNum = 1
      this.loadData()
    },
    handleReset() {
      this.query = { pageNum: 1, pageSize: 10, keyword: '', status: null }
      this.loadData()
    },
    async handleReturn(row) {
      try {
        await this.$confirm(`确认归还《${row.bookTitle}》吗？`, '归还确认', { type: 'warning' })
        const res = await returnBook(row.id)
        if (res.data.fineAmount > 0) {
          this.$message.warning(`归还成功，产生逾期罚款 ¥${res.data.fineAmount}`)
        } else {
          this.$message.success('归还成功')
        }
        this.loadData()
      } catch (e) { /* cancelled */ }
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
    },
    async handleCheckOverdue() {
      try {
        const res = await checkOverdue()
        this.$message.success(`检查完成，标记 ${res.data} 条逾期记录`)
        this.loadData()
      } catch (e) { /* handled */ }
    },
    handleBorrow() {
      this.$refs.borrowForm.validate(async valid => {
        if (!valid) return
        this.borrowing = true
        try {
          await borrowBook(this.borrowForm)
          this.$message.success('借阅成功')
          this.borrowDialogVisible = false
          this.borrowForm = { userId: null, bookId: null }
          this.bookOptions = []
          this.loadData()
        } finally {
          this.borrowing = false
        }
      })
    }
  }
}
</script>

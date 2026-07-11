<template>
  <div class="app-container">
    <!-- Search & Filter -->
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="query" size="small">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="书名/ISBN" clearable @keyup.enter.native="handleSearch" />
        </el-form-item>
        <el-form-item label="分类">
          <el-cascader
            v-model="query.categoryPath"
            :options="categoryTree"
            :props="{ checkStrictly: true, value: 'id', label: 'name' }"
            placeholder="选择分类"
            clearable
            @change="handleCategoryChange"
          />
        </el-form-item>
        <el-form-item label="作者">
          <el-input v-model="query.author" placeholder="作者名" clearable @keyup.enter.native="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Table -->
    <el-card shadow="never">
      <div class="table-actions">
        <el-button type="primary" icon="el-icon-plus" size="small" @click="handleAdd" v-if="canEdit">添加图书</el-button>
        <el-button icon="el-icon-refresh-right" size="small" @click="loadData">刷新</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe size="small">
        <el-table-column prop="isbn" label="ISBN" width="140" />
        <el-table-column prop="title" label="书名" min-width="160" show-overflow-tooltip />
        <el-table-column prop="author" label="作者" width="100" show-overflow-tooltip />
        <el-table-column prop="publisher" label="出版社" width="120" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="80" />
        <el-table-column prop="price" label="定价" width="70" align="right">
          <template slot-scope="{row}">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="location" label="位置" width="100" />
        <el-table-column label="库存" width="80" align="center">
          <template slot-scope="{row}">
            <span>{{ row.availableCount }} / {{ row.totalCount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="70" align="center">
          <template slot-scope="{row}">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="mini">
              {{ row.status === 1 ? '在馆' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template slot-scope="{row}">
            <el-button type="text" size="mini" @click="handleView(row)">详情</el-button>
            <el-button type="text" size="mini" @click="handleBorrow(row)" v-if="!canEdit && row.availableCount > 0">借阅</el-button>
            <el-button type="text" size="mini" @click="handleEdit(row)" v-if="canEdit">编辑</el-button>
            <el-button type="text" size="mini" style="color:#f56c6c" @click="handleDelete(row)" v-if="canEdit">删除</el-button>
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

    <!-- Add/Edit Dialog -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="650px" :close-on-click-modal="false">
      <el-form ref="bookForm" :model="bookForm" :rules="rules" label-width="80px" size="small">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="ISBN" prop="isbn">
              <el-input v-model="bookForm.isbn" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="书名" prop="title">
              <el-input v-model="bookForm.title" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="作者" prop="author">
              <el-input v-model="bookForm.author" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出版社">
              <el-input v-model="bookForm.publisher" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="分类">
              <el-select v-model="bookForm.categoryId" placeholder="选择分类" style="width:100%">
                <el-option v-for="c in flatCategories" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="定价">
              <el-input-number v-model="bookForm.price" :precision="2" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="馆藏位置">
              <el-input v-model="bookForm.location" placeholder="如: A区-01-003" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="馆藏数">
              <el-input-number v-model="bookForm.totalCount" :min="1" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="可借数">
              <el-input-number v-model="bookForm.availableCount" :min="0" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="出版日期">
          <el-date-picker v-model="bookForm.publishDate" type="date" value-format="yyyy-MM-dd" style="width:100%" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="bookForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button size="small" @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" size="small" :loading="saving" @click="handleSave">保 存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getBookPage, addBook, updateBook, deleteBook } from '@/api/book'
import { getCategoryTree, getAllCategories } from '@/api/category'
import { borrowBook } from '@/api/borrow'

export default {
  name: 'BookManage',
  data() {
    return {
      query: { pageNum: 1, pageSize: 10, keyword: '', categoryId: null, author: '', categoryPath: [] },
      tableData: [],
      total: 0,
      loading: false,
      categoryTree: [],
      flatCategories: [],
      dialogVisible: false,
      isEdit: false,
      saving: false,
      bookForm: {},
      rules: {
        isbn: [{ required: true, message: '请输入ISBN', trigger: 'blur' }],
        title: [{ required: true, message: '请输入书名', trigger: 'blur' }]
      }
    }
  },
  computed: {
    canEdit() {
      const role = this.$store.getters.roleCode
      return role === 'ADMIN' || role === 'LIBRARIAN'
    },
    dialogTitle() {
      return this.isEdit ? '编辑图书' : '添加图书'
    }
  },
  created() {
    this.loadCategories()
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const res = await getBookPage(this.query)
        this.tableData = res.data.records || []
        this.total = res.data.total || 0
      } finally {
        this.loading = false
      }
    },
    async loadCategories() {
      const [treeRes, flatRes] = await Promise.all([getCategoryTree(), getAllCategories()])
      this.categoryTree = treeRes.data || []
      this.flatCategories = flatRes.data || []
    },
    handleCategoryChange(val) {
      if (val && val.length > 0) {
        this.query.categoryId = val[val.length - 1]
      } else {
        this.query.categoryId = null
      }
    },
    handleSearch() {
      this.query.pageNum = 1
      this.loadData()
    },
    handleReset() {
      this.query = { pageNum: 1, pageSize: 10, keyword: '', categoryId: null, author: '', categoryPath: [] }
      this.loadData()
    },
    handleAdd() {
      this.isEdit = false
      this.bookForm = { totalCount: 1, availableCount: 1, status: 1 }
      this.dialogVisible = true
      this.$nextTick(() => this.$refs.bookForm && this.$refs.bookForm.clearValidate())
    },
    handleEdit(row) {
      this.isEdit = true
      this.bookForm = { ...row }
      if (row.publishDate) {
        this.bookForm.publishDate = row.publishDate
      }
      this.dialogVisible = true
    },
    handleView(row) {
      this.$alert(
        `<div style="line-height:2">
          <p><b>书名:</b> ${row.title}</p>
          <p><b>作者:</b> ${row.author || '-'}</p>
          <p><b>出版社:</b> ${row.publisher || '-'}</p>
          <p><b>ISBN:</b> ${row.isbn}</p>
          <p><b>分类:</b> ${row.categoryName || '-'}</p>
          <p><b>定价:</b> ¥${row.price}</p>
          <p><b>馆藏位置:</b> ${row.location || '-'}</p>
          <p><b>库存:</b> ${row.availableCount} / ${row.totalCount}</p>
          <p><b>简介:</b> ${row.description || '暂无'}</p>
        </div>`,
        '图书详情',
        { dangerouslyUseHTMLString: true }
      )
    },
    async handleBorrow(row) {
      try {
        await this.$confirm(`确认借阅《${row.title}》吗？`, '借阅确认', { type: 'info' })
        await borrowBook({ userId: this.$store.getters.userId, bookId: row.id })
        this.$message.success('借阅成功！借期14天，请按时归还')
        this.loadData()
      } catch (e) { /* cancelled */ }
    },
    async handleDelete(row) {
      try {
        await this.$confirm(`确认删除《${row.title}》吗？`, '警告', { type: 'warning' })
        await deleteBook(row.id)
        this.$message.success('删除成功')
        this.loadData()
      } catch (e) { /* cancelled */ }
    },
    handleSave() {
      this.$refs.bookForm.validate(async valid => {
        if (!valid) return
        this.saving = true
        try {
          if (this.isEdit) {
            await updateBook(this.bookForm)
            this.$message.success('更新成功')
          } else {
            await addBook(this.bookForm)
            this.$message.success('添加成功')
          }
          this.dialogVisible = false
          this.loadData()
        } finally {
          this.saving = false
        }
      })
    }
  }
}
</script>

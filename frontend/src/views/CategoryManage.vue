<template>
  <div class="app-container">
    <el-card shadow="never">
      <div class="table-actions">
        <el-button type="primary" icon="el-icon-plus" size="small" @click="handleAdd(null)">添加分类</el-button>
      </div>

      <el-table
        :data="categoryTree"
        v-loading="loading"
        row-key="id"
        border
        size="small"
        :tree-props="{ children: 'children' }"
      >
        <el-table-column prop="name" label="分类名称" min-width="200" />
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column prop="bookCount" label="图书数量" width="100" align="center">
          <template slot-scope="{row}">
            <el-tag :type="row.bookCount > 0 ? 'success' : 'info'" size="mini">
              {{ row.bookCount || 0 }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="200" fixed="right">
          <template slot-scope="{row}">
            <el-button type="text" size="mini" @click="handleAdd(row)">添加子分类</el-button>
            <el-button type="text" size="mini" @click="handleEdit(row)">编辑</el-button>
            <el-button type="text" size="mini" style="color:#f56c6c" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Add/Edit Dialog -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="450px" :close-on-click-modal="false">
      <el-form ref="categoryForm" :model="categoryForm" :rules="rules" label-width="80px" size="small">
        <el-form-item label="上级分类">
          <el-input :value="parentName" disabled />
        </el-form-item>
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="categoryForm.name" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="categoryForm.sort" :min="0" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="categoryForm.description" type="textarea" :rows="3" />
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
import { getCategoryTree, addCategory, updateCategory, deleteCategory } from '@/api/category'

export default {
  name: 'CategoryManage',
  data() {
    return {
      categoryTree: [],
      loading: false,
      dialogVisible: false,
      isEdit: false,
      saving: false,
      parentName: '顶级分类',
      categoryForm: { parentId: 0, sort: 0 },
      rules: {
        name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
      }
    }
  },
  computed: {
    dialogTitle() {
      return this.isEdit ? '编辑分类' : '添加分类'
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const res = await getCategoryTree()
        this.categoryTree = res.data || []
      } finally {
        this.loading = false
      }
    },
    handleAdd(parent) {
      this.isEdit = false
      if (parent) {
        this.parentName = parent.name
        this.categoryForm = { parentId: parent.id, sort: 0 }
      } else {
        this.parentName = '顶级分类'
        this.categoryForm = { parentId: 0, sort: 0 }
      }
      this.dialogVisible = true
      this.$nextTick(() => this.$refs.categoryForm && this.$refs.categoryForm.clearValidate())
    },
    handleEdit(row) {
      this.isEdit = true
      this.categoryForm = { ...row }
      this.parentName = row.parentId === 0 ? '顶级分类' : '子分类'
      this.dialogVisible = true
    },
    async handleDelete(row) {
      try {
        await this.$confirm(`确认删除分类"${row.name}"吗？`, '警告', { type: 'warning' })
        await deleteCategory(row.id)
        this.$message.success('删除成功')
        this.loadData()
      } catch (e) { /* cancelled */ }
    },
    handleSave() {
      this.$refs.categoryForm.validate(async valid => {
        if (!valid) return
        this.saving = true
        try {
          if (this.isEdit) {
            await updateCategory(this.categoryForm)
            this.$message.success('更新成功')
          } else {
            await addCategory(this.categoryForm)
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

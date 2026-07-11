<template>
  <div class="app-container">
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="query" size="small">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="用户名/姓名/读者证号/手机" clearable @keyup.enter.native="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <div class="table-actions">
        <el-button type="primary" icon="el-icon-plus" size="small" @click="handleAdd">添加用户</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe size="small">
        <el-table-column prop="readerNo" label="读者证号" width="120" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="100" />
        <el-table-column prop="gender" label="性别" width="60" align="center">
          <template slot-scope="{row}">
            {{ row.gender === 1 ? '男' : row.gender === 2 ? '女' : '未知' }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
        <el-table-column prop="roleName" label="角色" width="100" align="center">
          <template slot-scope="{row}">
            <el-tag size="mini" :type="roleTagType(row.roleCode)">{{ row.roleName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="70" align="center">
          <template slot-scope="{row}">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="mini">
              {{ row.status === 1 ? '正常' : '冻结' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template slot-scope="{row}">
            <el-button type="text" size="mini" @click="handleEdit(row)">编辑</el-button>
            <el-button type="text" size="mini" @click="handleResetPassword(row)">重置密码</el-button>
            <el-button type="text" size="mini" style="color:#f56c6c" @click="handleDelete(row)">删除</el-button>
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
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="550px" :close-on-click-modal="false">
      <el-form ref="userForm" :model="userForm" :rules="rules" label-width="80px" size="small">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="userForm.username" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="密码" prop="password" v-if="!isEdit">
              <el-input v-model="userForm.password" show-password />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="真实姓名">
              <el-input v-model="userForm.realName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="角色">
              <el-select v-model="userForm.roleId" placeholder="选择角色" style="width:100%">
                <el-option v-for="r in roles" :key="r.id" :label="r.roleName" :value="r.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="性别">
              <el-radio-group v-model="userForm.gender">
                <el-radio :label="1">男</el-radio>
                <el-radio :label="2">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-switch v-model="userForm.status" :active-value="1" :inactive-value="0" active-text="正常" inactive-text="冻结" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="手机号">
              <el-input v-model="userForm.phone" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱">
              <el-input v-model="userForm.email" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <span slot="footer">
        <el-button size="small" @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" size="small" :loading="saving" @click="handleSave">保 存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getUserPage, addUser, updateUser, deleteUser, resetPassword } from '@/api/user'
import request from '@/utils/request'

export default {
  name: 'ReaderManage',
  data() {
    return {
      query: { pageNum: 1, pageSize: 10, keyword: '' },
      tableData: [],
      total: 0,
      loading: false,
      dialogVisible: false,
      isEdit: false,
      saving: false,
      roles: [],
      userForm: {},
      rules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      }
    }
  },
  computed: {
    dialogTitle() {
      return this.isEdit ? '编辑用户' : '添加用户'
    }
  },
  async created() {
    await this.loadRoles()
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const res = await getUserPage(this.query)
        this.tableData = res.data.records || []
        this.total = res.data.total || 0
      } finally {
        this.loading = false
      }
    },
    async loadRoles() {
      try {
        const res = await request.get('/categories') // placeholder; roles loaded from endpoint if available
        // In a real app, roles would come from a dedicated endpoint
        this.roles = [
          { id: 1, roleName: '系统管理员', roleCode: 'ADMIN' },
          { id: 2, roleName: '图书管理员', roleCode: 'LIBRARIAN' },
          { id: 3, roleName: '读者', roleCode: 'READER' }
        ]
      } catch (e) {
        this.roles = [
          { id: 1, roleName: '系统管理员', roleCode: 'ADMIN' },
          { id: 2, roleName: '图书管理员', roleCode: 'LIBRARIAN' },
          { id: 3, roleName: '读者', roleCode: 'READER' }
        ]
      }
    },
    roleTagType(code) {
      const map = { ADMIN: 'danger', LIBRARIAN: 'warning', READER: '' }
      return map[code] || ''
    },
    handleSearch() {
      this.query.pageNum = 1
      this.loadData()
    },
    handleReset() {
      this.query = { pageNum: 1, pageSize: 10, keyword: '' }
      this.loadData()
    },
    handleAdd() {
      this.isEdit = false
      this.userForm = { username: '', password: '', realName: '', gender: 0, phone: '', email: '', roleId: 3, status: 1 }
      this.dialogVisible = true
      this.$nextTick(() => this.$refs.userForm && this.$refs.userForm.clearValidate())
    },
    handleEdit(row) {
      this.isEdit = true
      this.userForm = { ...row }
      this.dialogVisible = true
      this.$nextTick(() => this.$refs.userForm && this.$refs.userForm.clearValidate())
    },
    async handleDelete(row) {
      try {
        await this.$confirm(`确认删除用户"${row.username}"吗？`, '警告', { type: 'warning' })
        await deleteUser(row.id)
        this.$message.success('删除成功')
        this.loadData()
      } catch (e) { /* cancelled */ }
    },
    async handleResetPassword(row) {
      try {
        const { value } = await this.$prompt('请输入新密码', '重置密码', {
          inputPattern: /^.{6,}$/,
          inputErrorMessage: '密码至少6位'
        })
        await resetPassword(row.id, value)
        this.$message.success('密码重置成功')
      } catch (e) { /* cancelled */ }
    },
    handleSave() {
      this.$refs.userForm.validate(async valid => {
        if (!valid) return
        this.saving = true
        try {
          if (this.isEdit) {
            await updateUser(this.userForm)
            this.$message.success('更新成功')
          } else {
            await addUser(this.userForm)
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

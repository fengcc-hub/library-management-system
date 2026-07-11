<template>
  <el-container class="layout-container">
    <!-- Sidebar -->
    <el-aside :width="isCollapse ? '64px' : '230px'" class="sidebar">
      <div class="logo-area">
        <div class="logo-icon"><i class="el-icon-reading"></i></div>
        <transition name="fade">
          <div v-show="!isCollapse" class="logo-text">
            <span class="logo-title">图书馆</span>
            <span class="logo-sub">管理系统</span>
          </div>
        </transition>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        background-color="transparent"
        text-color="#a0aec0"
        active-text-color="#fff"
        router
        class="side-menu"
      >
        <template v-for="route in menuRoutes">
          <el-menu-item
            v-if="hasPermission(route.meta.roles)"
            :key="route.path"
            :index="'/' + route.path"
            class="menu-item"
          >
            <i :class="route.meta.icon"></i>
            <span slot="title">{{ route.meta.title }}</span>
          </el-menu-item>
        </template>
      </el-menu>
      <!-- Quote at bottom -->
      <transition name="fade" mode="out-in">
        <div v-if="!isCollapse" class="side-quote" :key="sidebarQuote.text">
          <i class="el-icon-chat-dot-square quote-icon"></i>
          <p class="side-quote-text">{{ sidebarQuote.text }}</p>
          <p class="side-quote-author">—— {{ sidebarQuote.author }}</p>
        </div>
      </transition>
    </el-aside>

    <!-- Main Content -->
    <el-container>
      <el-header class="topbar">
        <div class="topbar-left">
          <i
            :class="isCollapse ? 'el-icon-s-unfold' : 'el-icon-s-fold'"
            @click="isCollapse = !isCollapse"
            class="toggle-btn"
          ></i>
          <el-breadcrumb separator="/" class="crumb">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="topbar-right">
          <el-tooltip content="换一句名言" placement="bottom">
            <i class="el-icon-magic-stick refresh-btn" @click="refreshQuote"></i>
          </el-tooltip>
          <el-dropdown @command="handleCommand" trigger="click">
            <div class="user-dropdown">
              <div class="user-avatar" :style="{ background: avatarGradient }">
                {{ (realName || username || '?').charAt(0) }}
              </div>
              <span class="user-name">{{ realName || username }}</span>
              <el-tag size="mini" :type="roleTagType" effect="dark">{{ roleCode }}</el-tag>
              <i class="el-icon-arrow-down"></i>
            </div>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="profile" icon="el-icon-user">个人信息</el-dropdown-item>
              <el-dropdown-item command="logout" divided icon="el-icon-switch-button">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-area">
        <router-view :key="$route.fullPath" />
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import { logout } from '@/api/auth'
import router from '@/router'
import { getRandomQuote } from '@/utils/quotes'

export default {
  name: 'Layout',
  data() {
    return {
      isCollapse: false,
      sidebarQuote: getRandomQuote()
    }
  },
  computed: {
    username() {
      return this.$store.getters.username
    },
    realName() {
      return this.$store.getters.realName
    },
    roleCode() {
      const code = this.$store.getters.roleCode
      const map = { ADMIN: '管理员', LIBRARIAN: '图书管理员', READER: '读者' }
      return map[code] || code
    },
    roleTagType() {
      const map = { ADMIN: 'danger', LIBRARIAN: 'warning', READER: 'success' }
      return map[this.$store.getters.roleCode] || ''
    },
    avatarGradient() {
      const map = {
        ADMIN: 'linear-gradient(135deg, #ff6b6b, #ee5a24)',
        LIBRARIAN: 'linear-gradient(135deg, #f6ad55, #ed8936)',
        READER: 'linear-gradient(135deg, #68d391, #38b2ac)'
      }
      return map[this.$store.getters.roleCode] || 'linear-gradient(135deg, #667eea, #764ba2)'
    },
    activeMenu() {
      return this.$route.path
    },
    currentTitle() {
      return this.$route.meta.title || ''
    },
    menuRoutes() {
      return router.options.routes
        .find(r => r.path === '/')
        .children.filter(r => r.meta && r.meta.title)
    }
  },
  methods: {
    hasPermission(roles) {
      if (!roles || roles.length === 0) return true
      const userRole = this.$store.getters.roleCode
      return roles.includes(userRole)
    },
    refreshQuote() {
      this.sidebarQuote = getRandomQuote()
    },
    async handleCommand(command) {
      if (command === 'logout') {
        try {
          await this.$confirm('确定退出登录吗？', '提示', { type: 'warning' })
          await logout().catch(() => {})
          this.$store.dispatch('logout')
          this.$message.success('已退出登录')
          this.$router.push('/login')
        } catch (e) {}
      } else if (command === 'profile') {
        this.$message.info('个人信息功能开发中')
      }
    }
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

/* Sidebar */
.sidebar {
  background: linear-gradient(180deg, #1a1f36 0%, #2d3561 100%);
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  overflow: hidden;
}

.logo-area {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  gap: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  flex-shrink: 0;
}

.logo-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #fff;
  flex-shrink: 0;
}

.logo-text {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
  white-space: nowrap;
}

.logo-title {
  font-size: 16px;
  font-weight: 700;
  color: #fff;
}

.logo-sub {
  font-size: 11px;
  color: #a0aec0;
  letter-spacing: 1px;
}

/* Menu */
.side-menu {
  flex: 1;
  border: none !important;
  padding: 8px 12px;
}

.menu-item {
  border-radius: 8px !important;
  margin-bottom: 4px !important;
  height: 46px !important;
  line-height: 46px !important;
  transition: all 0.2s !important;
}

.menu-item:hover {
  background-color: rgba(255, 255, 255, 0.08) !important;
}

.menu-item.is-active {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.3), rgba(118, 75, 162, 0.2)) !important;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.15);
}

/* Sidebar Quote */
.side-quote {
  padding: 16px 20px;
  margin: 0 12px 12px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 10px;
  border-left: 3px solid rgba(102, 126, 234, 0.5);
}

.quote-icon {
  font-size: 16px;
  color: rgba(160, 174, 192, 0.5);
  margin-bottom: 8px;
  display: block;
}

.side-quote-text {
  font-size: 12px;
  color: #a0aec0;
  line-height: 1.6;
  margin: 0 0 6px 0;
  font-style: italic;
}

.side-quote-author {
  font-size: 11px;
  color: #718096;
  margin: 0;
}

/* Topbar */
.topbar {
  height: 64px !important;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 3px rgba(0, 21, 41, 0.05);
  z-index: 10;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.toggle-btn {
  font-size: 20px;
  cursor: pointer;
  color: #606266;
  transition: color 0.2s;
}

.toggle-btn:hover {
  color: #409eff;
}

.crumb /deep/ .el-breadcrumb__item {
  font-size: 14px;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.refresh-btn {
  font-size: 18px;
  cursor: pointer;
  color: #a0aec0;
  transition: all 0.3s;
}

.refresh-btn:hover {
  color: #667eea;
  transform: rotate(180deg);
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  transition: background 0.2s;
}

.user-dropdown:hover {
  background: #f5f7fa;
}

.user-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  font-weight: 600;
  color: #fff;
}

.user-name {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

/* Main */
.main-area {
  background: #f0f2f5;
  overflow-y: auto;
}

/* Transitions */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter, .fade-leave-to {
  opacity: 0;
}
</style>

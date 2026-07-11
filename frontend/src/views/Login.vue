<template>
  <div class="login-wrapper">
    <!-- Left: Image + Quote -->
    <div class="login-visual" :style="{ backgroundImage: `url(${bgImage})` }">
      <div class="visual-overlay"></div>
      <div class="visual-content">
        <div class="visual-logo">
          <i class="el-icon-reading"></i>
          <span>图书馆管理系统</span>
        </div>
        <transition name="quote-fade" mode="out-in">
          <div class="visual-quote" :key="currentQuote.text">
            <div class="quote-mark">"</div>
            <p class="quote-text">{{ currentQuote.text }}</p>
            <p class="quote-author">—— {{ currentQuote.author }}</p>
          </div>
        </transition>
        <div class="visual-footer">Library Management System</div>
      </div>
    </div>

    <!-- Right: Form -->
    <div class="login-form-side">
      <div class="form-container">
        <h2 class="form-title">{{ mode === 'login' ? '欢迎回来' : '创建账号' }}</h2>
        <p class="form-subtitle">{{ mode === 'login' ? '请登录您的账号' : '注册一个新读者账号' }}</p>

        <!-- Login Form -->
        <el-form v-if="mode === 'login'" ref="loginForm" :model="loginForm" :rules="loginRules" @submit.native.prevent="handleLogin">
          <el-form-item prop="username">
            <el-input v-model="loginForm.username" prefix-icon="el-icon-user" placeholder="用户名" clearable />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="loginForm.password" prefix-icon="el-icon-lock" placeholder="密码" show-password @keyup.enter.native="handleLogin" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" class="submit-btn" @click="handleLogin">登 录</el-button>
          </el-form-item>
          <p class="switch-mode">
            还没有账号？<el-button type="text" @click="switchMode('register')">立即注册 →</el-button>
          </p>
          <div class="demo-tips">
            <p><i class="el-icon-info"></i> 测试账号</p>
            <p>管理员 <code>admin</code> / 密码 <code>123456</code></p>
            <p>图书管理员 <code>librarian</code> / 密码 <code>123456</code></p>
            <p>读者 <code>reader01</code> / 密码 <code>123456</code></p>
          </div>
        </el-form>

        <!-- Register Form -->
        <el-form v-else ref="registerForm" :model="registerForm" :rules="registerRules" @submit.native.prevent="handleRegister">
          <el-form-item prop="username">
            <el-input v-model="registerForm.username" prefix-icon="el-icon-user" placeholder="设置用户名" clearable />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="registerForm.password" prefix-icon="el-icon-lock" placeholder="密码（至少6位）" show-password />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input v-model="registerForm.confirmPassword" prefix-icon="el-icon-lock" placeholder="确认密码" show-password />
          </el-form-item>
          <el-form-item prop="realName">
            <el-input v-model="registerForm.realName" prefix-icon="el-icon-postcard" placeholder="真实姓名" clearable />
          </el-form-item>
          <el-form-item prop="phone">
            <el-input v-model="registerForm.phone" prefix-icon="el-icon-phone" placeholder="手机号" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" class="submit-btn" @click="handleRegister">注 册</el-button>
          </el-form-item>
          <p class="switch-mode">
            已有账号？<el-button type="text" @click="switchMode('login')">← 返回登录</el-button>
          </p>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
import { login, register } from '@/api/auth'
import { getRandomQuote, getRandomImage } from '@/utils/quotes'

export default {
  name: 'Login',
  data() {
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.registerForm.password) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
    return {
      mode: 'login',
      loading: false,
      bgImage: getRandomImage(),
      currentQuote: getRandomQuote(),
      loginForm: { username: '', password: '' },
      loginRules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      },
      registerForm: { username: '', password: '', confirmPassword: '', realName: '', phone: '' },
      registerRules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 20, message: '用户名长度3-20个字符', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, message: '密码至少6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ],
        realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
        phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }]
      }
    }
  },
  methods: {
    switchMode(mode) {
      this.mode = mode
      this.$nextTick(() => {
        if (this.$refs.loginForm) this.$refs.loginForm.clearValidate()
        if (this.$refs.registerForm) this.$refs.registerForm.clearValidate()
      })
    },
    handleLogin() {
      this.$refs.loginForm.validate(async valid => {
        if (!valid) return
        this.loading = true
        try {
          const res = await login(this.loginForm)
          const { token, user } = res.data
          this.$store.dispatch('login', { token, user })
          this.$message.success('登录成功')
          this.$router.push('/dashboard')
        } catch (e) {
        } finally {
          this.loading = false
        }
      })
    },
    handleRegister() {
      this.$refs.registerForm.validate(async valid => {
        if (!valid) return
        this.loading = true
        try {
          await register({
            username: this.registerForm.username,
            password: this.registerForm.password,
            realName: this.registerForm.realName,
            phone: this.registerForm.phone,
            gender: 0
          })
          this.$message.success('注册成功，请登录')
          this.switchMode('login')
          this.loginForm.username = this.registerForm.username
          this.loginForm.password = ''
        } catch (e) {
        } finally {
          this.loading = false
        }
      })
    }
  }
}
</script>

<style scoped>
.login-wrapper {
  height: 100vh;
  display: flex;
  overflow: hidden;
}

/* Left visual side */
.login-visual {
  flex: 0 0 55%;
  position: relative;
  background-size: cover;
  background-position: center center;
  background-repeat: no-repeat;
  display: flex;
  align-items: center;
  justify-content: center;
}

.visual-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(44, 62, 80, 0.75) 0%, rgba(52, 73, 94, 0.65) 100%);
}

.visual-content {
  position: relative;
  z-index: 1;
  text-align: center;
  color: #fff;
  padding: 40px;
  max-width: 500px;
}

.visual-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 60px;
  letter-spacing: 2px;
}

.visual-logo i {
  font-size: 28px;
  color: #ffd700;
}

.visual-quote {
  margin-bottom: 60px;
}

.quote-mark {
  font-size: 64px;
  font-family: Georgia, serif;
  line-height: 0.5;
  opacity: 0.4;
  margin-bottom: 16px;
}

.quote-text {
  font-size: 26px;
  font-weight: 500;
  letter-spacing: 4px;
  line-height: 1.6;
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.3);
  margin: 0 0 16px 0;
}

.quote-author {
  font-size: 15px;
  opacity: 0.8;
  letter-spacing: 2px;
  margin: 0;
}

.visual-footer {
  font-size: 12px;
  opacity: 0.5;
  letter-spacing: 2px;
  text-transform: uppercase;
}

.quote-fade-enter-active, .quote-fade-leave-active {
  transition: all 0.5s ease;
}
.quote-fade-enter {
  opacity: 0;
  transform: translateY(15px);
}
.quote-fade-leave-to {
  opacity: 0;
  transform: translateY(-15px);
}

/* Right form side */
.login-form-side {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafbfc;
  overflow-y: auto;
}

.form-container {
  width: 380px;
  max-width: 90%;
  padding: 40px 0;
}

.form-title {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 8px 0;
}

.form-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0 0 32px 0;
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  letter-spacing: 4px;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

.switch-mode {
  text-align: center;
  font-size: 13px;
  color: #909399;
  margin: 16px 0;
}

.demo-tips {
  margin-top: 28px;
  padding: 16px;
  background: #f0f4ff;
  border-radius: 8px;
  font-size: 12px;
  color: #606266;
  line-height: 2;
}

.demo-tips p:first-child {
  font-weight: 600;
  color: #409eff;
}

.demo-tips code {
  background: #fff;
  padding: 2px 8px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  color: #e6a23c;
  font-weight: 600;
}

/* Deep styles for inputs */
/deep/ .el-input__inner {
  height: 44px;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s;
}

/deep/ .el-input__inner:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

/deep/ .el-input__icon {
  font-size: 18px;
}

/* Responsive: hide visual on small screens */
@media (max-width: 900px) {
  .login-visual {
    display: none;
  }
  .login-form-side {
    flex: 1;
  }
}
</style>

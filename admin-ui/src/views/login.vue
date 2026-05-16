<template>
  <div class="login-page">
    <!-- 左侧品牌区 -->
    <div class="brand-panel">
      <div class="brand-content">
        <div class="brand-logo">
          <span class="play-icon">▶</span>
        </div>
        <h1 class="brand-title">短剧平台</h1>
        <p class="brand-sub">管理后台系统</p>
        <div class="brand-tags">
          <span class="tag">内容管理</span>
          <span class="tag">用户运营</span>
          <span class="tag">商业变现</span>
        </div>
      </div>
      <div class="film-strip top" />
      <div class="film-strip bottom" />
    </div>

    <!-- 右侧登录区 -->
    <div class="form-panel">
      <div class="form-card">
        <div class="form-header">
          <h2>欢迎回来</h2>
          <p>请登录管理后台</p>
        </div>

        <el-form ref="loginRef" :model="loginForm" :rules="loginRules" class="login-form">
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              size="large"
              placeholder="账号"
              prefix-icon="User"
              autocomplete="off"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              size="large"
              placeholder="密码"
              prefix-icon="Lock"
              show-password
              autocomplete="off"
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <div class="form-opts">
            <el-checkbox v-model="loginForm.rememberMe">记住密码</el-checkbox>
          </div>
          <el-button
            :loading="loading"
            type="primary"
            size="large"
            class="login-btn"
            @click.prevent="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form>

        <div class="form-footer">短剧平台管理后台 &copy; {{ year }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import Cookies from 'js-cookie'
import { encrypt, decrypt } from '@/utils/jsencrypt'
import useUserStore from '@/store/modules/user'

const year = new Date().getFullYear()
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
const { proxy } = getCurrentInstance()

const loginForm = ref({
  username: 'admin',
  password: 'admin123',
  rememberMe: false,
})

const loginRules = {
  username: [{ required: true, trigger: 'blur', message: '请输入账号' }],
  password: [{ required: true, trigger: 'blur', message: '请输入密码' }],
}

const loading = ref(false)
const redirect = ref(undefined)

watch(route, (r) => { redirect.value = r.query?.redirect }, { immediate: true })

function handleLogin() {
  proxy.$refs.loginRef.validate(valid => {
    if (!valid) return
    loading.value = true
    if (loginForm.value.rememberMe) {
      Cookies.set('username', loginForm.value.username, { expires: 30 })
      Cookies.set('password', encrypt(loginForm.value.password), { expires: 30 })
      Cookies.set('rememberMe', true, { expires: 30 })
    } else {
      Cookies.remove('username')
      Cookies.remove('password')
      Cookies.remove('rememberMe')
    }
    userStore.login(loginForm.value).then(() => {
      const query = route.query
      const otherQuery = Object.keys(query).reduce((acc, k) => {
        if (k !== 'redirect') acc[k] = query[k]
        return acc
      }, {})
      router.push({ path: redirect.value || '/', query: otherQuery })
    }).catch(() => {
      loading.value = false
    })
  })
}

function getCookie() {
  const username = Cookies.get('username')
  const password = Cookies.get('password')
  const rememberMe = Cookies.get('rememberMe')
  loginForm.value = {
    username: username ?? loginForm.value.username,
    password: password ? decrypt(password) : loginForm.value.password,
    rememberMe: rememberMe === 'true',
  }
}

getCookie()
</script>

<style lang="scss" scoped>
.login-page {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

/* 左侧品牌区 */
.brand-panel {
  flex: 1;
  position: relative;
  background-image: url('@/assets/images/login-background.jpg');
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, rgba(15,10,40,.75) 0%, rgba(40,10,60,.6) 100%);
  }
}

.brand-content {
  position: relative;
  z-index: 1;
  text-align: center;
  color: #fff;
  padding: 0 40px;
}

.brand-logo {
  width: 80px;
  height: 80px;
  border-radius: 20px;
  background: linear-gradient(135deg, #ff6b35, #f7c59f);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
  box-shadow: 0 8px 32px rgba(255,107,53,.4);
}

.play-icon {
  font-size: 36px;
  color: #fff;
  margin-left: 6px;
}

.brand-title {
  font-size: 42px;
  font-weight: 800;
  letter-spacing: 4px;
  margin: 0 0 8px;
  text-shadow: 0 2px 12px rgba(0,0,0,.4);
}

.brand-sub {
  font-size: 16px;
  color: rgba(255,255,255,.7);
  letter-spacing: 2px;
  margin: 0 0 32px;
}

.brand-tags {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.tag {
  padding: 5px 14px;
  border-radius: 20px;
  font-size: 12px;
  border: 1px solid rgba(255,255,255,.3);
  background: rgba(255,255,255,.1);
  backdrop-filter: blur(4px);
  color: rgba(255,255,255,.9);
}

.film-strip {
  position: absolute;
  left: 0; right: 0;
  height: 32px;
  background:
    repeating-linear-gradient(
      90deg,
      transparent 0px,
      transparent 24px,
      rgba(0,0,0,.6) 24px,
      rgba(0,0,0,.6) 28px
    ),
    rgba(0,0,0,.5);

  &.top { top: 0; }
  &.bottom { bottom: 0; }
}

/* 右侧登录区 */
.form-panel {
  width: 440px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f6fa;
}

.form-card {
  width: 360px;
}

.form-header {
  margin-bottom: 32px;

  h2 {
    font-size: 26px;
    font-weight: 700;
    color: #1a1a2e;
    margin: 0 0 6px;
  }
  p {
    font-size: 14px;
    color: #909399;
    margin: 0;
  }
}

.login-form {
  :deep(.el-input__wrapper) {
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 1px 4px rgba(0,0,0,.06);
  }
  :deep(.el-form-item) {
    margin-bottom: 20px;
  }
}

.form-opts {
  margin-bottom: 24px;
}

.login-btn {
  width: 100%;
  height: 44px;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 2px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;

  &:hover {
    opacity: .9;
  }
}

.form-footer {
  margin-top: 32px;
  text-align: center;
  font-size: 12px;
  color: #c0c4cc;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .brand-panel { display: none; }
  .form-panel {
    width: 100%;
    background-image: url('@/assets/images/login-background.jpg');
    background-size: cover;

    .form-card {
      background: rgba(255,255,255,.95);
      border-radius: 16px;
      padding: 32px 28px;
      backdrop-filter: blur(12px);
    }
  }
}
</style>

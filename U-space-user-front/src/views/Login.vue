<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2 class="title">用户登录</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="学号" prop="studentNo">
          <el-input v-model="form.studentNo" placeholder="请输入学号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSubmit" :loading="loading" style="width: 100%">登 录</el-button>
        </el-form-item>
        <el-form-item>
          <el-link type="primary" @click="() => router.push('/register')">没有账号？去注册</el-link>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login as loginApi, getInfo } from '../api/auth'

const router = useRouter()
const loading = ref(false)

const form = reactive({
  studentNo: '',
  password: '',
})

const rules = {
  studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const formRef = ref()

const onSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await loginApi({ studentNo: form.studentNo, password: form.password })
      console.log('登录响应:', res)
      let tokenPayload = res.data
      if (tokenPayload && tokenPayload.data) {
        // 兼容嵌套的 R 结构 { code, data: { tokenName, tokenValue } }
        tokenPayload = tokenPayload.data
      }
      if (tokenPayload) {
        console.log('Token data:', tokenPayload)
        const { tokenName, tokenValue } = tokenPayload
        if (tokenName && tokenValue) {
          console.log('存储token:', tokenName, tokenValue)
          localStorage.setItem('tokenName', tokenName)
          localStorage.setItem('tokenValue', tokenValue)
          console.log('存储后检查:', localStorage.getItem('tokenName'), localStorage.getItem('tokenValue'))
        }
      }

      // 获取并保存用户信息
      try {
        const infoRes = await getInfo()
        console.log('getInfo response:', infoRes)
        if (infoRes.data) {
          localStorage.setItem('userInfo', JSON.stringify(infoRes.data))
        }
      } catch (e) {
        console.error('getInfo error:', e)
        // ignore error
      }

      ElMessage.success(res.msg || '登录成功')
      console.log('准备跳转首页', localStorage.getItem('tokenValue'))
      
      // 延迟跳转确保localStorage更新完成
      setTimeout(() => {
        console.log('延迟后检查token:', localStorage.getItem('tokenName'), localStorage.getItem('tokenValue'))
        // 使用直接跳转替代路由
        window.location.href = '/'
      }, 500)
    } catch (e) {
      // error handled globally
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: #f5f6f7;
}

.login-card {
  width: 90%;
  max-width: 360px;
}

.title {
  text-align: center;
  margin-bottom: 20px;
}
</style> 
<template>
  <div id="userRegisterPage">
    <div class="register-layout">
      <div class="register-form-wrap">
        <h2 class="title">喵绘网页 - 用户注册</h2>
        <div class="desc">不写一行代码，生成完整应用</div>
        <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
          <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入账号' }]">
            <a-input v-model:value="formState.userAccount" placeholder="请输入账号" />
          </a-form-item>
          <a-form-item
            name="userPassword"
            :rules="[
              { required: true, message: '请输入密码' },
              { min: 8, message: '密码不能小于 8 位' },
            ]"
          >
            <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码" />
          </a-form-item>
          <a-form-item
            name="checkPassword"
            :rules="[
              { required: true, message: '请确认密码' },
              { min: 8, message: '密码不能小于 8 位' },
              { validator: validateCheckPassword },
            ]"
          >
            <a-input-password v-model:value="formState.checkPassword" placeholder="请确认密码" />
          </a-form-item>
          <div class="tips">
            已有账号？
            <RouterLink to="/user/login">去登录</RouterLink>
          </div>
          <a-form-item>
            <a-button type="primary" html-type="submit" style="width: 100%">注册</a-button>
          </a-form-item>
        </a-form>
      </div>
      <div class="register-illustration">
        <img :src="petAdoption" alt="猫咪领养插画，欢迎加入喵绘网页" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { userRegister } from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import { reactive } from 'vue'
import petAdoption from '@/assets/undraw_pet-adoption_h7wv.svg'

const router = useRouter()

const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})

/**
 * 验证确认密码
 * @param rule
 * @param value
 * @param callback
 */
const validateCheckPassword = (rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (value && value !== formState.userPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: API.UserRegisterRequest) => {
  const res = await userRegister(values)
  // 注册成功，跳转到登录页面
  if (res.data.code === 0) {
    message.success('注册成功')
    router.push({
      path: '/user/login',
      replace: true,
    })
  } else {
    message.error('注册失败，' + res.data.message)
  }
}
</script>

<style scoped>
#userRegisterPage {
  background: rgba(255, 255, 255, 0.70);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.16);
  border-radius: 16px;
  max-width: 920px;
  padding: 32px 32px 28px;
  margin: 24px auto;
}

.register-layout {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 32px;
}

.register-form-wrap {
  flex: 1;
}

.register-illustration {
  flex: 1;
  display: flex;
  justify-content: center;
}

.register-illustration img {
  max-width: 320px;
  width: 100%;
  height: auto;
}

.title {
  text-align: center;
  margin-bottom: 16px;
}

.desc {
  text-align: center;
  color: #bbb;
  margin-bottom: 16px;
}

.tips {
  margin-bottom: 16px;
  color: #bbb;
  font-size: 13px;
  text-align: right;
}

@media (max-width: 768px) {
  #userRegisterPage {
    padding: 24px 16px;
  }

  .register-layout {
    flex-direction: column;
  }

  .register-illustration {
    display: none;
  }
}
</style>

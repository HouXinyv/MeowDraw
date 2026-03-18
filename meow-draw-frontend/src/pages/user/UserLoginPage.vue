<template>
  <div id="userLoginPage">
    <div class="login-layout">
      <div class="login-form-wrap">
        <h2 class="title">喵绘网页 - 用户登录</h2>
        <div class="desc">不写一行代码，生成完整应用</div>
        <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
          <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入账号' }]">
            <a-input v-model:value="formState.userAccount" placeholder="请输入账号" />
          </a-form-item>
          <a-form-item
            name="userPassword"
            :rules="[
              { required: true, message: '请输入密码' },
              { min: 8, message: '密码长度不能小于 8 位' },
            ]"
          >
            <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码" />
          </a-form-item>
          <div class="tips">
            没有账号
            <RouterLink to="/user/register">去注册</RouterLink>
          </div>
          <a-form-item>
            <a-button type="primary" html-type="submit" style="width: 100%">登录</a-button>
          </a-form-item>
        </a-form>
      </div>
      <div class="login-illustration">
        <img :src="friendsCats" alt="猫咪朋友插画，欢迎回到喵绘网页" />
      </div>
    </div>
  </div>
</template>
<script lang="ts" setup>
import { reactive } from 'vue'
import { userLogin } from '@/api/userController.ts'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import friendsCats from '@/assets/undraw_friends_xscy.svg'

const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})

const router = useRouter()
const loginUserStore = useLoginUserStore()

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: any) => {
  const res = await userLogin(values)
  // 登录成功，把登录态保存到全局状态中
  if (res.data.code === 0 && res.data.data) {
    await loginUserStore.fetchLoginUser()
    message.success('登录成功')
    router.push({
      path: '/',
      replace: true,
    })
  } else {
    message.error('登录失败，' + res.data.message)
  }
}
</script>

<style scoped>
#userLoginPage {
  background: rgba(255, 255, 255, 0.70);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.16);
  border-radius: 16px;
  max-width: 920px;
  padding: 32px 32px 28px;
  margin: 24px auto;
}

.login-layout {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 32px;
}

.login-form-wrap {
  flex: 1;
}

.login-illustration {
  flex: 1;
  display: flex;
  justify-content: center;
}

.login-illustration img {
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
  text-align: right;
  color: #bbb;
  font-size: 13px;
  margin-bottom: 16px;
}

@media (max-width: 768px) {
  #userLoginPage {
    padding: 24px 16px;
  }

  .login-layout {
    flex-direction: column;
  }

  .login-illustration {
    display: none;
  }
}
</style>

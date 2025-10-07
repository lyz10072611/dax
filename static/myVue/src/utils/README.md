# 状态码处理工具使用说明

## 概述

`statusCode.js` 工具提供了与后端 Result 类状态码一致的前端状态码处理功能，包括状态码常量、用户友好的错误消息、统一的响应处理等。

## 主要功能

### 1. 状态码常量

```javascript
import { STATUS_CODES } from '@/utils/statusCode'

// 使用状态码常量
if (response.code === STATUS_CODES.SUCCESS) {
  // 处理成功
}
```

### 2. 响应处理

```javascript
import { handleResponse, STATUS_CODES } from '@/utils/statusCode'

// 基本使用
const result = handleResponse(response, {
  showSuccess: true,    // 是否显示成功消息
  showError: true,      // 是否显示错误消息
  onSuccess: (data) => {
    // 成功回调
    console.log('操作成功', data)
  },
  onError: (code, message) => {
    // 错误回调
    console.log('操作失败', code, message)
  }
})
```

### 3. 状态码特定处理

```javascript
import { handleStatusCode, STATUS_CODES } from '@/utils/statusCode'

// 处理特定状态码
handleStatusCode(response.code, response, {
  onUnauthorized: (response) => {
    // 401 未授权处理
    window.location.href = '/login'
  },
  onForbidden: (response) => {
    // 403 禁止访问处理
    alert('权限不足')
  },
  onNotFound: (response) => {
    // 404 资源不存在处理
    alert('资源不存在')
  },
  onConflict: (response) => {
    // 409 冲突处理
    alert('数据冲突')
  },
  onTooManyRequests: (response) => {
    // 429 请求过多处理
    alert('操作过于频繁，请稍后再试')
  }
})
```

## 在 Vue 组件中的使用

### 1. 登录组件示例

```vue
<script setup>
import { handleResponse, STATUS_CODES } from '@/utils/statusCode'

const onSubmit = async () => {
  try {
    const { data } = await login(username.value, password.value)
    const result = handleResponse(data, {
      showSuccess: false,
      showError: true,
      onSuccess: (responseData) => {
        auth.setToken(responseData.token)
        router.push('/dashboard')
      },
      onError: (code, message) => {
        // 特殊处理某些状态码
        if (code === STATUS_CODES.TOO_MANY_REQUESTS) {
          alert('登录失败次数过多，请15分钟后重试')
        } else if (code === STATUS_CODES.NOT_FOUND) {
          alert('用户名不存在')
        } else if (code === STATUS_CODES.UNAUTHORIZED) {
          alert('密码错误')
        } else {
          alert(message)
        }
      }
    })
  } catch (error) {
    console.error('Login error:', error)
    alert('登录失败，请检查网络连接')
  }
}
</script>
```

### 2. 数据管理组件示例

```vue
<script setup>
import { handleResponse, STATUS_CODES } from '@/utils/statusCode'

const loadData = async () => {
  try {
    const { data } = await getData()
    const result = handleResponse(data, {
      showError: true,
      onSuccess: (responseData) => {
        dataList.value = responseData.items || []
      },
      onError: (code, message) => {
        if (code === STATUS_CODES.UNAUTHORIZED) {
          alert('请先登录')
        } else if (code === STATUS_CODES.FORBIDDEN) {
          alert('权限不足')
        } else {
          alert(message)
        }
      }
    })
  } catch (error) {
    console.error('加载数据失败:', error)
    alert('加载数据失败，请稍后重试')
  }
}

const saveData = async () => {
  try {
    const { data } = await saveData(formData.value)
    const result = handleResponse(data, {
      showSuccess: true,
      showError: true,
      onSuccess: () => {
        loadData() // 重新加载数据
      },
      onError: (code, message) => {
        if (code === STATUS_CODES.CONFLICT) {
          alert('数据冲突，请检查输入')
        } else if (code === STATUS_CODES.BAD_REQUEST) {
          alert('输入信息有误，请检查')
        } else {
          alert(message)
        }
      }
    })
  } catch (error) {
    console.error('保存失败:', error)
    alert('保存失败，请稍后重试')
  }
}
</script>
```

## 状态码映射表

| 状态码 | 常量 | 含义 | 用户友好消息 |
|--------|------|------|-------------|
| 200 | SUCCESS | 操作成功 | 操作成功 |
| 400 | BAD_REQUEST | 请求错误 | 请求参数有误，请检查输入 |
| 401 | UNAUTHORIZED | 未授权 | 请先登录 |
| 403 | FORBIDDEN | 禁止访问 | 权限不足，无法执行此操作 |
| 404 | NOT_FOUND | 资源不存在 | 请求的资源不存在 |
| 409 | CONFLICT | 冲突 | 数据冲突，请检查输入 |
| 429 | TOO_MANY_REQUESTS | 请求过多 | 操作过于频繁，请稍后再试 |
| 500 | INTERNAL_SERVER_ERROR | 内部服务器错误 | 服务器错误，请稍后重试 |

## 最佳实践

1. **统一使用状态码常量**：避免硬编码状态码数字
2. **提供用户友好的错误消息**：使用 `getUserFriendlyMessage` 获取友好的错误提示
3. **处理特定状态码**：根据不同状态码提供不同的处理逻辑
4. **添加加载状态**：在异步操作期间显示加载状态
5. **错误日志记录**：使用 `console.error` 记录详细错误信息
6. **网络错误处理**：在 catch 块中处理网络错误

## 注意事项

- 确保后端返回的响应格式与前端期望的格式一致
- 状态码处理应该与后端 Result 类的状态码保持一致
- 错误消息应该对用户友好，避免显示技术细节
- 敏感操作（如删除）应该添加确认提示

<template>
  <div class="chat-container">
    <el-container>
      <!-- 输出框 -->
      <el-main>
        <el-input
            v-model="outputText"
            style="width: 700px;"
            :rows="25"
            type="textarea"

        />
      </el-main>
      <!-- 输入框和按钮 -->
      <el-footer>
        <el-input
            v-model="textarea"
            style="width: 600px"
            :rows="4"
            type="textarea"
            placeholder="请输入问题"
        />
        <el-button type="primary" @click="sendRequest">发送</el-button>
      </el-footer>
    </el-container>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import axios from 'axios';

// 定义输入框和输出框的值
const textarea = ref('');
const outputText = ref('');

// 发送请求函数
async function sendRequest() {
  try {
    // 构造请求体
    const requestData = {
      type: 1,
      question: textarea.value
    };
    // 追加问题到输出框
    outputText.value += '我：' + textarea.value + '\n';

    // 发送 POST 请求
    const response = await axios.post('http://localhost:8080/chat', requestData, {
      headers: {
        'Content-Type': 'application/json;charset=UTF-8'
      }
    });

    // 追加回答到输出框
    outputText.value +=  response.data+ '\n\n';

    // 清空输入框
    textarea.value = '';
  } catch (error) {
    console.error('Error:', error);

    let errorMessage = '';

    if (axios.isAxiosError(error)) {
      if (error.response) {
        // 如果有响应，则根据响应状态码处理
        errorMessage = `HTTP ${error.response.status} ${error.response.statusText}\n`;
        errorMessage += `Response data: ${JSON.stringify(error.response.data)}\n`;
      } else if (error.request) {
        // 如果没有响应（可能是服务器没有响应）
        errorMessage = `No response received.\nRequest config: ${JSON.stringify(error.config)}\n`;
      } else {
        // 未知错误
        errorMessage = 'Unknown error occurred.\n';
      }
    } else {
      // 非 Axios 错误
      errorMessage = 'Non-Axios error occurred.\nError message: ' + error.message + '\n';
    }

    outputText.value += errorMessage + '\n';
    // 清空输入框
    textarea.value = '';
  }
}
</script>

<style scoped>

</style>


<template>
  <div class="chat-container">
    <!-- 聊天头部 -->
    <div class="chat-header">
      <button class="back-btn" @click="goBack">← 返回</button>
      <h2>💕 AI恋爱大师</h2>
      <div class="chat-id">会话ID: {{ chatId }}</div>
    </div>

    <!-- 聊天消息区域 -->
    <div class="chat-messages" ref="messagesContainer">
      <div v-for="message in messages" :key="message.id" :class="['message', message.type]">
        <div class="avatar" v-if="message.type === 'ai'">
          <img src="../assets/avatar-ai.svg" alt="AI" />
        </div>
        <div class="message-content">
          <span v-if="message.type === 'ai'" v-html="renderMarkdown(message.content)"></span>
          <span v-else>{{ message.content }}</span>
        </div>
        <div class="avatar" v-if="message.type === 'user'">
          <img src="../assets/avatar-user.svg" alt="用户" />
        </div>
      </div>
      <div v-if="isLoading" class="message ai">
        <div class="avatar">
          <img src="../assets/avatar-ai.svg" alt="AI" />
        </div>
        <div class="message-content">
          <span class="loading">AI正在思考中...</span>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="chat-input">
      <input
        v-model="inputMessage"
        @keyup.enter="sendMessage"
        :disabled="isLoading"
        placeholder="请输入您的问题..."
        class="input"
      />
      <button @click="sendMessage" :disabled="isLoading || !inputMessage.trim()" class="btn btn-primary">
        发送
      </button>
    </div>
  </div>
</template>

<script>
import { marked } from 'marked'
// 配置marked选项，确保所有Markdown格式都能正确解析
marked.setOptions({
  breaks: true,  // 允许换行
  gfm: true,     // 使用GitHub风格Markdown
  headerIds: false, // 禁用标题ID以避免潜在问题
  mangle: false  // 禁用mangle以避免某些特殊字符问题
})

export default {
  name: 'LoveChat',
  data() {
    return {
      chatId: '',
      messages: [],
      inputMessage: '',
      isLoading: false,
      messageIdCounter: 0
    }
  },
  mounted() {
    this.initChat()
  },
  methods: {
    initChat() {
      // 生成聊天室ID
      this.chatId = this.generateChatId()

      // 添加欢迎消息
      this.addMessage('ai', '你好！我是AI恋爱大师，很高兴为您提供情感咨询服务。请告诉我您遇到的问题吧～')
    },

    generateChatId() {
      return 'love_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
    },

    addMessage(type, content) {
      this.messages.push({
        id: ++this.messageIdCounter,
        type,
        content,
        timestamp: new Date()
      })
      this.$nextTick(() => {
        this.scrollToBottom()
      })
    },

    async sendMessage() {
      if (!this.inputMessage.trim() || this.isLoading) return

      const userMessage = this.inputMessage.trim()
      this.addMessage('user', userMessage)
      this.inputMessage = ''
      this.isLoading = true

      try {
        await this.sendToAI(userMessage)
      } catch (error) {
        console.error('发送消息失败:', error)
        this.addMessage('ai', '抱歉，网络连接出现问题，请稍后重试。')
      } finally {
        this.isLoading = false
      }
    },

    async sendToAI(message) {
      const apiUrl = `http://localhost:8123/api/ai/doChat/sse3?message=${encodeURIComponent(message)}&charId=${this.chatId}`

      const eventSource = new EventSource(apiUrl)
      let aiResponse = ''
      let aiMessageId = null

      eventSource.onmessage = (event) => {
        const chunk = event.data
        aiResponse += chunk

        if (aiMessageId) {
          // 更新现有消息
          const messageIndex = this.messages.findIndex(msg => msg.id === aiMessageId)
          if (messageIndex !== -1) {
            this.messages[messageIndex].content = aiResponse
          }
        } else {
          // 创建新消息
          aiMessageId = ++this.messageIdCounter
          this.messages.push({
            id: aiMessageId,
            type: 'ai',
            content: aiResponse,
            timestamp: new Date()
          })
        }

        this.$nextTick(() => {
          this.scrollToBottom()
        })
      }

      eventSource.onerror = (error) => {
        console.error('SSE连接错误:', error)
        eventSource.close()
        if (!aiResponse) {
          this.addMessage('ai', '抱歉，连接出现问题，请稍后重试。')
        }
      }

      eventSource.addEventListener('close', () => {
        eventSource.close()
      })
    },

    scrollToBottom() {
      const container = this.$refs.messagesContainer
      container.scrollTop = container.scrollHeight
    },

    goBack() {
      this.$router.push('/')
    },

    renderMarkdown(content) {
      if (!content) return ''
      try {
        return marked.parse(content)
      } catch (e) {
        console.error('Markdown渲染错误:', e)
        return content // 如果渲染出错，至少显示原始内容
      }
    },
  }
}
</script>

<style scoped>
.back-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.chat-id {
  font-size: 12px;
  opacity: 0.8;
}

.loading {
  font-style: italic;
  color: #666;
}

/* 添加Markdown渲染样式 */
:deep(.message-content) {
  overflow-wrap: break-word;
}

:deep(h1) {
  font-size: 1.8em;
  margin-top: 0.5em;
  margin-bottom: 0.5em;
}

:deep(h2) {
  font-size: 1.5em;
  margin-top: 0.5em;
  margin-bottom: 0.5em;
}

:deep(h3) {
  font-size: 1.3em;
  margin-top: 0.5em;
  margin-bottom: 0.5em;
}

:deep(ul), :deep(ol) {
  padding-left: 1.5em;
  margin-top: 0.5em;
  margin-bottom: 0.5em;
}

:deep(li) {
  margin-bottom: 0.3em;
}

:deep(pre), :deep(code) {
  background: #f0f0f0;
  border-radius: 3px;
  font-family: monospace;
  padding: 2px 4px;
}

:deep(pre) {
  padding: 0.5em;
  overflow-x: auto;
  margin: 0.5em 0;
}

:deep(pre code) {
  background: none;
  padding: 0;
}

:deep(p) {
  margin-bottom: 0.5em;
}

:deep(blockquote) {
  border-left: 4px solid #ddd;
  padding-left: 1em;
  color: #666;
  margin: 0.5em 0;
}

:deep(table) {
  border-collapse: collapse;
  margin: 0.5em 0;
  width: 100%;
}

:deep(th), :deep(td) {
  border: 1px solid #ddd;
  padding: 8px;
  text-align: left;
}

.message {
  margin-bottom: 15px;
  display: flex;
  align-items: flex-start;
}

.message.user {
  justify-content: flex-end;
}

.message.ai {
  justify-content: flex-start;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
  margin: 0 8px;
  flex-shrink: 0;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.message-content {
  max-width: calc(70% - 52px);
  padding: 12px 16px;
  border-radius: 18px;
  word-wrap: break-word;
}

.message.user .message-content {
  background: #007bff;
  color: white;
  border-bottom-right-radius: 4px;
  margin-right: 8px;
}

.message.ai .message-content {
  background: white;
  color: #333;
  border: 1px solid #e0e0e0;
  border-bottom-left-radius: 4px;
  margin-left: 8px;
}
</style>

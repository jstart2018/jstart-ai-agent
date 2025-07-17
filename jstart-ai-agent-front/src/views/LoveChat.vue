<template>
  <div class="chat-container love-theme">
    <!-- 聊天头部 -->
    <div class="chat-header">
      <button class="back-btn" @click="goBack">← 返回</button>
      <div class="header-content">
        <div class="love-icon">💖</div>
        <h2>AI恋爱大师</h2>
      </div>
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
          <div class="typing-indicator">
            <span></span>
            <span></span>
            <span></span>
          </div>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="chat-input">
      <input
        v-model="inputMessage"
        @keyup.enter="sendMessage"
        :disabled="isLoading"
        placeholder="和AI恋爱大师倾诉您的心事..."
        class="input"
      />
      <button @click="sendMessage" :disabled="isLoading || !inputMessage.trim()" class="btn btn-primary send-btn">
        <span>发送</span>
        <span class="send-icon">❤️</span>
      </button>
    </div>
  </div>
</template>

<script>
import { marked } from 'marked'
import { config } from '../config/index.ts'
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
      // 使用环境配置中的API地址，而不是硬编码的本地地址
      const apiUrl = `${config.aiLoveChatEndpoint}?message=${encodeURIComponent(message)}&charId=${this.chatId}`

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
.love-theme {
  --love-primary: #ffb6c1; /* 浅粉色 */
  --love-secondary: #87ceeb; /* 天蓝色 */
  --love-gradient: linear-gradient(135deg, #ffb6c1 0%, #87ceeb 100%);
  --love-message: #f8faff;
  --love-shadow: 0 4px 20px rgba(255, 182, 193, 0.3);
  --love-text: #9370db; /* 浅紫色文字 */
}

.chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f2f6ff;
  background-image:
    radial-gradient(#dbe6f6 1px, transparent 1px),
    radial-gradient(#dbe6f6 1px, transparent 1px);
  background-size: 20px 20px;
  background-position: 0 0, 10px 10px;
}

.chat-header {
  background: var(--love-gradient);
  color: white;
  padding: 15px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 10px rgba(106, 140, 175, 0.3);
}

.header-content {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-content h2 {
  color: var(--love-text);
  font-weight: 600;
  margin: 0;
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.3);
}

.love-icon {
  font-size: 24px;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
  }
}

.back-btn {
  background: rgba(255, 255, 255, 0.3);
  border: none;
  color: white;
  padding: 8px 12px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.5);
  transform: translateX(-3px);
}

.chat-id {
  font-size: 12px;
  opacity: 0.8;
}

/* 美化消息区域 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background-color: #fef6f6;
  background-image:
    radial-gradient(#ffe6e6 1px, transparent 1px),
    radial-gradient(#ffe6e6 1px, transparent 1px);
  background-size: 20px 20px;
  background-position: 0 0, 10px 10px;
}

.message {
  margin-bottom: 20px;
  display: flex;
  align-items: flex-start;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message.user {
  justify-content: flex-end;
}

.message.ai {
  justify-content: flex-start;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  margin: 0 10px;
  flex-shrink: 0;
  border: 2px solid white;
  box-shadow: var(--love-shadow);
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.message-content {
  max-width: calc(70% - 60px);
  padding: 15px 20px;
  border-radius: 20px;
  word-wrap: break-word;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  font-family: "Microsoft YaHei", "微软雅黑", sans-serif;
  font-size: 16px;
}

.message.user .message-content {
  background: var(--love-primary);
  color: white;
  border-bottom-right-radius: 5px;
  margin-right: 8px;
}

.message.ai .message-content {
  background: var(--love-message);
  color: #333;
  border: 1px solid #dbe6f6;
  border-bottom-left-radius: 5px;
  margin-left: 8px;
}

/* 输入区域美化 */
.chat-input {
  padding: 20px;
  background: white;
  border-top: 1px solid #ffdedb;
  display: flex;
  gap: 10px;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
}

.input {
  flex: 1;
  padding: 15px;
  border: 1px solid #dbe6f6;
  border-radius: 30px;
  font-size: 16px;
  outline: none;
  transition: all 0.3s ease;
  background: #f8faff;
}

.input:focus {
  border-color: var(--love-primary);
  box-shadow: 0 0 0 3px rgba(106, 140, 175, 0.2);
}

.send-btn {
  background: var(--love-gradient);
  border: none;
  color: white;
  padding: 0 25px;
  border-radius: 30px;
  cursor: pointer;
  font-size: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
}

.send-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(255, 107, 107, 0.4);
}

.send-btn:disabled {
  background: #ffcdcd;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.send-icon {
  font-size: 18px;
}

/* 输入提示动画 */
.typing-indicator {
  display: flex;
  align-items: center;
  gap: 5px;
}

.typing-indicator span {
  display: inline-block;
  width: 8px;
  height: 8px;
  background-color: var(--love-secondary);
  border-radius: 50%;
  animation: typingBounce 1.4s infinite ease-in-out both;
}

.typing-indicator span:nth-child(1) {
  animation-delay: -0.32s;
}

.typing-indicator span:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes typingBounce {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

/* Markdown内容样式 */
:deep(.message-content) {
  overflow-wrap: break-word;
  line-height: 1.5;
}

:deep(h1) {
  font-size: 1.6em;
  margin-top: 0.5em;
  margin-bottom: 0.5em;
  color: #6a8caf;
}

:deep(h2) {
  font-size: 1.4em;
  margin-top: 0.5em;
  margin-bottom: 0.5em;
  color: #6a8caf;
}

:deep(h3) {
  font-size: 1.2em;
  margin-top: 0.5em;
  margin-bottom: 0.5em;
  color: #6a8caf;
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
  background: #f0f5ff;
  border-radius: 3px;
  font-family: monospace;
  padding: 2px 4px;
  border: 1px solid #dbe6f6;
}

:deep(pre) {
  padding: 0.5em;
  overflow-x: auto;
  margin: 0.5em 0;
}

:deep(pre code) {
  background: none;
  padding: 0;
  border: none;
}

:deep(p) {
  margin-bottom: 0.5em;
}

:deep(blockquote) {
  border-left: 4px solid #98b4d4;
  padding-left: 1em;
  color: #666;
  margin: 0.5em 0;
  background: #f8faff;
}

:deep(a) {
  color: #6a8caf;
  text-decoration: none;
}

:deep(a:hover) {
  text-decoration: underline;
}
</style>

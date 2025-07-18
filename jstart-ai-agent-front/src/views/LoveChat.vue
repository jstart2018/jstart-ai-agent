<template>
  <div class="chat-container love-theme">
    <!-- 聊天头部 -->
    <div class="chat-header">
      <button class="back-btn" @click="goBack">← 返回</button>
      <div class="header-content">
        <div class="love-icon">💫</div>
        <h2>AI情感助手</h2>
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
        <div class="message-content thinking-animation">
          <div class="thinking-indicator">
            <span>正在思考</span>
            <span class="thinking-dots">
              <span>.</span>
              <span>.</span>
              <span>.</span>
            </span>
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
        placeholder="和AI情感助手分享您的心事..."
        class="input"
        ref="messageInput"
      />
      <button @click="sendMessage" :disabled="isLoading || !inputMessage.trim()" class="btn btn-primary send-btn">
        <span>发送</span>
        <span class="send-icon">💌</span>
      </button>
    </div>
  </div>
</template>

<script>
import { config } from '../config/index.ts'
import { marked } from 'marked'

// 配置marked选项，优化markdown渲染
marked.setOptions({
  breaks: true,      // 允许换行
  gfm: true,         // 使用GitHub风格Markdown
  headerIds: false,  // 禁用标题ID
  mangle: false,     // 禁用mangle以避免某些特殊字符问题
  sanitize: false,   // 禁用HTML清理，允许表情符号等特殊字符
  smartypants: false // 禁用智能标点，避免影响表情符号
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
      this.addMessage('ai', '你好！我是AI情感助手，很高兴为您提供情感咨询服务。无论是爱情、友情还是亲情问题，我都会用心倾听并为您提供建议。请告诉我您遇到的问题吧～')
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
        // 消息发送完成后，重新聚焦输入框
        this.$nextTick(() => {
          if (this.$refs.messageInput) {
            this.$refs.messageInput.focus()
          }
        })
      }
    },

    async sendToAI(message) {
      // 使用环境配置中的API地址，而不是硬编码的本地地址
      const apiUrl = `${config.aiLoveChatEndpoint}?message=${encodeURIComponent(message)}&charId=${this.chatId}`

      // 立即添加一条"正在思考"的消息
      const thinkingMessageId = ++this.messageIdCounter
      this.messages.push({
        id: thinkingMessageId,
        type: 'ai',
        content: '<div class="thinking-indicator"><span>正在思考</span><span class="thinking-dots"><span>.</span><span>.</span><span>.</span></span></div>',
        timestamp: new Date(),
        isThinking: true // 标记这是一个思考状态的消息
      })

      // 强制滚动到底部，确保用户看到思考动画
      this.$nextTick(() => {
        this.scrollToBottom()
      })

      const eventSource = new EventSource(apiUrl)
      let aiResponse = ''
      let isFirstChunk = true

      eventSource.onmessage = (event) => {
        let chunk = event.data

        // 处理可能的对象数据
        try {
          // 如果chunk是JSON字符串，尝试解析
          if (typeof chunk === 'string' && (chunk.startsWith('{') || chunk.startsWith('['))) {
            const parsedData = JSON.parse(chunk)
            // 如果是对象，提取其中的文本内容
            if (typeof parsedData === 'object' && parsedData !== null) {
              if (parsedData.content || parsedData.text || parsedData.message) {
                chunk = parsedData.content || parsedData.text || parsedData.message
              } else if (parsedData.data) {
                chunk = parsedData.data
              } else {
                // 如果对象没���明显的文本字段，尝试将整个对象转为字符串
                try {
                  chunk = JSON.stringify(parsedData)
                } catch (jsonError) {
                  console.warn('无法序列化对象:', parsedData)
                  chunk = '[解析错误]'
                }
              }
            }
          }
        } catch (e) {
          // 如果不是JSON或解析出错，尝试确保chunk是字符串
          console.warn('JSON解析错误:', e)
        }

        // 最终确保chunk绝对是字符串类型
        if (typeof chunk !== 'string') {
          try {
            // 对于对象类型，尝试序列化
            if (chunk && typeof chunk === 'object') {
              if (chunk.toString && typeof chunk.toString === 'function' && chunk.toString() !== '[object Object]') {
                chunk = chunk.toString()
              } else {
                // 如果toString返回[object Object]，尝试使用JSON序列化
                chunk = JSON.stringify(chunk)
              }
            } else {
              // 对于其他非字符串类型，直接转换为字符串
              chunk = String(chunk)
            }
          } catch (e) {
            console.warn('转换为字符串失败:', e)
            chunk = '[数据类型错误]'
          }
        }

        // 如果是第一个响应块，添加"思考完成"提示和分隔符
        if (isFirstChunk) {
          isFirstChunk = false
          aiResponse = '<div class="thinking-complete"><span class="thinking-complete-icon">✨</span><span>思考完成</span></div><div class="response-separator"></div>' + chunk
        } else {
          aiResponse += chunk
        }

        // 找到之前的"思考中"消息，并更新它
        const messageIndex = this.messages.findIndex(msg => msg.id === thinkingMessageId)
        if (messageIndex !== -1) {
          // 直接更新消息内容，确保Vue检测到变化
          this.messages[messageIndex].content = aiResponse
          this.messages[messageIndex].timestamp = new Date()
          this.messages[messageIndex].isThinking = false // 移除思考状态标记
          // 强制触发重新渲染
          this.$forceUpdate()
        }

        // 强制重新渲染并滚动到底部
        this.$nextTick(() => {
          this.scrollToBottom()
        })
      }

      eventSource.onerror = (error) => {
        console.error('SSE连接错误:', error)
        eventSource.close()

        // 查找"正在思考"消息
        const messageIndex = this.messages.findIndex(msg => msg.id === thinkingMessageId)
        if (messageIndex !== -1) {
          if (!aiResponse) {
            // 如果没有收到任何响应，替换为错误消息
            this.messages[messageIndex].content = '抱歉，连接出现问题，请稍后重试。'
            this.messages[messageIndex].isThinking = false
            // 强制触发重新渲染
            this.$forceUpdate()
          }
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

    renderMarkdown(markdown) {
      if (!markdown) return ''

      try {
        // 预处理markdown内容：修复标题格式(在#后添加空格)
        let processedContent = markdown
          // 匹配以一个或多个#开头，后面紧跟非空格字符的情况
          .replace(/^(#+)([^#\s])/gm, '$1 $2')  // 在行首的#后添加空格
          .replace(/\n(#+)([^#\s])/gm, '\n$1 $2')  // 在换行后的#后添加空格

        // 使用marked库渲染Markdown为HTML
        return marked(processedContent)
      } catch (error) {
        console.error('Markdown渲染错误:', error)
        // 出错时至少返回原始文本，确保换行符正确显示
        return markdown.replace(/\n/g, '<br>')
      }
    }
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

/* 正在思考的动画样式 */
.thinking-animation {
  background: var(--love-message);
  color: #555;
  border: 1px solid #dbe6f6;
}

:deep(.thinking-indicator) {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  position: relative;
}

:deep(.thinking-indicator span:first-child) {
  color: var(--love-text);
  font-weight: 500;
  letter-spacing: 0.5px;
  background-image: linear-gradient(45deg, #9370db, #ffb6c1);
  background-size: 100%;
  background-repeat: repeat;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: pulse-text 2s infinite ease-in-out;
}

@keyframes pulse-text {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

:deep(.thinking-dots) {
  display: inline-flex;
  align-items: center;
}

:deep(.thinking-dots span) {
  font-size: 22px;
  font-weight: bold;
  color: var(--love-primary);
  animation: thinkingDots 1.4s infinite;
  text-shadow: 0 0 3px rgba(255, 182, 193, 0.5);
  line-height: 0.7;
}

:deep(.thinking-dots span:nth-child(1)) {
  animation-delay: 0s;
}

:deep(.thinking-dots span:nth-child(2)) {
  animation-delay: 0.2s;
}

:deep(.thinking-dots span:nth-child(3)) {
  animation-delay: 0.4s;
}

@keyframes thinkingDots {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  30% {
    transform: translateY(-6px);
    opacity: 1;
  }
}

/* 思考完成提���样式 */
:deep(.thinking-complete) {
  color: var(--love-text);
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

:deep(.thinking-complete-icon) {
  animation: sparkle 1.5s infinite;
  display: inline-block;
}

@keyframes sparkle {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.2); opacity: 0.8; }
}

/* 分隔线样式 */
:deep(.response-separator) {
  margin: 12px 0;
  height: 1px;
  background: linear-gradient(to right, transparent, var(--love-primary), transparent);
  opacity: 0.6;
  position: relative;
  overflow: visible;
}

:deep(.response-separator::after) {
  content: '💭';
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  background-color: var(--love-message);
  padding: 0 10px;
  font-size: 14px;
  line-height: 1;
}

/* Markdown内容样式 */
:deep(.message-content) {
  overflow-wrap: break-word;
  line-height: 1.5;
}

/* 自定义markdown样式类 */
:deep(.markdown-heading) {
  font-weight: 600;
  margin: 0.8em 0 0.5em 0;
  line-height: 1.3;
}

:deep(.markdown-h1) {
  font-size: 1.6em;
  color: #6a8caf;
  border-bottom: 2px solid #dbe6f6;
  padding-bottom: 0.3em;
}

:deep(.markdown-h2) {
  font-size: 1.4em;
  color: #6a8caf;
}

:deep(.markdown-h3) {
  font-size: 1.2em;
  color: #6a8caf;
}

:deep(.markdown-h4) {
  font-size: 1.1em;
  color: #6a8caf;
}

:deep(.markdown-paragraph) {
  margin-bottom: 0.8em;
  text-align: justify;
  text-indent: 0;
}

:deep(.markdown-list) {
  padding-left: 2em;
  margin: 0.8em 0;
}

:deep(.markdown-listitem) {
  margin-bottom: 0.4em;
  padding-left: 0.3em;
}

:deep(.markdown-link) {
  color: #6a8caf;
  text-decoration: none;
  border-bottom: 1px solid transparent;
  transition: border-color 0.3s ease;
}

:deep(.markdown-link:hover) {
  border-bottom-color: #6a8caf;
}

/* 原有样式保持兼容 */
:deep(h1), :deep(h2), :deep(h3), :deep(h4), :deep(h5), :deep(h6) {
  margin-top: 0.8em;
  margin-bottom: 0.5em;
  color: #6a8caf;
}

/* 标题下的段落样式增强 */
:deep(h1) + :deep(p),
:deep(h2) + :deep(p),
:deep(h3) + :deep(p),
:deep(h4) + :deep(p),
:deep(h5) + :deep(p),
:deep(h6) + :deep(p) {
  letter-spacing: 0.02em; /* 增加字间距 */
  line-height: 1.7; /* 增加行间距 */
  margin-bottom: 0.7em; /* 增加段落底部间距 */
  padding-left: 0.3em; /* 轻微缩进 */
}

:deep(h1) {
  font-size: 1.6em;
  color: #6a8caf;
}

:deep(h2) {
  font-size: 1.4em;
  color: #6a8caf;
}

:deep(h3) {
  font-size: 1.2em;
  color: #6a8caf;
}
</style>

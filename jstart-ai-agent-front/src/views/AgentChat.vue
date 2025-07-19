<template>
  <div class="chat-container agent-theme">
    <!-- 聊天头部 -->
    <div class="chat-header">
      <button class="back-btn" @click="goBack">← 返回</button>
      <div class="header-content">
        <div class="agent-icon">🤖</div>
        <h2>AI超级智能体</h2>
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
        placeholder="请输入您的问题，AI超级智能体为您解答..."
        class="input"
      />
      <button @click="sendMessage" :disabled="isLoading || !inputMessage.trim()" class="btn btn-primary send-btn">
        <span>发送</span>
        <span class="send-icon">🚀</span>
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
  mangle: false  // 禁用mangle以避免某些���殊字符问题
})

export default {
  name: 'AgentChat',
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
      this.addMessage('ai', '你好！我是AI超级智能体，我可以帮助您解答各种问题、协助您完成任务。请告诉我您需要什么帮助？\n' +
          '(提示词示例：帮我整理一下广州适合团建的地方，并抓取部分图片)')
    },

    generateChatId() {
      return 'agent_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
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
      // 使用环境配置中的API地址
      const apiUrl = `${config.aiAgentChatEndpoint}?message=${encodeURIComponent(message)}`
      const eventSource = new EventSource(apiUrl)
      let aiResponse = ''
      let aiMessageId = null

      eventSource.onmessage = (event) => {
        let chunk = event.data

        // 处理可能的对象数据
        try {
          // 如果chunk是JSON字符串，尝试解析
          if (chunk.startsWith('{') || chunk.startsWith('[')) {
            const parsedData = JSON.parse(chunk)
            // 如果是对象，提取其中的文本内容
            if (typeof parsedData === 'object' && parsedData !== null) {
              if (parsedData.content || parsedData.text || parsedData.message) {
                chunk = parsedData.content || parsedData.text || parsedData.message
              } else if (parsedData.data) {
                chunk = parsedData.data
              } else {
                // 如果对象没有明显的文本字段，跳过这个chunk
                console.warn('跳过无效数据:', parsedData)
                return
              }
            }
          }
        } catch (e) {
          // 如果不是JSON，直接使用原始数据
          // 但要确保chunk是字符串
          if (typeof chunk !== 'string') {
            console.warn('跳过非字符串数据:', chunk)
            return
          }
        }

        // 确保chunk是字符串类型
        if (typeof chunk !== 'string') {
          console.warn('跳过非字符串chunk:', chunk)
          return
        }

        aiResponse += chunk

        if (aiMessageId) {
          const messageIndex = this.messages.findIndex(msg => msg.id === aiMessageId)
          if (messageIndex !== -1) {
            // 直接更新消息内容并强制重新渲染
            this.messages[messageIndex].content = aiResponse
            this.messages[messageIndex].timestamp = new Date()
            // 强制触发重新渲染
            this.$forceUpdate()
          }
        } else {
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
        // 使用独特标记和ID为每个步骤添加唯一标识
        let stepCount = 0;

        // 处理步骤标记和链接
        let processedContent = content
          // 处���常见步骤格式，转换为特殊标记
          .replace(/步骤\s*(\d+)[:：]/g, (match, num) => {
            stepCount++;
            return `<div class="step-title">步骤 ${num}:</div>`;
          })
          .replace(/Step\s*(\d+)[:：]?/gi, (match, num) => {
            stepCount++;
            return `<div class="step-title">步骤 ${num}:</div>`;
          })
          .replace(/第(\d+)步[:：]?/g, (match, num) => {
            stepCount++;
            return `<div class="step-title">步骤 ${num}:</div>`;
          })
          // 处理可能是步骤开头的编号形式（避免过度匹配普通列表）
          .replace(/(\d+)\.\s+([A-Z][^\.]+)/g, (match, num, text) => {
            // 只处理以大写字母开头且内容较长的情况
            if (text.length > 10) {
              stepCount++;
              return `<div class="step-title">步骤 ${num}:</div>${text}`;
            }
            return match; // 否则保持原样
          });

        // 处理链接，确保不会干扰已处理的步骤标记
        processedContent = processedContent
          // 处理JSON格式的链接
          .replace(/\"link\":\"(https?:\/\/[^\"]+)\"/g, (match, url) => {
            return `"link":"<a href="${url}" target="_blank" class="external-link">${url}</a>"`;
          })
          // 处理普通URL
          .replace(/(?<!\(|="|>)(https?:\/\/[^\s<"]+)(?!\))/g, (url) => {
            return `<a href="${url}" target="_blank" class="external-link">${url}</a>`;
          });

        // 处理数字列表格式
        processedContent = processedContent
          .replace(/(?<!\<div class="step-title">步骤 )(\d+)\.(?!\:)/g, '<span class="list-number">$1.</span>');

        // 配置marked选项，确保链接可点击
        const renderer = new marked.Renderer();
        renderer.link = (href, title, text) => {
          return `<a href="${href}" target="_blank" title="${title || ''}" class="markdown-link">${text}</a>`;
        };

        marked.setOptions({
          breaks: true,
          gfm: true,
          headerIds: false,
          mangle: false,
          renderer: renderer
        });

        // 将处理后的内容转换为HTML
        let markedContent = marked.parse(processedContent);

        // 给每个步骤内容添加缩进和样式
        markedContent = markedContent.replace(
          /(<div class="step-title">.*?<\/div>)([\s\S]*?)(?=<div class="step-title">|$)/g,
          '$1<div class="step-content">$2</div>'
        );

        return markedContent;
      } catch (e) {
        console.error('Markdown渲染错误:', e);
        return content; // 如果渲染出错，至少显示原始内容
      }
    },
  }
}
</script>

<style scoped>
.chat-header {
  background: #28a745;
}

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
  font-size: 1.6em;
  margin-top: 0.5em;
  margin-bottom: 0.5em;
}

:deep(h2) {
  font-size: 1.4em;
  margin-top: 0.5em;
  margin-bottom: 0.5em;
}

:deep(h3) {
  font-size: 1.2em;
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
  font-size: 14px; /* 添加基础字体大小设置 */
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

/* 新增步骤样式 */
 :deep(.step-title) {
  font-weight: bold;
  font-size: 1.2em;
  margin: 1.2em 0 0.6em;
  color: #ffffff;
  background-color: #28a745;
  padding: 10px 15px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(40, 167, 69, 0.2);
  display: inline-block;
  position: relative;
}

:deep(.step-title)::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 20px;
  width: 0;
  height: 0;
  border-left: 8px solid transparent;
  border-right: 8px solid transparent;
  border-top: 8px solid #28a745;
}

:deep(.step-content) {
  margin-left: 1.5em;
  margin-bottom: 1.2em;
  padding: 0.8em 1em;
  border-left: 3px solid #28a745;
  background-color: rgba(40, 167, 69, 0.05);
  border-radius: 0 8px 8px 0;
}

.list-number {
  font-weight: bold;
  color: #007bff;
}

.markdown-link {
  color: #007bff;
  text-decoration: underline;
}

.external-link {
  color: #007bff;
  text-decoration: none;
}

/* 新增整体聊天界面样式 */
.agent-theme {
  font-family: 'Helvetica Neue', Arial, sans-serif;
  background-color: #f8f9fa;
  color: #333;
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 0;
  margin: 0;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 20px;
  background-color: #28a745;
  color: white;
  border-bottom: 1px solid #ddd;
}

.header-content {
  display: flex;
  align-items: center;
}

.agent-icon {
  font-size: 1.5em;
  margin-right: 10px;
}

.chat-id {
  font-size: 0.9em;
  opacity: 0.8;
}

.chat-messages {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background-color: #fff;
}

.chat-input {
  display: flex;
  padding: 10px 20px;
  background-color: #f1f1f1;
  border-top: 1px solid #ddd;
}

.input {
  flex: 1;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 18px;
  margin-right: 10px;
  font-size: 14px;
}

.btn-primary {
  background-color: #007bff;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
}

.btn-primary:hover {
  background-color: #0056b3;
}

.send-btn {
  position: relative;
}

.send-icon {
  margin-left: 8px;
  font-size: 1.2em;
  display: inline-block;
  transition: transform 0.2s;
}

.send-btn:hover .send-icon {
  transform: translateY(-2px);
}

/* 新增打字指示器样式 */
.typing-indicator {
  display: flex;
  align-items: center;
}

.typing-indicator span {
  display: inline-block;
  width: 8px;
  height: 8px;
  margin-right: 4px;
  background-color: #28a745;
  border-radius: 50%;
  animation: typing 0.7s infinite alternate;
}

@keyframes typing {
  0% {
    transform: translateY(0);
  }
  100% {
    transform: translateY(-4px);
  }
}
</style>

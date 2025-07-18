<template>
  <div class="home-container">
    <!-- 增强的背景动效 -->
    <div class="floating-hearts">
      <div v-for="n in 20" :key="n" class="heart" :style="randomHeartStyle()"></div>
    </div>
    <div class="floating-bubbles">
      <div v-for="n in 15" :key="`bubble-${n}`" class="bubble" :style="randomBubbleStyle()"></div>
    </div>
    <div class="gradient-bg"></div>

    <div class="hero-section">
      <div class="logo-container">
        <div class="logo-icon">💫</div>
      </div>
      <h1 class="title">心灵伙伴</h1>
      <p class="subtitle">让AI为您的生活与情感提供专业指导</p>
    </div>

    <div class="apps-grid">
      <div class="app-card love-card" @click="navigateToLoveChat">
        <div class="card-header">
          <div class="app-icon">💫</div>
          <h3>AI情感助手</h3>
        </div>
        <p>专业的情感咨询服务，涵盖爱情、友情和亲情指导</p>
        <div class="features">
          <div class="feature"><span>💝</span> 情感分析</div>
          <div class="feature"><span>📚</span> 知识库</div>
          <div class="feature"><span>🔒</span> 隐私保护</div>
        </div>
        <button class="btn btn-love">开始情感咨询</button>
      </div>

      <div class="app-card agent-card" @click="navigateToAgentChat">
        <div class="card-header">
          <div class="app-icon">🤖</div>
          <h3>AI超级智能体</h3>
        </div>
        <p>全能AI助手，完成你提交的各种任务</p>
        <div class="features">
          <div class="feature"><span>🧠</span> 自主思考</div>
          <div class="feature"><span>📚</span> 联网搜索</div>
          <div class="feature"><span>🛠️</span> 多工具调用</div>
        </div>
        <button class="btn btn-agent">开始对话</button>
      </div>
    </div>

    <footer class="footer">
      <p>© 2025 心灵伙伴 - 让AI助你解锁情感密码</p>
    </footer>
  </div>
</template>

<script>
export default {
  name: 'Home',
  methods: {
    navigateToLoveChat() {
      this.$router.push('/love-chat')
    },
    navigateToAgentChat() {
      this.$router.push('/agent-chat')
    },
    randomHeartStyle() {
      const size = Math.random() * 20 + 10
      const left = Math.random() * 100
      const animationDuration = Math.random() * 15 + 10
      const delay = Math.random() * 5
      const opacity = Math.random() * 0.4 + 0.1

      return {
        width: `${size}px`,
        height: `${size}px`,
        left: `${left}%`,
        animationDuration: `${animationDuration}s`,
        animationDelay: `${delay}s`,
        opacity: opacity
      }
    },
    randomBubbleStyle() {
      const size = Math.random() * 60 + 20
      const left = Math.random() * 100
      const animationDuration = Math.random() * 20 + 15
      const delay = Math.random() * 10
      const opacity = Math.random() * 0.15 + 0.05

      return {
        width: `${size}px`,
        height: `${size}px`,
        left: `${left}%`,
        animationDuration: `${animationDuration}s`,
        animationDelay: `${delay}s`,
        opacity: opacity
      }
    }
  }
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  overflow: hidden;
}

.gradient-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(-45deg, #36D1DC, #5B86E5, #FF9A9E);
  background-size: 600% 600%;
  animation: gradientFlow 30s ease infinite;
  z-index: -1;
}

@keyframes gradientFlow {
  0% {
    background-position: 0% 50%;
  }
  25% {
    background-position: 50% 100%;
  }
  50% {
    background-position: 100% 50%;
  }
  75% {
    background-position: 50% 0%;
  }
  100% {
    background-position: 0% 50%;
  }
}

/* 漂浮爱心背景 */
.floating-hearts {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  z-index: 1;
  pointer-events: none;
}

.heart {
  position: absolute;
  bottom: -10%;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%23ffffff'%3E%3Cpath d='M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  animation: floatUp linear infinite;
}

/* 新增的气泡背景 */
.floating-bubbles {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  z-index: 1;
  pointer-events: none;
}

.bubble {
  position: absolute;
  bottom: -10%;
  background: radial-gradient(circle at 30% 30%, rgba(255, 255, 255, 0.8), rgba(255, 255, 255, 0.2));
  border-radius: 50%;
  animation: bubbleRise linear infinite;
  box-shadow: 0 0 10px rgba(255, 255, 255, 0.3), inset 0 0 15px rgba(255, 255, 255, 0.5);
}

@keyframes floatUp {
  0% {
    transform: translateY(0) rotate(0);
    opacity: 0;
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 0.8;
  }
  100% {
    transform: translateY(-1000px) rotate(720deg);
    opacity: 0;
  }
}

@keyframes bubbleRise {
  0% {
    transform: translateY(0) translateX(0) scale(1);
    opacity: 0;
  }
  10% {
    opacity: 0.2;
  }
  50% {
    transform: translateY(-500px) translateX(30px) scale(1.1);
  }
  90% {
    opacity: 0.15;
  }
  100% {
    transform: translateY(-1000px) translateX(-20px) scale(0.9);
    opacity: 0;
  }
}

/* 标题区样式 */
.hero-section {
  text-align: center;
  margin-bottom: 50px;
  color: white;
  position: relative;
  z-index: 2;
}

.logo-container {
  margin-bottom: 20px;
}

.logo-icon {
  width: 100px;
  height: 100px;
  margin: 0 auto;
  animation: pulse 2s infinite;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 4rem; /* 添加合适的字体大小 */
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
}

@keyframes pulse {
  0% {
    transform: scale(1);
    box-shadow: 0 10px 30px rgba(255, 105, 180, 0.4);
  }
  50% {
    transform: scale(1.05);
    box-shadow: 0 15px 40px rgba(255, 105, 180, 0.6);
  }
  100% {
    transform: scale(1);
    box-shadow: 0 10px 30px rgba(255, 105, 180, 0.4);
  }
}

.title {
  font-size: 3.5rem;
  font-weight: bold;
  margin-bottom: 15px;
  text-shadow: 2px 2px 8px rgba(0, 0, 0, 0.3);
  background: linear-gradient(to right, #fff, #e2e8ff);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.subtitle {
  font-size: 1.3rem;
  opacity: 0.9;
  max-width: 600px;
  margin: 0 auto;
  line-height: 1.6;
}

/* 应用卡片样式 */
.apps-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 30px;
  max-width: 900px;
  width: 100%;
  position: relative;
  z-index: 2;
}

.app-card {
  border-radius: 24px;
  padding: 40px 30px;
  text-align: center;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  position: relative;
  overflow: hidden;
}

.app-card::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.1);
  z-index: -1;
  transform: translateY(100%);
  transition: transform 0.4s ease;
}

.app-card:hover {
  transform: translateY(-10px);
}

.app-card:hover::before {
  transform: translateY(0);
}

.love-card {
  background: linear-gradient(135deg, rgba(255, 182, 193, 0.5) 0%, rgba(255, 218, 224, 0.7) 100%);
  box-shadow: 0 15px 35px rgba(255, 160, 180, 0.25);
  border: 1px solid rgba(255, 182, 193, 0.4);
}

.agent-card {
  background: linear-gradient(135deg, rgba(100, 200, 255, 0.5) 0%, rgba(138, 180, 250, 0.6) 100%);
  box-shadow: 0 15px 35px rgba(124, 170, 235, 0.25);
  border: 1px solid rgba(124, 180, 235, 0.3);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 15px;
  gap: 15px;
}

.app-icon {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2.5rem; /* 略微减小emoji的字体大小 */
  background: rgba(255, 192, 203, 0.1);
  border-radius: 50%;
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
  margin-bottom: 0; /* 移除底部间距 */
}

.app-card h3 {
  font-size: 1.8rem;
  color: #333;
  position: relative;
  display: inline-block;
  margin: 0; /* 移除默认边距 */
}

.app-card h3::after {
  content: "";
  position: absolute;
  bottom: -5px;
  left: 50%;
  transform: translateX(-50%);
  width: 50px;
  height: 3px;
  border-radius: 3px;
  transition: width 0.3s ease;
}

.love-card h3::after {
  background: linear-gradient(to right, #6a8caf, #98b4d4);
}

.agent-card h3::after {
  background: linear-gradient(to right, #7c6da5, #a594d0);
}

.app-card:hover h3::after {
  width: 80px;
}

.app-card p {
  color: #666;
  margin-bottom: 25px;
  line-height: 1.6;
  padding: 0 10px;
}

/* 特性列表 */
.features {
  margin: 20px 0 30px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.feature {
  display: flex;
  align-items: center;
  font-size: 0.95rem;
  color: #555;
  padding: 8px 15px;
  border-radius: 10px;
  transition: all 0.3s ease;
}

.love-card .feature:hover {
  background: rgba(106, 140, 175, 0.15);
  transform: translateX(5px);
}

.agent-card .feature:hover {
  background: rgba(124, 109, 165, 0.15);
  transform: translateX(5px);
}

.feature span {
  margin-right: 10px;
  font-size: 1.1rem;
}

/* 按钮样式 */
.btn {
  padding: 15px 30px;
  font-size: 16px;
  border: none;
  border-radius: 50px;
  cursor: pointer;
  font-weight: 600;
  letter-spacing: 0.5px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  color: white;
}

.btn::before {
  content: "";
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.2);
  transition: left 0.6s ease;
}

.btn:hover::before {
  left: 100%;
}

.btn-love {
  background: linear-gradient(45deg, #6a8caf, #98b4d4);
  box-shadow: 0 5px 15px rgba(106, 140, 175, 0.4);
}

.btn-love:hover {
  box-shadow: 0 8px 25px rgba(106, 140, 175, 0.6);
  transform: translateY(-3px);
}

.btn-agent {
  background: linear-gradient(45deg, #7c6da5, #a594d0);
  box-shadow: 0 5px 15px rgba(124, 109, 165, 0.4);
}

.btn-agent:hover {
  box-shadow: 0 8px 25px rgba(124, 109, 165, 0.6);
  transform: translateY(-3px);
}

/* 页脚样式 */
.footer {
  margin-top: 60px;
  text-align: center;
  color: rgba(255, 255, 255, 0.8);
  font-size: 0.9rem;
  position: relative;
  z-index: 2;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .title {
    font-size: 2.5rem;
  }

  .subtitle {
    font-size: 1.1rem;
  }

  .logo-icon {
    width: 80px;
    height: 80px;
  }

  .app-card {
    padding: 30px 20px;
  }
}
</style>

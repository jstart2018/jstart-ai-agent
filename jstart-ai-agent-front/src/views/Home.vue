<template>
  <div class="home-container">
    <div class="hero-section">
      <h1 class="title">AI智能助手平台</h1>
      <p class="subtitle">选择您需要的AI服务</p>
    </div>

    <div class="apps-grid">
      <div class="app-card" @click="navigateToLoveChat">
        <div class="app-icon">💕</div>
        <h3>AI恋爱大师</h3>
        <p>专业的恋爱建议和情感指导</p>
        <button class="btn btn-primary">开始聊天</button>
      </div>

      <div class="app-card" @click="navigateToAgentChat">
        <div class="app-icon">🤖</div>
        <h3>AI超级智能体</h3>
        <p>全能AI助手，解答各种问题</p>
        <button class="btn btn-secondary">开始聊天</button>
      </div>
    </div>
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
    }
  }
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.hero-section {
  text-align: center;
  margin-bottom: 50px;
  color: white;
}

.title {
  font-size: 3rem;
  font-weight: bold;
  margin-bottom: 10px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.subtitle {
  font-size: 1.2rem;
  opacity: 0.9;
}

.apps-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 30px;
  max-width: 800px;
  width: 100%;
}

.app-card {
  background: white;
  border-radius: 20px;
  padding: 40px 30px;
  text-align: center;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.app-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.3);
}

.app-icon {
  font-size: 4rem;
  margin-bottom: 20px;
}

.app-card h3 {
  font-size: 1.5rem;
  margin-bottom: 15px;
  color: #333;
}

.app-card p {
  color: #666;
  margin-bottom: 25px;
  line-height: 1.6;
}

.app-card .btn {
  width: 100%;
  font-weight: bold;
}
</style>

import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import App from './App.vue'
import Home from './views/Home.vue'
import LoveChat from './views/LoveChat.vue'
import AgentChat from './views/AgentChat.vue'
import './style.css'

const routes = [
  { path: '/', component: Home },
  { path: '/love-chat', component: LoveChat },
  { path: '/agent-chat', component: AgentChat }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

const app = createApp(App)
app.use(router)
app.mount('#app')

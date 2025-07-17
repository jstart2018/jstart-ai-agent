/**
 * 环境配置文件
 * 用于区分本地环境和生产环境
 */

// 为Vite环境变量声明类型，解决TypeScript错误
interface ImportMetaEnv {
  readonly DEV: boolean;
  readonly PROD: boolean;
  // 其他环境变量...
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}

// 更可靠地判断当前环境，多重判断确保正确性
let isDevelopment = false;
let baseApiUrl = '/api'; // 默认使用生产环境配置

// 安全地检测当前环境
try {
  // 检查1: 使用Vite的环境变量
  const viteEnvIsDev = import.meta.env.DEV === true;

  // 检查2: 使用域名判断
  const hostIsDev = typeof window !== 'undefined' &&
    (window.location.hostname === 'localhost' ||
     window.location.hostname === '127.0.0.1');

  // 只有在开发模式下才使用本地API
  isDevelopment = viteEnvIsDev || hostIsDev;

  // 根据环境设置API基础URL
  baseApiUrl = isDevelopment
    ? 'http://localhost:8123/api' // 本地开发环境API地址
    : '/api'; // 生产环境使用相对路径，将由nginx处理代理
} catch (e) {
  console.error('[环境配置] 环境检测出错，默认使用生产环境:', e);
  // 出错时使用默认的生产环境配置
  isDevelopment = false;
  baseApiUrl = '/api';
}

const isProduction = !isDevelopment;

// 在生产环境中强制使用相对路径，以防万一
if (isProduction) {
  baseApiUrl = '/api';
}

// 输出当前环境信息到控制台，便于调试
console.log('[环境配置] 当前环境:', isDevelopment ? 'Development' : 'Production');
console.log('[环境配置] API基础地址:', baseApiUrl);

// 导出配置
export const config = {
  isDevelopment,
  isProduction,
  baseApiUrl,
  // 其他配置项
  aiAgentChatEndpoint: `${baseApiUrl}/ai/agent/chat`,
  aiLoveChatEndpoint: `${baseApiUrl}/ai/doChat/sse3`,  // 修改为实际使用的API路径
  version: '1.0.0',
};

export default config;

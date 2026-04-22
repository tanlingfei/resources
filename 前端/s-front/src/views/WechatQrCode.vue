<template>
  <div class="wx-container">
    <div class="wx-card">
      <div class="wx-header">
        <h1 class="wx-title">关注我们的公众号</h1>
        <p class="wx-subtitle">获取更多优质资源和最新更新</p>
      </div>

      <div class="wx-content">
        <div class="wx-qr-section">
          <div class="qr-container">
            <img :src="wxQrCode" alt="公众号二维码" class="wx-qr-code">
            <div class="qr-shadow"></div>
          </div>
          <p class="qr-tip">扫码关注「糖糖资源站」</p>
        </div>

        <div class="wx-info-section">
          <h2 class="info-title">关注后你将获得：</h2>
          <ul class="info-list">
            <li class="info-item">
              <el-icon class="info-icon"><Check /></el-icon>
              <span>最新资源更新推送</span>
            </li>
            <li class="info-item">
              <el-icon class="info-icon"><Check /></el-icon>
              <span>添加客服微信 问题在线解答</span>
            </li>
          </ul>
        </div>
      </div>

      <div class="wx-footer">
        <div class="footer-tip">
          <el-icon class="tip-icon"><Warning /></el-icon>
          <span>温馨提示：扫码后请回复「资源」获取最新资源链接</span>
        </div>
        <el-button
          type="primary"
          class="back-button"
          @click="goBack"
          size="large"
        >
          <el-icon><ArrowLeft /></el-icon> 返回上一页
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { Check, Warning, ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()
const wxQrCode = new URL('@/assets/wx.jpg', import.meta.url).href

const goBack = () => {
  router.back()
}
</script>

<style scoped>
.wx-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.wx-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  max-width: 600px;
  width: 100%;
  overflow: hidden;
  animation: fadeIn 0.6s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.wx-header {
  text-align: center;
  padding: 40px 20px 30px;
  border-bottom: 1px solid #f0f0f0;
}

.wx-title {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin: 0 0 10px;
  line-height: 1.2;
}

.wx-subtitle {
  font-size: 16px;
  color: #666;
  margin: 0;
  line-height: 1.5;
}

.wx-content {
  padding: 30px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 40px;
}

.wx-qr-section {
  text-align: center;
  position: relative;
}

.qr-container {
  position: relative;
  display: inline-block;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 12px;
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s ease;
}

.qr-container:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.12);
}

.wx-qr-code {
  width: 200px;
  height: 200px;
  border-radius: 8px;
  display: block;
}

.qr-shadow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 12px;
  background: linear-gradient(45deg, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1));
  pointer-events: none;
}

.qr-tip {
  margin-top: 15px;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.wx-info-section {
  width: 100%;
  max-width: 400px;
}

.info-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0 0 20px;
  text-align: center;
}

.info-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.info-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s ease;
}

.info-item:hover {
  background-color: #f8f9fa;
}

.info-item:last-child {
  border-bottom: none;
}

.info-icon {
  color: #667eea;
  font-size: 20px;
  margin-right: 12px;
  flex-shrink: 0;
}

.info-item span {
  font-size: 14px;
  color: #555;
  line-height: 1.4;
}

.wx-footer {
  padding: 25px;
  border-top: 1px solid #f0f0f0;
  background-color: #f8f9fa;
}

.footer-tip {
  display: flex;
  align-items: center;
  background: #fff3cd;
  border: 1px solid #ffeaa7;
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 20px;
}

.tip-icon {
  color: #856404;
  margin-right: 10px;
  font-size: 18px;
}

.footer-tip span {
  font-size: 14px;
  color: #856404;
  line-height: 1.4;
}

.back-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.back-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

@media (max-width: 768px) {
  .wx-card {
    margin: 20px;
  }

  .wx-header {
    padding: 30px 20px 20px;
  }

  .wx-title {
    font-size: 24px;
  }

  .wx-content {
    padding: 20px;
    gap: 30px;
  }

  .wx-qr-code {
    width: 180px;
    height: 180px;
  }

  .qr-tip {
    font-size: 14px;
  }

  .info-title {
    font-size: 18px;
  }

  .info-item {
    padding: 10px 0;
  }

  .info-item span {
    font-size: 13px;
  }

  .wx-footer {
    padding: 20px;
  }

  .footer-tip {
    padding: 10px 14px;
  }

  .footer-tip span {
    font-size: 13px;
  }

  .back-button {
    height: 44px;
    font-size: 15px;
  }
}

@media (max-width: 480px) {
  .wx-title {
    font-size: 22px;
  }

  .wx-subtitle {
    font-size: 14px;
  }

  .wx-qr-code {
    width: 160px;
    height: 160px;
  }

  .info-title {
    font-size: 16px;
  }

  .info-item span {
    font-size: 12px;
  }
}
</style>

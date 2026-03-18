<template>
  <a-layout-footer class="footer">
    <div class="footer-content">
      <div class="footer-cat">
        <img :src="footerCat" alt="可爱的猫咪插画，来自喵绘网页" />
      </div>
      <p class="copyright">
        <span class="author-link">喵绘网页 (MeowDraw)</span>
      </p>
    </div>
  </a-layout-footer>
</template>

<script setup lang="ts">
import footerCat from '@/assets/undraw_cat_lqdj.svg'
</script>

<style scoped>
.footer {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  text-align: center;
  padding: 24px 20px 20px;
  margin-top: 40px;
  border-top: 1px solid rgba(102, 126, 234, 0.1);
}

.footer-cat {
  margin-bottom: 8px;
}

.footer-cat img {
  max-width: 80px;
  width: 100%;
  height: auto;
  opacity: 0.9;
}

.copyright {
  margin: 0;
  color: #666;
  font-size: 14px;
}
</style>

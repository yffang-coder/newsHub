<script setup lang="ts">
import { RouterLink } from 'vue-router';
</script>

<template>
  <footer class="bg-muted-background border-t border-border py-12 mt-12">
    <div class="container-width grid grid-cols-1 md:grid-cols-4 gap-8">
      <div class="space-y-4">
        <h3 class="text-lg font-bold text-primary">NewsHub</h3>
        <p class="text-sm text-muted-foreground leading-relaxed">
          为您提供最新、最深度的全球新闻报道。公正、客观、及时。
        </p>
      </div>
      
      <div>
        <h4 class="font-semibold mb-4 text-primary">栏目</h4>
        <ul class="space-y-2 text-sm text-muted-foreground list-none p-0">
          <li><RouterLink to="/category/world" class="hover:text-primary no-underline text-inherit">国际新闻</RouterLink></li>
          <li><RouterLink to="/category/tech" class="hover:text-primary no-underline text-inherit">科技前沿</RouterLink></li>
          <li><RouterLink to="/category/business" class="hover:text-primary no-underline text-inherit">财经观察</RouterLink></li>
          <li><RouterLink to="/category/culture" class="hover:text-primary no-underline text-inherit">文化生活</RouterLink></li>
        </ul>
      </div>

      <div>
        <h4 class="font-semibold mb-4 text-primary">关于</h4>
        <ul class="space-y-2 text-sm text-muted-foreground list-none p-0">
          <li><RouterLink to="/about" class="hover:text-primary no-underline text-inherit">关于我们</RouterLink></li>
          <li><RouterLink to="/contact" class="hover:text-primary no-underline text-inherit">联系方式</RouterLink></li>
          <li><RouterLink to="/careers" class="hover:text-primary no-underline text-inherit">加入我们</RouterLink></li>
          <li><RouterLink to="/privacy" class="hover:text-primary no-underline text-inherit">隐私政策</RouterLink></li>
        </ul>
      </div>

      <div>
        <h4 class="font-semibold mb-4 text-primary">订阅我们</h4>
        <p class="text-sm text-muted-foreground mb-4">
          获取每日精选新闻推送。
        </p>
        <el-form class="flex gap-2">
          <el-input placeholder="您的邮箱" class="flex-1" />
          <el-button type="primary">订阅</el-button>
        </el-form>
      </div>
    </div>
    <div class="container-width mt-12 pt-8 border-t border-border text-center text-sm text-muted-foreground">
      &copy; {{ new Date().getFullYear() }} NewsHub. All rights reserved.
    </div>
  </footer>
</template>

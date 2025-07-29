<template>
  <div :class="['msg-item', role]">
    <div class="msg-content" v-html="renderMarkdown(content)"></div>
  </div>
</template>

<script setup>
import { renderMarkdown } from '../utils/markdown.js'
const props = defineProps({
  role: { type: String, default: 'assistant' },
  content: { type: String, default: '' },
})
</script>

<style scoped>
.msg-item {
  margin-bottom: 12px;
  display: flex;
}
.msg-item.user {
  justify-content: flex-end;
}
.msg-item.assistant {
  justify-content: flex-start;
}
.msg-content {
  max-width: 80%;
  background: #f0f0f0;
  padding: 10px 14px;
  border-radius: 0 12px 12px 12px;
  line-height: 1.4;
  font-size: 0.9rem;
  color: #333;
  word-break: break-word;
  overflow-wrap: anywhere;
  position: relative;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
}
.msg-item.user .msg-content {
  background: linear-gradient(135deg, #4d9eff, #3a7bff);
  color: #fff;
  border-radius: 12px 0 12px 12px;
}
/* 代码块样式及滚动 */
.msg-content pre {
  margin: 0.5rem 0;
  padding: 8px;
  background: #f6f8fa;
  border-radius: 4px;
  overflow-x: auto;
  max-width: 100%;
}
.msg-content code {
  font-family: 'Source Code Pro', monospace;
  font-size: 0.85rem;
}
/* 气泡箭头 */
.msg-content::before {
  content: '';
  position: absolute;
  top: 10px;
  width: 0;
  height: 0;
  border: 6px solid transparent;
}
.msg-item.assistant .msg-content::before {
  left: -10px;
  border-right-color: #f0f0f0;
}
.msg-item.user .msg-content::before {
  right: -10px;
  border-left-color: #3a7bff;
}
</style> 
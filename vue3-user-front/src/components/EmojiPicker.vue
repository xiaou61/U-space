<template>
  <el-popover
    v-model:visible="visible"
    placement="bottom"
    :width="320"
    trigger="manual"
  >
    <template #reference>
      <el-button text @click="togglePicker">
        <span class="emoji-icon">😊</span>
        表情
      </el-button>
    </template>

    <div class="emoji-picker">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="表情" name="smileys">
          <div class="emoji-grid">
            <span 
              v-for="emoji in smileys" 
              :key="emoji" 
              class="emoji-item"
              @click.stop="selectEmoji(emoji)"
            >
              {{ emoji }}
            </span>
          </div>
        </el-tab-pane>
        <el-tab-pane label="手势" name="gestures">
          <div class="emoji-grid">
            <span 
              v-for="emoji in gestures" 
              :key="emoji" 
              class="emoji-item"
              @click.stop="selectEmoji(emoji)"
            >
              {{ emoji }}
            </span>
          </div>
        </el-tab-pane>
        <el-tab-pane label="其他" name="others">
          <div class="emoji-grid">
            <span 
              v-for="emoji in others" 
              :key="emoji" 
              class="emoji-item"
              @click.stop="selectEmoji(emoji)"
            >
              {{ emoji }}
            </span>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </el-popover>
</template>

<script setup>
import { ref } from 'vue'

const emit = defineEmits(['select'])

// 控制显示
const visible = ref(false)
const activeTab = ref('smileys')

// 表情数据
const smileys = ['😀', '😃', '😄', '😁', '😆', '😅', '🤣', '😂', '🙂', '🙃', '😉', '😊', '😇', '🥰', '😍', '🤩', '😘', '😗', '😚', '😙', '😋', '😛', '😜', '🤪', '😝', '🤑', '🤗', '🤭', '🤫', '🤔', '🤐', '🤨', '😐', '😑', '😶', '😏', '😒', '🙄', '😬', '🤥', '😌', '😔', '😪', '🤤', '😴', '😷', '🤒', '🤕', '🤢', '🤮', '🤧', '🥵', '🥶', '😵', '🤯', '🤠', '🥳', '😎', '🤓', '🧐']

const gestures = ['👍', '👎', '👊', '✊', '🤛', '🤜', '🤞', '✌️', '🤟', '🤘', '👌', '🤏', '👈', '👉', '👆', '👇', '☝️', '👋', '🤚', '🖐', '✋', '🖖', '👏', '🙌', '👐', '🤲', '🤝', '🙏']

const others = ['❤️', '🧡', '💛', '💚', '💙', '💜', '🖤', '🤍', '🤎', '💔', '❣️', '💕', '💞', '💓', '💗', '💖', '💘', '💝', '💟', '☮️', '✝️', '☪️', '🕉', '☸️', '✡️', '🔯', '🕎', '☯️', '☦️', '🛐', '⭐', '🌟', '✨', '⚡', '💥', '🔥', '☀️', '🌈', '☁️', '⛅', '⛈', '🌤', '🌥', '🌦', '🌧', '🌨', '🌩', '🌪', '❄️', '☃️', '⛄']

const togglePicker = () => {
  visible.value = !visible.value
}

const selectEmoji = (emoji) => {
  emit('select', emoji)
  // 不关闭弹窗，允许连续选择表情
  // visible.value = false
}
</script>

<style scoped>
.emoji-picker {
  max-height: 300px;
  overflow-y: auto;
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 8px;
  padding: 10px 0;
}

.emoji-item {
  font-size: 24px;
  cursor: pointer;
  text-align: center;
  padding: 5px;
  border-radius: 4px;
  transition: all 0.2s;
}

.emoji-item:hover {
  background-color: #f5f7fa;
  transform: scale(1.2);
}

.emoji-icon {
  font-size: 16px;
  margin-right: 4px;
}
</style>


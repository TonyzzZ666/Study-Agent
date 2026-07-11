<template>
  <div id="app">
    <router-view v-slot="{ Component }">
      <transition name="app-switch" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>
  </div>
</template>

<script setup>
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.app-switch-enter-active {
  transition: all 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}
.app-switch-leave-active {
  transition: all 0.25s ease-out;
}
.app-switch-enter-from {
  opacity: 0;
  transform: scale(1.03);
}
.app-switch-leave-to {
  opacity: 0;
  transform: scale(0.98);
}
</style>

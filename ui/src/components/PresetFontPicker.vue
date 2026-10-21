<script setup lang="ts">
import { Toast, VButton, VTag } from "@halo-dev/components";
import { computed, onMounted, ref } from "vue";
import { listPresets, listRecommended } from "../api";
import type { FontItem, PresetFont } from "../types";
import { loadFontCss } from "../utils/font-loader";

const props = defineProps<{
  existingNames: string[];
}>();

const emit = defineEmits<{
  (event: "close"): void;
  (event: "add", font: FontItem): void;
}>();

const activeTab = ref<"preset" | "recommended">("preset");
const presets = ref<PresetFont[]>([]);
const recommended = ref<PresetFont[]>([]);
const loading = ref(false);
const loadError = ref(false);
const activeCategory = ref("全部");
const previewText = ref("永和九年，岁在癸丑 The quick brown fox 0123456789");

const currentList = computed(() =>
  activeTab.value === "preset" ? presets.value : recommended.value
);

const categories = computed(() => {
  const set = new Set(currentList.value.map((p) => p.category).filter(Boolean));
  return ["全部", ...Array.from(set)];
});

const filtered = computed(() =>
  activeCategory.value === "全部"
    ? currentList.value
    : currentList.value.filter((p) => p.category === activeCategory.value)
);

function switchTab(tab: "preset" | "recommended") {
  activeTab.value = tab;
  activeCategory.value = "全部";
}

function isAdded(preset: PresetFont) {
  return props.existingNames.includes(preset.id);
}

async function onShow(preset: PresetFont) {
  try {
    await loadFontCss(preset.id, preset.cssUrl);
  } catch (e) {
    console.error("Failed to load preset font css", e);
    Toast.warning(`「${preset.displayName}」预览加载失败，请检查网络或配置加速规则`);
  }
}

function onAdd(preset: PresetFont) {
  emit("add", {
    name: preset.id,
    displayName: preset.displayName,
    family: preset.family,
    source: activeTab.value === "preset" ? "preset" : "custom",
    cssUrl: preset.cssUrl,
    enabled: true,
    weight: preset.weight || "",
  });
}

async function fetchPresets() {
  loading.value = true;
  loadError.value = false;
  try {
    const [presetsRes, recommendedRes] = await Promise.all([
      listPresets(),
      listRecommended(),
    ]);
    presets.value = presetsRes.data;
    recommended.value = recommendedRes.data;
  } catch (e) {
    console.error("Failed to fetch font libraries", e);
    loadError.value = true;
    Toast.error("获取字体库失败");
  } finally {
    loading.value = false;
  }
}

onMounted(fetchPresets);
</script>

<template>
  <div class="picker-mask" @click.self="emit('close')">
    <div class="picker-modal">
      <div class="picker-modal-header">
        <span class="picker-modal-title">添加字体</span>
        <button type="button" class="picker-modal-close" @click="emit('close')">&times;</button>
      </div>
      <div class="picker-body">
        <div class="picker-toolbar">
      <div class="picker-tabs">
        <button
          type="button"
          class="picker-tab"
          :class="{ 'picker-tab-active': activeTab === 'preset' }"
          @click="switchTab('preset')"
        >
          内置预设
        </button>
        <button
          type="button"
          class="picker-tab"
          :class="{ 'picker-tab-active': activeTab === 'recommended' }"
          @click="switchTab('recommended')"
        >
          推荐字体
        </button>
      </div>
      <div v-if="activeTab === 'recommended'" class="picker-tab-hint">
        推荐字体来自社区 CDN（可免费商用），添加后为「自定义」字体，名称与地址均可修改
      </div>
      <input
        v-model="previewText"
        class="picker-preview-input"
        placeholder="输入预览文本"
      />
      <div class="picker-categories">
        <VTag
          v-for="category in categories"
          :key="category"
          :class="{ 'picker-category-active': category === activeCategory }"
          class="picker-category"
          @click="activeCategory = category"
        >
          {{ category }}
        </VTag>
      </div>
    </div>

    <div v-if="loading" class="picker-loading">加载中...</div>

    <div v-if="!loading && loadError" class="picker-loading">
      <p>字体库加载失败，请检查插件是否已启用或刷新重试</p>
      <VButton size="sm" @click="fetchPresets">重试</VButton>
    </div>

    <div v-if="!loading && !loadError" class="picker-grid">
      <div
        v-for="preset in filtered"
        :key="preset.id"
        class="picker-card"
        @mouseenter="onShow(preset)"
      >
        <div
          class="picker-card-preview"
          :style="{ fontFamily: preset.family, fontWeight: preset.weight || undefined }"
        >
          {{ previewText }}
        </div>
        <div class="picker-card-meta">
          <span class="picker-card-name">{{ preset.displayName }}</span>
          <VTag>{{ preset.category }}</VTag>
        </div>
        <VButton
          size="sm"
          :type="isAdded(preset) ? 'default' : 'secondary'"
          :disabled="isAdded(preset)"
          block
          @click="onAdd(preset)"
        >
          {{ isAdded(preset) ? "已添加" : "添加" }}
        </VButton>
      </div>
    </div>
      </div>
      <div class="picker-modal-footer">
        <VButton @click="emit('close')">关闭</VButton>
      </div>
    </div>
  </div>
</template>

<style scoped>
.picker-mask {
  position: fixed;
  inset: 0;
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(15, 23, 42, 0.45);
}

.picker-modal {
  display: flex;
  flex-direction: column;
  width: 720px;
  max-width: calc(100vw - 48px);
  max-height: calc(100vh - 96px);
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.25);
  overflow: hidden;
}

.picker-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px;
  border-bottom: 1px solid #f0f0f0;
}

.picker-modal-title {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
}

.picker-modal-close {
  border: none;
  background: none;
  font-size: 20px;
  line-height: 1;
  color: #9ca3af;
  cursor: pointer;
}

.picker-modal-close:hover {
  color: #374151;
}

.picker-body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 16px 18px;
}

.picker-modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 12px 18px;
  border-top: 1px solid #f0f0f0;
}

.picker-toolbar {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 14px;
}

.picker-tabs {
  display: flex;
  gap: 4px;
  padding: 3px;
  background: #f3f4f6;
  border-radius: 8px;
  width: fit-content;
}

.picker-tab {
  padding: 5px 16px;
  border: none;
  border-radius: 6px;
  background: transparent;
  font-size: 13px;
  color: #6b7280;
  cursor: pointer;
}

.picker-tab-active {
  background: #fff;
  color: #111827;
  font-weight: 500;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.picker-tab-hint {
  font-size: 12px;
  color: #9ca3af;
}

.picker-preview-input {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
}

.picker-preview-input:focus {
  outline: none;
  border-color: #4f46e5;
  box-shadow: 0 0 0 2px rgba(79, 70, 229, 0.15);
}

.picker-categories {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.picker-category {
  cursor: pointer;
}

.picker-category-active {
  background-color: #4f46e5;
  color: #fff;
}

.picker-loading {
  padding: 40px 0;
  text-align: center;
  color: #6b7280;
}

.picker-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
}

.picker-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.picker-card-preview {
  min-height: 56px;
  font-size: 18px;
  line-height: 1.4;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.picker-card-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.picker-card-name {
  font-size: 13px;
  color: #374151;
}
</style>

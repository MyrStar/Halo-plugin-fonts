<script setup lang="ts">
import {
  Toast,
  VButton,
  VCard,
  VEmpty,
  VLoading,
  VPageHeader,
  VSpace,
  VTag,
} from "@halo-dev/components";
import { computed, onMounted, ref } from "vue";
import {
  getDefaultFont,
  listFonts,
  saveFonts,
  setDefaultFont,
} from "../api";
import PresetFontPicker from "../components/PresetFontPicker.vue";
import type { FontItem } from "../types";
import { loadFontCss } from "../utils/font-loader";

const THEME_DEFAULT = "__theme__";

const fonts = ref<FontItem[]>([]);
const defaultFont = ref(THEME_DEFAULT);
const previewText = ref("永和九年，岁在癸丑，暮春之初，会于会稽山阴之兰亭。The quick brown fox jumps over the lazy dog. 0123456789");
const loading = ref(false);
const saving = ref(false);
const pickerVisible = ref(false);
const editorVisible = ref(false);
const editingFont = ref<FontItem | null>(null);

const editorForm = ref({ displayName: "", family: "", cssUrl: "" });
const isEditing = computed(() => editingFont.value !== null);

async function fetchData() {
  loading.value = true;
  try {
    const [fontsRes, defaultRes] = await Promise.all([listFonts(), getDefaultFont()]);
    fonts.value = fontsRes.data.fonts || [];
    defaultFont.value = defaultRes.data.name || THEME_DEFAULT;
    fonts.value.forEach((font) => {
      if (font.enabled) {
        loadFontCss(font.name, font.cssUrl).catch(() => {
          Toast.warning(`「${font.displayName}」加载失败，请检查字体地址或网络`);
        });
      }
    });
  } catch (e) {
    console.error("Failed to fetch fonts", e);
    Toast.error("获取字体列表失败");
  } finally {
    loading.value = false;
  }
}

async function persist(message: string) {
  saving.value = true;
  try {
    await saveFonts(fonts.value);
    Toast.success(message);
  } catch (e) {
    console.error("Failed to save fonts", e);
    Toast.error("保存失败，请稍后重试");
  } finally {
    saving.value = false;
  }
}

async function onToggleEnabled(font: FontItem) {
  font.enabled = !font.enabled;
  if (font.enabled) {
    loadFontCss(font.name, font.cssUrl).catch(() => undefined);
  }
  if (!font.enabled && defaultFont.value === font.name) {
    await onSetDefault(THEME_DEFAULT);
  }
  await persist(font.enabled ? `已启用「${font.displayName}」` : `已停用「${font.displayName}」`);
}

async function onRemove(font: FontItem) {
  fonts.value = fonts.value.filter((item) => item.name !== font.name);
  if (defaultFont.value === font.name) {
    await onSetDefault(THEME_DEFAULT);
  }
  await persist(`已删除「${font.displayName}」`);
}

async function onSetDefault(name: string) {
  try {
    await setDefaultFont(name);
    defaultFont.value = name;
    Toast.success(
      name === THEME_DEFAULT
        ? "已恢复为跟随主题"
        : `已将「${fonts.value.find((f) => f.name === name)?.displayName}」设为默认字体`
    );
  } catch (e) {
    console.error("Failed to set default font", e);
    Toast.error("设置默认字体失败");
  }
}

function onPresetAdd(font: FontItem) {
  fonts.value = [...fonts.value, font];
  loadFontCss(font.name, font.cssUrl).catch(() => undefined);
  persist(`已添加「${font.displayName}」`);
}

function openEditor(font?: FontItem) {
  editingFont.value = font ?? null;
  editorForm.value = font
    ? { displayName: font.displayName, family: font.family, cssUrl: font.cssUrl }
    : { displayName: "", family: "", cssUrl: "" };
  editorVisible.value = true;
}

async function onEditorSubmit() {
  const { displayName, family, cssUrl } = editorForm.value;
  if (!displayName.trim() || !family.trim() || !cssUrl.trim()) {
    Toast.warning("请填写完整的字体信息");
    return;
  }
  if (editingFont.value) {
    const target = editingFont.value;
    target.displayName = displayName.trim();
    target.family = family.trim();
    target.cssUrl = cssUrl.trim();
    editorVisible.value = false;
    loadFontCss(target.name, target.cssUrl).catch(() => undefined);
    await persist(`已更新「${target.displayName}」`);
  } else {
    const font: FontItem = {
      name: `custom-${Date.now().toString(36)}`,
      displayName: displayName.trim(),
      family: family.trim(),
      source: "custom",
      cssUrl: cssUrl.trim(),
      enabled: true,
    };
    fonts.value = [...fonts.value, font];
    editorVisible.value = false;
    loadFontCss(font.name, font.cssUrl).catch(() => undefined);
    await persist(`已添加「${font.displayName}」`);
  }
}

onMounted(fetchData);
</script>

<template>
  <VPageHeader title="字体管理">
    <template #actions>
      <VSpace>
        <VButton @click="pickerVisible = true">从预设库添加</VButton>
        <VButton type="secondary" @click="openEditor()">添加自定义字体</VButton>
        <VButton type="default" @click="$router.push('/plugins/PluginFonts')">
          全局设置
        </VButton>
      </VSpace>
    </template>
  </VPageHeader>

  <div class="font-manager">
    <VCard>
      <div class="toolbar">
        <label class="toolbar-label">预览文本</label>
        <input v-model="previewText" class="toolbar-input" placeholder="输入用于预览的文字" />
        <span class="toolbar-hint">
          全局开关、字体加速地址与浮窗配置请在「全局设置」（插件设置页）中修改
        </span>
      </div>
    </VCard>

    <VLoading v-if="loading" />

    <VEmpty v-if="!loading && !fonts.length" title="还没有字体" message="从预设库添加或创建自定义字体开始使用">
      <template #actions>
        <VButton type="secondary" @click="pickerVisible = true">从预设库添加</VButton>
      </template>
    </VEmpty>

    <div v-if="!loading && fonts.length" class="font-list">
      <VCard v-for="font in fonts" :key="font.name" class="font-card">
        <div class="font-card-main">
          <div
            class="font-preview"
            :class="{ 'font-disabled': !font.enabled }"
            :style="{ fontFamily: font.family }"
          >
            {{ previewText }}
          </div>
          <div class="font-meta">
            <span class="font-name">{{ font.displayName }}</span>
            <VTag>{{ font.source === "preset" ? "预设" : "自定义" }}</VTag>
            <VTag v-if="defaultFont === font.name" class="font-default-tag">默认字体</VTag>
            <VTag v-if="!font.enabled">已停用</VTag>
          </div>
          <div class="font-detail">
            <code>{{ font.family }}</code>
            <span class="font-url">{{ font.cssUrl }}</span>
          </div>
        </div>
        <div class="font-actions">
          <label class="font-switch">
            <input
              type="checkbox"
              :checked="font.enabled"
              :disabled="saving"
              @change="onToggleEnabled(font)"
            />
            <span>启用</span>
          </label>
          <VButton
            v-if="defaultFont !== font.name && font.enabled"
            size="sm"
            :disabled="saving"
            @click="onSetDefault(font.name)"
          >
            设为默认
          </VButton>
          <VButton
            v-if="defaultFont === font.name"
            size="sm"
            :disabled="saving"
            @click="onSetDefault(THEME_DEFAULT)"
          >
            恢复跟随主题
          </VButton>
          <VButton v-if="font.source === 'custom'" size="sm" :disabled="saving" @click="openEditor(font)">
            编辑
          </VButton>
          <VButton size="sm" type="danger" :disabled="saving" @click="onRemove(font)">
            删除
          </VButton>
        </div>
      </VCard>
    </div>

    <PresetFontPicker
      v-if="pickerVisible"
      :existing-names="fonts.map((f) => f.name)"
      @close="pickerVisible = false"
      @add="onPresetAdd"
    />

    <div v-if="editorVisible" class="editor-mask" @click.self="editorVisible = false">
      <div class="editor-modal">
        <div class="editor-modal-header">
          <span class="editor-modal-title">{{ isEditing ? "编辑自定义字体" : "添加自定义字体" }}</span>
          <button type="button" class="editor-modal-close" @click="editorVisible = false">&times;</button>
        </div>
        <div class="editor-modal-body">
          <div class="editor-form">
        <div class="editor-field">
          <label class="editor-label">字体名称</label>
          <input v-model="editorForm.displayName" class="editor-input" placeholder="例如：得意黑" />
        </div>
        <div class="editor-field">
          <label class="editor-label">font-family 值</label>
          <input
            v-model="editorForm.family"
            class="editor-input"
            placeholder="例如：'Smiley Sans', sans-serif"
          />
          <span class="editor-help">CSS font-family 的完整取值，建议带通用回退字体</span>
        </div>
        <div class="editor-field">
          <label class="editor-label">字体 CSS 地址</label>
          <input
            v-model="editorForm.cssUrl"
            class="editor-input"
            placeholder="https://.../font.css（@font-face 定义所在的样式表地址）"
          />
          <span class="editor-help">保存后会应用「全局设置」中的加速地址规则</span>
        </div>
      </div>
          </div>
        <div class="editor-modal-footer">
          <VSpace>
            <VButton type="secondary" :loading="saving" @click="onEditorSubmit">保存</VButton>
            <VButton @click="editorVisible = false">取消</VButton>
          </VSpace>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.editor-mask {
  position: fixed;
  inset: 0;
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(15, 23, 42, 0.45);
}

.editor-modal {
  display: flex;
  flex-direction: column;
  width: 560px;
  max-width: calc(100vw - 48px);
  max-height: calc(100vh - 96px);
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.25);
  overflow: hidden;
}

.editor-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px;
  border-bottom: 1px solid #f0f0f0;
}

.editor-modal-title {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
}

.editor-modal-close {
  border: none;
  background: none;
  font-size: 20px;
  line-height: 1;
  color: #9ca3af;
  cursor: pointer;
}

.editor-modal-close:hover {
  color: #374151;
}

.editor-modal-body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 16px 18px;
}

.editor-modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 12px 18px;
  border-top: 1px solid #f0f0f0;
}

.font-manager {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  flex-wrap: wrap;
}

.toolbar-label {
  font-size: 13px;
  color: #4b5563;
  white-space: nowrap;
}

.toolbar-input {
  flex: 1;
  min-width: 220px;
  padding: 7px 10px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
}

.toolbar-input:focus {
  outline: none;
  border-color: #4f46e5;
  box-shadow: 0 0 0 2px rgba(79, 70, 229, 0.15);
}

.toolbar-hint {
  font-size: 12px;
  color: #9ca3af;
}

.font-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.font-card {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 16px;
}

.font-card-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.font-preview {
  font-size: 22px;
  line-height: 1.5;
  word-break: break-all;
}

.font-disabled {
  opacity: 0.45;
}

.font-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.font-name {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
}

.font-default-tag {
  background-color: #4f46e5;
  color: #fff;
}

.font-detail {
  display: flex;
  flex-direction: column;
  gap: 2px;
  font-size: 12px;
  color: #6b7280;
}

.font-url {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.font-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
  flex-wrap: wrap;
}

.font-switch {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #4b5563;
  cursor: pointer;
}

.editor-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.editor-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.editor-label {
  font-size: 13px;
  font-weight: 500;
  color: #374151;
}

.editor-input {
  padding: 8px 10px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
}

.editor-input:focus {
  outline: none;
  border-color: #4f46e5;
  box-shadow: 0 0 0 2px rgba(79, 70, 229, 0.15);
}

.editor-help {
  font-size: 12px;
  color: #9ca3af;
}
</style>

import { axiosInstance } from "@halo-dev/api-client";
import type { Fonts, FontItem, PresetFont } from "./types";

const base = "/apis/pluginfonts.console.halo.run/v1alpha1";

export function listPresets() {
  return axiosInstance.get<PresetFont[]>(`${base}/presets`);
}

export function listFonts() {
  return axiosInstance.get<Fonts>(`${base}/fonts`);
}

export function saveFonts(fonts: FontItem[]) {
  return axiosInstance.put<Fonts>(`${base}/fonts`, { fonts });
}

export function getDefaultFont() {
  return axiosInstance.get<{ name: string }>(`${base}/fonts/default`);
}

export function setDefaultFont(name: string) {
  return axiosInstance.put<void>(`${base}/fonts/default`, { name });
}

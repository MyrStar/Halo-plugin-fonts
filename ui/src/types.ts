export interface FontItem {
  name: string;
  displayName: string;
  family: string;
  /** preset | custom */
  source: string;
  cssUrl: string;
  enabled: boolean;
  /** 字重，空串表示跟随字体默认 */
  weight?: string;
}

export interface Fonts {
  fonts: FontItem[];
}

export interface PresetFont {
  id: string;
  displayName: string;
  family: string;
  cssUrl: string;
  category: string;
  /** 推荐字重，空串表示跟随字体默认 */
  weight?: string;
}

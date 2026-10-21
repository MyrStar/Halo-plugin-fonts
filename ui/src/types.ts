export interface FontItem {
  name: string;
  displayName: string;
  family: string;
  /** preset | custom */
  source: string;
  cssUrl: string;
  enabled: boolean;
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
}

const loaded = new Set<string>();

/**
 * 通过插入 <link rel="stylesheet"> 加载字体 CSS，用于 Console 内的即时预览。
 * 同一字体只加载一次；woff2 分片由浏览器按 unicode-range 惰性下载。
 */
export async function loadFontCss(id: string, cssUrl: string): Promise<void> {
  if (loaded.has(id) || !cssUrl) {
    return;
  }
  await new Promise<void>((resolve, reject) => {
    const link = document.createElement("link");
    link.rel = "stylesheet";
    link.href = cssUrl;
    link.onload = () => resolve();
    link.onerror = () => reject(new Error(`字体加载失败: ${cssUrl}`));
    document.head.appendChild(link);
  });
  loaded.add(id);
}

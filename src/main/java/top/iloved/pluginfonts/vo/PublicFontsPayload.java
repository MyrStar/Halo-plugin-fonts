package top.iloved.pluginfonts.vo;

import java.util.List;
import top.iloved.pluginfonts.FontsService.Widget;
import top.iloved.pluginfonts.FontsSetting.FontItem;

/**
 * 前台公开 API 返回体：浮窗渲染与字体切换所需的全部数据。
 */
public record PublicFontsPayload(String defaultFont,
                                 List<String> applySelectors,
                                 String fontWeight,
                                 Widget widget,
                                 List<FontItem> fonts) {
}

package top.iloved.pluginfonts;

import java.util.List;

/**
 * 插件设置模型，与 extensions/settings.yaml 及 ConfigMap 中存储的数据结构对应。
 *
 * <p>设置按 group 存储在插件 ConfigMap 中：{@link #BASIC_GROUP} 为全局设置（由原生设置
 * 表单维护），{@link #FONTS_GROUP} 为字体列表（由自定义 Console 页面通过 Endpoint 维护）。
 */
public final class FontsSetting {

    public static final String BASIC_GROUP = "basic";
    public static final String FONTS_GROUP = "fonts";
    public static final String CONFIGMAP_NAME = "plugin-fonts-configmap";

    /**
     * 默认字体取该值时表示跟随主题，不做全局覆盖。
     */
    public static final String THEME_DEFAULT = "__theme__";

    private FontsSetting() {
    }

    public record Basic(String defaultFont,
                        Boolean loadAllEnabled,
                        List<AcceleratorRule> acceleratorRules,
                        Boolean widgetEnabled,
                        String widgetPosition,
                        String widgetTitle,
                        String applySelectors) {

        public static Basic defaultBasic() {
            return new Basic(THEME_DEFAULT, true, List.of(), true,
                "bottom-right", "切换字体", "body");
        }
    }

    public record AcceleratorRule(String from, String to, Boolean enabled) {
    }

    public record Fonts(List<FontItem> fonts) {
    }

    public record FontItem(String name,
                           String displayName,
                           String family,
                           String source,
                           String cssUrl,
                           Boolean enabled,
                           String weight) {
    }
}

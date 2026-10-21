package top.iloved.pluginfonts;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.ReactiveSettingFetcher;
import top.iloved.pluginfonts.FontsSetting.AcceleratorRule;
import top.iloved.pluginfonts.FontsSetting.Basic;
import top.iloved.pluginfonts.FontsSetting.FontItem;
import top.iloved.pluginfonts.FontsSetting.Fonts;

/**
 * 字体插件核心服务：读取设置、应用加速规则、产出前台注入与公开 API 所需的模型。
 *
 * <p>设置通过 {@link ReactiveSettingFetcher} 每次实时读取，配置保存后刷新页面即生效，
 * 无需重启插件。
 */
@Service
@RequiredArgsConstructor
public class FontsService {

    public static final String CSS_VARIABLE = "--halo-plugin-fonts-family";

    private final ReactiveSettingFetcher settingFetcher;

    public Mono<Basic> basic() {
        return settingFetcher.fetch(FontsSetting.BASIC_GROUP, Basic.class)
            .defaultIfEmpty(Basic.defaultBasic());
    }

    public Mono<List<FontItem>> fonts() {
        return settingFetcher.fetch(FontsSetting.FONTS_GROUP, Fonts.class)
            .map(fonts -> fonts.fonts() == null ? List.<FontItem>of() : fonts.fonts())
            .defaultIfEmpty(List.of());
    }

    /**
     * 汇总当前生效配置：已启用字体（CSS 地址已应用加速规则）、默认字体、
     * 需要注入的 CSS 地址列表与覆盖样式文本、浮窗配置。
     */
    public Mono<ResolvedFonts> resolve() {
        return Mono.zip(basic(), fonts())
            .map(tuple -> {
                var basic = tuple.getT1();
                var enabledFonts = tuple.getT2().stream()
                    .filter(font -> !Boolean.FALSE.equals(font.enabled()))
                    .map(font -> new FontItem(font.name(), font.displayName(), font.family(),
                        font.source(), applyAccelerator(font.cssUrl(), basic.acceleratorRules()),
                        font.enabled()))
                    .toList();

                var defaultFontName = StringUtils.defaultIfBlank(basic.defaultFont(),
                    FontsSetting.THEME_DEFAULT);
                var defaultFont = enabledFonts.stream()
                    .filter(font -> font.name().equals(defaultFontName))
                    .findFirst()
                    .orElse(null);

                var loadAll = !Boolean.FALSE.equals(basic.loadAllEnabled());
                List<String> cssUrls;
                if (loadAll) {
                    cssUrls = enabledFonts.stream()
                        .map(FontItem::cssUrl)
                        .filter(StringUtils::isNotBlank)
                        .distinct()
                        .toList();
                } else if (defaultFont != null && StringUtils.isNotBlank(defaultFont.cssUrl())) {
                    cssUrls = List.of(defaultFont.cssUrl());
                } else {
                    cssUrls = List.of();
                }

                var styleText = defaultFont == null ? null
                    : buildStyleText(defaultFont.family(), parseSelectors(basic.applySelectors()),
                        basic.fontWeight());

                var widget = new Widget(!Boolean.FALSE.equals(basic.widgetEnabled()),
                    StringUtils.defaultIfBlank(basic.widgetPosition(), "bottom-right"),
                    StringUtils.defaultIfBlank(basic.widgetTitle(), "切换字体"));

                return new ResolvedFonts(enabledFonts, cssUrls, defaultFontName,
                    parseSelectors(basic.applySelectors()),
                    StringUtils.defaultIfBlank(basic.fontWeight(), ""), styleText, widget);
            });
    }

    /**
     * 按加速规则对字体 CSS 地址做主机替换。仅做字符串替换，不抓取远端内容。
     */
    static String applyAccelerator(String cssUrl, List<AcceleratorRule> rules) {
        if (StringUtils.isBlank(cssUrl) || rules == null) {
            return cssUrl;
        }
        var result = cssUrl;
        for (var rule : rules) {
            if (rule == null || Boolean.FALSE.equals(rule.enabled())
                || StringUtils.isBlank(rule.from()) || StringUtils.isBlank(rule.to())) {
                continue;
            }
            result = result.replace(rule.from().trim(), rule.to().trim());
        }
        return result;
    }

    /**
     * 解析字体作用选择器配置：每行一个选择器，默认作用 body。
     */
    static List<String> parseSelectors(String applySelectors) {
        var selectors = new ArrayList<String>();
        if (StringUtils.isNotBlank(applySelectors)) {
            for (var line : applySelectors.split("\\R")) {
                var selector = line.trim();
                if (!selector.isEmpty()) {
                    selectors.add(selector);
                }
            }
        }
        return selectors.isEmpty() ? List.of("body") : List.copyOf(selectors);
    }

    /**
     * 生成全局覆盖样式：CSS 变量承载当前字体族，配合 :where() 低特异性 + !important，
     * 既能压过主题普通规则，又允许主题带 !important 的规则覆盖插件。
     * fontWeight 非空时同步覆盖字重。
     */
    static String buildStyleText(String defaultFamily, List<String> selectors, String fontWeight) {
        var weightRule = StringUtils.isBlank(fontWeight) ? ""
            : "font-weight:" + fontWeight + " !important;";
        return ":root{" + CSS_VARIABLE + ":" + defaultFamily + "}"
            + ":where(" + String.join(",", selectors) + ")"
            + "{font-family:var(" + CSS_VARIABLE + ") !important;" + weightRule + "}";
    }

    public record Widget(boolean enabled, String position, String title) {
    }

    public record ResolvedFonts(List<FontItem> enabledFonts,
                                List<String> cssUrls,
                                String defaultFont,
                                List<String> applySelectors,
                                String fontWeight,
                                String styleText,
                                Widget widget) {
    }
}

package top.iloved.pluginfonts;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.AttributeValueQuotes;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import reactor.core.publisher.Mono;
import run.halo.app.theme.dialect.TemplateHeadProcessor;

/**
 * 向前台所有页面的 head 注入字体资源：
 *
 * <ul>
 *   <li>启用字体的 CSS link（已应用加速规则）</li>
 *   <li>全局字体覆盖样式（仅在设置了默认字体时注入）</li>
 *   <li>字体切换浮窗的 JS/CSS 引用（defer 注入，不依赖主题的 halo:footer 标签）</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class FontsHeadProcessor implements TemplateHeadProcessor {

    private static final String ASSETS_BASE = "/plugins/PluginFonts/assets/widget";

    private final FontsService fontsService;

    @Override
    public Mono<Void> process(ITemplateContext context, IModel model,
                              IElementModelStructureHandler structureHandler) {
        return fontsService.resolve()
            .doOnNext(resolved -> {
                var factory = context.getModelFactory();

                resolved.cssUrls().forEach(url -> model.add(factory.createStandaloneElementTag(
                    "link",
                    Map.of("rel", "stylesheet", "href", url, "data-plugin-fonts", ""),
                    AttributeValueQuotes.DOUBLE, false, true)));

                if (resolved.styleText() != null) {
                    addStyleElement(factory, model, resolved.styleText());
                }

                if (resolved.widget().enabled() && !resolved.enabledFonts().isEmpty()) {
                    model.add(factory.createStandaloneElementTag(
                        "link",
                        Map.of("rel", "stylesheet",
                            "href", ASSETS_BASE + "/font-switcher.css",
                            "data-plugin-fonts", ""),
                        AttributeValueQuotes.DOUBLE, false, true));
                    // script 需成对开闭标签，defer 保证在 DOM 解析完成后执行
                    model.add(factory.createOpenElementTag("script",
                        Map.of("defer", "", "src", ASSETS_BASE + "/font-switcher.js",
                            "data-plugin-fonts", ""),
                        AttributeValueQuotes.DOUBLE));
                    model.add(factory.createCloseElementTag("script"));
                }
            })
            .then();
    }

    private static void addStyleElement(IModelFactory factory, IModel model, String css) {
        model.add(factory.createOpenElementTag("style",
            Map.of("type", "text/css", "data-plugin-fonts", ""),
            AttributeValueQuotes.DOUBLE));
        model.add(factory.createText(css));
        model.add(factory.createCloseElementTag("style"));
    }
}

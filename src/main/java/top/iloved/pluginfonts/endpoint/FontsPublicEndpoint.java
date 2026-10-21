package top.iloved.pluginfonts.endpoint;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;

import lombok.RequiredArgsConstructor;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import top.iloved.pluginfonts.FontsService;
import top.iloved.pluginfonts.vo.PublicFontsPayload;

/**
 * 前台公开 API：为字体切换浮窗提供当前生效的字体列表与浮窗配置。
 *
 * <p>通过匿名角色模板开放 get/list 权限，未登录访客亦可访问。
 */
@Component
@RequiredArgsConstructor
public class FontsPublicEndpoint implements CustomEndpoint {

    static final String GROUP = "api.pluginfonts.halo.run";
    static final String VERSION = "v1alpha1";

    private final FontsService fontsService;

    @Override
    public GroupVersion groupVersion() {
        return new GroupVersion(GROUP, VERSION);
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = GROUP + "/" + VERSION + "/Font";
        return SpringdocRouteBuilder.route()
            .GET("fonts", this::getPublicFonts, builder -> builder
                .operationId("GetPublicFonts")
                .description("Get enabled fonts and widget config for theme side.")
                .tag(tag)
                .response(responseBuilder().implementation(PublicFontsPayload.class)))
            .build();
    }

    private Mono<ServerResponse> getPublicFonts(ServerRequest request) {
        return fontsService.resolve()
            .map(resolved -> new PublicFontsPayload(
                resolved.defaultFont(),
                resolved.applySelectors(),
                resolved.fontWeight(),
                resolved.widget(),
                resolved.enabledFonts()))
            .flatMap(payload -> ServerResponse.ok()
                .header("Cache-Control", "private, max-age=60")
                .bodyValue(payload));
    }
}

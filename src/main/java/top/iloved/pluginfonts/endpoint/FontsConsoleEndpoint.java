package top.iloved.pluginfonts.endpoint;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.utils.JsonUtils;
import top.iloved.pluginfonts.FontsService;
import top.iloved.pluginfonts.FontsSetting;
import top.iloved.pluginfonts.FontsSetting.FontItem;
import top.iloved.pluginfonts.FontsSetting.Fonts;
import top.iloved.pluginfonts.PresetFontsProvider;
import top.iloved.pluginfonts.PresetFontsProvider.PresetFont;

/**
 * Console 管理 API：预设字体库查询、字体列表读写、默认字体设置。
 *
 * <p>字体列表存储在插件 ConfigMap 的 {@code fonts} group 中（与原生设置表单存储
 * {@code basic} group 的格式一致：group 名为 key，值为 JSON 字符串）。
 */
@Component
@RequiredArgsConstructor
public class FontsConsoleEndpoint implements CustomEndpoint {

    static final String GROUP = "pluginfonts.console.halo.run";
    static final String VERSION = "v1alpha1";

    private final ReactiveExtensionClient client;
    private final FontsService fontsService;
    private final PresetFontsProvider presetFontsProvider;

    @Override
    public GroupVersion groupVersion() {
        return new GroupVersion(GROUP, VERSION);
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = GROUP + "/" + VERSION + "/Font";
        return SpringdocRouteBuilder.route()
            .GET("presets", this::listPresets, builder -> builder
                .operationId("ListPresetFonts")
                .description("List built-in preset fonts.")
                .tag(tag)
                .response(responseBuilder().implementation(PresetFont.class)))
            .GET("fonts", this::listFonts, builder -> builder
                .operationId("ListFonts")
                .description("List configured fonts.")
                .tag(tag)
                .response(responseBuilder().implementation(Fonts.class)))
            .PUT("fonts", this::saveFonts, builder -> builder
                .operationId("SaveFonts")
                .description("Save the whole font list.")
                .tag(tag)
                .requestBody(requestBodyBuilder()
                    .required(true)
                    .content(contentBuilder()
                        .mediaType(MediaType.APPLICATION_JSON_VALUE)
                        .schema(schemaBuilder().implementation(Fonts.class))))
                .response(responseBuilder().implementation(Fonts.class)))
            .GET("fonts/default", this::getDefaultFont, builder -> builder
                .operationId("GetDefaultFont")
                .description("Get the site-wide default font name.")
                .tag(tag)
                .response(responseBuilder().implementation(DefaultFont.class)))
            .PUT("fonts/default", this::setDefaultFont, builder -> builder
                .operationId("SetDefaultFont")
                .description("Set the site-wide default font.")
                .tag(tag)
                .requestBody(requestBodyBuilder()
                    .required(true)
                    .content(contentBuilder()
                        .mediaType(MediaType.APPLICATION_JSON_VALUE)
                        .schema(schemaBuilder().implementation(SetDefaultFontRequest.class))))
                .response(responseBuilder().implementation(Void.class)))
            .build();
    }

    private Mono<ServerResponse> listPresets(ServerRequest request) {
        return ServerResponse.ok().bodyValue(presetFontsProvider.presets());
    }

    private Mono<ServerResponse> listFonts(ServerRequest request) {
        return fontsService.fonts()
            .map(Fonts::new)
            .flatMap(fonts -> ServerResponse.ok().bodyValue(fonts));
    }

    private Mono<ServerResponse> saveFonts(ServerRequest request) {
        return request.bodyToMono(Fonts.class)
            .switchIfEmpty(Mono.error(
                () -> new IllegalArgumentException("Request body must not be empty")))
            .flatMap(this::persistFonts)
            .flatMap(fonts -> ServerResponse.ok().bodyValue(fonts));
    }

    @SuppressWarnings("deprecation")
    private Mono<Fonts> persistFonts(Fonts fonts) {
        var fontList = fonts.fonts() == null ? List.<FontItem>of() : fonts.fonts();
        var normalized = new Fonts(fontList);
        return configMap()
            .flatMap(configMap -> {
                var data = configMap.getData();
                data.put(FontsSetting.FONTS_GROUP, JsonUtils.objectToJson(normalized));
                return saveConfigMap(configMap).thenReturn(normalized);
            });
    }

    private Mono<ServerResponse> getDefaultFont(ServerRequest request) {
        return fontsService.basic()
            .map(basic -> new DefaultFont(basic.defaultFont()))
            .flatMap(result -> ServerResponse.ok().bodyValue(result));
    }

    private Mono<ServerResponse> setDefaultFont(ServerRequest request) {
        return request.bodyToMono(SetDefaultFontRequest.class)
            .switchIfEmpty(Mono.error(
                () -> new IllegalArgumentException("Request body must not be empty")))
            .flatMap(body -> {
                var name = body.name() == null || body.name().isBlank()
                    ? FontsSetting.THEME_DEFAULT : body.name();
                return persistDefaultFont(name);
            })
            .then(ServerResponse.ok().build());
    }

    @SuppressWarnings("unchecked")
    private Mono<Void> persistDefaultFont(String name) {
        return configMap()
            .flatMap(configMap -> {
                var data = configMap.getData();
                Map<String, Object> basic = new HashMap<>();
                var basicJson = data.get(FontsSetting.BASIC_GROUP);
                if (basicJson != null && !basicJson.isBlank()) {
                    basic.putAll(JsonUtils.jsonToObject(basicJson, Map.class));
                }
                basic.put("defaultFont", name);
                data.put(FontsSetting.BASIC_GROUP, JsonUtils.objectToJson(basic));
                return saveConfigMap(configMap).then();
            });
    }

    private Mono<ConfigMap> configMap() {
        return client.fetch(ConfigMap.class, FontsSetting.CONFIGMAP_NAME)
            .switchIfEmpty(Mono.defer(() -> {
                var configMap = new ConfigMap();
                var metadata = new Metadata();
                metadata.setName(FontsSetting.CONFIGMAP_NAME);
                configMap.setMetadata(metadata);
                configMap.setData(new HashMap<>());
                return Mono.just(configMap);
            }));
    }

    private Mono<ConfigMap> saveConfigMap(ConfigMap configMap) {
        if (configMap.getData() == null) {
            configMap.setData(new HashMap<>());
        }
        return configMap.getMetadata().getVersion() == null
            ? client.create(configMap)
            : client.update(configMap);
    }

    public record SetDefaultFontRequest(@Schema(requiredMode = REQUIRED) String name) {
    }

    public record DefaultFont(String name) {
    }
}

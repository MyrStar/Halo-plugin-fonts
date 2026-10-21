package top.iloved.pluginfonts;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

/**
 * 内置预设字体库提供者。
 *
 * <p>预设数据位于 classpath 的 presets/fonts.yaml，使用 SnakeYAML 解析以避免依赖
 * Jackson（Halo 2.23 起从 Jackson 2 迁移到 Jackson 3，跨版本引用存在风险）。
 * 预设为静态数据，首次访问后缓存。
 */
@Slf4j
@Component
public class PresetFontsProvider {

    private volatile List<PresetFont> presets;

    public List<PresetFont> presets() {
        var result = presets;
        if (result == null) {
            synchronized (this) {
                result = presets;
                if (result == null) {
                    result = load();
                    presets = result;
                }
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<PresetFont> load() {
        var resource = new ClassPathResource("presets/fonts.yaml");
        try (var in = resource.getInputStream()) {
            var yaml = new Yaml();
            List<Map<String, Object>> items = yaml.load(
                new String(in.readAllBytes(), StandardCharsets.UTF_8));
            if (items == null) {
                return List.of();
            }
            return items.stream()
                .map(item -> new PresetFont(
                    (String) item.get("id"),
                    (String) item.get("displayName"),
                    (String) item.get("family"),
                    (String) item.get("cssUrl"),
                    (String) item.get("category")))
                .toList();
        } catch (IOException e) {
            log.error("Failed to load preset fonts from presets/fonts.yaml", e);
            throw new UncheckedIOException(e);
        }
    }

    public record PresetFont(String id, String displayName, String family,
                             String cssUrl, String category) {
    }
}

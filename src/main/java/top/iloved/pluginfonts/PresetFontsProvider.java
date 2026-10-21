package top.iloved.pluginfonts;

import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 内置预设字体库提供者。
 *
 * <p>预设直接以 Java 代码定义，避免引入 YAML/JSON 解析依赖（Halo 2.23 起从 Jackson 2
 * 迁移到 Jackson 3，跨版本引用第三方解析库存在运行时风险）。预设均为免费可商用字体。
 */
@Component
public class PresetFontsProvider {

    private static final List<PresetFont> PRESETS = List.of(
        new PresetFont("noto-sans-sc", "思源黑体", "'Noto Sans SC', sans-serif",
            "https://fonts.googleapis.com/css2?family=Noto+Sans+SC:wght@400;500;700&display=swap",
            "黑体"),
        new PresetFont("noto-serif-sc", "思源宋体", "'Noto Serif SC', serif",
            "https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@400;600;700&display=swap",
            "宋体"),
        new PresetFont("lxgw-wenkai", "霞鹜文楷", "'LXGW WenKai Screen', sans-serif",
            "https://cdn.jsdelivr.net/npm/lxgw-wenkai-screen-webfont@1.7.0/style.css",
            "楷体"),
        new PresetFont("zcool-xiaowei", "站酷小薇体", "'ZCOOL XiaoWei', serif",
            "https://fonts.googleapis.com/css2?family=ZCOOL+XiaoWei&display=swap",
            "美术体"),
        new PresetFont("zcool-kuaile", "站酷快乐体", "'ZCOOL KuaiLe', sans-serif",
            "https://fonts.googleapis.com/css2?family=ZCOOL+KuaiLe&display=swap",
            "美术体"),
        new PresetFont("ma-shan-zheng", "马善政毛笔", "'Ma Shan Zheng', cursive",
            "https://fonts.googleapis.com/css2?family=Ma+Shan+Zheng&display=swap",
            "手写体"),
        new PresetFont("noto-sans-tc", "思源黑体·繁体", "'Noto Sans TC', sans-serif",
            "https://fonts.googleapis.com/css2?family=Noto+Sans+TC:wght@400;500;700&display=swap",
            "黑体"),
        new PresetFont("noto-serif-tc", "思源宋体·繁体", "'Noto Serif TC', serif",
            "https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@400;600;700&display=swap",
            "宋体"),
        new PresetFont("zcool-qingke-huangyou", "站酷庆科黄油体", "'ZCOOL QingKe HuangYou', sans-serif",
            "https://fonts.googleapis.com/css2?family=ZCOOL+QingKe+HuangYou&display=swap",
            "美术体"),
        new PresetFont("liu-jian-mao-cao", "刘健毛草体", "'Liu Jian Mao Cao', cursive",
            "https://fonts.googleapis.com/css2?family=Liu+Jian+Mao+Cao&display=swap",
            "手写体"),
        new PresetFont("zhi-mang-xing", "志莽行书", "'Zhi Mang Xing', cursive",
            "https://fonts.googleapis.com/css2?family=Zhi+Mang+Xing&display=swap",
            "手写体"),
        new PresetFont("long-cang", "龙藏体", "'Long Cang', cursive",
            "https://fonts.googleapis.com/css2?family=Long+Cang&display=swap",
            "手写体")
    );

    public List<PresetFont> presets() {
        return PRESETS;
    }

    public record PresetFont(String id, String displayName, String family,
                             String cssUrl, String category) {
    }
}

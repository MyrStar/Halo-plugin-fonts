package top.iloved.pluginfonts;

import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 内置预设字体库提供者。
 *
 * <p>预设直接以 Java 代码定义，避免引入 YAML/JSON 解析依赖（Halo 2.23 起从 Jackson 2
 * 迁移到 Jackson 3，跨版本引用第三方解析库存在运行时风险）。预设均为免费可商用字体。
 *
 * <p>{@code weight} 为该字体的推荐字重，添加预置字体时作为初始值，可在字体管理中单独调整；
 * 空串表示跟随字体默认字重。
 */
@Component
public class PresetFontsProvider {

    private static final List<PresetFont> PRESETS = List.of(
        new PresetFont("noto-sans-sc", "思源黑体", "'Noto Sans SC', sans-serif",
            "https://fonts.googleapis.com/css2?family=Noto+Sans+SC:wght@400;500;700&display=swap",
            "黑体", ""),
        new PresetFont("noto-serif-sc", "思源宋体", "'Noto Serif SC', serif",
            "https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@400;600;700&display=swap",
            "宋体", ""),
        new PresetFont("lxgw-wenkai", "霞鹜文楷", "'LXGW WenKai Screen', sans-serif",
            "https://cdn.jsdelivr.net/npm/lxgw-wenkai-screen-webfont@1.7.0/style.css",
            "楷体", ""),
        new PresetFont("zcool-xiaowei", "站酷小薇体", "'ZCOOL XiaoWei', serif",
            "https://fonts.googleapis.com/css2?family=ZCOOL+XiaoWei&display=swap",
            "美术体", ""),
        new PresetFont("zcool-kuaile", "站酷快乐体", "'ZCOOL KuaiLe', sans-serif",
            "https://fonts.googleapis.com/css2?family=ZCOOL+KuaiLe&display=swap",
            "美术体", ""),
        new PresetFont("ma-shan-zheng", "马善政毛笔", "'Ma Shan Zheng', cursive",
            "https://fonts.googleapis.com/css2?family=Ma+Shan+Zheng&display=swap",
            "手写体", "500"),
        new PresetFont("noto-sans-tc", "思源黑体·繁体", "'Noto Sans TC', sans-serif",
            "https://fonts.googleapis.com/css2?family=Noto+Sans+TC:wght@400;500;700&display=swap",
            "黑体", ""),
        new PresetFont("noto-serif-tc", "思源宋体·繁体", "'Noto Serif TC', serif",
            "https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@400;600;700&display=swap",
            "宋体", ""),
        new PresetFont("zcool-qingke-huangyou", "站酷庆科黄油体", "'ZCOOL QingKe HuangYou', sans-serif",
            "https://fonts.googleapis.com/css2?family=ZCOOL+QingKe+HuangYou&display=swap",
            "美术体", ""),
        new PresetFont("liu-jian-mao-cao", "刘健毛草体", "'Liu Jian Mao Cao', cursive",
            "https://fonts.googleapis.com/css2?family=Liu+Jian+Mao+Cao&display=swap",
            "手写体", "500"),
        new PresetFont("zhi-mang-xing", "志莽行书", "'Zhi Mang Xing', cursive",
            "https://fonts.googleapis.com/css2?family=Zhi+Mang+Xing&display=swap",
            "手写体", "500"),
        new PresetFont("long-cang", "龙藏体", "'Long Cang', cursive",
            "https://fonts.googleapis.com/css2?family=Long+Cang&display=swap",
            "手写体", "500")
    );

    /**
     * 推荐字体库：来自 cn-fontsource（中文网字计划，按 unicode-range 分包）与 npm CDN
     * 的免费可商用字体。与内置预设不同，推荐字体添加后为「自定义」来源，名称与地址
     * 均可修改（CDN 地址可能变动，允许用户自行换源）。
     */
    private static final List<PresetFont> RECOMMENDED = List.of(
        new PresetFont("rec-smiley-sans", "得意黑", "'Smiley Sans Oblique', sans-serif",
            "https://cdn.jsdelivr.net/npm/cn-fontsource-smiley-sans-oblique-regular@1.0.1/font.css",
            "美术体", ""),
        new PresetFont("rec-alimama-dongfangdakai", "阿里妈妈东方大楷", "'Alimama DongFangDaKai', serif",
            "https://cdn.jsdelivr.net/npm/cn-fontsource-alimama-dong-fang-da-kai-regular@1.0.3/font.css",
            "书法体", ""),
        new PresetFont("rec-dingtalk-jinbuti", "钉钉进步体", "'DingTalk JinBuTi', sans-serif",
            "https://cdn.jsdelivr.net/npm/cn-fontsource-ding-talk-jin-bu-ti-regular@1.0.3/font.css",
            "黑体", ""),
        new PresetFont("rec-lxgw-neo-xihei", "霞鹜新晰黑", "'LXGW Neo XiHei', sans-serif",
            "https://cdn.jsdelivr.net/npm/cn-fontsource-lxgw-neo-xi-hei-regular@1.0.1/font.css",
            "黑体", ""),
        new PresetFont("rec-lxgw-marker-gothic", "霞鹜漫黑", "'LXGW Marker Gothic', sans-serif",
            "https://cdn.jsdelivr.net/npm/cn-fontsource-lxgw-marker-gothic-regular@1.0.1/font.css",
            "黑体", ""),
        new PresetFont("rec-yozai", "悠哉字体", "'Yozai', cursive",
            "https://cdn.jsdelivr.net/npm/cn-fontsource-yozai-regular@1.0.9/font.css",
            "手写体", ""),
        new PresetFont("rec-xiaolai", "小赖字体", "'Xiaolai SC', cursive",
            "https://cdn.jsdelivr.net/npm/cn-fontsource-xiaolai-sc-regular@1.0.2/font.css",
            "手写体", ""),
        new PresetFont("rec-honglei-xingshu", "鸿雷行书", "'hongleixingshu', cursive",
            "https://cdn.jsdelivr.net/npm/cn-fontsource-hongleixingshu-regular@1.0.7/font.css",
            "手写体", "500"),
        new PresetFont("rec-maoken-zhuyuan", "猫啃珠圆体", "'MaokenZhuyuanTi', sans-serif",
            "https://cdn.jsdelivr.net/npm/cn-fontsource-maoken-zhuyuan-ti-regular@1.0.1/font.css",
            "圆体", ""),
        new PresetFont("rec-975-maru", "975 圆体", "'975Maru SC', sans-serif",
            "https://cdn.jsdelivr.net/npm/cn-fontsource-975-maru-sc-regular@1.0.1/font.css",
            "圆体", ""),
        new PresetFont("rec-misans", "MiSans", "'MiSans', sans-serif",
            "https://cdn.jsdelivr.net/npm/misans@5.0.0/lib/Normal/MiSans-Regular.min.css",
            "黑体", ""),
        new PresetFont("rec-lxgw-wenkai", "霞鹜文楷（原版）", "'LXGW WenKai', serif",
            "https://cdn.jsdelivr.net/npm/lxgw-wenkai-webfont@1.7.0/lxgwwenkai-regular.css",
            "楷体", "")
    );

    public List<PresetFont> presets() {
        return PRESETS;
    }

    public List<PresetFont> recommended() {
        return RECOMMENDED;
    }

    public record PresetFont(String id, String displayName, String family,
                             String cssUrl, String category, String weight) {
    }
}

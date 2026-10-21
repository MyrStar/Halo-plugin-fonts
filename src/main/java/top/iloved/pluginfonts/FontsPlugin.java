package top.iloved.pluginfonts;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import run.halo.app.plugin.BasePlugin;
import run.halo.app.plugin.PluginContext;

/**
 * 字体管理插件：全局管理站点字体，支持字体加速地址替换与前台访客字体切换浮窗。
 */
@Slf4j
@Component
public class FontsPlugin extends BasePlugin {

    public FontsPlugin(PluginContext pluginContext) {
        super(pluginContext);
    }

    @Override
    public void start() {
        log.info("Fonts plugin started.");
    }

    @Override
    public void stop() {
        log.info("Fonts plugin stopped.");
    }
}

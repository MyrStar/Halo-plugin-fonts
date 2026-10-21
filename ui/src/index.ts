import { definePlugin } from "@halo-dev/ui-shared";
import { markRaw } from "vue";
import TablerTypography from "~icons/tabler/typography";
import FontManager from "./views/FontManager.vue";

export default definePlugin({
  components: {},
  routes: [
    {
      parentName: "Root",
      route: {
        path: "/fonts",
        name: "PluginFonts",
        component: FontManager,
        meta: {
          title: "字体管理",
          permissions: ["plugin:fonts:manage"],
          menu: {
            name: "字体管理",
            group: "interface",
            icon: markRaw(TablerTypography),
          },
        },
      },
    },
  ],
  extensionPoints: {},
});

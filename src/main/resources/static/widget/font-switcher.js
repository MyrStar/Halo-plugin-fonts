/**
 * Halo 字体管理插件 - 前台字体切换浮窗
 * 无第三方依赖，数据来自插件公开 API，访客选择存储在 localStorage。
 */
(function () {
  "use strict";

  var API_URL = "/apis/api.pluginfonts.halo.run/v1alpha1/fonts";
  var STORAGE_KEY = "halo-plugin-fonts:selected";
  var CSS_VARIABLE = "--halo-plugin-fonts-family";
  var DYNAMIC_STYLE_ID = "halo-plugin-fonts-dynamic-override";
  var WIDGET_ID = "halo-plugin-fonts-widget";

  fetch(API_URL, { credentials: "same-origin" })
    .then(function (res) {
      if (!res.ok) throw new Error("unexpected status " + res.status);
      return res.json();
    })
    .then(init)
    .catch(function () {
      // 接口不可用时静默降级，不展示浮窗
    });

  function init(payload) {
    if (!payload || !payload.widget || !payload.widget.enabled) return;
    var fonts = payload.fonts || [];
    if (!fonts.length) return;

    var saved = null;
    try {
      saved = localStorage.getItem(STORAGE_KEY);
    } catch (e) {}

    var current = fonts.some(function (f) { return f.name === saved; }) ? saved : null;
    if (current) {
      applyFont(payload, current);
    } else if (saved) {
      try { localStorage.removeItem(STORAGE_KEY); } catch (e) {}
    }

    buildWidget(payload, current);
  }

  /** 切换字体：只改 CSS 变量，必要时补充覆盖规则（站点默认字体为跟随主题时） */
  function applyFont(payload, name) {
    var font = null;
    (payload.fonts || []).forEach(function (f) {
      if (f.name === name) font = f;
    });

    if (!font) {
      // 重置为站点默认
      document.documentElement.style.removeProperty(CSS_VARIABLE);
      var dynamic = document.getElementById(DYNAMIC_STYLE_ID);
      if (dynamic) dynamic.remove();
      try { localStorage.removeItem(STORAGE_KEY); } catch (e) {}
      return;
    }

    ensureOverrideRule(payload);
    document.documentElement.style.setProperty(CSS_VARIABLE, font.family);
    try { localStorage.setItem(STORAGE_KEY, name); } catch (e) {}
  }

  /** 若页面没有插件注入的覆盖规则（默认字体为跟随主题），由浮窗动态补充 */
  function ensureOverrideRule(payload) {
    if (document.getElementById(DYNAMIC_STYLE_ID)) return;
    var selectors = (payload.applySelectors && payload.applySelectors.length)
      ? payload.applySelectors.join(",") : "body";
    var weightRule = payload.fontWeight
      ? "font-weight:" + payload.fontWeight + " !important;" : "";
    var style = document.createElement("style");
    style.id = DYNAMIC_STYLE_ID;
    style.textContent = ":where(" + selectors + "){font-family:var("
      + CSS_VARIABLE + ") !important;" + weightRule + "}";
    document.head.appendChild(style);
  }

  function buildWidget(payload, current) {
    if (document.getElementById(WIDGET_ID)) return;

    var root = document.createElement("div");
    root.id = WIDGET_ID;
    root.className = "hpf-widget hpf-" + (payload.widget.position || "bottom-right");

    var button = document.createElement("button");
    button.type = "button";
    button.className = "hpf-trigger";
    button.title = payload.widget.title || "切换字体";
    button.setAttribute("aria-label", payload.widget.title || "切换字体");
    button.innerHTML = "<span class=\"hpf-trigger-icon\">Aa</span>";

    var panel = document.createElement("div");
    panel.className = "hpf-panel";
    panel.hidden = true;

    var panelTitle = document.createElement("div");
    panelTitle.className = "hpf-panel-title";
    panelTitle.textContent = payload.widget.title || "切换字体";
    panel.appendChild(panelTitle);

    var list = document.createElement("ul");
    list.className = "hpf-list";

    function markActive(name) {
      Array.prototype.forEach.call(list.children, function (li) {
        li.classList.toggle("hpf-active", li.dataset.fontName === (name || "__default__"));
      });
    }

    var defaultItem = document.createElement("li");
    defaultItem.dataset.fontName = "__default__";
    defaultItem.textContent = "站点默认";
    defaultItem.addEventListener("click", function () {
      applyFont(payload, null);
      markActive(null);
      toggle(false);
    });
    list.appendChild(defaultItem);

    payload.fonts.forEach(function (font) {
      var item = document.createElement("li");
      item.dataset.fontName = font.name;
      item.textContent = font.displayName || font.name;
      item.style.fontFamily = font.family;
      item.addEventListener("click", function () {
        applyFont(payload, font.name);
        markActive(font.name);
        toggle(false);
      });
      list.appendChild(item);
    });

    panel.appendChild(list);
    markActive(current);

    var open = false;
    function toggle(force) {
      open = typeof force === "boolean" ? force : !open;
      panel.hidden = !open;
      button.classList.toggle("hpf-open", open);
    }

    button.addEventListener("click", function (e) {
      e.stopPropagation();
      toggle();
    });
    document.addEventListener("click", function (e) {
      if (open && !root.contains(e.target)) toggle(false);
    });
    document.addEventListener("keydown", function (e) {
      if (open && e.key === "Escape") toggle(false);
    });

    root.appendChild(panel);
    root.appendChild(button);
    document.body.appendChild(root);
  }
})();

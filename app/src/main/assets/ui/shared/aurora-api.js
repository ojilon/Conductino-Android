/*
 * Conductino frontend <-> Java bridge helper.
 */
(function () {
  const hasNative = typeof window.AuroraNative !== 'undefined';

  window.Aurora = {
    ready(stateName) {
      if (hasNative) window.AuroraNative.uiReady(stateName);
    },
    submit(text) {
      if (hasNative) window.AuroraNative.submitOmnibox(text);
    },
    selectEngine(id) {
      if (hasNative) window.AuroraNative.selectSearchEngine(id);
    },
    setTheme(id) {
      if (hasNative) window.AuroraNative.setTheme(id);
    },
    open(url) {
      if (hasNative) window.AuroraNative.openResult(url);
    },
    suggest(partial) {
      return hasNative ? window.AuroraNative.suggest(partial) : '[]';
    },
    devtools() {
      if (hasNative) window.AuroraNative.openDevTools();
    },

    _handlers: {},
    on(event, cb) { this._handlers[event] = cb; },
    onEvent(event, payload) {
      const h = this._handlers[event];
      if (h) h(payload);
    },
  };
})();

/*
 * Aurora frontend <-> Java bridge helper.
 * Every UI state includes this. It keeps the JS thin: collect input, call
 * Java (window.AuroraNative), and render whatever Java emits back.
 */
(function () {
  const hasNative = typeof window.AuroraNative !== 'undefined';

  window.Aurora = {
    // --- calls INTO Java ---
    ready(stateName) {
      if (hasNative) window.AuroraNative.uiReady(stateName);
    },
    submit(text) {
      if (hasNative) window.AuroraNative.submitOmnibox(text);
    },
    selectEngine(id) {
      if (hasNative) window.AuroraNative.selectSearchEngine(id);
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

    // --- events FROM Java (Java calls window.Aurora.onEvent) ---
    _handlers: {},
    on(event, cb) { this._handlers[event] = cb; },
    onEvent(event, payload) {
      const h = this._handlers[event];
      if (h) h(payload);
    },
  };
})();

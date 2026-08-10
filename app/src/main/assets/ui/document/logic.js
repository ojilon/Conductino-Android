document.addEventListener('DOMContentLoaded', () => {
  Aurora.ready('document');

  Aurora.on('payload', (d) => {
    if (!d) return;
    const title = document.getElementById('title');
    const url = document.getElementById('url');
    const body = document.getElementById('body');
    if (title) title.textContent = d.title || 'Untitled';
    if (url) url.textContent = d.url || '';
    if (body) body.textContent = d.text || '(no text extracted)';
  });
});

document.addEventListener('DOMContentLoaded', () => {
  Aurora.ready('document');
  const bar = document.getElementById('progress');
  bar.style.width = '35%';

  Aurora.on('payload', (data) => {
    const q = document.getElementById('q');
    const page = document.getElementById('page');
    q.value = data.url || '';
    if (data.media) {
      page.innerHTML = '';
      const img = document.createElement('img');
      img.src = data.url; img.style.maxWidth = '100%';
      page.appendChild(img);
    } else {
      // Java hands us the downloaded HTML; the WebView paints it while
      // Java streams referenced resources through shouldInterceptRequest.
      page.innerHTML = data.html || '';
    }
    bar.classList.add('done');
  });

  document.getElementById('back').onclick = () => history.back();
  document.getElementById('dev').onclick = () => Aurora.devtools();
  document.getElementById('omni').addEventListener('submit', (e) => {
    e.preventDefault();
    const v = document.getElementById('q').value.trim();
    if (v) Aurora.submit(v);
  });
});

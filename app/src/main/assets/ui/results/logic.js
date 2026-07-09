document.addEventListener('DOMContentLoaded', () => {
  Aurora.ready('results');

  // Java pushes the parsed result set (capped at 300) here.
  Aurora.on('payload', (data) => {
    const q = document.getElementById('q');
    const meta = document.getElementById('meta');
    const list = document.getElementById('results');
    q.value = data.query || '';
    const items = data.items || [];
    meta.textContent = items.length + ' results';
    list.innerHTML = '';
    items.forEach((it, i) => {
      const li = document.createElement('li');
      li.className = 'result';
      li.style.animationDelay = Math.min(i * 20, 400) + 'ms';
      li.innerHTML =
        '<div class="url"></div><h3></h3><p></p>';
      li.querySelector('.url').textContent = it.url;
      li.querySelector('h3').textContent = it.title;
      li.querySelector('p').textContent = it.snippet;
      li.onclick = () => Aurora.open(it.url);
      list.appendChild(li);
    });
  });

  document.getElementById('omni').addEventListener('submit', (e) => {
    e.preventDefault();
    const v = document.getElementById('q').value.trim();
    if (v) Aurora.submit(v);
  });
});

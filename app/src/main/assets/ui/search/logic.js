const ENGINES = [
  { id: 'duckduckgo', name: 'DuckDuckGo' },
  { id: 'google', name: 'Google' },
  { id: 'brave', name: 'Brave' },
];
let active = 'duckduckgo';

document.addEventListener('DOMContentLoaded', () => {
  Aurora.ready('search');

  const bar = document.getElementById('engines');
  ENGINES.forEach((e) => {
    const b = document.createElement('button');
    b.textContent = e.name;
    if (e.id === active) b.classList.add('active');
    b.onclick = () => {
      active = e.id;
      Aurora.selectEngine(e.id);
      [...bar.children].forEach((c) => c.classList.remove('active'));
      b.classList.add('active');
    };
    bar.appendChild(b);
  });

  const q = document.getElementById('q');
  const list = document.getElementById('suggestions');
  q.addEventListener('input', () => {
    const raw = Aurora.suggest(q.value);
    let items = [];
    try { items = JSON.parse(raw); } catch (_) {}
    list.innerHTML = '';
    (items || []).slice(0, 8).forEach((s) => {
      const li = document.createElement('li');
      li.innerHTML = '<span class="k">⌕</span>' + s;
      li.onclick = () => Aurora.submit(s);
      list.appendChild(li);
    });
  });

  document.getElementById('omni').addEventListener('submit', (e) => {
    e.preventDefault();
    if (q.value.trim()) Aurora.submit(q.value.trim());
  });
});

document.addEventListener('DOMContentLoaded', () => {
  Aurora.ready('welcome');

  const sc = document.getElementById('shortcuts');
  const items = [
    { label: 'Wikipedia', q: 'Wikipedia' },
    { label: 'GitHub', q: 'GitHub' },
    { label: 'arXiv', q: 'arxiv.org' },
    { label: 'Scholar', q: 'Google Scholar' }
  ];

  items.forEach(({ label, q }) => {
    const a = document.createElement('a');
    a.textContent = label;
    a.href = '#';
    a.setAttribute('role', 'button');
    a.onclick = (e) => {
      e.preventDefault();
      Aurora.submit(q);
    };
    sc.appendChild(a);
  });

  document.getElementById('omni').addEventListener('submit', (e) => {
    e.preventDefault();
    const v = document.getElementById('q').value.trim();
    if (v) Aurora.submit(v);
  });

  const dev = document.getElementById('dev');
  if (dev) {
    dev.onclick = () => Aurora.devtools();
  }
});

document.addEventListener('DOMContentLoaded', () => {
  Aurora.ready('welcome');

  const sc = document.getElementById('shortcuts');
  ['Wikipedia', 'GitHub', 'News', 'Maps'].forEach((name) => {
    const a = document.createElement('a');
    a.textContent = name;
    a.href = '#';
    a.onclick = (e) => { e.preventDefault(); Aurora.submit(name); };
    sc.appendChild(a);
  });

  document.getElementById('omni').addEventListener('submit', (e) => {
    e.preventDefault();
    const v = document.getElementById('q').value.trim();
    if (v) Aurora.submit(v);
  });

  document.getElementById('dev').onclick = () => Aurora.devtools();
});

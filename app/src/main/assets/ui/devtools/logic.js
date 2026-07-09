document.addEventListener('DOMContentLoaded', () => {
  Aurora.ready('devtools');

  document.querySelectorAll('.tabs button').forEach((b) => {
    b.onclick = () => {
      document.querySelectorAll('.tabs button').forEach((x) => x.classList.remove('active'));
      document.querySelectorAll('.panel').forEach((x) => x.classList.remove('active'));
      b.classList.add('active');
      document.getElementById(b.dataset.tab).classList.add('active');
    };
  });

  // Java streams log lines + network entries here via events.
  Aurora.on('log', (line) => {
    const el = document.getElementById('log');
    el.textContent += line + '\n';
  });
  Aurora.on('net', (entry) => {
    const row = document.getElementById('net').insertRow();
    row.insertCell().textContent = entry.method || 'GET';
    row.insertCell().textContent = entry.status || '';
    row.insertCell().textContent = entry.url || '';
  });
});

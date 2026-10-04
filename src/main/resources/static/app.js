const API_BASE = '/api';
let queueChart = null;

document.addEventListener('DOMContentLoaded', () => {
    loadPorts();
    setupFileUpload();
    setInterval(loadPorts, 60000);
});

async function loadPorts() {
    try {
        const response = await fetch(`${API_BASE}/congestion/summary`);
        if (!response.ok) throw new Error('Failed to load ports');
        const ports = await response.json();
        renderPorts(ports);
    } catch (err) {
        console.error(err);
        document.getElementById('portsGrid').innerHTML = `<div class="error">Failed to load ports: ${err.message}</div>`;
    }
}

function renderPorts(ports) {
    const grid = document.getElementById('portsGrid');
    grid.innerHTML = ports.map(port => `
        <div class="card port-card ${port.isCongested ? `congested-${port.severity.toLowerCase()}` : ''}" onclick="showChart(${port.portId}, '${port.portName.replace(/'/g, "\\'")}')">
            <div class="port-header">
                <span class="port-name">${port.portName}</span>
                <span class="badge badge-${port.severity.toLowerCase()}">${port.severity}</span>
            </div>
            <div style="color: #6c757d; font-size: 0.9rem; margin-bottom: 10px;">${port.unLocode} • ${port.state} • ${port.berthCount} berths</div>
            <div class="stats">
                <div class="stat"><div class="stat-value">${port.vesselsInQueue}</div><div class="stat-label">Vessels in Queue</div></div>
                <div class="stat"><div class="stat-value">${port.avgDwellHours ? port.avgDwellHours.toFixed(1) : 0}</div><div class="stat-label">Avg Dwell (hrs)</div></div>
                <div class="stat"><div class="stat-value">${port.berthCount}</div><div class="stat-label">Berths</div></div>
            </div>
        </div>
    `).join('');
}

async function showChart(portId, portName) {
    document.getElementById('chartCard').style.display = 'block';
    document.getElementById('chartPortName').textContent = portName;
    try {
        const response = await fetch(`${API_BASE}/congestion/history/${portId}?hoursBack=168`);
        const events = await response.json();
        renderChart(events);
        document.getElementById('chartCard').scrollIntoView({ behavior: 'smooth' });
    } catch (err) { console.error(err); }
}

function renderChart(events) {
    const ctx = document.getElementById('queueChart').getContext('2d');
    const labels = events.map(e => new Date(e.startTime).toLocaleString());
    const data = events.map(e => e.vesselsInQueue);
    const colors = events.map(e => e.severity === 'HIGH' ? '#dc3545' : e.severity === 'MEDIUM' ? '#fd7e14' : '#ffc107');
    if (queueChart) queueChart.destroy();
    queueChart = new Chart(ctx, {
        type: 'bar',
        data: { labels: labels.reverse(), datasets: [{ label: 'Vessels in Queue', data: data.reverse(), backgroundColor: colors.reverse(), borderRadius: 4 }] },
        options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false } }, scales: { y: { beginAtZero: true, title: { display: true, text: 'Vessels' } }, x: { title: { display: true, text: 'Time' } } } }
    });
}

function setupFileUpload() {
    const area = document.getElementById('uploadArea');
    const input = document.getElementById('csvFile');
    const progress = document.getElementById('progressBar');
    const fill = document.getElementById('progressFill');
    const errorDiv = document.getElementById('uploadError');

    ['dragenter', 'dragover'].forEach(evt => area.addEventListener(evt, e => { e.preventDefault(); e.stopPropagation(); area.classList.add('dragover'); }));
    ['dragleave', 'drop'].forEach(evt => area.addEventListener(evt, e => { e.preventDefault(); e.stopPropagation(); area.classList.remove('dragover'); }));
    area.addEventListener('drop', e => handleFiles(e.dataTransfer.files));
    input.addEventListener('change', e => handleFiles(e.target.files));

    async function handleFiles(files) {
        if (!files.length) return;
        errorDiv.style.display = 'none';
        progress.style.display = 'block';
        fill.style.width = '0%';

        for (let i = 0; i < files.length; i++) {
            const file = files[i];
            const formData = new FormData();
            formData.append('file', file);
            try {
                fill.style.width = `${(i / files.length) * 100}%`;
                const response = await fetch(`${API_BASE}/ingest`, { method: 'POST', body: formData });
                if (!response.ok) { const err = await response.json(); throw new Error(err.message || 'Upload failed'); }
                const result = await response.json();
                console.log(`Uploaded ${file.name}:`, result);
            } catch (err) {
                errorDiv.textContent = `Error uploading ${file.name}: ${err.message}`;
                errorDiv.style.display = 'block';
                break;
            }
        }
        fill.style.width = '100%';
        setTimeout(() => { progress.style.display = 'none'; loadPorts(); }, 1000);
    }
}
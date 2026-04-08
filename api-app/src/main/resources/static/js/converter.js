const MAX_FILE_COUNT = 3;
const MAX_FILE_SIZE_MB = 20;
const MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024;
const ALLOWED_EXTENSIONS = ['png', 'jpg', 'jpeg', 'gif', 'bmp'];

let selectedFiles = [];
let fileEventSource = null;
let statsEventSource = null;

function getFileExtension(filename) {
    const parts = filename.split('.');
    return parts.length > 1 ? parts.pop().toLowerCase() : '';
}

function formatBytes(bytes) {
    const value = Number(bytes || 0);
    if (value < 1024) return `${value} B`;
    if (value < 1024 * 1024) return `${(value / 1024).toFixed(1)} KB`;
    if (value < 1024 * 1024 * 1024) return `${(value / (1024 * 1024)).toFixed(1)} MB`;
    return `${(value / (1024 * 1024 * 1024)).toFixed(1)} GB`;
}

function getOrCreateUuid() {
    let uuid = localStorage.getItem('uuid');
    if (!uuid) {
        uuid = crypto.randomUUID();
        localStorage.setItem('uuid', uuid);
    }
    return uuid;
}

// 브라우저 내부 파일 객체 동기화
function syncFileInputWithSelectedFiles() {
    const input = document.getElementById('fileInput');
    const dt = new DataTransfer();
    selectedFiles.forEach(file => dt.items.add(file));
    input.files = dt.files;
}


function validateFiles(files) {
    const validFiles = [];
    const errors = [];
    let hasInvalidExtension = false;

    if (files.length > MAX_FILE_COUNT) {
        errors.push(`파일은 최대 ${MAX_FILE_COUNT}개까지 업로드할 수 있습니다.`);
    }

    files.forEach(file => {
        const ext = getFileExtension(file.name);
        if (!ALLOWED_EXTENSIONS.includes(ext)) {
            errors.push(`[${file.name}] - 허용되지 않는 확장자입니다.`);
            hasInvalidExtension = true;
            return;
        }
        if (file.size > MAX_FILE_SIZE_BYTES) {
            errors.push(`[${file.name}] - 용량 초과 (${MAX_FILE_SIZE_MB}MB 제한)`);
            return;
        }
        if (validFiles.length < MAX_FILE_COUNT) {
            validFiles.push(file);
        }
    });
    return { validFiles, errors, hasInvalidExtension };
}

function updateFileList() {
    const input = document.getElementById('fileInput');
    const { validFiles, errors, hasInvalidExtension } = validateFiles(Array.from(input.files));
    selectedFiles = validFiles;
    syncFileInputWithSelectedFiles();

    if (errors.length > 0) {
        let msg = hasInvalidExtension ? "이미지 파일(PNG, JPG, GIF, BMP)만 가능합니다.\n\n" : "";
        alert(msg + errors.join('\n'));
    }
    renderSelectedFileList();
}

function renderSelectedFileList() {
    const list = document.getElementById('fileList');
    list.innerHTML = '';
    if (selectedFiles.length === 0) {
        list.innerHTML = `<li class="list-group-item text-muted text-center border-0" style="background:transparent">파일을 선택하세요</li>`;
        return;
    }
    selectedFiles.forEach((file, index) => {
        const li = document.createElement('li');
        li.className = 'list-group-item border-0 py-2';
        li.style.background = 'transparent';
        li.innerHTML = `
            <div class="selected-file-row">
                <div class="selected-file-info">
                    <span class="selected-file-name">${file.name}</span>
                    <span class="selected-file-meta">${formatBytes(file.size)}</span>
                </div>
                <button type="button" class="selected-file-remove-btn" onclick="window.removeSelectedFile(${index})">×</button>
            </div>
        `;
        list.appendChild(li);
    });
}

// HTML에 직접 문자열로 들어가는 이벤트 함수이므로 전역 공간(window)에 노출시켜 안전하게 호출
window.removeSelectedFile = function(index) {
    selectedFiles.splice(index, 1);
    syncFileInputWithSelectedFiles();
    renderSelectedFileList();
};

window.handleDownloadRequest = async (key) => {
    if (!key) return;
    try {
        const res = await fetch(`/api/file/download-url?key=${encodeURIComponent(key.trim())}`);
        const url = await res.text();
        const a = document.createElement('a');
        a.href = url;
        a.click();
    } catch (e) { alert("다운로드 실패"); }
};

function updateStatusUI(data) {
    if (data.uuid !== getOrCreateUuid()) return;
    const row = document.querySelector(`tr[data-s3-filename="${data.fileName || data.s3FileName}"]`);
    if (row) {
        const statusCell = row.querySelector('.status-cell');
        const progressBar = row.querySelector('.progress-bar');
        const percentText = row.querySelector('.percent-text');
        const messageCell = row.querySelector('.message-cell');

        let badge = "bg-secondary", text = "대기 중";
        if (data.status === "2") { text = "진행 중"; badge = "bg-primary"; }
        else if (data.status === "3") { text = "완료"; badge = "bg-success"; }
        else if (data.status === "9") { text = "실패"; badge = "bg-danger"; }

        statusCell.innerHTML = `<span class="badge ${badge}">${text}</span>`;
        if (progressBar && data.percent !== undefined) {
            progressBar.style.setProperty('width', data.percent + '%', 'important');
            percentText.innerText = data.percent + "%";
        }
        if (text === "완료" && messageCell) {
            messageCell.innerHTML = `<span class="download-link" onclick="window.handleDownloadRequest('${data.convertedFile}')" style="color: #a5b4fc; cursor: pointer; text-decoration: underline; font-weight: bold;">파일 다운로드</span>`;
        }
        row.classList.add('update-highlight');
        setTimeout(() => row.classList.remove('update-highlight'), 1000);
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const uuid = getOrCreateUuid();
    const fES = new EventSource(`/api/sse/connect/${uuid}`);
    fES.addEventListener('redis-caching-update', (e) => updateStatusUI(JSON.parse(e.data)));

    const sES = new EventSource('/api/sse/stats');
    sES.addEventListener('stats-summary-update', (e) => {
        const s = JSON.parse(e.data);
        document.getElementById('globalCompletedCount').textContent = Number(s.completedCount || 0).toLocaleString();
        document.getElementById('globalCompletedBytes').textContent = formatBytes(s.completedBytes || 0);
    });

    document.getElementById('fileInput').addEventListener('change', updateFileList);
    document.getElementById('uploadForm').addEventListener('submit', async function(e) {
        e.preventDefault();
        const btn = document.getElementById('submitBtn');
        const files = [...selectedFiles];
        if (files.length === 0) return;

        btn.disabled = true; btn.innerText = "처리 중...";
        try {
            const initRes = await fetch('/file/upload-init', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ uuid, targetFormat: 'PDF', files: files.map(f => ({ filename: f.name, contentType: f.type || "image/png", size: f.size })) })
            });
            const initData = await initRes.json();
            for (let i = 0; i < initData.items.length; i++) {
                await fetch(initData.items[i].uploadUrl, { method: 'PUT', body: files[i] });
            }
            const compRes = await fetch('/file/upload-complete', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ uuid, historyIds: initData.items.map(x => x.historyId) })
            });
            const newH = await compRes.json();
            const tbody = document.getElementById('status-tbody');
            if (document.getElementById('empty-row')) document.getElementById('empty-row').remove();

            newH.forEach(h => {
                const tr = document.createElement('tr');
                tr.className = 'history-row update-highlight';
                tr.setAttribute('data-s3-filename', h.s3FileName);
                tr.innerHTML = `
                    <td class="text-truncate">${h.fileName || h.originalFile}</td>
                    <td class="status-cell text-center"><span class="badge bg-secondary">대기 중</span></td>
                    <td class="progress-cell"><div class="progress" style="height: 8px; background: rgba(255,255,255,0.1);"><div class="progress-bar bg-info" style="width: 0%"></div></div><small class="percent-text d-block text-center mt-1" style="font-size: 0.7rem;">0%</small></td>
                    <td class="message-cell small text-end text-secondary" style="padding-right: 24px;">-</td>
                `;
                tbody.prepend(tr);
            });
            this.reset(); selectedFiles = []; renderSelectedFileList();
        } catch (err) { alert("업로드 오류"); } finally { btn.disabled = false; btn.innerText = "변환 시작"; }
    });
});
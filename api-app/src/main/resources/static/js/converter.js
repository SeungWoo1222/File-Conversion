// --- 1. 전역 상태 및 상수 설정 ---
const MAX_FILE_COUNT = 3;
const MAX_FILE_SIZE_MB = 20;
const MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024;
const ALLOWED_EXTENSIONS = ['png', 'jpg', 'jpeg', 'gif', 'bmp'];

let selectedFiles = [];
let fileEventSource = null;
let statsEventSource = null;

// --- 2. 유틸리티 함수 ---
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

// --- 3. UI 및 파일 검증 로직 ---
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
    const rawFiles = Array.from(input.files);

    const { validFiles, errors, hasInvalidExtension } = validateFiles(rawFiles);

    selectedFiles = validFiles;
    syncFileInputWithSelectedFiles();

    if (errors.length > 0) {
        let alertMsg = hasInvalidExtension ? "이미지 파일(PNG, JPG, GIF, BMP)만 선택 가능합니다.\n\n" : "";
        alert(alertMsg + errors.join('\n'));
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

window.handleDownloadRequest = async function(key) {
    if (!key) { alert("파일이 준비되지 않았습니다."); return; }
    try {
        const response = await fetch(`/api/file/download-url?key=${encodeURIComponent(key.trim())}`);
        if (!response.ok) throw new Error("서버 에러");
        const downloadUrl = await response.text();
        const link = document.createElement('a');
        link.href = downloadUrl;
        link.click();
    } catch (error) {
        alert("다운로드 중 오류가 발생했습니다.");
    }
};

// --- 4. SSE (실시간 상태) 통신 로직 ---
function initSse(uuid) {
    if (fileEventSource) fileEventSource.close();
    fileEventSource = new EventSource(`/api/sse/connect/${uuid}`);
    fileEventSource.addEventListener('redis-caching-update', (event) => {
        updateStatusUI(JSON.parse(event.data));
    });
}

function initStatsSse() {
    if (statsEventSource) statsEventSource.close();
    statsEventSource = new EventSource('/api/sse/stats');
    statsEventSource.addEventListener('stats-summary-update', (event) => {
        updateStatsSummaryUI(JSON.parse(event.data));
    });
}

function updateStatsSummaryUI(summary) {
    document.getElementById('globalCompletedCount').textContent = Number(summary.completedCount || 0).toLocaleString();
    document.getElementById('globalCompletedBytes').textContent = formatBytes(summary.completedBytes || 0);
}

function updateStatusUI(data) {
    const myUuid = getOrCreateUuid();
    if (data.uuid !== myUuid) return;

    const fileId = data.fileName || data.s3FileName;
    const row = document.querySelector(`tr[data-s3-filename="${fileId}"]`);

    if (row) {
        const statusCell = row.querySelector('.status-cell');
        const progressBar = row.querySelector('.progress-bar');
        const percentText = row.querySelector('.percent-text');
        const messageCell = row.querySelector('.message-cell');

        let badgeClass = "bg-secondary";
        let statusText = "대기 중";

        if (data.status === "2") { statusText = "진행 중"; badgeClass = "bg-primary"; }
        else if (data.status === "3") { statusText = "완료"; badgeClass = "bg-success"; }
        else if (data.status === "9") { statusText = "실패"; badgeClass = "bg-danger"; }

        statusCell.innerHTML = `<span class="badge ${badgeClass}">${statusText}</span>`;
        if (progressBar && data.percent !== undefined) {
            progressBar.style.setProperty('width', data.percent + '%', 'important');
            percentText.innerText = data.percent + "%";
        }
        if (statusText === "완료" && messageCell) {
            messageCell.innerHTML = `<span class="download-link" onclick="window.handleDownloadRequest('${data.convertedFile}')" style="color: #a5b4fc; cursor: pointer; text-decoration: underline; font-weight: bold;">파일 다운로드</span>`;
        }
        row.classList.add('update-highlight');
        setTimeout(() => row.classList.remove('update-highlight'), 1000);
    }
}

// --- 5. 이벤트 리스너 및 초기화 (DOMContentLoaded) ---
document.addEventListener('DOMContentLoaded', () => {
    const myUuid = getOrCreateUuid();

    // 초기 렌더링 및 SSE 연결
    initSse(myUuid);
    initStatsSse();
    renderSelectedFileList();

    // 파일 입력 변경 이벤트 리스너 연결 (HTML 인라인 제거용)
    const fileInput = document.getElementById('fileInput');
    if (fileInput) {
        fileInput.addEventListener('change', updateFileList);
    }

    // 폼 제출 이벤트 리스너
    const uploadForm = document.getElementById('uploadForm');
    if (uploadForm) {
        uploadForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            const btn = document.getElementById('submitBtn');
            const files = [...selectedFiles];
            const targetFormat = document.querySelector('select[name="targetFormat"]').value;

            if (files.length === 0) return;
            btn.disabled = true;
            btn.innerText = "처리 중...";

            try {
                // 1. 서버에 업로드 초기화 요청 (S3 Presigned URL 발급 등)
                const initReq = {
                    uuid: myUuid,
                    targetFormat: targetFormat,
                    files: files.map(f => ({ filename: f.name, contentType: f.type || "image/png", size: f.size }))
                };

                const initRes = await fetch('/file/upload-init', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(initReq)
                });

                const initData = await initRes.json();

                // 2. 발급받은 URL로 파일 직접 업로드 (병렬 처리 가능성 열어둠)
                for (let i = 0; i < initData.items.length; i++) {
                    await fetch(initData.items[i].uploadUrl, { method: 'PUT', body: files[i] });
                }

                // 3. 업로드 완료 서버 통지
                const completeRes = await fetch('/file/upload-complete', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ uuid: myUuid, historyIds: initData.items.map(x => x.historyId) })
                });

                const newHistories = await completeRes.json();

                // 4. UI 업데이트
                const tbody = document.getElementById('status-tbody');
                const emptyRow = document.getElementById('empty-row');
                if (emptyRow) emptyRow.remove();

                newHistories.forEach(h => {
                    const row = document.createElement('tr');
                    row.className = 'history-row update-highlight';
                    row.setAttribute('data-s3-filename', h.s3FileName);
                    row.innerHTML = `
                        <td class="text-truncate">${h.fileName || h.originalFile}</td>
                        <td class="status-cell text-center"><span class="badge bg-secondary">대기 중</span></td>
                        <td class="progress-cell">
                            <div class="progress" style="height: 8px; background: rgba(255,255,255,0.1);"><div class="progress-bar bg-info" style="width: 0%"></div></div>
                            <small class="percent-text d-block text-center mt-1" style="font-size: 0.7rem;">0%</small>
                        </td>
                        <td class="message-cell small text-secondary">-</td>
                    `;
                    tbody.prepend(row);
                });

                // 성공 후 폼 초기화
                this.reset();
                selectedFiles = [];
                renderSelectedFileList();
            } catch (err) {
                alert("업로드 중 오류가 발생했습니다.");
            } finally {
                btn.disabled = false;
                btn.innerText = "변환 시작";
            }
        });
    }
});
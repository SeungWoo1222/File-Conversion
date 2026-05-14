"""
큐 적체 시나리오용 부하 스크립트
- 10개 스레드로 동시에 업로드
- Worker(3 concurrent)보다 빠르게 RabbitMQ 큐를 채움
"""
import uuid
import requests
import concurrent.futures
import time
import os

API_BASE = "https://fileconversion.site"
IMAGE_PATH = os.path.join(os.path.dirname(__file__), "test-image.jpg")
TARGET_FORMAT = "PDF"
TOTAL_FILES = 1000     # 총 업로드 파일 수
CONCURRENT = 20        # 동시 업로드 스레드 수

session = requests.Session()
session.verify = False  # self-signed cert 무시

def upload_one(index: int) -> str:
    file_name = f"test-image-{index}.jpg"
    file_size = os.path.getsize(IMAGE_PATH)
    batch_uuid = str(uuid.uuid4())

    # 1) upload-init
    init_resp = session.post(
        f"{API_BASE}/file/upload-init",
        json={
            "uuid": batch_uuid,
            "targetFormat": TARGET_FORMAT,
            "files": [{"filename": file_name, "contentType": "image/jpeg", "size": file_size}]
        },
        timeout=15
    )
    init_resp.raise_for_status()
    item = init_resp.json()["items"][0]
    history_id = item["historyId"]
    upload_url = item["uploadUrl"]

    # 2) S3 presigned PUT
    with open(IMAGE_PATH, "rb") as f:
        put_resp = requests.put(
            upload_url,
            data=f,
            headers={"Content-Type": "image/jpeg"},
            timeout=30
        )
    put_resp.raise_for_status()

    # 3) upload-complete → outbox 생성 → Debezium → RabbitMQ
    complete_resp = session.post(
        f"{API_BASE}/file/upload-complete",
        json={"uuid": batch_uuid, "historyIds": [history_id]},
        timeout=15
    )
    complete_resp.raise_for_status()

    print(f"[{index:02d}] OK  historyId={history_id}")
    return f"OK:{history_id}"

def main():
    import urllib3
    urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

    print(f"=== 부하 시작: 총 {TOTAL_FILES}개, 동시 {CONCURRENT}스레드 ===")
    print(f"이미지: {IMAGE_PATH} ({os.path.getsize(IMAGE_PATH)//1024}KB)")
    print()

    start = time.time()
    success, fail = 0, 0

    with concurrent.futures.ThreadPoolExecutor(max_workers=CONCURRENT) as executor:
        futures = {executor.submit(upload_one, i): i for i in range(1, TOTAL_FILES + 1)}
        for future in concurrent.futures.as_completed(futures):
            try:
                future.result()
                success += 1
            except Exception as e:
                idx = futures[future]
                print(f"[{idx:02d}] FAIL: {e}")
                fail += 1

    elapsed = time.time() - start
    print()
    print(f"=== 완료: 성공 {success}개, 실패 {fail}개, 소요 {elapsed:.1f}초 ===")

if __name__ == "__main__":
    main()

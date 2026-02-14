from contextlib import contextmanager


@contextmanager
def expect_status(response, status_code=201):
    """응답 상태 코드를 검증하고 성공/실패를 Locust에 보고한다.

    사용법:
        with self.client.post(url, json=payload, catch_response=True) as resp:
            with expect_status(resp, 201) as data:
                # data: response.json() (성공 시)
                created_ids.append(data["id"])
    """
    if response.status_code == status_code:
        response.success()
        yield response.json()
    else:
        response.failure(f"Expected {status_code}, got {response.status_code}")
        yield None

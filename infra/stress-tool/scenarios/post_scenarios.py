import random

from locust import HttpUser, between, task, tag

from utils import random_string, expect_status


class PostUser(HttpUser):
    """게시글 CRUD 부하 테스트 시나리오"""

    wait_time = between(1, 3)
    abstract = True

    def on_start(self):
        self.created_post_ids = []

    @tag("post", "read")
    @task(5)
    def list_posts(self):
        """게시글 목록 조회 (페이징)"""
        page = random.randint(0, 5)
        self.client.get(
            f"/api/posts?page={page}&size=20",
            name="/api/posts?page=[N]",
        )

    @tag("post", "read")
    @task(3)
    def get_post(self):
        """게시글 단건 조회"""
        if not self.created_post_ids:
            return
        post_id = random.choice(self.created_post_ids)
        self.client.get(
            f"/api/posts/{post_id}",
            name="/api/posts/[id]",
        )

    @tag("post", "write")
    @task(2)
    def create_post(self):
        """게시글 생성"""
        payload = {
            "title": f"부하테스트 게시글 {random_string(8)}",
            "content": f"테스트 내용입니다. {random_string(50)}",
        }
        with self.client.post(
            "/api/posts",
            json=payload,
            catch_response=True,
        ) as response:
            with expect_status(response, 201) as data:
                if data:
                    self.created_post_ids.append(data["id"])

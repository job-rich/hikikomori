import random

from locust import HttpUser, between, task, tag

from utils import random_string, expect_status


class CommentUser(HttpUser):
    """댓글 CRUD 부하 테스트 시나리오"""

    wait_time = between(1, 3)
    abstract = True

    def on_start(self):
        """테스트 시작 시 인스턴스 상태 초기화 + 게시글 하나 생성하여 댓글 대상 확보"""
        self.target_post_ids = []
        self.created_comment_ids = {}  # {post_id: [comment_ids]}
        payload = {
            "title": f"댓글 테스트용 게시글 {random_string(6)}",
            "content": "댓글 부하 테스트를 위한 게시글입니다.",
        }
        response = self.client.post("/api/posts", json=payload)
        if response.status_code == 201:
            post_id = response.json()["id"]
            self.target_post_ids.append(post_id)
            self.created_comment_ids[post_id] = []

    def _get_post_id(self):
        if not self.target_post_ids:
            return None
        return random.choice(self.target_post_ids)

    @tag("comment", "read")
    @task(5)
    def list_comments(self):
        """댓글 목록 조회"""
        post_id = self._get_post_id()
        if post_id is None:
            return
        self.client.get(
            f"/api/posts/{post_id}/comments",
            name="/api/posts/[id]/comments",
        )

    @tag("comment", "write")
    @task(3)
    def create_comment(self):
        """댓글 생성"""
        post_id = self._get_post_id()
        if post_id is None:
            return
        payload = {
            "content": f"부하테스트 댓글 {random_string(20)}",
            "parentId": None,
        }
        with self.client.post(
            f"/api/posts/{post_id}/comments",
            json=payload,
            name="/api/posts/[id]/comments",
            catch_response=True,
        ) as response:
            with expect_status(response, 201) as data:
                if data:
                    self.created_comment_ids.setdefault(post_id, []).append(
                        data["id"]
                    )

    @tag("comment", "write")
    @task(2)
    def create_reply(self):
        """대댓글 생성 (기존 댓글에 답글)"""
        post_id = self._get_post_id()
        if post_id is None:
            return
        comments = self.created_comment_ids.get(post_id, [])
        if not comments:
            return
        payload = {
            "content": f"부하테스트 대댓글 {random_string(15)}",
            "parentId": random.choice(comments),
        }
        with self.client.post(
            f"/api/posts/{post_id}/comments",
            json=payload,
            name="/api/posts/[id]/comments [reply]",
            catch_response=True,
        ) as response:
            with expect_status(response, 201):
                pass

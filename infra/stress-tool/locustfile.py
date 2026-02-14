"""
Hikikomori Community API - Locust 부하 테스트

사용법:
  - Docker: docker compose up (Web UI: http://localhost:8089)
  - 로컬:   locust -f locustfile.py --host http://localhost:8080

시나리오:
  - CommunityUser: 게시글 + 댓글 통합 시나리오 (일반 사용자 행동 시뮬레이션)
  - PostOnlyUser: 게시글만 집중 테스트
  - CommentOnlyUser: 댓글만 집중 테스트

태그 필터링:
  - locust --tags post     (게시글 시나리오만)
  - locust --tags comment  (댓글 시나리오만)
  - locust --tags read     (읽기만)
  - locust --tags write    (쓰기만)
"""

from locust import between

from scenarios.post_scenarios import PostUser
from scenarios.comment_scenarios import CommentUser


class CommunityUser(PostUser, CommentUser):
    """통합 시나리오: 게시글 + 댓글 (일반 사용자 행동)"""

    weight = 6
    wait_time = between(1, 3)
    abstract = False

    def on_start(self):
        PostUser.on_start(self)
        CommentUser.on_start(self)


class PostOnlyUser(PostUser):
    """게시글 전용 시나리오"""

    weight = 2
    wait_time = between(1, 2)
    abstract = False


class CommentOnlyUser(CommentUser):
    """댓글 전용 시나리오"""

    weight = 2
    wait_time = between(1, 2)
    abstract = False

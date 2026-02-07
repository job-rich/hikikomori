package org.hikikomori.community.controller.data;

public record CommentCreateRequest(String content, Long parentId) {
}

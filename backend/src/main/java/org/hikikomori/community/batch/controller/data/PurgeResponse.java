package org.hikikomori.community.batch.controller.data;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PurgeResponse {

    private final Long executionId;
    private final long deletedCommentCount;
    private final long deletedPostCount;
    private final long totalDeletedCount;
}

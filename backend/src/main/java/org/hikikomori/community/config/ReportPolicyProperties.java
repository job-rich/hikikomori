package org.hikikomori.community.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "report")
public record ReportPolicyProperties(
        int hideThreshold,
        int banThreshold
) {
}

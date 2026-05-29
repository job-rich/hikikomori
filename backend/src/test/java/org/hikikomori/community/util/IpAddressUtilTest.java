package org.hikikomori.community.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IpAddressUtilTest {

    @Test
    @DisplayName("IPv4 주소는 그대로 반환한다")
    void IPv4는_그대로() {
        assertThat(IpAddressUtil.normalize("192.168.0.5")).isEqualTo("192.168.0.5");
    }

    @Test
    @DisplayName("IPv4-mapped IPv6(::ffff:x.x.x.x)는 IPv4로 변환한다")
    void IPv4매핑_IPv6는_IPv4로() {
        assertThat(IpAddressUtil.normalize("::ffff:192.168.0.5")).isEqualTo("192.168.0.5");
    }

    @Test
    @DisplayName("IPv6 루프백은 IPv4 루프백으로 변환한다")
    void IPv6_루프백은_127로() {
        assertThat(IpAddressUtil.normalize("0:0:0:0:0:0:0:1")).isEqualTo("127.0.0.1");
        assertThat(IpAddressUtil.normalize("::1")).isEqualTo("127.0.0.1");
    }

    @Test
    @DisplayName("IPv4로 변환 불가한 글로벌 IPv6는 정규화된 IPv6 문자열로 유지한다")
    void 글로벌_IPv6는_유지() {
        String result = IpAddressUtil.normalize("2001:db8::1");
        assertThat(result).contains(":");
        assertThat(result).isNotBlank();
    }

    @Test
    @DisplayName("null·빈 값은 그대로 반환한다")
    void null_빈값은_그대로() {
        assertThat(IpAddressUtil.normalize(null)).isNull();
        assertThat(IpAddressUtil.normalize("")).isEmpty();
    }

    @Test
    @DisplayName("파싱 불가한 문자열은 원본을 유지한다")
    void 파싱불가는_원본() {
        assertThat(IpAddressUtil.normalize("not-an-ip")).isEqualTo("not-an-ip");
    }
}

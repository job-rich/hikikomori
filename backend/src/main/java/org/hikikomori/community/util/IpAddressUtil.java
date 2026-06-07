package org.hikikomori.community.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 클라이언트 IP를 신고 집계용으로 정규화한다.
 * IPv4-mapped IPv6(::ffff:x.x.x.x)와 IPv6 루프백을 IPv4로 변환해
 * 같은 클라이언트가 IPv6/IPv4로 중복 집계되는 것을 막는다.
 */
public final class IpAddressUtil {

    private IpAddressUtil() {
    }

    public static String normalize(String rawIp) {
        if (rawIp == null || rawIp.isBlank()) {
            return rawIp;
        }
        String ip = rawIp.trim();
        // IP 리터럴이 아니면 DNS 조회를 피하기 위해 원본을 그대로 반환한다.
        if (!ip.matches("[0-9a-fA-F:.]+")) {
            return rawIp;
        }
        try {
            InetAddress addr = InetAddress.getByName(ip);
            if (addr instanceof Inet4Address) {
                // IPv4-mapped IPv6도 Java가 Inet4Address로 풀어주므로 여기서 IPv4로 반환된다.
                return addr.getHostAddress();
            }
            if (addr.isLoopbackAddress()) {
                return "127.0.0.1";
            }
            // IPv4로 변환 불가한 글로벌 IPv6는 정규화된 형태로 유지한다.
            return addr.getHostAddress();
        } catch (UnknownHostException e) {
            return rawIp;
        }
    }
}

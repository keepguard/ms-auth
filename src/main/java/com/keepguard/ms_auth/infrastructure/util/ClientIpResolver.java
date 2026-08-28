package com.keepguard.ms_auth.infrastructure.util;

import jakarta.servlet.http.HttpServletRequest;

public final class ClientIpResolver {

    private static final String[] HEADERS = {
            "X-Public-IP",
            "X-Client-IP",
            "CF-Connecting-IP",
            "True-Client-IP",
            "X-Real-IP",
            "X-Forwarded-For"
    };

    private ClientIpResolver() {
    }

    public static String from(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        for (String header : HEADERS) {
            String publicIp = IpAddressUtils.firstPublic(request.getHeader(header));
            if (publicIp != null) {
                return publicIp;
            }
        }
        String remotePublic = IpAddressUtils.firstPublic(request.getRemoteAddr());
        if (remotePublic != null) {
            return remotePublic;
        }
        for (String header : HEADERS) {
            String any = IpAddressUtils.firstIp(request.getHeader(header));
            if (any != null) {
                return any;
            }
        }
        return IpAddressUtils.firstIp(request.getRemoteAddr());
    }
}

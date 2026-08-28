package com.keepguard.ms_auth.infrastructure.util;

import java.net.InetAddress;

public final class IpAddressUtils {

    private IpAddressUtils() {
    }

    public static String firstIp(String raw) {
        String firstPublic = firstPublic(raw);
        if (firstPublic != null) {
            return firstPublic;
        }
        if (raw == null || raw.isBlank()) {
            return null;
        }
        for (String part : raw.split(",")) {
            String ip = stripPort(part.trim());
            if (ip != null) {
                return ip;
            }
        }
        return null;
    }

    public static String firstPublic(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        for (String part : raw.split(",")) {
            String ip = stripPort(part.trim());
            if (ip != null && !isPrivate(ip)) {
                return ip;
            }
        }
        return null;
    }

    public static boolean isPrivate(String ip) {
        if (ip == null || ip.isBlank()) {
            return true;
        }
        try {
            InetAddress address = InetAddress.getByName(ip);
            return address.isLoopbackAddress()
                    || address.isAnyLocalAddress()
                    || address.isLinkLocalAddress()
                    || address.isSiteLocalAddress()
                    || address.isMulticastAddress();
        } catch (Exception e) {
            return true;
        }
    }

    private static String stripPort(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String ip = value.trim();
        if (ip.startsWith("[")) {
            int close = ip.indexOf(']');
            if (close > 1) {
                return ip.substring(1, close);
            }
        }
        long colonCount = ip.chars().filter(ch -> ch == ':').count();
        if (colonCount == 1 && ip.contains(".")) {
            return ip.substring(0, ip.lastIndexOf(':'));
        }
        return ip;
    }
}

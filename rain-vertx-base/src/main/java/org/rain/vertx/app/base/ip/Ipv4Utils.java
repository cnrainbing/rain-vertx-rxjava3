package org.rain.vertx.app.base.ip;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * IPv4 utility class.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.3.2
 */
public class Ipv4Utils {
    private final static Pattern IP_PATTERN_FULL = Pattern
            .compile("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$");

    /**
     * Check is an IP is valid.
     *
     * @param ip
     * @return
     * @since 0.9.1.1
     */
    public static boolean isValidIp(String ip) {
        if (!IP_PATTERN_FULL.matcher(ip).matches()) {
            return false;
        }
        String[] ipArr = ip.split("\\.");
        for (String part : ipArr) {
            int v = Integer.parseInt(part);
            if (v < 0 || v > 255) {
                return false;
            }
        }
        return true;
    }

    /**
     * IP address to long (e.g. 10.0.0.1 -> 167772161).
     *
     * @param ip
     * @return
     */
    public static long ipToLong(String ip) {
        if (!isValidIp(ip)) {
            throw new IllegalArgumentException("Invalid IP address: " + ip);
        }

        long result = 0;
        String[] ipArr = ip.split("\\.");
        for (int i = 3; i >= 0; i--) {
            long part = Long.parseLong(ipArr[3 - i]);
            result |= part << (i * 8);
        }
        return result;
    }

    /**
     * Long to IP address (e.g. 167772161 -> 10.0.0.1)
     *
     * @param ip
     * @return
     */
    public static String longToIp(long ip) {
        return ((ip >> 24) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + ((ip >> 8) & 0xFF) + "."
                + (ip & 0xFF);
    }

    private final static Pattern IP_PATTERN_PARTIAL = Pattern
            .compile("^(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\\.\\*?$");

    private final static Pattern IP_PATTERN_RANGE = Pattern
            .compile("^(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\\.(\\d{1,3})\\-(\\d{1,3})$");

    private final static Pattern IP_PATTERN_SUBNET = Pattern
            .compile("^(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\\/(\\d{1,3})$");

    /**
     * Checks if an IP (e.g. 10.0.0.5) matches a subnet (e.g. 10.0.0.0/24).
     *
     * @param ip     in format {@code x.x.x.x}
     * @param subnet in format {@code x.x.x.x} or {@code x.x.x.x/y} or
     *               {@code x.x.x.x-y} or {@code x.x.x.*} (alias of
     *               {@code x.x.x.x/24}) or {@code x.x.x.} (another alias of
     *               {@code x.x.x.x/24})
     * @return
     */
    public static boolean isBelongToSubnet(String ip, String subnet) {
        if (subnet == null || ip == null) {
            throw new NullPointerException("IP or Subnet is null!");
        }
        if (!IP_PATTERN_FULL.matcher(ip).matches()) {
            throw new IllegalArgumentException("Invalid IP address: " + ip);
        }

        if (IP_PATTERN_FULL.matcher(subnet).matches()) {
            return StringUtils.equals(ip, subnet);
        }

        long ipAsLong = ipToLong(ip);
        Matcher matcherRange = IP_PATTERN_RANGE.matcher(subnet);
        if (matcherRange.matches()) {
            long ipAsLongStart = ipToLong(matcherRange.group(1) + "." + matcherRange.group(2));
            long ipAsLongEnd = ipToLong(matcherRange.group(1) + "." + matcherRange.group(3));
            return ipAsLongStart <= ipAsLong && ipAsLong <= ipAsLongEnd;
        }

        Matcher matcherPartial = IP_PATTERN_PARTIAL.matcher(subnet);
        if (matcherPartial.matches()) {
            subnet = matcherPartial.group(1) + ".0/24";
        }
        Matcher matcherSubnet = IP_PATTERN_SUBNET.matcher(subnet);
        if (matcherSubnet.matches()) {
            int mask = Integer.parseInt(matcherSubnet.group(2));
            if (mask > 32) {
                throw new IllegalArgumentException("Invalid subnet: " + subnet);
            }

            String maskAsBinary = StringUtils.repeat('1', mask)
                    + StringUtils.repeat('0', 32 - mask);
            long maskAsLong = Long.parseLong(maskAsBinary, 2);

            String ipBase = matcherSubnet.group(1);
            long ipBaseAsLong = ipToLong(ipBase);
            ipBaseAsLong &= maskAsLong;

            return ipBaseAsLong == (ipAsLong & maskAsLong);
        }

        throw new IllegalArgumentException("Invalid subnet: " + subnet);
    }
}

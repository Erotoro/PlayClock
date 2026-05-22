package dev.maksg.playclock.core.identity;

import dev.maksg.playclock.core.model.SourceType;
import dev.maksg.playclock.core.model.TrackedTarget;
import java.util.Locale;

public final class ServerIdentityNormalizer {

    private ServerIdentityNormalizer() {
    }

    public static TrackedTarget normalize(String rawAddress, SourceType sourceType) {
        String displayValue = rawAddress == null ? "" : rawAddress.trim();
        HostAndPort hostAndPort = parse(displayValue);
        String normalizedHost = hostAndPort.host().toLowerCase(Locale.ROOT);
        String normalizedValue = hostAndPort.hasExplicitPort() && hostAndPort.port() != 25565
                ? normalizedHost + ":" + hostAndPort.port()
                : normalizedHost;

        return new TrackedTarget(
                "multiplayer:" + normalizedValue,
                displayValue,
                normalizedValue,
                sourceType,
                true,
                isLocalAddress(normalizedHost));
    }

    private static HostAndPort parse(String value) {
        if (value.startsWith("[") && value.contains("]")) {
            int closingIndex = value.indexOf(']');
            String host = value.substring(0, closingIndex + 1).toLowerCase(Locale.ROOT);
            if (closingIndex + 1 < value.length() && value.charAt(closingIndex + 1) == ':') {
                return new HostAndPort(host, Integer.parseInt(value.substring(closingIndex + 2)), true);
            }

            return new HostAndPort(host, 25565, false);
        }

        int colonIndex = value.lastIndexOf(':');
        if (colonIndex > -1 && value.indexOf(':') == colonIndex) {
            return new HostAndPort(value.substring(0, colonIndex), Integer.parseInt(value.substring(colonIndex + 1)), true);
        }

        return new HostAndPort(value, 25565, false);
    }

    private static boolean isLocalAddress(String host) {
        return "localhost".equals(host) || "127.0.0.1".equals(host) || "[::1]".equals(host);
    }

    private record HostAndPort(String host, int port, boolean hasExplicitPort) {
    }
}

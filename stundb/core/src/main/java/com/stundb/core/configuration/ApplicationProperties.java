package com.stundb.core.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationProperties {

    private static final String PROPERTY_PATTERN = "\\$\\{([^}]+)}";

    private static final String PLACEHOLDER_PATTERN = "\\$\\{[^}]+}";

    private String name;
    private String ip;
    private String port;
    private Map<String, String> capacities;
    private Map<String, String> timeouts;
    private Map<String, String> tcpClient;
    private String digestAlgorithm;
    private List<String> seeds;

    public void setName(String name) {
        this.name = replaceProperty(name);
    }

    public void setIp(String ip) {
        this.ip = replaceProperty(ip);
    }

    public void setPort(String port) {
        this.port = replaceProperty(port);
    }

    public void setCapacity(Map<String, String> capacities) {
        capacities.replaceAll((key, value) -> replaceProperty(value));
        this.capacities = capacities;
    }

    public void setTimeouts(Map<String, String> timeouts) {
        timeouts.replaceAll((key, value) -> replaceProperty(value));
        this.timeouts = timeouts;
    }

    public void setTcpClient(Map<String, String> tcpClient) {
        tcpClient.replaceAll((key, value) -> replaceProperty(value));
        this.tcpClient = tcpClient;
    }

    public void setDigestAlgorithm(String digestAlgorithm) {
        this.digestAlgorithm = replaceProperty(digestAlgorithm);
    }

    public void setSeeds(List<String> seeds) {
        this.seeds = seeds.stream().map(this::replaceProperty).toList();
    }

    private String replaceProperty(String string) {
        var pattern = Pattern.compile(PROPERTY_PATTERN);
        var matcher = pattern.matcher(string);

        if (!matcher.find()) {
            return string;
        }

        var property = matcher.group(1).split(":");
        var value = System.getenv(property[0]);

        if (value != null) {
            return string.replaceAll(PLACEHOLDER_PATTERN, value);
        } else if (property.length > 1) {
            return string.replaceAll(PLACEHOLDER_PATTERN, property[1]);
        }

        return string.replaceAll(PLACEHOLDER_PATTERN, "");
    }
}


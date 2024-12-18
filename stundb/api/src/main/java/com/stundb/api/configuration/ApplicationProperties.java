package com.stundb.api.configuration;

import static java.util.Optional.ofNullable;

import jakarta.validation.constraints.*;

import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;

@SuppressWarnings("unused")
@ToString
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationProperties {

    private static final String ONLY_LETTERS_PATTERN = "^[a-zA-Z]+$";
    private static final String PROPERTY_PATTERN = "^\\$\\{([a-z.]+:?[^}]+?)}$";
    private static final String NUMBER_PATTERN = "^\\d+$";

    @NotBlank(message = "Property 'name' must be provided")
    private String name;

    @NotBlank(message = "Property 'ip' must be provided")
    private String ip;

    @NotBlank(message = "Property 'port' must be provided")
    @Pattern(regexp = NUMBER_PATTERN, message = "Property 'port' must be provided")
    private String port;

    @NotNull(message = "Property 'capacities' must be provided, along with its nested properties")
    private Map<
                    @NotBlank(message = "Capacity scope must be provided")
                    @Pattern(
                            regexp = ONLY_LETTERS_PATTERN,
                            message = "Capacity scope must be provided")
                    String,
                    @NotNull(message = "Timeout value must be provided")
                    @Pattern(regexp = NUMBER_PATTERN, message = "Timeout value must be provided")
                    String>
            capacities;

    @NotNull(message = "Property 'timeouts' must be provided, along with its nested properties")
    private Map<
                    @NotBlank(message = "Timeout scope must be provided")
                    @Pattern(
                            regexp = ONLY_LETTERS_PATTERN,
                            message = "Timeout scope must be provided")
                    String,
                    @NotNull(message = "Timeout value must be provided")
                    @Pattern(regexp = NUMBER_PATTERN, message = "Timeout value must be provided")
                    String>
            timeouts;

    @NotBlank(message = "Property 'digestAlgorithm' must be provided")
    private String digestAlgorithm;

    @NotNull(message = "Property 'seeds' must be provided")
    @NotEmpty(message = "At least one seed must be provided")
    private List<String> seeds;

    @NotNull(message = "Property 'executors' must be provided, along with its nested properties")
    @Setter
    private Map<
                    @Pattern(regexp = ONLY_LETTERS_PATTERN) String,
                    @NotNull(message = "Executor details must be provided") Map<
                            @NotBlank(message = "Property threads must be provided")
                            @Pattern(
                                    regexp = "^threads$",
                                    message = "Property threads must be provided")
                            String,
                            @NotNull(message = "A value greater than zero must be provided")
                            @Min(value = 0, message = "A value greater than zero must be provided")
                            Integer>>
            executors;

    @NotNull(
            message =
                    "Property 'backoffSettings' must be provided, along with its nested properties")
    @Setter
    private Map<
                    @NotBlank(message = "Backoff property must be provided")
                    @Pattern(
                            regexp = ONLY_LETTERS_PATTERN,
                            message = "Backoff property must be provided")
                    String,
                    @NotNull(message = "A value greater than zero must be provided")
                    @Min(value = 0, message = "A value greater than zero must be provided") Integer>
            backoffSettings;

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

    public void setDigestAlgorithm(String digestAlgorithm) {
        this.digestAlgorithm = replaceProperty(digestAlgorithm);
    }

    public void setSeeds(List<String> seeds) {
        this.seeds = seeds.stream().map(this::replaceProperty).toList();
    }

    private String replaceProperty(String string) {
        var pattern = java.util.regex.Pattern.compile(PROPERTY_PATTERN);
        return ofNullable(string)
                .map(pattern::matcher)
                .filter(Matcher::find)
                .map(matcher -> matcher.group(1))
                .flatMap(this::getString)
                .orElse(string);
    }

    private Optional<String> getString(String property) {
        return ofNullable(property)
                .map(prop -> prop.split(":"))
                .map(prop -> prop[0])
                .map(System::getProperty)
                .or(
                        () ->
                                ofNullable(property)
                                        .map(prop -> prop.split(":"))
                                        .filter(prop -> prop.length > 1)
                                        .map(prop -> prop[1]));
    }
}

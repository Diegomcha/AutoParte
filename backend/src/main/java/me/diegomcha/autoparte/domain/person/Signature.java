package me.diegomcha.autoparte.domain.person;

import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@EqualsAndHashCode()
public class Signature {

    @JdbcTypeCode(SqlTypes.JSON)
    private @NonNull Map<Instant, List<Point>> paths;
    private @NonNull Instant signedAt;

    private @NonNull String ipAddress;
    private @NonNull String userAgent;

    /**
     * Constructor for creating a Signature instance.
     *
     * @param paths            A map of timestamps to lists of Point objects representing the signature paths. Must not be null.
     * @param ipAddress        IP address from which the signature was captured. Must not be null.
     * @param userAgent        User agent string of the device used to capture the signature. Must not be null.
     */
    public Signature(@NonNull Map<Instant, List<Point>> paths, @NonNull String ipAddress, @NonNull String userAgent) {
        this.paths = paths.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> List.copyOf(e.getValue())
                ));
        this.signedAt = Instant.now();

        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }
}

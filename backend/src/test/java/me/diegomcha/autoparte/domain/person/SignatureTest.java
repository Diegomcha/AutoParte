package me.diegomcha.autoparte.domain.person;

import me.diegomcha.autoparte.TestingUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;

class SignatureTest {

    @Test
    void testConstructionContract() {
        try (var ignored = TestingUtils.getMockedInstantNow()) {
            Map<Instant, List<Point>> paths = Map.of(TestingUtils.INSTANT, List.of(new Point(1, 2), new Point(3, 4)));

            var signature = new Signature(paths, "IP_ADDRESS", "USER_AGENT");

            Assertions.assertEquals(paths, signature.getPaths());
            Assertions.assertEquals(Instant.now(), signature.getSignedAt());
            Assertions.assertEquals("IP_ADDRESS", signature.getIpAddress());
            Assertions.assertEquals("USER_AGENT", signature.getUserAgent());
        }
    }
}

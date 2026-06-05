package me.diegomcha.autoparte;

import org.mockito.MockedStatic;

import java.time.Instant;

import static org.mockito.Mockito.mockStatic;

public class TestingUtils {
    public static final Instant INSTANT = Instant.parse("2024-01-01T00:00:00Z");
    public static final Instant PAST_INSTANT = INSTANT.minusSeconds(3600);
    public static final Instant FUTURE_INSTANT = INSTANT.plusSeconds(3600);

    public static MockedStatic<Instant> getMockedInstantNow() {
        var mockedInstant = mockStatic(Instant.class);
        mockedInstant.when(Instant::now).thenReturn(INSTANT);
        return mockedInstant;
    }
}

package me.diegomcha.autoparte;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochRandomGenerator;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Answers.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;

public class TestingUtils {
    public static final Instant INSTANT = Instant.parse("2024-01-01T00:00:00Z");
    public static final Instant PAST_INSTANT = INSTANT.minusSeconds(3600);
    public static final Instant FUTURE_INSTANT = INSTANT.plusSeconds(3600);

    public static MockedStatic<Instant> getMockedInstantNow() {
        var mockedInstant = mockStatic(Instant.class, CALLS_REAL_METHODS);
        mockedInstant.when(Instant::now).thenReturn(INSTANT);
        return mockedInstant;
    }

    public static MockedStatic<Generators> getMockedUuidGenerator() {
        var counter = new AtomicInteger(1);

        var generatorMock = Mockito.mock(TimeBasedEpochRandomGenerator.class);
        Mockito.when(generatorMock.generate()).thenAnswer(inv -> {
            int currentId = counter.getAndIncrement();
            String hexSuffix = String.format("%012x", currentId);
            return UUID.fromString("00000000-0000-0000-0000-" + hexSuffix);
        });

        var generatorsMock = mockStatic(Generators.class, CALLS_REAL_METHODS);
        generatorsMock.when(Generators::timeBasedEpochRandomGenerator)
                .thenReturn(generatorMock);

        return generatorsMock;
    }
}

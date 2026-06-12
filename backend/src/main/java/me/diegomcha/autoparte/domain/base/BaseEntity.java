package me.diegomcha.autoparte.domain.base;

import com.fasterxml.uuid.Generators;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.UUID;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseEntity implements Persistable<UUID> {
    @EqualsAndHashCode.Include
    private final UUID id = Generators.timeBasedEpochRandomGenerator().generate();
    private long version;

    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;

    /**
     * Indicates whether the entity is new (not yet persisted) or not.
     * This is necessary because the ID is generated in the constructor,
     * so we can't rely on the ID being null to determine if the entity is new.
     */
    private boolean isNew = true;
    protected void markNotNew() {
        this.isNew = false;
    }
}

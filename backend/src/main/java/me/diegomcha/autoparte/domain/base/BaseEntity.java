package me.diegomcha.autoparte.domain.base;

import com.fasterxml.uuid.Generators;
import lombok.*;
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
    private final @NonNull UUID id = Generators.timeBasedEpochRandomGenerator().generate();
    private long version;

    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;

    /**
     * Returns a human-readable version of the entity's ID.
     *
     * @return a string representing the last 8 characters of the ID.
     */
    public String getHumanReadableId() {
        var strId = getId().toString().toUpperCase();
        return strId.substring(strId.length() - 8, strId.length() - 4) +  "-" + strId.substring(strId.length() - 4);
    } 

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

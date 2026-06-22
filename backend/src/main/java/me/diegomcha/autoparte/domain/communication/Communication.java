package me.diegomcha.autoparte.domain.communication;

import lombok.*;
import me.diegomcha.autoparte.domain.Booking;
import me.diegomcha.autoparte.domain.base.BaseEntity;

import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Communication extends BaseEntity {

    public enum CommunicationType {
        BOOKING,
        CHECKIN,
        CANCELLATION
    }

    public enum CommunicationStatus {
        PENDING,
        SENT,
        SUCCEEDED,
        FAILED,

        PENDING_VOIDED,
        VOIDED
    }

    public static Communication _of(@NonNull Booking booking, @NonNull CommunicationType type) {
        if (type == CommunicationType.CANCELLATION)
            return new CancelationCommunication(booking);

        return new Communication(booking, type);
    }

    private @NonNull Booking booking;
    private @NonNull CommunicationType type;

    @Setter(AccessLevel.PROTECTED)
    private @NonNull CommunicationStatus status = CommunicationStatus.PENDING;
    private Instant sentTimestamp;
    private UUID batchId;
    private UUID sesId;
    private Integer errorCode;

    protected Communication(@NonNull Booking booking, @NonNull CommunicationType type) {
        this.booking = booking;
        this.type = type;
    }

    public void markSent(@NonNull UUID batchId) {
        if (this.status != CommunicationStatus.PENDING)
            throw new IllegalStateException("Communication must be in PENDING status to mark as SENT");

        this.status = CommunicationStatus.SENT;
        this.sentTimestamp = Instant.now();
        this.batchId = batchId;
    }

    public void markFinishedSuccessfully(@NonNull UUID sesId) {
        if (this.status != CommunicationStatus.SENT)
            throw new IllegalStateException("Communication must be in SENT status to mark as SUCCEEDED");

        this.status = CommunicationStatus.SUCCEEDED;
        this.sesId = sesId;
    }

    public void markFinishedFailed(int errorCode) {
        if (this.status != CommunicationStatus.SENT)
            throw new IllegalStateException("Communication must be in SENT status to mark as FAILED");

        this.status = CommunicationStatus.FAILED;
        this.errorCode = errorCode;
    }

    public void markPendingVoided() {
        if (this.status != CommunicationStatus.SUCCEEDED)
            throw new IllegalStateException("Communication must be in SUCCEEDED status to mark as PENDING_VOIDED");

        this.status = CommunicationStatus.PENDING_VOIDED;
    }

    public void markVoided() {
        if (this.status == CommunicationStatus.SENT)
            throw new IllegalStateException("Communications SENT must be first processed before being marked as VOIDED");

        this.status = CommunicationStatus.VOIDED;
    }
}

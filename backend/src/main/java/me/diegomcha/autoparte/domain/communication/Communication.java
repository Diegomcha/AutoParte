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

    /**
     * Factory method to create a Communication instance based on the provided booking and communication type.
     *
     * @param booking Booking associated with the communication. Must not be null.
     * @param type    Type of communication to be created. Must not be null.
     * @return A Communication instance. If the type is CANCELLATION, a CancellationCommunication instance is returned; otherwise, a standard Communication instance is returned.
     * @throws IllegalArgumentException if either booking or type is null.
     */
    public static Communication _of(@NonNull Booking booking, @NonNull CommunicationType type) {
        if (type == CommunicationType.CANCELLATION)
            return new CancellationCommunication(booking);

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

    /**
     * Marks the communication as SENT, updating its status and recording the sent timestamp and batch ID.
     *
     * @param batchId The unique identifier for the batch in which the communication was sent. Must not be null.
     * @throws IllegalStateException if the communication is not in PENDING status.
     */
    public void markSent(@NonNull UUID batchId) {
        if (this.status != CommunicationStatus.PENDING)
            throw new IllegalStateException("Communication must be in PENDING status to mark as SENT");

        this.status = CommunicationStatus.SENT;
        this.sentTimestamp = Instant.now();
        this.batchId = batchId;
    }

    /**
     * Marks the communication as SUCCEEDED, updating its status and recording the SES ID.
     *
     * @param sesId The unique identifier for the communication in SES. Must not be null.
     * @throws IllegalStateException if the communication is not in SENT status.
     */
    public void markFinishedSuccessfully(@NonNull UUID sesId) {
        if (this.status != CommunicationStatus.SENT)
            throw new IllegalStateException("Communication must be in SENT status to mark as SUCCEEDED");

        this.status = CommunicationStatus.SUCCEEDED;
        this.sesId = sesId;
    }

    /**
     * Marks the communication as FAILED, updating its status and recording the error code.
     *
     * @param errorCode The error code associated with the failure. Must not be null.
     * @throws IllegalStateException if the communication is not in SENT status.
     */
    public void markFinishedFailed(int errorCode) {
        if (this.status != CommunicationStatus.SENT)
            throw new IllegalStateException("Communication must be in SENT status to mark as FAILED");

        this.status = CommunicationStatus.FAILED;
        this.errorCode = errorCode;
    }

    /**
     * Marks the communication as PENDING_VOIDED, updating its status.
     *
     * @throws IllegalStateException if the communication is not in SUCCEEDED status.
     */
    public void markPendingVoided() {
        if (this.status != CommunicationStatus.SUCCEEDED)
            throw new IllegalStateException("Communication must be in SUCCEEDED status to mark as PENDING_VOIDED");

        this.status = CommunicationStatus.PENDING_VOIDED;
    }

    /**
     * Marks the communication as VOIDED, updating its status.
     *
     * @throws IllegalStateException if the communication is in SENT status, as SENT communications must be processed before being marked as VOIDED.
     */
    public void markVoided() {
        if (this.status == CommunicationStatus.SENT)
            throw new IllegalStateException("Communications SENT must be first processed before being marked as VOIDED");

        this.status = CommunicationStatus.VOIDED;
    }
}

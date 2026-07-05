package me.diegomcha.autoparte.domain.communication;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import me.diegomcha.autoparte.domain.Booking;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CancellationCommunication extends Communication {

    protected CancellationCommunication(@NonNull Booking booking) {
        super(booking, CommunicationType.CANCELLATION);
    }

    public void markFinishedSuccessfully() {
        if (this.getStatus() != CommunicationStatus.PENDING)
            throw new IllegalStateException("Communication must be PENDING to mark as SUCCEEDED without sesId");

        this.setStatus(CommunicationStatus.SUCCEEDED);
    }

    @Override
    public void markPendingVoided() {
        throw new UnsupportedOperationException("Communication CANCELLATION cannot be marked as PENDING_VOIDED");
    }

    @Override
    public void markVoided() {
        throw new UnsupportedOperationException("Communication CANCELLATION cannot be marked as VOIDED");
    }
}

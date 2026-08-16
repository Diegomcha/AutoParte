package me.diegomcha.autoparte.api.booking;

import me.diegomcha.autoparte.api.booking.dto.BookingDtoRequest;
import me.diegomcha.autoparte.api.booking.dto.BookingDtoResponse;
import me.diegomcha.autoparte.domain.Accommodation;
import me.diegomcha.autoparte.domain.Booking;
import me.diegomcha.autoparte.domain.base.BaseEntity;
import me.diegomcha.autoparte.domain.booking.payment.CreditCardPayment;
import me.diegomcha.autoparte.domain.booking.payment.Payment;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Mapper(componentModel = "spring")
abstract class BookingMapper {

    @Mapping(target = "canBeModified", expression = "java(booking.canBeModified())")
    @Mapping(target = "canBeDeleted", expression = "java(booking.canBeDeleted())")
    @Mapping(target = "holderName", source = ".", qualifiedByName = "mapHolderName")
    public abstract BookingDtoResponse toResponse(Booking booking);

    public Page<BookingDtoResponse> toResponse(Page<Booking> bookings) {
        return bookings.map(this::toResponse);
    }

    @Mapping(target = "internetConnection", source = "dto.internetConnection")
    public abstract Booking fromCreate(Accommodation accommodation, BookingDtoRequest dto);

    public abstract void fromUpdate(BookingDtoRequest dto, @MappingTarget Booking booking);

    // Helpers

    @AfterMapping
    protected void updateDates(BookingDtoRequest dto, @MappingTarget Booking booking) {
        booking.setDates(dto.startTime(), dto.endTime());
    }

    @Mapping(target = "expiryDate", source = ".", qualifiedByName = "mapExpiryDate")
    protected abstract BookingDtoResponse.PaymentDtoResponse map(Payment payment);

    protected UUID map(BaseEntity entity) {
        return Optional
                .ofNullable(entity)
                .map(BaseEntity::getId)
                .orElse(null);
    }

    @Named("mapExpiryDate")
    protected Instant mapExpiryDate(Payment payment) {
        return payment instanceof CreditCardPayment cc
                ? cc.getExpiryDate()
                : null;
    }

    @Named("mapHolderName")
    protected String mapHolderName(Booking booking) {
        if (booking.getPeople().isEmpty())
            return null;

        var holder = booking.getPeople().getFirst();
        return String.format("%s %s.", holder.getPersonalInfo().getName(), holder.getPersonalInfo().getFirstSurname().charAt(0));
    }

    protected Payment map(BookingDtoRequest.PaymentDtoRequest dto) {
        return dto != null
                ? Payment.of(dto.type(), dto.mean(), dto.holder(), dto.date(), dto.expiryDate())
                : null;
    }

}

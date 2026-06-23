package me.diegomcha.autoparte.api.booking;

import me.diegomcha.autoparte.api.booking.dto.BookingDtoRequest;
import me.diegomcha.autoparte.api.booking.dto.BookingDtoResponse;
import me.diegomcha.autoparte.domain.Accommodation;
import me.diegomcha.autoparte.domain.Booking;
import me.diegomcha.autoparte.domain.base.BaseEntity;
import me.diegomcha.autoparte.domain.booking.payment.CreditCardPayment;
import me.diegomcha.autoparte.domain.booking.payment.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Mapper(componentModel = "spring")
abstract class BookingMapper {

    @Mapping(target = "canBeConfirmed", expression = "java(booking.canBeConfirmed())")
    @Mapping(target = "canBeCheckedIn", expression = "java(booking.canBeCheckedIn())")
    @Mapping(target = "payment.expiryDate", source = "booking.payment", qualifiedByName = "mapExpiryDate")
    public abstract BookingDtoResponse toResponse(Booking booking);

    public Page<BookingDtoResponse> toResponse(Page<Booking> bookings) {
        return bookings.map(this::toResponse);
    }

    @Mapping(target = "internetConnection", source = "dto.internetConnection")
    public abstract Booking fromCreate(Accommodation accommodation, BookingDtoRequest dto);

    public abstract void fromUpdate(BookingDtoRequest dto, @MappingTarget Booking booking);

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

    protected Payment map(BookingDtoRequest.PaymentDtoRequest dto) {
        return dto != null
                ? Payment.of(dto.type(), dto.mean(), dto.holder(), dto.date(), dto.expiryDate())
                : null;
    }

}

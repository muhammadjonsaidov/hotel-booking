package hotelbooking.notifications;

import java.time.LocalDate;

// The five fields needed to tell the guest about a confirmed reservation.
public record Confirmation(String reservationId, String guestEmail, String roomNumber,
                           LocalDate checkIn, LocalDate checkOut) {}

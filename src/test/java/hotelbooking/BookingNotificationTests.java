package hotelbooking;

import static hotelbooking.BookingServiceTestSupport.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

public class BookingNotificationTests {
    @Test
    public void one_confirmation_per_accepted_booking() {
        Fixture fixture = newService(false);
        fixture.service().reserve(MAYA, R7, nov(12, 14));
        fixture.service().reserve(OMAR, R7, nov(13, 15));
        fixture.service().reserve(NORA, R7, nov(14, 16));
        assertEquals(List.of("R101", "R102"), fixture.sender().sent());
    }

    @Test
    public void a_failed_confirmation_keeps_the_booking() {
        Fixture fixture = newService(true);
        BookingResult result = fixture.service().reserve(MAYA, R7, nov(12, 14));
        assertEquals("Accepted R101", result.toString());
        assertTrue(result.notificationFailed());
        assertEquals(1, fixture.store().count());
    }

    @Test
    public void sms_confirmations_need_no_change_to_BookingService() {
        SmsNotificationSender sms = new SmsNotificationSender();
        BookingService service = new BookingService(List.of(R7, R8), new InMemoryReservationStore(), sms);
        service.reserve(MAYA, R7, nov(12, 14));
        assertEquals(1, sms.sent());
    }
}

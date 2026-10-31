package hotelbooking;

import static hotelbooking.BookingServiceTestSupport.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BookingServiceTests {
    @Test
    void maya_gets_R7_when_it_is_free() {
        Fixture fixture = newService(false);
        assertEquals("Accepted R101", fixture.service().reserve(MAYA, R7, nov(12, 14)).toString());
    }

    @Test
    void omar_is_refused_when_a_night_is_shared() {
        Fixture fixture = newService(false);
        fixture.service().reserve(MAYA, R7, nov(12, 14));
        assertEquals("Unavailable", fixture.service().reserve(OMAR, R7, nov(13, 15)).toString());
        assertEquals(1, fixture.store().count());
    }

    @Test
    void nora_can_arrive_on_the_day_maya_leaves() {
        Fixture fixture = newService(false);
        fixture.service().reserve(MAYA, R7, nov(12, 14));
        assertEquals("Accepted R102", fixture.service().reserve(NORA, R7, nov(14, 16)).toString());
    }

    @Test
    void check_out_must_be_after_check_in() {
        Fixture fixture = newService(false);
        assertEquals("InvalidDates", fixture.service().reserve(OMAR, R8, nov(15, 15)).toString());
        assertEquals(0, fixture.store().count());
    }

    @Test
    void cancelling_frees_the_room_again() {
        Fixture fixture = newService(false);
        fixture.service().reserve(MAYA, R7, nov(12, 14));
        assertTrue(fixture.service().cancel("R101"));
        assertTrue(fixture.service().findFreeRooms(nov(12, 14)).contains(R7));
    }
}

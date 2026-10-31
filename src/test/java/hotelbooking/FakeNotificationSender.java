package hotelbooking;

import java.util.ArrayList;
import java.util.List;

// A test sender that records reservation ids or fails on purpose.
class FakeNotificationSender implements NotificationSender {
    private final boolean fail;
    final List<String> sent = new ArrayList<>();

    FakeNotificationSender(boolean fail) {
        this.fail = fail;
    }

    @Override
    public void sendConfirmation(Reservation reservation) {
        if (fail) throw new IllegalStateException("The mail server is down.");
        sent.add(reservation.id());
    }
}

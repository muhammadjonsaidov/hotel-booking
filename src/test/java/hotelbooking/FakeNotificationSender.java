package hotelbooking;

import java.util.ArrayList;
import java.util.List;

// A test sender that records reservation ids or fails on purpose.
public class FakeNotificationSender implements NotificationSender {
    private final boolean fail;
    private final List<String> sent = new ArrayList<>();

    public FakeNotificationSender(boolean fail) {
        this.fail = fail;
    }

    public List<String> sent() {
        return List.copyOf(sent);
    }

    @Override
    public void sendConfirmation(Reservation reservation) {
        if (fail) throw new IllegalStateException("The mail server is down.");
        sent.add(reservation.id());
    }
}

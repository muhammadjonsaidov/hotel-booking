package hotelbooking.notifications.internal;

import hotelbooking.notifications.Confirmation;
import hotelbooking.notifications.NotificationSender;
import java.util.concurrent.atomic.AtomicInteger;

// Simulated: counts the emails it would send and needs no network.
//
// sendSeconds makes it as slow as a real mail server. P10 Step 1 turns it up to one second, which
// is how a booking that does well under a millisecond of work becomes a one-second request.
public class EmailNotificationSender implements NotificationSender {
    private final double sendSeconds;
    private final AtomicInteger sent = new AtomicInteger();

    public EmailNotificationSender() {
        this(0);
    }

    public EmailNotificationSender(double sendSeconds) {
        this.sendSeconds = sendSeconds;
    }

    public int sent() {
        return sent.get();
    }

    @Override
    public void sendConfirmation(Confirmation confirmation) {
        if (sendSeconds > 0) {
            try {
                Thread.sleep((long) (sendSeconds * 1000));
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        sent.incrementAndGet();
    }
}

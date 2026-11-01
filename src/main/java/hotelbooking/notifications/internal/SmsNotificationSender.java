package hotelbooking.notifications.internal;

import hotelbooking.notifications.Confirmation;
import hotelbooking.notifications.NotificationSender;

// Simulated: counts confirmations without sending real messages.
public class SmsNotificationSender implements NotificationSender {
    private int sent;

    public int sent() {
        return sent;
    }

    @Override
    public void sendConfirmation(Confirmation confirmation) {
        sent++;
    }
}

package hotelbooking.notifications.internal;

import hotelbooking.notifications.Confirmation;
import hotelbooking.notifications.NotificationSender;

// Simulated: sends nothing and needs no network.
public class EmailNotificationSender implements NotificationSender {
    private int sent;

    public int sent() {
        return sent;
    }

    @Override
    public void sendConfirmation(Confirmation confirmation) {
        sent++;
    }
}

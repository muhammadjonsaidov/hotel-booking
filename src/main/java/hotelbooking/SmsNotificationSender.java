package hotelbooking;

// Simulated: counts confirmations without sending real messages.
public class SmsNotificationSender implements NotificationSender {
    private int sent;

    public int sent() {
        return sent;
    }

    @Override
    public void sendConfirmation(Reservation reservation) {
        sent++;
    }
}

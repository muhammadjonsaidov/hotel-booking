package hotelbooking;

// Simulated: sends nothing and needs no network.
public class EmailNotificationSender implements NotificationSender {
    private int sent;

    public int sent() {
        return sent;
    }

    @Override
    public void sendConfirmation(Reservation reservation) {
        sent++;
    }
}

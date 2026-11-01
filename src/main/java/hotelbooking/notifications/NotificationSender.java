package hotelbooking.notifications;

public interface NotificationSender {
    public void sendConfirmation(Confirmation confirmation);
}

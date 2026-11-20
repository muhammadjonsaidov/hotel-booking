package hotelbooking.notifications;

// A sender that also reports what it has promised, refused, finished and still owes (P10).
// The queued sender is one; BookingService still sees only the NotificationSender promise.
public interface CountingNotificationSender extends NotificationSender {
    NotificationCounts counts();
}

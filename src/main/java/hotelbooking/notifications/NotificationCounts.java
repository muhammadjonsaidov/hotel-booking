package hotelbooking.notifications;

// What a queued sender has promised, refused, finished and still owes.
//
// Once nothing is in flight, accepted == completed + queued. If that ever stops holding, work has
// been lost somewhere, and these four numbers are the only place it would show.
public record NotificationCounts(long accepted, long rejected, long completed, int queued) {}

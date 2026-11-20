package hotelbooking.notifications.internal;

import hotelbooking.notifications.Confirmation;
import hotelbooking.notifications.CountingNotificationSender;
import hotelbooking.notifications.NotificationCounts;
import hotelbooking.notifications.NotificationSender;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

// Takes confirmations off the request and gives them to a background worker.
//
// It keeps the NotificationSender promise, so BookingService cannot tell the difference: the
// booking hands over a Confirmation and carries on. What changes is when the email is sent, and
// what happens when there are more of them than the worker can keep up with.
//
// The queue is bounded and the write is offer(), not put(). offer returns false straight away when
// the queue is full; put would wait, and making the booking wait is the thing this class exists to
// stop. A refused confirmation is not a failed booking - the room is already the guest's - but it
// is not free either, and the rejected counter is what makes that cost visible instead of invisible.
public class QueuedNotificationSender implements CountingNotificationSender, AutoCloseable {
    private final NotificationSender sender;
    private final BlockingQueue<Confirmation> queue;
    private final List<Thread> workers = new ArrayList<>();
    private final AtomicLong accepted = new AtomicLong();
    private final AtomicLong rejected = new AtomicLong();
    private final AtomicLong completed = new AtomicLong();
    private volatile boolean stopping;

    public QueuedNotificationSender(NotificationSender sender) {
        this(sender, 10, 1);
    }

    public QueuedNotificationSender(NotificationSender sender, int capacity, int workerCount) {
        this.sender = sender;
        this.queue = new ArrayBlockingQueue<>(capacity);
        for (int number = 0; number < workerCount; number++) {
            Thread worker = new Thread(this::drain, "notifications-" + number);
            worker.setDaemon(true);
            worker.start();
            workers.add(worker);
        }
    }

    // Never waits and never throws: the whole point is that the booking is not held up.
    @Override
    public void sendConfirmation(Confirmation confirmation) {
        if (queue.offer(confirmation)) {
            accepted.incrementAndGet();
        } else {
            rejected.incrementAndGet();
        }
    }

    @Override
    public NotificationCounts counts() {
        return new NotificationCounts(accepted.get(), rejected.get(), completed.get(), queue.size());
    }

    // Stop the workers, emptying the queue first so a polite shutdown owes nobody an email.
    @Override
    public void close() {
        close(true);
    }

    // With drain the queue is emptied first; without it, whatever is still queued dies with the
    // process. Nothing here helps with a process that is killed: see docs/queue-trace.md.
    public void close(boolean drain) {
        try {
            if (drain) {
                while (!queue.isEmpty()) {
                    Thread.sleep(10);
                }
            }
            stopping = true;
            for (Thread worker : workers) {
                worker.join(5000);
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    private void drain() {
        while (!stopping) {
            Confirmation confirmation;
            try {
                confirmation = queue.poll(50, TimeUnit.MILLISECONDS);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                return;
            }
            if (confirmation == null) {
                continue;
            }
            try {
                sender.sendConfirmation(confirmation);
                completed.incrementAndGet();
            } catch (RuntimeException failure) {
                // A confirmation that could not be sent is lost, and saying so is the point: it
                // stays counted as accepted but never completed, so accepted != completed + queued,
                // which is exactly the signal that work went missing.
            }
        }
    }
}

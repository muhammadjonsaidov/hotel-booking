package hotelbooking;

// Simulated: sends nothing and needs no network.
public class EmailSender {
    private int sent;

    public int sent() {
        return sent;
    }

    public void sendConfirmation(Reservation reservation) {
        sent++;
    }
}

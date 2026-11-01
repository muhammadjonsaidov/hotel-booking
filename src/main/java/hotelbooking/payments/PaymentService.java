package hotelbooking.payments;

import java.math.BigDecimal;

// Public payment contract; booking does not call it yet.
public interface PaymentService {
    public PaymentStatus takeDeposit(String reservationId, BigDecimal amount);
}

package hotelbooking.payments.internal;

import java.math.BigDecimal;
import hotelbooking.payments.PaymentService;
import hotelbooking.payments.PaymentStatus;

// Simulated: no card and no network. A positive amount is Paid; anything else is Declined.
public class SimulatedPaymentService implements PaymentService {
    @Override
    public PaymentStatus takeDeposit(String reservationId, BigDecimal amount) {
        return amount.signum() > 0 ? PaymentStatus.PAID : PaymentStatus.DECLINED;
    }
}

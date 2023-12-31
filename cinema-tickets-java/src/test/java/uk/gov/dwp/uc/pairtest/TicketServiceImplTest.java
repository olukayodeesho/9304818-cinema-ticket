package uk.gov.dwp.uc.pairtest;

import junit.framework.TestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
public class TicketServiceImplTest extends TestCase {
    private TicketPaymentService mockPaymentService;
    private SeatReservationService mockReservationService;
    private TicketServiceImpl ticketService;
    public void testPurchaseTickets() {
    }

    @BeforeEach
    public void setUp() {
        mockPaymentService = Mockito.mock(TicketPaymentService.class);
        mockReservationService = Mockito.mock(SeatReservationService.class);
        ticketService = new TicketServiceImpl(mockPaymentService, mockReservationService);
    }

    @Test
    public void purchaseTickets_ValidRequest_Success() throws InvalidPurchaseException {
        // Arrange
        Long accountId = 500L;
        TicketTypeRequest adultTicket = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 6);
        TicketTypeRequest childTicket = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2);

        // Act
        ticketService.purchaseTickets(accountId, adultTicket, childTicket);

        // Assert
        Mockito.verify(mockPaymentService).makePayment(accountId, 140); // 6 Adult tickets * £20 + 2 Child ticket * £10
        Mockito.verify(mockReservationService).reserveSeat(accountId, 8); // 6 Adult tickets + 2 Child ticket
    }
    @Test
    public void purchaseTickets_NoTickets_ThrowsInvalidPurchaseException() {
        // Arrange
        Long accountId = 500L;

        // Act and Assert
        Assertions.assertThrows(InvalidPurchaseException.class, () -> {
            // No tickets provided
            ticketService.purchaseTickets(accountId);
        });
    }

    @Test
    public void purchaseTickets_MaximumTicketExceeded_ThrowsInvalidPurchaseException() {
        // Arrange
        Long accountId = 123L;
        TicketTypeRequest adultTicket = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 21);

        // Act and Assert
        Assertions.assertThrows(InvalidPurchaseException.class, () -> {
            // Attempt to purchase 21 tickets
            ticketService.purchaseTickets(accountId, adultTicket);
        });
    }

    @Test
    public void purchaseTickets_InfantOrChildWithoutAdult_ThrowsInvalidPurchaseException() {
        // Arrange
        Long accountId = 500L;
        TicketTypeRequest childTicket = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 8);
        TicketTypeRequest infantTicket = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 6);

        // Act and Assert
        Assertions.assertThrows(InvalidPurchaseException.class, () -> {
            // Child and Infant tickets without an Adult ticket
            ticketService.purchaseTickets(accountId, childTicket, infantTicket);
        });
    }

}
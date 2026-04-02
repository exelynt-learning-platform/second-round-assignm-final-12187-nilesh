package com.nv.ecommerce.service;

import com.nv.ecommerce.dto.request.PaymentRequestDto;
import com.nv.ecommerce.dto.request.RazorpayPaymentVerifyRequest;
import com.nv.ecommerce.entity.Order;
import com.nv.ecommerce.entity.Payment;
import com.nv.ecommerce.enums.OrderStatus;
import com.nv.ecommerce.enums.PaymentStatus;
import com.nv.ecommerce.repository.OrderRepository;
import com.nv.ecommerce.repository.PaymentRepository;
import com.nv.ecommerce.service.impl.PaymentServiceImpl;
import com.nv.ecommerce.util.RazorpaySignatureUtil;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentServiceImplTest {
	@Mock
	private OrderRepository orderRepository;

	@Mock
	private PaymentRepository paymentRepository;

	@Mock
	private RazorpayClient razorpayClient;

	@InjectMocks
	private PaymentServiceImpl paymentService;

	private Order order;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);

		order = new Order();
		order.setId(1L);
		order.setTotalAmount(BigDecimal.valueOf(1000));
	}

	@Test
	void testCreatePayment_Success() throws Exception {

		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

		when(paymentRepository.findByOrder(order)).thenReturn(Optional.empty());

		// Mock Razorpay response
		com.razorpay.Order razorpayOrder = mock(com.razorpay.Order.class);
		when(razorpayOrder.get("id")).thenReturn("razorpay_order_123");

		var orderApi = mock(com.razorpay.OrderClient.class);
		razorpayClient.orders = orderApi;
		when(orderApi.create(any(JSONObject.class))).thenReturn(razorpayOrder);

		when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

		PaymentRequestDto request = new PaymentRequestDto();
		request.setOrderId(1L);

		var response = paymentService.createPayment(request);

		assertNotNull(response);
		assertEquals("razorpay_order_123", response.getRazorpayOrderId());

		verify(paymentRepository).save(any(Payment.class));
	}

	@Test
	void testCreatePayment_AlreadyExists() {

		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

		when(paymentRepository.findByOrder(order)).thenReturn(Optional.of(new Payment()));

		PaymentRequestDto request = new PaymentRequestDto();
		request.setOrderId(1L);

		RuntimeException ex = assertThrows(RuntimeException.class, () -> paymentService.createPayment(request));

		assertEquals("Payment already exists for this order", ex.getMessage());
	}

	@Test
	void testVerifyPayment_Success() {

		Payment payment = new Payment();
		payment.setOrder(order);
		payment.setStatus(PaymentStatus.CREATED);
		payment.setRazorpayOrderId("order123");

		when(paymentRepository.findByRazorpayOrderId("order123")).thenReturn(Optional.of(payment));

		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

		try (MockedStatic<RazorpaySignatureUtil> mocked = mockStatic(RazorpaySignatureUtil.class)) {

			mocked.when(() -> RazorpaySignatureUtil.verify(any(), any(), any())).thenReturn(true);

			RazorpayPaymentVerifyRequest request = new RazorpayPaymentVerifyRequest();
			request.setRazorpayOrderId("order123");
			request.setRazorpayPaymentId("pay123");
			request.setRazorpaySignature("sig");

			var response = paymentService.verifyPayment(request);

			assertEquals("SUCCESS", response.getStatus());
			assertEquals(OrderStatus.PAID, order.getStatus());
		}
	}

	@Test
	void testVerifyPayment_Failed() {

		Payment payment = new Payment();
		payment.setOrder(order);
		payment.setStatus(PaymentStatus.CREATED);
		payment.setRazorpayOrderId("order123");

		when(paymentRepository.findByRazorpayOrderId("order123")).thenReturn(Optional.of(payment));

		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

		try (MockedStatic<RazorpaySignatureUtil> mocked = mockStatic(RazorpaySignatureUtil.class)) {

			mocked.when(() -> RazorpaySignatureUtil.verify(any(), any(), any())).thenReturn(false);

			RazorpayPaymentVerifyRequest request = new RazorpayPaymentVerifyRequest();
			request.setRazorpayOrderId("order123");
			request.setRazorpayPaymentId("pay123");

			var response = paymentService.verifyPayment(request);

			assertEquals("FAILED", response.getStatus());
			assertEquals(OrderStatus.CANCELLED, order.getStatus());
		}
	}

	@Test
	void testGetPaymentDetails_Success() {

		Payment payment = new Payment();
		payment.setId(10L);
		payment.setOrder(order);
		payment.setAmount(BigDecimal.valueOf(1000));
		payment.setCurrency("INR");
		payment.setStatus(PaymentStatus.SUCCESS);

		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

		when(paymentRepository.findByOrder(order)).thenReturn(Optional.of(payment));

		var response = paymentService.getPaymentDetails(1L);

		assertEquals(10L, response.getPaymentId());
		assertEquals("SUCCESS", response.getStatus());
	}
}

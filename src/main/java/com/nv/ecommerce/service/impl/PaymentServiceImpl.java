package com.nv.ecommerce.service.impl;

import com.nv.ecommerce.dto.request.PaymentRequestDto;
import com.nv.ecommerce.dto.request.RazorpayPaymentVerifyRequest;
import com.nv.ecommerce.dto.response.PaymentResponseDto;
import com.nv.ecommerce.dto.response.RazorpayOrderCreateResponse;
import com.nv.ecommerce.dto.response.RazorpayPaymentVerifyResponse;
import com.nv.ecommerce.entity.Order;
import com.nv.ecommerce.entity.Payment;
import com.nv.ecommerce.enums.OrderStatus;
import com.nv.ecommerce.enums.PaymentStatus;
import com.nv.ecommerce.exception.RazorpayOrderException;
import com.nv.ecommerce.exception.ResourceAlreadyExistException;
import com.nv.ecommerce.exception.ResourceNotFoundException;
import com.nv.ecommerce.repository.OrderRepository;
import com.nv.ecommerce.repository.PaymentRepository;
import com.nv.ecommerce.service.PaymentService;
import com.nv.ecommerce.util.RazorpaySignatureUtil;
import com.razorpay.RazorpayClient;

import jakarta.transaction.Transactional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class PaymentServiceImpl implements PaymentService {

	private final OrderRepository orderRepository;
	private final PaymentRepository paymentRepository;
	private RazorpayClient razorpayClient;
	private final String razorpayKeySecret;
	private final String razorpayWebhookSecret;

	public PaymentServiceImpl(OrderRepository orderRepository, PaymentRepository paymentRepository,
			RazorpayClient razorpayClient, @Value("${razorpay.key-secret}") String razorpayKeySecret,
			@Value("${razorpay.webhook-secret}") String razorpayWebhookSecret) {
		this.orderRepository = orderRepository;
		this.paymentRepository = paymentRepository;
		this.razorpayClient = razorpayClient;
		this.razorpayKeySecret = razorpayKeySecret;
		this.razorpayWebhookSecret = razorpayWebhookSecret;
	}

	@Override
	@Transactional
	public RazorpayOrderCreateResponse createPayment(PaymentRequestDto request) {

		// 1. Fetch Order
		Order order = orderRepository.findById(request.getOrderId())
				.orElseThrow(() -> new ResourceNotFoundException("Order not found"));

		// 2. Prevent duplicate payment
		if (paymentRepository.findByOrder(order).isPresent()) {
			throw new ResourceAlreadyExistException("Payment already exists for this order");
		}

		// 3. Get amount from order
		BigDecimal amount = order.getTotalAmount();

		// 4. Convert to paise
		Long amountInPaise = amount.multiply(BigDecimal.valueOf(100)).longValue();

		try {
			// 5. Create Razorpay Order
			JSONObject orderRequest = new JSONObject();
			orderRequest.put("amount", amountInPaise);
			orderRequest.put("currency", "INR");
			orderRequest.put("receipt", "orderId_" + order.getId());

			com.razorpay.Order razorpayOrder = razorpayClient.orders.create(orderRequest);

			String razorpayOrderId = razorpayOrder.get("id");

			// 6. Save Payment
			Payment payment = Payment.builder().order(order).amount(amount).currency("INR")
					.status(PaymentStatus.CREATED).razorpayOrderId(razorpayOrderId).build();

			paymentRepository.save(payment);

			// 7. Return Response DTO
			return RazorpayOrderCreateResponse.builder().razorpayOrderId(razorpayOrderId).amount(amount).currency("INR")
					.build();

		} catch (Exception e) {
			throw new RazorpayOrderException("Failed to create Razorpay order");
		}
	}

	@Override
	@Transactional
	public RazorpayPaymentVerifyResponse verifyPayment(RazorpayPaymentVerifyRequest request) {

		// 1. Find payment
		Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
				.orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

		// 2. Prevent duplicate processing
		if (payment.getStatus() == PaymentStatus.SUCCESS) {
			return new RazorpayPaymentVerifyResponse(PaymentStatus.SUCCESS.name());
		}

		// 3. Prepare payload
		String payload = request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();

		// 4. Verify signature
		boolean isValid = RazorpaySignatureUtil.verify(payload, razorpayKeySecret, request.getRazorpaySignature());

		// 5. Failed case
		if (!isValid) {

			payment.setStatus(PaymentStatus.FAILED);
			paymentRepository.save(payment);

			// Update Order on failed
			Order order = orderRepository.findById(payment.getOrder().getId())
					.orElseThrow(() -> new ResourceNotFoundException("Order not found"));

			order.setStatus(OrderStatus.CANCELLED);
			orderRepository.saveAndFlush(order);

			return new RazorpayPaymentVerifyResponse(PaymentStatus.FAILED.name());
		}

		// 6. Success case
		payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
		payment.setStatus(PaymentStatus.SUCCESS);

		paymentRepository.save(payment);

		// 7. Update Order
		Order order = orderRepository.findById(payment.getOrder().getId())
				.orElseThrow(() -> new ResourceNotFoundException("Order not found"));

		order.setStatus(OrderStatus.PAID);
		orderRepository.saveAndFlush(order);

		return new RazorpayPaymentVerifyResponse(PaymentStatus.SUCCESS.name());
	}

	@Override
	public PaymentResponseDto getPaymentDetails(Long orderId) {

		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found"));

		Payment payment = paymentRepository.findByOrder(order)
				.orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

		return mapToDto(payment);
	}

	private PaymentResponseDto mapToDto(Payment payment) {

		PaymentResponseDto dto = new PaymentResponseDto();

		dto.setPaymentId(payment.getId());
		dto.setOrderId(payment.getOrder().getId());
		dto.setAmount(payment.getAmount());
		dto.setCurrency(payment.getCurrency());
		dto.setStatus(payment.getStatus().name());
		dto.setPaymentMethod(payment.getPaymentMethod());
		dto.setRazorpayOrderId(payment.getRazorpayOrderId());
		dto.setRazorpayPaymentId(payment.getRazorpayPaymentId());
		dto.setCreatedAt(payment.getCreatedAt());

		return dto;
	}

	@Override
	@Transactional
	public void handleWebhook(String payload, String signature) {

		// 1. Verify webhook signature
		boolean valid = RazorpaySignatureUtil.verify(payload, razorpayWebhookSecret, signature);

		if (!valid) {
			return; // ignore invalid webhook
		}

		JSONObject json = new JSONObject(payload);
		String event = json.getString("event");

		if ("payment.captured".equals(event)) {
			handleCaptured(json);
		}

		if ("payment.failed".equals(event)) {
			handleFailed(json);
		}
	}

	@Transactional
	private void handleCaptured(JSONObject json) {

		JSONObject paymentEntity = json.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");

		String razorpayOrderId = paymentEntity.getString("order_id");
		String razorpayPaymentId = paymentEntity.getString("id");
		String paymentMethod = paymentEntity.getString("method");

		Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
				.orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

		if (payment.getStatus() == PaymentStatus.SUCCESS) {
			return;
		}

		payment.setStatus(PaymentStatus.SUCCESS);
		payment.setRazorpayPaymentId(razorpayPaymentId);
		payment.setPaymentMethod(paymentMethod);

		paymentRepository.save(payment);

		// Update Order
		Order order = orderRepository.findById(payment.getOrder().getId())
				.orElseThrow(() -> new ResourceNotFoundException("Order not found"));

		order.setStatus(OrderStatus.PAID);
		orderRepository.saveAndFlush(order);
	}

	@Transactional
	private void handleFailed(JSONObject json) {

		JSONObject paymentEntity = json.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");

		String razorpayOrderId = paymentEntity.getString("order_id");
		String razorpayPaymentId = paymentEntity.getString("id");
		String paymentMethod = paymentEntity.getString("method");

		Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
				.orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

		// avoid overwrite success
		if (payment.getStatus() == PaymentStatus.SUCCESS) {
			return;
		}

		payment.setStatus(PaymentStatus.FAILED);
		payment.setRazorpayPaymentId(razorpayPaymentId);
		payment.setPaymentMethod(paymentMethod);

		paymentRepository.save(payment);

		// Update Order
		Order order = orderRepository.findById(payment.getOrder().getId())
				.orElseThrow(() -> new ResourceNotFoundException("Order not found"));

		order.setStatus(OrderStatus.CANCELLED);
		orderRepository.saveAndFlush(order);
	}
}
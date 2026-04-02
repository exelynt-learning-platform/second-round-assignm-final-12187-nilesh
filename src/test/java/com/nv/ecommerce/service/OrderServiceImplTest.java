package com.nv.ecommerce.service;

import com.nv.ecommerce.dto.request.OrderRequestDto;
import com.nv.ecommerce.entity.*;
import com.nv.ecommerce.enums.OrderStatus;
import com.nv.ecommerce.repository.*;
import com.nv.ecommerce.service.impl.OrderServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceImplTest {

	@Mock
	private CartRepository cartRepository;

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private OrderServiceImpl orderService;

	private User user;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);

		// Mock logged-in user
		user = new User();
		user.setId(1L);
		user.setUsername("testuser");

		// Mock SecurityContext
		var auth = mock(org.springframework.security.core.Authentication.class);
		when(auth.getName()).thenReturn("testuser");

		var context = mock(org.springframework.security.core.context.SecurityContext.class);
		when(context.getAuthentication()).thenReturn(auth);

		SecurityContextHolder.setContext(context);

		when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
	}

	@Test
	void testPlaceOrder_Success() {

		// Step 1: Create Product
		Product product = new Product();
		product.setId(1L);
		product.setName("Laptop");
		product.setStockQuantity(10);

		// Step 2: Create CartItem
		CartItem cartItem = new CartItem();
		cartItem.setProduct(product);
		cartItem.setQuantity(2);
		cartItem.setPrice(BigDecimal.valueOf(1000));

		// Step 3: Create Cart
		Cart cart = new Cart();
		cart.setUser(user);
		cart.setItems(new ArrayList<>(List.of(cartItem)));

		// Mock cartRepository
		when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

		// Mock order save
		when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// Request DTO
		OrderRequestDto request = new OrderRequestDto();
		request.setShippingAddress("Pune");

		// Call method
		var response = orderService.placeOrder(request);

		// Assertions
		assertNotNull(response);

		// Verify interactions
		verify(orderRepository, times(1)).save(any(Order.class));
		verify(cartRepository, times(1)).save(cart);
	}

	@Test
	void testPlaceOrder_EmptyCart() {

		Cart cart = new Cart();
		cart.setUser(user);
		cart.setItems(new ArrayList<>());

		when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

		OrderRequestDto request = new OrderRequestDto();

		RuntimeException ex = assertThrows(RuntimeException.class, () -> orderService.placeOrder(request));

		assertEquals("Cart is empty", ex.getMessage());
	}

	@Test
	void testCancelOrder_Success() {

		// Product
		Product product = new Product();
		product.setStockQuantity(5);

		// OrderItem
		OrderItem item = new OrderItem();
		item.setProduct(product);
		item.setQuantity(2);

		// Order
		Order order = new Order();
		order.setId(1L);
		order.setUser(user);
		order.setStatus(OrderStatus.CREATED);
		order.setItems(List.of(item));

		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

		orderService.cancelOrder(1L);

		assertEquals(OrderStatus.CANCELLED, order.getStatus());
		assertEquals(7, product.getStockQuantity());

		verify(orderRepository).save(order);
	}

	@Test
	void testCancelOrder_Unauthorized() {

		User otherUser = new User();
		otherUser.setId(2L);

		Order order = new Order();
		order.setUser(otherUser);

		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

		RuntimeException ex = assertThrows(RuntimeException.class, () -> orderService.cancelOrder(1L));

		assertEquals("Unauthorized to cancel this order", ex.getMessage());
	}
}

package br.com.matheus.commerceapi.service;

import br.com.matheus.commerceapi.dto.request.order.CreateOrderRequestDto;
import br.com.matheus.commerceapi.dto.response.order.OrderResponseDto;
import br.com.matheus.commerceapi.entity.*;
import br.com.matheus.commerceapi.enums.OrderStatus;
import br.com.matheus.commerceapi.exception.NotFoundException;
import br.com.matheus.commerceapi.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final ProductService productService;
    private final StoreService storeService;
    private final StockService stockService;

    @Transactional
    public OrderResponseDto createOrder(Long customerId, CreateOrderRequestDto request) {
        User customer = userService.findUserById(customerId);
        Store store = storeService.findActiveStoreById(request.storeId());

        List<OrderItem> orderItems = request.items().stream()
                .map(item -> buildOrderItem(store, item))
                .collect(Collectors.toCollection(ArrayList::new));

        BigDecimal total = orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .customer(customer)
                .store(store)
                .total(total)
                .items(orderItems)
                .build();

        orderItems.forEach(item -> item.setOrder(order));

        Order savedOrder = orderRepository.save(order);

        log.info("Order created: ID {} for customer {} at store {}", savedOrder.getId(), customerId, store.getId());

        return OrderResponseDto.fromEntity(savedOrder);
    }

    public OrderResponseDto getOrder(Long orderId) {
        Order order = findOrderById(orderId);

        return OrderResponseDto.fromEntity(order);
    }

    @Transactional
    public OrderResponseDto confirmOrder(Long orderId) {
        Order order = findOrderById(orderId);

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new IllegalStateException("Order must be CREATED to be confirmed");
        }

        order.nextStatus();
        confirmStockReservations(order);

        Order savedOrder = orderRepository.save(order);

        log.info("Order confirmed: ID {}", orderId);

        return OrderResponseDto.fromEntity(savedOrder);
    }

    @Transactional
    public OrderResponseDto shipOrder(Long orderId) {
        Order order = findOrderById(orderId);

        if (order.getStatus() != OrderStatus.PAID) {
            throw new IllegalStateException("Order must be PAID to be shipped");
        }

        order.nextStatus();
        Order savedOrder = orderRepository.save(order);

        log.info("Order shipped: ID {}", orderId);

        return OrderResponseDto.fromEntity(savedOrder);
    }

    @Transactional
    public OrderResponseDto deliverOrder(Long orderId) {
        Order order = findOrderById(orderId);

        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new IllegalStateException("Order must be SHIPPED to be delivered");
        }

        order.nextStatus();
        Order savedOrder = orderRepository.save(order);

        log.info("Order delivered: ID {}", orderId);

        return OrderResponseDto.fromEntity(savedOrder);
    }

    @Transactional
    public OrderResponseDto cancelOrder(Long orderId) {
        Order order = findOrderById(orderId);

        if (!order.canCancel()) {
            throw new IllegalStateException("Order cannot be canceled in status: " + order.getStatus());
        }

        order.cancel();
        cancelStockReservations(order);

        Order savedOrder = orderRepository.save(order);

        log.info("Order canceled: ID {}", orderId);

        return OrderResponseDto.fromEntity(savedOrder);
    }

    public Page<OrderResponseDto> getCustomerOrders(Long customerId, Pageable pageable){
        Page<Order> orders = orderRepository.findByCustomerId(customerId, pageable);
        return orders.map(OrderResponseDto::fromEntity);
    }

    private void confirmStockReservations(Order order) {
        order.getItems().forEach(item ->
                stockService.confirmReservation(item.getProduct().getId()));
    }

    private void cancelStockReservations(Order order) {
        order.getItems().forEach(item ->
                stockService.cancelReservation(item.getProduct().getId()));
    }

    private Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));
    }

    private OrderItem buildOrderItem(Store store, CreateOrderRequestDto.OrderItemRequest itemRequest) {
        Product product = productService.findProductById(itemRequest.productId());

        if (!product.getStore().getId().equals(store.getId())) {
            throw new IllegalArgumentException("Product " + product.getId() + " does not belong to store " + store.getId());
        }
        if (!product.isActive()) {
            throw new IllegalArgumentException("Product " + product.getId() + " is not active");
        }

        stockService.reserveStock(product.getId(), itemRequest.quantity());

        return OrderItem.builder()
                .product(product)
                .quantity(itemRequest.quantity())
                .unitPrice(product.getPrice())
                .build();
    }
}

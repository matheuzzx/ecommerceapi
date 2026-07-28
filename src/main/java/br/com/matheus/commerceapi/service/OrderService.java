package br.com.matheus.commerceapi.service;

import br.com.matheus.commerceapi.dto.request.order.CreateOrderRequestDto;
import br.com.matheus.commerceapi.dto.response.order.OrderResponseDto;
import br.com.matheus.commerceapi.entity.*;
import br.com.matheus.commerceapi.exception.NotFoundException;
import br.com.matheus.commerceapi.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public OrderResponseDto cancelOrder(Long orderId) {
        Order order = findOrderById(orderId);
        order.cancel();
        orderRepository.save(order);
        return OrderResponseDto.fromEntity(order);
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

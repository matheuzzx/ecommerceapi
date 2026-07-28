package br.com.matheus.commerceapi.service;

import br.com.matheus.commerceapi.dto.request.order.CreateOrderRequestDto;
import br.com.matheus.commerceapi.dto.response.order.OrderResponseDto;
import br.com.matheus.commerceapi.entity.*;
import br.com.matheus.commerceapi.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (var itemRequest : request.items()) {
            Product product = productService.findProductById(itemRequest.productId());

            if (!product.isActive()) {
                throw new IllegalArgumentException("Product " + product.getId() + " is not active");
            }

            stockService.reserveStock(product.getId(), itemRequest.quantity());

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemRequest.quantity())
                    .unitPrice(product.getPrice())
                    .build();

            orderItems.add(orderItem);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.quantity())));
        }

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
}

package br.com.matheus.commerceapi.service;

import br.com.matheus.commerceapi.domain.Money;
import br.com.matheus.commerceapi.dto.request.order.CreateOrderRequestDto;
import br.com.matheus.commerceapi.dto.response.order.OrderResponseDto;
import br.com.matheus.commerceapi.entity.*;
import br.com.matheus.commerceapi.enums.OrderStatus;
import br.com.matheus.commerceapi.exception.ConflictException;
import br.com.matheus.commerceapi.exception.InvalidArgumentException;
import br.com.matheus.commerceapi.exception.NotFoundException;
import br.com.matheus.commerceapi.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final AddressService addressService;

    @Transactional
    public OrderResponseDto createOrder(Long customerId, CreateOrderRequestDto request) {
        User customer = userService.findUserById(customerId);
        Store store = storeService.findActiveStoreById(request.storeId());
        ShippingAddress shippingAddress = findShippingAddress(customerId, request.addressId());

        List<OrderItem> orderItems = request.items().stream()
                .map(item -> buildOrderItem(store, item))
                .collect(Collectors.toCollection(ArrayList::new));

        Money total = orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(Money.zero(), Money::add);

        Order order = Order.builder()
                .customer(customer)
                .store(store)
                .total(total)
                .shippingAddress(shippingAddress)
                .items(orderItems)
                .build();

        orderItems.forEach(item -> item.setOrder(order));

        Order savedOrder = orderRepository.save(order);

        return OrderResponseDto.fromEntity(savedOrder);
    }

    public OrderResponseDto getOrder(Long orderId, Long customerId) {
        Order order = findOrderByCustomer(orderId, customerId);

        return OrderResponseDto.fromEntity(order);
    }

    @Transactional
    public OrderResponseDto confirmOrder(Long orderId) {
        Order order = findOrderById(orderId);

        if (order.getStatus() != OrderStatus.CREATED) {
            log.warn("Order {} cannot be confirmed: status is {}", orderId, order.getStatus());
            throw new ConflictException("Order must be CREATED to be confirmed");
        }

        order.nextStatus();
        confirmStockReservations(order);

        Order savedOrder = orderRepository.save(order);

        return OrderResponseDto.fromEntity(savedOrder);
    }

    @Transactional
    public Order markOrderAsPaid(Long orderId, Long customerId) {
        Order order = findOrderByCustomer(orderId, customerId);

        if (order.getStatus() != OrderStatus.CREATED) {
            log.warn("Order {} cannot be paid: status is {}", orderId, order.getStatus());
            throw new ConflictException("Order must be CREATED to be paid");
        }

        order.nextStatus();
        confirmStockReservations(order);

        return orderRepository.save(order);
    }

    @Transactional
    public OrderResponseDto shipOrder(Long orderId) {
        return ship(findOrderById(orderId));
    }

    @Transactional
    public OrderResponseDto shipOrderForStoreOwner(Long orderId, Long storeOwnerId) {
        return ship(findOrderByStoreOwner(orderId, storeOwnerId));
    }

    @Transactional
    public OrderResponseDto deliverOrder(Long orderId) {
        return deliver(findOrderById(orderId));
    }

    @Transactional
    public OrderResponseDto deliverOrderForStoreOwner(Long orderId, Long storeOwnerId) {
        return deliver(findOrderByStoreOwner(orderId, storeOwnerId));
    }

    @Transactional
    public OrderResponseDto cancelOrder(Long orderId, Long customerId) {
        Order order = findOrderByCustomer(orderId, customerId);

        if (!order.canCancel()) {
            log.warn("Order {} cannot be canceled: status is {}", orderId, order.getStatus());
            throw new ConflictException("Order cannot be canceled in status: " + order.getStatus());
        }

        order.cancel();
        cancelStockReservations(order);

        Order savedOrder = orderRepository.save(order);

        return OrderResponseDto.fromEntity(savedOrder);
    }

    public Page<OrderResponseDto> getCustomerOrders(Long customerId, Pageable pageable){
        Page<Order> orders = orderRepository.findByCustomerId(customerId, pageable);
        return orders.map(OrderResponseDto::fromEntity);
    }

    public Page<OrderResponseDto> getStoreOwnerOrders(Long userId, Pageable pageable){
        Store store = storeService.findStoreByStoreOwner(userId);
        Page<Order> orders = orderRepository.findByStoreId(store.getId(), pageable);
        return orders.map(OrderResponseDto::fromEntity);
    }

    private OrderResponseDto ship(Order order) {
        if (order.getStatus() != OrderStatus.PAID) {
            log.warn("Order {} cannot be shipped: status is {}", order.getId(), order.getStatus());
            throw new ConflictException("Order must be PAID to be shipped");
        }

        order.nextStatus();
        Order savedOrder = orderRepository.save(order);

        return OrderResponseDto.fromEntity(savedOrder);
    }

    private OrderResponseDto deliver(Order order) {
        if (order.getStatus() != OrderStatus.SHIPPED) {
            log.warn("Order {} cannot be delivered: status is {}", order.getId(), order.getStatus());
            throw new ConflictException("Order must be SHIPPED to be delivered");
        }

        order.nextStatus();
        Order savedOrder = orderRepository.save(order);

        return OrderResponseDto.fromEntity(savedOrder);
    }

    private void confirmStockReservations(Order order) {
        order.getItems().forEach(item ->
                stockService.confirmReservation(item.getProduct().getId(), item.getQuantity()));
    }

    private void cancelStockReservations(Order order) {
        order.getItems().forEach(item ->
                stockService.cancelReservation(item.getProduct().getId(), item.getQuantity()));
    }

    private Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> {
            log.warn("Order not found: ID {}", orderId);
            return new NotFoundException("Order not found with id: " + orderId);
        });
    }

    public Order findOrderByCustomer(Long orderId, Long customerId) {
        return orderRepository.findByCustomerIdAndId(customerId, orderId)
                .orElseThrow(() -> {
                    log.warn("Order not found or not owned: ID {}, customer {}", orderId, customerId);
                    return new NotFoundException("Order not found with id: " + orderId);
                });
    }

    private Order findOrderByStoreOwner(Long orderId, Long storeOwnerId) {
        return orderRepository.findByStore_StoreOwnerIdAndId(storeOwnerId, orderId)
                .orElseThrow(() -> {
                    log.warn("Order not found or not owned: ID {}, storeOwner {}", orderId, storeOwnerId);
                    return new NotFoundException("Order not found with id: " + orderId);
                });
    }

    private ShippingAddress findShippingAddress(Long userId, Long addressId) {
        Address address = addressService.findAddressByIdAndUser(addressId, userId);

        return ShippingAddress.from(address);
    }

    private OrderItem buildOrderItem(Store store, CreateOrderRequestDto.OrderItemRequest itemRequest) {
        Product product = productService.findProductById(itemRequest.productId());

        if (!product.getStore().getId().equals(store.getId())) {
            log.warn("Product {} does not belong to store {}", product.getId(), store.getId());
            throw new InvalidArgumentException("Product " + product.getId() + " does not belong to store " + store.getId());
        }
        if (!product.isActive()) {
            log.warn("Product {} is not active", product.getId());
            throw new InvalidArgumentException("Product " + product.getId() + " is not active");
        }

        stockService.reserveStock(product.getId(), itemRequest.quantity());

        return OrderItem.builder()
                .product(product)
                .quantity(itemRequest.quantity())
                .unitPrice(product.getPrice())
                .build();
    }
}

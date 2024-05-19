package be.kdg.sa.warehouse.receivers;

import be.kdg.sa.warehouse.Senders.RestSender;
import be.kdg.sa.warehouse.config.RabbitTopology;
import be.kdg.sa.warehouse.controller.dto.IngredientDto;
import be.kdg.sa.warehouse.controller.dto.OrderDto;
import be.kdg.sa.warehouse.services.IngredientService;
import be.kdg.sa.warehouse.services.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderReceiver {
    private static final Logger logger = LoggerFactory.getLogger(OrderReceiver.class);
    private final OrderService orderService;
    private final RestSender restSender;

    public OrderReceiver(OrderService orderService, RestSender restSender) {
        this.orderService = orderService;
        this.restSender=restSender;
    }

    @RabbitListener(queues = RabbitTopology.ORDER_INGREDIENT_QUEUE, messageConverter = "#{jackson2JsonMessageConverter}")
    public void receiveOrderIngredients(OrderDto orderDto) throws JsonProcessingException {
        logger.info("Received an order message with UUID: {}", orderDto.getOrderId());
        orderService.addOrder(orderDto);

        restSender.sendOrder(orderDto.getOrderId());
    }
}

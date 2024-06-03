package be.kdg.sa.warehouse.services;

import be.kdg.sa.warehouse.domain.Enum.OrderStatus;
import be.kdg.sa.warehouse.domain.Ingredient;
import be.kdg.sa.warehouse.domain.Order;
import be.kdg.sa.warehouse.domain.OrderIngredient;
import be.kdg.sa.warehouse.repositories.IngredientRepository;
import be.kdg.sa.warehouse.repositories.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.UUID;

@Service
public class IngredientService {
    private static final Logger logger = LoggerFactory.getLogger(IngredientService.class);
    private final IngredientRepository ingredientRepository;
    private final OrderRepository orderRepository;

    @Autowired
    public IngredientService(IngredientRepository ingredientRepository, OrderRepository orderRepository){
        this.ingredientRepository=ingredientRepository;
        this.orderRepository=orderRepository;
    }


    public List<Ingredient> getAllIngredients() {
        return ingredientRepository.findAll();
    }

    public Ingredient getIngredientById(UUID IngredientId){
        return ingredientRepository.getIngredientByIngredientId(IngredientId);
    }

    @Transactional
    public void changeQuantityOfIngredient (UUID ingredientId, Integer Quantity){
        Ingredient ingredient = ingredientRepository.findIngredientByIngredientId(ingredientId);
        ingredient.setingredientQuantity(Quantity);
        logger.info("The ingredient quantity was changed for: {} to quantity: {}", ingredient.getingredientName(), Quantity);
        ingredientRepository.save(ingredient);
    }


    public Order stockUpdate (Order order){
        List<OrderIngredient> ingredientList = order.getIngredients().stream().map(i -> new OrderIngredient(i.getId(), i.getOrder(), i.getIngredient(), i.getQuantity())).toList();

        for(int i=0; i <= ingredientList.toArray().length; i++ ){
            OrderIngredient ingredient = ingredientList.get(i);
            if (getIngredientById(ingredient.getIngredient().getingredientId()).getingredientQuantity() < ingredient.getQuantity()){
                order.setOrderStatus(OrderStatus.FAILED);
                orderRepository.save(order);
                break;
            } else if (i == ingredientList.toArray().length ) {
                order.setOrderStatus(OrderStatus.SUCCESS);
                orderRepository.save(order);
                break;
            }
            i++;
        }

        if (order.getOrderStatus() == OrderStatus.SUCCESS){
            for(int i=0; i <= ingredientList.toArray().length; i++ ){
                OrderIngredient orderIngredient = ingredientList.get(i);
                Ingredient dbIngredient = getIngredientById(orderIngredient.getIngredient().getingredientId());//.getingredientQuantity();
                dbIngredient.setingredientQuantity(dbIngredient.getingredientQuantity() - orderIngredient.getQuantity());

                ingredientRepository.save(dbIngredient);
                i++;
            }
        }
        return order;
    }
}

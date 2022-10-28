package dev.hilla.runtime.transfertypes;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * A DTO for {@link org.springframework.data.domain.Sort}.
 */
public class Sort {
    @Nonnull
    private List<Order> orders = new ArrayList<>();

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

}

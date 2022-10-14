package dev.hilla.runtime.transfertypes;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotBlank;

import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.NullHandling;

/**
 * A DTO for {@link org.springframework.data.domain.Sort.Order}.
 */
public class Order {
    @Nonnull
    private Direction direction;
    private boolean ignoreCase;
    private NullHandling nullHandling;
    @Nonnull
    @NotBlank
    private String property;

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public NullHandling getNullHandling() {
        return nullHandling;
    }

    public void setNullHandling(NullHandling nullHandling) {
        this.nullHandling = nullHandling;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

}

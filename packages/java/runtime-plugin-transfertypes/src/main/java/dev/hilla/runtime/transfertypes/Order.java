package dev.hilla.runtime.transfertypes;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotBlank;

import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.NullHandling;

/**
 * A DTO for {@link org.springframework.data.domain.Sort.Order}.
 */
public class Order {
    @Nonnull
    private Direction direction;
    @Nonnull
    @NotBlank
    private String property;
    private boolean ignoreCase;
    private NullHandling nullHandling;

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
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

    public NullHandling getNullHandling() {
        return nullHandling;
    }

    public void setNullHandling(NullHandling nullHandling) {
        this.nullHandling = nullHandling;
    }

}

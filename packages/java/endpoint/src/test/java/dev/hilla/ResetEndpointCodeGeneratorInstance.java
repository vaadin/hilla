package dev.hilla;

import java.lang.reflect.Field;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
@Order(-1)
public class ResetEndpointCodeGeneratorInstance {

    public ResetEndpointCodeGeneratorInstance() {
        try {
            Field f = EndpointCodeGenerator.class.getDeclaredField("instance");
            f.setAccessible(true);
            f.set(null, null);
        } catch (NoSuchFieldException | SecurityException
                | IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }
}

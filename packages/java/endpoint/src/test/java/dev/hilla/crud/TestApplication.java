package dev.hilla.crud;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

@SpringBootApplication
@Import(CrudConfiguration.class)
public class TestApplication {
}

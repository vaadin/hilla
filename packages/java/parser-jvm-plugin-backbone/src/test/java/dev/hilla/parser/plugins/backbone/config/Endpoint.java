<<<<<<<< HEAD:packages/java/parser-jvm-plugin-backbone/src/test/java/dev/hilla/parser/plugins/backbone/config/Endpoint.java
package dev.hilla.parser.plugins.backbone.config;
========
package dev.hilla.parser.plugins.backbone.jackson;
>>>>>>>> main:packages/java/parser-jvm-plugin-backbone/src/test/java/dev/hilla/parser/plugins/backbone/jackson/Endpoint.java

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Endpoint {
}

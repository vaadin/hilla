/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.example.application.service;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * A constraint of this application, which the TypeScript generator knows
 * nothing about. The browser therefore sends a value that breaks it and only
 * the running application rejects it, which is what makes this the constraint
 * that covers the validation on the server: in a native image the validator
 * below is only found and instantiated when it is reachable through reflection.
 */
@Target(FIELD)
@Retention(RUNTIME)
@Constraint(validatedBy = NotReserved.Validator.class)
public @interface NotReserved {

    String RESERVED_NAME = "Reserved";

    String message() default "must not be the reserved name";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<NotReserved, String> {

        @Override
        public boolean isValid(String value, ConstraintValidatorContext ctx) {
            return !RESERVED_NAME.equalsIgnoreCase(value);
        }

    }

}

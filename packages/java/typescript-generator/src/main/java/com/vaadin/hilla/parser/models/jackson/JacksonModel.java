/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.hilla.parser.models.jackson;

import java.util.Optional;

import tools.jackson.databind.introspect.BeanPropertyDefinition;

import com.vaadin.hilla.parser.models.AnnotatedAbstractModel;
import com.vaadin.hilla.parser.models.AnnotatedModel;
import com.vaadin.hilla.parser.models.Model;

public abstract class JacksonModel<F extends Model, G extends Model, S extends Model>
        extends AnnotatedAbstractModel implements Model, AnnotatedModel {
    protected final BeanPropertyDefinition origin;
    private Optional<F> field;
    private Optional<G> getter;
    private Optional<S> setter;

    protected JacksonModel(BeanPropertyDefinition origin) {
        this.origin = origin;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof JacksonModel<?, ?, ?>)) {
            return false;
        }

        var other = (JacksonModel<?, ?, ?>) obj;

        return origin.getName().equals(other.origin.getName())
                && getField().equals(other.getField())
                && getGetter().equals(other.getGetter())
                && getSetter().equals(other.getSetter());
    }

    @Override
    public BeanPropertyDefinition get() {
        return origin;
    }

    public Optional<F> getField() {
        if (field == null) {
            field = Optional.ofNullable(prepareField());
        }

        return field;
    }

    public Optional<G> getGetter() {
        if (getter == null) {
            getter = Optional.ofNullable(prepareGetter());
        }

        return getter;
    }

    public Optional<S> getSetter() {
        if (setter == null) {
            setter = Optional.ofNullable(prepareSetter());
        }

        return setter;
    }

    public boolean hasField() {
        return origin.hasField();
    }

    public boolean hasGetter() {
        return origin.hasGetter();
    }

    public boolean hasSetter() {
        return origin.hasSetter();
    }

    @Override
    public int hashCode() {
        return (origin.getName().hashCode() + getField().hashCode()
                + getGetter().hashCode() + getSetter().hashCode()) ^ 0x10e6f7b;
    }

    protected abstract F prepareField();

    protected abstract G prepareGetter();

    protected abstract S prepareSetter();
}

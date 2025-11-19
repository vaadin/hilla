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
package com.vaadin.flow.connect;

import java.util.List;
import java.util.Objects;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.Endpoint;

/**
 *
 * {@link org.springframework.data.domain.Page} data.
 */
@Endpoint
@AnonymousAllowed
public class PagedEndpoint {
    private static List<Entity> DATA = List.of(new Entity("Foo", 30),
            new Entity("Bar", 20), new Entity("Baz", 10));

    public Page<@NonNull Entity> list(@Nullable Pageable p) {
        if (p == null) {
            p = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "qty"));
        }

        var toIndex = Math.min(p.getOffset() + p.getPageSize(), DATA.size());
        var content = DATA.subList((int) p.getOffset(), (int) toIndex);
        return new PageImpl<>(content, p, DATA.size());
    }

    public PageOfEntities listEntities(@Nullable Pageable p) {
        return new PageOfEntities(list(p));
    }

    /**
     * Page content entity.
     */
    public static class Entity {
        private final String name;

        private final int qty;

        public Entity(@NonNull String name, int qty) {
            this.name = Objects.requireNonNull(name);
            this.qty = qty;
        }

        @NonNull
        public String getName() {
            return name;
        }

        @NonNull
        public int getQty() {
            return qty;
        }
    }

    /**
     * Wrapper DTO for Page of Entity entries with Pageable.
     */
    public static class PageOfEntities {
        private final Page<Entity> page;
        private final Pageable pageable;

        public PageOfEntities(@NonNull Page<Entity> page,
                @NonNull Pageable pageable) {
            this.page = page;
            this.pageable = pageable;
        }

        public PageOfEntities(@NonNull Page<Entity> page) {
            this(page, page.getPageable());
        }

        public Page<Entity> getPage() {
            return page;
        }

        public Pageable getPageable() {
            return pageable;
        }
    }
}

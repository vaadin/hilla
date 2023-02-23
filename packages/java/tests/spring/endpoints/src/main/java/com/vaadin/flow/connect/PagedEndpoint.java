package com.vaadin.flow.connect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import dev.hilla.Endpoint;
import dev.hilla.Nonnull;
import jakarta.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;

import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * 
 * {@link org.springframework.data.domain.Page} data.
 */
@Endpoint
@AnonymousAllowed
public class PagedEndpoint {
    private static List<Entity> DATA = List.of(new Entity("Foo", 30),
            new Entity("Bar", 20), new Entity("Baz", 10));

    public Page<@Nonnull Entity> list(@Nullable Pageable p) {
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

        public Entity(@Nonnull String name, int qty) {
            this.name = Objects.requireNonNull(name);
            this.qty = qty;
        }

        @Nonnull
        public String getName() {
            return name;
        }

        @Nonnull
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

        public PageOfEntities(@Nonnull Page<Entity> page,
                @Nonnull Pageable pageable) {
            this.page = page;
            this.pageable = pageable;
        }

        public PageOfEntities(@Nonnull Page<Entity> page) {
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

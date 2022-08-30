package dev.hilla.parser.core;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class ScanLocation {
    private final List<ScanItem> context;
    private final List<ScanItem> next;

    public ScanLocation(@Nonnull List<ScanItem> context,
        @Nonnull List<ScanItem> next) {
        this.context =
            Collections.unmodifiableList(Objects.requireNonNull(context));
        this.next = Collections.unmodifiableList(Objects.requireNonNull(next));
    }

    @Nonnull
    public Optional<ScanItem> getCurrent() {
        return context.stream().findFirst();
    }

    public List<ScanItem> getContext() {
        return context;
    }

    public List<ScanItem> getNext() {
        return next;
    }

    @Override
    public boolean equals(Object another) {
        if (another instanceof ScanLocation) {
            return ((ScanLocation) another).getContext().equals(context) && ((ScanLocation) another).getNext().equals(next);
        }
        return super.equals(another);
    }

    @Override
    public int hashCode() {
        return 0x817d2127 ^ context.hashCode() ^ next.hashCode();
    }
}

package dev.hilla.test.reactgrid.entityreferences;

public record DepartmentReference(Long id, String name) {
    public static DepartmentReference fromEntity(Department entity) {
        return new DepartmentReference(entity.getId(), entity.getName());
    }
}

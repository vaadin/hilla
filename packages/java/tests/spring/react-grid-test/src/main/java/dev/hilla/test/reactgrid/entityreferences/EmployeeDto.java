package dev.hilla.test.reactgrid.entityreferences;

public record EmployeeDto(Long id, String name, AddressDto address, DepartmentReference department) {
    public static EmployeeDto fromEntity(Employee entity) {
        return new EmployeeDto(
                entity.getId(),
                entity.getName(),
                AddressDto.fromEntity(entity.getHomeAddress()),
                DepartmentReference.fromEntity(entity.getDepartment())
        );
    }
}

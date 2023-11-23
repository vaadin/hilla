package dev.hilla.test.reactgrid.entityreferences;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.hilla.BrowserCallable;
import dev.hilla.Nullable;
import dev.hilla.crud.CrudService;
import dev.hilla.crud.JpaFilterConverter;
import dev.hilla.crud.filter.Filter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

@BrowserCallable
@AnonymousAllowed
public class EmployeeDtoService implements CrudService<EmployeeDto, Long> {
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final AddressRepository addressRepository;
    private final JpaFilterConverter jpaFilterConverter;

    public EmployeeDtoService(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository, AddressRepository addressRepository, JpaFilterConverter jpaFilterConverter) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.addressRepository = addressRepository;
        this.jpaFilterConverter = jpaFilterConverter;
    }

    @Override
    public List<EmployeeDto> list(Pageable pageable, @Nullable Filter filter) {
        Specification<Employee> spec = jpaFilterConverter.toSpec(filter, Employee.class);
        return employeeRepository.findAll(spec, pageable).stream().map(EmployeeDto::fromEntity).toList();
    }

    @Override
    public @Nullable EmployeeDto save(EmployeeDto value) {
        Long id = value.id();
        Employee employee = id != null && id > 0 ? employeeRepository.findById(value.id()).orElseThrow() : new Employee();
        employee.setName(value.name());

        if (employee.getHomeAddress() == null) {
            employee.setHomeAddress(new Address());
        }
        employee.getHomeAddress().setStreetAddress(value.address().street());
        employee.getHomeAddress().setCity(value.address().city());
        employee.getHomeAddress().setCountry(value.address().country());
        addressRepository.save(employee.getHomeAddress());

        Department department = departmentRepository.findById(value.department().id()).orElseThrow();
        employee.setDepartment(department);

        employee = employeeRepository.save(employee);

        return EmployeeDto.fromEntity(employee);
    }

    @Override
    public void delete(Long aLong) {

    }

    @Override
    public EmployeeDto get(Long aLong) {
        return null;
    }
}

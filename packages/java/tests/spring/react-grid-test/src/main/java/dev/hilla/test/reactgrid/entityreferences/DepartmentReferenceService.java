package dev.hilla.test.reactgrid.entityreferences;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.hilla.BrowserCallable;

import java.util.List;

@BrowserCallable
@AnonymousAllowed
public class DepartmentReferenceService {
    private final DepartmentRepository departmentRepository;

    public DepartmentReferenceService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public List<DepartmentReference> listAll() {
        return departmentRepository.findAll().stream().map(DepartmentReference::fromEntity).toList();
    }
}

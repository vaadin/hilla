package dev.hilla.test.reactgrid;

import dev.hilla.BrowserCallable;
import dev.hilla.crud.CrudRepositoryService;
import org.springframework.stereotype.Service;

import com.vaadin.flow.server.auth.AnonymousAllowed;

@BrowserCallable
@Service
@AnonymousAllowed
public class AppointmentService
        extends CrudRepositoryService<Appointment, Long> {

    public AppointmentService(AppointmentRepository repository) {
        super(Appointment.class, repository);
    }
}

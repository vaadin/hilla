package com.vaadin.hilla.test.reactgrid;

import com.vaadin.hilla.BrowserCallable;
import com.vaadin.hilla.crud.CrudRepositoryService;
import org.springframework.stereotype.Service;

import com.vaadin.flow.server.auth.AnonymousAllowed;

@BrowserCallable
@Service
@AnonymousAllowed
public class AppointmentService extends
        CrudRepositoryService<Appointment, Long, AppointmentRepository> {
}

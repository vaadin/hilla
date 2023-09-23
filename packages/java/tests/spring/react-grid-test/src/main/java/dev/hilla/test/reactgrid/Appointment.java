package dev.hilla.test.reactgrid;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
public class Appointment extends AbstractEntity {

    @NotBlank
    private String name;
    @NotBlank
    private String doctor;
    @NotNull
    private LocalDateTime time;
    private boolean sendInvoice;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDoctor() {
        return doctor;
    }

    public void setDoctor(String doctor) {
        this.doctor = doctor;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public boolean isSendInvoice() {
        return sendInvoice;
    }

    public void setSendInvoice(boolean sendInvoice) {
        this.sendInvoice = sendInvoice;
    }
}

package dev.hilla.test.reactgrid;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Appointment extends AbstractEntity {

    @NotBlank
    private String name;
    @NotBlank
    private String doctor;
    private boolean sendInvoice;
    private int age;
    private float rating;

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

    public boolean isSendInvoice() {
        return sendInvoice;
    }

    public void setSendInvoice(boolean sendInvoice) {
        this.sendInvoice = sendInvoice;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
}

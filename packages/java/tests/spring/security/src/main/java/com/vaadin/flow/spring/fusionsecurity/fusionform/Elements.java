package com.vaadin.flow.spring.fusionsecurity.fusionform;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public class Elements {

    private final List<String> checkboxGroup = List.of("item-1");
    private final LocalDate datePicker = LocalDate.now().plusDays(1);
    private final LocalDateTime dateTimePicker = LocalDateTime.now().plusDays(1)
            .plusHours(1);
    @Email
    @NotEmpty
    private String emailField;
    private final LocalTime timePicker = LocalTime.now().plusMinutes(30);

    public Boolean getCheckbox() {
        return true;
    }

    public List<String> getCheckboxGroup() {
        return checkboxGroup;
    }

    public String getComboBox() {
        return "item-1";
    }

    public String getCustomField() {
        return "foo";
    }

    public LocalDate getDatePicker() {
        return datePicker;
    }

    public LocalDateTime getDateTimePicker() {
        return dateTimePicker;
    }

    public String getEmailField() {
        return emailField;
    }

    public Integer getIntegerField() {
        return 12;
    }

    public Integer getListBox() {
        return 1;
    }

    public Double getNumberField() {
        return 12.33d;
    }

    public String getPasswordField() {
        return "bar";
    }

    public Boolean getRadioButton() {
        return false;
    }

    public String getRadioButtonGroup() {
        return "item-2";
    }

    public String getRichText() {
        return "[{\"insert\":\"HTML Ipsum Presents\"},{\"attributes\":{\"header\":3},\"insert\":\"\\n\"},{\"attributes\":{\"bold\":true},\"insert\":\"Pellentesque habitant morbi tristique\"},{\"insert\":\" senectus et netus et malesuada fames ac turpis egestas. Vestibulum tortor quam, feugiat vitae. \"}]";
    }

    public String getSelect() {
        return "item-1";
    }

    public String getTextArea() {
        return "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.";
    }

    public String getTextField() {
        return "foo";
    }

    public LocalTime getTimePicker() {
        return timePicker;
    }

    public enum Options {
        ITEM_1, ITEM_2;

        public String toString() {
            return super.toString().toLowerCase().replace("_", "-");
        }
    }
}

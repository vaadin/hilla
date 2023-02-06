package com.vaadin.flow.spring.fusionsecurity.fusionform;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public class Elements {

    private final Boolean checkbox = true;
    private final List<String> checkboxGroup = List.of("item-1");
    private final String comboBox = "item-1";
    private final String customField = "foo";
    private final LocalDate datePicker = LocalDate.now().plusDays(1);
    private final LocalDateTime dateTimePicker = LocalDateTime.now().plusDays(1)
            .plusHours(1);
    private final Integer integerField = 12;
    private final Integer listBox = 1;
    private final Double numberField = 12.33d;
    private final String passwordField = "bar";
    private final Boolean radioButton = false;
    private final String radioButtonGroup = "item-2";
    private final String richText = "[{\"insert\":\"HTML Ipsum Presents\"},{\"attributes\":{\"header\":3},\"insert\":\"\\n\"},{\"attributes\":{\"bold\":true},\"insert\":\"Pellentesque habitant morbi tristique\"},{\"insert\":\" senectus et netus et malesuada fames ac turpis egestas. Vestibulum tortor quam, feugiat vitae. \"}]";
    private final String select = "item-1";
    private final String textArea = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.";
    private final String textField = "foo";
    private final LocalTime timePicker = LocalTime.now().plusMinutes(30);
    @Email
    @NotEmpty
    private String emailField;

    public Boolean getCheckbox() {
        return checkbox;
    }

    public List<String> getCheckboxGroup() {
        return checkboxGroup;
    }

    public String getComboBox() {
        return comboBox;
    }

    public String getCustomField() {
        return customField;
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
        return integerField;
    }

    public Integer getListBox() {
        return listBox;
    }

    public Double getNumberField() {
        return numberField;
    }

    public String getPasswordField() {
        return passwordField;
    }

    public Boolean getRadioButton() {
        return radioButton;
    }

    public String getRadioButtonGroup() {
        return radioButtonGroup;
    }

    public String getRichText() {
        return richText;
    }

    public String getSelect() {
        return select;
    }

    public String getTextArea() {
        return textArea;
    }

    public String getTextField() {
        return textField;
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

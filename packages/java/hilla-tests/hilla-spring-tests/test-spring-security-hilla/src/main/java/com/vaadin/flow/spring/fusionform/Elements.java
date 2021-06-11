package com.vaadin.flow.spring.fusionform;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

public class Elements {

    public static enum Options {
       ITEM_1,
       ITEM_2;
       public String toString() {
           return super.toString().toLowerCase().replace("_", "-");
       }
    };

    private Boolean radioButton = false;
    private String radioButtonGroup = "item-2";

    private Boolean checkbox = true;
    private List<String> checkboxGroup = Arrays.asList("item-1");

    private String comboBox = "item-1";
    private String select = "item-1";

    private String customField = "foo";
    private String textField = "foo";
    private String passwordField = "bar";
    private Integer integerField = 12;
    private Double numberField = 12.33d;
    
    @Email
    @NotEmpty
    private String emailField;

    private LocalDate datePicker = LocalDate.now().plusDays(1);
    private LocalDateTime dateTimePicker = LocalDateTime.now().plusDays(1).plusHours(1);
    private LocalTime timePicker = LocalTime.now().plusMinutes(30);

    private String textArea = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.";
    private Integer listBox = 1;
    private String richText =
      "[{\"insert\":\"HTML Ipsum Presents\"},{\"attributes\":{\"header\":3},\"insert\":\"\\n\"},{\"attributes\":{\"bold\":true},\"insert\":\"Pellentesque habitant morbi tristique\"},{\"insert\":\" senectus et netus et malesuada fames ac turpis egestas. Vestibulum tortor quam, feugiat vitae. \"}]";
}

package com.vaadin.hilla.signals;

import java.util.Objects;

public class Person {
    private String name;
    private int age;
    private boolean adult;

    public Person(String name, int age, boolean adult) {
        this.name = name;
        this.age = age;
        this.adult = adult;
    }

    public Person() {
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public boolean isAdult() {
        return adult;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setAdult(boolean adult) {
        this.adult = adult;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Person person))
            return false;
        return getAge() == person.getAge() && isAdult() == person.isAdult()
                && Objects.equals(getName(), person.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getAge(), isAdult());
    }
}

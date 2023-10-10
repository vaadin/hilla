package dev.hilla.crud;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class NestedObject {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String name;

    private long luckyNumber;

    @OneToOne
    private SecondLevelNestedObject secondLevelNestedObject;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getLuckyNumber() {
        return luckyNumber;
    }

    public void setLuckyNumber(long luckyNumber) {
        this.luckyNumber = luckyNumber;
    }

    public SecondLevelNestedObject getSecondLevelNestedObject() {
        return secondLevelNestedObject;
    }

    public void setSecondLevelNestedObject(
            SecondLevelNestedObject secondLevelNestedObject) {
        this.secondLevelNestedObject = secondLevelNestedObject;
    }
}

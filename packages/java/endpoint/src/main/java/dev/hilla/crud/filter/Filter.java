package dev.hilla.crud.filter;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "t")
@JsonSubTypes({ @Type(value = OrFilter.class, name = "o"),
        @Type(value = AndFilter.class, name = "a"),
        @Type(value = PropertyStringFilter.class, name = "p") })
public interface Filter {

}

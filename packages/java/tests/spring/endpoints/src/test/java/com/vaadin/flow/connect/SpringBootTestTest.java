package com.vaadin.flow.connect;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
// https://github.com/vaadin/flow/issues/9792
public class SpringBootTestTest {

    @Test
    public void hello() {
        // To test if @SpringBootTest works
        Assert.assertTrue(true);
    }
}

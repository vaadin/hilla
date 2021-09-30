package com.vaadin.fusion.parser.basic;

@Endpoint
public class BasicEndpoint {
  public final String foo = "FOO";
  private int bar = 111;

  public String getFoo() {
    return foo;
  }

  protected void baz(final int id) {}

  private int getBar() {
    return bar;
  }
}

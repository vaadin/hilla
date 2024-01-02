package com.vaadin.hilla.csrftest;

import jakarta.servlet.annotation.WebServlet;

import com.vaadin.flow.server.VaadinServlet;

@WebServlet("/*")
public class MyServlet extends VaadinServlet {

}

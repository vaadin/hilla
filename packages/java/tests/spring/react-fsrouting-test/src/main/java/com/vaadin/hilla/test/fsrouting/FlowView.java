package com.vaadin.hilla.test.fsrouting;

import com.vaadin.flow.component.HtmlContainer;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.router.Route;

@Route("flow")
public class FlowView extends HtmlContainer {

    public FlowView() {
        super("flow-view", new Text("Hello from FlowView"));
    }


}

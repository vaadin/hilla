package dev.hilla.push.messages.fromclient;

import com.fasterxml.jackson.databind.node.ArrayNode;

public class SubscribeMessage extends AbstractServerMessage {

    private String endpointName, methodName;
    private ArrayNode params;

    public String getEndpointName() {
        return endpointName;
    }

    public void setEndpointName(String endpointName) {
        this.endpointName = endpointName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public ArrayNode getParams() {
        return params;
    }

    public void setParams(ArrayNode params) {
        this.params = params;
    }

}

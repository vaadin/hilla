package com.vaadin.flow.server.connect.generator.endpoints.packageprivate;

import com.vaadin.flow.server.connect.Endpoint;
import com.vaadin.flow.server.connect.auth.AnonymousAllowed;

/**
 * com.vaadin.flow.server.connect.generator.endpoints.packageprivate.PackagePrivateEndpoint, created on 03/12/2020 18.22
 * @author nikolaigorokhov
 */
@Endpoint
@AnonymousAllowed
class PackagePrivateEndpoint {

  public PackagePrivateEndpoint() {}

  public String getRequest() {
    return "Hello";
  }
}

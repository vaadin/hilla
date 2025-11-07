package com.vaadin.hilla.parser.plugins.transfertypes.multipartfilechecker;

import com.vaadin.hilla.parser.testutils.annotations.Endpoint;
import org.springframework.web.multipart.MultipartFile;

@Endpoint
public class MultipartFileAsReturnTypeEndpoint {

    public MultipartFile downloadFile() {
        return null;
    }
}

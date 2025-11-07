package com.vaadin.hilla.parser.plugins.transfertypes.file;

import com.vaadin.hilla.parser.testutils.annotations.Endpoint;
import org.springframework.web.multipart.MultipartFile;

@Endpoint
public class MultipartFileEndpoint {
    public void uploadFile(MultipartFile file) {
    }
}

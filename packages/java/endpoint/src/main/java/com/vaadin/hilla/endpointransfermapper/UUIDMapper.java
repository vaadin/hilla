/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.hilla.endpointransfermapper;

import java.util.UUID;

/**
 * A mapper between {@link UUID} and {@link String}.
 */
public class UUIDMapper implements EndpointTransferMapper.Mapper<UUID, String> {

    @Override
    public String toTransferType(UUID uuid) {
        return uuid.toString();
    }

    @Override
    public UUID toEndpointType(String string) {
        return UUID.fromString(string);
    }

    @Override
    public Class<? extends UUID> getEndpointType() {
        return UUID.class;
    }

    @Override
    public Class<? extends String> getTransferType() {
        return String.class;
    }

}

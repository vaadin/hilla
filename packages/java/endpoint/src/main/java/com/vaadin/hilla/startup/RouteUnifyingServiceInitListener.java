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

package com.vaadin.hilla.startup;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.hilla.route.RouteUnifyingIndexHtmlRequestListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Service init listener to add the
 * {@link RouteUnifyingIndexHtmlRequestListener} to the service.
 */
@Component
public class RouteUnifyingServiceInitListener
        implements VaadinServiceInitListener {

    private final RouteUnifyingIndexHtmlRequestListener routeUnifyingIndexHtmlRequestListener;

    /**
     * Creates a new instance of the listener.
     *
     * @param routeUnifyingIndexHtmlRequestListener
     *            the listener to add
     */
    @Autowired
    public RouteUnifyingServiceInitListener(
            RouteUnifyingIndexHtmlRequestListener routeUnifyingIndexHtmlRequestListener) {
        this.routeUnifyingIndexHtmlRequestListener = routeUnifyingIndexHtmlRequestListener;
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.addIndexHtmlRequestListener(
                routeUnifyingIndexHtmlRequestListener);
    }
}

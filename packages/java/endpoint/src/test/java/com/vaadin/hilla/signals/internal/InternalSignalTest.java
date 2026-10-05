/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.hilla.signals.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import com.vaadin.flow.signals.Id;
import com.vaadin.flow.signals.shared.SharedNumberSignal;

public class InternalSignalTest {

    private static final String CLIENT_ID = "client-1";
    private static final String OTHER_CLIENT_ID = "client-2";

    private final ObjectMapper mapper = new ObjectMapper();

    private ObjectNode incrementCommand(String targetNodeId, double delta) {
        ObjectNode command = mapper.createObjectNode();
        command.put("@type", "inc");
        command.put("commandId", Id.random().asBase64());
        command.put("targetNodeId", targetNodeId);
        command.put("delta", delta);
        return command;
    }

    private List<JsonNode> collect(InternalSignal signal, String clientId) {
        List<JsonNode> received = new ArrayList<>();
        signal.subscribe(clientId).subscribe(received::add);
        return received;
    }

    @Test
    public void acceptedCommand_isEmittedToAllSubscribersAsAccepted() {
        var signal = new InternalSignal(new SharedNumberSignal(0.0), mapper);
        var first = collect(signal, CLIENT_ID);
        var second = collect(signal, OTHER_CLIENT_ID);

        signal.submit(CLIENT_ID, incrementCommand(Id.ZERO.asBase64(), 2));

        // Both clients get the snapshot on subscribe and the command after it
        assertEquals(2, first.size());
        assertEquals(2, second.size());

        for (var received : List.of(first.get(1), second.get(1))) {
            assertEquals("inc", received.get("@type").asText());
            assertTrue(received.get("accepted").asBoolean());
            assertFalse(received.has("reason"));
        }
    }

    @Test
    public void rejectedCommand_isEmittedOnlyToTheSubmittingClient() {
        var signal = new InternalSignal(new SharedNumberSignal(0.0), mapper);
        var submitter = collect(signal, CLIENT_ID);
        var other = collect(signal, OTHER_CLIENT_ID);

        // There is no such node, so the command cannot be applied
        signal.submit(CLIENT_ID, incrementCommand(Id.random().asBase64(), 2));

        // The client that submitted the command needs to know that it was
        // rejected so that it can revert the change it applied optimistically
        assertEquals(2, submitter.size());
        var rejection = submitter.get(1);
        assertEquals("inc", rejection.get("@type").asText());
        assertFalse(rejection.get("accepted").asBoolean());
        assertNotNull(rejection.get("reason"));

        // The other client never saw the change, so it is not notified
        assertEquals(1, other.size());
    }

    @Test
    public void serverOriginatedCommand_isEmittedToSubscribers() {
        var numberSignal = new SharedNumberSignal(0.0);
        var signal = new InternalSignal(numberSignal, mapper);
        var received = collect(signal, CLIENT_ID);

        // A command that is not submitted by any client has no submitted JSON
        // to reuse, so it has to be serialized from the processed command
        numberSignal.incrementBy(5);

        assertEquals(2, received.size());
        var command = received.get(1);
        assertEquals("inc", command.get("@type").asText());
        assertEquals(5.0, command.get("delta").asDouble(), 0.0);
        assertTrue(command.get("accepted").asBoolean());
    }
}

import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type DeclaredBase_1 from "./com/vaadin/hilla/parser/plugins/subtypes/existingproperty/DeclaredBase.js";
import type EnumDiscriminated_1 from "./com/vaadin/hilla/parser/plugins/subtypes/existingproperty/EnumDiscriminated.js";
import type InheritedBase_1 from "./com/vaadin/hilla/parser/plugins/subtypes/existingproperty/InheritedBase.js";
import client_1 from "./connect-client.default.js";
async function send_1(init?: EndpointRequestInit_1): Promise<DeclaredBase_1 | undefined> { return client_1.call("ExistingPropertyEndpoint", "send", {}, init); }
async function sendEnumDiscriminated_1(init?: EndpointRequestInit_1): Promise<EnumDiscriminated_1 | undefined> { return client_1.call("ExistingPropertyEndpoint", "sendEnumDiscriminated", {}, init); }
async function sendInherited_1(init?: EndpointRequestInit_1): Promise<InheritedBase_1 | undefined> { return client_1.call("ExistingPropertyEndpoint", "sendInherited", {}, init); }
export { send_1 as send, sendEnumDiscriminated_1 as sendEnumDiscriminated, sendInherited_1 as sendInherited };

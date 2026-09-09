import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type DeclaredBase_1 from "./com/vaadin/hilla/parser/plugins/subtypes/existingproperty/DeclaredBase.js";
import type EnumDiscriminated_1 from "./com/vaadin/hilla/parser/plugins/subtypes/existingproperty/EnumDiscriminated.js";
import type InheritedEnum_1 from "./com/vaadin/hilla/parser/plugins/subtypes/existingproperty/InheritedEnum.js";
import client_1 from "./connect-client.default.js";
async function send_1(init?: EndpointRequestInit_1): Promise<DeclaredBase_1 | undefined> { return client_1.call("ExistingPropertyEndpoint", "send", {}, init); }
async function sendEnumDiscriminated_1(init?: EndpointRequestInit_1): Promise<EnumDiscriminated_1 | undefined> { return client_1.call("ExistingPropertyEndpoint", "sendEnumDiscriminated", {}, init); }
async function sendInheritedEnum_1(init?: EndpointRequestInit_1): Promise<InheritedEnum_1 | undefined> { return client_1.call("ExistingPropertyEndpoint", "sendInheritedEnum", {}, init); }
export { send_1 as send, sendEnumDiscriminated_1 as sendEnumDiscriminated, sendInheritedEnum_1 as sendInheritedEnum };

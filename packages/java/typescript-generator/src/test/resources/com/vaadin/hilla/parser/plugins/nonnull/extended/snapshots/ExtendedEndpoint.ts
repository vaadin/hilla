import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type Entity_1 from "./com/vaadin/hilla/parser/plugins/nonnull/extended/ExtendedEndpoint/Entity.js";
import client_1 from "./connect-client.default.js";
async function getNonnullListOfNullableElements_1(init?: EndpointRequestInit_1): Promise<Array<Entity_1 | undefined>> { return client_1.call("ExtendedEndpoint", "getNonnullListOfNullableElements", {}, init); }
async function superComplexType_1(list: Array<Record<string, Array<Record<string, string> | undefined> | undefined> | undefined> | undefined, init?: EndpointRequestInit_1): Promise<Array<Record<string, Array<Record<string, string> | undefined> | undefined> | undefined> | undefined> { return client_1.call("ExtendedEndpoint", "superComplexType", { list }, init); }
export { getNonnullListOfNullableElements_1 as getNonnullListOfNullableElements, superComplexType_1 as superComplexType };

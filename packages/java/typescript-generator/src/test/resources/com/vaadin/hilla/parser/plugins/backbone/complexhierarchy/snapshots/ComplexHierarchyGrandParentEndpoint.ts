import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import client from "./connect-client.default.js";
async function executeGrandParentEndpointMethod(init?: EndpointRequestInit): Promise<string | undefined> { return client.call("ComplexHierarchyGrandParentEndpoint", "executeGrandParentEndpointMethod", {}, init); }
export { executeGrandParentEndpointMethod };

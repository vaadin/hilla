import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import client_1 from "./connect-client.default.js";
async function executeGrandParentEndpointMethod_1(init?: EndpointRequestInit_1): Promise<string | undefined> { return client_1.call("ComplexHierarchyGrandParentEndpoint", "executeGrandParentEndpointMethod", {}, init); }
export { executeGrandParentEndpointMethod_1 as executeGrandParentEndpointMethod };


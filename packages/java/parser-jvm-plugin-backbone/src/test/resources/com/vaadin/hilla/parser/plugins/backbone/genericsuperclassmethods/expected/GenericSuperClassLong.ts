import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import client_1 from "./connect-client.default.js";
async function genericMethod_1(param: number | undefined, init?: EndpointRequestInit_1): Promise<number | undefined> { return client_1.call("GenericSuperClassLong", "genericMethod", { param }, init); }
export { genericMethod_1 as genericMethod };


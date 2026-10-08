import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import client from "./connect-client.default.js";
async function genericMethod(param: number | undefined, init?: EndpointRequestInit): Promise<number | undefined> { return client.call("GenericSuperClassLong", "genericMethod", { param }, init); }
export { genericMethod };

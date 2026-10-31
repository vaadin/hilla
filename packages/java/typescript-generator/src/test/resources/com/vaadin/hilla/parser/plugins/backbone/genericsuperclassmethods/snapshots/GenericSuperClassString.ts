import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import client from "./connect-client.default.js";
async function genericMethod(param: string | undefined, init?: EndpointRequestInit): Promise<string | undefined> { return client.call("GenericSuperClassString", "genericMethod", { param }, init); }
async function methodTypeParameter(param: string | undefined, init?: EndpointRequestInit): Promise<unknown> { return client.call("GenericSuperClassString", "methodTypeParameter", { param }, init); }
export { genericMethod, methodTypeParameter };

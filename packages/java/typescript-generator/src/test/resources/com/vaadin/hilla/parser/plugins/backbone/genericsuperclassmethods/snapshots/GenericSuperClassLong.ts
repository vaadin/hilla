import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import client from "./connect-client.default.js";
async function genericMethod(param: number | undefined, init?: EndpointRequestInit): Promise<number | undefined> { return client.call("GenericSuperClassLong", "genericMethod", { param }, init); }
async function methodTypeParameter(param: number | undefined, init?: EndpointRequestInit): Promise<unknown> { return client.call("GenericSuperClassLong", "methodTypeParameter", { param }, init); }
export { genericMethod, methodTypeParameter };

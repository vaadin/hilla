import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import client from "./connect-client.default.js";
async function getMap(map: Record<string, unknown> | undefined, init?: EndpointRequestInit): Promise<Record<string, unknown> | undefined> { return client.call("GenericsExtendedEndpoint", "getMap", { map }, init); }
export { getMap };

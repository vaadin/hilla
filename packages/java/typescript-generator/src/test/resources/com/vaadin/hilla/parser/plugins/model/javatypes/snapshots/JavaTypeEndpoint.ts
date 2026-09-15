import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import type JavaTypeTestEntity from "./com/vaadin/hilla/parser/plugins/model/javatypes/JavaTypeEndpoint/JavaTypeTestEntity.js";
import client from "./connect-client.default.js";
async function getTestEntity(init?: EndpointRequestInit): Promise<JavaTypeTestEntity | undefined> { return client.call("JavaTypeEndpoint", "getTestEntity", {}, init); }
export { getTestEntity };

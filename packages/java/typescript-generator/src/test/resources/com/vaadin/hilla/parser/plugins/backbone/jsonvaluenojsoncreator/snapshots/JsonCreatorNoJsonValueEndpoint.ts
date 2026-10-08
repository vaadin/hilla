import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import type User from "./com/vaadin/hilla/parser/plugins/backbone/jsonvaluenojsoncreator/JsonCreatorNoJsonValueEndpoint/User.js";
import client from "./connect-client.default.js";
async function getUser(init?: EndpointRequestInit): Promise<User | undefined> { return client.call("JsonCreatorNoJsonValueEndpoint", "getUser", {}, init); }
async function setUser(user: User | undefined, init?: EndpointRequestInit): Promise<void> { return client.call("JsonCreatorNoJsonValueEndpoint", "setUser", { user }, init); }
export { getUser, setUser };

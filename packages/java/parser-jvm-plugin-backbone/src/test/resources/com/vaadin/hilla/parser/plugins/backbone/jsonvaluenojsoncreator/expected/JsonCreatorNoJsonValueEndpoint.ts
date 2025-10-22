import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type User_1 from "./com/vaadin/hilla/parser/plugins/backbone/jsonvaluenojsoncreator/JsonCreatorNoJsonValueEndpoint/User.js";
import client_1 from "./connect-client.default.js";
async function getUser_1(init?: EndpointRequestInit_1): Promise<User_1 | undefined> { return client_1.call("JsonCreatorNoJsonValueEndpoint", "getUser", {}, init); }
async function setUser_1(user: User_1 | undefined, init?: EndpointRequestInit_1): Promise<void> { return client_1.call("JsonCreatorNoJsonValueEndpoint", "setUser", { user }, init); }
export { getUser_1 as getUser, setUser_1 as setUser };


import { EndpointRequestInit as EndpointRequestInit_1, Subscription as Subscription_1 } from "@hilla/frontend";
import client_1 from "./connect-client.default";
import type UserEntity_1 from "./dev/hilla/parser/plugins/backbone/pushtype/PushTypeEndpoint/UserEntity";
async function getAllUsers_1(init?: EndpointRequestInit_1): Promise<Subscription_1<UserEntity_1 | undefined> | undefined> { return client_1.subscribe("PushTypeEndpoint", "getAllUsers", {}); }
async function getAllUsersNonnull_1(init?: EndpointRequestInit_1): Promise<Subscription_1<UserEntity_1>> { return client_1.subscribe("PushTypeEndpoint", "getAllUsersNonnull", {}); }
export { getAllUsers_1 as getAllUsers, getAllUsersNonnull_1 as getAllUsersNonnull };

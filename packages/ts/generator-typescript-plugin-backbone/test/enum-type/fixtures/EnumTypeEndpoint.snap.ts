import { EndpointRequestInit as EndpointRequestInit_1 } from "@hilla/frontend";
import client_1 from "./connect-client.default.js";
import type EnumEntity_1 from "./dev/hilla/parser/plugins/backbone/enumtype/EnumTypeEndpoint/EnumEntity.js";
async function echoEnum_1(value: EnumEntity_1 | undefined, init?: EndpointRequestInit_1): Promise<EnumEntity_1 | undefined> { return client_1.call("EnumTypeEndpoint", "echoEnum", { value }, init); }
async function echoListEnum_1(enumList: Array<EnumEntity_1 | undefined> | undefined, init?: EndpointRequestInit_1): Promise<Array<EnumEntity_1 | undefined> | undefined> { return client_1.call("EnumTypeEndpoint", "echoListEnum", { enumList }, init); }
async function getEnum_1(init?: EndpointRequestInit_1): Promise<EnumEntity_1 | undefined> { return client_1.call("EnumTypeEndpoint", "getEnum", {}, init); }
async function setEnum_1(value: EnumEntity_1 | undefined, init?: EndpointRequestInit_1): Promise<void> { return client_1.call("EnumTypeEndpoint", "setEnum", { value }, init); }
export { echoEnum_1 as echoEnum, echoListEnum_1 as echoListEnum, getEnum_1 as getEnum, setEnum_1 as setEnum };

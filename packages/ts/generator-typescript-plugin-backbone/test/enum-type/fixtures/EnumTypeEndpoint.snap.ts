import client_1 from "./connect-client.default";
import type EnumEntity_1 from "./dev/hilla/parser/plugins/backbone/enumtype/EnumTypeEndpoint/EnumEntity";
async function echoEnum_1(value: EnumEntity_1 | undefined): Promise<EnumEntity_1 | undefined> { return client_1.call("EnumTypeEndpoint", "echoEnum", { value }); }
async function echoListEnum_1(enumList: Array<EnumEntity_1 | undefined> | undefined): Promise<Array<EnumEntity_1 | undefined> | undefined> { return client_1.call("EnumTypeEndpoint", "echoListEnum", { enumList }); }
async function getEnum_1(): Promise<EnumEntity_1 | undefined> { return client_1.call("EnumTypeEndpoint", "getEnum"); }
async function setEnum_1(value: EnumEntity_1 | undefined): Promise<void> { client_1.call("EnumTypeEndpoint", "setEnum", { value }); }
export { echoEnum_1 as echoEnum, echoListEnum_1 as echoListEnum, getEnum_1 as getEnum, setEnum_1 as setEnum };

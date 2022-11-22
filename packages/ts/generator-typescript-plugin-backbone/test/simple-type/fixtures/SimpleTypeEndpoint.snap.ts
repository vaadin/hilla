import { EndpointRequestInit as EndpointRequestInit_1 } from "@hilla/frontend";
import client_1 from "./connect-client.default";
async function getArray_1(init?: EndpointRequestInit_1): Promise<Array<number> | undefined> { return client_1.call("SimpleTypeEndpoint", "getArray", {}, init); }
async function getBigDecimal_1(init?: EndpointRequestInit_1): Promise<number | undefined> { return client_1.call("SimpleTypeEndpoint", "getBigDecimal", {}, init); }
async function getBigInteger_1(init?: EndpointRequestInit_1): Promise<number | undefined> { return client_1.call("SimpleTypeEndpoint", "getBigInteger", {}, init); }
async function getBoolean_1(init?: EndpointRequestInit_1): Promise<boolean> { return client_1.call("SimpleTypeEndpoint", "getBoolean", {}, init); }
async function getBooleanWrapper_1(init?: EndpointRequestInit_1): Promise<boolean | undefined> { return client_1.call("SimpleTypeEndpoint", "getBooleanWrapper", {}, init); }
async function getByte_1(init?: EndpointRequestInit_1): Promise<number> { return client_1.call("SimpleTypeEndpoint", "getByte", {}, init); }
async function getByteWrapper_1(init?: EndpointRequestInit_1): Promise<number | undefined> { return client_1.call("SimpleTypeEndpoint", "getByteWrapper", {}, init); }
async function getChar_1(init?: EndpointRequestInit_1): Promise<string> { return client_1.call("SimpleTypeEndpoint", "getChar", {}, init); }
async function getCharWrapper_1(init?: EndpointRequestInit_1): Promise<string | undefined> { return client_1.call("SimpleTypeEndpoint", "getCharWrapper", {}, init); }
async function getDouble_1(init?: EndpointRequestInit_1): Promise<number> { return client_1.call("SimpleTypeEndpoint", "getDouble", {}, init); }
async function getDoubleWrapper_1(init?: EndpointRequestInit_1): Promise<number | undefined> { return client_1.call("SimpleTypeEndpoint", "getDoubleWrapper", {}, init); }
async function getFloat_1(init?: EndpointRequestInit_1): Promise<number> { return client_1.call("SimpleTypeEndpoint", "getFloat", {}, init); }
async function getFloatWrapper_1(init?: EndpointRequestInit_1): Promise<number | undefined> { return client_1.call("SimpleTypeEndpoint", "getFloatWrapper", {}, init); }
async function getInteger_1(init?: EndpointRequestInit_1): Promise<number> { return client_1.call("SimpleTypeEndpoint", "getInteger", {}, init); }
async function getIntegerWrapper_1(init?: EndpointRequestInit_1): Promise<number | undefined> { return client_1.call("SimpleTypeEndpoint", "getIntegerWrapper", {}, init); }
async function getLong_1(init?: EndpointRequestInit_1): Promise<number> { return client_1.call("SimpleTypeEndpoint", "getLong", {}, init); }
async function getLongWrapper_1(init?: EndpointRequestInit_1): Promise<number | undefined> { return client_1.call("SimpleTypeEndpoint", "getLongWrapper", {}, init); }
async function getShort_1(init?: EndpointRequestInit_1): Promise<number> { return client_1.call("SimpleTypeEndpoint", "getShort", {}, init); }
async function getShortWrapper_1(init?: EndpointRequestInit_1): Promise<number | undefined> { return client_1.call("SimpleTypeEndpoint", "getShortWrapper", {}, init); }
async function getString_1(init?: EndpointRequestInit_1): Promise<string | undefined> { return client_1.call("SimpleTypeEndpoint", "getString", {}, init); }
async function doSomething_1(init?: EndpointRequestInit_1): Promise<void> { return client_1.call("SimpleTypeEndpoint", "doSomething", {}, init); }
export { doSomething_1 as doSomething, getArray_1 as getArray, getBigDecimal_1 as getBigDecimal, getBigInteger_1 as getBigInteger, getBoolean_1 as getBoolean, getBooleanWrapper_1 as getBooleanWrapper, getByte_1 as getByte, getByteWrapper_1 as getByteWrapper, getChar_1 as getChar, getCharWrapper_1 as getCharWrapper, getDouble_1 as getDouble, getDoubleWrapper_1 as getDoubleWrapper, getFloat_1 as getFloat, getFloatWrapper_1 as getFloatWrapper, getInteger_1 as getInteger, getIntegerWrapper_1 as getIntegerWrapper, getLong_1 as getLong, getLongWrapper_1 as getLongWrapper, getShort_1 as getShort, getShortWrapper_1 as getShortWrapper, getString_1 as getString };

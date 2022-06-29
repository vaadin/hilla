import { EndpointRequestInit as EndpointRequestInit_1 } from "@hilla/frontend";
import client_1 from "./connect-client.default";
function getArray_1(init?: EndpointRequestInit_1): Promise<Array<number> | undefined> { return client_1.call("SimpleTypeEndpoint", "getArray", {}, init); }
function getBoolean_1(init?: EndpointRequestInit_1): Promise<boolean> { return client_1.call("SimpleTypeEndpoint", "getBoolean", {}, init); }
function getBooleanWrapper_1(init?: EndpointRequestInit_1): Promise<boolean | undefined> { return client_1.call("SimpleTypeEndpoint", "getBooleanWrapper", {}, init); }
function getByte_1(init?: EndpointRequestInit_1): Promise<number> { return client_1.call("SimpleTypeEndpoint", "getByte", {}, init); }
function getByteWrapper_1(init?: EndpointRequestInit_1): Promise<number | undefined> { return client_1.call("SimpleTypeEndpoint", "getByteWrapper", {}, init); }
function getChar_1(init?: EndpointRequestInit_1): Promise<string> { return client_1.call("SimpleTypeEndpoint", "getChar", {}, init); }
function getCharWrapper_1(init?: EndpointRequestInit_1): Promise<string | undefined> { return client_1.call("SimpleTypeEndpoint", "getCharWrapper", {}, init); }
function getDouble_1(init?: EndpointRequestInit_1): Promise<number> { return client_1.call("SimpleTypeEndpoint", "getDouble", {}, init); }
function getDoubleWrapper_1(init?: EndpointRequestInit_1): Promise<number | undefined> { return client_1.call("SimpleTypeEndpoint", "getDoubleWrapper", {}, init); }
function getFloat_1(init?: EndpointRequestInit_1): Promise<number> { return client_1.call("SimpleTypeEndpoint", "getFloat", {}, init); }
function getFloatWrapper_1(init?: EndpointRequestInit_1): Promise<number | undefined> { return client_1.call("SimpleTypeEndpoint", "getFloatWrapper", {}, init); }
function getInteger_1(init?: EndpointRequestInit_1): Promise<number> { return client_1.call("SimpleTypeEndpoint", "getInteger", {}, init); }
function getIntegerWrapper_1(init?: EndpointRequestInit_1): Promise<number | undefined> { return client_1.call("SimpleTypeEndpoint", "getIntegerWrapper", {}, init); }
function getLong_1(init?: EndpointRequestInit_1): Promise<number> { return client_1.call("SimpleTypeEndpoint", "getLong", {}, init); }
function getLongWrapper_1(init?: EndpointRequestInit_1): Promise<number | undefined> { return client_1.call("SimpleTypeEndpoint", "getLongWrapper", {}, init); }
function getShort_1(init?: EndpointRequestInit_1): Promise<number> { return client_1.call("SimpleTypeEndpoint", "getShort", {}, init); }
function getShortWrapper_1(init?: EndpointRequestInit_1): Promise<number | undefined> { return client_1.call("SimpleTypeEndpoint", "getShortWrapper", {}, init); }
function getString_1(init?: EndpointRequestInit_1): Promise<string | undefined> { return client_1.call("SimpleTypeEndpoint", "getString", {}, init); }
export { getArray_1 as getArray, getBoolean_1 as getBoolean, getBooleanWrapper_1 as getBooleanWrapper, getByte_1 as getByte, getByteWrapper_1 as getByteWrapper, getChar_1 as getChar, getCharWrapper_1 as getCharWrapper, getDouble_1 as getDouble, getDoubleWrapper_1 as getDoubleWrapper, getFloat_1 as getFloat, getFloatWrapper_1 as getFloatWrapper, getInteger_1 as getInteger, getIntegerWrapper_1 as getIntegerWrapper, getLong_1 as getLong, getLongWrapper_1 as getLongWrapper, getShort_1 as getShort, getShortWrapper_1 as getShortWrapper, getString_1 as getString };

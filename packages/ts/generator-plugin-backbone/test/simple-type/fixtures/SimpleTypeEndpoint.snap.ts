import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import client from "./connect-client.default.js";
async function getArray(init?: EndpointRequestInit): Promise<Array<number> | undefined> { return client.call("SimpleTypeEndpoint", "getArray", {}, init); }
async function getBigDecimal(init?: EndpointRequestInit): Promise<number | undefined> { return client.call("SimpleTypeEndpoint", "getBigDecimal", {}, init); }
async function getBigInteger(init?: EndpointRequestInit): Promise<number | undefined> { return client.call("SimpleTypeEndpoint", "getBigInteger", {}, init); }
async function getBoolean(init?: EndpointRequestInit): Promise<boolean> { return client.call("SimpleTypeEndpoint", "getBoolean", {}, init); }
async function getBooleanWrapper(init?: EndpointRequestInit): Promise<boolean | undefined> { return client.call("SimpleTypeEndpoint", "getBooleanWrapper", {}, init); }
async function getByte(init?: EndpointRequestInit): Promise<number> { return client.call("SimpleTypeEndpoint", "getByte", {}, init); }
async function getByteWrapper(init?: EndpointRequestInit): Promise<number | undefined> { return client.call("SimpleTypeEndpoint", "getByteWrapper", {}, init); }
async function getChar(init?: EndpointRequestInit): Promise<string> { return client.call("SimpleTypeEndpoint", "getChar", {}, init); }
async function getCharWrapper(init?: EndpointRequestInit): Promise<string | undefined> { return client.call("SimpleTypeEndpoint", "getCharWrapper", {}, init); }
async function getDouble(init?: EndpointRequestInit): Promise<number> { return client.call("SimpleTypeEndpoint", "getDouble", {}, init); }
async function getDoubleWrapper(init?: EndpointRequestInit): Promise<number | undefined> { return client.call("SimpleTypeEndpoint", "getDoubleWrapper", {}, init); }
async function getFloat(init?: EndpointRequestInit): Promise<number> { return client.call("SimpleTypeEndpoint", "getFloat", {}, init); }
async function getFloatWrapper(init?: EndpointRequestInit): Promise<number | undefined> { return client.call("SimpleTypeEndpoint", "getFloatWrapper", {}, init); }
async function getInteger(init?: EndpointRequestInit): Promise<number> { return client.call("SimpleTypeEndpoint", "getInteger", {}, init); }
async function getIntegerWrapper(init?: EndpointRequestInit): Promise<number | undefined> { return client.call("SimpleTypeEndpoint", "getIntegerWrapper", {}, init); }
async function getLong(init?: EndpointRequestInit): Promise<number> { return client.call("SimpleTypeEndpoint", "getLong", {}, init); }
async function getLongWrapper(init?: EndpointRequestInit): Promise<number | undefined> { return client.call("SimpleTypeEndpoint", "getLongWrapper", {}, init); }
async function getShort(init?: EndpointRequestInit): Promise<number> { return client.call("SimpleTypeEndpoint", "getShort", {}, init); }
async function getShortWrapper(init?: EndpointRequestInit): Promise<number | undefined> { return client.call("SimpleTypeEndpoint", "getShortWrapper", {}, init); }
async function getString(init?: EndpointRequestInit): Promise<string | undefined> { return client.call("SimpleTypeEndpoint", "getString", {}, init); }
async function doSomething(init?: EndpointRequestInit): Promise<void> { return client.call("SimpleTypeEndpoint", "doSomething", {}, init); }
export { doSomething, getArray, getBigDecimal, getBigInteger, getBoolean, getBooleanWrapper, getByte, getByteWrapper, getChar, getCharWrapper, getDouble, getDoubleWrapper, getFloat, getFloatWrapper, getInteger, getIntegerWrapper, getLong, getLongWrapper, getShort, getShortWrapper, getString };

import client_1 from "./connect-client.default.js";
async function getBoolean_1(): Promise<boolean> { return client_1.call("SimpleTypeEndpoint", "getBoolean"); }
async function getBooleanWrapper_1(): Promise<boolean | undefined> { return client_1.call("SimpleTypeEndpoint", "getBooleanWrapper"); }
async function getByte_1(): Promise<number> { return client_1.call("SimpleTypeEndpoint", "getByte"); }
async function getByteWrapper_1(): Promise<number | undefined> { return client_1.call("SimpleTypeEndpoint", "getByteWrapper"); }
async function getShort_1(): Promise<number> { return client_1.call("SimpleTypeEndpoint", "getShort"); }
async function getShortWrapper_1(): Promise<number | undefined> { return client_1.call("SimpleTypeEndpoint", "getShortWrapper"); }
async function getInteger_1(): Promise<number> { return client_1.call("SimpleTypeEndpoint", "getInteger"); }
async function getIntegerWrapper_1(): Promise<number | undefined> { return client_1.call("SimpleTypeEndpoint", "getIntegerWrapper"); }
async function getLong_1(): Promise<number> { return client_1.call("SimpleTypeEndpoint", "getLong"); }
async function getLongWrapper_1(): Promise<number | undefined> { return client_1.call("SimpleTypeEndpoint", "getLongWrapper"); }
async function getFloat_1(): Promise<number> { return client_1.call("SimpleTypeEndpoint", "getFloat"); }
async function getFloatWrapper_1(): Promise<number | undefined> { return client_1.call("SimpleTypeEndpoint", "getFloatWrapper"); }
async function getDouble_1(): Promise<number> { return client_1.call("SimpleTypeEndpoint", "getDouble"); }
async function getDoubleWrapper_1(): Promise<number | undefined> { return client_1.call("SimpleTypeEndpoint", "getDoubleWrapper"); }
async function getChar_1(): Promise<string> { return client_1.call("SimpleTypeEndpoint", "getChar"); }
async function getCharWrapper_1(): Promise<string | undefined> { return client_1.call("SimpleTypeEndpoint", "getCharWrapper"); }
async function getString_1(): Promise<string | undefined> { return client_1.call("SimpleTypeEndpoint", "getString"); }
async function getArray_1(): Promise<Array<number> | undefined> { return client_1.call("SimpleTypeEndpoint", "getArray"); }
export { getArray_1 as getArray, getBoolean_1 as getBoolean, getBooleanWrapper_1 as getBooleanWrapper, getByte_1 as getByte, getByteWrapper_1 as getByteWrapper, getChar_1 as getChar, getCharWrapper_1 as getCharWrapper, getDouble_1 as getDouble, getDoubleWrapper_1 as getDoubleWrapper, getFloat_1 as getFloat, getFloatWrapper_1 as getFloatWrapper, getInteger_1 as getInteger, getIntegerWrapper_1 as getIntegerWrapper, getLong_1 as getLong, getLongWrapper_1 as getLongWrapper, getShort_1 as getShort, getShortWrapper_1 as getShortWrapper, getString_1 as getString };

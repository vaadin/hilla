import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import client_1 from "./connect-client.default.js";
async function getOneParam_1(init: string | undefined, _init?: EndpointRequestInit_1): Promise<string | undefined> { return client_1.call("NameClashEndpoint", "getOneParam", { init }, _init); }
async function getThreeParams_1(__init: string | undefined, _init: string | undefined, init: string | undefined, ___init?: EndpointRequestInit_1): Promise<string | undefined> { return client_1.call("NameClashEndpoint", "getThreeParams", { __init, _init, init }, ___init); }
async function getTwoParams_1(init: string | undefined, _init: string | undefined, __init?: EndpointRequestInit_1): Promise<string | undefined> { return client_1.call("NameClashEndpoint", "getTwoParams", { init, _init }, __init); }
export { getOneParam_1 as getOneParam, getThreeParams_1 as getThreeParams, getTwoParams_1 as getTwoParams };

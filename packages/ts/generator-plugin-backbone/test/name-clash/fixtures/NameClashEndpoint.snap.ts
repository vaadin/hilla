import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import client_1 from "./connect-client.default.js";
async function getOneParam_1(init: string, _init?: EndpointRequestInit_1): Promise<string> { return client_1.call("NameClashEndpoint", "getOneParam", { init }, _init); }
async function getTwoParams_1(init: string, _init: string, __init?: EndpointRequestInit_1): Promise<string> { return client_1.call("NameClashEndpoint", "getTwoParams", { init, _init }, __init); }
async function getThreeParams_1(__init: string, _init: string, init: string, ___init?: EndpointRequestInit_1): Promise<string> { return client_1.call("NameClashEndpoint", "getThreeParams", { __init, _init, init }, ___init); }
export { getOneParam_1 as getOneParam, getThreeParams_1 as getThreeParams, getTwoParams_1 as getTwoParams };

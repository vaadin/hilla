import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import client from "./connect-client.default.js";
async function getOneParam(init: string, _init?: EndpointRequestInit): Promise<string> { return client.call("NameClashEndpoint", "getOneParam", { init }, _init); }
async function getTwoParams(init: string, _init: string, __init?: EndpointRequestInit): Promise<string> { return client.call("NameClashEndpoint", "getTwoParams", { init, _init }, __init); }
async function getThreeParams(__init: string, _init: string, init: string, ___init?: EndpointRequestInit): Promise<string> { return client.call("NameClashEndpoint", "getThreeParams", { __init, _init, init }, ___init); }
export { getOneParam, getThreeParams, getTwoParams };

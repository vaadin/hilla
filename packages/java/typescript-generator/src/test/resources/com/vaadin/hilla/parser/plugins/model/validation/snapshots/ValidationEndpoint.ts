import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import type ValidationData from "./com/vaadin/hilla/parser/plugins/model/validation/ValidationEndpoint/ValidationData.js";
import client from "./connect-client.default.js";
async function getValidationData(init?: EndpointRequestInit): Promise<ValidationData | undefined> { return client.call("ValidationEndpoint", "getValidationData", {}, init); }
export { getValidationData };

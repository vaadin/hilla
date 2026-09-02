import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type ValidationData_1 from "./com/vaadin/hilla/parser/plugins/model/validation/ValidationEndpoint/ValidationData.js";
import client_1 from "./connect-client.default.js";
async function getValidationData_1(init?: EndpointRequestInit_1): Promise<ValidationData_1 | undefined> { return client_1.call("ValidationEndpoint", "getValidationData", {}, init); }
export { getValidationData_1 as getValidationData };

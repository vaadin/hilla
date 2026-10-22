import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import type ComplexTypeModel from "./com/vaadin/hilla/parser/plugins/backbone/complextype/ComplexTypeEndpoint/ComplexTypeModel.js";
import client from "./connect-client.default.js";
async function getComplexTypeModel(data: Array<Record<string, string | undefined> | undefined> | undefined, init?: EndpointRequestInit): Promise<ComplexTypeModel | undefined> { return client.call("ComplexTypeEndpoint", "getComplexTypeModel", { data }, init); }
export { getComplexTypeModel };

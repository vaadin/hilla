import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import type ComplexHierarchyModel from "./com/vaadin/hilla/parser/plugins/backbone/complexhierarchy/models/ComplexHierarchyModel.js";
import client from "./connect-client.default.js";
async function getModel(init?: EndpointRequestInit): Promise<ComplexHierarchyModel | undefined> { return client.call("ComplexHierarchyEndpoint", "getModel", {}, init); }
export { getModel };

import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import type HierarchyRef from "./com/vaadin/hilla/parser/plugins/backbone/hierarchyref/HierarchyRefEndpoint/HierarchyRef.js";
import client from "./connect-client.default.js";
async function getHierarchyRef(data: Array<Record<string, string | undefined> | undefined> | undefined, init?: EndpointRequestInit): Promise<HierarchyRef | undefined> { return client.call("HierarchyRefEndpoint", "getHierarchyRef", { data }, init); }
export { getHierarchyRef };

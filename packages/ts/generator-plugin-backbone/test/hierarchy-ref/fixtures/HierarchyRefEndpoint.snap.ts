import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-core";
import type HierarchyRef_1 from "./com/vaadin/hilla/parser/plugins/backbone/hierarchyref/HierarchyRefEndpoint/HierarchyRef.js";
import client_1 from "./connect-client.default.js";
async function getHierarchyRef_1(data: Array<Record<string, string | undefined> | undefined> | undefined, init?: EndpointRequestInit_1): Promise<HierarchyRef_1 | undefined> { return client_1.call("HierarchyRefEndpoint", "getHierarchyRef", { data }, init); }
export { getHierarchyRef_1 as getHierarchyRef };

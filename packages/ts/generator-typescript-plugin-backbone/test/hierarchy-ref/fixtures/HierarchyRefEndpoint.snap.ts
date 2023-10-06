import { EndpointRequestInit as EndpointRequestInit_1 } from "@hilla/frontend";
import client_1 from "./connect-client.default.js";
import type HierarchyRef_1 from "./dev/hilla/parser/plugins/backbone/hierarchyref/HierarchyRefEndpoint/HierarchyRef.js";
async function getHierarchyRef_1(data: Array<Record<string, string | undefined> | undefined> | undefined, init?: EndpointRequestInit_1): Promise<HierarchyRef_1 | undefined> { return client_1.call("HierarchyRefEndpoint", "getHierarchyRef", { data }, init); }
export { getHierarchyRef_1 as getHierarchyRef };

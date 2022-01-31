import client_1 from "./connect-client.default";
import type ComplexHierarchyModel_1 from "./dev/hilla/parser/plugins/backbone/complexhierarchy/models/ComplexHierarchyModel";
async function getModel_1(): Promise<ComplexHierarchyModel_1 | undefined> { return client_1.call("ComplexHierarchyEndpoint", "getModel"); }
export { getModel_1 as getModel };

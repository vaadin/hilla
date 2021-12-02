import type ComplexHierarchyModel_1 from "./com/vaadin/fusion/parser/plugins/backbone/complexhierarchy/models/ComplexHierarchyModel";
import client_1 from "./connect-client.default";
async function getModel_1(): Promise<ComplexHierarchyModel_1 | undefined> { return client_1.call("ComplexHierarchyEndpoint", "getModel"); }
export { getModel_1 as getModel };

import client_1 from "./connect-client.default";
import type ComplexTypeModel_1 from "./dev/hilla/parser/plugins/backbone/complextype/ComplexTypeEndpoint/ComplexTypeModel";
async function getComplexTypeModel_1(data: Array<Record<string, string | undefined> | undefined> | undefined): Promise<ComplexTypeModel_1 | undefined> { return client_1.call("ComplexTypeEndpoint", "getComplexTypeModel", { data }); }
export { getComplexTypeModel_1 as getComplexTypeModel };

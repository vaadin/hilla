import type ComplexTypeModel_1 from "./com/vaadin/fusion/parser/plugins/backbone/complextype/ComplexTypeEndpoint/ComplexTypeModel.js";
import client_1 from "./connect-client.default.js";
async function getComplexTypeModel_1(data: Array<Record<string, string | undefined> | undefined> | undefined): Promise<ComplexTypeModel_1 | undefined> { return client_1.call("ComplexTypeEndpoint", "getComplexTypeModel", { data }); }
export { getComplexTypeModel_1 as getComplexTypeModel };

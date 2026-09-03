import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type NullableFieldModel_1 from "./com/vaadin/hilla/parser/plugins/nonnull/nullable/NullableEndpoint/NullableFieldModel.js";
import client_1 from "./connect-client.default.js";
async function nullableFieldModel_1(nullableFieldModel: NullableFieldModel_1 | undefined, init?: EndpointRequestInit_1): Promise<NullableFieldModel_1 | undefined> { return client_1.call("NullableEndpoint", "nullableFieldModel", { nullableFieldModel }, init); }
export { nullableFieldModel_1 as nullableFieldModel };

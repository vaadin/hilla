import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type NullableNonNullFieldModel_1 from "./com/vaadin/hilla/parser/plugins/nonnull/nullable/nonNullApi/NullableNonNullEndpoint/NullableNonNullFieldModel.js";
import client_1 from "./connect-client.default.js";
async function nullableNonNullFieldModel_1(nullableNonNullFieldModel: NullableNonNullFieldModel_1, init?: EndpointRequestInit_1): Promise<NullableNonNullFieldModel_1> { return client_1.call("NullableNonNullEndpoint", "nullableNonNullFieldModel", { nullableNonNullFieldModel }, init); }
export { nullableNonNullFieldModel_1 as nullableNonNullFieldModel };

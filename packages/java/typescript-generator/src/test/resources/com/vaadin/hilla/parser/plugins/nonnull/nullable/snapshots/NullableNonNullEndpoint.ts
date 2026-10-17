import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import type NullableNonNullFieldModel from "./com/vaadin/hilla/parser/plugins/nonnull/nullable/nonNullApi/NullableNonNullEndpoint/NullableNonNullFieldModel.js";
import client from "./connect-client.default.js";
async function nullableNonNullFieldModel(nullableNonNullFieldModel: NullableNonNullFieldModel, init?: EndpointRequestInit): Promise<NullableNonNullFieldModel> { return client.call("NullableNonNullEndpoint", "nullableNonNullFieldModel", { nullableNonNullFieldModel }, init); }
export { nullableNonNullFieldModel };

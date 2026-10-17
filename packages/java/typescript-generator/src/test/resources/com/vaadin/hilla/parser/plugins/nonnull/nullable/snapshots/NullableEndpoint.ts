import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import type NullableFieldModel from "./com/vaadin/hilla/parser/plugins/nonnull/nullable/NullableEndpoint/NullableFieldModel.js";
import client from "./connect-client.default.js";
async function nullableFieldModel(nullableFieldModel: NullableFieldModel | undefined, init?: EndpointRequestInit): Promise<NullableFieldModel | undefined> { return client.call("NullableEndpoint", "nullableFieldModel", { nullableFieldModel }, init); }
export { nullableFieldModel };

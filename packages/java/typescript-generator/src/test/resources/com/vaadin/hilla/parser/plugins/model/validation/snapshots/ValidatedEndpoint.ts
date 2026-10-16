import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type Validated_1 from "./com/vaadin/hilla/parser/plugins/model/validation/ValidatedEndpoint/Validated.js";
import client_1 from "./connect-client.default.js";
async function save_1(entity: Validated_1 | undefined, init?: EndpointRequestInit_1): Promise<void> { return client_1.call("ValidatedEndpoint", "save", { entity }, init); }
export { save_1 as save };

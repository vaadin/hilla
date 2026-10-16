import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type Nested_1 from "./com/vaadin/hilla/parser/plugins/model/nested/NestedEndpoint/Nested.js";
import client_1 from "./connect-client.default.js";
async function getNested_1(init?: EndpointRequestInit_1): Promise<Nested_1 | undefined> { return client_1.call("NestedEndpoint", "getNested", {}, init); }
async function transpose_1(matrix: Array<Array<number> | undefined> | undefined, init?: EndpointRequestInit_1): Promise<Array<Array<number> | undefined> | undefined> { return client_1.call("NestedEndpoint", "transpose", { matrix }, init); }
export { getNested_1 as getNested, transpose_1 as transpose };

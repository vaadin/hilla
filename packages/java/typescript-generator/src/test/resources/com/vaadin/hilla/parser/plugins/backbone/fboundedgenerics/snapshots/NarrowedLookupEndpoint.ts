import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type EnumLookupValue_1 from "./com/vaadin/hilla/parser/plugins/backbone/fboundedgenerics/EnumLookupValue.js";
import type NarrowedLookup_1 from "./com/vaadin/hilla/parser/plugins/backbone/fboundedgenerics/NarrowedLookup.js";
import type NodeType_1 from "./com/vaadin/hilla/parser/plugins/backbone/fboundedgenerics/NodeType.js";
import client_1 from "./connect-client.default.js";
async function getNarrowedLookup_1(init?: EndpointRequestInit_1): Promise<NarrowedLookup_1<EnumLookupValue_1<NodeType_1 | undefined> | undefined> | undefined> { return client_1.call("NarrowedLookupEndpoint", "getNarrowedLookup", {}, init); }
export { getNarrowedLookup_1 as getNarrowedLookup };

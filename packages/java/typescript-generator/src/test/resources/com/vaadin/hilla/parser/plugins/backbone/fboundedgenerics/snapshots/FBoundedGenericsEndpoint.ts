import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type EnumLookupValue_1 from "./com/vaadin/hilla/parser/plugins/backbone/fboundedgenerics/EnumLookupValue.js";
import type MutuallyBoundedPair_1 from "./com/vaadin/hilla/parser/plugins/backbone/fboundedgenerics/MutuallyBoundedPair.js";
import type NodeType_1 from "./com/vaadin/hilla/parser/plugins/backbone/fboundedgenerics/NodeType.js";
import client_1 from "./connect-client.default.js";
async function getNodeType_1(init?: EndpointRequestInit_1): Promise<EnumLookupValue_1<NodeType_1 | undefined> | undefined> { return client_1.call("FBoundedGenericsEndpoint", "getNodeType", {}, init); }
async function getPair_1(init?: EndpointRequestInit_1): Promise<MutuallyBoundedPair_1<string | undefined, string | undefined> | undefined> { return client_1.call("FBoundedGenericsEndpoint", "getPair", {}, init); }
export { getNodeType_1 as getNodeType, getPair_1 as getPair };

import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type AnnotationTestEntity_1 from "./com/vaadin/hilla/parser/plugins/model/annotations/AnnotationsEndpoint/AnnotationTestEntity.js";
import client_1 from "./connect-client.default.js";
async function getTestEntity_1(init?: EndpointRequestInit_1): Promise<AnnotationTestEntity_1 | undefined> { return client_1.call("AnnotationsEndpoint", "getTestEntity", {}, init); }
export { getTestEntity_1 as getTestEntity };

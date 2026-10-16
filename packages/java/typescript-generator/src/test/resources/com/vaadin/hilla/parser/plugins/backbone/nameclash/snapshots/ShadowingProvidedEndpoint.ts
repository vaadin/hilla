import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type File_1 from "./com/vaadin/hilla/parser/plugins/backbone/nameclash/ShadowingProvidedEndpoint/File.js";
import client_1 from "./connect-client.default.js";
async function describe_1(file: File | undefined, init?: EndpointRequestInit_1): Promise<File_1 | undefined> { return client_1.call("ShadowingProvidedEndpoint", "describe", { file }, init); }
export { describe_1 as describe };

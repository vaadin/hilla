import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type File_1 from "./com/vaadin/hilla/runtime/transfertypes/File.js";
import client_1 from "./connect-client.default.js";
async function uploadFile_1(file: File_1 | undefined, init?: EndpointRequestInit_1): Promise<void> { return client_1.call("MultipartFileEndpoint", "uploadFile", { file }, init); }
export { uploadFile_1 as uploadFile };

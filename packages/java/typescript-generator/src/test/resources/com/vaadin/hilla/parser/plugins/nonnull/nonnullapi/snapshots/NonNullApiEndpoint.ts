import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type Dependency_1 from "./com/vaadin/hilla/parser/plugins/nonnull/nonnullapi/NonNullApiEndpoint/Dependency.js";
import type Generic_1 from "./com/vaadin/hilla/parser/plugins/nonnull/nonnullapi/NonNullApiEndpoint/Generic.js";
import type SubPackageDependency_1 from "./com/vaadin/hilla/parser/plugins/nonnull/nonnullapi/subpackage/SubPackageDependency.js";
import client_1 from "./connect-client.default.js";
async function defaultMethod_1(param: string, init?: EndpointRequestInit_1): Promise<Dependency_1> { return client_1.call("NonNullApiEndpoint", "defaultMethod", { param }, init); }
async function genericMethod_1(generic: Generic_1<string>, init?: EndpointRequestInit_1): Promise<Generic_1<string>> { return client_1.call("NonNullApiEndpoint", "genericMethod", { generic }, init); }
async function genericNullableMethod_1(generic: Generic_1<string | undefined>, init?: EndpointRequestInit_1): Promise<Generic_1<string | undefined>> { return client_1.call("NonNullApiEndpoint", "genericNullableMethod", { generic }, init); }
async function nestedSignatureMethod_1(param: Array<Dependency_1>, init?: EndpointRequestInit_1): Promise<Record<string, Array<Dependency_1>>> { return client_1.call("NonNullApiEndpoint", "nestedSignatureMethod", { param }, init); }
async function nullableMethod_1(param: string | undefined, init?: EndpointRequestInit_1): Promise<Dependency_1 | undefined> { return client_1.call("NonNullApiEndpoint", "nullableMethod", { param }, init); }
async function nullableNestedSignatureMethod_1(param: Array<Dependency_1 | undefined>, init?: EndpointRequestInit_1): Promise<Record<string, Array<Dependency_1 | undefined> | undefined>> { return client_1.call("NonNullApiEndpoint", "nullableNestedSignatureMethod", { param }, init); }
async function nullableSignature_1(param: string | undefined, init?: EndpointRequestInit_1): Promise<Dependency_1 | undefined> { return client_1.call("NonNullApiEndpoint", "nullableSignature", { param }, init); }
async function optionalMethod_1(opt: string | undefined, init?: EndpointRequestInit_1): Promise<string | undefined> { return client_1.call("NonNullApiEndpoint", "optionalMethod", { opt }, init); }
async function subPackageMethod_1(entity: SubPackageDependency_1, init?: EndpointRequestInit_1): Promise<SubPackageDependency_1> { return client_1.call("NonNullApiEndpoint", "subPackageMethod", { entity }, init); }
export { defaultMethod_1 as defaultMethod, genericMethod_1 as genericMethod, genericNullableMethod_1 as genericNullableMethod, nestedSignatureMethod_1 as nestedSignatureMethod, nullableMethod_1 as nullableMethod, nullableNestedSignatureMethod_1 as nullableNestedSignatureMethod, nullableSignature_1 as nullableSignature, optionalMethod_1 as optionalMethod, subPackageMethod_1 as subPackageMethod };

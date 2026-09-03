import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type ExtendedNonNullableModel_1 from "./com/vaadin/hilla/parser/plugins/nonnull/basic/BasicEndpoint/ExtendedNonNullableModel.js";
import type NonNullableFieldModel_1 from "./com/vaadin/hilla/parser/plugins/nonnull/basic/BasicEndpoint/NonNullableFieldModel.js";
import type NonNullableModel_1 from "./com/vaadin/hilla/parser/plugins/nonnull/basic/BasicEndpoint/NonNullableModel.js";
import client_1 from "./connect-client.default.js";
async function complexType_1(map: Record<string, Array<ExtendedNonNullableModel_1>>, init?: EndpointRequestInit_1): Promise<Record<string, Array<ExtendedNonNullableModel_1>>> { return client_1.call("BasicEndpoint", "complexType", { map }, init); }
async function mixedAnnotations_1(parameter: string, init?: EndpointRequestInit_1): Promise<Array<string>> { return client_1.call("BasicEndpoint", "mixedAnnotations", { parameter }, init); }
async function nonNullableFieldModel_1(nonNullableFieldModel: NonNullableFieldModel_1, init?: EndpointRequestInit_1): Promise<NonNullableFieldModel_1> { return client_1.call("BasicEndpoint", "nonNullableFieldModel", { nonNullableFieldModel }, init); }
async function nonTypeAnnotation_1(nonTypeParameter: string, init?: EndpointRequestInit_1): Promise<string> { return client_1.call("BasicEndpoint", "nonTypeAnnotation", { nonTypeParameter }, init); }
async function nullableType_1(nullableParameter: string | undefined, init?: EndpointRequestInit_1): Promise<string | undefined> { return client_1.call("BasicEndpoint", "nullableType", { nullableParameter }, init); }
async function optional_1(opt: string | undefined, init?: EndpointRequestInit_1): Promise<string | undefined> { return client_1.call("BasicEndpoint", "optional", { opt }, init); }
async function simpleType_1(str: string, init?: EndpointRequestInit_1): Promise<string> { return client_1.call("BasicEndpoint", "simpleType", { str }, init); }
async function typeArgumentWildcard_1(list: Array<string> | undefined, init?: EndpointRequestInit_1): Promise<Array<string> | undefined> { return client_1.call("BasicEndpoint", "typeArgumentWildcard", { list }, init); }
async function typeParameter_1(list: Array<string> | undefined, init?: EndpointRequestInit_1): Promise<Array<string> | undefined> { return client_1.call("BasicEndpoint", "typeParameter", { list }, init); }
async function typeWithTypeArgument_1(list: Array<NonNullableModel_1> | undefined, init?: EndpointRequestInit_1): Promise<Array<NonNullableModel_1> | undefined> { return client_1.call("BasicEndpoint", "typeWithTypeArgument", { list }, init); }
export { complexType_1 as complexType, mixedAnnotations_1 as mixedAnnotations, nonNullableFieldModel_1 as nonNullableFieldModel, nonTypeAnnotation_1 as nonTypeAnnotation, nullableType_1 as nullableType, optional_1 as optional, simpleType_1 as simpleType, typeArgumentWildcard_1 as typeArgumentWildcard, typeParameter_1 as typeParameter, typeWithTypeArgument_1 as typeWithTypeArgument };

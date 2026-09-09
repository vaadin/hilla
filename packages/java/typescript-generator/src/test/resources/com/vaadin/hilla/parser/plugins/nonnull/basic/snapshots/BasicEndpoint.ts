import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import type ExtendedNonNullableModel from "./com/vaadin/hilla/parser/plugins/nonnull/basic/BasicEndpoint/ExtendedNonNullableModel.js";
import type NonNullableFieldModel from "./com/vaadin/hilla/parser/plugins/nonnull/basic/BasicEndpoint/NonNullableFieldModel.js";
import type NonNullableModel from "./com/vaadin/hilla/parser/plugins/nonnull/basic/BasicEndpoint/NonNullableModel.js";
import client from "./connect-client.default.js";
async function complexType(map: Record<string, Array<ExtendedNonNullableModel>>, init?: EndpointRequestInit): Promise<Record<string, Array<ExtendedNonNullableModel>>> { return client.call("BasicEndpoint", "complexType", { map }, init); }
async function mixedAnnotations(parameter: string, init?: EndpointRequestInit): Promise<Array<string>> { return client.call("BasicEndpoint", "mixedAnnotations", { parameter }, init); }
async function nonNullableFieldModel(nonNullableFieldModel: NonNullableFieldModel, init?: EndpointRequestInit): Promise<NonNullableFieldModel> { return client.call("BasicEndpoint", "nonNullableFieldModel", { nonNullableFieldModel }, init); }
async function nonTypeAnnotation(nonTypeParameter: string, init?: EndpointRequestInit): Promise<string> { return client.call("BasicEndpoint", "nonTypeAnnotation", { nonTypeParameter }, init); }
async function nullableType(nullableParameter: string | undefined, init?: EndpointRequestInit): Promise<string | undefined> { return client.call("BasicEndpoint", "nullableType", { nullableParameter }, init); }
async function optional(opt: string | undefined, init?: EndpointRequestInit): Promise<string | undefined> { return client.call("BasicEndpoint", "optional", { opt }, init); }
async function simpleType(str: string, init?: EndpointRequestInit): Promise<string> { return client.call("BasicEndpoint", "simpleType", { str }, init); }
async function typeArgumentWildcard(list: Array<string> | undefined, init?: EndpointRequestInit): Promise<Array<string> | undefined> { return client.call("BasicEndpoint", "typeArgumentWildcard", { list }, init); }
async function typeParameter(list: Array<string> | undefined, init?: EndpointRequestInit): Promise<Array<string> | undefined> { return client.call("BasicEndpoint", "typeParameter", { list }, init); }
async function typeWithTypeArgument(list: Array<NonNullableModel> | undefined, init?: EndpointRequestInit): Promise<Array<NonNullableModel> | undefined> { return client.call("BasicEndpoint", "typeWithTypeArgument", { list }, init); }
export { complexType, mixedAnnotations, nonNullableFieldModel, nonTypeAnnotation, nullableType, optional, simpleType, typeArgumentWildcard, typeParameter, typeWithTypeArgument };

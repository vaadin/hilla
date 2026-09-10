import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import client from "./connect-client.default.js";
async function echoCustomDate(init?: EndpointRequestInit): Promise<string | undefined> { return client.call("DateTimeEndpoint", "echoCustomDate", {}, init); }
async function echoDate(date: string | undefined, init?: EndpointRequestInit): Promise<string | undefined> { return client.call("DateTimeEndpoint", "echoDate", { date }, init); }
async function echoInstant(instant: string | undefined, init?: EndpointRequestInit): Promise<string | undefined> { return client.call("DateTimeEndpoint", "echoInstant", { instant }, init); }
async function echoListLocalDateTime(localDateTimeList: Array<string | undefined> | undefined, init?: EndpointRequestInit): Promise<Array<string | undefined> | undefined> { return client.call("DateTimeEndpoint", "echoListLocalDateTime", { localDateTimeList }, init); }
async function echoLocalDate(localDate: string | undefined, init?: EndpointRequestInit): Promise<string | undefined> { return client.call("DateTimeEndpoint", "echoLocalDate", { localDate }, init); }
async function echoLocalDateTime(localDateTime: string | undefined, init?: EndpointRequestInit): Promise<string | undefined> { return client.call("DateTimeEndpoint", "echoLocalDateTime", { localDateTime }, init); }
async function echoLocalTime(localTime: string | undefined, init?: EndpointRequestInit): Promise<string | undefined> { return client.call("DateTimeEndpoint", "echoLocalTime", { localTime }, init); }
async function echoMapInstant(mapInstant: Record<string, string | undefined> | undefined, init?: EndpointRequestInit): Promise<Record<string, string | undefined> | undefined> { return client.call("DateTimeEndpoint", "echoMapInstant", { mapInstant }, init); }
async function echoOffsetDateTime(offsetDateTime: string | undefined, init?: EndpointRequestInit): Promise<string | undefined> { return client.call("DateTimeEndpoint", "echoOffsetDateTime", { offsetDateTime }, init); }
async function echoZonedDateTime(zonedDateTime: string | undefined, init?: EndpointRequestInit): Promise<string | undefined> { return client.call("DateTimeEndpoint", "echoZonedDateTime", { zonedDateTime }, init); }
export { echoCustomDate, echoDate, echoInstant, echoListLocalDateTime, echoLocalDate, echoLocalDateTime, echoLocalTime, echoMapInstant, echoOffsetDateTime, echoZonedDateTime };

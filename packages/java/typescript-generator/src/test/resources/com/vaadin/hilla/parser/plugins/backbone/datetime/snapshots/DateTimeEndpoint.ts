import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import client from './connect-client.default.js';

export async function echoCustomDate(init?: EndpointRequestInit): Promise<string | undefined> {
  return client.call('DateTimeEndpoint', 'echoCustomDate', {}, init);
}

export async function echoDate(date: string | undefined, init?: EndpointRequestInit): Promise<string | undefined> {
  return client.call('DateTimeEndpoint', 'echoDate', { date }, init);
}

export async function echoInstant(
  instant: string | undefined,
  init?: EndpointRequestInit,
): Promise<string | undefined> {
  return client.call('DateTimeEndpoint', 'echoInstant', { instant }, init);
}

export async function echoListLocalDateTime(
  localDateTimeList: Array<string | undefined> | undefined,
  init?: EndpointRequestInit,
): Promise<Array<string | undefined> | undefined> {
  return client.call('DateTimeEndpoint', 'echoListLocalDateTime', { localDateTimeList }, init);
}

export async function echoLocalDate(
  localDate: string | undefined,
  init?: EndpointRequestInit,
): Promise<string | undefined> {
  return client.call('DateTimeEndpoint', 'echoLocalDate', { localDate }, init);
}

export async function echoLocalDateTime(
  localDateTime: string | undefined,
  init?: EndpointRequestInit,
): Promise<string | undefined> {
  return client.call('DateTimeEndpoint', 'echoLocalDateTime', { localDateTime }, init);
}

export async function echoLocalTime(
  localTime: string | undefined,
  init?: EndpointRequestInit,
): Promise<string | undefined> {
  return client.call('DateTimeEndpoint', 'echoLocalTime', { localTime }, init);
}

export async function echoMapInstant(
  mapInstant: Record<string, string | undefined> | undefined,
  init?: EndpointRequestInit,
): Promise<Record<string, string | undefined> | undefined> {
  return client.call('DateTimeEndpoint', 'echoMapInstant', { mapInstant }, init);
}

export async function echoOffsetDateTime(
  offsetDateTime: string | undefined,
  init?: EndpointRequestInit,
): Promise<string | undefined> {
  return client.call('DateTimeEndpoint', 'echoOffsetDateTime', { offsetDateTime }, init);
}

export async function echoZonedDateTime(
  zonedDateTime: string | undefined,
  init?: EndpointRequestInit,
): Promise<string | undefined> {
  return client.call('DateTimeEndpoint', 'echoZonedDateTime', { zonedDateTime }, init);
}

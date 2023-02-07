/**
 * This module is generated from DateTimeEndpoint.java
 * All changes to this file are overridden. Consider editing the corresponding Java file if necessary.
 * @module DateTimeEndpoint
 */

// @ts-ignore
import client from './connect-client.default';
// @ts-ignore
import { EndpointRequestInit, Subscription } from '@hilla/frontend';

function _echoDate(
  date: string | undefined,
  __init?: EndpointRequestInit
): Promise<string | undefined> {
  return client.call('DateTimeEndpoint', 'echoDate', {date}, __init);
}

function _echoInstant(
  instant: string | undefined,
  __init?: EndpointRequestInit
): Promise<string | undefined> {
  return client.call('DateTimeEndpoint', 'echoInstant', {instant}, __init);
}

function _echoListLocalDateTime(
  localDateTimeList: Array<string | undefined> | undefined,
  __init?: EndpointRequestInit
): Promise<Array<string | undefined> | undefined> {
  return client.call('DateTimeEndpoint', 'echoListLocalDateTime', {localDateTimeList}, __init);
}

function _echoLocalDate(
  localDate: string | undefined,
  __init?: EndpointRequestInit
): Promise<string | undefined> {
  return client.call('DateTimeEndpoint', 'echoLocalDate', {localDate}, __init);
}

function _echoLocalDateTime(
  localDateTime: string | undefined,
  __init?: EndpointRequestInit
): Promise<string | undefined> {
  return client.call('DateTimeEndpoint', 'echoLocalDateTime', {localDateTime}, __init);
}

function _echoLocalTime(
  localTime: string | undefined,
  __init?: EndpointRequestInit
): Promise<string | undefined> {
  return client.call('DateTimeEndpoint', 'echoLocalTime', {localTime}, __init);
}

function _echoMapInstant(
  mapInstant: Record<string, string | undefined> | undefined,
  __init?: EndpointRequestInit
): Promise<Record<string, string | undefined> | undefined> {
  return client.call('DateTimeEndpoint', 'echoMapInstant', {mapInstant}, __init);
}

export {
  _echoDate as echoDate,
  _echoInstant as echoInstant,
  _echoListLocalDateTime as echoListLocalDateTime,
  _echoLocalDate as echoLocalDate,
  _echoLocalDateTime as echoLocalDateTime,
  _echoLocalTime as echoLocalTime,
  _echoMapInstant as echoMapInstant,
};

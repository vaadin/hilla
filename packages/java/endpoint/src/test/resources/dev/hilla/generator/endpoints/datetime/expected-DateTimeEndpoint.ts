/**
 * This module is generated from DateTimeEndpoint.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module DateTimeEndpoint
 */

// @ts-ignore
import client from './connect-client.default';
// @ts-ignore
import { EndpointRequestInit, Subscription } from '@hilla/frontend';

function _echoDate(
  date: string | undefined,
  init?: EndpointRequestInit
): Promise<string | undefined> {
  return client.call('DateTimeEndpoint', 'echoDate', {date}, init);
}

function _echoInstant(
  instant: string | undefined,
  init?: EndpointRequestInit
): Promise<string | undefined> {
  return client.call('DateTimeEndpoint', 'echoInstant', {instant}, init);
}

function _echoListLocalDateTime(
  localDateTimeList: Array<string | undefined> | undefined,
  init?: EndpointRequestInit
): Promise<Array<string | undefined> | undefined> {
  return client.call('DateTimeEndpoint', 'echoListLocalDateTime', {localDateTimeList}, init);
}

function _echoLocalDate(
  localDate: string | undefined,
  init?: EndpointRequestInit
): Promise<string | undefined> {
  return client.call('DateTimeEndpoint', 'echoLocalDate', {localDate}, init);
}

function _echoLocalDateTime(
  localDateTime: string | undefined,
  init?: EndpointRequestInit
): Promise<string | undefined> {
  return client.call('DateTimeEndpoint', 'echoLocalDateTime', {localDateTime}, init);
}

function _echoLocalTime(
  localTime: string | undefined,
  init?: EndpointRequestInit
): Promise<string | undefined> {
  return client.call('DateTimeEndpoint', 'echoLocalTime', {localTime}, init);
}

function _echoMapInstant(
  mapInstant: Record<string, string | undefined> | undefined,
  init?: EndpointRequestInit
): Promise<Record<string, string | undefined> | undefined> {
  return client.call('DateTimeEndpoint', 'echoMapInstant', {mapInstant}, init);
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

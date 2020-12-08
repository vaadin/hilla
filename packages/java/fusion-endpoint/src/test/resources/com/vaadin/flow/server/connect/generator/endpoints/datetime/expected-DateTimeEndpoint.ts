/**
 * This module is generated from DateTimeEndpoint.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module DateTimeEndpoint
 */

// @ts-ignore
import client from './connect-client.default';

function _echoDate(
  date: string
): Promise<string> {
  return client.call('DateTimeEndpoint', 'echoDate', {date});
}
export {_echoDate as echoDate};

function _echoInstant(
  instant: string
): Promise<string> {
  return client.call('DateTimeEndpoint', 'echoInstant', {instant});
}
export {_echoInstant as echoInstant};

function _echoListLocalDateTime(
  localDateTimeList: Array<string>
): Promise<Array<string>> {
  return client.call('DateTimeEndpoint', 'echoListLocalDateTime', {localDateTimeList});
}
export {_echoListLocalDateTime as echoListLocalDateTime};

function _echoLocalDate(
  localDate: string
): Promise<string> {
  return client.call('DateTimeEndpoint', 'echoLocalDate', {localDate});
}
export {_echoLocalDate as echoLocalDate};

function _echoLocalDateTime(
  localDateTime: string
): Promise<string> {
  return client.call('DateTimeEndpoint', 'echoLocalDateTime', {localDateTime});
}
export {_echoLocalDateTime as echoLocalDateTime};

function _echoLocalTime(
  localTime: string
): Promise<string> {
  return client.call('DateTimeEndpoint', 'echoLocalTime', {localTime});
}
export {_echoLocalTime as echoLocalTime};

function _echoMapInstant(
  mapInstant: { [key: string]: string; }
): Promise<{ [key: string]: string; }> {
  return client.call('DateTimeEndpoint', 'echoMapInstant', {mapInstant});
}
export {_echoMapInstant as echoMapInstant};
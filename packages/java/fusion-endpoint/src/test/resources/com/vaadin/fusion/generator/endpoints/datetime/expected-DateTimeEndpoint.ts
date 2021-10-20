/**
 * This module is generated from DateTimeEndpoint.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module DateTimeEndpoint
 */

// @ts-ignore
import client from './connect-client.default';

function _echoDate(
  date: string | undefined
): Promise<string | undefined> {
  return client.call('DateTimeEndpoint', 'echoDate', {date});
}

function _echoInstant(
  instant: string | undefined
): Promise<string | undefined> {
  return client.call('DateTimeEndpoint', 'echoInstant', {instant});
}

function _echoListLocalDateTime(
  localDateTimeList: Array<string | undefined> | undefined
): Promise<Array<string | undefined> | undefined> {
  return client.call('DateTimeEndpoint', 'echoListLocalDateTime', {localDateTimeList});
}

function _echoLocalDate(
  localDate: string | undefined
): Promise<string | undefined> {
  return client.call('DateTimeEndpoint', 'echoLocalDate', {localDate});
}

function _echoLocalDateTime(
  localDateTime: string | undefined
): Promise<string | undefined> {
  return client.call('DateTimeEndpoint', 'echoLocalDateTime', {localDateTime});
}

function _echoLocalTime(
  localTime: string | undefined
): Promise<string | undefined> {
  return client.call('DateTimeEndpoint', 'echoLocalTime', {localTime});
}

function _echoMapInstant(
  mapInstant: Record<string, string | undefined> | undefined
): Promise<Record<string, string | undefined> | undefined> {
  return client.call('DateTimeEndpoint', 'echoMapInstant', {mapInstant});
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

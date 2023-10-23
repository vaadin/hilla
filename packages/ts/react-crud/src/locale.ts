import type { DatePickerDate } from '@hilla/react-components/DatePicker.js';
import { createContext, useContext, useMemo } from 'react';

export const LocaleContext = createContext(navigator.language);

function getFormatRegex(format: Intl.DateTimeFormat) {
  const sampleDate = new Date(1234, 5 - 1, 6);
  const formattedSample = format.format(sampleDate);
  const pattern = formattedSample
    .replace('1234', '(?<year>\\d+)')
    .replace('05', '(?<month>\\d+)')
    .replace('5', '(?<month>\\d+)')
    .replace('06', '(?<day>\\d+)')
    .replace('6', '(?<day>\\d+)');

  return new RegExp(pattern, 'u');
}

function tryFormatDate(formatter: Intl.DateTimeFormat, value?: string): string {
  try {
    return value ? formatter.format(new Date(value)) : '';
  } catch (e) {
    return '';
  }
}

export class LocaleFormatter {
  readonly #date: Intl.DateTimeFormat;
  readonly #localTime: Intl.DateTimeFormat;
  readonly #localDateTime: Intl.DateTimeFormat;
  readonly #integer: Intl.NumberFormat;
  readonly #decimal: Intl.NumberFormat;
  readonly #parsePattern: RegExp;

  constructor(locale?: string) {
    this.#date = new Intl.DateTimeFormat(locale);

    this.#localTime = new Intl.DateTimeFormat(locale, {
      hour: 'numeric',
      minute: 'numeric',
    });

    this.#localDateTime = new Intl.DateTimeFormat(locale, {
      day: 'numeric',
      month: 'numeric',
      year: 'numeric',
      hour: 'numeric',
      minute: 'numeric',
    });

    this.#integer = new Intl.NumberFormat(locale, {
      maximumFractionDigits: 0,
    });

    this.#decimal = new Intl.NumberFormat(locale, {
      maximumFractionDigits: 2,
      minimumFractionDigits: 2,
    });

    this.#parsePattern = getFormatRegex(this.#date);
  }

  formatDate(value?: string): string;
  formatDate(value: DatePickerDate): string;
  formatDate(value?: DatePickerDate | string): string {
    if (typeof value === 'object') {
      const { year, month, day } = value;
      const date = new Date();
      date.setFullYear(year);
      date.setMonth(month);
      date.setDate(day);
      return this.#date.format(date);
    }

    return tryFormatDate(this.#date, value);
  }

  formatLocalTime(value?: string): string {
    return tryFormatDate(this.#localTime, `2000-01-01T${value}`);
  }

  formatLocalDateTime(value?: string): string {
    return tryFormatDate(this.#localDateTime, value);
  }

  formatInteger(value?: number): string {
    return value && Number.isFinite(value) ? this.#integer.format(value) : '';
  }

  formatDecimal(value?: number): string {
    return value && Number.isFinite(value) ? this.#decimal.format(value) : '';
  }

  parse(dateString: string): DatePickerDate | undefined {
    const match = this.#parsePattern.exec(dateString);
    const year = Number(match?.groups?.year);
    const month = Number(match?.groups?.month) - 1;
    const day = Number(match?.groups?.day);

    // Verify that the parsed date is valid
    const dateInstance = new Date();
    dateInstance.setFullYear(year);
    dateInstance.setMonth(month);
    dateInstance.setDate(day);

    if (dateInstance.getFullYear() !== year || dateInstance.getMonth() !== month || dateInstance.getDate() !== day) {
      return undefined;
    }

    return { year, month, day };
  }
}

export function useLocaleFormatter(): LocaleFormatter {
  const locale = useContext(LocaleContext);
  return useMemo(() => new LocaleFormatter(locale), [locale]);
}

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

export interface DateObject {
  year: number;
  month: number;
  day: number;
}

export class DateFormatter {
  private readonly _format: Intl.DateTimeFormat;
  private readonly _formatRegex: RegExp;

  constructor(locale?: string) {
    this._format = new Intl.DateTimeFormat(locale);
    this._formatRegex = getFormatRegex(this._format);
  }

  format(date: DateObject): string {
    const dateInstance = new Date();
    dateInstance.setFullYear(date.year);
    dateInstance.setMonth(date.month);
    dateInstance.setDate(date.day);
    return this._format.format(dateInstance);
  }

  parse(dateString: string): DateObject | null {
    const match = this._formatRegex.exec(dateString);
    const year = Number(match?.groups?.year);
    const month = Number(match?.groups?.month) - 1;
    const day = Number(match?.groups?.day);

    // Verify that the parsed date is valid
    const dateInstance = new Date();
    dateInstance.setFullYear(year);
    dateInstance.setMonth(month);
    dateInstance.setDate(day);
    if (dateInstance.getFullYear() !== year || dateInstance.getMonth() !== month || dateInstance.getDate() !== day) {
      return null;
    }

    return { year, month, day };
  }
}

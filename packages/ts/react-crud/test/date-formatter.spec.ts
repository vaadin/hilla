import { expect } from '@esm-bundle/chai';
import { type DateObject, DateFormatter } from '../src/date-formatter';

describe('DateFormatter', () => {
  const testCases = [
    { locale: 'en-US', input: '5/7/2020', output: '5/7/2020', iso: '2020-05-07' },
    { locale: 'en-US', input: '05/07/2020', output: '5/7/2020', iso: '2020-05-07' },
    { locale: 'en-US', input: '12/31/2020', output: '12/31/2020', iso: '2020-12-31' },
    { locale: 'en-US', input: '5/7/1999', output: '5/7/1999', iso: '1999-05-07' },
    { locale: 'en-US', input: '5/7/999', output: '5/7/999', iso: '0999-05-07' },
    { locale: 'en-US', input: '5/7/99', output: '5/7/99', iso: '0099-05-07' },
    { locale: 'de-DE', input: '7.5.2020', output: '7.5.2020', iso: '2020-05-07' },
    { locale: 'de-DE', input: '07.05.2020', output: '7.5.2020', iso: '2020-05-07' },
    { locale: 'de-DE', input: '31.12.2020', output: '31.12.2020', iso: '2020-12-31' },
    { locale: 'de-DE', input: '7.5.1999', output: '7.5.1999', iso: '1999-05-07' },
    { locale: 'de-DE', input: '7.5.999', output: '7.5.999', iso: '0999-05-07' },
    { locale: 'de-DE', input: '7.5.99', output: '7.5.99', iso: '0099-05-07' },
    { locale: 'ko-KR', input: '2020. 5. 7.', output: '2020. 5. 7.', iso: '2020-05-07' },
    { locale: 'ko-KR', input: '2020. 05. 07.', output: '2020. 5. 7.', iso: '2020-05-07' },
    { locale: 'ko-KR', input: '2020. 12. 31.', output: '2020. 12. 31.', iso: '2020-12-31' },
    { locale: 'ko-KR', input: '1999. 12. 31.', output: '1999. 12. 31.', iso: '1999-12-31' },
    { locale: 'ko-KR', input: '999. 12. 31.', output: '999. 12. 31.', iso: '0999-12-31' },
    { locale: 'ko-KR', input: '99. 12. 31.', output: '99. 12. 31.', iso: '0099-12-31' },
    { locale: 'bg', input: '7.5.2020 г.', output: '7.05.2020 г.', iso: '2020-05-07' },
    { locale: 'bg', input: '07.05.2020 г.', output: '7.05.2020 г.', iso: '2020-05-07' },
    { locale: 'bg', input: '31.12.2020 г.', output: '31.12.2020 г.', iso: '2020-12-31' },
    { locale: 'bg', input: '5.07.1999 г.', output: '5.07.1999 г.', iso: '1999-07-05' },
    { locale: 'bg', input: '5.07.999 г.', output: '5.07.999 г.', iso: '0999-07-05' },
    { locale: 'bg', input: '5.07.99 г.', output: '5.07.99 г.', iso: '0099-07-05' },
  ];

  it('should format and parse dates', () => {
    function toIso(dateObject: DateObject) {
      const day = dateObject.day.toString().padStart(2, '0');
      const month = (dateObject.month + 1).toString().padStart(2, '0');
      const year = dateObject.year.toString().padStart(4, '0');
      return `${year}-${month}-${day}`;
    }

    function fromIso(iso: string): DateObject {
      const [year, month, day] = iso.split('-').map(Number);
      return { year, month: month - 1, day };
    }

    testCases.forEach((testCase) => {
      const { locale, iso, input, output } = testCase;
      const formatter = new DateFormatter(locale);

      const parsedDate = formatter.parse(input);
      expect(parsedDate, `Failed to parse date in ${locale}: ${input}`).to.not.be.null;
      const parsedIso = toIso(parsedDate!);
      expect(parsedIso, `Parsing date in locale ${locale} returned wrong result`).to.equal(iso);

      const formattedDate = formatter.format(fromIso(iso));
      expect(formattedDate, `Formatting date in locale ${locale} returned wrong result`).to.equal(output);
    });
  });

  it('should return null when parsing invalid dates', () => {
    const formatter = new DateFormatter('en-US');

    expect(formatter.parse('')).to.be.null;
    expect(formatter.parse('23')).to.be.null;
    expect(formatter.parse('11/23')).to.be.null;
    expect(formatter.parse('11/32/2020')).to.be.null;
    expect(formatter.parse('23/23/2020')).to.be.null;
    expect(formatter.parse('0/23/2020')).to.be.null;
    expect(formatter.parse('11/0/2020')).to.be.null;
  });
});

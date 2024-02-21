import { expect } from '@esm-bundle/chai';
import { I18n } from '../index';

describe('i18n', () => {
  let i18n: I18n;

  beforeEach(() => {
    i18n = new I18n();
  });

  it('should translate', () => {
    expect(i18n.translate('foo')).to.equal('foo');
  });
});

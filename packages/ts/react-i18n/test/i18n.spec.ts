import { expect, use } from '@esm-bundle/chai';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import type { I18nBackend } from '../src/backend.js';
import { I18n } from '../src/index.js';

use(sinonChai);

describe('i18n', () => {
  let i18n: I18n;
  let backend: I18nBackend;
  let loadSpy: sinon.SinonStub;

  function mockTranslations() {
    loadSpy.resolves({ 'addresses.form.name.label': 'Name', 'addresses.form.street.label': 'Street' });
  }

  beforeEach(() => {
    i18n = new I18n();
    // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
    backend = (i18n as any)._backend;
    loadSpy = sinon.stub(backend, 'loadTranslations');
    mockTranslations();
  });

  describe('configure', () => {
    it('should use browser language by default', async () => {
      await i18n.configure();

      expect(i18n.language).to.equal(navigator.language);
      expect(loadSpy).to.have.been.calledOnceWith(navigator.language);
    });

    it('should use explicitly configured language', async () => {
      await i18n.configure({ language: 'zh-Hant' });

      expect(i18n.language).to.equal('zh-Hant');
      expect(loadSpy).to.have.been.calledOnceWith('zh-Hant');
    });

    it('should not throw when loading translations fails', async () => {
      loadSpy.rejects(new Error('Failed to load translations'));

      await i18n.configure();

      expect(i18n.language).to.equal(navigator.language);
    });
  });

  describe('language', () => {
    const initialLanguage = 'en-US';

    beforeEach(async () => {
      await i18n.configure({ language: initialLanguage });
      loadSpy.resetHistory();
    });

    it('should return current language', () => {
      expect(i18n.language).to.equal(initialLanguage);
    });

    it('should set language and load translations', async () => {
      await i18n.setLanguage('de-DE');

      expect(i18n.language).to.equal('de-DE');
      expect(loadSpy).to.have.been.calledOnceWith('de-DE');
    });

    it('should not load translations if language is unchanged', async () => {
      await i18n.setLanguage(initialLanguage);

      expect(i18n.language).to.equal(initialLanguage);
      expect(loadSpy).not.to.have.been.called;
    });
  });

  describe('translate', () => {
    beforeEach(async () => {
      await i18n.configure();
    });

    it('should return translated string', () => {
      expect(i18n.translate('addresses.form.name.label')).to.equal('Name');
      expect(i18n.translate('addresses.form.street.label')).to.equal('Street');
    });

    it('should return key when there is no translation', () => {
      expect(i18n.translate('unknown.key')).to.equal('unknown.key');
    });
  });
});

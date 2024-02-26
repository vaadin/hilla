import { expect, use } from '@esm-bundle/chai';
import { effect } from '@vaadin/hilla-react-signals';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import type { I18nBackend } from '../src/backend.js';
import { I18n } from '../src/index.js';

use(sinonChai);

describe('i18n', () => {
  let i18n: I18n;
  let backend: I18nBackend;
  let loadStub: sinon.SinonStub;

  function mockTranslations() {
    loadStub.resolves({ 'addresses.form.name.label': 'Name', 'addresses.form.street.label': 'Street' });
  }

  beforeEach(() => {
    i18n = new I18n();
    // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
    backend = (i18n as any)._backend;
    loadStub = sinon.stub(backend, 'loadTranslations');
    mockTranslations();
  });

  describe('configure', () => {
    it('should use browser language by default', async () => {
      await i18n.configure();

      expect(i18n.language.value).to.equal(navigator.language);
      expect(loadStub).to.have.been.calledOnceWith(navigator.language);
    });

    it('should use explicitly configured language', async () => {
      await i18n.configure({ language: 'zh-Hant' });

      expect(i18n.language.value).to.equal('zh-Hant');
      expect(loadStub).to.have.been.calledOnceWith('zh-Hant');
    });

    it('should not throw when loading translations fails', async () => {
      loadStub.rejects(new Error('Failed to load translations'));

      await i18n.configure();

      expect(i18n.language.value).to.equal(navigator.language);
    });
  });

  describe('language', () => {
    const initialLanguage = 'en-US';

    beforeEach(async () => {
      await i18n.configure({ language: initialLanguage });
      loadStub.resetHistory();
    });

    it('should return current language', () => {
      expect(i18n.language.value).to.equal(initialLanguage);
    });

    it('should set language and load translations', async () => {
      await i18n.setLanguage('de-DE');

      expect(i18n.language.value).to.equal('de-DE');
      expect(loadStub).to.have.been.calledOnceWith('de-DE');
    });

    it('should not load translations if language is unchanged', async () => {
      await i18n.setLanguage(initialLanguage);

      expect(i18n.language.value).to.equal(initialLanguage);
      expect(loadStub).not.to.have.been.called;
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

  describe('global side effects', () => {
    let effectSpy: sinon.SinonSpy;

    beforeEach(() => {
      effectSpy = sinon.spy();
      effect(() => {
        effectSpy(i18n.translate('addresses.form.name.label'));
      });
    });

    it('should run effects when language changes', async () => {
      // Runs once initially
      expect(effectSpy.calledOnce).to.be.true;

      // Configure initial language
      await i18n.configure({ language: 'en-US' });
      expect(effectSpy.calledTwice).to.be.true;

      // Change language
      // await i18n.setLanguage('de-DE');
      // expect(effectSpy.calledThrice).to.be.true;
    });
  });
});

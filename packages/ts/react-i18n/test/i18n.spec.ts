import { expect, use } from '@esm-bundle/chai';
import { effect } from '@vaadin/hilla-react-signals';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import type { I18nBackend } from '../src/backend.js';
import { i18n as globalI18n, I18n } from '../src/index.js';

use(sinonChai);

describe('@vaadin/hilla-react-i18n', () => {
  describe('i18n', () => {
    let i18n: I18n;
    let backend: I18nBackend;
    let loadStub: sinon.SinonStub;

    beforeEach(() => {
      i18n = new I18n();
      // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
      backend = (i18n as any)._backend;
      loadStub = sinon.stub(backend, 'loadTranslations');
      loadStub.resolves({
        'addresses.form.city.label': 'City',
        'addresses.form.street.label': 'Street',
      });
      loadStub.withArgs('de-DE').resolves({
        'addresses.form.city.label': 'Stadt',
        'addresses.form.street.label': 'Strasse',
      });
    });

    describe('global instance', () => {
      it('should expose a global I18n instance', () => {
        expect(globalI18n).to.exist;
        expect(globalI18n).to.be.instanceof(I18n);
      });
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
        expect(i18n.translate('addresses.form.city.label')).to.equal('City');
        expect(i18n.translate('addresses.form.street.label')).to.equal('Street');
      });

      it('should return key when there is no translation', () => {
        expect(i18n.translate('unknown.key')).to.equal('unknown.key');
      });
    });

    describe('global side effects', () => {
      it('should run effects when language changes', async () => {
        const effectSpy = sinon.spy();
        effect(() => {
          // Use multiple signals in the effect to verify signals are updated in batch
          effectSpy(i18n.language.value, i18n.translate('addresses.form.city.label'));
        });

        // Runs once initially
        expect(effectSpy.calledOnceWith(undefined, 'addresses.form.city.label')).to.be.true;
        effectSpy.resetHistory();

        // Configure initial language
        await i18n.configure({ language: 'en-US' });
        expect(effectSpy.calledOnceWith('en-US', 'City')).to.be.true;
        effectSpy.resetHistory();

        // Change language
        await i18n.setLanguage('de-DE');
        expect(effectSpy.calledOnceWith('de-DE', 'Stadt')).to.be.true;
      });
    });
  });
});

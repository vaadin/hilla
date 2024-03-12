import { expect, use } from '@esm-bundle/chai';
import { render } from '@testing-library/react';
import CookieManager from '@vaadin/hilla-frontend/CookieManager.js';
import { effect, useComputed, useSignalEffect } from '@vaadin/hilla-react-signals';
import fetchMock from 'fetch-mock';
import { useEffect, useMemo } from 'react';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import { i18n as globalI18n, I18n, translate as globalTranslate } from '../src/index.js';
import type { LanguageSettings } from '../src/settings.js';

use(sinonChai);

describe('@vaadin/hilla-react-i18n', () => {
  describe('i18n', () => {
    let i18n: I18n;

    function getSettingsCookie(): LanguageSettings | undefined {
      const cookie = CookieManager.get('vaadinLanguageSettings');
      return cookie && JSON.parse(cookie);
    }

    function setSettingsCookie(settings: LanguageSettings) {
      CookieManager.set('vaadinLanguageSettings', JSON.stringify(settings));
    }

    function setInvalidSettingsCookie() {
      CookieManager.set('vaadinLanguageSettings', 'foo');
    }

    function clearSettingsCookie() {
      CookieManager.remove('vaadinLanguageSettings');
    }

    function verifyLoadTranslations(language: string) {
      expect(fetchMock.called(`./?v-r=i18n&langtag=${language}`)).to.be.true;
    }

    beforeEach(() => {
      clearSettingsCookie();
      i18n = new I18n();
      fetchMock
        .get('./?v-r=i18n&langtag=de-DE', {
          body: {
            'addresses.form.city.label': 'Stadt',
            'addresses.form.street.label': 'Strasse',
          },
          status: 200,
          headers: { 'X-Vaadin-Retrieved-Locale': 'de-DE' },
        })
        .get('./?v-r=i18n&langtag=not-found', 404)
        .get('./?v-r=i18n&langtag=unknown', {
          body: {
            'addresses.form.city.label': 'Sehir',
            'addresses.form.street.label': 'Sokak',
          },
          status: 200,
        })
        .get('*', {
          body: {
            'addresses.form.city.label': 'City',
            'addresses.form.street.label': 'Street',
          },
          status: 200,
          headers: { 'X-Vaadin-Retrieved-Locale': 'und' },
        });
    });

    afterEach(() => {
      fetchMock.restore();
    });

    describe('configure', () => {
      it('should use browser language by default', async () => {
        await i18n.configure();

        expect(i18n.language.value).to.equal(navigator.language);
        verifyLoadTranslations(navigator.language);
      });

      it('should use last used language if defined', async () => {
        setSettingsCookie({ language: 'zh-Hant' });
        await i18n.configure();

        expect(i18n.language.value).to.equal('zh-Hant');
        expect(i18n.resolvedLanguage.value).to.equal('und');
        verifyLoadTranslations('zh-Hant');
      });

      it('should use browser language if settings cookie is invalid', async () => {
        setInvalidSettingsCookie();
        await i18n.configure();

        expect(i18n.language.value).to.equal(navigator.language);
        verifyLoadTranslations(navigator.language);
      });

      it('should use explicitly configured language if specified', async () => {
        await i18n.configure({ language: 'zh-Hant' });

        expect(i18n.language.value).to.equal('zh-Hant');
        expect(i18n.resolvedLanguage.value).to.equal('und');
        verifyLoadTranslations('zh-Hant');
      });

      it('should prefer explicitly configured language over last used language', async () => {
        setSettingsCookie({ language: 'de-DE' });
        await i18n.configure({ language: 'zh-Hant' });

        expect(i18n.language.value).to.equal('zh-Hant');
        expect(i18n.resolvedLanguage.value).to.equal('und');
        verifyLoadTranslations('zh-Hant');
      });

      it('should not store last used language when initializing', async () => {
        await i18n.configure();

        expect(getSettingsCookie()?.language).to.not.exist;
      });

      it('should not throw when loading translations fails', async () => {
        const initialLanguage = i18n.language.value;
        await i18n.configure({ language: 'not-found' });

        expect(i18n.language.value).to.equal(initialLanguage);
        expect(i18n.resolvedLanguage.value).to.equal(initialLanguage);
      });
    });

    describe('language', () => {
      const initialLanguage = 'en-US';
      const initialResolvedLanguage = 'und';

      beforeEach(async () => {
        await i18n.configure({ language: initialLanguage });
        fetchMock.resetHistory();
      });

      it('should return current language', () => {
        expect(i18n.language.value).to.equal(initialLanguage);
        expect(i18n.resolvedLanguage.value).to.equal(initialResolvedLanguage);
      });

      it('should set language and load translations', async () => {
        await i18n.setLanguage('de-DE');

        expect(i18n.language.value).to.equal('de-DE');
        expect(i18n.resolvedLanguage.value).to.equal('de-DE');
        verifyLoadTranslations('de-DE');
      });

      it('should store last used language', async () => {
        await i18n.setLanguage('de-DE');

        expect(getSettingsCookie()?.language).to.equal('de-DE');
      });

      it('should not load translations if language is unchanged', async () => {
        await i18n.setLanguage(initialLanguage);

        expect(i18n.language.value).to.equal(initialLanguage);
        expect(i18n.resolvedLanguage.value).to.equal(initialResolvedLanguage);
        expect(fetchMock.called()).to.be.false;
      });

      it('should still load translations if resolved language is empty', async () => {
        await i18n.setLanguage('unknown');

        expect(i18n.language.value).to.equal('unknown');
        expect(i18n.resolvedLanguage.value).to.equal(undefined);
        verifyLoadTranslations('unknown');
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

    describe('global instance', () => {
      it('should expose a global I18n instance', () => {
        expect(globalI18n).to.exist;
        expect(globalI18n).to.be.instanceof(I18n);
      });

      it('should expose a global translate function that delegates to global I18n instance', async () => {
        await globalI18n.configure({ language: 'en-US' });
        expect(globalTranslate('addresses.form.city.label')).to.equal('City');

        await globalI18n.setLanguage('de-DE');
        expect(globalTranslate('addresses.form.city.label')).to.equal('Stadt');
      });
    });

    describe('react integration', () => {
      it('should re-render when language changes', async () => {
        function TestTranslateComponent() {
          return <div>{i18n.translate('addresses.form.city.label')}</div>;
        }

        const { getByText } = render(<TestTranslateComponent />);

        // No language
        expect(getByText('addresses.form.city.label')).to.exist;

        // Configure initial language
        await i18n.configure({ language: 'en-US' });
        expect(getByText('City')).to.exist;

        // Change language
        await i18n.setLanguage('de-DE');
        expect(getByText('Stadt')).to.exist;
      });

      it('should run signal effects when language changes', async () => {
        let signalEffectResult = '';

        function TestUseSignalEffectComponent() {
          useSignalEffect(() => {
            signalEffectResult = i18n.translate('addresses.form.city.label');
          });
          return <div></div>;
        }

        render(<TestUseSignalEffectComponent />);

        // No language
        expect(signalEffectResult).to.equal('addresses.form.city.label');

        // Configure initial language
        await i18n.configure({ language: 'en-US' });
        expect(signalEffectResult).to.equal('City');

        // Change language
        await i18n.setLanguage('de-DE');
        expect(signalEffectResult).to.equal('Stadt');
      });

      it('should update computed signals when language changes', async () => {
        function TestUseComputedComponent() {
          const computedTranslation = useComputed(
            () => `Computed translation: ${i18n.translate('addresses.form.city.label')}`,
          );
          return <div>{computedTranslation.value}</div>;
        }

        const { getByText } = render(<TestUseComputedComponent />);

        // No language
        expect(getByText('Computed translation: addresses.form.city.label')).to.exist;

        // Configure initial language
        await i18n.configure({ language: 'en-US' });
        expect(getByText('Computed translation: City')).to.exist;

        // Change language
        await i18n.setLanguage('de-DE');
        expect(getByText('Computed translation: Stadt')).to.exist;
      });

      it('should run default effects when language changes', async () => {
        let defaultEffectResult = '';

        function TestUseEffectComponent() {
          useEffect(() => {
            defaultEffectResult = i18n.translate('addresses.form.city.label');
          }, [i18n.language.value]);
          return <div></div>;
        }

        render(<TestUseEffectComponent />);

        // No language
        expect(defaultEffectResult).to.equal('addresses.form.city.label');

        // Configure initial language
        await i18n.configure({ language: 'en-US' });
        expect(defaultEffectResult).to.equal('City');

        // Change language
        await i18n.setLanguage('de-DE');
        expect(defaultEffectResult).to.equal('Stadt');
      });

      it('should update memoizations when language changes', async () => {
        function TestUseMemoComponent() {
          const memoizedTranslation = useMemo(
            () => `Memoized translation: ${i18n.translate('addresses.form.city.label')}`,
            [i18n.language.value],
          );
          return <div>{memoizedTranslation}</div>;
        }

        const { getByText } = render(<TestUseMemoComponent />);

        // No language
        expect(getByText('Memoized translation: addresses.form.city.label')).to.exist;

        // Configure initial language
        await i18n.configure({ language: 'en-US' });
        expect(getByText('Memoized translation: City')).to.exist;

        // Change language
        await i18n.setLanguage('de-DE');
        expect(getByText('Memoized translation: Stadt')).to.exist;
      });
    });
  });
});

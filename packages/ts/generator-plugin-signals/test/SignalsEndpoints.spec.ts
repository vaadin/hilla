import { readFile } from 'node:fs/promises';
import Generator from '@vaadin/hilla-generator-core/Generator.js';
import BackbonePlugin from '@vaadin/hilla-generator-plugin-backbone';
import LoggerFactory from '@vaadin/hilla-generator-utils/LoggerFactory.js';
import snapshotMatcher from '@vaadin/hilla-generator-utils/testing/snapshotMatcher.js';
import { expect, use } from 'chai';
import sinonChai from 'sinon-chai';
import SignalsPlugin from '../src/index.js';

use(sinonChai);
use(snapshotMatcher);

describe('SignalsPlugin', () => {
  context('Endpoint methods with Signals as return type', () => {
    it('correctly generates service with mixture of normal and signal returning methods', async () => {
      const generator = new Generator([BackbonePlugin, SignalsPlugin], {
        logger: new LoggerFactory({ name: 'signals-plugin-test', verbose: true }),
      });
      const input = await readFile(new URL('./hilla-openapi-mix.json', import.meta.url), 'utf8');
      const files = await generator.process(input);

      const generatedNumberSignalService = files.find((f) => f.name === 'NumberSignalService.ts')!;
      await expect(await generatedNumberSignalService.text()).toMatchSnapshot(
        `NumberSignalServiceMix.snap.ts`,
        import.meta.url,
      );

      // Non-signal returning services should remain unchanged as before:
      const generatedHelloWorldService = files.find((f) => f.name === 'HelloWorldService.ts')!;
      await expect(await generatedHelloWorldService.text()).toMatchSnapshot(
        `HelloWorldService.snap.ts`,
        import.meta.url,
      );
    });

    it('removes unused request init import', async () => {
      const generator = new Generator([BackbonePlugin, SignalsPlugin], {
        logger: new LoggerFactory({ name: 'signals-plugin-test', verbose: true }),
      });
      const input = await readFile(new URL('./hilla-openapi.json', import.meta.url), 'utf8');
      const files = await generator.process(input);

      // Signal-only returning services should have the init import removed:
      const generatedNumberSignalService = files.find((f) => f.name === 'NumberSignalService.ts')!;
      await expect(await generatedNumberSignalService.text()).toMatchSnapshot(
        `NumberSignalServiceSignalOnly.snap.ts`,
        import.meta.url,
      );

      // Non-signal returning services should remain unchanged as before:
      const generatedHelloWorldService = files.find((f) => f.name === 'HelloWorldService.ts')!;
      await expect(await generatedHelloWorldService.text()).toMatchSnapshot(
        `HelloWorldService.snap.ts`,
        import.meta.url,
      );
    });

    it('correctly generates service with ListSignal returning methods, with and without parameters', async () => {
      const generator = new Generator([BackbonePlugin, SignalsPlugin], {
        logger: new LoggerFactory({ name: 'signals-plugin-test', verbose: true }),
      });
      const input = await readFile(new URL('./hilla-openapi-mix.json', import.meta.url), 'utf8');
      const files = await generator.process(input);

      const generatedListSignalService = files.find((f) => f.name === 'ChatService.ts')!;
      await expect(await generatedListSignalService.text()).toMatchSnapshot(`ChatService.snap.ts`, import.meta.url);
    });

    it('correctly generates service with mixture of all Signal returning methods', async () => {
      const generator = new Generator([BackbonePlugin, SignalsPlugin], {
        logger: new LoggerFactory({ name: 'signals-plugin-test', verbose: true }),
      });
      const input = await readFile(new URL('./hilla-openapi-mix.json', import.meta.url), 'utf8');
      const files = await generator.process(input);

      const generatedValueSignalService = files.find((f) => f.name === 'PersonService.ts')!;
      await expect(await generatedValueSignalService.text()).toMatchSnapshot(
        `SignalServiceMix.snap.ts`,
        import.meta.url,
      );

      // Non-signal returning services should remain unchanged as before:
      const generatedHelloWorldService = files.find((f) => f.name === 'HelloWorldService.ts')!;
      await expect(await generatedHelloWorldService.text()).toMatchSnapshot(
        `HelloWorldService.snap.ts`,
        import.meta.url,
      );
    });
  });
});

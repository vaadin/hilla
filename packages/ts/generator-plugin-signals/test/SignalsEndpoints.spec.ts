import { readFile } from 'node:fs/promises';
import Generator from '@vaadin/hilla-generator-core/Generator.js';
import BackbonePlugin from '@vaadin/hilla-generator-plugin-backbone';
import TransferTypesPlugin from '@vaadin/hilla-generator-plugin-transfertypes';
import LoggerFactory from '@vaadin/hilla-generator-utils/LoggerFactory.js';
import sinonChai from 'sinon-chai';
import { beforeEach, chai, describe, expect, it } from 'vitest';
import SignalsPlugin from '../src/index.js';

chai.use(sinonChai);

describe('SignalsPlugin', () => {
  describe('Endpoint methods with Signals as return type', () => {
    let generator: Generator;

    beforeEach(() => {
      generator = new Generator([TransferTypesPlugin, BackbonePlugin, SignalsPlugin], {
        logger: new LoggerFactory({ name: 'signals-plugin-test', verbose: true }),
      });
    });

    it('correctly generates service with mixture of normal and signal returning methods', async () => {
      const input = await readFile(new URL('./hilla-openapi-mix.json', import.meta.url), 'utf8');
      const files = await generator.process(input);

      const generatedNumberSignalService = files.find((f) => f.name === 'NumberSignalService.ts')!;
      await expect(generatedNumberSignalService.text()).resolves.toMatchFileSnapshot(
        `fixtures/NumberSignalServiceMix.snap.ts`,
      );

      // Non-signal returning services should remain unchanged as before:
      const generatedHelloWorldService = files.find((f) => f.name === 'HelloWorldService.ts')!;
      await expect(await generatedHelloWorldService.text()).toMatchFileSnapshot(`fixtures/HelloWorldService.snap.ts`);
    });

    it('removes unused request init import', async () => {
      const input = await readFile(new URL('./hilla-openapi.json', import.meta.url), 'utf8');
      const files = await generator.process(input);

      // Signal-only returning services should have the init import removed:
      const generatedNumberSignalService = files.find((f) => f.name === 'NumberSignalService.ts')!;
      await expect(await generatedNumberSignalService.text()).toMatchFileSnapshot(
        `fixtures/NumberSignalServiceSignalOnly.snap.ts`,
      );

      // Non-signal returning services should remain unchanged as before:
      const generatedHelloWorldService = files.find((f) => f.name === 'HelloWorldService.ts')!;
      await expect(await generatedHelloWorldService.text()).toMatchFileSnapshot(`fixtures/HelloWorldService.snap.ts`);
    });

    it('correctly generates service with ListSignal returning methods, with and without parameters', async () => {
      const input = await readFile(new URL('./hilla-openapi-mix.json', import.meta.url), 'utf8');
      const files = await generator.process(input);

      const generatedListSignalService = files.find((f) => f.name === 'ChatService.ts')!;
      await expect(await generatedListSignalService.text()).toMatchFileSnapshot(`fixtures/ChatService.snap.ts`);
    });

    it('correctly generates service with mixture of all Signal returning methods', async () => {
      const input = await readFile(new URL('./hilla-openapi-mix.json', import.meta.url), 'utf8');
      const files = await generator.process(input);

      const generatedValueSignalService = files.find((f) => f.name === 'PersonService.ts')!;
      await expect(await generatedValueSignalService.text()).toMatchFileSnapshot(`fixtures/SignalServiceMix.snap.ts`);

      // Non-signal returning services should remain unchanged as before:
      const generatedHelloWorldService = files.find((f) => f.name === 'HelloWorldService.ts')!;
      await expect(await generatedHelloWorldService.text()).toMatchFileSnapshot(`fixtures/HelloWorldService.snap.ts`);
    });

    it('correctly generates service with automatic default values for value signals of primitive types', async () => {
      const input = await readFile(new URL('./hilla-openapi-default-value.json', import.meta.url), 'utf8');
      const files = await generator.process(input);

      const generatedValueSignalService = files.find((f) => f.name === 'PrimitiveTypeValueSignalService.ts')!;
      await expect(await generatedValueSignalService.text()).toMatchFileSnapshot(
        `fixtures/PrimitiveTypeValueSignalService.snap.ts`,
      );
    });
  });
});

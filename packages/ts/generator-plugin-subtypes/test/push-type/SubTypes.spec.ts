/* eslint-disable import/no-extraneous-dependencies */
import BackbonePlugin from '@vaadin/hilla-generator-plugin-backbone/index.js';
import ModelPlugin from '@vaadin/hilla-generator-plugin-model/index.js';
import sinonChai from 'sinon-chai';
import { beforeAll, chai, describe, expect, it } from 'vitest';
import SubTypesPlugin from '../../src/index.js';
import { createGenerator, loadInput, pathBase } from '../utils/common.js';

chai.use(sinonChai);

const sectionName = 'SubTypes';

describe('SubTypesPlugin', () => {
  let files: readonly File[];

  beforeAll(async () => {
    const generator = createGenerator([BackbonePlugin, ModelPlugin, SubTypesPlugin]);
    const input = await loadInput(sectionName, import.meta.url);
    files = await generator.process(input);
  });

  async function expectSource(name: string): Promise<void> {
    const file = files.find((f) => f.name === name)!;
    expect(file, name).to.exist;
    await expect(await file.text()).toMatchFileSnapshot(
      `fixtures/${name.split('/').pop()!.replace('.ts', '.snap.ts')}`,
    );
  }

  describe('when the entity has `oneOf`', () => {
    it('generates as union type', async () => {
      // the union type model is not generated, the other files are
      expect(files.map((f) => f.name)).to.not.include(`${pathBase}/BaseEventUnionModel.ts`);
      await expectSource(`${sectionName}Endpoint.ts`);
      await expectSource(`${pathBase}/BaseEventUnion.ts`);
      await expectSource(`${pathBase}/BaseEvent.ts`);
      await expectSource(`${pathBase}/AddEvent.ts`);
    });

    it('removes the discriminator from the model', async () => {
      await expectSource(`${pathBase}/AddEventModel.ts`);
    });
  });

  describe('when `@JsonTypeInfo` defines a custom property', () => {
    it('uses that property as the discriminator', async () => {
      await expectSource(`${pathBase}/NotificationUnion.ts`);
      await expectSource(`${pathBase}/EmailNotification.ts`);
      await expectSource(`${pathBase}/NotificationModel.ts`);
    });

    it('adds the discriminator to the type that declares the subtypes', async () => {
      await expectSource(`${pathBase}/Notification.ts`);
    });

    it('adds the discriminator to indirect subtypes', async () => {
      await expectSource(`${pathBase}/HtmlEmailNotification.ts`);
    });
  });
});

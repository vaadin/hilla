/* eslint-disable import/no-extraneous-dependencies */
import BackbonePlugin from '@vaadin/hilla-generator-plugin-backbone/index.js';
import ModelPlugin from '@vaadin/hilla-generator-plugin-model/index.js';
import sinonChai from 'sinon-chai';
import { beforeAll, chai, describe, expect, it } from 'vitest';
import SubTypesPlugin from '../../src/index.js';
import { createGenerator, loadInput, pathBase, typeCheck } from '../utils/common.js';

chai.use(sinonChai);

const sectionName = 'SubTypes';

// exhaustive switches that only compile if the generated union types are
// properly discriminated
const USAGE = `
import type BaseEventUnion from './${pathBase}/BaseEventUnion.js';
import type NotificationUnion from './${pathBase}/NotificationUnion.js';
import type ShapeUnion from './${pathBase}/ShapeUnion.js';

export function describeEvent(event: BaseEventUnion): string {
  switch (event['@type']) {
    case 'add': return event.item ?? '';
    case 'update': return event.newItem ?? '';
    case 'delete': return String(event.force);
    default: { const exhaustive: never = event; return exhaustive; }
  }
}

export function describeNotification(notification: NotificationUnion): string {
  switch (notification.kind) {
    case 'plain': return notification.message ?? '';
    case 'email': return notification.address ?? '';
    case 'html-email': return notification.html ?? '';
    case 'multipart-sms': return String(notification.parts);
    default: { const exhaustive: never = notification; return exhaustive; }
  }
}

export function describeShape(shape: ShapeUnion): number {
  switch (shape.shape) {
    case 'circle': return shape.radius;
    case 'square': return shape.side;
    default: { const exhaustive: never = shape; return exhaustive; }
  }
}
`;

describe('SubTypesPlugin', () => {
  let files: readonly File[];

  beforeAll(async () => {
    const generator = createGenerator([BackbonePlugin, ModelPlugin, SubTypesPlugin]);
    files = await generator.process(await loadInput(sectionName, import.meta.url));
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
      await expectSource(`${pathBase}/HtmlEmailNotification.ts`);
    });

    it('keeps a subtype that is a supertype of another subtype open', async () => {
      // the discriminator of those types is pinned in the union type instead,
      // as it is narrowed to another value further down the hierarchy
      await expectSource(`${pathBase}/Notification.ts`);
      await expectSource(`${pathBase}/NotificationModel.ts`);
      await expectSource(`${pathBase}/EmailNotification.ts`);
      await expectSource(`${pathBase}/EmailNotificationModel.ts`);
    });

    it('adds the discriminator below a class that is not a subtype', async () => {
      await expectSource(`${pathBase}/SmsNotification.ts`);
      await expectSource(`${pathBase}/MultipartSmsNotification.ts`);
    });
  });

  describe('when the supertype is an interface', () => {
    it('adds the discriminator to the implementations', async () => {
      await expectSource(`${pathBase}/ShapeUnion.ts`);
      await expectSource(`${pathBase}/Circle.ts`);
      // the import of the model that only the discriminator used is dropped
      await expectSource(`${pathBase}/CircleModel.ts`);
    });
  });

  it('generates code that compiles and discriminates the union types', async () => {
    expect(await typeCheck(files, { 'usage.ts': USAGE })).to.deep.equal([]);
  }, 30000); // compiling the whole hierarchy is slow on CI
});

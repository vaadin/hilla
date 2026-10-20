import m, { BooleanModel, type Model, NumberModel, StringModel } from '@vaadin/hilla-models';
import { describe, expect, it } from 'vitest';
import { getDefaultProperties, ModelInfo } from '../src/model-info.js';
import { NicknamedModel, PersonModel } from './test-models-and-services.js';

describe('@vaadin/hilla-react-crud', () => {
  describe('ModelInfo', () => {
    interface TestItem {
      property: any;
    }

    function getPropertyType(modelType: Model, jvmType?: string) {
      const TestModel = m
        .object<TestItem>('TestItem')
        .property('property', jvmType ? m.meta(modelType, { jvmType }) : modelType)
        .build();

      const property = new ModelInfo(TestModel).getProperty('property');
      expect(property).to.exist;
      return property!.type;
    }

    it('detects correct property types based on Java type and model type', () => {
      expect(getPropertyType(StringModel)).to.equal('string');
      expect(getPropertyType(NumberModel)).to.equal('decimal');
      expect(getPropertyType(BooleanModel)).to.equal('boolean');

      expect(getPropertyType(NumberModel, 'byte')).to.equal('integer');
      expect(getPropertyType(NumberModel, 'java.lang.Byte')).to.equal('integer');
      expect(getPropertyType(NumberModel, 'short')).to.equal('integer');
      expect(getPropertyType(NumberModel, 'java.lang.Short')).to.equal('integer');
      expect(getPropertyType(NumberModel, 'int')).to.equal('integer');
      expect(getPropertyType(NumberModel, 'java.lang.Integer')).to.equal('integer');
      expect(getPropertyType(NumberModel, 'long')).to.equal('integer');
      expect(getPropertyType(NumberModel, 'java.lang.Long')).to.equal('integer');
      expect(getPropertyType(NumberModel, 'float')).to.equal('decimal');
      expect(getPropertyType(NumberModel, 'java.lang.Float')).to.equal('decimal');
      expect(getPropertyType(NumberModel, 'double')).to.equal('decimal');
      expect(getPropertyType(NumberModel, 'java.lang.Double')).to.equal('decimal');

      expect(getPropertyType(StringModel, 'java.time.LocalDate')).to.equal('date');
      expect(getPropertyType(StringModel, 'java.time.LocalTime')).to.equal('time');
      expect(getPropertyType(StringModel, 'java.time.OffsetTime')).to.equal('time');
      expect(getPropertyType(StringModel, 'java.time.LocalDateTime')).to.equal('datetime');
      expect(getPropertyType(StringModel, 'java.time.ZonedDateTime')).to.equal('datetime');
      expect(getPropertyType(StringModel, 'java.time.OffsetDateTime')).to.equal('datetime');
      expect(getPropertyType(StringModel, 'java.util.Date')).to.equal('datetime');
      expect(getPropertyType(StringModel, 'java.sql.Date')).to.equal('datetime');
    });

    describe('getProperty', () => {
      it('returns property information', () => {
        const modelInfo = new ModelInfo(PersonModel);

        let prop = modelInfo.getProperty('firstName');
        expect(prop).to.exist;
        expect(prop!.name).to.equal('firstName');
        expect(prop!.type).to.equal('string');
        expect(prop!.humanReadableName).to.equal('First name');

        prop = modelInfo.getProperty('address.street');
        expect(prop).to.exist;
        expect(prop!.name).to.equal('address.street');
        expect(prop!.type).to.equal('string');
        expect(prop!.humanReadableName).to.equal('Street');
      });

      it('handles unknown properties', () => {
        const modelInfo = new ModelInfo(PersonModel);
        expect(modelInfo.getProperty('foo')).to.be.undefined;
        expect(modelInfo.getProperty('address.foo')).to.be.undefined;
        expect(modelInfo.getProperty('foo.bar')).to.be.undefined;
      });
    });

    describe('getRootProperties', () => {
      it('returns property information', () => {
        const modelInfo = new ModelInfo(PersonModel);

        const props = modelInfo.getRootProperties();
        const propertyNames = props.map((prop) => prop.name);

        expect(propertyNames).to.eql([
          'firstName',
          'lastName',
          'id',
          'version',
          'gender',
          'email',
          'someInteger',
          'someDecimal',
          'vip',
          'birthDate',
          'shiftStart',
          'appointmentTime',
          'address',
          'department',
        ]);
      });

      it('returns property information for a model with a deferred base', () => {
        const modelInfo = new ModelInfo(NicknamedModel);

        const propertyNames = modelInfo.getRootProperties().map((prop) => prop.name);

        // the base is kept out of the prototype chain, so its properties have to
        // arrive exactly once and still ahead of the ones declared here
        expect(propertyNames).to.eql(['firstName', 'lastName', 'nickname']);
      });

      it('returns property information in order for a deferred base that inherits properties', () => {
        const modelInfo = new ModelInfo(
          m
            .extend(() => NicknamedModel)
            .object('Pseudonymous')
            .build(),
        );

        const propertyNames = modelInfo.getRootProperties().map((prop) => prop.name);

        // the properties the base inherits come ahead of its own
        expect(propertyNames).to.eql(['firstName', 'lastName', 'nickname']);
      });

      it('returns property information for nested model', () => {
        const modelInfo = new ModelInfo(PersonModel);

        const props = modelInfo.getRootProperties('address');
        const propertyNames = props.map((prop) => prop.name);

        expect(propertyNames).to.eql(['address.street', 'address.city', 'address.country']);
      });

      it('returns empty array for unknown property paths', () => {
        const modelInfo = new ModelInfo(PersonModel);
        expect(modelInfo.getRootProperties('foo')).to.eql([]);
      });
    });

    describe('getProperties', () => {
      it('returns empty array for unknown property paths', () => {
        const modelInfo = new ModelInfo(PersonModel);

        const props = modelInfo.getProperties(['firstName', 'address.street', 'department.name']);
        const propertyNames = props.map((prop) => prop.name);

        expect(propertyNames).to.eql(['firstName', 'address.street', 'department.name']);
      });

      it('returns empty array for unknown property paths', () => {
        const modelInfo = new ModelInfo(PersonModel);
        expect(modelInfo.getProperties(['foo', 'address.foo', 'foo.bar'])).to.eql([]);
      });
    });

    describe('getDefaultProperties', () => {
      it('excludes a type discriminator', () => {
        interface Event {
          '@type': 'add';
          name: string;
        }

        const EventModel = m
          .object<Event>('Event')
          .property('@type', m.literal('add'))
          .property('name', StringModel)
          .build();

        const names = getDefaultProperties(new ModelInfo(EventModel)).map((prop) => prop.name);

        expect(names).to.eql(['name']);
      });
    });
  });
});

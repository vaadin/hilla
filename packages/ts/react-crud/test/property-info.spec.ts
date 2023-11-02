import { expect } from '@esm-bundle/chai';
import { _getPropertyModel, BooleanModel, NumberModel, ObjectModel, StringModel } from '@hilla/form';
import { getProperties } from '../src/property-info';

describe('@hilla/react-crud', () => {
  describe('PropertyInfo', () => {
    interface TestItem {
      property: any;
    }

    function getPropertyType(modelType: any, javaType?: string) {
      class TestModel extends ObjectModel<TestItem> {
        get property(): any {
          const options = javaType ? { meta: { javaType } } : undefined;
          // eslint-disable-next-line @typescript-eslint/no-unsafe-call
          return this[_getPropertyModel]('property', (parent, key) => new modelType(parent, key, false, options));
        }
      }

      const properties = getProperties(TestModel);
      expect(properties.length).to.equal(1);
      return properties[0].type;
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
      expect(getPropertyType(StringModel, 'java.time.LocalDateTime')).to.equal('datetime');
    });
  });
});

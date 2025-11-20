import { BooleanModel, NumberModel, EnumModel, type Enum, $enum } from '@vaadin/hilla-models';
import isNumeric from 'validator/es/lib/isNumeric.js';
import { AbstractModel } from './Models.js';
import type { ProvisionalModel } from './ProvisionalModel.js';
import type { StringConverter } from './StringConverter.js';

function toString(this: void, value: number | string | boolean | undefined): string {
  // Undefined value is represented with an empty string in form fields.
  if (value === undefined) {
    return '';
  }

  return String(value);
}

const booleanStringConverter: StringConverter<boolean> = {
  fromString(this: void, value: string): boolean | undefined {
    return ['true', '1', 'yes'].includes(value.toLowerCase());
  },
  toString,
};

const numberStringConverter: StringConverter<number> = {
  fromString(this: void, value: string): number | undefined {
    if (value === '') {
      return undefined;
    }
    return isNumeric(value) ? Number.parseFloat(value) : NaN;
  },
  toString,
};

const enumStringConverters = new WeakMap<EnumModel<typeof Enum>, StringConverter<typeof Enum>>();

export function getStringConverter<V>(model: ProvisionalModel<V>): StringConverter<V> | undefined {
  if (model instanceof AbstractModel) {
    return undefined;
  }

  if (model instanceof BooleanModel) {
    return booleanStringConverter as StringConverter<V>;
  }
  if (model instanceof NumberModel) {
    return numberStringConverter as StringConverter<V>;
  }
  if (model instanceof EnumModel) {
    if (enumStringConverters.has(model)) {
      return enumStringConverters.get(model) as StringConverter<V>;
    }

    const modelEnum = model[$enum];
    const enumStringConverter = {
      toString,
      fromString(this: void, value: string): V | undefined {
        return value in modelEnum ? (value as V) : undefined;
      },
    };
    enumStringConverters.set(model, enumStringConverter as StringConverter<typeof Enum>);
    return enumStringConverter as StringConverter<V>;
  }

  return undefined;
}

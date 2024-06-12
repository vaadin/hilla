import { CoreModelBuilder } from './builders.js';
import { $enum, $itemModel, type Enum, Model } from './model.js';

export const PrimitiveModel = CoreModelBuilder.from(Model, (): unknown => undefined)
  .name('primitive')
  .build();

export const StringModel = CoreModelBuilder.from(PrimitiveModel, () => '')
  .name('string')
  .build();

export const NumberModel = CoreModelBuilder.from(PrimitiveModel, () => 0)
  .name('number')
  .build();

export const BooleanModel = CoreModelBuilder.from(PrimitiveModel, () => false)
  .name('boolean')
  .build();

export const ArrayModel = CoreModelBuilder.from(Model, (): unknown[] => [])
  .name('Array')
  .define($itemModel, Model)
  .build();

export const ObjectModel = CoreModelBuilder.from(Model, () => ({}))
  .name('Object')
  .build();

export const EnumModel = CoreModelBuilder.from(
  Model,
  (): (typeof Enum)[keyof typeof Enum] => Object.values(EnumModel[$enum])[0],
)
  .name('Enum')
  .define($enum, {} as typeof Enum)
  .build();

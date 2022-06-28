// @ts-nocheck

import MyBazModel from './MyBazModel';
import MyEntityIdModel from './MyEntityIdModel';
import MyEntity from './MyEntity';

import {ObjectModel,StringModel,NumberModel,ArrayModel,BooleanModel,Required,ModelValue,_getPropertyModel} from '@hilla/form';

import {Email,Null,NotNull,NotEmpty,NotBlank,AssertTrue,AssertFalse,Negative,NegativeOrZero,Positive,PositiveOrZero,Size,Past,Future,Digits,Min,Max,Pattern,DecimalMin,DecimalMax} from '@hilla/form';

/**
 * This module is generated from dev.hilla.generator.tsmodel.TsFormEndpoint.MyEntity.
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @see {@link file:///.../endpoint/src/test/java/dev/hilla/generator/tsmodel/TsFormEndpoint.java}
 */
export default class MyEntityModel<T extends MyEntity = MyEntity> extends MyEntityIdModel<T> {
  static createEmptyValue: () => MyEntity;

  get myId(): NumberModel {
    return this[_getPropertyModel]('myId', NumberModel, [true]);
  }

  get foo(): StringModel {
    return this[_getPropertyModel]('foo', StringModel, [true]);
  }

  get bar(): MyBazModel {
    return this[_getPropertyModel]('bar', MyBazModel, [true]);
  }

  get baz(): ArrayModel<ModelValue<MyBazModel>, MyBazModel> {
    return this[_getPropertyModel]('baz', ArrayModel, [true, MyBazModel, [true]]);
  }

  get boolWrapper(): BooleanModel {
    return this[_getPropertyModel]('boolWrapper', BooleanModel, [true]);
  }

  get bool(): BooleanModel {
    return this[_getPropertyModel]('bool', BooleanModel, [false]);
  }

  get list(): ArrayModel<string, StringModel> {
    return this[_getPropertyModel]('list', ArrayModel, [true, StringModel, [true], new NotEmpty()]);
  }

  get email(): StringModel {
    return this[_getPropertyModel]('email', StringModel, [true, new Email({message:"foo"})]);
  }

  get isNull(): StringModel {
    return this[_getPropertyModel]('isNull', StringModel, [true, new Null()]);
  }

  get notNull(): StringModel {
    return this[_getPropertyModel]('notNull', StringModel, [true, new NotNull()]);
  }

  get notEmpty(): StringModel {
    return this[_getPropertyModel]('notEmpty', StringModel, [true, new NotEmpty(), new NotNull()]);
  }

  get notNullEntity(): MyEntityModel {
    return this[_getPropertyModel]('notNullEntity', MyEntityModel, [true, new NotNull()]);
  }

  get notBlank(): StringModel {
    return this[_getPropertyModel]('notBlank', StringModel, [true, new NotBlank()]);
  }

  get assertTrue(): StringModel {
    return this[_getPropertyModel]('assertTrue', StringModel, [true, new AssertTrue()]);
  }

  get assertFalse(): StringModel {
    return this[_getPropertyModel]('assertFalse', StringModel, [true, new AssertFalse()]);
  }

  get min(): NumberModel {
    return this[_getPropertyModel]('min', NumberModel, [true, new Min({value:1, message:"foo"})]);
  }

  get max(): NumberModel {
    return this[_getPropertyModel]('max', NumberModel, [true, new Max(2)]);
  }

  get decimalMin(): NumberModel {
    return this[_getPropertyModel]('decimalMin', NumberModel, [false, new DecimalMin("0.01")]);
  }

  get decimalMax(): NumberModel {
    return this[_getPropertyModel]('decimalMax', NumberModel, [false, new DecimalMax({value:"0.01", inclusive:false})]);
  }

  get negative(): NumberModel {
    return this[_getPropertyModel]('negative', NumberModel, [false, new Negative()]);
  }

  get negativeOrZero(): NumberModel {
    return this[_getPropertyModel]('negativeOrZero', NumberModel, [false, new NegativeOrZero()]);
  }

  get positive(): NumberModel {
    return this[_getPropertyModel]('positive', NumberModel, [false, new Positive()]);
  }

  get positiveOrZero(): NumberModel {
    return this[_getPropertyModel]('positiveOrZero', NumberModel, [false, new PositiveOrZero()]);
  }

  get size(): StringModel {
    return this[_getPropertyModel]('size', StringModel, [true, new Size()]);
  }

  get size1(): StringModel {
    return this[_getPropertyModel]('size1', StringModel, [true, new Size({min:1})]);
  }

  get digits(): StringModel {
    return this[_getPropertyModel]('digits', StringModel, [true, new Digits({integer:5, fraction:2})]);
  }

  get past(): StringModel {
    return this[_getPropertyModel]('past', StringModel, [true, new Past()]);
  }

  get future(): StringModel {
    return this[_getPropertyModel]('future', StringModel, [true, new Future()]);
  }

  get localTime(): StringModel {
    return this[_getPropertyModel]('localTime', StringModel, [true]);
  }

  get pattern(): StringModel {
    return this[_getPropertyModel]('pattern', StringModel, [true, new Pattern({regexp:"\\d+\\..+"})]);
  }

  get children(): ArrayModel<ModelValue<MyEntityModel>, MyEntityModel> {
    return this[_getPropertyModel]('children', ArrayModel, [true, MyEntityModel, [true]]);
  }

  get stringArray(): ArrayModel<string, StringModel> {
    return this[_getPropertyModel]('stringArray', ArrayModel, [true, StringModel, [true]]);
  }

  get numberMatrix(): ArrayModel<ModelValue<ArrayModel<number, NumberModel>>, ArrayModel<number, NumberModel>> {
    return this[_getPropertyModel]('numberMatrix', ArrayModel, [true, ArrayModel, [true, NumberModel, [true]]]);
  }

  get entityMatrix(): ArrayModel<ModelValue<ArrayModel<ModelValue<MyEntityModel>, MyEntityModel>>, ArrayModel<ModelValue<MyEntityModel>, MyEntityModel>> {
    return this[_getPropertyModel]('entityMatrix', ArrayModel, [true, ArrayModel, [true, MyEntityModel, [true]]]);
  }

  get stringMap(): ObjectModel<Record<string, string>> {
    return this[_getPropertyModel]('stringMap', ObjectModel, [true]);
  }

  get entityMap(): ObjectModel<Record<string, ModelValue<MyBazModel>>> {
    return this[_getPropertyModel]('entityMap', ObjectModel, [true]);
  }

  get optionalString(): StringModel {
    return this[_getPropertyModel]('optionalString', StringModel, [true]);
  }

  get optionalEntity(): MyEntityModel {
    return this[_getPropertyModel]('optionalEntity', MyEntityModel, [true]);
  }

  get optionalList(): ArrayModel<string, StringModel> {
    return this[_getPropertyModel]('optionalList', ArrayModel, [true, StringModel, [true]]);
  }

  get optionalMatrix(): ArrayModel<ModelValue<ArrayModel<string, StringModel>>, ArrayModel<string, StringModel>> {
    return this[_getPropertyModel]('optionalMatrix', ArrayModel, [true, ArrayModel, [true, StringModel, [true]]]);
  }

  get nonNullableString(): StringModel {
    return this[_getPropertyModel]('nonNullableString', StringModel, [false]);
  }

  get nonNullableList(): ArrayModel<string, StringModel> {
    return this[_getPropertyModel]('nonNullableList', ArrayModel, [false, StringModel, [true]]);
  }

  get nonNullableMatrix(): ArrayModel<ModelValue<ArrayModel<string, StringModel>>, ArrayModel<string, StringModel>> {
    return this[_getPropertyModel]('nonNullableMatrix', ArrayModel, [false, ArrayModel, [true, StringModel, [true]]]);
  }

  get mapWithList(): ObjectModel<Record<string, ModelValue<ArrayModel<string, StringModel>>>> {
    return this[_getPropertyModel]('mapWithList', ObjectModel, [true]);
  }

  get complexMap(): ObjectModel<Record<string, ModelValue<ObjectModel<Record<string, ModelValue<ArrayModel<ModelValue<MyEntityModel>, MyEntityModel>>>>>>> {
    return this[_getPropertyModel]('complexMap', ObjectModel, [true]);
  }

  get nestedArrays(): ArrayModel<ModelValue<ArrayModel<ModelValue<ObjectModel<Record<string, ModelValue<ArrayModel<string, StringModel>>>>>, ObjectModel<Record<string, ModelValue<ArrayModel<string, StringModel>>>>>>, ArrayModel<ModelValue<ObjectModel<Record<string, ModelValue<ArrayModel<string, StringModel>>>>>, ObjectModel<Record<string, ModelValue<ArrayModel<string, StringModel>>>>>> {
    return this[_getPropertyModel]('nestedArrays', ArrayModel, [true, ArrayModel, [true, ObjectModel, [true]]]);
  }
}

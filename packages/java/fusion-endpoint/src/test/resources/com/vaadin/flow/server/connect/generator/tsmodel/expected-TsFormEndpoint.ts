// @ts-nocheck

import MyBazModel from './MyBazModel';
import MyEntityIdModel from './MyEntityIdModel';
import MyEntity from './MyEntity';

import {ObjectModel,StringModel,NumberModel,ArrayModel,BooleanModel,Required,ModelType,_getPropertyModel} from '@vaadin/form';

import {Email,Null,NotNull,NotEmpty,NotBlank,AssertTrue,AssertFalse,Negative,NegativeOrZero,Positive,PositiveOrZero,Size,Past,Future,Digits,Min,Max,Pattern,DecimalMin,DecimalMax} from '@vaadin/form';

/**
 * This module is generated from com.vaadin.flow.server.connect.generator.tsmodel.TsFormEndpoint.MyEntity.
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @see {@link file:///.../fusion-endpoint/src/test/java/com/vaadin/flow/server/connect/generator/tsmodel/TsFormEndpoint.java}
 */
export default class MyEntityModel<T extends MyEntity = MyEntity> extends MyEntityIdModel<T> {
  static createEmptyValue: () => MyEntity;

  get assertFalse(): StringModel {
    return this[_getPropertyModel]('assertFalse', StringModel, [true, new AssertFalse()]);
  }

  get assertTrue(): StringModel {
    return this[_getPropertyModel]('assertTrue', StringModel, [true, new AssertTrue()]);
  }

  get bar(): MyBazModel {
    return this[_getPropertyModel]('bar', MyBazModel, [true]);
  }

  get baz(): ArrayModel<ModelType<MyBazModel>, MyBazModel> {
    return this[_getPropertyModel]('baz', ArrayModel, [true, MyBazModel, [false]]);
  }

  get bool(): BooleanModel {
    return this[_getPropertyModel]('bool', BooleanModel, [true]);
  }

  get children(): ArrayModel<ModelType<MyEntityModel>, MyEntityModel> {
    return this[_getPropertyModel]('children', ArrayModel, [true, MyEntityModel, [false]]);
  }

  get decimalMax(): NumberModel {
    return this[_getPropertyModel]('decimalMax', NumberModel, [true, new DecimalMax({value:"0.01", inclusive:false})]);
  }

  get decimalMin(): NumberModel {
    return this[_getPropertyModel]('decimalMin', NumberModel, [true, new DecimalMin("0.01")]);
  }

  get digits(): StringModel {
    return this[_getPropertyModel]('digits', StringModel, [true, new Digits({integer:5, fraction:2})]);
  }

  get email(): StringModel {
    return this[_getPropertyModel]('email', StringModel, [true, new Email({message:"foo"})]);
  }

  get entityMap(): ObjectModel<{ [key: string]: ModelType<MyBazModel>; }> {
    return this[_getPropertyModel]('entityMap', ObjectModel, [true]);
  }

  get entityMatrix(): ArrayModel<ModelType<ArrayModel<ModelType<MyEntityModel>, MyEntityModel>>, ArrayModel<ModelType<MyEntityModel>, MyEntityModel>> {
    return this[_getPropertyModel]('entityMatrix', ArrayModel, [true, ArrayModel, [false, MyEntityModel, [false]]]);
  }

  get foo(): StringModel {
    return this[_getPropertyModel]('foo', StringModel, [true]);
  }

  get future(): StringModel {
    return this[_getPropertyModel]('future', StringModel, [true, new Future()]);
  }

  get isNull(): StringModel {
    return this[_getPropertyModel]('isNull', StringModel, [true, new Null()]);
  }

  get list(): ArrayModel<string, StringModel> {
    return this[_getPropertyModel]('list', ArrayModel, [true, StringModel, [false], new NotEmpty()]);
  }

  get localTime(): StringModel {
    return this[_getPropertyModel]('localTime', StringModel, [true]);
  }

  get max(): NumberModel {
    return this[_getPropertyModel]('max', NumberModel, [true, new Max(2)]);
  }

  get min(): NumberModel {
    return this[_getPropertyModel]('min', NumberModel, [true, new Min({value:1, message:"foo"})]);
  }

  get myId(): NumberModel {
    return this[_getPropertyModel]('myId', NumberModel, [true]);
  }

  get negative(): NumberModel {
    return this[_getPropertyModel]('negative', NumberModel, [true, new Negative()]);
  }

  get negativeOrCero(): NumberModel {
    return this[_getPropertyModel]('negativeOrCero', NumberModel, [true, new NegativeOrZero()]);
  }

  get nonNullableList(): ArrayModel<string, StringModel> {
    return this[_getPropertyModel]('nonNullableList', ArrayModel, [false, StringModel, [true]]);
  }

  get nonNullableMatrix(): ArrayModel<ModelType<ArrayModel<string, StringModel>>, ArrayModel<string, StringModel>> {
    return this[_getPropertyModel]('nonNullableMatrix', ArrayModel, [false, ArrayModel, [false, StringModel, [true]]]);
  }

  get nonNullableString(): StringModel {
    return this[_getPropertyModel]('nonNullableString', StringModel, [false]);
  }

  get notBlank(): StringModel {
    return this[_getPropertyModel]('notBlank', StringModel, [true, new NotBlank()]);
  }

  get notEmpty(): StringModel {
    return this[_getPropertyModel]('notEmpty', StringModel, [true, new NotEmpty(), new NotNull()]);
  }

  get notNull(): StringModel {
    return this[_getPropertyModel]('notNull', StringModel, [true, new NotNull()]);
  }

  get notNullEntity(): MyEntityModel {
    return this[_getPropertyModel]('notNullEntity', MyEntityModel, [true, new NotNull()]);
  }

  get numberMatrix(): ArrayModel<ModelType<ArrayModel<number, NumberModel>>, ArrayModel<number, NumberModel>> {
    return this[_getPropertyModel]('numberMatrix', ArrayModel, [true, ArrayModel, [false, NumberModel, [false]]]);
  }

  get optionalEntity(): MyEntityModel {
    return this[_getPropertyModel]('optionalEntity', MyEntityModel, [true]);
  }

  get optionalList(): ArrayModel<string, StringModel> {
    return this[_getPropertyModel]('optionalList', ArrayModel, [true, StringModel, [true]]);
  }

  get optionalMatrix(): ArrayModel<ModelType<ArrayModel<string, StringModel>>, ArrayModel<string, StringModel>> {
    return this[_getPropertyModel]('optionalMatrix', ArrayModel, [true, ArrayModel, [false, StringModel, [true]]]);
  }

  get optionalString(): StringModel {
    return this[_getPropertyModel]('optionalString', StringModel, [true]);
  }

  get past(): StringModel {
    return this[_getPropertyModel]('past', StringModel, [true, new Past()]);
  }

  get pattern(): StringModel {
    return this[_getPropertyModel]('pattern', StringModel, [true, new Pattern({regexp:"\\d+\\..+"})]);
  }

  get positive(): NumberModel {
    return this[_getPropertyModel]('positive', NumberModel, [true, new Positive()]);
  }

  get positiveOrCero(): NumberModel {
    return this[_getPropertyModel]('positiveOrCero', NumberModel, [true, new PositiveOrZero()]);
  }

  get size(): StringModel {
    return this[_getPropertyModel]('size', StringModel, [true, new Size()]);
  }

  get size1(): StringModel {
    return this[_getPropertyModel]('size1', StringModel, [true, new Size({min:1})]);
  }

  get stringArray(): ArrayModel<string, StringModel> {
    return this[_getPropertyModel]('stringArray', ArrayModel, [true, StringModel, [false]]);
  }

  get stringMap(): ObjectModel<{ [key: string]: string; }> {
    return this[_getPropertyModel]('stringMap', ObjectModel, [true]);
  }
}

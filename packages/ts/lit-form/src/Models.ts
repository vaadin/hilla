import type { $itemModel, ArrayModel, Model, OptionalModel } from '@vaadin/hilla-models';

export type Value<M> = M extends Model<infer T> ? T : never;

export type ArrayItemModel<M extends Model> = M extends ArrayModel | OptionalModel<ArrayModel>
  ? M[typeof $itemModel]
  : never;

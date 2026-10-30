import type { $itemModel, ArrayModel, Model } from '@vaadin/hilla-models';

export type Value<M> = M extends Model<infer T> ? T : never;

export type ArrayItemModel<M extends Model> = M extends ArrayModel ? M[typeof $itemModel] : never;

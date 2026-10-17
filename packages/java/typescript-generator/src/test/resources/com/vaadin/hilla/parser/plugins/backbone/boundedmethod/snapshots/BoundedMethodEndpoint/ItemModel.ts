import m, { StringModel } from '@vaadin/hilla-models';
import type Item from './Item.js';

const ItemModel = m
  .object<Item>('Item')
  .property('name', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
  .build();
type ItemModel = typeof ItemModel;

export default ItemModel;

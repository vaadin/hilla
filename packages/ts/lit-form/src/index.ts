import { _items, type ArrayItemModel, type ArrayModel } from './Models.js';

export * from './Binder.js';
export * from './BinderRoot.js';
export * from './BinderNode.js';
export * from './Field.js';
export * from './Models.js';
export * from './Validation.js';
export * from './Validators.js';
export * from './Validity.js';

const m = {
  /**
   * Returns an iterator over item models in the array model.
   *
   * @param model - The array model to iterate over.
   * @returns An iterator over item models.
   */
  items<M extends ArrayModel>(model: M): Generator<ArrayItemModel<M>, void, void> {
    return model[_items]() as Generator<ArrayItemModel<M>, void, void>;
  },
};

export default m;

// @ts-expect-error: esbuild injection
// eslint-disable-next-line @typescript-eslint/no-unsafe-call
__REGISTER__();

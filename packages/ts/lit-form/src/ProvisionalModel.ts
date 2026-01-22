import type { Model } from '@vaadin/hilla-models';

import type { AbstractModel, DetachedModelConstructor } from './Models.js';

/**
 * Temporary composite type to support both {@link AbstractModel}
 * and {@link Model} during the transition phase.
 *
 * @deprecated use {@link Model} instead
 */
export type ProvisionalModel<T = unknown> = AbstractModel<T> | Model<T>;

/**
 * Temporary composite type to support both {@link AbstractModel}
 * and {@link Model} constructor-type arguments during the transition phase.
 *
 * @deprecated use {@link Model} instead
 */
export type ProvisionalModelConstructor<M extends ProvisionalModel> = M extends AbstractModel
  ? DetachedModelConstructor<M>
  : M extends Model
    ? M
    : never;

import type { Model } from '@vaadin/hilla-models';

import type { AbstractModel, DetachedModelConstructor } from './Models.js';

/**
 * Temporary composite type to support both {@link AbstractModel}
 * and {@link Model} during the transition phase.
 *
 * @deprecated use {@link Model} from `@vaadin/hilla-models` instead
 */
export type ProvisionalModel<T = unknown> = AbstractModel<T> | Model<T>;

/**
 * Temporary composite type to support both {@link AbstractModel}
 * and {@link Model} constructor-type arguments during the transition phase.
 *
 * @deprecated pass a {@link Model} from `@vaadin/hilla-models` instead; the
 * model itself takes the place of the constructor
 */
export type ProvisionalModelConstructor<M extends ProvisionalModel> = M extends AbstractModel
  ? DetachedModelConstructor<M>
  : M extends Model
    ? M
    : never;

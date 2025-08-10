import type { Model } from '@vaadin/hilla-models';

import type { AbstractModel } from './Models.js';

/**
 * Temporary composite type to support both {@link AbstractModel}
 * and {@link Model} during the transition phase.
 *
 * @deprecated use {@link Model} instead
 */
export type ProvisionalModel<T = unknown> = (AbstractModel<T> | Model<T>) & {};

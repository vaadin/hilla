// TODO: Fix dependency cycle

import type { Binder } from './Binder.js';
import type { BinderNode } from './BinderNode.js';
// eslint-disable-next-line import/no-cycle
import { AbstractModel, getBinderNode, NumberModel } from './Models.js';
// eslint-disable-next-line import/no-cycle
import { Required } from './Validators.js';

export interface ValueError<T> {
  property: string | AbstractModel<any>;
  message: string;
  value: T;
  validator: Validator<T>;
}

export interface ValidationResult {
  property: string | AbstractModel<any>;
  message?: string;
}

export class ValidationError extends Error {
  public constructor(public errors: ReadonlyArray<ValueError<any>>) {
    super(
      [
        'There are validation errors in the form.',
        ...errors.map((e) => `${e.property} - ${e.validator.constructor.name}${e.message ? `: ${e.message}` : ''}`),
      ].join('\n - '),
    );
    this.name = this.constructor.name;
  }
}

export type ValidationCallback<T> = (
  value: T,
  binder: Binder<any, AbstractModel<T>>,
) =>
  | boolean
  | ValidationResult
  | ReadonlyArray<ValidationResult>
  | Promise<boolean | ValidationResult | ReadonlyArray<ValidationResult>>;

export type InterpolateMessageCallback<T> = (
  message: string,
  validator: Validator<T>,
  binderNode: BinderNode<T, AbstractModel<T>>,
) => string;

export interface Validator<T> {
  validate: ValidationCallback<T>;
  message: string;
  impliesRequired?: boolean;
}

export class ServerValidator implements Validator<any> {
  public message: string;

  public constructor(message: string) {
    this.message = message;
  }

  public validate = () => false;
}

export async function runValidator<T>(
  model: AbstractModel<T>,
  validator: Validator<T>,
  interpolateMessageCallback?: InterpolateMessageCallback<T>,
): Promise<ReadonlyArray<ValueError<T>>> {
  const binderNode = getBinderNode(model);
  const { value } = binderNode;

  const interpolateMessage = (message: string) => {
    if (!interpolateMessageCallback) {
      return message;
    }
    return interpolateMessageCallback(message, validator, binderNode);
  };

  // If model is not required and value empty, do not run any validator. Except
  // always validate NumberModel, which has a mandatory builtin validator
  // to indicate NaN input.
  if (!getBinderNode(model).required && !new Required().validate(value!) && !(model instanceof NumberModel)) {
    return [];
  }
  return (async () => validator.validate(value!, getBinderNode(model).binder))().then((result) => {
    if (result === false) {
      return [
        { property: getBinderNode(model).name, value, validator, message: interpolateMessage(validator.message) },
      ];
    }
    if (result === true || (Array.isArray(result) && result.length === 0)) {
      return [];
    }
    if (Array.isArray(result)) {
      return result.map((result2) => ({
        message: interpolateMessage(validator.message),
        ...result2,
        value,
        validator,
      }));
    }
    return [{ message: interpolateMessage(validator.message), ...result, value, validator }];
  });
}

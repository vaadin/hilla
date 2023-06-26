// TODO: Fix dependency cycle

// eslint-disable-next-line import/no-cycle

import type { Binder } from './Binder.js';
import type { BinderNode } from './BinderNode.js';
import { type AbstractModel, NumberModel, getBinderNode } from './Models.js';
// eslint-disable-next-line import/no-cycle
import { Required } from './Validators.js';

export interface ValueError<T> {
  property: AbstractModel<any> | string;
  message: string;
  value: T;
  validator: Validator<T>;
}

export interface ValidationResult {
  property: AbstractModel<any> | string;
  message?: string;
}

export class ValidationError extends Error {
  constructor(public errors: ReadonlyArray<ValueError<any>>) {
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
  | Promise<ValidationResult | boolean | readonly ValidationResult[]>
  | ValidationResult
  | boolean
  | readonly ValidationResult[];

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
  message: string;

  constructor(message: string) {
    this.message = message;
  }

  validate = () => false;
}

// The `property` field of `ValidationResult`s is a path relative to the parent.
function setPropertyAbsolutePath<T>(binderNodeName: string, result: ValidationResult): ValidationResult {
  if (typeof result.property === 'string' && binderNodeName.length > 0) {
    result.property = `${binderNodeName}.${result.property}`;
  }
  return result;
}

export async function runValidator<T>(
  model: AbstractModel<T>,
  validator: Validator<T>,
  interpolateMessageCallback?: InterpolateMessageCallback<T>,
): Promise<ReadonlyArray<ValueError<T>>> {
  const binderNode = getBinderNode(model);
  const value = binderNode.value!;

  const interpolateMessage = (message: string) => {
    if (!interpolateMessageCallback) {
      return message;
    }
    return interpolateMessageCallback(message, validator, binderNode);
  };

  // If model is not required and value empty, do not run any validator. Except
  // always validate NumberModel, which has a mandatory builtin validator
  // to indicate NaN input.
  if (!binderNode.required && !new Required().validate(value) && !(model instanceof NumberModel)) {
    return [];
  }
  return (async () => validator.validate(value, binderNode.binder))().then((result) => {
    if (result === false) {
      return [{ property: binderNode.name, value, validator, message: interpolateMessage(validator.message) }];
    }
    if (result === true || (Array.isArray(result) && result.length === 0)) {
      return [];
    }
    if (Array.isArray(result)) {
      return result.map((result2) => ({
        message: interpolateMessage(validator.message),
        ...setPropertyAbsolutePath(binderNode.name, result2),
        value,
        validator,
      }));
    }
    return [
      {
        message: interpolateMessage(validator.message),
        ...setPropertyAbsolutePath(binderNode.name, result as ValidationResult),
        value,
        validator,
      },
    ];
  });
}

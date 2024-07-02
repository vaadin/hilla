import { $defaultValue, $key, $owner, type AttachTarget, Model, nothing } from './model.js';

function getRawValue<T>(model: Model<T>): T | typeof nothing {
  // TODO: Remove the error suppression when TypeScript 5.5 is released
  // @ts-expect-error: https://github.com/microsoft/TypeScript/issues/56536
  // (fixed in upcoming TS 5.5)
  if (model[$owner] instanceof Model) {
    // If the current model is a property of another model, the owner is
    // definitely an object. So we just return the part of the value of
    // the owner.
    const parentValue = getRawValue(model[$owner] as Model<Record<keyof any, T>>);
    return parentValue === nothing ? nothing : parentValue[model[$key]];
  }

  // Otherwise, the owner is an AttachTarget, so we can return the full
  // value.
  return (model[$owner] as AttachTarget<T>).value;
}

export function getValue<T>(model: Model<T>): T {
  const value = getRawValue(model);

  // If the value is `nothing`, we return the default value of the model.
  return value === nothing ? model[$defaultValue] : value;
}

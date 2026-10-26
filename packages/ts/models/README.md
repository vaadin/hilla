# Hilla Models

[![Latest Stable Version](https://img.shields.io/npm/v/@vaadin/hilla-models.svg)](https://www.npmjs.com/package/@vaadin/hilla-models)

Models that describe the shape of a value at run time: its properties, their
types, default values, validation constraints and Java metadata. Hilla
generates a model for every Java bean and enum an endpoint uses, and the form
binder, Auto CRUD and other tools read them for data binding and validation.

A part of the Hilla project.

## Installation

```bash
$ npm install @vaadin/hilla-models
```

## Usage

Generated models are built with the `m` namespace, the default export:

```ts
import m, { $defaultValue, $constraints, NotBlank, NumberModel, StringModel } from '@vaadin/hilla-models';

const PersonModel = m
  .object<Person>('Person')
  .property('name', m.constrained(StringModel, NotBlank()))
  .property('age', m.optional(NumberModel))
  .build();

PersonModel[$defaultValue]; // { name: '', age: undefined }
PersonModel.name[$constraints]; // [NotBlank()]
```

- `m.object`, `m.extend`, `m.enum`, `m.literal` and `m.union` create models;
  `m.optional`, `m.array`, `m.record`, `m.constrained` and `m.meta` derive new
  ones; `m.self` and `m.lazy` refer to models that are not built yet.
- `m.attach` attaches a model to a value holder, `m.getValue` reads the value at
  a model, and `m.items` gives the models of the items of an array.
- The reflection data is kept under symbols: `$defaultValue`, `$optional`,
  `$constraints`, `$meta`, `$itemModel` and others.
- The constraints, such as `NotBlank`, `Size` and `Pattern`, mirror the Jakarta
  Bean Validation annotations.

## Contribution

Read the [contributing guide](https://vaadin.com/docs/latest/contributing-docs/overview) to learn about our development process, how to propose bugfixes and improvements, and how to test your changes.

## License

Apache License 2.0

Vaadin collects development time usage statistics to improve this product.
For details and to opt-out, see https://github.com/vaadin/vaadin-usage-statistics.

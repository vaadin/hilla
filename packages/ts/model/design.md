I would like to suggest the new design for Hilla models that replaces TypeScript class based definitions with objects defined using a builder API.

## Motivation

Hilla models were originally introduced to support form binding use cases. But recently we started to add more high-level data-oriented frontend developer productivity features to Hilla, such as `AutoGrid` and the upcoming `AutoForm`/`AutoCrud`, where we also take the data structure and metadata from the models. Some limitations of the current class-based models design became apparent:

- Accessing object properties is hard. It is expected that the described properties are easily available: for example, `NamedModel.name` references the `name` property of a `NamedModel`, and `Object.keys(SomeModel)` could iterate the keys. The model classes do not meet this expectation, and require an extra step for these use cases: either instantiation or a prototype access of some sort.
- The aforementioned model instantiation is hard. The model class constructor historically requires a value container in a form of either a form binder or the “parent” container model.
- Creating models manually is hard. Ideally an object model should use getters with a lazy initialization, and the generated models do use getters implemented with a helper.

## Usage

Hilla models are similar in structure with TypeScript interfaces. They both describe object properties, but the models are retained in runtime, have extra metadata, and could be used as a reference to some value of the described type.

Let me go through the typical use cases.

### Simple Properties


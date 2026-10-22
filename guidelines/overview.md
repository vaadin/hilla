# Hilla Guidelines

These guidelines describe how features in Hilla — the framework that connects a
Spring Boot Java backend to a TypeScript frontend through generated, type-safe
endpoint clients — should be designed and implemented in the `hilla`
repository. Chapters can be read selectively for the topics your work touches.

Treat these as guidelines, not hard rules. They are best practices that should
be followed by default, but can be deviated from when necessary to make
something work.

For repository-level commands (build, test, format) see
[`.claude/CLAUDE.md`](../.claude/CLAUDE.md). For how the TypeScript generator
works and how its snapshot tests are written, see the
[generator README](../packages/java/typescript-generator/README.md).

## Chapters

| Chapter                         | Topic                                                                                   |
| ------------------------------- | --------------------------------------------------------------------------------------- |
| [Repository](repository.md)     | Tech stack, Java and TypeScript package layout, test applications, code style.          |
| [Architecture](architecture.md) | Endpoint calls, code generation, nullability, Jackson, push and signals, hotswap.       |
| [Design](design.md)             | Shape of new public API: the generated TypeScript, Java API, TypeScript package API.    |
| [Documenting](documenting.md)   | Javadoc and TSDoc expectations.                                                         |
| [Testing](testing.md)           | Java unit tests, generator snapshots, TypeScript tests, integration tests, debugging.   |

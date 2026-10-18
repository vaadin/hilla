# Testing

## Where tests live

**Java unit tests** live in `src/test/java/` of each Java module. They use JUnit
and Mockito and focus on the behavior of individual classes. Both JUnit 4 and
JUnit 5 are in use; follow the style of the module and the test class you are
extending.

**Generator tests** live in `packages/java/typescript-generator/src/test/`.
They compare the generated TypeScript of a set of browser callable classes
against snapshots; a new case is a package with an endpoint in it, with no test
class. See the [generator README](../packages/java/typescript-generator/README.md#tests)
for how cases are discovered and how snapshots are updated with
`-Dhilla.test.updateSnapshots`.

**TypeScript tests** live in the `test/` folder of each package in
`packages/ts/`. They run with Vitest (`npm test` in the package, or at the
root for all packages); React components are tested with Testing Library.

**Integration tests** live in the test applications under
`packages/java/tests/spring/` (and `packages/java/tests/gradle/` for the Gradle
plugin). They are `*IT` classes using TestBench (`ChromeBrowserTest`), run by
Maven Failsafe against the application, which the build starts and stops.

## Writing tests

- **Write the tests that should pass first.** If they expose problems in
  the implementation, fix the implementation afterwards — don't rewrite
  the tests to match a broken implementation.
- Keep the test count minimal — only the essential cases. More tests are
  not better; focused tests are.
- Assert the observable outcome — the generated TypeScript, the HTTP
  response, the rendered DOM, the public API result — not how it is
  produced. A test must fail when the behavior breaks and keep passing
  through a refactor that preserves it.
- Test the code under change, not framework behavior that is already
  covered elsewhere.
- When changing an existing test's setup — swapping a mock, a fixture or
  a helper — keep every case it already covered.
- Don't depend on JVM or machine defaults (locale, time zone, charset).
  Set them explicitly, or choose a value that differs from the default, so
  the test cannot pass without exercising the code.
- For a change to the generated TypeScript, a generator snapshot case is
  the test. Prefer adding a case over a hand-written assertion on the
  output.
- Only add an IT when the behavior needs a browser and a running server.
  Logic of a TypeScript package belongs in its Vitest tests; the endpoint
  runtime can usually be tested with unit tests of `EndpointController` and
  `EndpointInvoker`.
- Before adding a new IT view, endpoint or test class, look for an existing
  one in the test application covering the same area and extend it.
- ITs assert concrete outputs, not just "not null" — assert the content of
  what the endpoint returned or the view rendered.
- When improving existing tests, verify actual behavior rather than just
  "not null", and cover the edge cases the change actually introduces.

## Debugging failures

- Analyze *why* a test fails, code does not compile, or a build breaks, before
  changing anything. Do not start rewriting code.
- When a snapshot test fails, read the diff between the snapshot and the
  generated file first: it says what the generator now writes differently.
- When an integration test fails, look at what the browser is actually doing —
  run it with a visible browser or use Playwright — rather than guessing at
  the cause.

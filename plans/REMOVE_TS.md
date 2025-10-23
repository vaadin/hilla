# Plan: Eliminate TypeScript Generator - Move Generation to Java

## Overview
Currently, Hilla has a two-step code generation process:
1. **Java → OpenAPI**: Parser (now `typescript-generator` module) analyzes Java and generates OpenAPI JSON
2. **OpenAPI → TypeScript**: Node.js TypeScript generator (`@vaadin/hilla-generator-cli`) processes OpenAPI and generates TypeScript files

**Goal**: Eliminate step 2 by implementing TypeScript generation directly in Java within the `typescript-generator` module.

## Current Architecture Analysis

### What Needs to be Replaced:
**TypeScript packages to eliminate:**
- `generator-cli` - CLI tool that invokes generation
- `generator-core` - Core orchestrator and plugin manager
- `generator-plugin-backbone` - Base TS structure generation
- `generator-plugin-model` - TypeScript interfaces from schemas
- `generator-plugin-client` - Endpoint client methods
- `generator-plugin-barrel` - Index/barrel files (endpoints.ts)
- `generator-plugin-push` - Flux/server-push support
- `generator-plugin-signals` - React Signals integration
- `generator-plugin-subtypes` - Polymorphic type guards
- `generator-plugin-transfertypes` - Transfer type utilities
- `generator-utils` - Shared utilities

### What Stays (Runtime Dependencies):
- `frontend` - Client-side runtime (Connect client, auth, etc.)
- `react-*` packages - React hooks and components
- `lit-form` - Lit form handling
- `file-router` - File-based routing

## Implementation Plan

### Phase 1: Create TypeScript Generation Infrastructure in Java (1-2 weeks)

**1.1 Create Simple Template Utilities**
- Add `com.vaadin.hilla.typescript.codegen` package
- Simple utility classes: `TypeScriptWriter`, `IndentationHelper`
- Use Java 17+ text blocks (""") for TypeScript templates
- String interpolation/formatting for dynamic values
- No AST needed - direct string generation

**Example approach:**
```java
String generateInterface(String name, List<Property> properties) {
    String propertiesCode = properties.stream()
        .map(p -> "  %s: %s;".formatted(p.name(), p.type()))
        .collect(Collectors.joining("\n"));

    return """
        export interface Person {
          name: string;
          age: number;
        }
        """
        .replace("Person", name)
        .replace("  name: string;\n  age: number;", propertiesCode);
}
```

This approach uses a complete, valid TypeScript example as the template, making it easy to read and maintain. Parts like `Person` and the property list are then replaced with actual values.

**1.2 Create Generator Plugin Architecture**
- Add `TypeScriptGenerator` interface (similar to existing `Plugin`)
- Plugin manager to orchestrate multiple generators
- Context object to share state between plugins
- Output: Map<String, String> (filename → content)

**1.3 Implement Core Generator Plugins Using Templates**

**BackbonePlugin** - Base structure:
- Generate endpoint client files using text block templates
- Template for imports: `import { EndpointRequestInit } from '@vaadin/hilla-frontend';`
- Template for class structure with method stubs
- Simple string concatenation for building files

**ModelPlugin** - Type definitions:
- Templates for TypeScript interfaces
- Handle nullability: `property?: Type` vs `property: Type`
- Template for type references and imports
- Property list generation from schemas

**ClientPlugin** - Client method implementations:
- Template for Connect client method calls
- Format: `call('EndpointName', 'methodName', params, options)`
- Template includes type annotations
- Parameter serialization in template

**BarrelPlugin** - Index files:
- Simple template for `endpoints.ts`
- Template: `export * as Name from './NameEndpoint.js';`
- Loop through endpoints and accumulate exports
- Straightforward string building

**PushPlugin** - Flux support:
- Template for Flux/subscription methods
- Different template for streaming endpoints
- EventSource wrapper template

**SignalsPlugin** - Signals integration:
- Template for Signal hook generation
- React Signals specific imports template
- Hook pattern template with type safety

**SubtypesPlugin** - Polymorphic support:
- Template for type guard functions
- Format: `export function isType(obj: any): obj is Type { ... }`
- Discriminated union helper templates

**TransferTypesPlugin** - Special types:
- Template for special type imports
- Map Java types to TS imports using templates
- Simple type alias templates

### Phase 2: Refactor Engine to Use Java Generator (1 week)

**2.1 Update GeneratorProcessor**
- Remove Node.js invocation code (lines 43-68, 184-208)
- Replace with direct Java generator invocation
- Keep cleanup logic (lines 70-115)
- Remove `getTsgenPath()`, `GeneratorShellRunner` usage

**2.2 Update ParserProcessor**
- Currently generates OpenAPI JSON → modify to generate TypeScript directly
- Chain: Java source → internal model → TypeScript (skip OpenAPI serialization)
- OpenAPI can still be generated optionally for debugging

**2.3 Configuration Updates**
- Remove `nodeCommand` from `EngineAutoConfiguration`
- Update Maven/Gradle plugin configuration
- Remove TypeScript package dependencies from build

### Phase 3: Update Tests (1 week)

**3.1 Enhance Existing Tests**
- Current tests only validate OpenAPI JSON output
- Add TypeScript output validation
- Create `TypeScriptAssertions` utility class
- Compare generated TS against expected TS (structure, not formatting)

**3.2 End-to-End Tests**
- Generate TypeScript from sample Java endpoints
- Validate all plugin outputs (models, clients, barrels, etc.)
- Test edge cases: generics, nullability, inheritance, etc.

**3.3 Integration Tests**
- Test with actual Hilla applications
- Verify generated TypeScript compiles with tsc
- Ensure runtime behavior unchanged

### Phase 4: Migration & Cleanup (1 week)

**4.1 Update Documentation**
- Update CLAUDE.md architecture section
- Remove Node.js from dependency requirements
- Update build instructions

**4.2 Remove TypeScript Generator Packages**
- Delete `packages/ts/generator-*` directories
- Update `package.json` workspaces
- Remove from Nx configuration
- Update CI/CD pipelines

**4.3 Update Maven/Gradle Plugins**
- Remove `nodeExecutable` configuration option
- Simplify plugin execution (no Node.js process spawning)
- Update plugin documentation

**4.4 Version Bump & Breaking Change Notice**
- This is a major internal change
- Users shouldn't notice (generated output identical)
- But breaking for anyone extending the generator

## Benefits

✅ **No Node.js dependency** - Pure Java toolchain
✅ **Faster builds** - No process spawning, IPC overhead
✅ **Simpler architecture** - One language, one runtime
✅ **Better debugging** - Java stack traces end-to-end
✅ **Easier maintenance** - TypeScript + Java → just Java
✅ **Better IDE support** - Java refactoring works across entire pipeline

## Risks & Mitigation

⚠️ **Risk**: TypeScript generation logic is complex
✅ **Mitigation**: Start with simple cases, iterate. Existing TS generator is reference

⚠️ **Risk**: Subtle differences in generated code
✅ **Mitigation**: Extensive test coverage comparing outputs. Golden file tests.

⚠️ **Risk**: Breaking changes for users extending generator
✅ **Mitigation**: Clear migration guide. Most users don't extend generator.

## Timeline Estimate
- **Total**: 4-5 weeks for complete implementation
- **MVP** (basic generation working): 2-3 weeks
- **Production ready**: 4-5 weeks

## First Step
Start with Phase 1.1: Create TypeScript code model infrastructure (classes for representing TypeScript code structures like interfaces, types, methods, etc.)

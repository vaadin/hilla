# Plan: Eliminate TypeScript Generator & OpenAPI - Pure Java Generation

## Overview
Currently, Hilla has a two-step code generation process:
1. **Java → OpenAPI**: Parser (now `typescript-generator` module) analyzes Java and generates OpenAPI JSON
2. **OpenAPI → TypeScript**: Node.js TypeScript generator (`@vaadin/hilla-generator-cli`) processes OpenAPI and generates TypeScript files

**Goal**:
- **Eliminate OpenAPI completely** - it's an unnecessary intermediate format
- **Direct Java → TypeScript generation** using parser's internal model
- **Pure Java toolchain** - no Node.js dependency

**New Architecture**: Java source → Parser internal model → TypeScript files (no OpenAPI step)

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
- **Input**: Parser's internal model (ClassInfoModel, MethodInfoModel, etc.)
- **Output**: Map<String, String> (filename → content)
- **NO OpenAPI** - work directly with parser models

**1.3 Implement Core Generator Plugins Using Templates**

**ModelPlugin** - Type definitions:
- **Input**: ClassInfoModel from parser
- Read properties from ClassInfoModel.getFields(), ClassInfoModel.getMethods()
- Templates for TypeScript interfaces
- Handle nullability: `property?: Type` vs `property: Type`
- Template for type references and imports
- Property list generation from parser model (not OpenAPI schemas)

**ClientPlugin** - Client method implementations:
- **Input**: Endpoint classes and methods from parser model
- Read method signatures from MethodInfoModel
- Template for Connect client method calls
- Format: `call('EndpointName', 'methodName', params, options)`
- Template includes type annotations
- Parameter serialization in template
- Extract return types from MethodInfoModel.getResultType()

**BarrelPlugin** - Index files:
- **Input**: List of endpoint ClassInfoModel objects
- Simple template for `endpoints.ts`
- Template: `export * as Name from './NameEndpoint.js';`
- Loop through endpoints and accumulate exports
- Straightforward string building

**PushPlugin** - Flux support:
- **Input**: Methods returning Flux<T> from parser model
- Template for Flux/subscription methods
- Different template for streaming endpoints
- EventSource wrapper template
- Detect Flux return types from MethodInfoModel

**SignalsPlugin** - Signals integration:
- **Input**: Endpoint methods from parser model
- Template for Signal hook generation
- React Signals specific imports template
- Hook pattern template with type safety

**SubtypesPlugin** - Polymorphic support:
- **Input**: ClassInfoModel with inheritance hierarchies
- Template for type guard functions
- Format: `export function isType(obj: any): obj is Type { ... }`
- Discriminated union helper templates
- Use parser's type system to detect polymorphic types

**TransferTypesPlugin** - Special types:
- **Input**: Special Java types (Instant, LocalDate, etc.) from parser
- Template for special type imports
- Map Java types to TS imports using templates
- Simple type alias templates

### Phase 2: Refactor Parser to Generate TypeScript Directly (1-2 weeks)

**2.1 Create TypeScript Generator Plugins That Work with Parser Model**
- Refactor existing plugins to accept parser's internal model instead of OpenAPI
- Change signature: `generate(OpenAPI openAPI, ...)` → `generate(ParserOutput parserOutput, ...)`
- ParserOutput contains: List<ClassInfoModel> for endpoints, List<ClassInfoModel> for entities
- Extract all information from ClassInfoModel, MethodInfoModel, FieldInfoModel
- Map Java types to TypeScript types using parser's type system

**2.2 Integrate TypeScript Generation into Parser**
- Add new method to Parser: `generateTypeScript(List<Class<?>> endpoints, Path outputDir)`
- After building internal model, invoke TypeScript generator plugins
- Chain: Java source → Parser model → TypeScript files
- Remove OpenAPI generation from Parser (or make it optional for debugging)

**2.3 Update GeneratorProcessor in Engine**
- Remove Node.js invocation code completely
- Replace with direct Parser.generateTypeScript() call
- Remove `getTsgenPath()`, `GeneratorShellRunner` usage
- Remove OpenAPI JSON file generation
- Keep cleanup logic for old generated files

**2.4 Configuration Updates**
- Remove `nodeCommand` from `EngineAutoConfiguration`
- Update Maven/Gradle plugin configuration
- Remove TypeScript package dependencies from build
- Remove OpenAPI-related configuration options

### Phase 3: Update Tests & Remove OpenAPI (1 week)

**3.1 Migrate Tests from OpenAPI Validation to TypeScript Validation**
- Current tests validate OpenAPI JSON output - replace with TypeScript validation
- Create `TypeScriptAssertions` utility class
- Compare generated TS against expected TS (structure, not formatting)
- Remove all OpenAPI assertion code

**3.2 End-to-End Tests**
- Generate TypeScript from sample Java endpoints using parser model
- Validate all plugin outputs (models, clients, barrels, etc.)
- Test edge cases: generics, nullability, inheritance, etc.
- Ensure no OpenAPI artifacts remain

**3.3 Integration Tests**
- Test with actual Hilla applications
- Verify generated TypeScript compiles with tsc
- Ensure runtime behavior unchanged
- Confirm OpenAPI.json is no longer generated

**3.4 Remove OpenAPI Dependencies**
- Remove swagger-parser, swagger-core dependencies from POM
- Remove OpenAPI model classes if no longer needed
- Update parser to not generate OpenAPI at all

### Phase 4: Cleanup & Documentation (1 week)

**4.1 Remove TypeScript Generator Packages**
- Delete `packages/ts/generator-*` directories (generator-cli, generator-core, all plugins)
- Update `package.json` workspaces
- Remove from Nx configuration
- Update CI/CD pipelines

**4.2 Remove OpenAPI Artifacts**
- Remove OpenAPI.json generation from all code paths
- Remove OpenAPI file cleanup code (no longer needed)
- Remove OpenAPI-related configuration options from Maven/Gradle plugins
- Search codebase for any remaining OpenAPI references

**4.3 Update Documentation**
- Update CLAUDE.md architecture section to reflect new flow
- Document: Java → Parser Model → TypeScript (no OpenAPI, no Node.js)
- Remove Node.js from dependency requirements
- Update build instructions
- Add migration notes for anyone who relied on OpenAPI.json

**4.4 Update Maven/Gradle Plugins**
- Remove `nodeExecutable` configuration option
- Remove OpenAPI file path configuration
- Simplify plugin execution (no Node.js process spawning, no OpenAPI generation)
- Update plugin documentation

**4.5 Version Bump & Breaking Change Notice**
- This is a major internal change
- Generated TypeScript output should be identical
- Breaking for anyone extending the generator
- Breaking for anyone relying on OpenAPI.json output (document workarounds)

## Benefits

✅ **No Node.js dependency** - Pure Java toolchain
✅ **No OpenAPI intermediate format** - Direct Java → TypeScript
✅ **Faster builds** - No process spawning, no IPC, no JSON serialization overhead
✅ **Simpler architecture** - One language, one runtime, fewer moving parts
✅ **Better debugging** - Java stack traces end-to-end, no cross-process debugging
✅ **Easier maintenance** - TypeScript + Java + OpenAPI → just Java
✅ **Better IDE support** - Java refactoring works across entire pipeline
✅ **More type safety** - Work with typed parser models instead of OpenAPI maps

## Risks & Mitigation

⚠️ **Risk**: TypeScript generation logic is complex
✅ **Mitigation**: Start with simple cases, iterate. Existing TS generator is reference. Template-based approach is simpler than AST.

⚠️ **Risk**: Subtle differences in generated code
✅ **Mitigation**: Extensive test coverage comparing outputs. Golden file tests.

⚠️ **Risk**: Breaking changes for users extending generator
✅ **Mitigation**: Clear migration guide. Most users don't extend generator.

⚠️ **Risk**: Losing OpenAPI.json breaks external tools
✅ **Mitigation**: Survey usage, provide optional OpenAPI export if needed. Document alternatives.

⚠️ **Risk**: Parser model may be missing information that OpenAPI had
✅ **Mitigation**: Parser model is the source of truth for OpenAPI, so it has all information. May need to extract it differently.

## Timeline Estimate
- **Total**: 5-6 weeks for complete implementation
- **Phase 1** (Java TS generator infrastructure): 1-2 weeks ✅ DONE (basic plugins)
- **Phase 2** (Refactor to use parser model): 1-2 weeks
- **Phase 3** (Tests + remove OpenAPI): 1 week
- **Phase 4** (Cleanup): 1 week
- **Production ready**: 5-6 weeks

## Progress

**✅ Phase 1.1-1.3 Complete** (Partial):
- Created TypeScript generation infrastructure in Java
- Implemented 3 core plugins: ModelPlugin, ClientPlugin, BarrelPlugin
- All plugins currently use OpenAPI as input (temporary)
- Template-based generation with Java text blocks working
- 5 tests passing, no regressions

**🔄 Next Steps**:
- Implement remaining plugins (Push, Signals, Subtypes, TransferTypes)
- Refactor all plugins to use Parser model instead of OpenAPI
- Integrate into Parser class
- Remove OpenAPI completely

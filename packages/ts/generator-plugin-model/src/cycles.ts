import { isReferenceSchema, type ReferenceSchema, type Schema } from '@vaadin/hilla-generator-core/Schema.js';
import type { OpenAPIV3 } from 'openapi-types';

const SCHEMA_PREFIX = '#/components/schemas/';

/**
 * Tells which references of a schema graph have to be resolved lazily.
 *
 * Object models are built eagerly, so a property that refers to a model which
 * refers back reads an uninitialised binding of the circular import. Only the
 * references taking part in a cycle need deferring; the rest stay direct, which
 * keeps the generated code readable and its types navigable.
 */
export interface Cycles {
  /**
   * Whether a property of `owner` referring to `target` has to be deferred.
   */
  isDeferred(owner: string, target: string): boolean;

  /**
   * Whether the model of the schema takes part in a cycle with another schema.
   * The type of such a model cannot be inferred, as it would refer to itself
   * through the other one, so it needs an explicit type annotation.
   */
  isMutuallyReferencing(name: string): boolean;
}

/**
 * The name a reference schema points at, which is the key of the schema in the
 * components of the OpenAPI document.
 */
export function schemaName(schema: ReferenceSchema): string {
  return schema.$ref.substring(SCHEMA_PREFIX.length);
}

function referencedName(schema: unknown): string | undefined {
  return isReferenceSchema(schema as Schema) && (schema as OpenAPIV3.ReferenceObject).$ref.startsWith(SCHEMA_PREFIX)
    ? (schema as OpenAPIV3.ReferenceObject).$ref.substring(SCHEMA_PREFIX.length)
    : undefined;
}

/**
 * Collects every schema the given node refers to, at any depth: an array item,
 * a map value or a branch of a composed schema all count as references of the
 * schema that holds them.
 */
function collectReferences(node: unknown, found: Set<string>): void {
  if (Array.isArray(node)) {
    node.forEach((item) => collectReferences(item, found));
    return;
  }

  if (typeof node !== 'object' || node === null) {
    return;
  }

  const name = referencedName(node);

  if (name !== undefined) {
    found.add(name);
    return;
  }

  Object.values(node).forEach((value) => collectReferences(value, found));
}

export function analyzeCycles(schemas: Readonly<Record<string, Schema>>): Cycles {
  const edges = new Map<string, ReadonlySet<string>>();

  Object.entries(schemas).forEach(([name, component]) => {
    const found = new Set<string>();
    collectReferences(component, found);
    edges.set(name, found);
  });

  const reachable = new Map<string, ReadonlySet<string>>();

  function reach(from: string): ReadonlySet<string> {
    const known = reachable.get(from);

    if (known) {
      return known;
    }

    const result = new Set<string>();
    // seeded before the walk so that a cycle back to `from` terminates
    reachable.set(from, result);

    const queue = [...(edges.get(from) ?? [])];

    while (queue.length > 0) {
      const next = queue.pop()!;

      if (!result.has(next)) {
        result.add(next);
        queue.push(...(edges.get(next) ?? []));
      }
    }

    return result;
  }

  return {
    isDeferred(owner, target) {
      return owner === target || reach(target).has(owner);
    },
    isMutuallyReferencing(name) {
      // a schema referring only to itself needs no annotation: `m.self`
      // resolves against the owner model rather than the declared constant
      return [...(edges.get(name) ?? [])].some((target) => target !== name && reach(target).has(name));
    },
  };
}

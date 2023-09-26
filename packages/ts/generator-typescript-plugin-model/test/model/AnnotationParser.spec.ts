import { expect } from 'chai';
import sinon from 'sinon';
import ts, { type NewExpression } from 'typescript';
import { ValidationConstraintParser, type ValidationConstraint } from '../../src/ValidationConstraintParser.js';

type AnnotationPack = Readonly<{
  expected?: string;
  str: string;
  obj: ValidationConstraint;
}>;

function assertValidationConstraint(actual: NewExpression, expected: string): void {
  const printer = ts.createPrinter();
  let file = ts.createSourceFile('f.ts', '', ts.ScriptTarget.Latest, false, ts.ScriptKind.TS);
  file = ts.factory.updateSourceFile(file, [
    ts.factory.createVariableStatement(
      undefined,
      ts.factory.createVariableDeclarationList(
        [ts.factory.createVariableDeclaration(ts.factory.createIdentifier('a'), undefined, undefined, actual)],
        ts.NodeFlags.Const,
      ),
    ),
  ]);
  expect(printer.printFile(file).trim()).to.equal(`const a = new ${expected};`);
}

describe('ValidationConstraintParser', () => {
  let importer: sinon.SinonSpy;
  let parser: ValidationConstraintParser;

  beforeEach(() => {
    importer = sinon.fake((name: string) => ts.factory.createIdentifier(name));
    parser = new ValidationConstraintParser(importer);
  });

  const notBlank: AnnotationPack = {
    obj: {
      simpleName: 'NotBlank',
    },
    str: 'NotBlank()',
  };

  const min: AnnotationPack = {
    obj: {
      attributes: { value: 2 },
      simpleName: 'Min',
    },
    str: 'Min(2)',
  };

  const max: AnnotationPack = {
    expected: 'Max(1000)',
    obj: {
      attributes: { value: 1000 },
      simpleName: 'Max',
    },
    str: 'Max(1e3)',
  };

  const sizeSimple: AnnotationPack = {
    obj: {
      attributes: { min: 1 },
      simpleName: 'Size',
    },
    str: 'Size({ min: 1 })',
  };

  const sizeComplex: AnnotationPack = {
    obj: {
      attributes: { message: 'foo', min: 1 },
      simpleName: 'Size',
    },
    str: 'Size({ message: "foo", min: 1 })',
  };

  const decimalMin: AnnotationPack = {
    obj: {
      attributes: { value: '0.01' },
      simpleName: 'DecimalMin',
    },
    str: 'DecimalMin("0.01")',
  };

  const decimalMax: AnnotationPack = {
    obj: {
      attributes: {
        inclusive: false,
        value: '100',
      },
      simpleName: 'DecimalMax',
    },
    str: 'DecimalMax({ inclusive: false, value: "100" })',
  };

  const decimalMaxInclusive: AnnotationPack = {
    obj: {
      attributes: {
        inclusive: true,
        value: '100',
      },
      simpleName: 'DecimalMax',
    },
    str: 'DecimalMax({ inclusive: true, value: "100" })',
  };

  const digits: AnnotationPack = {
    obj: {
      attributes: { fraction: 2, integer: 5 },
      simpleName: 'Digits',
    },
    str: 'Digits({ fraction: 2, integer: 5 })',
  };

  const email: AnnotationPack = {
    obj: {
      attributes: { message: 'foo' },
      simpleName: 'Email',
    },
    str: 'Email({ message: "foo" })',
  };

  const pattern: AnnotationPack = {
    obj: {
      attributes: { regexp: '\\d+\\..+' },
      simpleName: 'Pattern',
    },
    str: String.raw`Pattern({ regexp: "\\d+\\..+" })`,
  };

  it('should parse object annotations', () => {
    assertValidationConstraint(parser.parse(notBlank.obj), notBlank.expected ?? notBlank.str);
    assertValidationConstraint(parser.parse(min.obj), min.expected ?? min.str);
    assertValidationConstraint(parser.parse(max.obj), max.expected ?? max.str);
    assertValidationConstraint(parser.parse(sizeSimple.obj), sizeSimple.expected ?? sizeSimple.str);
    assertValidationConstraint(parser.parse(sizeComplex.obj), sizeComplex.expected ?? sizeComplex.str);
    assertValidationConstraint(parser.parse(decimalMin.obj), decimalMin.expected ?? decimalMin.str);
    assertValidationConstraint(parser.parse(decimalMax.obj), decimalMax.expected ?? decimalMax.str);
    assertValidationConstraint(
      parser.parse(decimalMaxInclusive.obj),
      decimalMaxInclusive.expected ?? decimalMaxInclusive.str,
    );
    assertValidationConstraint(parser.parse(digits.obj), digits.expected ?? digits.str);
    assertValidationConstraint(parser.parse(email.obj), email.expected ?? email.str);
    assertValidationConstraint(parser.parse(pattern.obj), pattern.expected ?? pattern.str);
  });
});

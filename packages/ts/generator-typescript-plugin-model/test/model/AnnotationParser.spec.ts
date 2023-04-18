import { expect } from 'chai';
import sinon from 'sinon';
import ts, { type NewExpression } from 'typescript';
import type { Annotation } from '../../Annotation.js';
import { AnnotationParser } from '../../src/annotation.js';

type AnnotationPack = Readonly<{
  expected?: string;
  str: string;
  obj: Annotation;
}>;

function assertAnnotation(actual: NewExpression, expected: string): void {
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

describe('AnnotationParser', () => {
  let importer: sinon.SinonSpy;
  let parser: AnnotationParser;

  beforeEach(() => {
    importer = sinon.fake((name: string) => ts.factory.createIdentifier(name));
    parser = new AnnotationParser(importer);
  });

  const notBlank: AnnotationPack = {
    str: 'NotBlank()',
    obj: {
      simpleName: 'NotBlank',
    },
  };

  const min: AnnotationPack = {
    str: 'Min(2)',
    obj: {
      simpleName: 'Min',
      attributes: { value: 2 },
    },
  };

  const max: AnnotationPack = {
    expected: 'Max(1000)',
    str: 'Max(1e3)',
    obj: {
      simpleName: 'Max',
      attributes: { value: 1000 },
    },
  };

  const sizeSimple: AnnotationPack = {
    str: 'Size({ min: 1 })',
    obj: {
      simpleName: 'Size',
      attributes: { min: 1 },
    },
  };

  const sizeComplex: AnnotationPack = {
    str: 'Size({ min: 1, message: "foo" })',
    obj: {
      simpleName: 'Size',
      attributes: { min: 1, message: 'foo' },
    },
  };

  const decimalMin: AnnotationPack = {
    str: 'DecimalMin("0.01")',
    obj: {
      simpleName: 'DecimalMin',
      attributes: { value: '0.01' },
    },
  };

  const decimalMax: AnnotationPack = {
    str: 'DecimalMax({ value: "100", inclusive: false })',
    obj: {
      simpleName: 'DecimalMax',
      attributes: {
        value: '100',
        inclusive: false,
      },
    },
  };

  const decimalMaxInclusive: AnnotationPack = {
    str: 'DecimalMax({ value: "100", inclusive: true })',
    obj: {
      simpleName: 'DecimalMax',
      attributes: {
        value: '100',
        inclusive: true,
      },
    },
  };

  const digits: AnnotationPack = {
    str: 'Digits({ integer: 5, fraction: 2 })',
    obj: {
      simpleName: 'Digits',
      attributes: { integer: 5, fraction: 2 },
    },
  };

  const email: AnnotationPack = {
    str: 'Email({ message: "foo" })',
    obj: {
      simpleName: 'Email',
      attributes: { message: 'foo' },
    },
  };

  const pattern: AnnotationPack = {
    str: String.raw`Pattern({ regexp: "\\d+\\..+" })`,
    obj: {
      simpleName: 'Pattern',
      attributes: { regexp: '\\d+\\..+' },
    },
  };

  it('should parse string annotations', () => {
    assertAnnotation(parser.parse(notBlank.str), notBlank.expected ?? notBlank.str);
    assertAnnotation(parser.parse(min.str), min.expected ?? min.str);
    assertAnnotation(parser.parse(max.str), max.expected ?? max.str);
    assertAnnotation(parser.parse(sizeSimple.str), sizeSimple.expected ?? sizeSimple.str);
    assertAnnotation(parser.parse(sizeComplex.str), sizeComplex.expected ?? sizeComplex.str);
    assertAnnotation(parser.parse(decimalMin.str), decimalMin.expected ?? decimalMin.str);
    assertAnnotation(parser.parse(decimalMax.str), decimalMax.expected ?? decimalMax.str);
    assertAnnotation(parser.parse(decimalMaxInclusive.str), decimalMaxInclusive.expected ?? decimalMaxInclusive.str);
    assertAnnotation(parser.parse(digits.str), digits.expected ?? digits.str);
    assertAnnotation(parser.parse(email.str), email.expected ?? email.str);
    assertAnnotation(parser.parse(pattern.str), pattern.expected ?? pattern.str);
  });

  it('should parse object annotations', () => {
    assertAnnotation(parser.parse(notBlank.obj), notBlank.expected ?? notBlank.str);
    assertAnnotation(parser.parse(min.obj), min.expected ?? min.str);
    assertAnnotation(parser.parse(max.obj), max.expected ?? max.str);
    assertAnnotation(parser.parse(sizeSimple.obj), sizeSimple.expected ?? sizeSimple.str);
    assertAnnotation(parser.parse(sizeComplex.obj), sizeComplex.expected ?? sizeComplex.str);
    assertAnnotation(parser.parse(decimalMin.obj), decimalMin.expected ?? decimalMin.str);
    assertAnnotation(parser.parse(decimalMax.obj), decimalMax.expected ?? decimalMax.str);
    assertAnnotation(parser.parse(decimalMaxInclusive.obj), decimalMaxInclusive.expected ?? decimalMaxInclusive.str);
    assertAnnotation(parser.parse(digits.obj), digits.expected ?? digits.str);
    assertAnnotation(parser.parse(email.obj), email.expected ?? email.str);
    assertAnnotation(parser.parse(pattern.obj), pattern.expected ?? pattern.str);
  });
});

import { expect } from 'chai';
import type { Annotation, AnnotationNamedAttributes } from '../../src/Annotation.js';
import parseAnnotation from '../../src/parseAnnotation.js';

function assertAnnotation(
  actual: Annotation,
  expectedSimpleName: string,
  expectedAttributes?: AnnotationNamedAttributes,
): void {
  expect(actual.simpleName).to.equal(expectedSimpleName);
  expect(actual.attributes).to.deep.equal(expectedAttributes);
}

describe('parseAnnotation', () => {
  context('default', () => {
    it('should parse annotations', () => {
      assertAnnotation(parseAnnotation('NotBlank()'), 'NotBlank');
      assertAnnotation(parseAnnotation('Min(2)'), 'Min', { value: 2 });
      assertAnnotation(parseAnnotation('Max(1e3)'), 'Max', { value: 1000 });
      assertAnnotation(parseAnnotation('Size({min:1})'), 'Size', { min: 1 });
      assertAnnotation(parseAnnotation('Size({min:1, message:"foo"})'), 'Size', { min: 1, message: 'foo' });
      assertAnnotation(parseAnnotation('DecimalMin("0.01")'), 'DecimalMin', { value: '0.01' });
      assertAnnotation(parseAnnotation('Digits({integer:5, fraction:2})'), 'Digits', { integer: 5, fraction: 2 });
      assertAnnotation(parseAnnotation('DecimalMax({value:"100", inclusive:false})'), 'DecimalMax', {
        value: '100',
        inclusive: false,
      });
      assertAnnotation(parseAnnotation('DecimalMax({value:"100", inclusive:true})'), 'DecimalMax', {
        value: '100',
        inclusive: true,
      });
      assertAnnotation(parseAnnotation('Email({message:"foo"})'), 'Email', { message: 'foo' });
      assertAnnotation(parseAnnotation(String.raw`Pattern({regexp:"\\d+\\..+"})`), 'Pattern', { regexp: '\\d+\\..+' });
    });
  });
});

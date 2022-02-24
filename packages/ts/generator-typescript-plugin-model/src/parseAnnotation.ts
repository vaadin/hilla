import PluginError from '@hilla/generator-typescript-utils/PluginError.js';
import type { Annotation, AnnotationNamedAttributes, AnnotationPrimitiveAttribute } from './Annotation.js';

const keywords: Record<string, AnnotationPrimitiveAttribute> = { true: true, false: false };

function parseAttribute(attributeText: string): AnnotationPrimitiveAttribute {
  if (attributeText in keywords) {
    return keywords[attributeText];
  }

  if (attributeText.startsWith('"') && attributeText.endsWith('"')) {
    return attributeText.slice(1, attributeText.length - 1).replace(/\\\\/g, '\\');
  }

  const number = Number(attributeText);
  if (!Number.isNaN(number) || attributeText.toLowerCase() === 'nan') {
    return number;
  }

  throw new PluginError(`Unable to parse annotation attribute "${attributeText}"`);
}

function parseAttributes(attributesText: string): AnnotationNamedAttributes {
  attributesText = attributesText.trim();
  if (attributesText.startsWith('{') && attributesText.endsWith('}')) {
    const namedList = attributesText.slice(1, attributesText.length - 1);
    const attributes: AnnotationNamedAttributes = namedList.split(',').reduce((record, pairText) => {
      const [key, valueText] = pairText.split(':');
      record[key.trim()] = parseAttribute(valueText);
      return record;
    }, {} as Record<string, AnnotationPrimitiveAttribute>);
    return attributes;
  }

  return { value: parseAttribute(attributesText) };
}

export default function parseAnnotation(annotationText: string): Annotation {
  const [, simpleName, argumentsText] = /^(\w+)\((.*)\)$/.exec(annotationText) || [];
  if (simpleName === undefined) {
    throw new PluginError(`Unknown annotation format when processing "${annotationText}"`);
  }

  if (argumentsText !== undefined && argumentsText.trim() !== '') {
    const attributes = parseAttributes(argumentsText);
    return { simpleName, attributes };
  }

  return { simpleName };
}

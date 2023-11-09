import type { CSSProperties } from 'react';

export function convertToTitleCase(inputString: string): string {
  // Convert underscores to spaces
  const stringWithSpaces = inputString.replace(/_/gu, ' ');

  // Convert to title case
  const words = stringWithSpaces.split(' ');
  const titleCaseWords = words.map((word) => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase());

  // Join the title case words with spaces
  return titleCaseWords.join(' ');
}

export type ComponentStyleProps = Readonly<{
  id?: string;
  style?: CSSProperties;
  className?: string;
}>;

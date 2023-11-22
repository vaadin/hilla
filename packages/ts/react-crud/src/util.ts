import type { CSSProperties, FunctionComponent } from 'react';

export type ComponentStyleProps = Readonly<{
  id?: string;
  style?: CSSProperties;
  className?: string;
}>;

export function convertToTitleCase(inputString: string): string {
  // Convert underscores to spaces
  const stringWithSpaces = inputString.replace(/_/gu, ' ');

  // Convert to title case
  const words = stringWithSpaces.split(' ');
  const titleCaseWords = words.map((word) => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase());

  // Join the title case words with spaces
  return titleCaseWords.join(' ');
}

export function registerStylesheet(stylesheet: CSSStyleSheet): void {
  const css = Array.from(stylesheet.cssRules)
    .map((rule) => rule.cssText)
    .join('\n');

  const styleTag = document.createElement('style');
  styleTag.textContent = css;
  document.head.prepend(styleTag);
}

const registeredFeatures = new Set<string>();
function useFeatureRegistration(feature: string): void {
  if (registeredFeatures.has(feature)) {
    return;
  }

  registeredFeatures.add(feature);
  // @ts-expect-error: esbuild injection
  // eslint-disable-next-line @typescript-eslint/no-unsafe-call
  __REGISTER__(feature);
}

export function featureRegistration<C extends (...args: any[]) => any>(Component: C, feature: string): C {
  const ComponentWithFeatureRegistration = (...args: any[]) => {
    useFeatureRegistration(feature);
    return Component(...args);
  };
  return ComponentWithFeatureRegistration as C;
}

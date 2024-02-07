export function convertFSPatternToURLPatternString(fsPattern: string): string {
  return (
    fsPattern
      // /url/{...rest}/page -> /url/*/page
      .replaceAll(/\{\.{3}.+\}/gu, '*')
      // /url/{{param}}/page -> /url/:param?/page
      .replaceAll(/\{{2}(.+)\}{2}/gu, ':$1?')
      // /url/{param}/page -> /url/:param/page
      .replaceAll(/\{(.+)\}/gu, ':$1')
  );
}

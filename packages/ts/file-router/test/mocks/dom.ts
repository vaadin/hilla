/* eslint-disable */
export function mockDocumentBaseURI(baseURI: string): () => void {
  const doc = globalThis.document;

  // @ts-expect-error: mocking the document object for testing purposes
  globalThis.document = {
    baseURI,
  };

  return () => {
    globalThis.document = doc;
  };
}

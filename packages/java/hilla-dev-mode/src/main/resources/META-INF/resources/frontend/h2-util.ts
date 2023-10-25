export async function waitForConsole(h2Win: Window): Promise<void> {
  const isLoaded = (): boolean => {
    return !!h2Win.document.querySelector("[name='url']");
  };
  let resolve;
  const promise = new Promise<void>((r) => {
    resolve = r;
  });
  const intervalId = setInterval(() => {
    if (isLoaded()) {
      clearInterval(intervalId);
      resolve!();
    }
  }, 10);
  return promise;
}

export function loginUsingUrl(h2Window: Window, jdbcUrl: string | undefined) {
  const h2Document = h2Window.document;
  (h2Document.querySelector("[name='url']") as any).value = jdbcUrl;
  (h2Document.querySelector("[type='submit']") as HTMLElement).click();
}

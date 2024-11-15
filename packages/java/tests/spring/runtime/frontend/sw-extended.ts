/// <reference lib="webworker" />

addEventListener('message', (event) => {
  if (event.data.message === 'Hello') {
    event.source?.postMessage({ message: 'Hi' });
  }
});

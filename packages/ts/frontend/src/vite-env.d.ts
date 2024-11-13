// / <reference types="vite/client" />

// eslint-disable-next-line import/unambiguous
interface ImportMetaEnv {
  readonly VITE_SW_CONTEXT: boolean;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}

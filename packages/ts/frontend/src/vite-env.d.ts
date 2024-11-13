// / <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_SW_CONTEXT: boolean;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}

export {};

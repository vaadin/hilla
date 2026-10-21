# Documenting

## Write for the application developer

Public API — Java and TypeScript — is written for developers building an
application, who do not know how Hilla works inside. Explain: what the API does
in their terms; when to use it; what the parameters and return value mean; how
it affects the generated TypeScript, when it does; and any caveats. Do not
assume the reader will read the generator or the endpoint runtime.

## Javadoc

- Javadoc for public API explains the *why* and the caveats, not just the
  type signature.
- For annotations and types an application puts on its endpoints, say what
  they change in the generated TypeScript and at run time.
- Call out reliability concerns prominently — if a behavior holds only in
  development mode, or only for some build tools, say so.
- Prefer short, runnable examples in the class-level Javadoc, consistent with
  the real APIs.
- A new overload opens with the same sentence as its siblings and then says
  what is different about it. Javadoc that starts with the difference — or
  that only walks through the parameters and the return value — leaves out
  the sentence that says what the method does.
- Javadoc describes the code today, not what changed. Change history belongs
  in commit messages.

## TSDoc

- Exported TypeScript API is documented with TSDoc comments; ESLint checks
  their syntax (`tsdoc/syntax`).
- The same expectations as for Javadoc apply: what it is for, when to use it,
  and the caveats — the comment is what the editor shows at the call site.

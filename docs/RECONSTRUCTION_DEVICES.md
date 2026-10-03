# Source constructs that reproduce class-file output

This reconstruction targets class-file output, not only equivalent runtime behavior. Some source declarations, references, and statement shapes are retained because controlled project experiments showed that the pinned compiler and optimizer otherwise emit different bytes. They are reconstruction techniques, not claims about the original developers' source.

Examples of output-sensitive choices include:

- **Instance-qualified constants.** Reading a compile-time constant through an instance can make javac emit a null-check-related instruction sequence that a plain literal or class-qualified read would omit. The optimizer may later remove the placeholder field while preserving the emitted use-site behavior.
- **Early references and constant-pool order.** An otherwise removable reference can cause javac to create a method-name/type entry earlier; shrinking can remove the reference while the surviving pool order still reflects it.
- **Declaration and member order.** Java source order can determine field or method table order and can influence constant-pool creation order. Reordering a declaration can therefore change an output-exact class even when behavior is unchanged.
- **Name mapping and retention policy.** Mapping complete same-name member groups, together with matching keep rules, can reproduce the retail names and ordering that partial renaming does not.
- **Locals and statement form.** Dead locals, explicit boolean comparisons, casts, loop-variable scope, and branch structure can affect local-slot counts or bytecode instruction layout.

These effects are target- and toolchain-specific. The source should not be “simplified” based only on appearance: a proposed cleanup must be checked through the pinned build pipeline and exact-output verifier. Conversely, a recorded output match does not establish that any artificial-looking construct appeared in the historical source.

This document is intentionally conceptual. It omits experiment identifiers, internal transcripts, and speculative method-by-method claims.

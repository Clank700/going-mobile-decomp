# Build inputs and reproducibility

This repository is not a self-contained build distribution. It does not bundle the retail JAR or assets, compiled retail classes, the Sun/Oracle SDK payload, WTK binaries, or other third-party toolchain binaries.

A future build interface should accept locally supplied inputs, verify each against a reviewed SHA-256 lock, and fail closed on missing or mismatched inputs. The pinned Java compiler, ProGuard, CLDC APIs, and WTK preverification tools are local/external dependencies. Their exact paths and hashes must be provided through a sanitized, user-configured input manifest; machine-specific absolute paths and credentials must not be committed.

The checked-in ProGuard configuration preserves the verified matching inputs and contains relative references for the original local runner layout. It is not a portable, out-of-the-box build command. No standalone portable replay claim is made here. A reviewed build interface and external-input contract would be required before executable clone-and-build instructions can be provided.

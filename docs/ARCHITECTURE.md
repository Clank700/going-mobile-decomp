# Reconstruction architecture

This describes the reconstruction and verification pipeline, not the game's runtime architecture.

## Source and transformation layers

The proposed package contains ten reconstructed Java class sources. They use a readable source-level naming layer; the checked-in mapping and shrinker configuration describe how the historical obfuscated output names and retained members are reproduced. Those files are part of the output-matching input, not proof that the historical source used these names or declarations.

The recorded production pipeline is:

1. Compile the reconstructed Java sources with the pinned Sun javac 1.4.1 toolchain.
2. Apply the pinned ProGuard 3.2 configuration and mapping policy.
3. Run CLDC preverification with WTK 2.2.
4. Compare the resulting class files and method records against the immutable target.

The target JAR, compiler/optimizer/preverification binaries, CLDC libraries, and proprietary assets are external inputs and are not proposed package contents. A portable build claim requires a reviewed input contract and a staged build/verifier interface; neither is finalized here.

## Verification boundary

The included source set was freshly verified on 2026-10-03: 351/351 method rows EXACT and 10/10 class files byte-identical to the retail reference and production-v4 output. That result is tied to the tested source/configuration/toolchain inputs. It establishes output equality, not recovery of the original historical Java source. Further readability work may be applied later, but it must preserve exact output through the same verification pipeline.

No emulator is used as the acceptance oracle; compiled and preverified class-file output is compared directly.

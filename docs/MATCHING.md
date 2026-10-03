# Matching method

The target is Ratchet & Clank: Going Mobile v1.1.0. Acceptance is based on compiler-output matching, not merely equivalent runtime behavior.

The pipeline is reconstructed Java source → Sun javac 1.4.1 → ProGuard 3.2 → WTK 2.2 CLDC preverification → class/method comparison. Source order, declarations, casts, control flow, and compiler/optimizer behavior can affect emitted class files, so changes are tested through the pinned pipeline rather than accepted from decompiler appearance alone.

The included source set was freshly verified on 2026-10-03: all 351 method records were EXACT and all ten class files were byte-identical to both the retail reference and production-v4 output. The test also required closed class-file sets and checked that gold inputs remained unchanged.

Exactness is a property of the tested source, configuration, and pipeline. It does not prove historical source authenticity, which remains **NOT ESTABLISHED / NOT CLAIMED**.

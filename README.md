# Ratchet & Clank: Going Mobile v1.1.0 — reconstructed Java source

This private preservation/development repository contains a readable Java reconstruction and the mapping/configuration used to reproduce the target class-file output. It is not a claim that the historical developer source has been recovered.

## Verified output

The included ten-source set was freshly built and checked on 2026-10-03. The run produced **351/351 methods EXACT** and **10/10 class files byte-identical** to both the retail reference and the recorded production-v4 output. The build also checked closed class-file sets and confirmed the production-v4 gold inputs were unchanged.

This establishes output equivalence for the tested source, configuration, and pinned local toolchain. Historical/original-source authenticity remains **NOT ESTABLISHED / NOT CLAIMED**. The source is a reconstruction and may contain output-matching constructs that need not resemble the original authors' source.

## Matching process

Reconstructed Java source is compiled with the pinned Sun javac 1.4.1 toolchain, processed with ProGuard 3.2, and CLDC-preverified with WTK 2.2. The resulting method Code/StackMap data and class files are compared against the target. Source and member order, compiler output, constant-pool order, and optimizer behavior can affect exact results; changes must be tested through the historical pipeline.

## Scope and limits

This repository is an unofficial preservation and reverse-engineering project. It contains reconstructed Java source, mapping/build configuration, tooling, and documentation. It does not include the original retail JAR, retail compiled classes, standalone graphics or audio assets, level data, decoded localization resources, or proprietary compiler, optimizer, or preverification binaries.

The reconstructed source may reproduce or closely reconstruct expression originating from the original game software. No claim is made that such material is independently owned by this project.

Except where expressly stated for particular files or original project material, no software license is granted by this repository. See [LEGAL.md](https://github.com/Clank700/going-mobile-decomp/blob/main/LEGAL.md) for attribution and rights information and [docs/BUILD.md](https://github.com/Clank700/going-mobile-decomp/blob/main/docs/BUILD.md) for build-input constraints.

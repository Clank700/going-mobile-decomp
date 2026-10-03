# Naming scheme

The reconstructed Java sources use descriptive source-level member names to make
the code easier to read. These names describe this reconstruction; they are not
a claim about the historical Java source. Retail class and member names remain
the names required by the target output.

## Source names and retail names

[`config/d-preobf.map`](../config/d-preobf.map) maps reconstructed member names
back to their retail obfuscated names. The pinned ProGuard mapping and retention
configuration use this map to reproduce the target names.

Program classes retain their retail names (`a` through `i` and
`ratchetandclank`). Renaming a class would change descriptors throughout the
program and is outside this naming policy.

Methods that implement or override required library APIs keep the names and
signatures required by those APIs. They are not candidates for descriptive
renaming.

## Complete member groups

When a program member is given a descriptive source name, handle the complete
group of program members that share its retail name across the reconstructed
classes. Update the mapping and matching shrinker keep rules together. Partial
renaming can change ProGuard's `NameAndType` creation order and therefore the
constant-pool ordering in output class files.

## Reconstruction devices

Names and placement of `opus*` reconstruction devices are intentional. These
source forms can affect compiler and optimizer output even when they do not
change runtime behavior. Preserve them when changing surrounding source. See
[`ARCHITECTURE.md`](ARCHITECTURE.md) for the build and output-equality boundary
and [`RECONSTRUCTION_DEVICES.md`](RECONSTRUCTION_DEVICES.md) for examples of
output-sensitive source constructs.

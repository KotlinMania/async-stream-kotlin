# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 3/4 (75.0%)
- **Function parity:** 11/11 matched (target 15) — 100.0%
- **Class/type parity:** 8/8 matched (target 9) — 100.0%
- **Combined symbol parity:** 19/19 matched (target 24) — 100.0%
- **Average inline-code cosine:** 0.17 (function body across 3 matched files)
- **Average documentation cosine:** 0.00 (doc text across 3 matched files)
- **Cheat-zeroed Files:** 0
- **Critical Issues:** 3 files with <0.60 function similarity
- **Needs Review:** 0 files with 0.60-0.84 function similarity
- **Excellent:** 0 files with >=0.85 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. async_stream

- **Target:** `asyncstream.AsyncStream`
- **Similarity:** 0.20
- **Dependents:** 1
- **Priority Score:** 1000507.9
- **Functions:** 4/4 matched (target 5)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_

### 2. next

- **Target:** `asyncstream.Next`
- **Similarity:** 0.19
- **Dependents:** 1
- **Priority Score:** 1000408.1
- **Functions:** 2/2 matched (target 3)
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_

### 3. yielder

- **Target:** `asyncstream.Yielder`
- **Similarity:** 0.11
- **Dependents:** 0
- **Priority Score:** 1008.9
- **Functions:** 5/5 matched (target 7)
- **Missing functions:** _none_
- **Types:** 5/5 matched
- **Missing types:** _none_

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

## Reexport / Wiring Modules

These files match `reexport_modules` patterns in `.ast_distance_config.json`. They are filtered out of
normal priority and missing-file ladders because they are wiring
modules, not direct logic ports. Consult them for call-site routing;
do not treat them as the next implementation target by default.

### Missing

| Source | Expected target | Deps | Source path | Expected path |
|--------|-----------------|------|-------------|---------------|
| `lib` | `Lib` | 0 | `lib.rs` | `Lib.kt` |


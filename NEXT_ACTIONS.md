# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 4/4 (100.0%)
- **Function parity:** 11/11 matched (target 19) — 100.0%
- **Class/type parity:** 8/8 matched (target 9) — 100.0%
- **Combined symbol parity:** 19/19 matched (target 28) — 100.0%
- **Average inline-code cosine:** 0.46 (function body across 4 matched files)
- **Average documentation cosine:** 0.10 (doc text across 4 matched files)
- **Cheat-zeroed Files:** 1
- **Critical Issues:** 2 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. async_stream

- **Target:** `asyncstream.AsyncStream`
- **Similarity:** 0.73
- **Dependents:** 1
- **Priority Score:** 1000502.8
- **Functions:** 4/4 matched (target 5)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_

### 2. next

- **Target:** `asyncstream.Next`
- **Similarity:** 0.60
- **Dependents:** 1
- **Priority Score:** 1000403.9
- **Functions:** 2/2 matched (target 3)
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_

### 3. yielder

- **Target:** `asyncstream.Yielder`
- **Similarity:** 0.50
- **Dependents:** 0
- **Priority Score:** 1005.0
- **Functions:** 5/5 matched (target 7)
- **Missing functions:** _none_
- **Types:** 5/5 matched
- **Missing types:** _none_

### 4. lib

- **Target:** `asyncstream.Stream [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched (target 4)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present


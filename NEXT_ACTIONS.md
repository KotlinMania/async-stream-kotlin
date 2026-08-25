# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 4/4 (100.0%)
- **Function parity:** 5/11 matched (target 10) — 45.5%
- **Class/type parity:** 2/8 matched (target 3) — 25.0%
- **Combined symbol parity:** 7/19 matched (target 13) — 36.8%
- **Average inline-code cosine:** 0.25 (function body across 4 matched files)
- **Average documentation cosine:** 0.11 (doc text across 4 matched files)
- **Cheat-zeroed Files:** 1
- **Critical Issues:** 4 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. async_stream

- **Target:** `asyncstream.AsyncStream`
- **Similarity:** 0.36
- **Dependents:** 1
- **Priority Score:** 1030506.4
- **Functions:** 2/4 matched (target 3)
- **Missing functions:** `new`, `poll_next`
- **Types:** 0/1 matched
- **Missing types:** `Item`

### 2. next

- **Target:** `asyncstream.Next`
- **Similarity:** 0.38
- **Dependents:** 1
- **Priority Score:** 1030406.2
- **Functions:** 1/2 matched (target 1)
- **Missing functions:** `poll`
- **Types:** 0/2 matched (target 0)
- **Missing types:** `Next`, `Output`

### 3. yielder

- **Target:** `asyncstream.Yielder`
- **Similarity:** 0.28
- **Dependents:** 0
- **Priority Score:** 61007.2
- **Functions:** 2/5 matched (target 2)
- **Missing functions:** `poll`, `enter`, `drop`
- **Types:** 2/5 matched (target 2)
- **Missing types:** `Enter`, `Send`, `Output`

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


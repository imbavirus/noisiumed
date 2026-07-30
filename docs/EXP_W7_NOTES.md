# W7 Aquifer cache (deferred multi)

## Intent
Cache repeated `getAquiferStatus` / fluid lattice within a cell or column (prefix-cache analogue).

## Status
**Not multi-scored this session.** High golden risk; thrash seed always pays full aquifer path.

## Implementation notes for next session
1. Mixin `AquiferSampler.Impl`: cache last aquifer cell key (x/z/y grid) + status.
2. Invalidate on sampleUniqueIndex / cell boundary.
3. Gate with `-Dnoisiumed.exp.w7.aquifer.cache=true` default off until golden green.
4. Multi ×5 + golden FULL before ADOPT.

## Why skipped multi now
Machine thrash + file locks made isolation walls non-comparable; prioritize cold re-bench of W1/W4 first.

#!/usr/bin/env python3
"""Parse Spark sampler .bin for worldgen / density / aquifer hot frames.

Usage:
  python parse_spark_worldgen.py <path-or-id.bin> [path2.bin ...]
  python parse_spark_worldgen.py --aquifer results/foo.bin
"""
from __future__ import annotations

import argparse
import importlib.util
import sys
from pathlib import Path

_BENCH = Path(__file__).resolve().parent
_RESULTS = _BENCH / "results"

spec = importlib.util.spec_from_file_location("p", _BENCH / "parse_spark_sampler.py")
p = importlib.util.module_from_spec(spec)
assert spec.loader is not None
spec.loader.exec_module(p)


def deep_methods(path: Path):
    data = path.read_bytes()
    root = p.read_fields(data)
    threads = []
    for t, v in root.get(2, []):
        if t == "ld":
            th = p.parse_thread_node(v)
            p.unflatten_thread(th)
            threads.append(th)
    acc: dict[str, float] = {}

    def walk(n, is_th=False):
        if is_th:
            label = "THREAD:" + (n.get("name") or "")
        else:
            label = f"{n.get('className', '')}#{n.get('methodName', '')}"
        acc[label] = acc.get(label, 0) + float(n.get("time") or 0)
        for c in n.get("children") or []:
            walk(c)

    for th in threads:
        walk(th, True)

    needles = (
        "noise",
        "chunk",
        "biome",
        "height",
        "surface",
        "aquifer",
        "structure",
        "worldgen",
        "noisium",
        "bulk",
        "staging",
        "zenx",
        "zfast",
        "palette",
        "populate",
        "fillfrom",
        "serverlevel",
        "density",
        "carver",
        "feature",
        "specdensity",
        "beard",
        "ore",
        "interpolat",
    )
    keys = [
        k
        for k in acc
        if any(x in k.lower() for x in needles)
        and "Unsafe" not in k
        and "park" not in k.lower()
        and "LockSupport" not in k
        and "Concurrent" not in k
    ]
    keys.sort(key=lambda k: -acc[k])
    return acc, keys, threads


AQUIFER_NEEDLES = (
    "aquifer",
    "noisebasedaquifer",
    "fluidstatus",
    "computesubstance",
    "needsfluidtick",
)


def print_report(path: Path, aquifer_only: bool = False) -> None:
    if not path.is_file():
        # bare spark id
        cand = _RESULTS / path.name
        if not cand.suffix:
            cand = _RESULTS / f"{path.name}.bin"
        path = cand if cand.is_file() else path
    if not path.is_file():
        print(f"MISSING {path}", file=sys.stderr)
        return

    acc, keys, threads = deep_methods(path)
    label = path.stem
    print(f"==== {label} ({path})")
    for th in threads:
        name = th.get("name") or ""
        if name in ("Server thread", "Worker-Main (x31)") or "Worker-Main" in name or name == "Server thread":
            print(f"  THREAD {name}: {th.get('time'):.0f} ms")

    if aquifer_only:
        aq_keys = [k for k in acc if any(n in k.lower() for n in AQUIFER_NEEDLES)]
        aq_keys.sort(key=lambda k: -acc[k])
        total_aq = sum(acc[k] for k in aq_keys)
        print(f"  -- aquifer-related frames (inclusive ms), sum={total_aq:.0f} --")
        for k in aq_keys[:40]:
            print(f"  {acc[k]:10.0f}  {k[:140]}")
        # also sample / surface for context
        for needle, title in (
            ("sampleblockstate", "sampleBlockState"),
            ("getinterpolatedstate", "getInterpolatedState"),
            ("bulknoisefiller", "BulkNoiseFiller"),
            ("fastworldgen", "FastWorldgen"),
            ("populate", "populate"),
            ("buildsurface", "buildSurface"),
            ("fastworldgen", "FastWorldgen"),
        ):
            hits = [(k, acc[k]) for k in acc if needle in k.lower()]
            if hits:
                hits.sort(key=lambda x: -x[1])
                print(f"  -- context {title} --")
                for k, v in hits[:5]:
                    print(f"  {v:10.0f}  {k[:140]}")
        print()
        return

    print("  -- top worldgen-ish frames (inclusive ms) --")
    for k in keys[:40]:
        print(f"  {acc[k]:10.0f}  {k[:140]}")
    print()


def main() -> None:
    ap = argparse.ArgumentParser(description="Parse Spark worldgen sampler bins")
    ap.add_argument("bins", nargs="+", help="path to .bin or spark id under results/")
    ap.add_argument(
        "--aquifer",
        action="store_true",
        help="Focus on aquifer-related frames + sample/surface context",
    )
    args = ap.parse_args()
    for b in args.bins:
        print_report(Path(b), aquifer_only=args.aquifer)


if __name__ == "__main__":
    main()

#!/usr/bin/env python3
"""
Golden section hash for Minecraft 1.18+ anvil regions.

Hashes each chunk's block-state sections (palette names + packed data) for a
chunk-radius around (0,0) in the Overworld. Used to compare vanilla / baseline
worlds vs Noisiumed for terrain parity after noise fill.

Requires: nbtlib  (pip install nbtlib)

Usage:
  python parity_hash.py <world_dir> [--radius R] [--seed N] [--out path.json]
  python parity_hash.py --compare a.json b.json
"""
from __future__ import annotations

import argparse
import gzip
import hashlib
import json
import struct
import sys
import zlib
from io import BytesIO
from pathlib import Path
from typing import Any, Dict, List, Optional, Tuple

try:
    from nbtlib import File as NbtFile
except ImportError:
    print("Install nbtlib: pip install nbtlib", file=sys.stderr)
    sys.exit(1)


def decompress_chunk(blob: bytes) -> bytes:
    if not blob:
        raise ValueError("empty chunk")
    ctype = blob[0]
    payload = blob[1:]
    if ctype == 1:
        return gzip.decompress(payload)
    if ctype == 2:
        return zlib.decompress(payload)
    if ctype == 3:
        return payload
    if ctype == 4:
        try:
            import zstandard as zstd  # type: ignore
        except ImportError as e:
            raise ValueError("chunk uses zstd; pip install zstandard") from e
        return zstd.ZstdDecompressor().decompress(payload)
    raise ValueError(f"unknown compression type {ctype}")


def read_mca_chunk(region_path: Path, local_x: int, local_z: int) -> Optional[bytes]:
    data = region_path.read_bytes()
    if len(data) < 8192:
        return None
    idx = 4 * (local_x + local_z * 32)
    loc = struct.unpack(">I", data[idx : idx + 4])[0]
    offset = (loc >> 8) * 4096
    sectors = loc & 0xFF
    if offset == 0 or sectors == 0:
        return None
    if offset + 4 > len(data):
        return None
    length = struct.unpack(">I", data[offset : offset + 4])[0]
    if length <= 0 or offset + 4 + length > len(data):
        return None
    return data[offset + 4 : offset + 4 + length]


def palette_name(entry: Any) -> str:
    # nbtlib Compound
    if hasattr(entry, "keys"):
        name = str(entry.get("Name", entry.get("name", "?")))
        props = entry.get("Properties") or entry.get("properties")
        if props is not None and hasattr(props, "keys") and len(props) > 0:
            keys = sorted(str(k) for k in props.keys())
            p = ",".join(f"{k}={props[k]}" for k in keys)
            return f"{name}[{p}]"
        return name
    return str(entry)


def as_int(v: Any) -> int:
    try:
        return int(v)
    except Exception:
        return int(str(v))


def chunk_status(root: Any) -> str:
    st = root.get("Status")
    if st is None and "Level" in root:
        st = root["Level"].get("Status")
    return str(st) if st is not None else ""


def hash_chunk_sections(root: Any) -> str:
    h = hashlib.sha256()
    # nbtlib File acts as root compound
    sections = root.get("sections")
    if sections is None and "Level" in root:
        level = root["Level"]
        sections = level.get("Sections") or level.get("sections")
        cx = as_int(level.get("xPos", root.get("xPos", 0)))
        cz = as_int(level.get("zPos", root.get("zPos", 0)))
    else:
        cx = as_int(root.get("xPos", 0))
        cz = as_int(root.get("zPos", 0))

    h.update(struct.pack(">ii", cx, cz))
    # Include status so incomplete gen cannot silently match another incomplete world.
    h.update(chunk_status(root).encode("utf-8"))
    h.update(b"|")
    if not sections:
        h.update(b"empty")
        return h.hexdigest()

    def sec_y(s: Any) -> int:
        return as_int(s.get("Y", s.get("y", 0)))

    ordered = sorted(list(sections), key=sec_y)
    for sec in ordered:
        y = sec_y(sec)
        h.update(struct.pack(">i", y))
        bs = sec.get("block_states") or sec.get("BlockStates")
        if bs is None:
            palette = sec.get("Palette") or sec.get("palette")
            data = sec.get("BlockStates") or sec.get("data")
            if palette is None:
                h.update(b"no-bs")
                continue
            for e in palette:
                h.update(palette_name(e).encode("utf-8"))
                h.update(b"\0")
            if data is not None:
                for v in data:
                    h.update(struct.pack(">q", as_int(v)))
            continue

        palette = bs.get("palette") or bs.get("Palette") or []
        data = bs.get("data") or bs.get("Data")
        for e in palette:
            h.update(palette_name(e).encode("utf-8"))
            h.update(b"\0")
        if data is not None:
            for v in data:
                h.update(struct.pack(">q", as_int(v)))
        else:
            h.update(b"single")
    return h.hexdigest()


def iter_chunks(radius: int) -> List[Tuple[int, int]]:
    coords = []
    for cz in range(-radius, radius + 1):
        for cx in range(-radius, radius + 1):
            coords.append((cx, cz))
    return coords


def load_chunk_root(world_dir: Path, cx: int, cz: int) -> Optional[Any]:
    region_dir = world_dir / "region"
    if not region_dir.is_dir():
        alt = world_dir / "world" / "region"
        if alt.is_dir():
            region_dir = alt
    rx, rz = cx >> 5, cz >> 5
    path = region_dir / f"r.{rx}.{rz}.mca"
    if not path.is_file():
        return None
    blob = read_mca_chunk(path, cx & 31, cz & 31)
    if blob is None:
        return None
    raw = decompress_chunk(blob)
    return NbtFile.parse(BytesIO(raw))


def hash_world(world_dir: Path, radius: int) -> Dict[str, Any]:
    chunks: Dict[str, str] = {}
    statuses: Dict[str, str] = {}
    missing = 0
    errors = 0
    not_full = 0
    for cx, cz in iter_chunks(radius):
        key = f"{cx},{cz}"
        try:
            root = load_chunk_root(world_dir, cx, cz)
            if root is None:
                missing += 1
                chunks[key] = "MISSING"
                statuses[key] = "MISSING"
                continue
            st = chunk_status(root)
            statuses[key] = st
            if "full" not in st.lower() and "minecraft:full" not in st.lower():
                not_full += 1
            chunks[key] = hash_chunk_sections(root)
        except Exception as ex:
            errors += 1
            chunks[key] = f"ERROR:{type(ex).__name__}:{ex}"
            statuses[key] = "ERROR"

    h = hashlib.sha256()
    for key in sorted(chunks.keys(), key=lambda k: (int(k.split(",")[1]), int(k.split(",")[0]))):
        h.update(key.encode())
        h.update(b"=")
        h.update(chunks[key].encode())
        h.update(b"\n")
    return {
        "world": str(world_dir.resolve()),
        "radius": radius,
        "chunk_count": len(chunks),
        "missing": missing,
        "errors": errors,
        "not_full": not_full,
        "statuses": statuses,
        "overall": h.hexdigest(),
        "chunks": chunks,
    }


def compare(a: Dict[str, Any], b: Dict[str, Any]) -> Dict[str, Any]:
    ca, cb = a.get("chunks", {}), b.get("chunks", {})
    keys = sorted(set(ca) | set(cb))
    mismatches = []
    matches = 0
    for k in keys:
        ha, hb = ca.get(k), cb.get(k)
        if ha == hb:
            matches += 1
        else:
            mismatches.append({"chunk": k, "a": ha, "b": hb})
    return {
        "match": len(mismatches) == 0 and a.get("overall") == b.get("overall"),
        "overall_a": a.get("overall"),
        "overall_b": b.get("overall"),
        "matches": matches,
        "mismatches": len(mismatches),
        "diff": mismatches[:64],
        "diff_truncated": len(mismatches) > 64,
    }


def main() -> int:
    ap = argparse.ArgumentParser(description="Golden section hash for anvil worlds")
    ap.add_argument("world", nargs="?", help="Path to world folder (contains region/)")
    ap.add_argument("--radius", type=int, default=2, help="Chunk radius around 0,0 (default 2)")
    ap.add_argument("--seed", type=int, default=None)
    ap.add_argument("--out", type=str, default=None)
    ap.add_argument("--compare-a", type=str, default=None, help="Hash JSON A (with --compare-b)")
    ap.add_argument("--compare-b", type=str, default=None, help="Hash JSON B (with --compare-a)")
    args = ap.parse_args()

    if args.compare_a or args.compare_b:
        if not args.compare_a or not args.compare_b:
            ap.error("both --compare-a and --compare-b required")
        with open(args.compare_a, encoding="utf-8") as f:
            a = json.load(f)
        with open(args.compare_b, encoding="utf-8") as f:
            b = json.load(f)
        rep = compare(a, b)
        print(json.dumps(rep, indent=2))
        return 0 if rep["match"] else 2

    if not args.world:
        ap.error("world path required (or --compare-a/--compare-b)")
    world = Path(args.world)
    if not world.is_dir():
        print(f"not a directory: {world}", file=sys.stderr)
        return 1

    report = hash_world(world, args.radius)
    if args.seed is not None:
        report["seed"] = args.seed
    text = json.dumps(report, indent=2, sort_keys=True)
    if args.out:
        Path(args.out).write_text(text, encoding="utf-8")
        print(
            f"wrote {args.out} overall={report['overall']} "
            f"missing={report['missing']} errors={report['errors']} not_full={report['not_full']}"
        )
    else:
        print(text)
    if report["errors"]:
        return 4
    if report["missing"]:
        return 3
    return 0


if __name__ == "__main__":
    sys.exit(main())

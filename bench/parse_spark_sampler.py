#!/usr/bin/env python3
"""Minimal protobuf wire decoder for lucko spark SamplerData (see spark-viewer proto/spark.proto)."""
from __future__ import annotations

import collections
import struct
import sys
from pathlib import Path


def read_varint(buf: bytes, i: int):
    shift = 0
    result = 0
    while True:
        b = buf[i]
        i += 1
        result |= (b & 0x7F) << shift
        if not (b & 0x80):
            return result, i
        shift += 7


def read_fields(buf: bytes):
    i = 0
    n = len(buf)
    fields = collections.defaultdict(list)
    while i < n:
        key, i = read_varint(buf, i)
        field = key >> 3
        wtype = key & 7
        if wtype == 0:  # varint
            val, i = read_varint(buf, i)
            fields[field].append(("v", val))
        elif wtype == 1:  # 64-bit
            val = buf[i : i + 8]
            i += 8
            fields[field].append(("64", val))
        elif wtype == 2:  # length-delimited
            ln, i = read_varint(buf, i)
            val = buf[i : i + ln]
            i += ln
            fields[field].append(("ld", val))
        elif wtype == 5:  # 32-bit
            val = buf[i : i + 4]
            i += 4
            fields[field].append(("32", val))
        else:
            raise ValueError(f"unsupported wire type {wtype} at field {field}")
    return fields


def as_str(val: bytes) -> str:
    return val.decode("utf-8", errors="replace")


def as_f64(val: bytes) -> float:
    return struct.unpack("<d", val)[0]


def as_i64(val: bytes) -> int:
    return struct.unpack("<q", val)[0]


def packed_doubles(val: bytes):
    """Decode repeated fixed64 or packed doubles from length-delimited payload."""
    if len(val) % 8 == 0 and len(val) > 0:
        return list(struct.unpack("<" + "d" * (len(val) // 8), val))
    # non-packed: shouldn't happen as length-delimited for repeated scalar pack
    return []


def packed_varints(val: bytes):
    out = []
    i = 0
    while i < len(val):
        v, i = read_varint(val, i)
        out.append(v)
    return out


def parse_stack_node(buf: bytes) -> dict:
    f = read_fields(buf)
    node = {
        "time": 0.0,
        "times": [],
        "className": "",
        "methodName": "",
        "children": [],
        "childrenRefs": [],
    }
    if 1 in f:
        for t, v in f[1]:
            if t == "64":
                node["time"] = as_f64(v)
    if 8 in f:  # times repeated double
        for t, v in f[8]:
            if t == "64":
                node["times"].append(as_f64(v))
            elif t == "ld":
                node["times"].extend(packed_doubles(v))
    if 3 in f:
        for t, v in f[3]:
            if t == "ld":
                node["className"] = as_str(v)
    if 4 in f:
        for t, v in f[4]:
            if t == "ld":
                node["methodName"] = as_str(v)
    if 2 in f:
        for t, v in f[2]:
            if t == "ld":
                node["children"].append(parse_stack_node(v))
    if 9 in f:  # childrenRefs
        for t, v in f[9]:
            if t == "v":
                node["childrenRefs"].append(v)
            elif t == "ld":
                node["childrenRefs"].extend(packed_varints(v))
    if node["times"]:
        node["time"] = sum(node["times"])
    return node


def parse_thread_node(buf: bytes) -> dict:
    f = read_fields(buf)
    node = {
        "name": "",
        "time": 0.0,
        "times": [],
        "children": [],
        "childrenRefs": [],
        "flatChildren": [],
    }
    if 1 in f:
        for t, v in f[1]:
            if t == "ld":
                node["name"] = as_str(v)
    if 2 in f:
        for t, v in f[2]:
            if t == "64":
                node["time"] = as_f64(v)
    if 4 in f:  # times
        for t, v in f[4]:
            if t == "64":
                node["times"].append(as_f64(v))
            elif t == "ld":
                node["times"].extend(packed_doubles(v))
    if 3 in f:
        for t, v in f[3]:
            if t == "ld":
                # In flat encoding these are the flat array, not nested children
                node["flatChildren"].append(parse_stack_node(v))
    if 5 in f:  # childrenRefs
        for t, v in f[5]:
            if t == "v":
                node["childrenRefs"].append(v)
            elif t == "ld":
                node["childrenRefs"].extend(packed_varints(v))
    if node["times"]:
        node["time"] = sum(node["times"])
    return node


def unflatten_thread(thread: dict):
    flat = thread.get("flatChildren") or []
    if not thread.get("childrenRefs") and thread.get("children"):
        return
    # if childrenRefs present, children field 3 is flat array
    if flat and thread.get("childrenRefs") is not None:

        def resolve(node):
            refs = node.get("childrenRefs") or []
            node["children"] = [flat[r] for r in refs if 0 <= r < len(flat)]
            for ch in node["children"]:
                resolve(ch)

        # thread itself uses childrenRefs into flat
        thread["children"] = [
            flat[r] for r in (thread.get("childrenRefs") or []) if 0 <= r < len(flat)
        ]
        for ch in thread["children"]:
            resolve(ch)
        # also fix times on flat nodes
        for n in flat:
            if n.get("times"):
                n["time"] = sum(n["times"])
    elif flat and not thread.get("children"):
        # nested encoding already in flatChildren? treat as direct children
        thread["children"] = flat


def parse_window_stats(buf: bytes) -> dict:
    f = read_fields(buf)
    out = {}
    mapping = {
        1: ("ticks", "v"),
        2: ("cpuProcess", "d"),
        3: ("cpuSystem", "d"),
        4: ("tps", "d"),
        5: ("msptMedian", "d"),
        6: ("msptMax", "d"),
        10: ("chunks", "v"),
        13: ("duration", "v"),
    }
    for num, (name, kind) in mapping.items():
        if num not in f:
            continue
        t, v = f[num][0]
        if kind == "v" and t == "v":
            out[name] = v
        elif kind == "d" and t == "64":
            out[name] = as_f64(v)
    return out


def parse_metadata(buf: bytes) -> dict:
    f = read_fields(buf)
    meta = {}
    if 2 in f:  # startTime
        t, v = f[2][0]
        if t == "v":
            meta["startTime"] = v
    if 3 in f:  # interval
        t, v = f[3][0]
        if t == "v":
            meta["interval"] = v
    if 11 in f:  # endTime
        t, v = f[11][0]
        if t == "v":
            meta["endTime"] = v
    if 12 in f:
        t, v = f[12][0]
        if t == "v":
            meta["numberOfTicks"] = v
    if 7 in f:  # platform
        pf = read_fields(f[7][0][1])
        plat = {}
        if 2 in pf:
            plat["name"] = as_str(pf[2][0][1])
        if 3 in pf:
            plat["version"] = as_str(pf[3][0][1])
        if 4 in pf:
            plat["minecraftVersion"] = as_str(pf[4][0][1])
        meta["platform"] = plat
    if 13 in f:  # sources map entries
        sources = []
        for t, v in f[13]:
            if t != "ld":
                continue
            # map entry: field1 key, field2 value message
            e = read_fields(v)
            key = as_str(e[1][0][1]) if 1 in e else "?"
            val = {}
            if 2 in e:
                vm = read_fields(e[2][0][1])
                if 1 in vm:
                    val["name"] = as_str(vm[1][0][1])
                if 2 in vm:
                    val["version"] = as_str(vm[2][0][1])
            sources.append((key, val))
        meta["sources"] = sources
    return meta


def walk(node: dict, path: list, acc: dict, is_thread=False):
    time = float(node.get("time") or 0.0)
    if is_thread:
        label = f"THREAD:{node.get('name','')}"
    else:
        cls = node.get("className") or ""
        meth = node.get("methodName") or ""
        label = f"{cls}#{meth}" if cls or meth else "?"
    acc[label] = acc.get(label, 0.0) + time
    # also accumulate keywords
    low = label.lower()
    for key in KEYWORDS:
        if key.lower() in low:
            acc[f"KW:{key}"] = acc.get(f"KW:{key}", 0.0) + time
    for ch in node.get("children") or []:
        walk(ch, path + [label], acc, False)


KEYWORDS = [
    "Density",
    "DensityFunction",
    "HolderHolder",
    "Mapped",
    "Ap2",
    "MulOrAdd",
    "RangeChoice",
    "RegistryEntry",
    "compute",
    "fillArray",
    "transform",

    "populateNoise",
    "fillFromNoise",
    "NoiseChunkGenerator",
    "NoiseChunk",
    "sampleBlockState",
    "setBlockState",
    "PalettedContainer",
    "ChunkSection",
    "Heightmap",
    "trackUpdate",
    "Aquifer",
    "buildSurface",
    "applyBiomeDecoration",
    "createStructures",
    "WorldGenRegion",
    "ServerChunkCache",
    "ChunkMap",
    "noisium",
    "Noisiumed",
    "BulkNoise",
    "Staging",
    "zfastnoise",
    "zenxarch",
    "FastNoise",
    "FastChunk",
    "generateFeatures",
    "doWork",
    "runTask",
    "minecraft",
]


def analyze(path: Path, label: str):
    data = path.read_bytes()
    root = read_fields(data)
    meta = {}
    if 1 in root:
        meta = parse_metadata(root[1][0][1])
    threads = []
    if 2 in root:
        for t, v in root[2]:
            if t == "ld":
                th = parse_thread_node(v)
                unflatten_thread(th)
                threads.append(th)

    windows = {}
    if 7 in root:
        for t, v in root[7]:
            if t != "ld":
                continue
            # map entry
            e = read_fields(v)
            key = e[1][0][1] if 1 in e else 0
            if isinstance(key, bytes):
                continue
            if 2 in e:
                windows[key] = parse_window_stats(e[2][0][1])

    acc = {}
    total_thread = 0.0
    for th in threads:
        total_thread += float(th.get("time") or 0)
        walk(th, [], acc, True)

    # top methods (non-thread)
    methods = [(k, v) for k, v in acc.items() if not k.startswith("THREAD:") and not k.startswith("KW:")]
    methods.sort(key=lambda x: x[1], reverse=True)

    print(f"\n======== {label} ========")
    print(f"file={path.name} bytes={len(data)}")
    if meta:
        print(f"meta interval_ms={meta.get('interval')} ticks={meta.get('numberOfTicks')}")
        if "startTime" in meta and "endTime" in meta:
            print(f"duration_ms={meta['endTime'] - meta['startTime']}")
        if "platform" in meta:
            print(f"platform={meta['platform']}")
        if "sources" in meta:
            print("mods:", ", ".join(f"{k}:{v.get('version','')}" for k, v in meta["sources"][:12]))
    print(f"threads={len(threads)} sum_thread_time_ms={total_thread:.1f}")
    for th in sorted(threads, key=lambda x: x.get("time") or 0, reverse=True)[:12]:
        print(f"  thread {th.get('name')!r:40} time_ms={th.get('time'):.1f}")

    if windows:
        # average window stats
        cpu = [w["cpuProcess"] for w in windows.values() if "cpuProcess" in w]
        tps = [w["tps"] for w in windows.values() if "tps" in w]
        mspt = [w["msptMedian"] for w in windows.values() if "msptMedian" in w]
        mspt_max = [w["msptMax"] for w in windows.values() if "msptMax" in w]
        chunks = [w["chunks"] for w in windows.values() if "chunks" in w]
        print(f"windows={len(windows)}")
        if cpu:
            print(f"  cpuProcess avg={sum(cpu)/len(cpu):.3f} max={max(cpu):.3f}")
        if tps:
            print(f"  tps avg={sum(tps)/len(tps):.2f} min={min(tps):.2f}")
        if mspt:
            print(f"  msptMedian avg={sum(mspt)/len(mspt):.2f} max={max(mspt):.2f}")
        if mspt_max:
            print(f"  msptMax peak={max(mspt_max):.2f}")
        if chunks:
            print(f"  chunks loaded avg={sum(chunks)/len(chunks):.1f} max={max(chunks)}")

    print("keyword inclusive time (ms, summed where method name matches; can double-count trees):")
    kws = [(k, v) for k, v in acc.items() if k.startswith("KW:")]
    kws.sort(key=lambda x: x[1], reverse=True)
    for k, v in kws[:40]:
        if v > 0.5:
            print(f"  {k[3:]:28} {v:10.1f}")

    print("top 25 stack frames by inclusive time (ms):")
    for k, v in methods[:25]:
        if v < 1:
            break
        short = k if len(k) < 100 else k[:50] + "..." + k[-40:]
        print(f"  {v:10.1f}  {short}")

    return {
        "label": label,
        "total_thread_ms": total_thread,
        "threads": {th.get("name"): th.get("time") for th in threads},
        "keywords": {k[3:]: v for k, v in kws},
        "top": methods[:40],
        "windows": windows,
        "meta": meta,
    }


def main():
    if len(sys.argv) >= 3:
        pairs = [
            (sys.argv[1], Path(sys.argv[2])),
        ]
        if len(sys.argv) >= 5:
            pairs.append((sys.argv[3], Path(sys.argv[4])))
    elif len(sys.argv) == 2:
        pairs = [(Path(sys.argv[1]).stem, Path(sys.argv[1]))]
    else:
        pairs = [
            ("Noisiumed beta.2 density", Path(r"C:\Users\imba\Git\infernos\minecraft-mods\noisium\bench\results\zBo6wluizL.bin")),
            ("Fast Noise 1.0.13", Path(r"C:\Users\imba\Git\infernos\minecraft-mods\noisium\bench\results\h1zZrWPuC4.bin")),
        ]
    results = []
    for label, path in pairs:
        results.append(analyze(path, label))

    if len(results) < 2:
        return

    # head-to-head keywords
    print("\n======== HEAD-TO-HEAD (keyword inclusive ms) ========")
    a, b = results[0], results[1]
    keys = sorted(set(a["keywords"]) | set(b["keywords"]), key=lambda k: -(a["keywords"].get(k, 0) + b["keywords"].get(k, 0)))
    print(f"{'keyword':28} {a['label'][:12]:>12} {b['label'][:12]:>12} {'delta(A-B)':>12}")
    for k in keys:
        va = a["keywords"].get(k, 0.0)
        vb = b["keywords"].get(k, 0.0)
        if va < 1 and vb < 1:
            continue
        print(f"{k:28} {va:12.1f} {vb:12.1f} {va-vb:12.1f}")

    print(f"\n{'metric':28} {a['label'][:12]:>12} {b['label'][:12]:>12}")
    print(f"{'sum_thread_time_ms':28} {a['total_thread_ms']:12.1f} {b['total_thread_ms']:12.1f}")


if __name__ == "__main__":
    main()

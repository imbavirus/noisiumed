import importlib.util
from pathlib import Path

spec = importlib.util.spec_from_file_location(
    "p",
    r"C:\Users\imba\Git\infernos\minecraft-mods\noisium\bench\parse_spark_sampler.py",
)
p = importlib.util.module_from_spec(spec)
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
    acc = {}

    def walk(n, is_th=False):
        if is_th:
            label = "THREAD:" + (n.get("name") or "")
        else:
            label = f"{n.get('className','')}#{n.get('methodName','')}"
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


for lab, id_ in [("NOISIUMED 4.0 L1", "kTQbjSiNVK"), ("FAST NOISE 1.0.13", "FFMyCkO3Zn")]:
    acc, keys, threads = deep_methods(
        Path(rf"C:\Users\imba\Git\infernos\minecraft-mods\noisium\bench\results\{id_}.bin")
    )
    print("====", lab)
    for th in threads:
        name = th.get("name") or ""
        if name in ("Server thread", "Worker-Main (x31)") or "Server" in name:
            print(f"  THREAD {name}: {th.get('time'):.0f} ms")
    print("  -- top worldgen-ish frames (inclusive ms) --")
    for k in keys[:35]:
        print(f"  {acc[k]:10.0f}  {k[:130]}")
    print()

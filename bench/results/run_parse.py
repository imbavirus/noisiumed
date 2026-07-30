from pathlib import Path
import importlib.util
spec = importlib.util.spec_from_file_location("p", r"C:\Users\imba\Git\infernos\minecraft-mods\noisium\bench\parse_spark_sampler.py")
p = importlib.util.module_from_spec(spec); spec.loader.exec_module(p)
ids = Path(r"C:\Users\imba\Git\infernos\minecraft-mods\noisium\bench\results")
# find newest bins matching pattern - pass via env
import os
n = os.environ["NID"]; f = os.environ["FID"]
p.analyze(ids / f"{n}.bin", "Noisiumed 4.0.0-alpha.2")
p.analyze(ids / f"{f}.bin", "Fast Noise 1.0.13")
# head to head keywords after both analyzes - re-run main style
a = p.analyze  # just print from analyze already

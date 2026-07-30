import importlib.util
from pathlib import Path
import sys
spec = importlib.util.spec_from_file_location("p", r"C:\Users\imba\Git\infernos\minecraft-mods\noisium\bench\parse_spark_sampler.py")
m = importlib.util.module_from_spec(spec); spec.loader.exec_module(m)
base = Path(r"C:\Users\imba\Git\infernos\minecraft-mods\noisium\bench\results")
m.analyze(base / (sys.argv[1]+".bin"), "Noisiumed-beta.1")
m.analyze(base / (sys.argv[2]+".bin"), "FastNoise")

# Noisiumed vs Fast Noise - Spark worldgen compare
Date: 2026-07-30T18:22:38.9241070+02:00
Platform: NeoForge 21.1.233 / MC 1.21.1 (dedicated, nogui, RCON)
Seed=12345 forceload radius=7 profile=100s Xmx=4G
Baseline mods: Spark + candidate only
Huge-win metrics: wall_boot_ms / wall_forceload_ms / wall_profile_window_ms / wall_total_ms + PathMetrics

**Scope:** Primary **1.21.1**. A/B = **noisiumed-4.0.0-beta.16.5-neoforge-1.21.1.jar** vs Fast Noise 1.0.13.

## Noisiumed
```
label=noisiumed
seed=12345 radiusChunks=7 profileSeconds=100 port=25570
wall_boot_ms=29346 wall_forceload_ms=54083 wall_profile_window_ms=154229 wall_total_ms=197443
spark_url=https://spark.lucko.me/2P2gGBxLEi
spark_bin=C:\Users\imba\Git\infernos\minecraft-mods\noisium\bench\results\2P2gGBxLEi.bin
--- path metrics (from log) ---
[30Jul2026 18:23:57.270] [Worker-Main-11/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1920 l2=1897 l1_fail=0 biome=56352 surface_skip=0 l1_avg_us=79355 direct_writes=63532899 sections_touched=17745 sample_pct=70 write_pct=29 specialize=3658302 nc1_s1=0 nc1_s2=3006 nc1_s3=0 nc3_grid=3006 nc5_ore=3006 aq_spec=3006 spec_deep=0 spec_nodes=3006 vanilla_arith=0 l0_reasons{}
[30Jul2026 18:23:59.961] [Worker-Main-11/INFO] [noisiumed/]: noisiumed.path l0=0 l1=2048 l2=2028 l1_fail=0 biome=59160 surface_skip=0 l1_avg_us=77616 direct_writes=67792982 sections_touched=18951 sample_pct=71 write_pct=28 specialize=3799474 nc1_s1=0 nc1_s2=3122 nc1_s3=0 nc3_grid=3122 nc5_ore=3122 aq_spec=3122 spec_deep=0 spec_nodes=3122 vanilla_arith=0 l0_reasons{}
[30Jul2026 18:24:03.602] [Worker-Main-24/INFO] [noisiumed/]: noisiumed.path l0=0 l1=2176 l2=2166 l1_fail=0 biome=63192 surface_skip=0 l1_avg_us=75980 direct_writes=72573467 sections_touched=20252 sample_pct=72 write_pct=27 specialize=4013666 nc1_s1=0 nc1_s2=3298 nc1_s3=0 nc3_grid=3298 nc5_ore=3298 aq_spec=3298 spec_deep=0 spec_nodes=3298 vanilla_arith=0 l0_reasons{}
--- rcon spark profiler stop ---

--- rcon spark tps ---

--- rcon spark health ---

--- matched log lines ---
[18:22:41.899] [main/INFO] [mixin/]: SpongePowered MIXIN Subsystem Version=0.8.7 Source=union:/C:/Users/imba/Git/infernos/minecraft-mods/noisium/bench/run-noisiumed/libraries/net/fabricmc/sponge-mixin/0.15.2+mixin.0.8.7/sponge-mixin-0.15.2+mixin.0.8.7.jar%2373!/ Service=ModLauncher Env=SERVER 
[18:22:42.416] [main/INFO] [loading.moddiscovery.ModDiscoverer/SCAN]: Found mod file "noisiumed-4.0.0-beta.16.5-neoforge-1.21.1.jar" [locator: {mods folder locator at C:\Users\imba\Git\infernos\minecraft-mods\noisium\bench\run-noisiumed\mods}, reader: mod manifest] 
[18:22:42.425] [main/INFO] [loading.moddiscovery.ModDiscoverer/SCAN]: Found mod file "spark-1.10.124-neoforge.jar" [locator: {mods folder locator at C:\Users\imba\Git\infernos\minecraft-mods\noisium\bench\run-noisiumed\mods}, reader: mod manifest] 
		Noisiumed 4.0.0-beta.16.5-neoforge-1.21.1 (noisiumed)
		spark 1.10.124 (spark) 
[18:22:54] [modloading-worker-0/INFO] [noisiumed/]: Loading Noisiumed (L0�L2 + density specializer + surface/coverage). Metrics: PathMetrics.snapshot()
[18:22:55] [main/INFO] [mojang/YggdrasilAuthenticationService]: Environment: Environment[sessionHost=https://sessionserver.mojang.com, servicesHost=https://api.minecraftservices.com, name=PROD]
[18:22:55] [main/WARN] [minecraft/VanillaPackResourcesBuilder]: Assets URL 'union:/C:/Users/imba/Git/infernos/minecraft-mods/noisium/bench/run-noisiumed/libraries/net/minecraft/server/1.21.1-20240808.144430/server-1.21.1-20240808.144430-srg.jar%23119!/assets/.mcassetsroot' uses unexpected schema
[18:22:55] [main/WARN] [minecraft/VanillaPackResourcesBuilder]: Assets URL 'union:/C:/Users/imba/Git/infernos/minecraft-mods/noisium/bench/run-noisiumed/libraries/net/minecraft/server/1.21.1-20240808.144430/server-1.21.1-20240808.144430-srg.jar%23119!/data/.mcassetsroot' uses unexpected schema
[18:22:59] [Server thread/INFO] [spark/]: Starting background profiler...
[18:22:59] [Server thread/INFO] [spark/]: The async-profiler engine is not supported for your os/arch (windows11/amd64), so the built-in Java engine will be used instead.
[18:23:10] [spark-worker-pool-1-thread-1/INFO] [minecraft/MinecraftServer]: [?] Starting a new profiler, please wait...
[18:23:10] [spark-worker-pool-1-thread-1/INFO] [spark/]: The async-profiler engine is not supported for your os/arch (windows11/amd64), so the built-in Java engine will be used instead.
[18:23:10] [spark-worker-pool-1-thread-1/INFO] [minecraft/MinecraftServer]: [?] Profiler is now running! (built-in java)
[18:23:10] [spark-worker-pool-1-thread-1/INFO] [minecraft/MinecraftServer]: [?] It will run in the background until it is stopped by an admin.
[18:23:10] [spark-worker-pool-1-thread-1/INFO] [minecraft/MinecraftServer]: [?] To stop the profiler and upload the results, run:
[18:23:10] [spark-worker-pool-1-thread-1/INFO] [minecraft/MinecraftServer]: [?]   /spark profiler stop
[18:23:10] [spark-worker-pool-1-thread-1/INFO] [minecraft/MinecraftServer]: [?] To view the profiler while it's running, run:
[18:23:10] [spark-worker-pool-1-thread-1/INFO] [minecraft/MinecraftServer]: [?]   /spark profiler open
[18:23:11] [Worker-Main-17/INFO] [noisiumed/]: noisiumed.path l0=0 l1=129 l2=121 l1_fail=0 biome=5232 surface_skip=0 l1_avg_us=290785 direct_writes=5196100 sections_touched=1460 sample_pct=54 write_pct=45 specialize=311552 nc1_s1=0 nc1_s2=256 nc1_s3=0 nc3_grid=256 nc5_ore=256 aq_spec=256 spec_deep=0 spec_nodes=256 vanilla_arith=0 l0_reasons{}
[18:23:15] [Worker-Main-25/INFO] [noisiumed/]: noisiumed.path l0=0 l1=256 l2=245 l1_fail=0 biome=8808 surface_skip=0 l1_avg_us=181284 direct_writes=9099515 sections_touched=2571 sample_pct=54 write_pct=45 specialize=519659 nc1_s1=0 nc1_s2=427 nc1_s3=0 nc3_grid=427 nc5_ore=427 aq_spec=427 spec_deep=0 spec_nodes=427 vanilla_arith=0 l0_reasons{}
[18:23:18] [Worker-Main-10/INFO] [noisiumed/]: noisiumed.path l0=0 l1=384 l2=372 l1_fail=0 biome=12408 surface_skip=0 l1_avg_us=155259 direct_writes=12696989 sections_touched=3661 sample_pct=54 write_pct=45 specialize=719247 nc1_s1=0 nc1_s2=591 nc1_s3=0 nc3_grid=591 nc5_ore=591 aq_spec=591 spec_deep=0 spec_nodes=591 vanilla_arith=0 l0_reasons{}
[18:23:21] [Worker-Main-12/INFO] [noisiumed/]: noisiumed.path l0=0 l1=512 l2=506 l1_fail=0 biome=15696 surface_skip=0 l1_avg_us=131592 direct_writes=16542805 sections_touched=4748 sample_pct=59 write_pct=40 specialize=901797 nc1_s1=0 nc1_s2=741 nc1_s3=0 nc3_grid=741 nc5_ore=741 aq_spec=741 spec_deep=0 spec_nodes=741 vanilla_arith=0 l0_reasons{}
[18:23:24] [Server thread/INFO] [minecraft/MinecraftServer]: [Rcon: Marked 225 chunks in Overworld from [121, 121] to [135, 135] to be force loaded]
[18:23:25] [Worker-Main-26/INFO] [noisiumed/]: noisiumed.path l0=0 l1=640 l2=625 l1_fail=0 biome=19056 surface_skip=0 l1_avg_us=115533 direct_writes=20512243 sections_touched=5834 sample_pct=63 write_pct=36 specialize=1271765 nc1_s1=0 nc1_s2=1045 nc1_s3=0 nc3_grid=1045 nc5_ore=1045 aq_spec=1045 spec_deep=0 spec_nodes=1045 vanilla_arith=0 l0_reasons{}
[18:23:28] [Worker-Main-13/INFO] [noisiumed/]: noisiumed.path l0=0 l1=768 l2=758 l1_fail=0 biome=23280 surface_skip=0 l1_avg_us=110494 direct_writes=24615769 sections_touched=6930 sample_pct=64 write_pct=35 specialize=1530986 nc1_s1=0 nc1_s2=1258 nc1_s3=0 nc3_grid=1258 nc5_ore=1258 aq_spec=1258 spec_deep=0 spec_nodes=1258 vanilla_arith=0 l0_reasons{}
[18:23:31] [Worker-Main-3/INFO] [noisiumed/]: noisiumed.path l0=0 l1=896 l2=872 l1_fail=0 biome=26856 surface_skip=0 l1_avg_us=103447 direct_writes=28777082 sections_touched=8048 sample_pct=67 write_pct=32 specialize=1722055 nc1_s1=0 nc1_s2=1415 nc1_s3=0 nc3_grid=1415 nc5_ore=1415 aq_spec=1415 spec_deep=0 spec_nodes=1415 vanilla_arith=0 l0_reasons{}
[18:23:34] [Worker-Main-14/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1024 l2=1007 l1_fail=0 biome=30456 surface_skip=0 l1_avg_us=97117 direct_writes=32958803 sections_touched=9214 sample_pct=67 write_pct=32 specialize=1920426 nc1_s1=0 nc1_s2=1578 nc1_s3=0 nc3_grid=1578 nc5_ore=1578 aq_spec=1578 spec_deep=0 spec_nodes=1578 vanilla_arith=0 l0_reasons{}
[18:23:37] [Server thread/INFO] [minecraft/MinecraftServer]: [Rcon: Marked 225 chunks in Overworld from [153, 121] to [167, 135] to be force loaded]
[18:23:37] [Worker-Main-29/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1152 l2=1145 l1_fail=0 biome=33432 surface_skip=0 l1_avg_us=91380 direct_writes=37298948 sections_touched=10396 sample_pct=67 write_pct=32 specialize=2083504 nc1_s1=0 nc1_s2=1712 nc1_s3=0 nc3_grid=1712 nc5_ore=1712 aq_spec=1712 spec_deep=0 spec_nodes=1712 vanilla_arith=0 l0_reasons{}
[18:23:40] [Worker-Main-6/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1280 l2=1268 l1_fail=0 biome=37752 surface_skip=0 l1_avg_us=86851 direct_writes=41413783 sections_touched=11480 sample_pct=68 write_pct=31 specialize=2315951 nc1_s1=0 nc1_s2=1903 nc1_s3=0 nc3_grid=1903 nc5_ore=1903 aq_spec=1903 spec_deep=0 spec_nodes=1903 vanilla_arith=0 l0_reasons{}
[18:23:42] [Worker-Main-7/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1408 l2=1379 l1_fail=0 biome=41808 surface_skip=0 l1_avg_us=83569 direct_writes=45503072 sections_touched=12659 sample_pct=68 write_pct=31 specialize=2543530 nc1_s1=0 nc1_s2=2090 nc1_s3=0 nc3_grid=2090 nc5_ore=2090 aq_spec=2090 spec_deep=0 spec_nodes=2090 vanilla_arith=0 l0_reasons{}
[18:23:46] [Worker-Main-28/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1536 l2=1530 l1_fail=0 biome=44809 surface_skip=0 l1_avg_us=80601 direct_writes=49988642 sections_touched=13920 sample_pct=70 write_pct=29 specialize=2700523 nc1_s1=0 nc1_s2=2219 nc1_s3=0 nc3_grid=2219 nc5_ore=2219 aq_spec=2219 spec_deep=0 spec_nodes=2219 vanilla_arith=0 l0_reasons{}
[18:23:48] [Server thread/INFO] [minecraft/MinecraftServer]: [Rcon: Marked 225 chunks in Overworld from [121, 153] to [135, 167] to be force loaded]
[18:23:49] [Worker-Main-2/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1664 l2=1659 l1_fail=0 biome=48408 surface_skip=0 l1_avg_us=78025 direct_writes=54226993 sections_touched=15138 sample_pct=70 write_pct=29 specialize=2890375 nc1_s1=0 nc1_s2=2375 nc1_s3=0 nc3_grid=2375 nc5_ore=2375 aq_spec=2375 spec_deep=0 spec_nodes=2375 vanilla_arith=0 l0_reasons{}
[18:23:54] [Worker-Main-27/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1792 l2=1789 l1_fail=0 biome=52248 surface_skip=0 l1_avg_us=81201 direct_writes=59090924 sections_touched=16473 sample_pct=70 write_pct=29 specialize=3451412 nc1_s1=0 nc1_s2=2836 nc1_s3=0 nc3_grid=2836 nc5_ore=2836 aq_spec=2836 spec_deep=0 spec_nodes=2836 vanilla_arith=0 l0_reasons{}
[18:23:57] [Worker-Main-11/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1920 l2=1897 l1_fail=0 biome=56352 surface_skip=0 l1_avg_us=79355 direct_writes=63532899 sections_touched=17745 sample_pct=70 write_pct=29 specialize=3658302 nc1_s1=0 nc1_s2=3006 nc1_s3=0 nc3_grid=3006 nc5_ore=3006 aq_spec=3006 spec_deep=0 spec_nodes=3006 vanilla_arith=0 l0_reasons{}
[18:23:59] [Worker-Main-11/INFO] [noisiumed/]: noisiumed.path l0=0 l1=2048 l2=2028 l1_fail=0 biome=59160 surface_skip=0 l1_avg_us=77616 direct_writes=67792982 sections_touched=18951 sample_pct=71 write_pct=28 specialize=3799474 nc1_s1=0 nc1_s2=3122 nc1_s3=0 nc3_grid=3122 nc5_ore=3122 aq_spec=3122 spec_deep=0 spec_nodes=3122 vanilla_arith=0 l0_reasons{}
[18:24:03] [Worker-Main-24/INFO] [noisiumed/]: noisiumed.path l0=0 l1=2176 l2=2166 l1_fail=0 biome=63192 surface_skip=0 l1_avg_us=75980 direct_writes=72573467 sections_touched=20252 sample_pct=72 write_pct=27 specialize=4013666 nc1_s1=0 nc1_s2=3298 nc1_s3=0 nc3_grid=3298 nc5_ore=3298 aq_spec=3298 spec_deep=0 spec_nodes=3298 vanilla_arith=0 l0_reasons{}
[18:24:04] [Server thread/INFO] [minecraft/MinecraftServer]: [Rcon: Marked 225 chunks in Overworld from [153, 153] to [167, 167] to be force loaded]
[18:25:44] [spark-worker-pool-1-thread-3/INFO] [minecraft/MinecraftServer]: [?] Stopping the profiler & uploading results, please wait...
[18:25:45] [spark-worker-pool-1-thread-3/INFO] [spark/]: If you see a warning above that says "WARNING: A Java agent has been loaded dynamically", it can be safely ignored.
[18:25:45] [spark-worker-pool-1-thread-3/INFO] [spark/]: See here for more information: https://spark.lucko.me/docs/misc/Java-agent-warning
[18:25:49] [spark-worker-pool-1-thread-3/INFO] [minecraft/MinecraftServer]: [?] Profiler stopped & upload complete!
[18:25:49] [spark-worker-pool-1-thread-3/INFO] [minecraft/MinecraftServer]: https://spark.lucko.me/2P2gGBxLEi
[18:25:49] [spark-worker-pool-1-thread-3/INFO] [spark/]: The async-profiler engine is not supported for your os/arch (windows11/amd64), so the built-in Java engine will be used instead.
[18:25:49] [spark-worker-pool-1-thread-3/INFO] [minecraft/MinecraftServer]: [?] Restarted the background profiler. (If you don't want this to happen, run: /spark profiler cancel)
[18:25:50] [Server thread/INFO] [minecraft/MinecraftServer]: [Rcon: Unmarked all force loaded chunks in Overworld]
[18:25:52] [Server thread/INFO] [minecraft/MinecraftServer]: Saving chunks for level 'ServerLevel[world]'/minecraft:overworld
[18:25:55] [Server thread/INFO] [minecraft/MinecraftServer]: Saving chunks for level 'ServerLevel[world]'/minecraft:the_end
[18:25:55] [Server thread/INFO] [minecraft/MinecraftServer]: Saving chunks for level 'ServerLevel[world]'/minecraft:the_nether
[18:25:55] [Server thread/INFO] [minecraft/MinecraftServer]: ThreadedAnvilChunkStorage (world): All chunks are saved
[18:25:55] [Server thread/INFO] [minecraft/MinecraftServer]: ThreadedAnvilChunkStorage (DIM1): All chunks are saved
[18:25:55] [Server thread/INFO] [minecraft/MinecraftServer]: ThreadedAnvilChunkStorage (DIM-1): All chunks are saved
[18:25:55] [Server thread/INFO] [minecraft/MinecraftServer]: ThreadedAnvilChunkStorage: All dimensions are saved
[30Jul2026 18:22:41.899] [main/INFO] [mixin/]: SpongePowered MIXIN Subsystem Version=0.8.7 Source=union:/C:/Users/imba/Git/infernos/minecraft-mods/noisium/bench/run-noisiumed/libraries/net/fabricmc/sponge-mixin/0.15.2+mixin.0.8.7/sponge-mixin-0.15.2+mixin.0.8.7.jar%2373!/ Service=ModLauncher Env=SERVER
[30Jul2026 18:22:42.416] [main/INFO] [net.neoforged.fml.loading.moddiscovery.ModDiscoverer/SCAN]: Found mod file "noisiumed-4.0.0-beta.16.5-neoforge-1.21.1.jar" [locator: {mods folder locator at C:\Users\imba\Git\infernos\minecraft-mods\noisium\bench\run-noisiumed\mods}, reader: mod manifest]
[30Jul2026 18:22:42.425] [main/INFO] [net.neoforged.fml.loading.moddiscovery.ModDiscoverer/SCAN]: Found mod file "spark-1.10.124-neoforge.jar" [locator: {mods folder locator at C:\Users\imba\Git\infernos\minecraft-mods\noisium\bench\run-noisiumed\mods}, reader: mod manifest]
		Noisiumed 4.0.0-beta.16.5-neoforge-1.21.1 (noisiumed)
		spark 1.10.124 (spark)
[30Jul2026 18:22:54.459] [modloading-worker-0/INFO] [noisiumed/]: Loading Noisiumed (L0–L2 + density specializer + surface/coverage). Metrics: PathMetrics.snapshot()
[30Jul2026 18:22:55.391] [main/INFO] [com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService/]: Environment: Environment[sessionHost=https://sessionserver.mojang.com, servicesHost=https://api.minecraftservices.com, name=PROD]
[30Jul2026 18:22:55.472] [main/WARN] [net.minecraft.server.packs.VanillaPackResourcesBuilder/]: Assets URL 'union:/C:/Users/imba/Git/infernos/minecraft-mods/noisium/bench/run-noisiumed/libraries/net/minecraft/server/1.21.1-20240808.144430/server-1.21.1-20240808.144430-srg.jar%23119!/assets/.mcassetsroot' uses unexpected schema
[30Jul2026 18:22:55.474] [main/WARN] [net.minecraft.server.packs.VanillaPackResourcesBuilder/]: Assets URL 'union:/C:/Users/imba/Git/infernos/minecraft-mods/noisium/bench/run-noisiumed/libraries/net/minecraft/server/1.21.1-20240808.144430/server-1.21.1-20240808.144430-srg.jar%23119!/data/.mcassetsroot' uses unexpected schema
[30Jul2026 18:22:59.213] [Server thread/INFO] [spark/]: Starting background profiler...
[30Jul2026 18:22:59.231] [Server thread/INFO] [spark/]: The async-profiler engine is not supported for your os/arch (windows11/amd64), so the built-in Java engine will be used instead.
[30Jul2026 18:23:10.681] [spark-worker-pool-1-thread-1/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] Starting a new profiler, please wait...
[30Jul2026 18:23:10.682] [spark-worker-pool-1-thread-1/INFO] [spark/]: The async-profiler engine is not supported for your os/arch (windows11/amd64), so the built-in Java engine will be used instead.
[30Jul2026 18:23:10.685] [spark-worker-pool-1-thread-1/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] Profiler is now running! (built-in java)
[30Jul2026 18:23:10.686] [spark-worker-pool-1-thread-1/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] It will run in the background until it is stopped by an admin.
[30Jul2026 18:23:10.686] [spark-worker-pool-1-thread-1/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] To stop the profiler and upload the results, run:
[30Jul2026 18:23:10.689] [spark-worker-pool-1-thread-1/INFO] [net.minecraft.server.MinecraftServer/]: [⚡]   /spark profiler stop
[30Jul2026 18:23:10.690] [spark-worker-pool-1-thread-1/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] To view the profiler while it's running, run:
[30Jul2026 18:23:10.691] [spark-worker-pool-1-thread-1/INFO] [net.minecraft.server.MinecraftServer/]: [⚡]   /spark profiler open
[30Jul2026 18:23:11.976] [Worker-Main-17/INFO] [noisiumed/]: noisiumed.path l0=0 l1=129 l2=121 l1_fail=0 biome=5232 surface_skip=0 l1_avg_us=290785 direct_writes=5196100 sections_touched=1460 sample_pct=54 write_pct=45 specialize=311552 nc1_s1=0 nc1_s2=256 nc1_s3=0 nc3_grid=256 nc5_ore=256 aq_spec=256 spec_deep=0 spec_nodes=256 vanilla_arith=0 l0_reasons{}
[30Jul2026 18:23:15.031] [Worker-Main-25/INFO] [noisiumed/]: noisiumed.path l0=0 l1=256 l2=245 l1_fail=0 biome=8808 surface_skip=0 l1_avg_us=181284 direct_writes=9099515 sections_touched=2571 sample_pct=54 write_pct=45 specialize=519659 nc1_s1=0 nc1_s2=427 nc1_s3=0 nc3_grid=427 nc5_ore=427 aq_spec=427 spec_deep=0 spec_nodes=427 vanilla_arith=0 l0_reasons{}
[30Jul2026 18:23:18.466] [Worker-Main-10/INFO] [noisiumed/]: noisiumed.path l0=0 l1=384 l2=372 l1_fail=0 biome=12408 surface_skip=0 l1_avg_us=155259 direct_writes=12696989 sections_touched=3661 sample_pct=54 write_pct=45 specialize=719247 nc1_s1=0 nc1_s2=591 nc1_s3=0 nc3_grid=591 nc5_ore=591 aq_spec=591 spec_deep=0 spec_nodes=591 vanilla_arith=0 l0_reasons{}
[30Jul2026 18:23:21.258] [Worker-Main-12/INFO] [noisiumed/]: noisiumed.path l0=0 l1=512 l2=506 l1_fail=0 biome=15696 surface_skip=0 l1_avg_us=131592 direct_writes=16542805 sections_touched=4748 sample_pct=59 write_pct=40 specialize=901797 nc1_s1=0 nc1_s2=741 nc1_s3=0 nc3_grid=741 nc5_ore=741 aq_spec=741 spec_deep=0 spec_nodes=741 vanilla_arith=0 l0_reasons{}
[30Jul2026 18:23:24.414] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: [Rcon: Marked 225 chunks in Overworld from [121, 121] to [135, 135] to be force loaded]
[30Jul2026 18:23:25.135] [Worker-Main-26/INFO] [noisiumed/]: noisiumed.path l0=0 l1=640 l2=625 l1_fail=0 biome=19056 surface_skip=0 l1_avg_us=115533 direct_writes=20512243 sections_touched=5834 sample_pct=63 write_pct=36 specialize=1271765 nc1_s1=0 nc1_s2=1045 nc1_s3=0 nc3_grid=1045 nc5_ore=1045 aq_spec=1045 spec_deep=0 spec_nodes=1045 vanilla_arith=0 l0_reasons{}
[30Jul2026 18:23:28.879] [Worker-Main-13/INFO] [noisiumed/]: noisiumed.path l0=0 l1=768 l2=758 l1_fail=0 biome=23280 surface_skip=0 l1_avg_us=110494 direct_writes=24615769 sections_touched=6930 sample_pct=64 write_pct=35 specialize=1530986 nc1_s1=0 nc1_s2=1258 nc1_s3=0 nc3_grid=1258 nc5_ore=1258 aq_spec=1258 spec_deep=0 spec_nodes=1258 vanilla_arith=0 l0_reasons{}
[30Jul2026 18:23:31.463] [Worker-Main-3/INFO] [noisiumed/]: noisiumed.path l0=0 l1=896 l2=872 l1_fail=0 biome=26856 surface_skip=0 l1_avg_us=103447 direct_writes=28777082 sections_touched=8048 sample_pct=67 write_pct=32 specialize=1722055 nc1_s1=0 nc1_s2=1415 nc1_s3=0 nc3_grid=1415 nc5_ore=1415 aq_spec=1415 spec_deep=0 spec_nodes=1415 vanilla_arith=0 l0_reasons{}
[30Jul2026 18:23:34.483] [Worker-Main-14/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1024 l2=1007 l1_fail=0 biome=30456 surface_skip=0 l1_avg_us=97117 direct_writes=32958803 sections_touched=9214 sample_pct=67 write_pct=32 specialize=1920426 nc1_s1=0 nc1_s2=1578 nc1_s3=0 nc3_grid=1578 nc5_ore=1578 aq_spec=1578 spec_deep=0 spec_nodes=1578 vanilla_arith=0 l0_reasons{}
[30Jul2026 18:23:37.406] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: [Rcon: Marked 225 chunks in Overworld from [153, 121] to [167, 135] to be force loaded]
[30Jul2026 18:23:37.476] [Worker-Main-29/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1152 l2=1145 l1_fail=0 biome=33432 surface_skip=0 l1_avg_us=91380 direct_writes=37298948 sections_touched=10396 sample_pct=67 write_pct=32 specialize=2083504 nc1_s1=0 nc1_s2=1712 nc1_s3=0 nc3_grid=1712 nc5_ore=1712 aq_spec=1712 spec_deep=0 spec_nodes=1712 vanilla_arith=0 l0_reasons{}
[30Jul2026 18:23:40.142] [Worker-Main-6/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1280 l2=1268 l1_fail=0 biome=37752 surface_skip=0 l1_avg_us=86851 direct_writes=41413783 sections_touched=11480 sample_pct=68 write_pct=31 specialize=2315951 nc1_s1=0 nc1_s2=1903 nc1_s3=0 nc3_grid=1903 nc5_ore=1903 aq_spec=1903 spec_deep=0 spec_nodes=1903 vanilla_arith=0 l0_reasons{}
[30Jul2026 18:23:42.614] [Worker-Main-7/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1408 l2=1379 l1_fail=0 biome=41808 surface_skip=0 l1_avg_us=83569 direct_writes=45503072 sections_touched=12659 sample_pct=68 write_pct=31 specialize=2543530 nc1_s1=0 nc1_s2=2090 nc1_s3=0 nc3_grid=2090 nc5_ore=2090 aq_spec=2090 spec_deep=0 spec_nodes=2090 vanilla_arith=0 l0_reasons{}
[30Jul2026 18:23:46.225] [Worker-Main-28/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1536 l2=1530 l1_fail=0 biome=44809 surface_skip=0 l1_avg_us=80601 direct_writes=49988642 sections_touched=13920 sample_pct=70 write_pct=29 specialize=2700523 nc1_s1=0 nc1_s2=2219 nc1_s3=0 nc3_grid=2219 nc5_ore=2219 aq_spec=2219 spec_deep=0 spec_nodes=2219 vanilla_arith=0 l0_reasons{}
[30Jul2026 18:23:48.931] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: [Rcon: Marked 225 chunks in Overworld from [121, 153] to [135, 167] to be force loaded]
[30Jul2026 18:23:49.002] [Worker-Main-2/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1664 l2=1659 l1_fail=0 biome=48408 surface_skip=0 l1_avg_us=78025 direct_writes=54226993 sections_touched=15138 sample_pct=70 write_pct=29 specialize=2890375 nc1_s1=0 nc1_s2=2375 nc1_s3=0 nc3_grid=2375 nc5_ore=2375 aq_spec=2375 spec_deep=0 spec_nodes=2375 vanilla_arith=0 l0_reasons{}
[30Jul2026 18:23:54.514] [Worker-Main-27/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1792 l2=1789 l1_fail=0 biome=52248 surface_skip=0 l1_avg_us=81201 direct_writes=59090924 sections_touched=16473 sample_pct=70 write_pct=29 specialize=3451412 nc1_s1=0 nc1_s2=2836 nc1_s3=0 nc3_grid=2836 nc5_ore=2836 aq_spec=2836 spec_deep=0 spec_nodes=2836 vanilla_arith=0 l0_reasons{}
[30Jul2026 18:23:57.270] [Worker-Main-11/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1920 l2=1897 l1_fail=0 biome=56352 surface_skip=0 l1_avg_us=79355 direct_writes=63532899 sections_touched=17745 sample_pct=70 write_pct=29 specialize=3658302 nc1_s1=0 nc1_s2=3006 nc1_s3=0 nc3_grid=3006 nc5_ore=3006 aq_spec=3006 spec_deep=0 spec_nodes=3006 vanilla_arith=0 l0_reasons{}
[30Jul2026 18:23:59.961] [Worker-Main-11/INFO] [noisiumed/]: noisiumed.path l0=0 l1=2048 l2=2028 l1_fail=0 biome=59160 surface_skip=0 l1_avg_us=77616 direct_writes=67792982 sections_touched=18951 sample_pct=71 write_pct=28 specialize=3799474 nc1_s1=0 nc1_s2=3122 nc1_s3=0 nc3_grid=3122 nc5_ore=3122 aq_spec=3122 spec_deep=0 spec_nodes=3122 vanilla_arith=0 l0_reasons{}
[30Jul2026 18:24:03.602] [Worker-Main-24/INFO] [noisiumed/]: noisiumed.path l0=0 l1=2176 l2=2166 l1_fail=0 biome=63192 surface_skip=0 l1_avg_us=75980 direct_writes=72573467 sections_touched=20252 sample_pct=72 write_pct=27 specialize=4013666 nc1_s1=0 nc1_s2=3298 nc1_s3=0 nc3_grid=3298 nc5_ore=3298 aq_spec=3298 spec_deep=0 spec_nodes=3298 vanilla_arith=0 l0_reasons{}
[30Jul2026 18:24:04.235] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: [Rcon: Marked 225 chunks in Overworld from [153, 153] to [167, 167] to be force loaded]
[30Jul2026 18:25:44.987] [spark-worker-pool-1-thread-3/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] Stopping the profiler & uploading results, please wait...
[30Jul2026 18:25:45.983] [spark-worker-pool-1-thread-3/INFO] [spark/]: If you see a warning above that says "WARNING: A Java agent has been loaded dynamically", it can be safely ignored.
[30Jul2026 18:25:45.984] [spark-worker-pool-1-thread-3/INFO] [spark/]: See here for more information: https://spark.lucko.me/docs/misc/Java-agent-warning
[30Jul2026 18:25:49.567] [spark-worker-pool-1-thread-3/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] Profiler stopped & upload complete!
[30Jul2026 18:25:49.567] [spark-worker-pool-1-thread-3/INFO] [net.minecraft.server.MinecraftServer/]: https://spark.lucko.me/2P2gGBxLEi
[30Jul2026 18:25:49.570] [spark-worker-pool-1-thread-3/INFO] [spark/]: The async-profiler engine is not supported for your os/arch (windows11/amd64), so the built-in Java engine will be used instead.
[30Jul2026 18:25:49.572] [spark-worker-pool-1-thread-3/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] Restarted the background profiler. (If you don't want this to happen, run: /spark profiler cancel)
[30Jul2026 18:25:50.294] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: [Rcon: Unmarked all force loaded chunks in Overworld]
[30Jul2026 18:25:52.209] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: Saving chunks for level 'ServerLevel[world]'/minecraft:overworld
[30Jul2026 18:25:55.668] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: Saving chunks for level 'ServerLevel[world]'/minecraft:the_end
[30Jul2026 18:25:55.687] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: Saving chunks for level 'ServerLevel[world]'/minecraft:the_nether
[30Jul2026 18:25:55.718] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: ThreadedAnvilChunkStorage (world): All chunks are saved
[30Jul2026 18:25:55.718] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: ThreadedAnvilChunkStorage (DIM1): All chunks are saved
[30Jul2026 18:25:55.718] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: ThreadedAnvilChunkStorage (DIM-1): All chunks are saved
[30Jul2026 18:25:55.718] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: ThreadedAnvilChunkStorage: All dimensions are saved

```

## Fast Noise 1.0.13
```
label=fastnoise
seed=12345 radiusChunks=7 profileSeconds=100 port=25571
wall_boot_ms=28763 wall_forceload_ms=48174 wall_profile_window_ms=148303 wall_total_ms=190701
spark_url=https://spark.lucko.me/KlNHWgtyB1
spark_bin=C:\Users\imba\Git\infernos\minecraft-mods\noisium\bench\results\KlNHWgtyB1.bin
--- path metrics (from log) ---
--- rcon spark profiler stop ---

--- rcon spark tps ---

--- rcon spark health ---

--- matched log lines ---
[18:26:03.834] [main/INFO] [mixin/]: SpongePowered MIXIN Subsystem Version=0.8.7 Source=union:/C:/Users/imba/Git/infernos/minecraft-mods/noisium/bench/run-fastnoise/libraries/net/fabricmc/sponge-mixin/0.15.2+mixin.0.8.7/sponge-mixin-0.15.2+mixin.0.8.7.jar%2373!/ Service=ModLauncher Env=SERVER 
[18:26:04.398] [main/INFO] [loading.moddiscovery.ModDiscoverer/SCAN]: Found mod file "spark-1.10.124-neoforge.jar" [locator: {mods folder locator at C:\Users\imba\Git\infernos\minecraft-mods\noisium\bench\run-fastnoise\mods}, reader: mod manifest] 
[18:26:04.409] [main/INFO] [loading.moddiscovery.ModDiscoverer/SCAN]: Found mod file "zfastnoise-1.0.13+1.21.1+neoforge.jar" [locator: {mods folder locator at C:\Users\imba\Git\infernos\minecraft-mods\noisium\bench\run-fastnoise\mods}, reader: mod manifest] 
		Fast Noise Mod 1.0.13+1.21.1+neoforge (zfastnoise)
		spark 1.10.124 (spark) 
[18:26:16] [modloading-worker-0/INFO] [zfastnoise/]: Hello Fabric world!
[18:26:17] [main/INFO] [mojang/YggdrasilAuthenticationService]: Environment: Environment[sessionHost=https://sessionserver.mojang.com, servicesHost=https://api.minecraftservices.com, name=PROD]
[18:26:17] [main/WARN] [minecraft/VanillaPackResourcesBuilder]: Assets URL 'union:/C:/Users/imba/Git/infernos/minecraft-mods/noisium/bench/run-fastnoise/libraries/net/minecraft/server/1.21.1-20240808.144430/server-1.21.1-20240808.144430-srg.jar%23119!/assets/.mcassetsroot' uses unexpected schema
[18:26:17] [main/WARN] [minecraft/VanillaPackResourcesBuilder]: Assets URL 'union:/C:/Users/imba/Git/infernos/minecraft-mods/noisium/bench/run-fastnoise/libraries/net/minecraft/server/1.21.1-20240808.144430/server-1.21.1-20240808.144430-srg.jar%23119!/data/.mcassetsroot' uses unexpected schema
[18:26:20] [Server thread/INFO] [spark/]: Starting background profiler...
[18:26:20] [Server thread/INFO] [spark/]: The async-profiler engine is not supported for your os/arch (windows11/amd64), so the built-in Java engine will be used instead.
[18:26:32] [spark-worker-pool-1-thread-1/INFO] [minecraft/MinecraftServer]: [?] Starting a new profiler, please wait...
[18:26:32] [spark-worker-pool-1-thread-1/INFO] [spark/]: The async-profiler engine is not supported for your os/arch (windows11/amd64), so the built-in Java engine will be used instead.
[18:26:32] [spark-worker-pool-1-thread-1/INFO] [minecraft/MinecraftServer]: [?] Profiler is now running! (built-in java)
[18:26:32] [spark-worker-pool-1-thread-1/INFO] [minecraft/MinecraftServer]: [?] It will run in the background until it is stopped by an admin.
[18:26:32] [spark-worker-pool-1-thread-1/INFO] [minecraft/MinecraftServer]: [?] To stop the profiler and upload the results, run:
[18:26:32] [spark-worker-pool-1-thread-1/INFO] [minecraft/MinecraftServer]: [?]   /spark profiler stop
[18:26:32] [spark-worker-pool-1-thread-1/INFO] [minecraft/MinecraftServer]: [?] To view the profiler while it's running, run:
[18:26:32] [spark-worker-pool-1-thread-1/INFO] [minecraft/MinecraftServer]: [?]   /spark profiler open
[18:26:43] [Server thread/INFO] [minecraft/MinecraftServer]: [Rcon: Marked 225 chunks in Overworld from [121, 121] to [135, 135] to be force loaded]
[18:26:55] [Server thread/INFO] [minecraft/MinecraftServer]: [Rcon: Marked 225 chunks in Overworld from [153, 121] to [167, 135] to be force loaded]
[18:27:05] [spark-java-sampler-1-4/WARN] [spark/]: Timed out waiting for world statistics
[18:27:06] [Server thread/INFO] [minecraft/MinecraftServer]: [Rcon: Marked 225 chunks in Overworld from [121, 153] to [135, 167] to be force loaded]
[18:27:19] [Server thread/INFO] [minecraft/MinecraftServer]: [Rcon: Marked 225 chunks in Overworld from [153, 153] to [167, 167] to be force loaded]
[18:29:00] [spark-worker-pool-1-thread-3/INFO] [minecraft/MinecraftServer]: [?] Stopping the profiler & uploading results, please wait...
[18:29:01] [spark-worker-pool-1-thread-3/INFO] [spark/]: If you see a warning above that says "WARNING: A Java agent has been loaded dynamically", it can be safely ignored.
[18:29:01] [spark-worker-pool-1-thread-3/INFO] [spark/]: See here for more information: https://spark.lucko.me/docs/misc/Java-agent-warning
[18:29:04] [spark-worker-pool-1-thread-3/INFO] [minecraft/MinecraftServer]: [?] Profiler stopped & upload complete!
[18:29:04] [spark-worker-pool-1-thread-3/INFO] [minecraft/MinecraftServer]: https://spark.lucko.me/KlNHWgtyB1
[18:29:04] [spark-worker-pool-1-thread-3/INFO] [spark/]: The async-profiler engine is not supported for your os/arch (windows11/amd64), so the built-in Java engine will be used instead.
[18:29:04] [spark-worker-pool-1-thread-3/INFO] [minecraft/MinecraftServer]: [?] Restarted the background profiler. (If you don't want this to happen, run: /spark profiler cancel)
[18:29:05] [Server thread/INFO] [minecraft/MinecraftServer]: [Rcon: Unmarked all force loaded chunks in Overworld]
[18:29:07] [Server thread/INFO] [minecraft/MinecraftServer]: Saving chunks for level 'ServerLevel[world]'/minecraft:overworld
[18:29:10] [Server thread/INFO] [minecraft/MinecraftServer]: Saving chunks for level 'ServerLevel[world]'/minecraft:the_nether
[18:29:10] [Server thread/INFO] [minecraft/MinecraftServer]: Saving chunks for level 'ServerLevel[world]'/minecraft:the_end
[18:29:10] [Server thread/INFO] [minecraft/MinecraftServer]: ThreadedAnvilChunkStorage (world): All chunks are saved
[18:29:10] [Server thread/INFO] [minecraft/MinecraftServer]: ThreadedAnvilChunkStorage (DIM-1): All chunks are saved
[18:29:10] [Server thread/INFO] [minecraft/MinecraftServer]: ThreadedAnvilChunkStorage (DIM1): All chunks are saved
[18:29:10] [Server thread/INFO] [minecraft/MinecraftServer]: ThreadedAnvilChunkStorage: All dimensions are saved
[30Jul2026 18:26:03.834] [main/INFO] [mixin/]: SpongePowered MIXIN Subsystem Version=0.8.7 Source=union:/C:/Users/imba/Git/infernos/minecraft-mods/noisium/bench/run-fastnoise/libraries/net/fabricmc/sponge-mixin/0.15.2+mixin.0.8.7/sponge-mixin-0.15.2+mixin.0.8.7.jar%2373!/ Service=ModLauncher Env=SERVER
[30Jul2026 18:26:04.398] [main/INFO] [net.neoforged.fml.loading.moddiscovery.ModDiscoverer/SCAN]: Found mod file "spark-1.10.124-neoforge.jar" [locator: {mods folder locator at C:\Users\imba\Git\infernos\minecraft-mods\noisium\bench\run-fastnoise\mods}, reader: mod manifest]
[30Jul2026 18:26:04.409] [main/INFO] [net.neoforged.fml.loading.moddiscovery.ModDiscoverer/SCAN]: Found mod file "zfastnoise-1.0.13+1.21.1+neoforge.jar" [locator: {mods folder locator at C:\Users\imba\Git\infernos\minecraft-mods\noisium\bench\run-fastnoise\mods}, reader: mod manifest]
		Fast Noise Mod 1.0.13+1.21.1+neoforge (zfastnoise)
		spark 1.10.124 (spark)
[30Jul2026 18:26:16.940] [modloading-worker-0/INFO] [zfastnoise/]: Hello Fabric world!
[30Jul2026 18:26:17.200] [main/INFO] [com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService/]: Environment: Environment[sessionHost=https://sessionserver.mojang.com, servicesHost=https://api.minecraftservices.com, name=PROD]
[30Jul2026 18:26:17.327] [main/WARN] [net.minecraft.server.packs.VanillaPackResourcesBuilder/]: Assets URL 'union:/C:/Users/imba/Git/infernos/minecraft-mods/noisium/bench/run-fastnoise/libraries/net/minecraft/server/1.21.1-20240808.144430/server-1.21.1-20240808.144430-srg.jar%23119!/assets/.mcassetsroot' uses unexpected schema
[30Jul2026 18:26:17.327] [main/WARN] [net.minecraft.server.packs.VanillaPackResourcesBuilder/]: Assets URL 'union:/C:/Users/imba/Git/infernos/minecraft-mods/noisium/bench/run-fastnoise/libraries/net/minecraft/server/1.21.1-20240808.144430/server-1.21.1-20240808.144430-srg.jar%23119!/data/.mcassetsroot' uses unexpected schema
[30Jul2026 18:26:20.714] [Server thread/INFO] [spark/]: Starting background profiler...
[30Jul2026 18:26:20.745] [Server thread/INFO] [spark/]: The async-profiler engine is not supported for your os/arch (windows11/amd64), so the built-in Java engine will be used instead.
[30Jul2026 18:26:32.204] [spark-worker-pool-1-thread-1/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] Starting a new profiler, please wait...
[30Jul2026 18:26:32.204] [spark-worker-pool-1-thread-1/INFO] [spark/]: The async-profiler engine is not supported for your os/arch (windows11/amd64), so the built-in Java engine will be used instead.
[30Jul2026 18:26:32.208] [spark-worker-pool-1-thread-1/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] Profiler is now running! (built-in java)
[30Jul2026 18:26:32.208] [spark-worker-pool-1-thread-1/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] It will run in the background until it is stopped by an admin.
[30Jul2026 18:26:32.209] [spark-worker-pool-1-thread-1/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] To stop the profiler and upload the results, run:
[30Jul2026 18:26:32.212] [spark-worker-pool-1-thread-1/INFO] [net.minecraft.server.MinecraftServer/]: [⚡]   /spark profiler stop
[30Jul2026 18:26:32.213] [spark-worker-pool-1-thread-1/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] To view the profiler while it's running, run:
[30Jul2026 18:26:32.214] [spark-worker-pool-1-thread-1/INFO] [net.minecraft.server.MinecraftServer/]: [⚡]   /spark profiler open
[30Jul2026 18:26:43.916] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: [Rcon: Marked 225 chunks in Overworld from [121, 121] to [135, 135] to be force loaded]
[30Jul2026 18:26:55.088] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: [Rcon: Marked 225 chunks in Overworld from [153, 121] to [167, 135] to be force loaded]
[30Jul2026 18:27:05.009] [spark-java-sampler-1-4/WARN] [spark/]: Timed out waiting for world statistics
[30Jul2026 18:27:06.457] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: [Rcon: Marked 225 chunks in Overworld from [121, 153] to [135, 167] to be force loaded]
[30Jul2026 18:27:19.839] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: [Rcon: Marked 225 chunks in Overworld from [153, 153] to [167, 167] to be force loaded]
[30Jul2026 18:29:00.574] [spark-worker-pool-1-thread-3/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] Stopping the profiler & uploading results, please wait...
[30Jul2026 18:29:01.376] [spark-worker-pool-1-thread-3/INFO] [spark/]: If you see a warning above that says "WARNING: A Java agent has been loaded dynamically", it can be safely ignored.
[30Jul2026 18:29:01.376] [spark-worker-pool-1-thread-3/INFO] [spark/]: See here for more information: https://spark.lucko.me/docs/misc/Java-agent-warning
[30Jul2026 18:29:04.674] [spark-worker-pool-1-thread-3/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] Profiler stopped & upload complete!
[30Jul2026 18:29:04.674] [spark-worker-pool-1-thread-3/INFO] [net.minecraft.server.MinecraftServer/]: https://spark.lucko.me/KlNHWgtyB1
[30Jul2026 18:29:04.677] [spark-worker-pool-1-thread-3/INFO] [spark/]: The async-profiler engine is not supported for your os/arch (windows11/amd64), so the built-in Java engine will be used instead.
[30Jul2026 18:29:04.679] [spark-worker-pool-1-thread-3/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] Restarted the background profiler. (If you don't want this to happen, run: /spark profiler cancel)
[30Jul2026 18:29:05.871] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: [Rcon: Unmarked all force loaded chunks in Overworld]
[30Jul2026 18:29:07.458] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: Saving chunks for level 'ServerLevel[world]'/minecraft:overworld
[30Jul2026 18:29:10.895] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: Saving chunks for level 'ServerLevel[world]'/minecraft:the_nether
[30Jul2026 18:29:10.917] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: Saving chunks for level 'ServerLevel[world]'/minecraft:the_end
[30Jul2026 18:29:10.945] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: ThreadedAnvilChunkStorage (world): All chunks are saved
[30Jul2026 18:29:10.945] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: ThreadedAnvilChunkStorage (DIM-1): All chunks are saved
[30Jul2026 18:29:10.945] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: ThreadedAnvilChunkStorage (DIM1): All chunks are saved
[30Jul2026 18:29:10.945] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: ThreadedAnvilChunkStorage: All dimensions are saved

```


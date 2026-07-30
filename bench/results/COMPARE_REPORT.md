# Noisiumed vs Fast Noise - Spark worldgen compare
Date: 2026-07-30T20:47:01.6498887+02:00
Platform: NeoForge 21.1.233 / MC 1.21.1 (dedicated, nogui, RCON)
Seed=12345 forceload radius=7 profile=100s Xmx=4G
Baseline mods: Spark + candidate only
Huge-win metrics: wall_boot_ms / wall_forceload_ms / wall_profile_window_ms / wall_total_ms + PathMetrics

**Scope:** Primary **1.21.1**. A/B = **noisiumed-4.0.0-beta.16.5-w1-neoforge-1.21.1.jar** vs Fast Noise 1.0.13.

## Noisiumed
```
label=noisiumed
seed=12345 radiusChunks=7 profileSeconds=100 port=25570
wall_boot_ms=24439 wall_forceload_ms=40509 wall_profile_window_ms=140645 wall_total_ms=177808
spark_url=https://spark.lucko.me/q3qNue01Im
spark_bin=C:\Users\imba\Git\infernos\minecraft-mods\noisium\bench\results\q3qNue01Im.bin
--- path metrics (from log) ---
[30Jul2026 20:48:05.172] [Worker-Main-4/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1920 l2=1897 l1_fail=0 biome=56136 surface_skip=0 l1_avg_us=70181 direct_writes=63557024 sections_touched=17748 sample_pct=95 write_pct=4 specialize=3644915 nc1_s1=0 nc1_s2=2995 nc1_s3=0 nc3_grid=2995 nc5_ore=2995 aq_spec=2995 spec_deep=0 spec_nodes=2995 vanilla_arith=0 l0_reasons{}
[30Jul2026 20:48:07.486] [Worker-Main-26/INFO] [noisiumed/]: noisiumed.path l0=0 l1=2048 l2=2045 l1_fail=0 biome=59784 surface_skip=0 l1_avg_us=68724 direct_writes=67792982 sections_touched=18951 sample_pct=95 write_pct=4 specialize=3832333 nc1_s1=0 nc1_s2=3149 nc1_s3=0 nc3_grid=3149 nc5_ore=3149 aq_spec=3149 spec_deep=0 spec_nodes=3149 vanilla_arith=0 l0_reasons{}
[30Jul2026 20:48:09.831] [Worker-Main-16/INFO] [noisiumed/]: noisiumed.path l0=0 l1=2176 l2=2165 l1_fail=0 biome=62832 surface_skip=0 l1_avg_us=66668 direct_writes=72625780 sections_touched=20265 sample_pct=95 write_pct=4 specialize=3995411 nc1_s1=0 nc1_s2=3283 nc1_s3=0 nc3_grid=3283 nc5_ore=3283 aq_spec=3283 spec_deep=0 spec_nodes=3283 vanilla_arith=0 l0_reasons{}
--- rcon spark profiler stop ---

https://spark.lucko.me/q3qNue01Im
--- rcon spark tps ---

--- rcon spark health ---

--- matched log lines ---
[20:47:06.502] [main/INFO] [mixin/]: SpongePowered MIXIN Subsystem Version=0.8.7 Source=union:/C:/Users/imba/Git/infernos/minecraft-mods/noisium/bench/run-noisiumed/libraries/net/fabricmc/sponge-mixin/0.15.2+mixin.0.8.7/sponge-mixin-0.15.2+mixin.0.8.7.jar%2373!/ Service=ModLauncher Env=SERVER 
[20:47:07.029] [main/INFO] [loading.moddiscovery.ModDiscoverer/SCAN]: Found mod file "noisiumed-4.0.0-beta.16.5-w1-neoforge-1.21.1.jar" [locator: {mods folder locator at C:\Users\imba\Git\infernos\minecraft-mods\noisium\bench\run-noisiumed\mods}, reader: mod manifest] 
[20:47:07.037] [main/INFO] [loading.moddiscovery.ModDiscoverer/SCAN]: Found mod file "spark-1.10.124-neoforge.jar" [locator: {mods folder locator at C:\Users\imba\Git\infernos\minecraft-mods\noisium\bench\run-noisiumed\mods}, reader: mod manifest] 
		Noisiumed 4.0.0-beta.16.5-w1-neoforge-1.21.1 (noisiumed)
		spark 1.10.124 (spark) 
[20:47:17] [modloading-worker-0/INFO] [noisiumed/]: Loading Noisiumed (L0�L2 + density specializer + surface/coverage). Metrics: PathMetrics.snapshot()
[20:47:17] [main/INFO] [mojang/YggdrasilAuthenticationService]: Environment: Environment[sessionHost=https://sessionserver.mojang.com, servicesHost=https://api.minecraftservices.com, name=PROD]
[20:47:17] [main/WARN] [minecraft/VanillaPackResourcesBuilder]: Assets URL 'union:/C:/Users/imba/Git/infernos/minecraft-mods/noisium/bench/run-noisiumed/libraries/net/minecraft/server/1.21.1-20240808.144430/server-1.21.1-20240808.144430-srg.jar%23119!/assets/.mcassetsroot' uses unexpected schema
[20:47:17] [main/WARN] [minecraft/VanillaPackResourcesBuilder]: Assets URL 'union:/C:/Users/imba/Git/infernos/minecraft-mods/noisium/bench/run-noisiumed/libraries/net/minecraft/server/1.21.1-20240808.144430/server-1.21.1-20240808.144430-srg.jar%23119!/data/.mcassetsroot' uses unexpected schema
[20:47:20] [Server thread/INFO] [spark/]: Starting background profiler...
[20:47:20] [Server thread/INFO] [spark/]: The async-profiler engine is not supported for your os/arch (windows11/amd64), so the built-in Java engine will be used instead.
[20:47:30] [spark-worker-pool-1-thread-1/INFO] [minecraft/MinecraftServer]: [?] Starting a new profiler, please wait...
[20:47:30] [spark-worker-pool-1-thread-1/INFO] [spark/]: The async-profiler engine is not supported for your os/arch (windows11/amd64), so the built-in Java engine will be used instead.
[20:47:30] [spark-worker-pool-1-thread-1/INFO] [minecraft/MinecraftServer]: [?] Profiler is now running! (built-in java)
[20:47:30] [spark-worker-pool-1-thread-1/INFO] [minecraft/MinecraftServer]: [?] It will run in the background until it is stopped by an admin.
[20:47:30] [spark-worker-pool-1-thread-1/INFO] [minecraft/MinecraftServer]: [?] To stop the profiler and upload the results, run:
[20:47:30] [spark-worker-pool-1-thread-1/INFO] [minecraft/MinecraftServer]: [?]   /spark profiler stop
[20:47:30] [spark-worker-pool-1-thread-1/INFO] [minecraft/MinecraftServer]: [?] To view the profiler while it's running, run:
[20:47:30] [spark-worker-pool-1-thread-1/INFO] [minecraft/MinecraftServer]: [?]   /spark profiler open
[20:47:31] [Worker-Main-3/INFO] [noisiumed/]: noisiumed.path l0=0 l1=128 l2=121 l1_fail=0 biome=5232 surface_skip=0 l1_avg_us=296054 direct_writes=5168420 sections_touched=1452 sample_pct=94 write_pct=5 specialize=311552 nc1_s1=0 nc1_s2=256 nc1_s3=0 nc3_grid=256 nc5_ore=256 aq_spec=256 spec_deep=0 spec_nodes=256 vanilla_arith=0 l0_reasons{}
[20:47:33] [Worker-Main-4/INFO] [noisiumed/]: noisiumed.path l0=0 l1=256 l2=247 l1_fail=0 biome=8808 surface_skip=0 l1_avg_us=175997 direct_writes=9104234 sections_touched=2570 sample_pct=94 write_pct=5 specialize=519659 nc1_s1=0 nc1_s2=427 nc1_s3=0 nc3_grid=427 nc5_ore=427 aq_spec=427 spec_deep=0 spec_nodes=427 vanilla_arith=0 l0_reasons{}
[20:47:36] [Worker-Main-2/INFO] [noisiumed/]: noisiumed.path l0=0 l1=384 l2=371 l1_fail=0 biome=12432 surface_skip=0 l1_avg_us=145034 direct_writes=12737837 sections_touched=3658 sample_pct=94 write_pct=5 specialize=720464 nc1_s1=0 nc1_s2=592 nc1_s3=0 nc3_grid=592 nc5_ore=592 aq_spec=592 spec_deep=0 spec_nodes=592 vanilla_arith=0 l0_reasons{}
[20:47:38] [Worker-Main-29/INFO] [noisiumed/]: noisiumed.path l0=0 l1=512 l2=511 l1_fail=0 biome=16056 surface_skip=0 l1_avg_us=119827 direct_writes=16542781 sections_touched=4748 sample_pct=94 write_pct=5 specialize=921269 nc1_s1=0 nc1_s2=757 nc1_s3=0 nc3_grid=757 nc5_ore=757 aq_spec=757 spec_deep=0 spec_nodes=757 vanilla_arith=0 l0_reasons{}
[20:47:40] [Server thread/INFO] [minecraft/MinecraftServer]: [Rcon: Marked 225 chunks in Overworld from [121, 121] to [135, 135] to be force loaded]
[20:47:41] [Worker-Main-16/INFO] [noisiumed/]: noisiumed.path l0=0 l1=640 l2=628 l1_fail=0 biome=19056 surface_skip=0 l1_avg_us=105327 direct_writes=20505545 sections_touched=5835 sample_pct=94 write_pct=5 specialize=1271765 nc1_s1=0 nc1_s2=1045 nc1_s3=0 nc3_grid=1045 nc5_ore=1045 aq_spec=1045 spec_deep=0 spec_nodes=1045 vanilla_arith=0 l0_reasons{}
[20:47:43] [Worker-Main-2/INFO] [noisiumed/]: noisiumed.path l0=0 l1=768 l2=758 l1_fail=0 biome=23280 surface_skip=0 l1_avg_us=100191 direct_writes=24648907 sections_touched=6939 sample_pct=94 write_pct=5 specialize=1530986 nc1_s1=0 nc1_s2=1258 nc1_s3=0 nc3_grid=1258 nc5_ore=1258 aq_spec=1258 spec_deep=0 spec_nodes=1258 vanilla_arith=0 l0_reasons{}
[20:47:45] [Worker-Main-3/INFO] [noisiumed/]: noisiumed.path l0=0 l1=896 l2=877 l1_fail=0 biome=26856 surface_skip=0 l1_avg_us=92847 direct_writes=28773163 sections_touched=8047 sample_pct=94 write_pct=5 specialize=1722055 nc1_s1=0 nc1_s2=1415 nc1_s3=0 nc3_grid=1415 nc5_ore=1415 aq_spec=1415 spec_deep=0 spec_nodes=1415 vanilla_arith=0 l0_reasons{}
[20:47:47] [Worker-Main-7/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1024 l2=994 l1_fail=0 biome=30312 surface_skip=0 l1_avg_us=87091 direct_writes=33014119 sections_touched=9215 sample_pct=95 write_pct=4 specialize=1913124 nc1_s1=0 nc1_s2=1572 nc1_s3=0 nc3_grid=1572 nc5_ore=1572 aq_spec=1572 spec_deep=0 spec_nodes=1572 vanilla_arith=0 l0_reasons{}
[20:47:50] [Server thread/INFO] [minecraft/MinecraftServer]: [Rcon: Marked 225 chunks in Overworld from [153, 121] to [167, 135] to be force loaded]
[20:47:50] [Worker-Main-17/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1152 l2=1143 l1_fail=0 biome=33624 surface_skip=0 l1_avg_us=82352 direct_writes=37287216 sections_touched=10394 sample_pct=95 write_pct=4 specialize=2094457 nc1_s1=0 nc1_s2=1721 nc1_s3=0 nc3_grid=1721 nc5_ore=1721 aq_spec=1721 spec_deep=0 spec_nodes=1721 vanilla_arith=0 l0_reasons{}
[20:47:52] [Worker-Main-18/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1280 l2=1278 l1_fail=0 biome=37752 surface_skip=0 l1_avg_us=78475 direct_writes=41417122 sections_touched=11480 sample_pct=95 write_pct=4 specialize=2315951 nc1_s1=0 nc1_s2=1903 nc1_s3=0 nc3_grid=1903 nc5_ore=1903 aq_spec=1903 spec_deep=0 spec_nodes=1903 vanilla_arith=0 l0_reasons{}
[20:47:54] [Worker-Main-25/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1408 l2=1399 l1_fail=0 biome=41856 surface_skip=0 l1_avg_us=74849 direct_writes=45514761 sections_touched=12662 sample_pct=95 write_pct=4 specialize=2545964 nc1_s1=0 nc1_s2=2092 nc1_s3=0 nc3_grid=2092 nc5_ore=2092 aq_spec=2092 spec_deep=0 spec_nodes=2092 vanilla_arith=0 l0_reasons{}
[20:47:56] [Worker-Main-28/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1536 l2=1522 l1_fail=0 biome=44832 surface_skip=0 l1_avg_us=71808 direct_writes=49975929 sections_touched=13917 sample_pct=95 write_pct=4 specialize=2700523 nc1_s1=0 nc1_s2=2219 nc1_s3=0 nc3_grid=2219 nc5_ore=2219 aq_spec=2219 spec_deep=0 spec_nodes=2219 vanilla_arith=0 l0_reasons{}
[20:47:58] [Worker-Main-10/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1664 l2=1636 l1_fail=0 biome=48312 surface_skip=0 l1_avg_us=69345 direct_writes=54216821 sections_touched=15136 sample_pct=95 write_pct=4 specialize=2885507 nc1_s1=0 nc1_s2=2371 nc1_s3=0 nc3_grid=2371 nc5_ore=2371 aq_spec=2371 spec_deep=0 spec_nodes=2371 vanilla_arith=0 l0_reasons{}
[20:47:59] [Server thread/INFO] [minecraft/MinecraftServer]: [Rcon: Marked 225 chunks in Overworld from [121, 153] to [135, 167] to be force loaded]
[20:48:02] [Worker-Main-2/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1792 l2=1780 l1_fail=0 biome=52176 surface_skip=0 l1_avg_us=72013 direct_writes=59090924 sections_touched=16473 sample_pct=95 write_pct=4 specialize=3435591 nc1_s1=0 nc1_s2=2823 nc1_s3=0 nc3_grid=2823 nc5_ore=2823 aq_spec=2823 spec_deep=0 spec_nodes=2823 vanilla_arith=0 l0_reasons{}
[20:48:05] [spark-java-sampler-1-2/WARN] [spark/]: Timed out waiting for world statistics
[20:48:05] [Worker-Main-4/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1920 l2=1897 l1_fail=0 biome=56136 surface_skip=0 l1_avg_us=70181 direct_writes=63557024 sections_touched=17748 sample_pct=95 write_pct=4 specialize=3644915 nc1_s1=0 nc1_s2=2995 nc1_s3=0 nc3_grid=2995 nc5_ore=2995 aq_spec=2995 spec_deep=0 spec_nodes=2995 vanilla_arith=0 l0_reasons{}
[20:48:07] [Worker-Main-26/INFO] [noisiumed/]: noisiumed.path l0=0 l1=2048 l2=2045 l1_fail=0 biome=59784 surface_skip=0 l1_avg_us=68724 direct_writes=67792982 sections_touched=18951 sample_pct=95 write_pct=4 specialize=3832333 nc1_s1=0 nc1_s2=3149 nc1_s3=0 nc3_grid=3149 nc5_ore=3149 aq_spec=3149 spec_deep=0 spec_nodes=3149 vanilla_arith=0 l0_reasons{}
[20:48:09] [Worker-Main-16/INFO] [noisiumed/]: noisiumed.path l0=0 l1=2176 l2=2165 l1_fail=0 biome=62832 surface_skip=0 l1_avg_us=66668 direct_writes=72625780 sections_touched=20265 sample_pct=95 write_pct=4 specialize=3995411 nc1_s1=0 nc1_s2=3283 nc1_s3=0 nc3_grid=3283 nc5_ore=3283 aq_spec=3283 spec_deep=0 spec_nodes=3283 vanilla_arith=0 l0_reasons{}
[20:48:10] [Server thread/INFO] [minecraft/MinecraftServer]: [Rcon: Marked 225 chunks in Overworld from [153, 153] to [167, 167] to be force loaded]
[20:49:50] [spark-worker-pool-1-thread-3/INFO] [minecraft/MinecraftServer]: [?] Stopping the profiler & uploading results, please wait...
[20:49:51] [spark-worker-pool-1-thread-3/INFO] [spark/]: If you see a warning above that says "WARNING: A Java agent has been loaded dynamically", it can be safely ignored.
[20:49:51] [spark-worker-pool-1-thread-3/INFO] [spark/]: See here for more information: https://spark.lucko.me/docs/misc/Java-agent-warning
[20:49:54] [spark-worker-pool-1-thread-3/INFO] [minecraft/MinecraftServer]: [?] Profiler stopped & upload complete!
[20:49:54] [spark-worker-pool-1-thread-3/INFO] [minecraft/MinecraftServer]: https://spark.lucko.me/q3qNue01Im
[20:49:54] [spark-worker-pool-1-thread-3/INFO] [spark/]: The async-profiler engine is not supported for your os/arch (windows11/amd64), so the built-in Java engine will be used instead.
[20:49:54] [spark-worker-pool-1-thread-3/INFO] [minecraft/MinecraftServer]: [?] Restarted the background profiler. (If you don't want this to happen, run: /spark profiler cancel)
[20:49:56] [Server thread/INFO] [minecraft/MinecraftServer]: [Rcon: Unmarked all force loaded chunks in Overworld]
[20:49:57] [Server thread/INFO] [minecraft/MinecraftServer]: Saving chunks for level 'ServerLevel[world]'/minecraft:overworld
[20:50:00] [Server thread/INFO] [minecraft/MinecraftServer]: Saving chunks for level 'ServerLevel[world]'/minecraft:the_end
[20:50:00] [Server thread/INFO] [minecraft/MinecraftServer]: Saving chunks for level 'ServerLevel[world]'/minecraft:the_nether
[20:50:00] [Server thread/INFO] [minecraft/MinecraftServer]: ThreadedAnvilChunkStorage (world): All chunks are saved
[20:50:00] [Server thread/INFO] [minecraft/MinecraftServer]: ThreadedAnvilChunkStorage (DIM1): All chunks are saved
[20:50:00] [Server thread/INFO] [minecraft/MinecraftServer]: ThreadedAnvilChunkStorage (DIM-1): All chunks are saved
[20:50:00] [Server thread/INFO] [minecraft/MinecraftServer]: ThreadedAnvilChunkStorage: All dimensions are saved
[30Jul2026 20:47:06.502] [main/INFO] [mixin/]: SpongePowered MIXIN Subsystem Version=0.8.7 Source=union:/C:/Users/imba/Git/infernos/minecraft-mods/noisium/bench/run-noisiumed/libraries/net/fabricmc/sponge-mixin/0.15.2+mixin.0.8.7/sponge-mixin-0.15.2+mixin.0.8.7.jar%2373!/ Service=ModLauncher Env=SERVER
[30Jul2026 20:47:07.029] [main/INFO] [net.neoforged.fml.loading.moddiscovery.ModDiscoverer/SCAN]: Found mod file "noisiumed-4.0.0-beta.16.5-w1-neoforge-1.21.1.jar" [locator: {mods folder locator at C:\Users\imba\Git\infernos\minecraft-mods\noisium\bench\run-noisiumed\mods}, reader: mod manifest]
[30Jul2026 20:47:07.037] [main/INFO] [net.neoforged.fml.loading.moddiscovery.ModDiscoverer/SCAN]: Found mod file "spark-1.10.124-neoforge.jar" [locator: {mods folder locator at C:\Users\imba\Git\infernos\minecraft-mods\noisium\bench\run-noisiumed\mods}, reader: mod manifest]
		Noisiumed 4.0.0-beta.16.5-w1-neoforge-1.21.1 (noisiumed)
		spark 1.10.124 (spark)
[30Jul2026 20:47:17.054] [modloading-worker-0/INFO] [noisiumed/]: Loading Noisiumed (L0–L2 + density specializer + surface/coverage). Metrics: PathMetrics.snapshot()
[30Jul2026 20:47:17.675] [main/INFO] [com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService/]: Environment: Environment[sessionHost=https://sessionserver.mojang.com, servicesHost=https://api.minecraftservices.com, name=PROD]
[30Jul2026 20:47:17.785] [main/WARN] [net.minecraft.server.packs.VanillaPackResourcesBuilder/]: Assets URL 'union:/C:/Users/imba/Git/infernos/minecraft-mods/noisium/bench/run-noisiumed/libraries/net/minecraft/server/1.21.1-20240808.144430/server-1.21.1-20240808.144430-srg.jar%23119!/assets/.mcassetsroot' uses unexpected schema
[30Jul2026 20:47:17.786] [main/WARN] [net.minecraft.server.packs.VanillaPackResourcesBuilder/]: Assets URL 'union:/C:/Users/imba/Git/infernos/minecraft-mods/noisium/bench/run-noisiumed/libraries/net/minecraft/server/1.21.1-20240808.144430/server-1.21.1-20240808.144430-srg.jar%23119!/data/.mcassetsroot' uses unexpected schema
[30Jul2026 20:47:20.685] [Server thread/INFO] [spark/]: Starting background profiler...
[30Jul2026 20:47:20.707] [Server thread/INFO] [spark/]: The async-profiler engine is not supported for your os/arch (windows11/amd64), so the built-in Java engine will be used instead.
[30Jul2026 20:47:30.272] [spark-worker-pool-1-thread-1/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] Starting a new profiler, please wait...
[30Jul2026 20:47:30.273] [spark-worker-pool-1-thread-1/INFO] [spark/]: The async-profiler engine is not supported for your os/arch (windows11/amd64), so the built-in Java engine will be used instead.
[30Jul2026 20:47:30.275] [spark-worker-pool-1-thread-1/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] Profiler is now running! (built-in java)
[30Jul2026 20:47:30.276] [spark-worker-pool-1-thread-1/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] It will run in the background until it is stopped by an admin.
[30Jul2026 20:47:30.276] [spark-worker-pool-1-thread-1/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] To stop the profiler and upload the results, run:
[30Jul2026 20:47:30.280] [spark-worker-pool-1-thread-1/INFO] [net.minecraft.server.MinecraftServer/]: [⚡]   /spark profiler stop
[30Jul2026 20:47:30.281] [spark-worker-pool-1-thread-1/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] To view the profiler while it's running, run:
[30Jul2026 20:47:30.281] [spark-worker-pool-1-thread-1/INFO] [net.minecraft.server.MinecraftServer/]: [⚡]   /spark profiler open
[30Jul2026 20:47:31.263] [Worker-Main-3/INFO] [noisiumed/]: noisiumed.path l0=0 l1=128 l2=121 l1_fail=0 biome=5232 surface_skip=0 l1_avg_us=296054 direct_writes=5168420 sections_touched=1452 sample_pct=94 write_pct=5 specialize=311552 nc1_s1=0 nc1_s2=256 nc1_s3=0 nc3_grid=256 nc5_ore=256 aq_spec=256 spec_deep=0 spec_nodes=256 vanilla_arith=0 l0_reasons{}
[30Jul2026 20:47:33.589] [Worker-Main-4/INFO] [noisiumed/]: noisiumed.path l0=0 l1=256 l2=247 l1_fail=0 biome=8808 surface_skip=0 l1_avg_us=175997 direct_writes=9104234 sections_touched=2570 sample_pct=94 write_pct=5 specialize=519659 nc1_s1=0 nc1_s2=427 nc1_s3=0 nc3_grid=427 nc5_ore=427 aq_spec=427 spec_deep=0 spec_nodes=427 vanilla_arith=0 l0_reasons{}
[30Jul2026 20:47:36.064] [Worker-Main-2/INFO] [noisiumed/]: noisiumed.path l0=0 l1=384 l2=371 l1_fail=0 biome=12432 surface_skip=0 l1_avg_us=145034 direct_writes=12737837 sections_touched=3658 sample_pct=94 write_pct=5 specialize=720464 nc1_s1=0 nc1_s2=592 nc1_s3=0 nc3_grid=592 nc5_ore=592 aq_spec=592 spec_deep=0 spec_nodes=592 vanilla_arith=0 l0_reasons{}
[30Jul2026 20:47:38.351] [Worker-Main-29/INFO] [noisiumed/]: noisiumed.path l0=0 l1=512 l2=511 l1_fail=0 biome=16056 surface_skip=0 l1_avg_us=119827 direct_writes=16542781 sections_touched=4748 sample_pct=94 write_pct=5 specialize=921269 nc1_s1=0 nc1_s2=757 nc1_s3=0 nc3_grid=757 nc5_ore=757 aq_spec=757 spec_deep=0 spec_nodes=757 vanilla_arith=0 l0_reasons{}
[30Jul2026 20:47:40.539] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: [Rcon: Marked 225 chunks in Overworld from [121, 121] to [135, 135] to be force loaded]
[30Jul2026 20:47:41.211] [Worker-Main-16/INFO] [noisiumed/]: noisiumed.path l0=0 l1=640 l2=628 l1_fail=0 biome=19056 surface_skip=0 l1_avg_us=105327 direct_writes=20505545 sections_touched=5835 sample_pct=94 write_pct=5 specialize=1271765 nc1_s1=0 nc1_s2=1045 nc1_s3=0 nc3_grid=1045 nc5_ore=1045 aq_spec=1045 spec_deep=0 spec_nodes=1045 vanilla_arith=0 l0_reasons{}
[30Jul2026 20:47:43.977] [Worker-Main-2/INFO] [noisiumed/]: noisiumed.path l0=0 l1=768 l2=758 l1_fail=0 biome=23280 surface_skip=0 l1_avg_us=100191 direct_writes=24648907 sections_touched=6939 sample_pct=94 write_pct=5 specialize=1530986 nc1_s1=0 nc1_s2=1258 nc1_s3=0 nc3_grid=1258 nc5_ore=1258 aq_spec=1258 spec_deep=0 spec_nodes=1258 vanilla_arith=0 l0_reasons{}
[30Jul2026 20:47:45.984] [Worker-Main-3/INFO] [noisiumed/]: noisiumed.path l0=0 l1=896 l2=877 l1_fail=0 biome=26856 surface_skip=0 l1_avg_us=92847 direct_writes=28773163 sections_touched=8047 sample_pct=94 write_pct=5 specialize=1722055 nc1_s1=0 nc1_s2=1415 nc1_s3=0 nc3_grid=1415 nc5_ore=1415 aq_spec=1415 spec_deep=0 spec_nodes=1415 vanilla_arith=0 l0_reasons{}
[30Jul2026 20:47:47.968] [Worker-Main-7/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1024 l2=994 l1_fail=0 biome=30312 surface_skip=0 l1_avg_us=87091 direct_writes=33014119 sections_touched=9215 sample_pct=95 write_pct=4 specialize=1913124 nc1_s1=0 nc1_s2=1572 nc1_s3=0 nc3_grid=1572 nc5_ore=1572 aq_spec=1572 spec_deep=0 spec_nodes=1572 vanilla_arith=0 l0_reasons{}
[30Jul2026 20:47:50.226] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: [Rcon: Marked 225 chunks in Overworld from [153, 121] to [167, 135] to be force loaded]
[30Jul2026 20:47:50.549] [Worker-Main-17/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1152 l2=1143 l1_fail=0 biome=33624 surface_skip=0 l1_avg_us=82352 direct_writes=37287216 sections_touched=10394 sample_pct=95 write_pct=4 specialize=2094457 nc1_s1=0 nc1_s2=1721 nc1_s3=0 nc3_grid=1721 nc5_ore=1721 aq_spec=1721 spec_deep=0 spec_nodes=1721 vanilla_arith=0 l0_reasons{}
[30Jul2026 20:47:52.657] [Worker-Main-18/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1280 l2=1278 l1_fail=0 biome=37752 surface_skip=0 l1_avg_us=78475 direct_writes=41417122 sections_touched=11480 sample_pct=95 write_pct=4 specialize=2315951 nc1_s1=0 nc1_s2=1903 nc1_s3=0 nc3_grid=1903 nc5_ore=1903 aq_spec=1903 spec_deep=0 spec_nodes=1903 vanilla_arith=0 l0_reasons{}
[30Jul2026 20:47:54.745] [Worker-Main-25/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1408 l2=1399 l1_fail=0 biome=41856 surface_skip=0 l1_avg_us=74849 direct_writes=45514761 sections_touched=12662 sample_pct=95 write_pct=4 specialize=2545964 nc1_s1=0 nc1_s2=2092 nc1_s3=0 nc3_grid=2092 nc5_ore=2092 aq_spec=2092 spec_deep=0 spec_nodes=2092 vanilla_arith=0 l0_reasons{}
[30Jul2026 20:47:56.958] [Worker-Main-28/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1536 l2=1522 l1_fail=0 biome=44832 surface_skip=0 l1_avg_us=71808 direct_writes=49975929 sections_touched=13917 sample_pct=95 write_pct=4 specialize=2700523 nc1_s1=0 nc1_s2=2219 nc1_s3=0 nc3_grid=2219 nc5_ore=2219 aq_spec=2219 spec_deep=0 spec_nodes=2219 vanilla_arith=0 l0_reasons{}
[30Jul2026 20:47:58.797] [Worker-Main-10/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1664 l2=1636 l1_fail=0 biome=48312 surface_skip=0 l1_avg_us=69345 direct_writes=54216821 sections_touched=15136 sample_pct=95 write_pct=4 specialize=2885507 nc1_s1=0 nc1_s2=2371 nc1_s3=0 nc3_grid=2371 nc5_ore=2371 aq_spec=2371 spec_deep=0 spec_nodes=2371 vanilla_arith=0 l0_reasons{}
[30Jul2026 20:47:59.146] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: [Rcon: Marked 225 chunks in Overworld from [121, 153] to [135, 167] to be force loaded]
[30Jul2026 20:48:02.918] [Worker-Main-2/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1792 l2=1780 l1_fail=0 biome=52176 surface_skip=0 l1_avg_us=72013 direct_writes=59090924 sections_touched=16473 sample_pct=95 write_pct=4 specialize=3435591 nc1_s1=0 nc1_s2=2823 nc1_s3=0 nc3_grid=2823 nc5_ore=2823 aq_spec=2823 spec_deep=0 spec_nodes=2823 vanilla_arith=0 l0_reasons{}
[30Jul2026 20:48:05.009] [spark-java-sampler-1-2/WARN] [spark/]: Timed out waiting for world statistics
[30Jul2026 20:48:05.172] [Worker-Main-4/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1920 l2=1897 l1_fail=0 biome=56136 surface_skip=0 l1_avg_us=70181 direct_writes=63557024 sections_touched=17748 sample_pct=95 write_pct=4 specialize=3644915 nc1_s1=0 nc1_s2=2995 nc1_s3=0 nc3_grid=2995 nc5_ore=2995 aq_spec=2995 spec_deep=0 spec_nodes=2995 vanilla_arith=0 l0_reasons{}
[30Jul2026 20:48:07.486] [Worker-Main-26/INFO] [noisiumed/]: noisiumed.path l0=0 l1=2048 l2=2045 l1_fail=0 biome=59784 surface_skip=0 l1_avg_us=68724 direct_writes=67792982 sections_touched=18951 sample_pct=95 write_pct=4 specialize=3832333 nc1_s1=0 nc1_s2=3149 nc1_s3=0 nc3_grid=3149 nc5_ore=3149 aq_spec=3149 spec_deep=0 spec_nodes=3149 vanilla_arith=0 l0_reasons{}
[30Jul2026 20:48:09.831] [Worker-Main-16/INFO] [noisiumed/]: noisiumed.path l0=0 l1=2176 l2=2165 l1_fail=0 biome=62832 surface_skip=0 l1_avg_us=66668 direct_writes=72625780 sections_touched=20265 sample_pct=95 write_pct=4 specialize=3995411 nc1_s1=0 nc1_s2=3283 nc1_s3=0 nc3_grid=3283 nc5_ore=3283 aq_spec=3283 spec_deep=0 spec_nodes=3283 vanilla_arith=0 l0_reasons{}
[30Jul2026 20:48:10.264] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: [Rcon: Marked 225 chunks in Overworld from [153, 153] to [167, 167] to be force loaded]
[30Jul2026 20:49:50.992] [spark-worker-pool-1-thread-3/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] Stopping the profiler & uploading results, please wait...
[30Jul2026 20:49:51.775] [spark-worker-pool-1-thread-3/INFO] [spark/]: If you see a warning above that says "WARNING: A Java agent has been loaded dynamically", it can be safely ignored.
[30Jul2026 20:49:51.776] [spark-worker-pool-1-thread-3/INFO] [spark/]: See here for more information: https://spark.lucko.me/docs/misc/Java-agent-warning
[30Jul2026 20:49:54.935] [spark-worker-pool-1-thread-3/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] Profiler stopped & upload complete!
[30Jul2026 20:49:54.936] [spark-worker-pool-1-thread-3/INFO] [net.minecraft.server.MinecraftServer/]: https://spark.lucko.me/q3qNue01Im
[30Jul2026 20:49:54.938] [spark-worker-pool-1-thread-3/INFO] [spark/]: The async-profiler engine is not supported for your os/arch (windows11/amd64), so the built-in Java engine will be used instead.
[30Jul2026 20:49:54.939] [spark-worker-pool-1-thread-3/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] Restarted the background profiler. (If you don't want this to happen, run: /spark profiler cancel)
[30Jul2026 20:49:56.296] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: [Rcon: Unmarked all force loaded chunks in Overworld]
[30Jul2026 20:49:57.768] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: Saving chunks for level 'ServerLevel[world]'/minecraft:overworld
[30Jul2026 20:50:00.437] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: Saving chunks for level 'ServerLevel[world]'/minecraft:the_end
[30Jul2026 20:50:00.454] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: Saving chunks for level 'ServerLevel[world]'/minecraft:the_nether
[30Jul2026 20:50:00.482] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: ThreadedAnvilChunkStorage (world): All chunks are saved
[30Jul2026 20:50:00.482] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: ThreadedAnvilChunkStorage (DIM1): All chunks are saved
[30Jul2026 20:50:00.482] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: ThreadedAnvilChunkStorage (DIM-1): All chunks are saved
[30Jul2026 20:50:00.482] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: ThreadedAnvilChunkStorage: All dimensions are saved

```

## Fast Noise 1.0.13
```
label=fastnoise
seed=12345 radiusChunks=7 profileSeconds=100 port=25571
wall_boot_ms=24612 wall_forceload_ms=44070 wall_profile_window_ms=144182 wall_total_ms=182676
spark_url=https://spark.lucko.me/JhFjub6yP5
spark_bin=C:\Users\imba\Git\infernos\minecraft-mods\noisium\bench\results\JhFjub6yP5.bin
--- path metrics (from log) ---
--- rcon spark profiler stop ---

--- rcon spark tps ---

--- rcon spark health ---

--- matched log lines ---
[20:50:10.371] [main/INFO] [mixin/]: SpongePowered MIXIN Subsystem Version=0.8.7 Source=union:/C:/Users/imba/Git/infernos/minecraft-mods/noisium/bench/run-fastnoise/libraries/net/fabricmc/sponge-mixin/0.15.2+mixin.0.8.7/sponge-mixin-0.15.2+mixin.0.8.7.jar%2373!/ Service=ModLauncher Env=SERVER 
[20:50:10.859] [main/INFO] [loading.moddiscovery.ModDiscoverer/SCAN]: Found mod file "spark-1.10.124-neoforge.jar" [locator: {mods folder locator at C:\Users\imba\Git\infernos\minecraft-mods\noisium\bench\run-fastnoise\mods}, reader: mod manifest] 
[20:50:10.868] [main/INFO] [loading.moddiscovery.ModDiscoverer/SCAN]: Found mod file "zfastnoise-1.0.13+1.21.1+neoforge.jar" [locator: {mods folder locator at C:\Users\imba\Git\infernos\minecraft-mods\noisium\bench\run-fastnoise\mods}, reader: mod manifest] 
		Fast Noise Mod 1.0.13+1.21.1+neoforge (zfastnoise)
		spark 1.10.124 (spark) 
[20:50:20] [modloading-worker-0/INFO] [zfastnoise/]: Hello Fabric world!
[20:50:20] [main/INFO] [mojang/YggdrasilAuthenticationService]: Environment: Environment[sessionHost=https://sessionserver.mojang.com, servicesHost=https://api.minecraftservices.com, name=PROD]
[20:50:20] [main/WARN] [minecraft/VanillaPackResourcesBuilder]: Assets URL 'union:/C:/Users/imba/Git/infernos/minecraft-mods/noisium/bench/run-fastnoise/libraries/net/minecraft/server/1.21.1-20240808.144430/server-1.21.1-20240808.144430-srg.jar%23119!/assets/.mcassetsroot' uses unexpected schema
[20:50:20] [main/WARN] [minecraft/VanillaPackResourcesBuilder]: Assets URL 'union:/C:/Users/imba/Git/infernos/minecraft-mods/noisium/bench/run-fastnoise/libraries/net/minecraft/server/1.21.1-20240808.144430/server-1.21.1-20240808.144430-srg.jar%23119!/data/.mcassetsroot' uses unexpected schema
[20:50:23] [Server thread/INFO] [spark/]: Starting background profiler...
[20:50:23] [Server thread/INFO] [spark/]: The async-profiler engine is not supported for your os/arch (windows11/amd64), so the built-in Java engine will be used instead.
[20:50:34] [spark-worker-pool-1-thread-1/INFO] [minecraft/MinecraftServer]: [?] Starting a new profiler, please wait...
[20:50:34] [spark-worker-pool-1-thread-1/INFO] [spark/]: The async-profiler engine is not supported for your os/arch (windows11/amd64), so the built-in Java engine will be used instead.
[20:50:34] [spark-worker-pool-1-thread-1/INFO] [minecraft/MinecraftServer]: [?] Profiler is now running! (built-in java)
[20:50:34] [spark-worker-pool-1-thread-1/INFO] [minecraft/MinecraftServer]: [?] It will run in the background until it is stopped by an admin.
[20:50:34] [spark-worker-pool-1-thread-1/INFO] [minecraft/MinecraftServer]: [?] To stop the profiler and upload the results, run:
[20:50:34] [spark-worker-pool-1-thread-1/INFO] [minecraft/MinecraftServer]: [?]   /spark profiler stop
[20:50:34] [spark-worker-pool-1-thread-1/INFO] [minecraft/MinecraftServer]: [?] To view the profiler while it's running, run:
[20:50:34] [spark-worker-pool-1-thread-1/INFO] [minecraft/MinecraftServer]: [?]   /spark profiler open
[20:50:48] [Server thread/INFO] [minecraft/MinecraftServer]: [Rcon: Marked 225 chunks in Overworld from [121, 121] to [135, 135] to be force loaded]
[20:50:57] [Server thread/INFO] [minecraft/MinecraftServer]: [Rcon: Marked 225 chunks in Overworld from [153, 121] to [167, 135] to be force loaded]
[20:51:05] [spark-java-sampler-1-2/WARN] [spark/]: Timed out waiting for world statistics
[20:51:06] [Server thread/INFO] [minecraft/MinecraftServer]: [Rcon: Marked 225 chunks in Overworld from [121, 153] to [135, 167] to be force loaded]
[20:51:18] [Server thread/INFO] [minecraft/MinecraftServer]: [Rcon: Marked 225 chunks in Overworld from [153, 153] to [167, 167] to be force loaded]
[20:52:58] [spark-worker-pool-1-thread-3/INFO] [minecraft/MinecraftServer]: [?] Stopping the profiler & uploading results, please wait...
[20:52:59] [spark-worker-pool-1-thread-3/INFO] [spark/]: If you see a warning above that says "WARNING: A Java agent has been loaded dynamically", it can be safely ignored.
[20:52:59] [spark-worker-pool-1-thread-3/INFO] [spark/]: See here for more information: https://spark.lucko.me/docs/misc/Java-agent-warning
[20:53:03] [spark-worker-pool-1-thread-3/INFO] [minecraft/MinecraftServer]: [?] Profiler stopped & upload complete!
[20:53:03] [spark-worker-pool-1-thread-3/INFO] [minecraft/MinecraftServer]: https://spark.lucko.me/JhFjub6yP5
[20:53:03] [spark-worker-pool-1-thread-3/INFO] [spark/]: The async-profiler engine is not supported for your os/arch (windows11/amd64), so the built-in Java engine will be used instead.
[20:53:03] [spark-worker-pool-1-thread-3/INFO] [minecraft/MinecraftServer]: [?] Restarted the background profiler. (If you don't want this to happen, run: /spark profiler cancel)
[20:53:04] [Server thread/INFO] [minecraft/MinecraftServer]: [Rcon: Unmarked all force loaded chunks in Overworld]
[20:53:06] [Server thread/INFO] [minecraft/MinecraftServer]: Saving chunks for level 'ServerLevel[world]'/minecraft:overworld
[20:53:09] [Server thread/INFO] [minecraft/MinecraftServer]: Saving chunks for level 'ServerLevel[world]'/minecraft:the_nether
[20:53:09] [Server thread/INFO] [minecraft/MinecraftServer]: Saving chunks for level 'ServerLevel[world]'/minecraft:the_end
[20:53:09] [Server thread/INFO] [minecraft/MinecraftServer]: ThreadedAnvilChunkStorage (world): All chunks are saved
[20:53:09] [Server thread/INFO] [minecraft/MinecraftServer]: ThreadedAnvilChunkStorage (DIM-1): All chunks are saved
[20:53:09] [Server thread/INFO] [minecraft/MinecraftServer]: ThreadedAnvilChunkStorage (DIM1): All chunks are saved
[20:53:09] [Server thread/INFO] [minecraft/MinecraftServer]: ThreadedAnvilChunkStorage: All dimensions are saved
[30Jul2026 20:50:10.371] [main/INFO] [mixin/]: SpongePowered MIXIN Subsystem Version=0.8.7 Source=union:/C:/Users/imba/Git/infernos/minecraft-mods/noisium/bench/run-fastnoise/libraries/net/fabricmc/sponge-mixin/0.15.2+mixin.0.8.7/sponge-mixin-0.15.2+mixin.0.8.7.jar%2373!/ Service=ModLauncher Env=SERVER
[30Jul2026 20:50:10.859] [main/INFO] [net.neoforged.fml.loading.moddiscovery.ModDiscoverer/SCAN]: Found mod file "spark-1.10.124-neoforge.jar" [locator: {mods folder locator at C:\Users\imba\Git\infernos\minecraft-mods\noisium\bench\run-fastnoise\mods}, reader: mod manifest]
[30Jul2026 20:50:10.868] [main/INFO] [net.neoforged.fml.loading.moddiscovery.ModDiscoverer/SCAN]: Found mod file "zfastnoise-1.0.13+1.21.1+neoforge.jar" [locator: {mods folder locator at C:\Users\imba\Git\infernos\minecraft-mods\noisium\bench\run-fastnoise\mods}, reader: mod manifest]
		Fast Noise Mod 1.0.13+1.21.1+neoforge (zfastnoise)
		spark 1.10.124 (spark)
[30Jul2026 20:50:20.737] [modloading-worker-0/INFO] [zfastnoise/]: Hello Fabric world!
[30Jul2026 20:50:20.881] [main/INFO] [com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService/]: Environment: Environment[sessionHost=https://sessionserver.mojang.com, servicesHost=https://api.minecraftservices.com, name=PROD]
[30Jul2026 20:50:20.974] [main/WARN] [net.minecraft.server.packs.VanillaPackResourcesBuilder/]: Assets URL 'union:/C:/Users/imba/Git/infernos/minecraft-mods/noisium/bench/run-fastnoise/libraries/net/minecraft/server/1.21.1-20240808.144430/server-1.21.1-20240808.144430-srg.jar%23119!/assets/.mcassetsroot' uses unexpected schema
[30Jul2026 20:50:20.975] [main/WARN] [net.minecraft.server.packs.VanillaPackResourcesBuilder/]: Assets URL 'union:/C:/Users/imba/Git/infernos/minecraft-mods/noisium/bench/run-fastnoise/libraries/net/minecraft/server/1.21.1-20240808.144430/server-1.21.1-20240808.144430-srg.jar%23119!/data/.mcassetsroot' uses unexpected schema
[30Jul2026 20:50:23.877] [Server thread/INFO] [spark/]: Starting background profiler...
[30Jul2026 20:50:23.896] [Server thread/INFO] [spark/]: The async-profiler engine is not supported for your os/arch (windows11/amd64), so the built-in Java engine will be used instead.
[30Jul2026 20:50:34.587] [spark-worker-pool-1-thread-1/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] Starting a new profiler, please wait...
[30Jul2026 20:50:34.588] [spark-worker-pool-1-thread-1/INFO] [spark/]: The async-profiler engine is not supported for your os/arch (windows11/amd64), so the built-in Java engine will be used instead.
[30Jul2026 20:50:34.592] [spark-worker-pool-1-thread-1/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] Profiler is now running! (built-in java)
[30Jul2026 20:50:34.593] [spark-worker-pool-1-thread-1/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] It will run in the background until it is stopped by an admin.
[30Jul2026 20:50:34.594] [spark-worker-pool-1-thread-1/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] To stop the profiler and upload the results, run:
[30Jul2026 20:50:34.600] [spark-worker-pool-1-thread-1/INFO] [net.minecraft.server.MinecraftServer/]: [⚡]   /spark profiler stop
[30Jul2026 20:50:34.601] [spark-worker-pool-1-thread-1/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] To view the profiler while it's running, run:
[30Jul2026 20:50:34.601] [spark-worker-pool-1-thread-1/INFO] [net.minecraft.server.MinecraftServer/]: [⚡]   /spark profiler open
[30Jul2026 20:50:48.262] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: [Rcon: Marked 225 chunks in Overworld from [121, 121] to [135, 135] to be force loaded]
[30Jul2026 20:50:57.986] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: [Rcon: Marked 225 chunks in Overworld from [153, 121] to [167, 135] to be force loaded]
[30Jul2026 20:51:05.039] [spark-java-sampler-1-2/WARN] [spark/]: Timed out waiting for world statistics
[30Jul2026 20:51:06.711] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: [Rcon: Marked 225 chunks in Overworld from [121, 153] to [135, 167] to be force loaded]
[30Jul2026 20:51:18.120] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: [Rcon: Marked 225 chunks in Overworld from [153, 153] to [167, 167] to be force loaded]
[30Jul2026 20:52:58.836] [spark-worker-pool-1-thread-3/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] Stopping the profiler & uploading results, please wait...
[30Jul2026 20:52:59.623] [spark-worker-pool-1-thread-3/INFO] [spark/]: If you see a warning above that says "WARNING: A Java agent has been loaded dynamically", it can be safely ignored.
[30Jul2026 20:52:59.624] [spark-worker-pool-1-thread-3/INFO] [spark/]: See here for more information: https://spark.lucko.me/docs/misc/Java-agent-warning
[30Jul2026 20:53:03.784] [spark-worker-pool-1-thread-3/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] Profiler stopped & upload complete!
[30Jul2026 20:53:03.784] [spark-worker-pool-1-thread-3/INFO] [net.minecraft.server.MinecraftServer/]: https://spark.lucko.me/JhFjub6yP5
[30Jul2026 20:53:03.788] [spark-worker-pool-1-thread-3/INFO] [spark/]: The async-profiler engine is not supported for your os/arch (windows11/amd64), so the built-in Java engine will be used instead.
[30Jul2026 20:53:03.788] [spark-worker-pool-1-thread-3/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] Restarted the background profiler. (If you don't want this to happen, run: /spark profiler cancel)
[30Jul2026 20:53:04.156] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: [Rcon: Unmarked all force loaded chunks in Overworld]
[30Jul2026 20:53:06.090] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: Saving chunks for level 'ServerLevel[world]'/minecraft:overworld
[30Jul2026 20:53:09.416] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: Saving chunks for level 'ServerLevel[world]'/minecraft:the_nether
[30Jul2026 20:53:09.434] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: Saving chunks for level 'ServerLevel[world]'/minecraft:the_end
[30Jul2026 20:53:09.462] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: ThreadedAnvilChunkStorage (world): All chunks are saved
[30Jul2026 20:53:09.462] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: ThreadedAnvilChunkStorage (DIM-1): All chunks are saved
[30Jul2026 20:53:09.462] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: ThreadedAnvilChunkStorage (DIM1): All chunks are saved
[30Jul2026 20:53:09.462] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: ThreadedAnvilChunkStorage: All dimensions are saved

```


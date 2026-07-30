# Noisiumed vs Fast Noise - Spark worldgen compare
Date: 2026-07-30T21:59:54.7013896+02:00
Platform: NeoForge 21.1.233 / MC 1.21.1 (dedicated, nogui, RCON)
Seed=12345 forceload radius=7 profile=100s Xmx=4G
Baseline mods: Spark + candidate only
Huge-win metrics: wall_boot_ms / wall_forceload_ms / wall_profile_window_ms / wall_total_ms + PathMetrics

**Scope:** Primary **1.21.1**. A/B = **noisiumed-4.0.0-beta.16.5-w5-neoforge-1.21.1.jar** vs Fast Noise 1.0.13.

## Noisiumed
```
label=noisiumed
seed=12345 radiusChunks=7 profileSeconds=100 port=25570
wall_boot_ms=29978 wall_forceload_ms=46833 wall_profile_window_ms=146974 wall_total_ms=307786
spark_url=https://spark.lucko.me/FxrOEcNa6P
spark_bin=C:\Users\imba\Git\infernos\minecraft-mods\noisium\bench\results\FxrOEcNa6P.bin
--- path metrics (from log) ---
[30Jul2026 22:01:10.084] [Worker-Main-17/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1920 l2=1898 l1_fail=0 biome=56664 surface_skip=0 l1_avg_us=83829 direct_writes=63546514 sections_touched=17745 sample_pct=80 write_pct=19 specialize=3671689 nc1_s1=0 nc1_s2=3017 nc1_s3=0 nc3_grid=3017 nc5_ore=3017 aq_spec=3017 spec_deep=0 spec_nodes=3017 vanilla_arith=0 l0_reasons{}
[30Jul2026 22:01:12.767] [Worker-Main-19/INFO] [noisiumed/]: noisiumed.path l0=0 l1=2048 l2=2044 l1_fail=0 biome=59496 surface_skip=0 l1_avg_us=81601 direct_writes=67807252 sections_touched=18952 sample_pct=80 write_pct=19 specialize=3816512 nc1_s1=0 nc1_s2=3136 nc1_s3=0 nc3_grid=3136 nc5_ore=3136 aq_spec=3136 spec_deep=0 spec_nodes=3136 vanilla_arith=0 l0_reasons{}
[30Jul2026 22:01:14.891] [Worker-Main-30/INFO] [noisiumed/]: noisiumed.path l0=0 l1=2176 l2=2145 l1_fail=0 biome=62784 surface_skip=0 l1_avg_us=79380 direct_writes=72549721 sections_touched=20260 sample_pct=81 write_pct=18 specialize=3992977 nc1_s1=0 nc1_s2=3281 nc1_s3=0 nc3_grid=3281 nc5_ore=3281 aq_spec=3281 spec_deep=0 spec_nodes=3281 vanilla_arith=0 l0_reasons{}
--- rcon spark profiler stop ---

--- rcon spark tps ---

--- rcon spark health ---

--- matched log lines ---
[22:00:00.665] [main/INFO] [mixin/]: SpongePowered MIXIN Subsystem Version=0.8.7 Source=union:/C:/Users/imba/Git/infernos/minecraft-mods/noisium/bench/run-noisiumed/libraries/net/fabricmc/sponge-mixin/0.15.2+mixin.0.8.7/sponge-mixin-0.15.2+mixin.0.8.7.jar%2373!/ Service=ModLauncher Env=SERVER 
[22:00:01.221] [main/INFO] [loading.moddiscovery.ModDiscoverer/SCAN]: Found mod file "noisiumed-4.0.0-beta.16.5-w5-neoforge-1.21.1.jar" [locator: {mods folder locator at C:\Users\imba\Git\infernos\minecraft-mods\noisium\bench\run-noisiumed\mods}, reader: mod manifest] 
[22:00:01.230] [main/INFO] [loading.moddiscovery.ModDiscoverer/SCAN]: Found mod file "spark-1.10.124-neoforge.jar" [locator: {mods folder locator at C:\Users\imba\Git\infernos\minecraft-mods\noisium\bench\run-noisiumed\mods}, reader: mod manifest] 
		Noisiumed 4.0.0-beta.16.5-w5-neoforge-1.21.1 (noisiumed)
		spark 1.10.124 (spark) 
[22:00:12] [modloading-worker-0/INFO] [noisiumed/]: Loading Noisiumed (L0�L2 + density specializer + surface/coverage). Metrics: PathMetrics.snapshot()
[22:00:13] [main/INFO] [mojang/YggdrasilAuthenticationService]: Environment: Environment[sessionHost=https://sessionserver.mojang.com, servicesHost=https://api.minecraftservices.com, name=PROD]
[22:00:14] [main/WARN] [minecraft/VanillaPackResourcesBuilder]: Assets URL 'union:/C:/Users/imba/Git/infernos/minecraft-mods/noisium/bench/run-noisiumed/libraries/net/minecraft/server/1.21.1-20240808.144430/server-1.21.1-20240808.144430-srg.jar%23119!/assets/.mcassetsroot' uses unexpected schema
[22:00:14] [main/WARN] [minecraft/VanillaPackResourcesBuilder]: Assets URL 'union:/C:/Users/imba/Git/infernos/minecraft-mods/noisium/bench/run-noisiumed/libraries/net/minecraft/server/1.21.1-20240808.144430/server-1.21.1-20240808.144430-srg.jar%23119!/data/.mcassetsroot' uses unexpected schema
[22:00:17] [Server thread/INFO] [spark/]: Starting background profiler...
[22:00:17] [Server thread/INFO] [spark/]: The async-profiler engine is not supported for your os/arch (windows11/amd64), so the built-in Java engine will be used instead.
[22:00:29] [spark-worker-pool-1-thread-1/INFO] [minecraft/MinecraftServer]: [?] Starting a new profiler, please wait...
[22:00:29] [spark-worker-pool-1-thread-1/INFO] [spark/]: The async-profiler engine is not supported for your os/arch (windows11/amd64), so the built-in Java engine will be used instead.
[22:00:29] [spark-worker-pool-1-thread-1/INFO] [minecraft/MinecraftServer]: [?] Profiler is now running! (built-in java)
[22:00:29] [spark-worker-pool-1-thread-1/INFO] [minecraft/MinecraftServer]: [?] It will run in the background until it is stopped by an admin.
[22:00:29] [spark-worker-pool-1-thread-1/INFO] [minecraft/MinecraftServer]: [?] To stop the profiler and upload the results, run:
[22:00:29] [spark-worker-pool-1-thread-1/INFO] [minecraft/MinecraftServer]: [?]   /spark profiler stop
[22:00:29] [spark-worker-pool-1-thread-1/INFO] [minecraft/MinecraftServer]: [?] To view the profiler while it's running, run:
[22:00:29] [spark-worker-pool-1-thread-1/INFO] [minecraft/MinecraftServer]: [?]   /spark profiler open
[22:00:30] [Worker-Main-12/INFO] [noisiumed/]: noisiumed.path l0=0 l1=128 l2=121 l1_fail=0 biome=5232 surface_skip=0 l1_avg_us=363770 direct_writes=5167546 sections_touched=1452 sample_pct=73 write_pct=26 specialize=311552 nc1_s1=0 nc1_s2=256 nc1_s3=0 nc3_grid=256 nc5_ore=256 aq_spec=256 spec_deep=0 spec_nodes=256 vanilla_arith=0 l0_reasons{}
[22:00:33] [Worker-Main-21/INFO] [noisiumed/]: noisiumed.path l0=0 l1=256 l2=239 l1_fail=0 biome=8544 surface_skip=0 l1_avg_us=221076 direct_writes=9089341 sections_touched=2570 sample_pct=73 write_pct=26 specialize=506272 nc1_s1=0 nc1_s2=416 nc1_s3=0 nc3_grid=416 nc5_ore=416 aq_spec=416 spec_deep=0 spec_nodes=416 vanilla_arith=0 l0_reasons{}
[22:00:36] [Worker-Main-17/INFO] [noisiumed/]: noisiumed.path l0=0 l1=384 l2=370 l1_fail=0 biome=12408 surface_skip=0 l1_avg_us=182179 direct_writes=12735881 sections_touched=3658 sample_pct=74 write_pct=25 specialize=719247 nc1_s1=0 nc1_s2=591 nc1_s3=0 nc3_grid=591 nc5_ore=591 aq_spec=591 spec_deep=0 spec_nodes=591 vanilla_arith=0 l0_reasons{}
[22:00:38] [Worker-Main-28/INFO] [noisiumed/]: noisiumed.path l0=0 l1=512 l2=509 l1_fail=0 biome=16056 surface_skip=0 l1_avg_us=148355 direct_writes=16538831 sections_touched=4746 sample_pct=76 write_pct=23 specialize=920954 nc1_s1=0 nc1_s2=756 nc1_s3=0 nc3_grid=756 nc5_ore=756 aq_spec=757 spec_deep=0 spec_nodes=757 vanilla_arith=0 l0_reasons{}
[22:00:41] [Server thread/INFO] [minecraft/MinecraftServer]: [Rcon: Marked 225 chunks in Overworld from [121, 121] to [135, 135] to be force loaded]
[22:00:41] [Worker-Main-30/INFO] [noisiumed/]: noisiumed.path l0=0 l1=640 l2=626 l1_fail=0 biome=19056 surface_skip=0 l1_avg_us=129553 direct_writes=20510591 sections_touched=5834 sample_pct=76 write_pct=23 specialize=1271765 nc1_s1=0 nc1_s2=1045 nc1_s3=0 nc3_grid=1045 nc5_ore=1045 aq_spec=1045 spec_deep=0 spec_nodes=1045 vanilla_arith=0 l0_reasons{}
[22:00:45] [Worker-Main-3/INFO] [noisiumed/]: noisiumed.path l0=0 l1=768 l2=758 l1_fail=0 biome=23280 surface_skip=0 l1_avg_us=123261 direct_writes=24615951 sections_touched=6930 sample_pct=76 write_pct=23 specialize=1530986 nc1_s1=0 nc1_s2=1258 nc1_s3=0 nc3_grid=1258 nc5_ore=1258 aq_spec=1258 spec_deep=0 spec_nodes=1258 vanilla_arith=0 l0_reasons{}
[22:00:47] [Worker-Main-7/INFO] [noisiumed/]: noisiumed.path l0=0 l1=897 l2=871 l1_fail=0 biome=26856 surface_skip=0 l1_avg_us=114000 direct_writes=28804934 sections_touched=8056 sample_pct=77 write_pct=22 specialize=1722055 nc1_s1=0 nc1_s2=1415 nc1_s3=0 nc3_grid=1415 nc5_ore=1415 aq_spec=1415 spec_deep=0 spec_nodes=1415 vanilla_arith=0 l0_reasons{}
[22:00:50] [Worker-Main-19/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1024 l2=997 l1_fail=0 biome=30432 surface_skip=0 l1_avg_us=105522 direct_writes=32981950 sections_touched=9213 sample_pct=77 write_pct=22 specialize=1919209 nc1_s1=0 nc1_s2=1577 nc1_s3=0 nc3_grid=1577 nc5_ore=1577 aq_spec=1577 spec_deep=0 spec_nodes=1577 vanilla_arith=0 l0_reasons{}
[22:00:52] [Server thread/INFO] [minecraft/MinecraftServer]: [Rcon: Marked 225 chunks in Overworld from [153, 121] to [167, 135] to be force loaded]
[22:00:52] [Worker-Main-6/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1152 l2=1146 l1_fail=0 biome=33432 surface_skip=0 l1_avg_us=98406 direct_writes=37288758 sections_touched=10394 sample_pct=78 write_pct=21 specialize=2083504 nc1_s1=0 nc1_s2=1712 nc1_s3=0 nc3_grid=1712 nc5_ore=1712 aq_spec=1712 spec_deep=0 spec_nodes=1712 vanilla_arith=0 l0_reasons{}
[22:00:55] [Worker-Main-19/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1280 l2=1278 l1_fail=0 biome=37752 surface_skip=0 l1_avg_us=93403 direct_writes=41411313 sections_touched=11480 sample_pct=78 write_pct=21 specialize=2315951 nc1_s1=0 nc1_s2=1903 nc1_s3=0 nc3_grid=1903 nc5_ore=1903 aq_spec=1903 spec_deep=0 spec_nodes=1903 vanilla_arith=0 l0_reasons{}
[22:00:56] [Worker-Main-5/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1408 l2=1378 l1_fail=0 biome=41472 surface_skip=0 l1_avg_us=89511 direct_writes=45526631 sections_touched=12664 sample_pct=78 write_pct=21 specialize=2526492 nc1_s1=0 nc1_s2=2076 nc1_s3=0 nc3_grid=2076 nc5_ore=2076 aq_spec=2076 spec_deep=0 spec_nodes=2076 vanilla_arith=0 l0_reasons{}
[22:01:00] [Worker-Main-4/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1536 l2=1521 l1_fail=0 biome=45216 surface_skip=0 l1_avg_us=86126 direct_writes=49972695 sections_touched=13915 sample_pct=79 write_pct=20 specialize=2719995 nc1_s1=0 nc1_s2=2235 nc1_s3=0 nc3_grid=2235 nc5_ore=2235 aq_spec=2235 spec_deep=0 spec_nodes=2235 vanilla_arith=0 l0_reasons{}
[22:01:03] [Worker-Main-1/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1664 l2=1650 l1_fail=0 biome=48115 surface_skip=0 l1_avg_us=84206 direct_writes=54195987 sections_touched=15132 sample_pct=79 write_pct=20 specialize=2878261 nc1_s1=0 nc1_s2=2365 nc1_s3=0 nc3_grid=2365 nc5_ore=2365 aq_spec=2365 spec_deep=0 spec_nodes=2365 vanilla_arith=0 l0_reasons{}
[22:01:03] [Server thread/INFO] [minecraft/MinecraftServer]: [Rcon: Marked 225 chunks in Overworld from [121, 153] to [135, 167] to be force loaded]
[22:01:07] [Worker-Main-31/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1792 l2=1780 l1_fail=0 biome=52488 surface_skip=0 l1_avg_us=85914 direct_writes=59091439 sections_touched=16474 sample_pct=80 write_pct=19 specialize=3451412 nc1_s1=0 nc1_s2=2836 nc1_s3=0 nc3_grid=2836 nc5_ore=2836 aq_spec=2836 spec_deep=0 spec_nodes=2836 vanilla_arith=0 l0_reasons{}
[22:01:10] [Worker-Main-17/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1920 l2=1898 l1_fail=0 biome=56664 surface_skip=0 l1_avg_us=83829 direct_writes=63546514 sections_touched=17745 sample_pct=80 write_pct=19 specialize=3671689 nc1_s1=0 nc1_s2=3017 nc1_s3=0 nc3_grid=3017 nc5_ore=3017 aq_spec=3017 spec_deep=0 spec_nodes=3017 vanilla_arith=0 l0_reasons{}
[22:01:12] [Worker-Main-19/INFO] [noisiumed/]: noisiumed.path l0=0 l1=2048 l2=2044 l1_fail=0 biome=59496 surface_skip=0 l1_avg_us=81601 direct_writes=67807252 sections_touched=18952 sample_pct=80 write_pct=19 specialize=3816512 nc1_s1=0 nc1_s2=3136 nc1_s3=0 nc3_grid=3136 nc5_ore=3136 aq_spec=3136 spec_deep=0 spec_nodes=3136 vanilla_arith=0 l0_reasons{}
[22:01:14] [Worker-Main-30/INFO] [noisiumed/]: noisiumed.path l0=0 l1=2176 l2=2145 l1_fail=0 biome=62784 surface_skip=0 l1_avg_us=79380 direct_writes=72549721 sections_touched=20260 sample_pct=81 write_pct=18 specialize=3992977 nc1_s1=0 nc1_s2=3281 nc1_s3=0 nc3_grid=3281 nc5_ore=3281 aq_spec=3281 spec_deep=0 spec_nodes=3281 vanilla_arith=0 l0_reasons{}
[22:01:15] [Server thread/INFO] [minecraft/MinecraftServer]: [Rcon: Marked 225 chunks in Overworld from [153, 153] to [167, 167] to be force loaded]
[22:02:56] [spark-worker-pool-1-thread-3/INFO] [minecraft/MinecraftServer]: [?] Stopping the profiler & uploading results, please wait...
[22:02:58] [spark-worker-pool-1-thread-3/INFO] [spark/]: If you see a warning above that says "WARNING: A Java agent has been loaded dynamically", it can be safely ignored.
[22:02:58] [spark-worker-pool-1-thread-3/INFO] [spark/]: See here for more information: https://spark.lucko.me/docs/misc/Java-agent-warning
[22:03:01] [spark-worker-pool-1-thread-1/WARN] [spark/]: A command execution has not completed after 5 seconds, it *might* be stuck. Trace: 
  java.base/sun.net.www.protocol.https.HttpsURLConnectionImpl.getHeaderField(HttpsURLConnectionImpl.java:260)
  TRANSFORMER/spark@1.10.124/me.lucko.spark.common.util.BytebinClient.postContent(BytebinClient.java:72)
  TRANSFORMER/spark@1.10.124/me.lucko.spark.common.util.BytebinClient.postContent(BytebinClient.java:84)
  TRANSFORMER/spark@1.10.124/me.lucko.spark.common.util.BytebinClient.postContent(BytebinClient.java:94)
  TRANSFORMER/spark@1.10.124/me.lucko.spark.common.command.modules.SamplerModule.handleUpload(SamplerModule.java:427)
  TRANSFORMER/spark@1.10.124/me.lucko.spark.common.command.modules.SamplerModule.profilerStop(SamplerModule.java:406)
  TRANSFORMER/spark@1.10.124/me.lucko.spark.common.command.modules.SamplerModule.profiler(SamplerModule.java:141)
  TRANSFORMER/spark@1.10.124/me.lucko.spark.common.SparkPlatform.executeCommand0(SparkPlatform.java:472)
  TRANSFORMER/spark@1.10.124/me.lucko.spark.common.SparkPlatform.lambda$executeCommand$3(SparkPlatform.java:370)
[22:03:01] [spark-worker-pool-1-thread-1/WARN] [spark/]: If the command subsequently completes without any errors, this warning should be ignored. :)
[22:03:02] [Server thread/INFO] [minecraft/MinecraftServer]: [Rcon: Unmarked all force loaded chunks in Overworld]
[22:03:02] [spark-worker-pool-1-thread-3/INFO] [minecraft/MinecraftServer]: [?] Profiler stopped & upload complete!
[22:03:02] [spark-worker-pool-1-thread-3/INFO] [minecraft/MinecraftServer]: https://spark.lucko.me/FxrOEcNa6P
[22:03:02] [spark-worker-pool-1-thread-3/INFO] [spark/]: The async-profiler engine is not supported for your os/arch (windows11/amd64), so the built-in Java engine will be used instead.
[22:03:02] [spark-worker-pool-1-thread-3/INFO] [minecraft/MinecraftServer]: [?] Restarted the background profiler. (If you don't want this to happen, run: /spark profiler cancel)
[22:03:04] [Server thread/INFO] [minecraft/MinecraftServer]: Saving chunks for level 'ServerLevel[world]'/minecraft:overworld
[22:03:09] [Server thread/INFO] [minecraft/MinecraftServer]: Saving chunks for level 'ServerLevel[world]'/minecraft:the_end
[22:03:09] [Server thread/INFO] [minecraft/MinecraftServer]: Saving chunks for level 'ServerLevel[world]'/minecraft:the_nether
[22:03:09] [Server thread/INFO] [minecraft/MinecraftServer]: ThreadedAnvilChunkStorage (world): All chunks are saved
[22:03:09] [Server thread/INFO] [minecraft/MinecraftServer]: ThreadedAnvilChunkStorage (DIM1): All chunks are saved
[22:03:09] [Server thread/INFO] [minecraft/MinecraftServer]: ThreadedAnvilChunkStorage (DIM-1): All chunks are saved
[22:03:09] [Server thread/INFO] [minecraft/MinecraftServer]: ThreadedAnvilChunkStorage: All dimensions are saved
[30Jul2026 22:00:00.665] [main/INFO] [mixin/]: SpongePowered MIXIN Subsystem Version=0.8.7 Source=union:/C:/Users/imba/Git/infernos/minecraft-mods/noisium/bench/run-noisiumed/libraries/net/fabricmc/sponge-mixin/0.15.2+mixin.0.8.7/sponge-mixin-0.15.2+mixin.0.8.7.jar%2373!/ Service=ModLauncher Env=SERVER
[30Jul2026 22:00:01.221] [main/INFO] [net.neoforged.fml.loading.moddiscovery.ModDiscoverer/SCAN]: Found mod file "noisiumed-4.0.0-beta.16.5-w5-neoforge-1.21.1.jar" [locator: {mods folder locator at C:\Users\imba\Git\infernos\minecraft-mods\noisium\bench\run-noisiumed\mods}, reader: mod manifest]
[30Jul2026 22:00:01.230] [main/INFO] [net.neoforged.fml.loading.moddiscovery.ModDiscoverer/SCAN]: Found mod file "spark-1.10.124-neoforge.jar" [locator: {mods folder locator at C:\Users\imba\Git\infernos\minecraft-mods\noisium\bench\run-noisiumed\mods}, reader: mod manifest]
		Noisiumed 4.0.0-beta.16.5-w5-neoforge-1.21.1 (noisiumed)
		spark 1.10.124 (spark)
[30Jul2026 22:00:12.935] [modloading-worker-0/INFO] [noisiumed/]: Loading Noisiumed (L0–L2 + density specializer + surface/coverage). Metrics: PathMetrics.snapshot()
[30Jul2026 22:00:13.931] [main/INFO] [com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService/]: Environment: Environment[sessionHost=https://sessionserver.mojang.com, servicesHost=https://api.minecraftservices.com, name=PROD]
[30Jul2026 22:00:14.019] [main/WARN] [net.minecraft.server.packs.VanillaPackResourcesBuilder/]: Assets URL 'union:/C:/Users/imba/Git/infernos/minecraft-mods/noisium/bench/run-noisiumed/libraries/net/minecraft/server/1.21.1-20240808.144430/server-1.21.1-20240808.144430-srg.jar%23119!/assets/.mcassetsroot' uses unexpected schema
[30Jul2026 22:00:14.019] [main/WARN] [net.minecraft.server.packs.VanillaPackResourcesBuilder/]: Assets URL 'union:/C:/Users/imba/Git/infernos/minecraft-mods/noisium/bench/run-noisiumed/libraries/net/minecraft/server/1.21.1-20240808.144430/server-1.21.1-20240808.144430-srg.jar%23119!/data/.mcassetsroot' uses unexpected schema
[30Jul2026 22:00:17.361] [Server thread/INFO] [spark/]: Starting background profiler...
[30Jul2026 22:00:17.386] [Server thread/INFO] [spark/]: The async-profiler engine is not supported for your os/arch (windows11/amd64), so the built-in Java engine will be used instead.
[30Jul2026 22:00:29.530] [spark-worker-pool-1-thread-1/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] Starting a new profiler, please wait...
[30Jul2026 22:00:29.530] [spark-worker-pool-1-thread-1/INFO] [spark/]: The async-profiler engine is not supported for your os/arch (windows11/amd64), so the built-in Java engine will be used instead.
[30Jul2026 22:00:29.535] [spark-worker-pool-1-thread-1/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] Profiler is now running! (built-in java)
[30Jul2026 22:00:29.535] [spark-worker-pool-1-thread-1/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] It will run in the background until it is stopped by an admin.
[30Jul2026 22:00:29.536] [spark-worker-pool-1-thread-1/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] To stop the profiler and upload the results, run:
[30Jul2026 22:00:29.540] [spark-worker-pool-1-thread-1/INFO] [net.minecraft.server.MinecraftServer/]: [⚡]   /spark profiler stop
[30Jul2026 22:00:29.541] [spark-worker-pool-1-thread-1/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] To view the profiler while it's running, run:
[30Jul2026 22:00:29.541] [spark-worker-pool-1-thread-1/INFO] [net.minecraft.server.MinecraftServer/]: [⚡]   /spark profiler open
[30Jul2026 22:00:30.678] [Worker-Main-12/INFO] [noisiumed/]: noisiumed.path l0=0 l1=128 l2=121 l1_fail=0 biome=5232 surface_skip=0 l1_avg_us=363770 direct_writes=5167546 sections_touched=1452 sample_pct=73 write_pct=26 specialize=311552 nc1_s1=0 nc1_s2=256 nc1_s3=0 nc3_grid=256 nc5_ore=256 aq_spec=256 spec_deep=0 spec_nodes=256 vanilla_arith=0 l0_reasons{}
[30Jul2026 22:00:33.530] [Worker-Main-21/INFO] [noisiumed/]: noisiumed.path l0=0 l1=256 l2=239 l1_fail=0 biome=8544 surface_skip=0 l1_avg_us=221076 direct_writes=9089341 sections_touched=2570 sample_pct=73 write_pct=26 specialize=506272 nc1_s1=0 nc1_s2=416 nc1_s3=0 nc3_grid=416 nc5_ore=416 aq_spec=416 spec_deep=0 spec_nodes=416 vanilla_arith=0 l0_reasons{}
[30Jul2026 22:00:36.643] [Worker-Main-17/INFO] [noisiumed/]: noisiumed.path l0=0 l1=384 l2=370 l1_fail=0 biome=12408 surface_skip=0 l1_avg_us=182179 direct_writes=12735881 sections_touched=3658 sample_pct=74 write_pct=25 specialize=719247 nc1_s1=0 nc1_s2=591 nc1_s3=0 nc3_grid=591 nc5_ore=591 aq_spec=591 spec_deep=0 spec_nodes=591 vanilla_arith=0 l0_reasons{}
[30Jul2026 22:00:38.976] [Worker-Main-28/INFO] [noisiumed/]: noisiumed.path l0=0 l1=512 l2=509 l1_fail=0 biome=16056 surface_skip=0 l1_avg_us=148355 direct_writes=16538831 sections_touched=4746 sample_pct=76 write_pct=23 specialize=920954 nc1_s1=0 nc1_s2=756 nc1_s3=0 nc3_grid=756 nc5_ore=756 aq_spec=757 spec_deep=0 spec_nodes=757 vanilla_arith=0 l0_reasons{}
[30Jul2026 22:00:41.258] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: [Rcon: Marked 225 chunks in Overworld from [121, 121] to [135, 135] to be force loaded]
[30Jul2026 22:00:41.947] [Worker-Main-30/INFO] [noisiumed/]: noisiumed.path l0=0 l1=640 l2=626 l1_fail=0 biome=19056 surface_skip=0 l1_avg_us=129553 direct_writes=20510591 sections_touched=5834 sample_pct=76 write_pct=23 specialize=1271765 nc1_s1=0 nc1_s2=1045 nc1_s3=0 nc3_grid=1045 nc5_ore=1045 aq_spec=1045 spec_deep=0 spec_nodes=1045 vanilla_arith=0 l0_reasons{}
[30Jul2026 22:00:45.550] [Worker-Main-3/INFO] [noisiumed/]: noisiumed.path l0=0 l1=768 l2=758 l1_fail=0 biome=23280 surface_skip=0 l1_avg_us=123261 direct_writes=24615951 sections_touched=6930 sample_pct=76 write_pct=23 specialize=1530986 nc1_s1=0 nc1_s2=1258 nc1_s3=0 nc3_grid=1258 nc5_ore=1258 aq_spec=1258 spec_deep=0 spec_nodes=1258 vanilla_arith=0 l0_reasons{}
[30Jul2026 22:00:47.780] [Worker-Main-7/INFO] [noisiumed/]: noisiumed.path l0=0 l1=897 l2=871 l1_fail=0 biome=26856 surface_skip=0 l1_avg_us=114000 direct_writes=28804934 sections_touched=8056 sample_pct=77 write_pct=22 specialize=1722055 nc1_s1=0 nc1_s2=1415 nc1_s3=0 nc3_grid=1415 nc5_ore=1415 aq_spec=1415 spec_deep=0 spec_nodes=1415 vanilla_arith=0 l0_reasons{}
[30Jul2026 22:00:50.046] [Worker-Main-19/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1024 l2=997 l1_fail=0 biome=30432 surface_skip=0 l1_avg_us=105522 direct_writes=32981950 sections_touched=9213 sample_pct=77 write_pct=22 specialize=1919209 nc1_s1=0 nc1_s2=1577 nc1_s3=0 nc3_grid=1577 nc5_ore=1577 aq_spec=1577 spec_deep=0 spec_nodes=1577 vanilla_arith=0 l0_reasons{}
[30Jul2026 22:00:52.657] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: [Rcon: Marked 225 chunks in Overworld from [153, 121] to [167, 135] to be force loaded]
[30Jul2026 22:00:52.705] [Worker-Main-6/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1152 l2=1146 l1_fail=0 biome=33432 surface_skip=0 l1_avg_us=98406 direct_writes=37288758 sections_touched=10394 sample_pct=78 write_pct=21 specialize=2083504 nc1_s1=0 nc1_s2=1712 nc1_s3=0 nc3_grid=1712 nc5_ore=1712 aq_spec=1712 spec_deep=0 spec_nodes=1712 vanilla_arith=0 l0_reasons{}
[30Jul2026 22:00:55.135] [Worker-Main-19/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1280 l2=1278 l1_fail=0 biome=37752 surface_skip=0 l1_avg_us=93403 direct_writes=41411313 sections_touched=11480 sample_pct=78 write_pct=21 specialize=2315951 nc1_s1=0 nc1_s2=1903 nc1_s3=0 nc3_grid=1903 nc5_ore=1903 aq_spec=1903 spec_deep=0 spec_nodes=1903 vanilla_arith=0 l0_reasons{}
[30Jul2026 22:00:56.965] [Worker-Main-5/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1408 l2=1378 l1_fail=0 biome=41472 surface_skip=0 l1_avg_us=89511 direct_writes=45526631 sections_touched=12664 sample_pct=78 write_pct=21 specialize=2526492 nc1_s1=0 nc1_s2=2076 nc1_s3=0 nc3_grid=2076 nc5_ore=2076 aq_spec=2076 spec_deep=0 spec_nodes=2076 vanilla_arith=0 l0_reasons{}
[30Jul2026 22:01:00.151] [Worker-Main-4/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1536 l2=1521 l1_fail=0 biome=45216 surface_skip=0 l1_avg_us=86126 direct_writes=49972695 sections_touched=13915 sample_pct=79 write_pct=20 specialize=2719995 nc1_s1=0 nc1_s2=2235 nc1_s3=0 nc3_grid=2235 nc5_ore=2235 aq_spec=2235 spec_deep=0 spec_nodes=2235 vanilla_arith=0 l0_reasons{}
[30Jul2026 22:01:03.080] [Worker-Main-1/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1664 l2=1650 l1_fail=0 biome=48115 surface_skip=0 l1_avg_us=84206 direct_writes=54195987 sections_touched=15132 sample_pct=79 write_pct=20 specialize=2878261 nc1_s1=0 nc1_s2=2365 nc1_s3=0 nc3_grid=2365 nc5_ore=2365 aq_spec=2365 spec_deep=0 spec_nodes=2365 vanilla_arith=0 l0_reasons{}
[30Jul2026 22:01:03.638] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: [Rcon: Marked 225 chunks in Overworld from [121, 153] to [135, 167] to be force loaded]
[30Jul2026 22:01:07.635] [Worker-Main-31/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1792 l2=1780 l1_fail=0 biome=52488 surface_skip=0 l1_avg_us=85914 direct_writes=59091439 sections_touched=16474 sample_pct=80 write_pct=19 specialize=3451412 nc1_s1=0 nc1_s2=2836 nc1_s3=0 nc3_grid=2836 nc5_ore=2836 aq_spec=2836 spec_deep=0 spec_nodes=2836 vanilla_arith=0 l0_reasons{}
[30Jul2026 22:01:10.084] [Worker-Main-17/INFO] [noisiumed/]: noisiumed.path l0=0 l1=1920 l2=1898 l1_fail=0 biome=56664 surface_skip=0 l1_avg_us=83829 direct_writes=63546514 sections_touched=17745 sample_pct=80 write_pct=19 specialize=3671689 nc1_s1=0 nc1_s2=3017 nc1_s3=0 nc3_grid=3017 nc5_ore=3017 aq_spec=3017 spec_deep=0 spec_nodes=3017 vanilla_arith=0 l0_reasons{}
[30Jul2026 22:01:12.767] [Worker-Main-19/INFO] [noisiumed/]: noisiumed.path l0=0 l1=2048 l2=2044 l1_fail=0 biome=59496 surface_skip=0 l1_avg_us=81601 direct_writes=67807252 sections_touched=18952 sample_pct=80 write_pct=19 specialize=3816512 nc1_s1=0 nc1_s2=3136 nc1_s3=0 nc3_grid=3136 nc5_ore=3136 aq_spec=3136 spec_deep=0 spec_nodes=3136 vanilla_arith=0 l0_reasons{}
[30Jul2026 22:01:14.891] [Worker-Main-30/INFO] [noisiumed/]: noisiumed.path l0=0 l1=2176 l2=2145 l1_fail=0 biome=62784 surface_skip=0 l1_avg_us=79380 direct_writes=72549721 sections_touched=20260 sample_pct=81 write_pct=18 specialize=3992977 nc1_s1=0 nc1_s2=3281 nc1_s3=0 nc3_grid=3281 nc5_ore=3281 aq_spec=3281 spec_deep=0 spec_nodes=3281 vanilla_arith=0 l0_reasons{}
[30Jul2026 22:01:15.837] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: [Rcon: Marked 225 chunks in Overworld from [153, 153] to [167, 167] to be force loaded]
[30Jul2026 22:02:56.722] [spark-worker-pool-1-thread-3/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] Stopping the profiler & uploading results, please wait...
[30Jul2026 22:02:58.646] [spark-worker-pool-1-thread-3/INFO] [spark/]: If you see a warning above that says "WARNING: A Java agent has been loaded dynamically", it can be safely ignored.
[30Jul2026 22:02:58.646] [spark-worker-pool-1-thread-3/INFO] [spark/]: See here for more information: https://spark.lucko.me/docs/misc/Java-agent-warning
[30Jul2026 22:03:01.718] [spark-worker-pool-1-thread-1/WARN] [spark/]: A command execution has not completed after 5 seconds, it *might* be stuck. Trace: 
  java.base/sun.net.www.protocol.https.HttpsURLConnectionImpl.getHeaderField(HttpsURLConnectionImpl.java:260)
  TRANSFORMER/spark@1.10.124/me.lucko.spark.common.util.BytebinClient.postContent(BytebinClient.java:72)
  TRANSFORMER/spark@1.10.124/me.lucko.spark.common.util.BytebinClient.postContent(BytebinClient.java:84)
  TRANSFORMER/spark@1.10.124/me.lucko.spark.common.util.BytebinClient.postContent(BytebinClient.java:94)
  TRANSFORMER/spark@1.10.124/me.lucko.spark.common.command.modules.SamplerModule.handleUpload(SamplerModule.java:427)
  TRANSFORMER/spark@1.10.124/me.lucko.spark.common.command.modules.SamplerModule.profilerStop(SamplerModule.java:406)
  TRANSFORMER/spark@1.10.124/me.lucko.spark.common.command.modules.SamplerModule.profiler(SamplerModule.java:141)
  TRANSFORMER/spark@1.10.124/me.lucko.spark.common.SparkPlatform.executeCommand0(SparkPlatform.java:472)
  TRANSFORMER/spark@1.10.124/me.lucko.spark.common.SparkPlatform.lambda$executeCommand$3(SparkPlatform.java:370)
[30Jul2026 22:03:01.719] [spark-worker-pool-1-thread-1/WARN] [spark/]: If the command subsequently completes without any errors, this warning should be ignored. :)
[30Jul2026 22:03:02.192] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: [Rcon: Unmarked all force loaded chunks in Overworld]
[30Jul2026 22:03:02.846] [spark-worker-pool-1-thread-3/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] Profiler stopped & upload complete!
[30Jul2026 22:03:02.847] [spark-worker-pool-1-thread-3/INFO] [net.minecraft.server.MinecraftServer/]: https://spark.lucko.me/FxrOEcNa6P
[30Jul2026 22:03:02.853] [spark-worker-pool-1-thread-3/INFO] [spark/]: The async-profiler engine is not supported for your os/arch (windows11/amd64), so the built-in Java engine will be used instead.
[30Jul2026 22:03:02.856] [spark-worker-pool-1-thread-3/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] Restarted the background profiler. (If you don't want this to happen, run: /spark profiler cancel)
[30Jul2026 22:03:04.612] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: Saving chunks for level 'ServerLevel[world]'/minecraft:overworld
[30Jul2026 22:03:09.391] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: Saving chunks for level 'ServerLevel[world]'/minecraft:the_end
[30Jul2026 22:03:09.411] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: Saving chunks for level 'ServerLevel[world]'/minecraft:the_nether
[30Jul2026 22:03:09.447] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: ThreadedAnvilChunkStorage (world): All chunks are saved
[30Jul2026 22:03:09.447] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: ThreadedAnvilChunkStorage (DIM1): All chunks are saved
[30Jul2026 22:03:09.447] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: ThreadedAnvilChunkStorage (DIM-1): All chunks are saved
[30Jul2026 22:03:09.447] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: ThreadedAnvilChunkStorage: All dimensions are saved

```

## Fast Noise 1.0.13
```
label=fastnoise
seed=12345 radiusChunks=7 profileSeconds=100 port=25571
wall_boot_ms=47123 wall_forceload_ms=117947 wall_profile_window_ms=218212 wall_total_ms=397529
spark_url=https://spark.lucko.me/A96ixnMFqr
spark_bin=C:\Users\imba\Git\infernos\minecraft-mods\noisium\bench\results\A96ixnMFqr.bin
--- path metrics (from log) ---
--- rcon spark profiler stop ---

--- rcon spark tps ---

--- rcon spark health ---

--- matched log lines ---
[22:05:18.684] [main/INFO] [mixin/]: SpongePowered MIXIN Subsystem Version=0.8.7 Source=union:/C:/Users/imba/Git/infernos/minecraft-mods/noisium/bench/run-fastnoise/libraries/net/fabricmc/sponge-mixin/0.15.2+mixin.0.8.7/sponge-mixin-0.15.2+mixin.0.8.7.jar%2373!/ Service=ModLauncher Env=SERVER 
[22:05:19.651] [main/INFO] [loading.moddiscovery.ModDiscoverer/SCAN]: Found mod file "spark-1.10.124-neoforge.jar" [locator: {mods folder locator at C:\Users\imba\Git\infernos\minecraft-mods\noisium\bench\run-fastnoise\mods}, reader: mod manifest] 
[22:05:19.665] [main/INFO] [loading.moddiscovery.ModDiscoverer/SCAN]: Found mod file "zfastnoise-1.0.13+1.21.1+neoforge.jar" [locator: {mods folder locator at C:\Users\imba\Git\infernos\minecraft-mods\noisium\bench\run-fastnoise\mods}, reader: mod manifest] 
		Fast Noise Mod 1.0.13+1.21.1+neoforge (zfastnoise)
		spark 1.10.124 (spark) 
[22:05:39] [modloading-worker-0/INFO] [zfastnoise/]: Hello Fabric world!
[22:05:39] [main/INFO] [mojang/YggdrasilAuthenticationService]: Environment: Environment[sessionHost=https://sessionserver.mojang.com, servicesHost=https://api.minecraftservices.com, name=PROD]
[22:05:40] [main/WARN] [minecraft/VanillaPackResourcesBuilder]: Assets URL 'union:/C:/Users/imba/Git/infernos/minecraft-mods/noisium/bench/run-fastnoise/libraries/net/minecraft/server/1.21.1-20240808.144430/server-1.21.1-20240808.144430-srg.jar%23119!/assets/.mcassetsroot' uses unexpected schema
[22:05:40] [main/WARN] [minecraft/VanillaPackResourcesBuilder]: Assets URL 'union:/C:/Users/imba/Git/infernos/minecraft-mods/noisium/bench/run-fastnoise/libraries/net/minecraft/server/1.21.1-20240808.144430/server-1.21.1-20240808.144430-srg.jar%23119!/data/.mcassetsroot' uses unexpected schema
[22:05:46] [Server thread/INFO] [spark/]: Starting background profiler...
[22:05:46] [Server thread/INFO] [spark/]: The async-profiler engine is not supported for your os/arch (windows11/amd64), so the built-in Java engine will be used instead.
[22:06:02] [spark-worker-pool-1-thread-1/INFO] [minecraft/MinecraftServer]: [?] Starting a new profiler, please wait...
[22:06:02] [spark-worker-pool-1-thread-1/INFO] [spark/]: The async-profiler engine is not supported for your os/arch (windows11/amd64), so the built-in Java engine will be used instead.
[22:06:02] [spark-worker-pool-1-thread-1/INFO] [minecraft/MinecraftServer]: [?] Profiler is now running! (built-in java)
[22:06:02] [spark-worker-pool-1-thread-1/INFO] [minecraft/MinecraftServer]: [?] It will run in the background until it is stopped by an admin.
[22:06:02] [spark-worker-pool-1-thread-1/INFO] [minecraft/MinecraftServer]: [?] To stop the profiler and upload the results, run:
[22:06:02] [spark-worker-pool-1-thread-1/INFO] [minecraft/MinecraftServer]: [?]   /spark profiler stop
[22:06:02] [spark-worker-pool-1-thread-1/INFO] [minecraft/MinecraftServer]: [?] To view the profiler while it's running, run:
[22:06:02] [spark-worker-pool-1-thread-1/INFO] [minecraft/MinecraftServer]: [?]   /spark profiler open
[22:06:32] [Server thread/INFO] [minecraft/MinecraftServer]: [Rcon: Marked 225 chunks in Overworld from [121, 121] to [135, 135] to be force loaded]
[22:07:03] [Server thread/INFO] [minecraft/MinecraftServer]: [Rcon: Marked 225 chunks in Overworld from [153, 121] to [167, 135] to be force loaded]
[22:07:24] [Server thread/INFO] [minecraft/MinecraftServer]: [Rcon: Marked 225 chunks in Overworld from [121, 153] to [135, 167] to be force loaded]
[22:07:59] [Server thread/INFO] [minecraft/MinecraftServer]: [Rcon: Marked 225 chunks in Overworld from [153, 153] to [167, 167] to be force loaded]
[22:09:40] [spark-worker-pool-1-thread-3/INFO] [minecraft/MinecraftServer]: [?] Stopping the profiler & uploading results, please wait...
[22:09:42] [spark-worker-pool-1-thread-3/INFO] [spark/]: If you see a warning above that says "WARNING: A Java agent has been loaded dynamically", it can be safely ignored.
[22:09:42] [spark-worker-pool-1-thread-3/INFO] [spark/]: See here for more information: https://spark.lucko.me/docs/misc/Java-agent-warning
[22:09:45] [spark-worker-pool-1-thread-1/WARN] [spark/]: A command execution has not completed after 5 seconds, it *might* be stuck. Trace: 
  java.base/sun.net.www.protocol.https.HttpsClient.<init>(HttpsClient.java:264)
  java.base/sun.net.www.protocol.https.HttpsClient.New(HttpsClient.java:377)
  java.base/sun.net.www.protocol.https.AbstractDelegateHttpsURLConnection.getNewHttpClient(AbstractDelegateHttpsURLConnection.java:193)
  java.base/sun.net.www.protocol.https.AbstractDelegateHttpsURLConnection.connect(AbstractDelegateHttpsURLConnection.java:179)
  java.base/sun.net.www.protocol.https.HttpsURLConnectionImpl.connect(HttpsURLConnectionImpl.java:141)
  TRANSFORMER/spark@1.10.124/me.lucko.spark.common.util.BytebinClient.postContent(BytebinClient.java:67)
  TRANSFORMER/spark@1.10.124/me.lucko.spark.common.util.BytebinClient.postContent(BytebinClient.java:84)
  TRANSFORMER/spark@1.10.124/me.lucko.spark.common.util.BytebinClient.postContent(BytebinClient.java:94)
  TRANSFORMER/spark@1.10.124/me.lucko.spark.common.command.modules.SamplerModule.handleUpload(SamplerModule.java:427)
  TRANSFORMER/spark@1.10.124/me.lucko.spark.common.command.modules.SamplerModule.profilerStop(SamplerModule.java:406)
  TRANSFORMER/spark@1.10.124/me.lucko.spark.common.command.modules.SamplerModule.profiler(SamplerModule.java:141)
  TRANSFORMER/spark@1.10.124/me.lucko.spark.common.SparkPlatform.executeCommand0(SparkPlatform.java:472)
  TRANSFORMER/spark@1.10.124/me.lucko.spark.common.SparkPlatform.lambda$executeCommand$3(SparkPlatform.java:370)
[22:09:45] [spark-worker-pool-1-thread-1/WARN] [spark/]: If the command subsequently completes without any errors, this warning should be ignored. :)
[22:09:46] [Server thread/INFO] [minecraft/MinecraftServer]: [Rcon: Unmarked all force loaded chunks in Overworld]
[22:09:48] [spark-worker-pool-1-thread-3/INFO] [minecraft/MinecraftServer]: [?] Profiler stopped & upload complete!
[22:09:48] [spark-worker-pool-1-thread-3/INFO] [minecraft/MinecraftServer]: https://spark.lucko.me/A96ixnMFqr
[22:09:48] [spark-worker-pool-1-thread-3/INFO] [spark/]: The async-profiler engine is not supported for your os/arch (windows11/amd64), so the built-in Java engine will be used instead.
[22:09:48] [spark-worker-pool-1-thread-3/INFO] [minecraft/MinecraftServer]: [?] Restarted the background profiler. (If you don't want this to happen, run: /spark profiler cancel)
[22:09:51] [Server thread/INFO] [minecraft/MinecraftServer]: Saving chunks for level 'ServerLevel[world]'/minecraft:overworld
[22:09:57] [Server thread/INFO] [minecraft/MinecraftServer]: Saving chunks for level 'ServerLevel[world]'/minecraft:the_nether
[22:09:57] [Server thread/INFO] [minecraft/MinecraftServer]: Saving chunks for level 'ServerLevel[world]'/minecraft:the_end
[22:09:57] [Server thread/INFO] [minecraft/MinecraftServer]: ThreadedAnvilChunkStorage (world): All chunks are saved
[22:09:57] [Server thread/INFO] [minecraft/MinecraftServer]: ThreadedAnvilChunkStorage (DIM-1): All chunks are saved
[22:09:57] [Server thread/INFO] [minecraft/MinecraftServer]: ThreadedAnvilChunkStorage (DIM1): All chunks are saved
[22:09:57] [Server thread/INFO] [minecraft/MinecraftServer]: ThreadedAnvilChunkStorage: All dimensions are saved
[30Jul2026 22:05:18.684] [main/INFO] [mixin/]: SpongePowered MIXIN Subsystem Version=0.8.7 Source=union:/C:/Users/imba/Git/infernos/minecraft-mods/noisium/bench/run-fastnoise/libraries/net/fabricmc/sponge-mixin/0.15.2+mixin.0.8.7/sponge-mixin-0.15.2+mixin.0.8.7.jar%2373!/ Service=ModLauncher Env=SERVER
[30Jul2026 22:05:19.651] [main/INFO] [net.neoforged.fml.loading.moddiscovery.ModDiscoverer/SCAN]: Found mod file "spark-1.10.124-neoforge.jar" [locator: {mods folder locator at C:\Users\imba\Git\infernos\minecraft-mods\noisium\bench\run-fastnoise\mods}, reader: mod manifest]
[30Jul2026 22:05:19.665] [main/INFO] [net.neoforged.fml.loading.moddiscovery.ModDiscoverer/SCAN]: Found mod file "zfastnoise-1.0.13+1.21.1+neoforge.jar" [locator: {mods folder locator at C:\Users\imba\Git\infernos\minecraft-mods\noisium\bench\run-fastnoise\mods}, reader: mod manifest]
		Fast Noise Mod 1.0.13+1.21.1+neoforge (zfastnoise)
		spark 1.10.124 (spark)
[30Jul2026 22:05:39.502] [modloading-worker-0/INFO] [zfastnoise/]: Hello Fabric world!
[30Jul2026 22:05:39.931] [main/INFO] [com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService/]: Environment: Environment[sessionHost=https://sessionserver.mojang.com, servicesHost=https://api.minecraftservices.com, name=PROD]
[30Jul2026 22:05:40.112] [main/WARN] [net.minecraft.server.packs.VanillaPackResourcesBuilder/]: Assets URL 'union:/C:/Users/imba/Git/infernos/minecraft-mods/noisium/bench/run-fastnoise/libraries/net/minecraft/server/1.21.1-20240808.144430/server-1.21.1-20240808.144430-srg.jar%23119!/assets/.mcassetsroot' uses unexpected schema
[30Jul2026 22:05:40.113] [main/WARN] [net.minecraft.server.packs.VanillaPackResourcesBuilder/]: Assets URL 'union:/C:/Users/imba/Git/infernos/minecraft-mods/noisium/bench/run-fastnoise/libraries/net/minecraft/server/1.21.1-20240808.144430/server-1.21.1-20240808.144430-srg.jar%23119!/data/.mcassetsroot' uses unexpected schema
[30Jul2026 22:05:46.584] [Server thread/INFO] [spark/]: Starting background profiler...
[30Jul2026 22:05:46.611] [Server thread/INFO] [spark/]: The async-profiler engine is not supported for your os/arch (windows11/amd64), so the built-in Java engine will be used instead.
[30Jul2026 22:06:02.519] [spark-worker-pool-1-thread-1/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] Starting a new profiler, please wait...
[30Jul2026 22:06:02.521] [spark-worker-pool-1-thread-1/INFO] [spark/]: The async-profiler engine is not supported for your os/arch (windows11/amd64), so the built-in Java engine will be used instead.
[30Jul2026 22:06:02.523] [spark-worker-pool-1-thread-1/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] Profiler is now running! (built-in java)
[30Jul2026 22:06:02.524] [spark-worker-pool-1-thread-1/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] It will run in the background until it is stopped by an admin.
[30Jul2026 22:06:02.525] [spark-worker-pool-1-thread-1/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] To stop the profiler and upload the results, run:
[30Jul2026 22:06:02.528] [spark-worker-pool-1-thread-1/INFO] [net.minecraft.server.MinecraftServer/]: [⚡]   /spark profiler stop
[30Jul2026 22:06:02.530] [spark-worker-pool-1-thread-1/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] To view the profiler while it's running, run:
[30Jul2026 22:06:02.530] [spark-worker-pool-1-thread-1/INFO] [net.minecraft.server.MinecraftServer/]: [⚡]   /spark profiler open
[30Jul2026 22:06:32.126] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: [Rcon: Marked 225 chunks in Overworld from [121, 121] to [135, 135] to be force loaded]
[30Jul2026 22:07:03.487] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: [Rcon: Marked 225 chunks in Overworld from [153, 121] to [167, 135] to be force loaded]
[30Jul2026 22:07:24.641] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: [Rcon: Marked 225 chunks in Overworld from [121, 153] to [135, 167] to be force loaded]
[30Jul2026 22:07:59.797] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: [Rcon: Marked 225 chunks in Overworld from [153, 153] to [167, 167] to be force loaded]
[30Jul2026 22:09:40.882] [spark-worker-pool-1-thread-3/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] Stopping the profiler & uploading results, please wait...
[30Jul2026 22:09:42.643] [spark-worker-pool-1-thread-3/INFO] [spark/]: If you see a warning above that says "WARNING: A Java agent has been loaded dynamically", it can be safely ignored.
[30Jul2026 22:09:42.643] [spark-worker-pool-1-thread-3/INFO] [spark/]: See here for more information: https://spark.lucko.me/docs/misc/Java-agent-warning
[30Jul2026 22:09:45.880] [spark-worker-pool-1-thread-1/WARN] [spark/]: A command execution has not completed after 5 seconds, it *might* be stuck. Trace: 
  java.base/sun.net.www.protocol.https.HttpsClient.<init>(HttpsClient.java:264)
  java.base/sun.net.www.protocol.https.HttpsClient.New(HttpsClient.java:377)
  java.base/sun.net.www.protocol.https.AbstractDelegateHttpsURLConnection.getNewHttpClient(AbstractDelegateHttpsURLConnection.java:193)
  java.base/sun.net.www.protocol.https.AbstractDelegateHttpsURLConnection.connect(AbstractDelegateHttpsURLConnection.java:179)
  java.base/sun.net.www.protocol.https.HttpsURLConnectionImpl.connect(HttpsURLConnectionImpl.java:141)
  TRANSFORMER/spark@1.10.124/me.lucko.spark.common.util.BytebinClient.postContent(BytebinClient.java:67)
  TRANSFORMER/spark@1.10.124/me.lucko.spark.common.util.BytebinClient.postContent(BytebinClient.java:84)
  TRANSFORMER/spark@1.10.124/me.lucko.spark.common.util.BytebinClient.postContent(BytebinClient.java:94)
  TRANSFORMER/spark@1.10.124/me.lucko.spark.common.command.modules.SamplerModule.handleUpload(SamplerModule.java:427)
  TRANSFORMER/spark@1.10.124/me.lucko.spark.common.command.modules.SamplerModule.profilerStop(SamplerModule.java:406)
  TRANSFORMER/spark@1.10.124/me.lucko.spark.common.command.modules.SamplerModule.profiler(SamplerModule.java:141)
  TRANSFORMER/spark@1.10.124/me.lucko.spark.common.SparkPlatform.executeCommand0(SparkPlatform.java:472)
  TRANSFORMER/spark@1.10.124/me.lucko.spark.common.SparkPlatform.lambda$executeCommand$3(SparkPlatform.java:370)
[30Jul2026 22:09:45.882] [spark-worker-pool-1-thread-1/WARN] [spark/]: If the command subsequently completes without any errors, this warning should be ignored. :)
[30Jul2026 22:09:46.770] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: [Rcon: Unmarked all force loaded chunks in Overworld]
[30Jul2026 22:09:48.131] [spark-worker-pool-1-thread-3/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] Profiler stopped & upload complete!
[30Jul2026 22:09:48.132] [spark-worker-pool-1-thread-3/INFO] [net.minecraft.server.MinecraftServer/]: https://spark.lucko.me/A96ixnMFqr
[30Jul2026 22:09:48.134] [spark-worker-pool-1-thread-3/INFO] [spark/]: The async-profiler engine is not supported for your os/arch (windows11/amd64), so the built-in Java engine will be used instead.
[30Jul2026 22:09:48.136] [spark-worker-pool-1-thread-3/INFO] [net.minecraft.server.MinecraftServer/]: [⚡] Restarted the background profiler. (If you don't want this to happen, run: /spark profiler cancel)
[30Jul2026 22:09:51.058] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: Saving chunks for level 'ServerLevel[world]'/minecraft:overworld
[30Jul2026 22:09:57.558] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: Saving chunks for level 'ServerLevel[world]'/minecraft:the_nether
[30Jul2026 22:09:57.576] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: Saving chunks for level 'ServerLevel[world]'/minecraft:the_end
[30Jul2026 22:09:57.612] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: ThreadedAnvilChunkStorage (world): All chunks are saved
[30Jul2026 22:09:57.613] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: ThreadedAnvilChunkStorage (DIM-1): All chunks are saved
[30Jul2026 22:09:57.613] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: ThreadedAnvilChunkStorage (DIM1): All chunks are saved
[30Jul2026 22:09:57.613] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: ThreadedAnvilChunkStorage: All dimensions are saved

```


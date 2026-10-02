---
name: minecraft-version-port
description: Minecraft 大版本移植流程（Fabric mod，26.x）：版本情报、构建骨架升级、API/mixin 差异定位、数据包 JSON 迁移、验收三关、双版本分支并行维护。用于把本仓库的老吴 Mod 或同类 Fabric mod 从一个 MC 版本升到下一个。
whenToUse: 需要把 mod 移植到新的 Minecraft 版本（例如 26.2→26.3、26.3→26.4）、需要判断两个版本之间到底改了什么、或需要在两个游戏版本上同时维护代码时。
metadata:
  modloader: fabric
  minecraft: 26.3
  language: java
  project: old_wu_java
---

# Minecraft 版本移植流程（26.x）

## 0. 五条铁律

1. **一条版本一条分支**：`master` = 当前主线版本，`mc<版本>`（如 `mc26.3`）= 新版本；改动**只单向** `master → mc<版本>`，绝不反向合并。
2. **先侦察、再动手**。26.x 的 Minecraft **不再混淆**，任何 API 差异都能用 `javap` 精确定位，不要靠猜、也不要"编译报错修到哪算哪"。
3. **编译通过 ≠ 移植成功**。注入点被删除的 mixin 会**静默失效**（编译不报错，运行时功能丢失且可能整个 mixin 配置报错）。必须做第 8 节的"验收三关"。
4. **数据包 JSON 格式不保证向后兼容**。`loot_table` / `worldgen/biome` / `dimension_type` 这三类必须与官方 jar 内同名文件逐一对照重写。
5. **动存档之前先备份**。`run/` 在两版之间是共享目录，26.x 世界被新版打开后**不可逆升级**。

## 1. 版本情报

- 官方版本清单（拿 client.jar 地址、判断所需 Java 版本）：<https://piston-meta.mojang.com/mc/game/version_manifest_v2.json>
- Fabric 版本（loader / loom / fabric-api）：<https://fabricmc.net/develop> 与 <https://maven.fabricmc.net/>
- **新版本的官方模板 zip 是最好的骨架参照**：优先照模板改 `gradle.properties` / `build.gradle` / wrapper，再逐个对齐版本号，不要靠手改试错。

```powershell
$mf = Invoke-RestMethod 'https://piston-meta.mojang.com/mc/game/version_manifest_v2.json' -TimeoutSec 30
$mf.versions | Where-Object { $_.id -like '26.*' } | Select-Object -First 10 id,type,releaseTime
# 取某个版本的 client.jar 地址：
($mf.versions | Where-Object id -eq '26.3' | Select-Object -First 1).url |
  ForEach-Object { (Invoke-RestMethod $_).downloads.client.url }
```

本项目实测组合：

| 版本 | minecraft | loader | loom | fabric-api | Gradle | Java |
|---|---|---|---|---|---|---|
| 26.2 | 26.2 | 0.19.3 | 1.17-SNAPSHOT | 0.156.0+26.2 | 9.5.1 | 25 |
| 26.3 | 26.3 | 0.19.5 | 1.18-SNAPSHOT | 0.161.0+26.3 | 9.7.1 | 25 |

## 2. 关键事实：26.x 已不再混淆

- 官方 `client.jar` 里就是**真实类名**（`net.minecraft.resources.Identifier`、`net.minecraft.world.entity.animal.feline.Cat`），因此 **Loom 不需要 `mappings` 行**，模板与本项目都没有它。
- 推论：**两版差异可以直接下载两个 jar 做类名 + 方法签名 diff**，这是本流程所有侦察手段的基础。
- 26.2 的类可以直接用 Loom 缓存里的 remap jar（无需重新下载）：
  `.gradle\loom-cache\minecraftMaven\net\minecraft\minecraft-common-<hash>\26.2\*.jar`
  与 `minecraft-clientOnly-<hash>\26.2\*.jar`（common 含 `data/`；clientOnly 含客户端类与 `assets/`）。

## 3. 侦察四件套（务必全部做完再改代码）

把新版 jar 下到临时目录（示例：`%TEMP%\mcver\client-<版本>.jar`）。

### 3.1 类级差异

```powershell
Add-Type -AssemblyName System.IO.Compression.FileSystem
function Get-ClassList([string[]]$jars){
  $set = New-Object 'System.Collections.Generic.HashSet[string]'
  foreach($j in $jars){
    $z=[System.IO.Compression.ZipFile]::OpenRead($j)
    foreach($e in $z.Entries){ if($e.FullName -like '*.class'){ [void]$set.Add($e.FullName) } }
    $z.Dispose()
  }
  return $set
}
$old = Get-ClassList @($loomCommon262, $loomClient262)
$new = Get-ClassList @($tmpClient263)
$onlyOld = @($old | Where-Object { -not $new.Contains($_) })   # 被删除/改名/移动
$onlyNew = @($new | Where-Object { -not $old.Contains($_) })
```

### 3.2 mod 引用面差异（必现编译错误清单）

从源码里抽出所有 `net.minecraft.*` / `com.mojang.*` 全限定名，与新版类表求差集：

```powershell
$fqns = [System.Collections.Generic.HashSet[string]]::new()
Get-ChildItem src -Recurse -Filter *.java | ForEach-Object {
  $t = Get-Content $_.FullName -Raw
  foreach($m in [regex]::Matches($t,'(net\.minecraft|com\.mojang)(\.[A-Za-z0-9_$]+)+')){ [void]$fqns.Add($m.Value) }
}
# 与 $onlyOld（路径形式 net/minecraft/... .class）比对即可列出"引用已消失"的类型
```

注意：只在官方 jar 里找不到的才是问题；`com.mojang.serialization.*` 等属于独立库（DataFixerUpper），不在 client.jar 里，属正常。

### 3.3 mixin 目标差异（最容易静默失效，务必逐个查）

先列出全部注入目标与注入方法：

```powershell
Get-ChildItem src -Recurse -Filter *.java | ForEach-Object {
  $t = Get-Content $_.FullName -Raw
  foreach($m in [regex]::Matches($t,'@Mixin\(([^)]*)\)')){ "{0,-40} {1}" -f $_.Name, ($m.Groups[1].Value -replace '\s+',' ') }
  foreach($m in [regex]::Matches($t,'method\s*=\s*"([^"]+)"')){ "{0,-40} {1}" -f $_.Name, $m.Groups[1].Value }
}
```

再对每个目标类做新旧签名对比：

```powershell
$javap = 'C:\Program Files\Java\jdk-25.0.4\bin\javap.exe'
$a = & $javap -p -cp $cpOld <目标类> 2>&1 | Where-Object { $_ -match '\(' } | ForEach-Object { $_.Trim() }
$b = & $javap -p -cp $cpNew <目标类> 2>&1 | Where-Object { $_ -match '\(' } | ForEach-Object { $_.Trim() }
Compare-Object $a $b | ForEach-Object { "{0} {1}" -f $(if($_.SideIndicator -eq '<='){'[旧]'}else{'[新]'}), $_.InputObject }
```

判读规则：
- 注入的方法**不出现在差异里** = 签名未变，mixin 安全；
- 差异里出现 `[旧] ... 方法名(...)` 而新版没有 = **该方法被删/改名 → 必须重定目标**（见第 6 节）；
- 类本身在两版都有、只是父类/可见性变了（如 `protected → public`）= 通常无碍。

本项目的 13 个目标类：`Cat`、`Wolf`、`Mob`、`LivingEntity`、`AbstractThrownPotion`、`EffectCommands`、`MinecraftServer`、`CatRenderer`、`LivingEntityRenderer`、`CatCollarLayer`、`CatRenderState`、`LivingEntityRenderState`、`Hud$HeartType`。

### 3.4 数据包 JSON 格式差异

两种手段配合使用：

**(a) 官方 jar 内同名文件对照**（最可靠）：

```powershell
# 例：对比两版 data/minecraft/loot_table/blocks/stone.json / worldgen/biome/plains.json / dimension_type/overworld.json
# 直接读出条目文本肉眼比对字段名即可
```

**(b) 从 Codec 类里提取字段名字符串**（判断字段是否被改名/删除）：

```powershell
& $javap -v -p -cp <jar> net.minecraft.world.level.storage.loot.LootPool 2>&1 |
  Select-String '= String\s+#\d+\s+//\s*(.+)$' |
  ForEach-Object { $_.Matches[0].Groups[1].Value.Trim() } | Sort-Object -Unique
```

常用类：`loot.LootPool`、`loot.entries.LootItem`、`worldgen.biome.MobSpawnSettings`、`world.attribute.BedRule`、`world.level.levelgen.flat.FlatLevelGeneratorSettings`、`world.attribute.EnvironmentAttributes`。

## 4. 构建骨架升级清单

| 文件 | 要改什么 |
|---|---|
| `gradle.properties` | `minecraft_version` / `loader_version` / `loom_version` / `fabric_api_version`；`mod_version` 采用 `x.y.z+<版本>` 形式 |
| `gradle/wrapper/gradle-wrapper.properties` | `distributionUrl`（照模板）。**本机网络需额外放大 `networkTimeout`**，见第 10 节 |
| `gradlew` / `gradlew.bat` / `gradle-wrapper.jar` | 整体照模板替换（用哈希确认一致） |
| `fabric.mod.json` | `depends.minecraft: "~<版本>"`、`depends.fabricloader` 下限 |
| `build.gradle` | 只保留本项目自己的东西（`version`/`group`、`runs.client.programArgs`）；模板里的 `fabricApi.configureDataGeneration` 若无 datagen 需求则不要引入 |
| `settings.gradle` / `.gitignore` / `.github/workflows/build.yml` | 只要与模板语义一致就不必动 |

模板与本项目的差异先做一次 diff，只吸收"版本相关"的部分，别把模板的示例包名/示例 mixin 带进来。

## 5. 编译适配常见模式（26.2 → 26.3 实测）

| 类别 | 旧写法 | 新写法 |
|---|---|---|
| 工具类 | `instanceof ShovelItem`（26.3 删除了 `ShovelItem`/`SwordItem`/`AxeItem`/`PickaxeItem`/`HoeItem`/`DiggerItem`，工具改为纯数据组件） | `stack.is(ItemTags.SHOVELS)`（**标签在 26.2 也存在，两版通用，优先这么写**） |
| 方块 codec | `public static final MapCodec<T> CODEC = simpleCodec(T::new);` + `protected MapCodec<?> codec()` | 整个删掉：26.3 的 `Block`/`BlockBehaviour` 不再有 codec 机制 |
| 掉落 | `LivingEntity.drop(ItemStack, boolean, boolean)` | `drop(ItemStack, boolean, Prediction)`，真实掉落传 `Prediction.SERVER_ONLY` |
| 姿态栈 | `poseStack.mulPose(Axis.YP.rotationDegrees(deg))` | `poseStack.rotateDegrees(Axis.YP, deg)`（26.3 删除了 `mulPose(Quaternionfc)`） |
| 模型提交 | `submitModel(model, state, pose, tex, light, overlay, color, null)` | 末尾的 `UvMapping` 参数去掉（Identifier 重载只有 7 个参数） |
| 渲染包迁移 | `com.mojang.blaze3d.pipeline.RenderPipeline`、`blaze3d.textures.GpuTextureView/GpuSampler` | `com.mojang.renderpearl.api.pipeline.*` / `com.mojang.renderpearl.api.textures.*`（尽量别直接引用这些类型，跨版本重灾区） |
| 实体渲染 | `EntityRenderer.shouldRender(...,double,double,double)`、`getBoundingBoxForCulling(T)` | 各多一个 `float` 参数；覆写过这两个方法就要同步改 |
| LivingEntity 杂项 | `hurtArmor/hurtHelmet` 为 public；`swing(hand,boolean)`；`getAttackAnim`；`getInterpolation`；`onEquippedItemBroken(Item)` | 变 protected；新增 `swing(hand,SwingAnimation,boolean)`；`getSwingAnimation`；`createInterpolationHandler`；参数变 `ItemStack` |

同时也要确认"没变"的部分，避免过度修改：`TickRateManager`、`LevelData.RespawnData`、`ServerPlayer.RespawnConfig`、`Entity.teleportTo(8 参)`、`GuiGraphicsExtractor` 的 `fill/text/blitSprite`、`SynchedEntityData`、`Registry`/`BuiltInRegistries` 在 26.2→26.3 均未变。

Fabric API 侧：把 aggregator jar 的嵌套模块解出来，对用到的入口类做同样的 `javap` 签名对比（本项目 13 个入口在 0.156→0.161 之间**零差异**）。

## 6. mixin 重定目标流程

以 `AbstractThrownPotion.onHitAsWater(ServerLevel)` 在 26.3 被删除为例：

1. `javap -p -c <新版jar> <目标类>`，从方法列表 + 字节码里的调用关系找到替代者：26.3 把水的逻辑并进了 `affectEntitiesAround(ServerLevel, PotionContents)`，`onHit` 里对**所有**药水都调用它。
2. 写 descriptor 形式的 `method`（私有方法也能注入，只要有确切签名）：
   `"affectEntitiesAround(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/alchemy/PotionContents;)V"`
3. **补回旧版的进入条件**保持行为等价：旧版只有水瓶才走 `onHitAsWater`，新版对所有药水都调用 → 在新 handler 开头加 `if (!contents.is(Potions.WATER)) return;`。
4. 回调签名必须与新目标参数一一对应：`(ServerLevel, PotionContents, CallbackInfo)`。
5. 用第 8 节"关 2"强制加载目标类来验证真的应用成功。

## 7. 数据包 JSON 迁移对照（26.2 → 26.3）

| 类型 | 26.2 | 26.3 |
|---|---|---|
| `loot_table` 条件 | 条目/池级 `"conditions": [{"condition": "...", "predicate": {...}}]` | 条目级 `"condition": "minecraft:xxx"`（无参）或 `"condition": {"type": "...", ...}` |
| `worldgen/biome` 刷怪 | 顶层 `"spawners": {...}` + `"spawn_costs": {...}` | `attributes."minecraft:gameplay/natural_mob_spawns"` = `{ "argument": { "spawn_costs": {}, "spawns_by_category": {...} }, "modifier": "overlay" }` |
| `dimension_type` 床 | `bed_rule.explodes` | `bed_rule.destroy_on_use`，并新增 `minecraft:gameplay/straw_bed_rule`（草席床，需单独声明才与普通床行为一致） |

**未变、可放心复用**：`advancement`（`icon.id`/criteria）、`recipe`（`result.id`）、物品模型定义 `assets/<ns>/items/*.json`、`blockstates`、`particles`、`models/item`、`sounds.json`、`lang`、贴图与 `ogg`。

`data/<ns>/dimension/*.json` 两版官方 jar 都不提供样本（维度是运行时注册表），只能用 `runClient`/内置服务端实跑验证。`FlatLevelGeneratorSettings` 的字段（`biome`/`features`/`lakes`/`layers`/`structure_overrides`）26.2→26.3 未变。

## 8. 验收三关

### 关 1：构建

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-25.0.4'   # 本机默认是 JDK 21，必须覆盖
.\gradlew.bat clean build --console=plain
```

### 关 2：mixin 强制加载审计（临时改动，跑完必须回滚）

把全部 mixin 目标类强制加载一次，任何注入失败都会在此暴露（正式版没有任何代码会主动加载所有目标类）：

```java
// 临时加到 client 入口，用客户端第一 tick 触发，跑完 git checkout 回滚
boolean[] done = {false};
ClientTickEvents.END_CLIENT_TICK.register(client -> {
    if (done[0]) return; done[0] = true;
    for (String cn : new String[] { /* 全部 @Mixin 目标类名 */ }) {
        try { Class.forName(cn); Old_Wu_java.LOGGER.info("[MIXIN-AUDIT] OK {}", cn); }
        catch (Throwable t) { Old_Wu_java.LOGGER.error("[MIXIN-AUDIT] FAIL {}", cn, t); }
    }
});
```

日志里应当全部 `OK`，且**没有任何** `MixinApplyError` / `was not found in target` / `Critical injection failure`。回滚后用 `clean build` 重建并确认 jar 哈希与提交版一致。

### 关 3：实机 + 日志 + 产物对比

```powershell
# 先备份存档、清空 run/mods（旧版 mod 会污染新版客户端）
Copy-Item run\saves 'G:\backup-run-saves' -Recurse
# 用存档副本进世界，避免升级原存档
Copy-Item 'run\saves\<世界>' 'run\saves\smoketest' -Recurse
$env:JAVA_HOME='C:\Program Files\Java\jdk-25.0.4'
.\gradlew.bat runClient --console=plain --args="--quickPlaySingleplayer smoketest"
```

日志关键字检查：

```powershell
Select-String -Path run\logs\latest.log -Pattern 'Loading Minecraft|MIXIN-AUDIT|old_wu_java|Saving chunks for level|Mixin apply|Failed to load|DataPack|ERROR'
```

通过标准：版本号正确、模组注册日志齐全、**自定义维度出现在 `Saving chunks for level` 里**（= 数据包全部解析成功）、无 mixin/数据包错误。

最后做**产物逐文件对比**，证明资源是一比一搬过来的：

```powershell
# 对比旧版 jar 与新版 jar 的条目集合与逐文件 SHA256
# 期望：条目数相同、assets/** 差异为 0；差异只应出现在版本相关文件（构建文件、fabric.mod.json、适配过的类、迁移过的 data JSON）
```

日志噪声不必惊慌：离线开发环境的 `Failed to fetch user properties(401)`、`Could not authorize you against Realms`、世界存档里残留的第三方 mod 游戏规则（如 `jade:*`）都属正常；**旧版历史日志里本来就有的告警**（本项目是 `Missing texture references in model old_wu_java:item/paper_roll`）不算回归。

## 9. 双版本并行维护

```powershell
# ① 主线开发（26.2）
git switch master
# ...改代码、实测...
git add -A; git commit -m "1.9.0 xxx"

# ② 同步到新版本分支
git switch mc26.3
git merge master                 # 冲突通常只落在第 5/7 节的"适配层"
$env:JAVA_HOME='C:\Program Files\Java\jdk-25.0.4'; .\gradlew.bat build
# 关 2、关 3 ...
git add -A; git commit

# ③ 只想搬一个功能
git cherry-pick <提交号>
```

要点：
- **新版本分支独立运行目录**，否则存档会被单向升级：

```groovy
loom {
	runs {
		client {
			runDir "run-26.3"                  // 旧版分支保持默认 "run"
			programArgs("--username", project.dev_username)
		}
	}
}
```

- 想同时开两个版本：`git worktree add ..\old_wu_java-263 mc26.3`（新目录的 `run/` 是空的，复制光影/配置过去即可）。
- 版本号带后缀 `x.y.z+26.2` / `x.y.z+26.3`，产物自然区分。
- CI 可用矩阵一次构建两条分支：

```yaml
strategy:
  matrix:
    ref: [master, mc26.3]
```

- **在旧版分支上就写"两版都能编译"的代码**（例如一律用物品标签而不是已被删除的工具类），能从源头消灭大部分合并冲突。

## 10. 坑清单（都踩过）

1. **`services.gradle.org` 可能超时**：模板的 `networkTimeout=10000` 太短 → 调到 `180000` 并把 `retries` 设为 3；若仍失败，用镜像（如 `https://mirrors.cloud.tencent.com/gradle/gradle-<v>-bin.zip`）下载后放进
   `%USERPROFILE%\.gradle\wrapper\dists\gradle-<v>-bin\<hash>\gradle-<v>-bin.zip`，wrapper 会直接解压，仓库里的 `distributionUrl` 可保持官方地址。
2. **`JAVA_HOME` 默认指向 JDK 21**（本机），`release = 25` 会直接报"不支持发行版 25"，每次构建前必须覆盖。
3. **专用服务端需要同意 EULA**（`run/eula.txt`）。不要代替用户同意；数据包/维度验证改用客户端的**内置服务端**（`--quickPlaySingleplayer`），效果等价。
4. **`run/`、`build/`、`.gradle/` 都不随分支切换**：旧版 mod 会污染新版客户端，存档会被单向升级。动手前先备份存档、把旧版 mod 移出 `run/mods`。
5. **PowerShell 对带 `[ ]` 的文件名会当通配符**（如 `[钠] xxx.jar`），`Move-Item`/`Remove-Item` 会静默失败 → 用 `-LiteralPath` 或 `[System.IO.File]::Move(...)`。
6. **删除/移动前先打印 `Resolve-Path` 的绝对路径并比对预期**，再做破坏性操作。
7. 中文/大体积文件不要进 git（本项目 `run/`、`tmp_cat_models/`、`*.txt1` 已在 `.gitignore` 中）。
8. 移植完成的判据不是"能编译"，而是"关 1+2+3 全过 + 资源逐文件对比无意外差异"；玩家的实际操作体验（状态机手感、Boss 战、HUD 表现）必须由人实测，Agent 只能覆盖到加载层。

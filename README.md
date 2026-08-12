# Old_Wu_java  吴家物语
-老吴Mod Java版
# 游戏版本: 26.2 fabric版
由FunctionHookTJU(宇佐见函钩)制作并开源
## 状态机

状态按优先级处理，高优先级状态会无条件打断低优先级状态。

### 最高优先级：矿车追踪

- 猫在非压扁状态下，每 tick 检测 16 格内的矿车。
- 若检测到矿车，立即中断当前状态，寻路至矿车。
- 接触矿车或使用铲子右键时，进入压扁状态（flat），模型切换为 flat，无法移动。
- 15 秒后恢复正常状态，并清除配对 UUID。

### 次优先级：配对相关状态

仅在无矿车追踪时生效。

| 状态 | 行为 |
| --- | --- |
| common（普通） | 无特殊行为 |
| angry（愤怒） | 检测到 16 格内另一只猫且自身未配对时进入，切换 angry 模型并寻路至对方附近 |
| pairing（配对） | 两只猫到达对方附近后进入，面对面，播放配对音频，双方互相用 String 存储对方 UUID；此时不响应第三只猫 |
| battle（战斗） | 配对中每只猫每 tick 有 5% 概率发动，任意一方发动则双方进入 battle 模型；每只猫独立每 10~20 tick 攻击对方一次，造成 0.5 点伤害，播放战斗音频 |
| recovery（回血） | 战斗中任意猫生命 ≤1 时，双方进入 recovery 模型，获得生命恢复 I（无粒子），播放回血音频；生命恢复至最大值的 80% 以上时，双方退出恢复并回到 common，清除配对 |
| dance（街舞） | 被马/驴/骡/猪/骆驼冲撞时 50% 概率进入：不停跳跃，模型在全部状态模型间每 5 tick 随机切换，不播放音效，持续 100 tick 后恢复 common |
| grooming（理毛） | 被泼溅水瓶/喷溅药水的水面命中（半径 4 格内）进入：播放理毛动画 100 tick，同时获得 600 tick 和平期（不战斗、不配对），清除配对 |
| hitground（撼地掌） | 对玩家/敌对目标使用老吴撼地掌时进入：播放 `laowuhitground`（成年/幼年）0.5 秒动画，停导航，结束后恢复 common |



## 镜子方块（mirror）

### 方块

- 贴墙放置（东西南北四个朝向），**无碰撞箱**，可含水，硬度 1、玻璃音效、可空手挖掘。
- 反射效果：镜面贴图使用**高反光 PBR（specular）材质**（`texture5_s.png` 全白），由光影包自行计算反射，无需自定义渲染代码。
- 已加入模组专属创造模式物品栏（老吴）。

### 猫对镜配对

- 普通猫在 **3 格内**观察到镜子（从猫眼到镜子中心做射线检测，视线直达可见）时，进入 **pairing 配对状态**：原地不动、视角锁定在镜子上，并播放配对音频（laowu 系列，每段播完再播下一段）。
- **优先级低于与其他猫的配对**：若周围出现可配对的其它猫（或耄耋），猫会**放弃镜子**、优先去与其他猫配对；无其它猫时才继续对镜配对。
- 镜子不可见或超出范围后恢复正常（common）状态。
- 耄耋（maodie）不会对镜配对。

## 其他规则

- 矿车追踪会打断愤怒、寻路、配对、战斗、回血等一切行为。
- UUID 必须用 String 序列化存储，配对时互相锁定。
- 所有模型（common、angry、battle、recovery、flat、grooming）必须准备成年/幼年各一套。
- 音频按状态触发，不可重叠。
- 街舞（dance）与理毛（grooming）是最高优先级打断状态，进入时会立即清除配对。
- **驯服的猫互相之间不会主动进入配对/战斗**（`findCandidate` 对驯服猫直接返回空）；只有**未驯服的猫**会挑衅发起配对战斗（未驯服猫可将驯服猫或其它未驯服猫作为目标）。驯服猫仍会正常索敌耄耋（maodie）。
- **猫与猫之间的配对状态优先级高于攻击玩家**：仅在无配对候选时才对玩家/其他目标使用老吴撼地掌。

## 好猫值（Good Cat Value）

每只猫（自然生成、命令召唤、刷怪蛋、繁殖）都有随机的好猫值（0~100 整数，持久化到 NBT `oldwu_good_value` 并同步客户端）：

| 好猫值 | 类型 | 说明 |
| --- | --- | --- |
| 80~100 | 绝世好猫 | 任意情况不攻击玩家；不会主动与其他绝世好猫配对战斗（可被动接受普通/坏/键帽发起的配对） |
| 40~79 | 普通猫 | 中立单位：玩家不攻击则不攻击玩家，被玩家攻击后 200 tick 内反击；保留全部配对/战斗/回血逻辑 |
| 20~39 | 坏猫 | 保留全部猫行为，并主动对玩家使用老吴撼地掌 |
| 0~19 | 键帽 | 同坏猫；额外血量上限 +20、防御 +10，攻击 20% 概率附带 5 秒凋零 I |

- **驯服**：只有好猫值 ≥40（普通猫及以上）的猫才能被驯服，坏猫/键帽喂生鱼直接无效（不消耗、不挥臂）；被驯服后好猫值 **+10**。
- **繁殖**：后代好猫值 = min(100, max(父母好猫值) + 5)。
- **铲子压扁**：用铲子右键使猫进入压扁（flat）状态时，坏猫/键帽有 **50% 概率好猫值 +1**（每次铲子压扁都可触发，无需等待恢复）。
- 未驯服绝世好猫不与绝世好猫互相配对，但普通/坏/键帽可对其发起配对。

## 老吴撼地掌（HITGROUND 状态）

坏猫/键帽主动对玩家使用，普通猫被玩家攻击后 200 tick 内反击。触发时进入 **hitground** 状态，播放 `laowuhitground` 成年/幼年动画（0.5 秒，一次性）。

- **两类**：恐吓类（无伤害，概率 = 好猫值/100）与攻击类（伤害 = (100 - 好猫值) × 0.1）。
- 索敌范围 **16 格**、近战距离 **3 格**、攻击间隔 **10 tick**。
- 键帽攻击额外 20% 概率附带凋零 I（5 秒）。
- 未驯服绝世好猫永不攻击玩家；驯服猫不攻击玩家（改为攻击其他目标，见下）。

## 已驯服猫的强化

- 已驯服的**普通猫**/绝世好猫均不主动发起配对（沿用原规则，含不主动与绝世好猫配对战斗）。
- 已驯服的**绝世好猫**血量上限提升为 **40**；已驯服的**普通猫**血量上限提升为 **25**（首次变化补满血，幂等）。
- 两者都能**主动攻击玩家以外的目标**：目标优先级 = 主人正在攻击的生物（`owner.getLastHurtMob()`）→ 猫当前仇恨目标（`getTarget()`）→ 16 格内最近的敌对生物（`Monster`）；目标均需存活、非玩家、16 格球形内且视线可见。
- 老吴撼地掌改为对上述目标使用；**恐吓概率降至 10%、攻击概率提升至 90%**。
- 已驯服猫撼地掌伤害改为 **好猫值 × 0.15**（未驯服仍为 (100 - 好猫值) × 0.1）。

## maodie 血量保留

- 猫一旦被命名为 `maodie`，即使之后改回其他名字，也**永久保留 325 血量上限**（所有类型：驯服/未驯服）；标记 `WAS_MAODIE` 持久化到 NBT。改名后恢复普通行为逻辑，但不再应用键帽/已驯服血量加成。

## 猫信息 HUD

- 准星看向任意猫时，屏幕中央偏左显示半透明面板：好猫值及类型（键帽/坏猫/普通猫/绝世好猫）、血量上限、撼地掌伤害、恐吓/攻击概率（已驯服猫显示 10%/90%）；视角离开猫立即消失。

## 新增物品：大狗叫（dagoujiao）

- 工作台合成配方：中间一行 `[线][任意颜色羊毛地毯][线]`，消耗 **2 根线 + 1 块羊毛地毯**，产出 1 个大狗叫。
- 大狗叫只能对**已驯服**的狼使用（右键喂食），效果只在服务端执行，客户端仅返回成功触发挥臂与交互音效。
- **只能喂食属于自己的狼**：非主人的玩家对别人的狼使用大狗叫时，交互直接放行（不消耗、无效果、不挥臂）；进度条 HUD 同样只对狼主人显示。
- **血量不满时**：每次喂食回复 **10 点生命**并消耗 1 个大狗叫。
- **满血时**：每次喂食按喂食次数 1、2、3... 递增提升血量上限（第 n 次喂食 +n，最多 64 次），并回复等量生命；喂食次数通过物品数据组件 `oldwu_dagoujiao_feeds` 持久化。
- 血量上限提升使用无上限的自定义属性 `old_wu_java:extra_max_health`（通过 `LivingEntity#getMaxHealth` 叠加），绕开原版 `generic.max_health` 的 **1024 上限**，喂食 64 次累计可达到 2100 最大生命。
- **满 64 次后**：狼获得**永久力量 IV + 生命恢复 III**；此后开启**自动充能**——**仅当狼存在攻击目标时**（32 格内），每 tick 检测主人背包中是否有大狗叫，存在则自动消耗 1 个并蓄力 +1 格（共 **12 格**），依序播放 `大狗1~10`、`大狗11_re`、`dog_launch` 音频（蓄力格数与音频编号一一对应，序号同步到 `old_wu_java:charge` 属性）。**血量不满时也可充能**。音频来源：`tmp_cat_models/audio/` 中 `大狗1~13.ogg` 实际存在的 10 个文件（跳过缺失的 5、7）重新编号为 dagou_1~10，外加 `大狗11_re.ogg`（dagou_11_re）与 `dog_launch.ogg`（`dog_launch.ogg` 取自第三方 MIT 许可模组，见「第三方资源与许可」）。
- **充能范围与冷却**：自动充能需主人位于狼 16 格内；每次蓄力需等上一段 `大狗N` 音频播放结束（按各音频实测时长换算 tick），冷却期间不消耗。
- **音波攻击**：第 **12 次**蓄力（`dog_launch` 音频**开始后 1 tick**，不等待其播完）清空蓄力条并触发与监守者（warden）相同的**音波攻击**（`sonic_boom` 伤害 + 密集 `SONIC_BOOM` 粒子 + 击退，粒子密度约为监守者的 4 倍），伤害为 **67 点**（监守者为 10）。目标优先为狼当前仇恨目标（32 格内），否则为 16 格内最近的敌对生物（排除自己、主人、其它已驯服狼）。
- 手持大狗叫或背包中有大狗叫并看向已驯服狼时，屏幕中央偏左显示 HUD：左侧绿色蓄力条（12 格）+ 右侧黄色喂食进度条（满 64 次填满，填充色 `#E5A822`、蓄力色 `#55FF55`、边框与底色 `#1E1F22`），视角离开狼立即消失。
- 创造模式喂食不消耗物品。

## 新增物品：纸卷（paper_roll）

- 手持纸卷按住右键蓄力（BOW 拉弓动画），松开后沿视线发射纸卷弹射物；每组可堆叠 67 个。
- 生存/冒险模式发射消耗 1 个纸卷；创造模式发射不消耗。
- 发射速度与蓄力时长正相关：满蓄力 1 秒（20 tick），速度 0.25 ~ 2.5 方块/tick；蓄力不足 10% 不发射。
- 弹射物直线飞行（无抛物线、无空气阻力），碰撞箱 0.5×0.5。
- 命中生物造成固定 15 点弹射物伤害；命中任意目标（生物/方块）产生约恶魂火焰弹强度的爆炸（破坏小范围方块，不产生火焰）。
- 已加入模组专属创造模式物品栏（老吴），模型使用 Blockbench 导出的 3D 纸卷模型。

## 耄耋（maodie）

将猫用命名牌命名为 `maodie` 后，成为 Boss 级耄耋：

| 特性 | 说明                                                                                                                                                              |
| --- |-------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 血量上限 | 提升至 325（首次变身后补满血）                                                                                                                                    |
| 体型 | 使用原版 `generic.scale` 属性放大 1.5 倍（模型 + 碰撞箱），同步给所有客户端                                                                                       |
| 专属模型/贴图 | 使用 maodie 专属模型；攻击/发射纸卷时贴图切换为 haqi.png                                                                                                          |
| 主动索敌 | **需视线可见**（`hasLineOfSight`）：优先攻击 128 格内最近玩家（不含创造/旁观），无可见玩家时攻击可见生物；无可见目标时随机游荡                                                                        |
| 近战攻击 | 攻击距离 3 格，伤害 5~20 随机；20% 概率中毒 II（5 秒）、10% 概率中毒 II + 凋零 II（8 秒）；攻击时播放 ha 系列音效并产生两个爆炸粒子（仅视觉效果，不产生真实爆炸） |
| 狂暴 | 血量 ≤114 时每 80 tick 向目标发射一枚全速纸卷（速度 4.0）；发射前 5 tick 起贴图切换为 haqi                                                                        |
| 身后光环 | 身后常驻由 #CCA675 色尘粒子构成的圆环                                                                                                                             |
| Boss 血条 | 屏幕顶部显示 Boss 血条（使用黄色）                                                                                                                                |
| 掉落 | 被击败后随机掉落 2至5 个纸卷，掉落数量受抢夺附魔影响（每级抢夺额外 +0~1）                                                                                         |
| 自伤免疫 | 不受自己发射的纸卷的直接伤害，也不受其爆炸的伤害（`PaperRoll` 对 maodie 发射者做伤害/爆炸豁免）                                                                    |

普通猫索敌：当普通猫索敌范围内存在耄耋时，所有普通猫停止互相配对，目标统一指向耄耋，并正常经历 angry → pairing → battle → recovery 状态。

**血量保留**：猫一旦被命名为 `maodie`（无论驯服与否），之后即使改名也会永久保留 325 血量上限（见「maodie 血量保留」）。

**AI 恢复**：变身为 maodie 时原版 AI（goalSelector/targetSelector）会先暂存再清空；改名恢复普通猫时自动重新添加，恢复游荡/看向等全部原版行为（不会出现改名后不会动）。

## 猫的新增属性（SynchedEntityData）

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| PARTNER_UUID | String | 配对目标 UUID（空串 = 未配对） |
| STATE | int | 当前状态枚举序号 |
| ATTACK_COOLDOWN | int | 战斗/耄耋近战攻击冷却 |
| FLAT_TIMER | int | 压扁剩余时间 |
| PAIRING_TIMER | int | 配对延迟剩余时间 |
| DANCE_MODEL_INDEX | int | 街舞状态切换的模型序号 |
| DANCE_TIMER | int | 街舞剩余时间 |
| MAODIE_HAQI_TIMER | int | 耄耋 haqi 贴图剩余时间 |
| MAODIE_RAGE_COOLDOWN | int | 耄耋狂暴发射纸卷冷却 |
| MAODIE_ANIM_TICK | int | 耄耋攻击/发射动画触发 tick |
| GROOMING_TIMER | int | 理毛剩余时间 |
| BATTLE_PEACE_TIMER | int | 战斗和平期（不配对/不战斗）剩余时间 |
| MIRROR_TICKS | int | 对镜注视剩余时间（>0 = 正在对镜配对） |
| GOOD_VALUE | int | 好猫值（-1 = 未分配） |
| HITGROUND_TIMER | int | 撼地掌动画剩余时间 |
| HITGROUND_COOLDOWN | int | 撼地掌攻击间隔冷却 |
| HITGROUND_ANIM_TICK | int | 撼地掌动画触发 tick（客户端播放动画） |
| WAS_MAODIE | boolean | 是否曾是 maodie（改名后保留 325 血） |

耄耋额外使用原版属性：`generic.max_health` 基值 325、`generic.scale` 基值 1.5。

## 新增物品：纸卷（paper_roll）与大狗叫（dagoujiao）

纸卷与大狗叫均加入模组专属创造模式物品栏（老吴），使用 Blockbench 导出的 3D 模型。

## 第三方资源与许可（Third-Party Assets & Licenses）

- **`dog_launch.ogg`**（第 12 次蓄力音效）取自 [ikunkk02-afk/Big-Dog-Bark](https://github.com/ikunkk02-afk/Big-Dog-Bark)（Minecraft 1.21.1 Fabric 模组「大狗叫」），源文件位于其 `assets/big_dog_bark/sounds/entity/dog_launch.ogg`。该仓库以 **MIT 许可** 发布，版权归 **寿云** 所有。

### MIT License — Big-Dog-Bark

> MIT License
>
> Copyright (c) 2026 寿云
>
> Permission is hereby granted, free of charge, to any person obtaining a copy
> of this software and associated documentation files (the "Software"), to deal
> in the Software without restriction, including without limitation the rights
> to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
> copies of the Software, and to permit persons to whom the Software is
> furnished to do so, subject to the following conditions:
>
> The above copyright notice and this permission notice shall be included in all
> copies or substantial portions of the Software.
>
> THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
> IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
> FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
> AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
> LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
> OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
> SOFTWARE.

## License

This template is available under the CC0 license. Feel free to learn from it and incorporate it in your own projects.

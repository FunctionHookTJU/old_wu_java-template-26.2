# Old_Wu_java  老吴Mod Java版

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

## 其他规则

- 矿车追踪会打断愤怒、寻路、配对、战斗、回血等一切行为。
- UUID 必须用 String 序列化存储，配对时互相锁定。
- 所有模型（common、angry、battle、recovery、flat）必须准备成年/幼年各一套。
- 音频按状态触发，不可重叠。

## License

This template is available under the CC0 license. Feel free to learn from it and incorporate it in your own projects.

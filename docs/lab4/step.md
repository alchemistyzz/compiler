# 实验步骤

!!! info "不完备的寄存器分配"
    我们定义一种不完备的寄存器分配, 它的分配策略非常简单: 当我们要对一个变量做寄存器指派时, 有寄存器空闲, 则指派任意空闲寄存器分配; 当无寄存器空闲时, 判断当前是否有寄存器被不再使用的变量占用, 若有则指派任意满足该条件的寄存器, 若无则直接报错. 由于这种分配方法不能处理更复杂的程序, 所以称之为不完备的寄存器分配. 

    完备的寄存器应做到什么我们在 [附加题](./extra.md) 中讨论. 

=== "不想完成完备的寄存器分配"

    1. 加载前端提供的中间代码, 视情况进行一些前处理. 
    2. 视情况而定, 生成部分在代码生成时会用到的信息, 如变量的引用信息. 
    3. 根据每条 IR 的类型, 参数类型确定对应的代码生成. 
        - 寄存器全部被占用时直接抛出异常, 完成的标准为跑过样例 `input-code.txt`
    4. 验证输出的汇编代码正确性

=== "想完成完备的寄存器分配"

    1. 加载前端提供的中间代码, 并进行一些前处理(对于想要完成完备的寄存器分配的同学, 这步可以大大简化代码生成时繁琐的类型判断). 
    2. 生成在代码生成时会用到的信息, 如变量的引用信息. (对于想要完成完备的寄存器分配的同学, 这步可以在代码生成阶段便捷地取得你需要的信息).
    3. 根据每条 IR 的类型和参数, 目前的寄存器占用状态, 变量的生存信息, 内存的使用情况, 确定对应的代码生成. 
        1. 进指令判断寄存器占用情况, 当占用满时需要溢出到内存中. 
        2. 出指令判断变量存活情况, 释放占用的内存和寄存器. 
        3. 完成的标准见 [附加题](./extra.md).
    4. 验证输出的汇编代码正确性. 

## 提示

### 预处理思路

我们推荐在加载中间代码时对中间代码进行预处理, 使其更符合 RISC-V 汇编代码的形式. 

!!! question "为什么需要进行预处理"
    中间代码 (IR) 是机器无关的, 它和我们最终要翻译成的目标平台无关, 因此 IR 在设计时不可能对某一个特定的平台做出约束. 而代码生成是与目标语言目标平台相关的, 在翻译的过程中往往对于指令的操作数的类型和位置等有一定的约束. 
    
    例如在 RISC-V 的汇编中, 对于整数加法, 我们考虑 `add` 和 `addi` 指令格式, 其要求加法指令只能有最多一个立即数, 且立即数需要放在右手处, 即 `addi rs, rd, imm`. 然而在 IR 中并没有这个限制(且这个限制在前面的实验中就不应该存在), 这会为我们在生成目标代码时带来繁琐的判断和指令插入. 通过预处理可以简化目标代码的生成. 

下面给出一个预处理的思路:

对于 `BinaryOp`(两个操作数的指令):

- 将操作两个立即数的 `BinaryOp` 直接进行求值得到结果, 然后替换成 `MOV` 指令
- 将操作一个立即数的指令 (除了乘法和左立即数减法) 进行调整, 使之满足 `a := b op imm` 的格式
- 将操作一个立即数的乘法和左立即数减法调整, 前插一条 `MOV a, imm`, 用 `a` 替换原立即数, 将指令调整为无立即数指令.

对于 `UnaryOp`(一个操作数的指令):

- 根据语言规定, 当遇到 Ret 指令后直接舍弃后续指令.


### 映射的维护

无论是否想要完成完备的寄存器分配, 你都需要维护的信息为寄存器的占用信息. 这个信息可以通过一个 `Map<IRVariable, Reg>` 或 `Map<Reg, IRVariable>` 进行维护, 取决于你需要通过 `IRVariable` 拿到占用的 `Reg` 还是通过 `Reg` 找到占用其的 `IRVariable`. 通过 key 查找 value 的行为在 `HashMap` 中时间复杂度为 O(1). 

`Map` 可以简单理解为一个单射, 通过 key 可以马上拿到其的像, 然而找到原像就相对复杂了. 而有时候我们更希望这个映射可以双向查找, 当然前提是这个映射本身就是一个双射. 于是我们期望维护一个可以双向查找的双射, 然而在 Java 的容器里没有双射, 需要自行想办法. 

??? example "一个双射实现"

    ```java
    // 双射
    public class BMap<K, V> {
        private final Map<K, V> KVmap = new HashMap<>();
        private final Map<V, K> VKmap = new HashMap<>();

        public void removeByKey(K key) {
            VKmap.remove(KVmap.remove(key));
        }

        public void removeByValue(V value) {
            KVmap.remove(VKmap.remove(value));
            
        }

        public boolean containsKey(K key) {
            return KVmap.containsKey(key);
        }

        public boolean containsValue(V value) {
            return VKmap.containsKey(value);
        }

        public void replace(K key, V value) {
            // 对于双射关系, 将会删除交叉项
            removeByKey(key);
            removeByValue(value);
            KVmap.put(key, value);
            VKmap.put(value, key);
        }

        public V getByKey(K key) {
            return KVmap.get(key);
        }

        public K getByValue(V value) {
            return VKmap.get(value);
        }
    }
    ```

### 集合的维护

你可能需要维护集合, 并期望在尽可能短的时间里查找到某个元素是否在该集合中. 在 Java 中你可以通过使用 `Set` 实现. 特别的, 若你不需要集合中元素的加入顺序这一信息, 使用 `HashSet`, 若你需要这个信息, 使用 `LinkedHashSet`. 

### `equals` 与 `hashCode` 方法的重载

上述的集合对于对象的相等和对象的哈希值都通过 `equals` 与 `hashCode` 方法获取. 当你的对象没有实现单例, 包括显式或者隐式(即类定义或工厂方法未体现, 但在实际代码中是单例的), 也就是可能出现几个对象实际上是同一个然而却由于多次创建导致了指向不同的对象示例. 这时候需要手动重载 `equals` 与 `hashCode` 方法. 

```java
class Student{
    int id;
    String name;
    int age;
    String school;

    @Override
    public boolean equals(Object obj){
        // 对于学生, 从实际情况来看 id 和 school 可以唯一确定
        return obj instanceof Student stu
            && name.equals(stu.name)
            && school.equals(stu.school);
    }

    @Override
    public int hashCode(){
        // 在 equals 里考虑的所有域都要考虑进来
        return Objects.hash(id, school);
    }
}

```

## 正确性验证与 RARS 的使用

对于位于 "data/out/" 的汇编代码, 我们需要对其进行执行, 并与 `data/std` 中的结果进行比对, 从而验证其正确性. 若你的前几次实验都无问题, 亦可使用 `data/out/ir_emulate_result.txt` 中的结果作为标准值. 

RARS可以 [在此下载](https://gitee.com/hitsz-cslab/Compiler/releases/latest). 在 windows 系统下可以双击 `rars.jar` 文件打开, 或在控制台输入:

```shell
java -jar rars.jar
```

在 `File -> Open` 中选定输出的 `.asm` 汇编文件, 在 `Settings -> Memory Configuration` 中选择 `Compact, Data at Address 0`, 后点击 ![assemble.png](./assets/assemble.png). 之后就可以选择单步执行或者执行整个程序. 在右边可以查看寄存器的 value. 根据我们对 `return` 的约定, 程序执行结束的 `a0` 值就是程序的输出, 以十六进制的格式显示. 

对于正确性的验证就是判断程序输出是否和预期输出一致. 

!!! tips
    在 debug 的过程中, 我们可能需要频繁地更改程序, 输出汇编文件, 然后执行. 如果每次都去点按钮会略显麻烦. `RARS` 提供了 cli 的运行方式, 可以通过如下方式运行汇编代码并输出 `a0` 的十进制值:
    ```shell title="input"
    java -jar rars.jar mc CompactDataAtZero a0 nc dec ae255 riscv1.asm
    ```
    ```title="output"
    Program terminated by dropping off the bottom.
    a0	10945
    ```
    你需要将 `rars.jar` 或 `riscv1.asm` 更改为你的 `rars` 程序路径和汇编文件的路径. 
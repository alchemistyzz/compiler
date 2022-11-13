## `LRTable`: LR 分析表

所谓 LR 分析表, 实际上就是一张类似自动机的状态转移图, 当在当前状态遇到某某符号时便执行动作并转移. 规约时就根据 "Action 表" 里写的产生式生成非终结符, 然后将生成的非终结符作为 "输入符号" 根据 "Goto 表" 来确定转移; 移入时就直接根据 "Action 表" 转移到对应状态.

我们采用一种更接近邻接表而非邻接矩阵的方式来提供对 ACTION 与 GOTO 的访问; 虽然, 我们亦在 `LRTable` 中提供了 "更像邻接矩阵" 的访问接口:

```java
class LRTable {
    /**
     * 根据当前状态与当前词法单元获取对应动作
     * @param status 当前状态
     * @param token 当前词法单元
     * @return 应采取的动作
     */
    public Action getAction(Status status, Token token) {
        final var tokenKind = token.getKind();
        return status.getAction(tokenKind);
    }

    /**
     * 根据当前状态与规约到非终结符获得应转移到的状态
     * @param status 当前状态
     * @param nonTerminal 规约出的非终结符
     * @return 应转移到的状态
     */
    public Status getGoto(Status status, NonTerminal nonTerminal) {
        return status.getGoto(nonTerminal);
    }

    /**
     * @return 起始状态
     */
    public Status getInit() {
        // return initStatus;
        return statusInIndexOrder.get(0);
    }
}
```

理论上一个 LRTable 只需要保存起始状态 (`initStatus`) 即可. 但是为了方便将整个表输出为 csv 查看, 我们于 LRTable 中保存了一些多余的状态, 而你的实现 **不应该** 使用这些信息:

```java
class LRTable {
    private final List<Status> statusInIndexOrder;
    private final List<TokenKind> terminals;
    private final List<NonTerminal> nonTerminals;
}
```

## `Status`: 状态

一个状态由 `action` 和 `goto` 组成, 它们的语义如下:

- `action`: 在当前状态下遇到某个终结符需要采取什么动作
- `goto`: 在当前状态下遇到 (规约到) 某个非终结符需要转移到什么状态

为了节省内存空间, 表中并不会储存对应错误动作/错误状态的值.

我们并不采用整数来代表状态, 而是直接使用状态自身来代表它自身. (即 Java 对象引用)

```java
class Status {
    private Map<TokenKind, Action> action;
    private Map<NonTerminal, Status> goto_;     // goto 是 Java 保留关键字, 故尾附一下划线
}
```

获取这些信息可采用 `getAction` 与 `getGoto` 方法:

```java
class Status {
    public Action getAction(TokenKind terminal) {
        return action.getOrDefault(terminal, Action.error());
    }

    public Action getAction(Token token) {
        return getAction(token.getKind());
    }

    public Status getGoto(NonTerminal nonTerminal) {
        return goto_.getOrDefault(nonTerminal, Status.error());
    }
}
```

## `Action`: 动作

LR 分析中的动作有四种, 动作的类别使用一枚举来表示, 同时还有一些特殊的动作独有的成分:

```java
enum ActionKind {
    Shift,      // 移入
    Reduce,     // 规约
    Accept,     // 接受
    Error,      // 出错
}

class Action {
    private ActionKind kind;
    private Production production;  // 当且仅当动作为规约时它非空
    private Status status;          // 当且仅当动作为移入时它非空
}
```

你应当采用各个访问方法来访问这些信息, 这些访问性方法会检查动作类别并确定返回值非空:

```java
class Action {
    public ActionKind getKind() {
        return kind;
    }

    public Production getProduction() {
        if (kind != ActionKind.Reduce) {
            throw new RuntimeException("Only reduce action could have a production");
        }

        assert production != null;
        return production;
    }

    public Status getStatus() {
        if (kind != ActionKind.Shift) {
            throw new RuntimeException("Only shift action could hava a status");
        }

        assert status != null;
        return status;
    }
}
```

你可以参考如下代码片段对动作进行分类:

```java
switch (action.getKind()) {
    case Shift -> {
        final var shiftTo = action.getStatus();
        // ...
    }

    case Reduce -> {
        final var production = action.getProduction();
        // ...
    }

    case Accept -> {
        // ...
    }

    case Error -> {
        // ...
    }
}
```

# 框架设计简介

## `Token`: 词法单元

一个词法单元逻辑上由两个部分组成: 词法单元的类型, 词法单元代表的文本.

```java
class Token {
    private TokenKind kind;
    private String text;
}
```

然而, 对于大部分简单的词法单元而言, 并不需要保存词法单元代表的文本. 在这种情况下, 我们将此 `Token` 的 `text` 设置为 `""` (空字符串), 而不是 `null`. 为了方便构造这两种 `Token`, 我们提供了两个静态方法以供使用:

```java
class Token {
    public static Token simple(TokenKind kind) {
        return new Token(kind, "");
    }

    public static Token normal(TokenKind kind, String text) {
        return new Token(kind, text);
    }
}
```

## `TokenKind`: 词法单元类别

在传统实现中, 词法单元的类别一般实现为一枚举, 将所有可能的词法单元类型硬编码到代码中. 但鉴于本实验的历史原因, 我们不得不在运行时从一个文件中读入所有可能的词法单元:

``` title="coding_map.txt"
1 int
2 return
3 =
4 ,
5 Semicolon
6 +
7 -
8 *
9 /
10 (
11 )
51 id
52 IntConst
```

考虑到代码可读性, 我们最终选择使用某个词法单元类别的名字来唯一标识该类别, 而将所谓 "码点" 作为词法单元类别的附加信息, 仅供参考使用. 于是我们的词法单元类别设计如下:

```java
class TokenKind {
    private String id;
    private int code;
}
```

鉴于使用字符串作为唯一标识符有极大的因为拼写错误而出错的可能, 我们会在编译器运行时从 `coding_map.txt` 中读入所有可能的词法单元类别标识符, 然后在每次构造词法单元类别时进行检查:

```java
class TokenKind {
    public static TokenKind fromString(String id) {
        // 检查 id 是否被允许使用 (即是否在 coding_map.txt 中有定义)
        if (allowed == null || !allowed.containsKey(id)) {
            throw new RuntimeException("Illegal Identifier");
        }

        return allowed.get(id);
    }

    // 禁止外界构造新的 TokenKind
    private TokenKind(String id, int code) {
        // ...
    }
}
```

同时, 为了进一步方便 `Token` 的构造, 我们也为 `simple` 和 `normal` 提供了直接使用词法单元类别标识符的重载版本:

```java
class Token {
    public static Token simple(String tokenKindId) {
        return simple(TokenKind.fromString(tokenKindId));
    }

    public static Token normal(String tokenKindId, String text) {
        return normal(TokenKind.fromString(tokenKindId), text);
    }
}
```

另外, `TokenKind` 同时还作为 `Term` 的子类, 代表文法中的非终结符. 详见[实验二]()的指导.

## `SymbolTable`: 符号表

在实验一中, 你仅需要在读到一个标识符时 (即生成类别为 `id` 的词法单元时), 检测符号表中是否已含有该标识符, 若无向符号表加入该标识符即可.

```java
// 在你的实现的某个地方...
if (!symbolTable.has(identifierText)) {
    symbolTable.add(identifierText);
}
```
1. 确定符号栈与状态栈的实现方式, 实现 `loadTokens` 与 `loadLRTable`
2. 编写 `run`

## 提示

在处理符号栈时, 我们可能希望将 `Token` 和 `NonTerminal` 同时装在栈中. 但 `Token` 和 `NonTerminal` 并没有共同祖先类(除了`Object`). 我们当然可以使用 `Stack<Object>`, 但其在使用中有诸多不便. 于是我们期望有一个类似 `Union<Token, NonTerminal>` 的结构, 就可以将栈定义为: `Stack<Union<Token, NonTerminal>>`. 因为我们只将 `Union` 在这里使用一次, 我们可以简单定义一个 `Symbol` 来实现 `Union<Token, NonTerminal>` 的功能

下面给出一个 `Symbol` 的简单实现示例:

```Java
class Symbol{
    Token token;
    NonTerminal nonTerminal;

    private Symbol(Token token, NonTerminal nonTerminal){
        this.token = token;
        this.nonTerminal = nonTerminal;
    }

    public Symbol(Token token){
        this(token, null);
    }

    public Symbol(NonTerminal nonTerminal){
        this(null, nonTerminal);
    }

    public isToken(){
        return this.token != null;
    }

    public isNonterminal(){
        return this.nonTerminal != null;
    }
}
```

package cn.edu.hitsz.compiler.parser;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.parser.table.Production;
import cn.edu.hitsz.compiler.parser.table.Status;
import cn.edu.hitsz.compiler.symtab.SourceCodeType;
import cn.edu.hitsz.compiler.symtab.SymbolTable;

import java.util.Stack;

// TODO: 实验三: 实现语义分析
public class SemanticAnalyzer implements ActionObserver {
    private SymbolTable symbolTable;
    private Stack<Symbol> symbolStack=new Stack<>();
    @Override
    public void whenAccept(Status currentStatus) {
        // TODO: 该过程在遇到 Accept 时要采取的代码动作

//        throw new NotImplementedException();
    }

    @Override
    public void whenReduce(Status currentStatus, Production production) {
        // TODO: 该过程在遇到 reduce production 时要采取的代码动作
        int  idx= production.index();
        if (symbolTable == null){
            return;
        }
        switch (idx){
            case 4:
                    String s =symbolStack.peek().getToken().getText();
                    Symbol symbol = symbolStack.peek();
                    symbolStack.pop();
                    SourceCodeType type = symbolStack.peek().getType();
//                    System.out.println(type.toString()+','+s);
                    if(symbolTable.has(s)){
                        symbolTable.get(s).setType(type);
                    }
                    for(int i=0;i<production.body().size()-1;i++){
                        symbolStack.pop();
                    }
                    symbolStack.push(new Symbol());
                    break;

            case 5:
                break;
            default:
                for(int i=0;i<production.body().size();i++){
                    symbolStack.pop();
                }
                symbolStack.push(new Symbol());
                break;
        }
//        throw new NotImplementedException();
    }

    @Override
    public void whenShift(Status currentStatus, Token currentToken) {
        // TODO: 该过程在遇到 shift 时要采取的代码动作
        symbolStack.push(new Symbol(currentToken));
//        throw new NotImplementedException();
    }

    @Override
    public void setSymbolTable(SymbolTable table) {
        // TODO: 设计你可能需要的符号表存储结构
        // 如果需要使用符号表的话, 可以将它或者它的一部分信息存起来, 比如使用一个成员变量存储
            this.symbolTable = table;
//        throw new NotImplementedException();
    }
    class Symbol{
        Token token;
        SourceCodeType sourceCodeType;

        private Symbol(Token token,SourceCodeType sourceCodeType){
            this.token=token;
            this.sourceCodeType=sourceCodeType;
        }

        public Symbol(Token token){
            this(token,null);
            if(token.getKindId().equals("int")) {
                this.sourceCodeType = SourceCodeType.Int;
            }
        }
        public Symbol(SourceCodeType sourceCodeType){
            this(null,sourceCodeType);
        }
        public Symbol(){
            this(null, null);
        }

        public boolean isToken(){
            return this.token != null;
        }

        public Token getToken(){
            return this.token;
        }

        public SourceCodeType getType(){
            return  this.sourceCodeType;
        }
    }


}


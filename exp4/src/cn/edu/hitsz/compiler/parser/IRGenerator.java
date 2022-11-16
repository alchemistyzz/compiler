package cn.edu.hitsz.compiler.parser;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.ir.IRImmediate;
import cn.edu.hitsz.compiler.ir.IRValue;
import cn.edu.hitsz.compiler.ir.IRVariable;
import cn.edu.hitsz.compiler.ir.Instruction;
import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.parser.table.Production;
import cn.edu.hitsz.compiler.parser.table.Status;
import cn.edu.hitsz.compiler.symtab.SourceCodeType;
import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static cn.edu.hitsz.compiler.ir.IRVariable.named;
import static cn.edu.hitsz.compiler.ir.IRVariable.temp;

// TODO: 实验三: 实现 IR 生成

/**
 *
 */
public class IRGenerator implements ActionObserver {
    List<Instruction> immediatecode =new ArrayList<>();
    private SymbolTable symbolTable;
    private Stack<Symbol> symbolStack=new Stack<>();
    @Override
    public void whenShift(Status currentStatus, Token currentToken) {
        // TODO
//        throw new NotImplementedException();
        symbolStack.push(new Symbol(currentToken));


    }

    @Override
    public void whenReduce(Status currentStatus, Production production) {
        // TODO
//        throw new NotImplementedException();
        int  idx= production.index();
        switch (idx){
            case 6:
                Symbol symbol1 = symbolStack.peek();

                symbolStack.pop();
                symbolStack.pop();
                Symbol symbol2 = symbolStack.peek();

//                System.out.println(symbol2.irVariable);
//                System.out.println(symbol1.getIRvalue());
                Instruction instruction = Instruction.createMov(symbol2.irVariable,symbol1.getIRvalue());
                immediatecode.add(instruction);
                for(int i=0;i<production.body().size()-2;i++){
                    symbolStack.pop();
                }
                symbolStack.push(new Symbol());
                break;
            case 7:
                Symbol symbol3=symbolStack.peek();
                symbolStack.pop();
                Instruction instruction1 = Instruction.createRet(symbol3.getIRvalue());
                for(int i=0;i<production.body().size()-1;i++){
                    symbolStack.pop();
                }
                immediatecode.add(instruction1);
                symbolStack.push(new Symbol());
                break;
            case 8:
                Symbol symbol4=symbolStack.peek();
                symbolStack.pop();
                symbolStack.pop();
                Symbol symbol5=symbolStack.peek();
                symbolStack.pop();
                IRVariable temp1 = IRVariable.temp();
                Instruction instruction2=Instruction.createAdd(temp1,symbol5.getIRvalue(),symbol4.getIRvalue());
                immediatecode.add(instruction2);
                for(int i=0;i<production.body().size()-3;i++){
//                    System.out.println('1');
                    symbolStack.pop();
                }
                symbolStack.push(new Symbol(temp1));
                break;
            case 9:
//                System.out.println("1");
                Symbol symbol6=symbolStack.peek();
                symbolStack.pop();
                symbolStack.pop();
                Symbol symbol7=symbolStack.peek();
                symbolStack.pop();
                IRVariable temp2 = IRVariable.temp();
                Instruction instruction3=Instruction.createSub(temp2,symbol7.getIRvalue(),symbol6.getIRvalue());
                immediatecode.add(instruction3);
                for(int i=0;i<production.body().size()-3;i++){
                    symbolStack.pop();
                }
                symbolStack.push(new Symbol(temp2));
                break;
            case 11:
                Symbol symbol8=symbolStack.peek();
                symbolStack.pop();
                symbolStack.pop();
                Symbol symbol9=symbolStack.peek();
                symbolStack.pop();
                IRVariable temp3 = IRVariable.temp();
                Instruction instruction4=Instruction.createMul(temp3,symbol9.getIRvalue(),symbol8.getIRvalue());
                immediatecode.add(instruction4);
                for(int i=0;i<production.body().size()-3;i++){
                    symbolStack.pop();
                }
                symbolStack.push(new Symbol(temp3));
                break;
            case 13:
//                System.out.println('1');
                symbolStack.pop();
                Symbol symbol10 = symbolStack.peek();
                for(int i=0;i<production.body().size()-1;i++){
                    symbolStack.pop();
                }
                symbolStack.push(symbol10);
                break;
            case 1,2,3:
                for(int i=0;i<production.body().size();i++){
                    symbolStack.pop();
                }
                symbolStack.push(new Symbol());
                break;

            default:
                break;

        }
    }


    @Override
    public void whenAccept(Status currentStatus) {
        // TODO
//        throw new NotImplementedException();
    }

    @Override
    public void setSymbolTable(SymbolTable table) {
        // TODO
        this.symbolTable=table;
//        throw new NotImplementedException();
    }

    public List<Instruction> getIR() {
        // TODO
        return immediatecode;
//        throw new NotImplementedException();
    }

    public void dumpIR(String path) {
        FileUtils.writeLines(path, getIR().stream().map(Instruction::toString).toList());
    }
    class Symbol{
        Token token;
        IRVariable irVariable;
        IRImmediate irImmediate;

        private Symbol(Token token, IRVariable irVariable,IRImmediate immediate){
            this.token = token;
            this.irVariable = irVariable;
            this.irImmediate = immediate;
        }

        public Symbol(Token token){
            this(token, null,null);
            setIrVariable();
        }

        public Symbol(IRVariable irVariable){
            this(null, irVariable,null);
        }

        public Symbol(IRImmediate immediate){
            this(null, null,immediate);
        }

        public Symbol(){ this(null,null,null); }

        public boolean isToken(){
            return this.token != null;
        }

        public Token getToken(){
            return this.token;
        }

        public IRValue getIRvalue(){
//            if(this.token == null)  return null;
            if(this.irImmediate!=null){
                return this.irImmediate;
            }
            else{
                return this.irVariable;
            }

        }


        public void setIrVariable() {
            if(this.token != null && this.token.getText() != null){
                if(this.token.getKindId().equals("IntConst")){
                    this.irImmediate = IRImmediate.of(Integer.parseInt(this.token.getText()));
                }else {
                    this.irVariable = IRVariable.named(this.token.getText());
                }
            }
        }
    }
}


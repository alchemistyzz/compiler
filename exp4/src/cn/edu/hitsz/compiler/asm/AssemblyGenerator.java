package cn.edu.hitsz.compiler.asm;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.ir.*;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.util.*;


/**
 * TODO: 实验四: 实现汇编生成
 * <br>
 * 在编译器的整体框架中, 代码生成可以称作后端, 而前面的所有工作都可称为前端.
 * <br>
 * 在前端完成的所有工作中, 都是与目标平台无关的, 而后端的工作为将前端生成的目标平台无关信息
 * 根据目标平台生成汇编代码. 前后端的分离有利于实现编译器面向不同平台生成汇编代码. 由于前后
 * 端分离的原因, 有可能前端生成的中间代码并不符合目标平台的汇编代码特点. 具体到本项目你可以
 * 尝试加入一个方法将中间代码调整为更接近 risc-v 汇编的形式, 这样会有利于汇编代码的生成.
 * <br>
 * 为保证实现上的自由, 框架中并未对后端提供基建, 在具体实现时可自行设计相关数据结构.
 *
 * @see AssemblyGenerator#run() 代码生成与寄存器分配
 */
public class AssemblyGenerator {
    private  List<Instruction> preservedInstructions;
    List<AssemblyCode> assemblyCodeList = new ArrayList<>();
    //这里采用了双射的map
    BMap<IRValue, Reg.RegName> RegMap = new BMap<>();


    /**
     * 加载前端提供的中间代码
     * <br>
     * 视具体实现而定, 在加载中或加载后会生成一些在代码生成中会用到的信息. 如变量的引用
     * 信息. 这些信息可以通过简单的映射维护, 或者自行增加记录信息的数据结构.
     *
     * @param originInstructions 前端提供的中间代码
     */
    public void loadIR(List<Instruction> originInstructions) {
        // TODO: 读入前端提供的中间代码并生成所需要的信息
        this.preservedInstructions = originInstructions;
//        System.out.println(this.preservedInstructions);
        List<Instruction> newInstrucions = new ArrayList<Instruction>();
        for(int i=0;i<originInstructions.size();i++){
            Instruction instruction = originInstructions.get(i);
            InstructionKind instructionKind =instruction.getKind();
            if(instructionKind.isReturn()){
                newInstrucions.add(instruction);
                break;
            }



            //判定是否是二元参数如果是的话，需要在此句之前加上一个MOV语句
            else if(instructionKind.isBinary()){
                IRValue lhs = instruction.getLHS();
                IRValue rhs = instruction.getRHS();
                IRVariable result_reg = instruction.getResult();
                if(lhs.isImmediate()&& rhs.isImmediate()){//如果都是立即数的话直接计算出答案
                    int numlhs = ((IRImmediate)lhs).getValue();
                    int numrhs = ((IRImmediate)rhs).getValue();
                    int numresult = 0;
                    switch (instructionKind){
                        case ADD -> numresult = numlhs + numrhs;
                        case MUL -> numresult = numlhs * numrhs;
                        case SUB -> numresult = numlhs - numrhs;
                    }
                    IRImmediate tempImm = IRImmediate.of(numresult);
                    Instruction newinstruction = Instruction.createMov(result_reg,tempImm);
                    newInstrucions.add(newinstruction);
                }
                else if(lhs.isImmediate()|| rhs.isImmediate()){
                    switch (instructionKind){
                        case ADD -> {
                            if (lhs.isImmediate()) {//调整顺序成 a: b op imm
                                newInstrucions.add(Instruction.createAdd(result_reg, rhs, lhs));
                            } else {//格式正确不用更改
                                newInstrucions.add(instruction);
                            }
                        }
                        case MUL-> {
                            if (lhs.isImmediate()) {
                                newInstrucions.add(Instruction.createMov(result_reg, lhs));
                                newInstrucions.add(Instruction.createMul(result_reg, result_reg, rhs));
                            } else {
                                newInstrucions.add(Instruction.createMov(result_reg, rhs));
                                newInstrucions.add(Instruction.createMul(result_reg, lhs, result_reg));
                            }
                        }
                        case SUB-> {
                            if (lhs.isImmediate()) {
                                newInstrucions.add(Instruction.createMov(result_reg, lhs));
                                newInstrucions.add(Instruction.createSub(result_reg, result_reg, rhs));
                            } else {
                                newInstrucions.add(instruction);
                            }
                        }

                    }
                }
                else {
                    newInstrucions.add(instruction);
                }

            }
            else {
                    newInstrucions.add(instruction);
            }


        }
//        throw new NotImplementedException();
        this.preservedInstructions = newInstrucions;
//        System.out.println(this.preservedInstructions);
    }


    /**
     * 执行代码生成.
     * <br>
     * 根据理论课的做法, 在代码生成时同时完成寄存器分配的工作. 若你觉得这样的做法不好,
     * 也可以将寄存器分配和代码生成分开进行.
     * <br>
     * 提示: 寄存器分配中需要的信息较多, 关于全局的与代码生成过程无关的信息建议在代码生
     * 成前完成建立, 与代码生成的过程相关的信息可自行设计数据结构进行记录并动态维护.
     */
    public void run() {
        // TODO: 执行寄存器分配与代码生成
//        throw new NotImplementedException();
        assemblyCodeList.add(AssemblyCode.CreatText());
        for(int i =0 ;i<this.preservedInstructions.size();i++){
            Instruction instruction = this.preservedInstructions.get(i);
            InstructionKind instructionKind = instruction.getKind();
            //分配空闲寄存器
            if(instructionKind.isUnary()){
                var rd = instruction.getResult();
                var rs = instruction.getFrom();
                //分配寄存器
                Reg rd_reg =  getFreeReg(rd,i);
                if(rs.isImmediate()){//如果是立即数的话直接用li指令就可以了
                    assemblyCodeList.add(AssemblyCode.CreatLi(rd_reg,((IRImmediate)rs).getValue(),instruction.toString()));
                }
                else{//如果不是立即数的话还是要用mov指令
                    //分配寄存器
                    var rs_reg=getFreeReg((IRVariable)rs,i);
                    assemblyCodeList.add(AssemblyCode.CreatMv(rd_reg,rs_reg,instruction.toString()));
                }

            }
            else if(instructionKind.isBinary()){
                var rd = instruction.getResult();
                var lhs = instruction.getLHS();
                var rhs = instruction.getRHS();
                Reg rd_reg = getFreeReg(rd,i);
//                System.out.println(rd_reg.getRegtext());
                Reg lhs_reg = getFreeReg(lhs,i);
//                System.out.println(lhs_reg.getRegtext());
                Reg rhs_reg = getFreeReg(rhs,i);
//                System.out.println(rhs_reg.getRegtext());
//                System.out.println('1');

                switch (instructionKind) {
                    case ADD -> {
                        if (rhs.isIRVariable()){
                            assemblyCodeList.add(AssemblyCode.CreatAdd(rd_reg, lhs_reg, rhs_reg, instruction.toString()));
                        } else {
                            assemblyCodeList.add(AssemblyCode.CreatAddi(rd_reg, lhs_reg, ((IRImmediate)rhs).getValue(), instruction.toString()));
                        }
                    }
                    case SUB -> {
                        if (rhs.isIRVariable()){
                            assemblyCodeList.add(AssemblyCode.CreatSub(rd_reg, lhs_reg, rhs_reg, instruction.toString()));
                        } else {
                            assemblyCodeList.add(AssemblyCode.CreatSubi(rd_reg, lhs_reg, ((IRImmediate)rhs).getValue(), instruction.toString()));
                        }
                    }
                    case MUL -> {
                        assemblyCodeList.add(AssemblyCode.CreatMul(rd_reg, lhs_reg, rhs_reg, instruction.toString()));
                    }
                }

            }
            else {
                //只剩ret了
                var retval = instruction.getReturnValue();
                if (retval.isImmediate()){
                    int retval_imm = ((IRImmediate)retval).getValue();
                    assemblyCodeList.add(AssemblyCode.CreatRet(retval_imm,instruction.toString()));
                }
                else{
                    var retval_reg = getFreeReg(retval,i);
                    assemblyCodeList.add(AssemblyCode.CreatRet(retval_reg,instruction.toString()));
                }
                break;
            }


        }

    }
    public  Reg getFreeReg(IRValue op,int instructionIdx){
        if(op.isImmediate()){
            return null;
        }
        if(op.isIRVariable()){
            //变量已经找到对应的临时寄存器
            if(RegMap.containsKey(op)){
                Reg.RegName regname = RegMap.getByKey(op);
                return new Reg(regname);
            }
            List <Reg.RegName>freeRegNames = new ArrayList<>();
//            for(Reg.RegName regName: Reg.RegName.values()){
//                freeRegNames.add(regName);
//            }
            //变量还未找到对应的寄存器，通过对对应的寄存器进行循环判定是否有空闲的寄存器
            for(Reg.RegName regName: Reg.RegName.values()){
//                System.out.println(regName);
                if(RegMap.containsValue(regName)){
//                    System.out.println(1);
                    continue;
                }
                else{
                    //找对应的空闲的寄存器，加入到相应的list中
                    freeRegNames.add(regName);
                }
            }
            //如果有空闲寄存器的话
            if(!freeRegNames.isEmpty()){
                Reg.RegName random_reg1 = freeRegNames.get((int)Math.random()*(freeRegNames.size()));
                RegMap.replace(op,random_reg1);
                return new Reg(random_reg1);
            }
        }

            //根据指导书，如果没有找到空闲的寄存器的话需要找到后面指令不需要的寄存器进行寄存器分配，如果有则直接分配
            //如果找不到则直接报错
            List<Reg.RegName> regNames = new ArrayList<>();
            for(Reg.RegName regName : Reg.RegName.values()){//统计可用的非ret语句所有可用的寄存器名字应该是t0-t6
                if(regName != Reg.RegName.a0){
                    regNames.add(regName);
                }
            }
            for(int i=instructionIdx;i<this.preservedInstructions.size();i++){
                Instruction instruction = this.preservedInstructions.get(i);
                for(IRValue irValue:instruction.getOperands()){
                    Reg.RegName regName =  RegMap.getByKey(irValue);
                    if(regNames.contains(regName)){
                        regNames.remove(regName);
                    }
                }
            }
//            System.out.println(regNames);
            if(regNames.isEmpty()){
                System.out.println("没有空余的寄存器可供分配！");
                throw new RuntimeException();
            }
            else{

                Reg.RegName random_reg2 = regNames.get((int)Math.random()*(regNames.size()));
                RegMap.replace(op,random_reg2);
                return new Reg(random_reg2);
            }
    }

    /**
     * 输出汇编代码到文件
     *
     * @param path 输出文件路径
     */
    public void dump(String path) {
        // TODO: 输出汇编代码到文件
        FileUtils.writeLines(path, assemblyCodeList.stream().map(AssemblyCode::toString).toList());
//        throw new NotImplementedException();
    }

}


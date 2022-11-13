package cn.edu.hitsz.compiler.asm;

import java.util.Objects;
import java.util.stream.Collectors;

public class AssemblyCode {

//============================== 不同种类 AssemblyCode 的构造函数 ==============================
    public static AssemblyCode CreatAdd(Reg rd, Reg rs1, Reg rs2, String originCode) {
        return new AssemblyCode("add", rd.getRegtext(), rs1.getRegtext(), rs2.getRegtext(), originCode);
    }
    public static AssemblyCode CreatAddi(Reg rd, Reg rs1, int imm, String originCode){
        return new AssemblyCode("addi", rd.getRegtext(), rs1.getRegtext(), ""+imm, originCode);
    }
    public static AssemblyCode CreatMul(Reg rd, Reg rs1, Reg rs2, String originCode){
        return new AssemblyCode("mul", rd.getRegtext(), rs1.getRegtext(), rs2.getRegtext(), originCode);
    }
    public static AssemblyCode CreatSub(Reg rd, Reg rs1, Reg rs2, String originCode){
        return new AssemblyCode("sub", rd.getRegtext(), rs1.getRegtext(), rs2.getRegtext(), originCode);
    }
    public static AssemblyCode CreatSubi(Reg rd, Reg rs1, int imm, String originCode){
        return new AssemblyCode("subi", rd.getRegtext(), rs1.getRegtext(), ""+imm, originCode);
    }
    public static AssemblyCode CreatMv(Reg rd, Reg rs, String originCode){
        return new AssemblyCode("mv", rd.getRegtext(), null, rs.getRegtext(), originCode);
    }
    public static AssemblyCode CreatLi(Reg rd, int imm, String originCode){
        return new AssemblyCode("li", rd.getRegtext(), null, ""+imm, originCode);
    }
    public static AssemblyCode CreatRet(Reg rs2, String originCode){
        return new AssemblyCode("mv", "a0", null, rs2.getRegtext(), originCode);
    }
    public static AssemblyCode CreatRet(int imm, String originCode){
        return new AssemblyCode("mv", "a0", null, ""+imm, originCode);
    }
    public static AssemblyCode CreatText() {
        return new AssemblyCode(".text", null, null, null,null);
    }


    //============================== 基础设施 ==============================
    private String op;
    private String rd;
    private String rs1;
    private String rs2;
    private String OriginInstruction;

    private AssemblyCode(String op, String rd, String rs1, String rs2, String originCode) {
        this.op = op;
        this.rd = rd;
        this.rs1 = rs1;
        this.rs2 = rs2;
        this.OriginInstruction = originCode;
    }

    /*
        格式化输出
     */
    @Override
    public String toString() {
        if(".text".equals(this.op)){
            return ".text";
        }
        String text ="\t"+this.op+" "+this.rd+","+" ";
        if(this.rs1!=null){
            text += this.rs1+","+" ";

        }
        if(this.rs2!=null){
            text += this.rs2+"\t";
        }
        if(this.OriginInstruction!=null){
            text += "\t#"+this.OriginInstruction;
        }
        return text;
    }
}

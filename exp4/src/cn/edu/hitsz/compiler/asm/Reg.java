package cn.edu.hitsz.compiler.asm;
import java.util.ArrayList;
import java.util.List;

public class Reg{
   public enum RegName{
         a0 ,t0, t1, t2, t3, t4, t5, t6;
   }
   private RegName regName;
   private String regtext;

   public Reg(RegName regName){
        this.regName = regName;
        this.regtext = regName.toString();
   }

   public String toString(){
        return regName.toString();
   }
   public String getRegtext(){
       return regtext;
   }
    public RegName getRegName() {
        return regName;
    }
}

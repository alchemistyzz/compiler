package cn.edu.hitsz.compiler.lexer;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.io.*;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.stream.StreamSupport;

/**
 * TODO: 实验一: 实现词法分析
 * <br>
 * 你可能需要参考的框架代码如下:
 *
 * @see Token 词法单元的实现
 * @see TokenKind 词法单元类型的实现
 */
public class LexicalAnalyzer {
    private final SymbolTable symbolTable;
    private static final ArrayList<Token> TOKEN_ARRAY_LIST =new ArrayList<>();
    private static final ArrayList<String> lines = new ArrayList<>();
    public LexicalAnalyzer(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;//这个对象在下面操作读入对应文本前是空的
    }


    /**
     * 从给予的路径中读取并加载文件内容
     *
     * @param path 路径
     */
    public void loadFile(String path) {
        // TODO: 词法分析前的缓冲区实现
        // 可自由实现各类缓冲区
        // 或直接采用完整读入方法
        try {
            File file = new File(path);
            if(file.isFile() && file.exists()) {
                InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "utf-8");
                BufferedReader br = new BufferedReader(isr);
                String lineTxt = null;
                while ((lineTxt = br.readLine()) != null) {
                     lines.add(lineTxt);
                     System.out.println(lineTxt);
                }
                br.close();
            } else {
                System.out.println("文件不存在!");
            }
        } catch (Exception e) {
            System.out.println("文件读取错误!");
        }

//        throw new NotImplementedException();
    }

    /**
     * 执行词法分析, 准备好用于返回的 token 列表 <br>
     * 需要维护实验一所需的符号表条目, 而得在语法分析中才能确定的符号表条目的成员可以先设置为 null
     */
    public void run() {
        // TODO: 自动机实现的词法分析过程
        //循环遍历所有的lines
        for(String line:lines){
            
            char now;
            for(int p=0;p<line.length();p++){
                now = line.charAt(p);
                switch (now){
                    case ' ','\n':
                        break;
                    case ';':
                        TOKEN_ARRAY_LIST.add(Token.simple(TokenKind.fromString("Semicolon")));
                        break;
                    case '*','/','+','-','=','(',')':
                        TOKEN_ARRAY_LIST.add(Token.simple(TokenKind.fromString(now+"")));
                        break;
                    case '0','1','2','3','4','5','6','7','8','9':
                        int end = p;
                        String constint = "";
                        while(Character.isDigit(line.charAt(end))){
                            constint += line.charAt(end);
                            end++;
                        }
                        p = end-1;
                        TOKEN_ARRAY_LIST.add(Token.normal("IntConst",constint));
                        break;
                    default:
                        if(Character.isLetter(now)){
                            int end2 = p;
                            String identifier="";
                            while(Character.isLetterOrDigit(line.charAt(end2))){
                                identifier += line.charAt(end2);
                                end2 ++;
                            }
                            p = end2-1;
                            if(!TokenKind.isAllowed(identifier)){
                                //标志符为id像result、a、b
                                TOKEN_ARRAY_LIST.add(Token.normal(TokenKind.fromString("id"),identifier));
                                if(!symbolTable.has(identifier)){
                                    symbolTable.add(identifier);
                                }
                    
                            }
                            else {
                                //正常的关键字像int,return
                                TOKEN_ARRAY_LIST.add(Token.simple(TokenKind.fromString(identifier)));
                            }
                            break;
                        }    
                }
                    
            }
        }
        //最后加上终结符eof
        TOKEN_ARRAY_LIST.add(Token.eof());
    }









    /**
     * 获得词法分析的结果, 保证在调用了 run 方法之后调用
     *
     * @return Token 列表
     */
    public Iterable<Token> getTokens() {
        // TODO: 从词法分析过程中获取 Token 列表
        // 词法分析过程可以使用 Stream 或 Iterator 实现按需分析
        // 亦可以直接分析完整个文件
        // 总之实现过程能转化为一列表即可
        return TOKEN_ARRAY_LIST;

//        throw new NotImplementedException();
    }

    public void dumpTokens(String path) {
        FileUtils.writeLines(
            path,
            StreamSupport.stream(getTokens().spliterator(), false).map(Token::toString).toList()
            //并行(虽然并没有用)遍历的迭代器
        );
    }


}

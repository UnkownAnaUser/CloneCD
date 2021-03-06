package ccd;

import antlr.Java8Lexer;
import antlr.Java8Parser;
import ccd.h1.MethodVisitor;
import ccd.h1.WithTokenVisitor;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import redis.clients.jedis.Jedis;
import util.RedisUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class WorkThread extends Thread{
    private List<String> fileList;
    private int threadId;

    public WorkThread(List<String> fileList, int threadId) {
        this.fileList = fileList;
        this.threadId = threadId;
    }

    public void run(){
        CharStream inputStream;
        Java8Parser parser;
        ParseTree parseTree;
//        MethodVisitor methodVisitor = new MethodVisitor();
        WithTokenVisitor methodVisitor = new WithTokenVisitor();
        Jedis redis = RedisUtil.getJedis();
        int fileSize = fileList.size();
        int i = 0;
        for (String file : fileList) {
            try {
                inputStream = CharStreams.fromFileName(file);
                parser = new Java8Parser(new CommonTokenStream(new Java8Lexer(inputStream)));
                parseTree = parser.compilationUnit();
                methodVisitor.setPathFilename(file);
//                methodVisitor.setRedis(redis);
                methodVisitor.visit(parseTree);
//                File f = new File(file);
//                if(f.exists()&&f.isFile())
//                    f.delete();
                i++;
                System.out.println("thread"+threadId+": "+i+"/"+fileSize);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("thread"+threadId+"end time: "+new SimpleDateFormat("yyy-MM-dd hh:mm:ss")
                .format(new Date(System.currentTimeMillis())));
        RedisUtil.returnResource(redis);
    }
}

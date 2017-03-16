package com.study.thread;

import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.List;

/**
 * Created by zhanglei on 2017/3/16.
 */
public class TestTask implements Runnable {
    private int num;

    private List<Integer> list;

    public TestTask() {

    }

    public TestTask(int num, List<Integer> list) {
        this.num = num;
        this.list = list;
    }

    @Override
    public void run() {
        File file = new File("d:/test.txt");
        try {
            if (num == 5) {
                FileWriter writer = new FileWriter(file);
                writer.write("2");
                writer.flush();
                for(int i=0;i<list.size();i++){
                    list.set(i,10);
                }
            } else {
                FileReader reader = new FileReader(file);
                BufferedReader br = new BufferedReader(reader);
                String line = "";
                while ((line = br.readLine()) != null) {
                    if (StringUtils.equals("2", line)) {
                        for(int i=0;i<list.size();i++){
                            list.set(i,10);
                        }
                    } else {
                        for(int i=0;i<list.size();i++){
                            list.set(i,list.get(i).intValue()-i);
                        }
                    }
                }
            }
            System.out.println("当前第"+num+"进程："+list);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

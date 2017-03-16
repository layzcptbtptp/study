package com.study.test;

import com.study.thread.TestTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhanglei on 2017/3/16.
 */
public class TestFor {
    public static void main(String[] args) throws Exception {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(i, 10);
        }
        for (int i = 0; i < 10; i++) {
            TestTask task = new TestTask(i, list);
            Thread t = new Thread(task);
            t.start();
        }
    }
}

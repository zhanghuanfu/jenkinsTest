package beforetest;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class TestSomeThing {

//	public static void main(String[] args) {

//		File file = new File("D://test.txt");
//		String path = file.getAbsolutePath();
//		System.out.println(path);
//		try {
//			new FileWriter(file);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		if(!file.exists()){
//			try {
//				file.createNewFile();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		TestForMain testForMain = new TestForMain();
//		try {
//			testForMain.test();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	private int j;

    public static void main(String[] args) {
    	TestSomeThing t = new TestSomeThing();
        for(int i =0;i<2;i++) {
        	Thread t1 = new Inc(t);
        	t1.start();
            Thread t2 = new Dec(t);
            t2.start();
        }
    }

    public synchronized void inc() {
        j++;
        System.out.println(Thread.currentThread().getName() + ":inc" + j);
    }

    public synchronized void dec() {
        j--;
        System.out.println(Thread.currentThread().getName() + ":dec" + j);
    }

}

class Inc extends Thread {
    private TestSomeThing a;

    Inc(TestSomeThing a) {
        this.a = a;
    }

    @Override
    public void run() {
        for (int i = 0; i < 100; i++) {
            a.inc();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class Dec extends Thread {
    private TestSomeThing a;

    Dec(TestSomeThing a) {
        this.a = a;
    }

    @Override
    public void run() {
        for (int i = 0; i < 100; i++) {
            a.dec();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    List features = Arrays.asList("Lambdas", "Default Method", "Stream API", "Date and Time API");
//    features.forEach(n -> System.out.println(n));
     
    // 使用Java 8的方法引用更方便，方法引用由::双冒号操作符标示，
    // 看起来像C++的作用域解析运算符
//    features.forEach(System.out::println);
}

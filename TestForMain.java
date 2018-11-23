package beforetest;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

public class TestForMain {

	public void test() throws IOException {
		 
		 DataOutputStream os;  
		 FileWriter fw;
		 FileOutputStream fo;
		 
        os = new DataOutputStream(new FileOutputStream("D://test1.txt"));  
        long time1 = System.currentTimeMillis();  
        for (int i = 0; i < 100000000; i++) {  
            os.write("���Բ��Բ��Բ���/n".getBytes());  
        }  
        os.flush();  
        os.close();
        
        fo = new FileOutputStream("D://test2.txt");
        long time2 = System.currentTimeMillis();
        for (int i = 0; i < 100000000; i++) {
		 fo.write("���Բ��Բ��Բ���/n".getBytes());
        }
		 fo.flush();
		 fo.close();
 
        long time3 = System.currentTimeMillis();  
        fw = new FileWriter(new File("D://test3.txt"));  
        for (int i = 0; i < 100000000; i++) {  
            fw.write("���Բ��Բ��Բ���/n");  
        }  
        fw.flush();  
        fw.close();  
        long time4 = System.currentTimeMillis();  
 
        System.out.println("DataOutputStream��ʱ:" + (time2 - time1));  
        System.out.println("FileOutputStream��ʱ:" + (time3 - time2));
//        System.out.println("FileWriter��ʱ:" + (time4 - time3));
	     }
}

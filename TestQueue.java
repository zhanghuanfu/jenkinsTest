package beforetest;

import java.util.Random;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

public class TestQueue {  
    public static void main(String[] args) throws InterruptedException {  
        SynchronousQueue<Integer> queue = new SynchronousQueue<Integer>();  
      
        new Product(queue).start();  
        new Customer(queue).start();  
    }  
    static class Product extends Thread{  
        SynchronousQueue<Integer> queue;  
        public Product(SynchronousQueue<Integer> queue){  
            this.queue = queue;  
        }  
        @Override  
        public void run(){  
            while(true){  
                int rand = new Random().nextInt(1000);  
                System.out.println("生产了一个产品："+rand);  
                System.out.println("等待三秒后运送出去...");  
//                try {  
//                    TimeUnit.SECONDS.sleep(3);  
//                } catch (InterruptedException e) {  
//                    e.printStackTrace();  
//                }  
                try {  
                    queue.put(rand);
                    System.out.println("队列中put了  " + rand);
                    System.out.println(queue.isEmpty());
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {  
                    e.printStackTrace();  
                }  
                  
//                System.out.println(queue.isEmpty());  
            }  
        }  
    }  
    static class Customer extends Thread{  
        SynchronousQueue<Integer> queue;  
        public Customer(SynchronousQueue<Integer> queue){  
            this.queue = queue;  
        }  
        @Override  
        public void run(){  
            while(true){  
                try {  
                    System.out.println("消费了一个产品:"+queue.take());  
                } catch (InterruptedException e) {  
                    e.printStackTrace();  
                }  
                System.out.println("------------------------------------------");  
            }  
        }  
    }  
  
}

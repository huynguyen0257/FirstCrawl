package huyng.thread;

public class BaseThread extends Thread{
    protected BaseThread(){}

    private static BaseThread instance;
    private final static Object LOCK = new Object();

    public static BaseThread getInstance(){
        synchronized (LOCK){
            if (instance == null){
                instance = new BaseThread();
            }
        }
        return  instance;
    }

    private static boolean suspended = false;
    public static boolean isSuspended(){
        return suspended;
    }

    public static void setSuspended(boolean aSyspended){
        suspended = aSyspended;
    }

    public void suspendThread(){
        setSuspended(true);
        System.out.println("suspended");
    }

    public  synchronized void resumeThread(){
        setSuspended(false);
        notifyAll();
        System.out.println("resume");
    }
}

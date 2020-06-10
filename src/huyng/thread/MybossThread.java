package huyng.thread;

import huyng.constant.AppConstant;
import huyng.crawler.MybossCategoriesCrawler;
import huyng.crawler.MybossCrawler;

import javax.servlet.ServletContext;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

class run{
    public static void main(String[] args) {
        MybossThread a = new MybossThread(null);
        a.start();
    }
}

public class MybossThread extends BaseThread implements Runnable {

    private ServletContext context;

    public MybossThread(ServletContext context) {
        this.context = context;
    }

    @Override
    public void run() {
        while (true){
            try{
                MybossCategoriesCrawler categoriesCrawler = new MybossCategoriesCrawler(context);
                Map<String,String> categories = categoriesCrawler.getCategories(AppConstant.urlMyboss);
                for (Map.Entry<String,String> entry : categories.entrySet()){
                    Thread cralingThread = new Thread(new MybossCrawler(context, entry.getKey(), entry.getValue()));
                    cralingThread.start();

                    synchronized (BaseThread.getInstance()){
                        while (BaseThread.isSuspended()) {
                            BaseThread.getInstance().wait();
                        }
                    }
                }//end for each categories

                MybossThread.sleep(TimeUnit.DAYS.toMillis(AppConstant.breakTimeCrawling));
            } catch (InterruptedException e) {
                Logger.getLogger(MybossThread.class.getName()).log(Level.SEVERE, null,e);
            }
        }
    }
}

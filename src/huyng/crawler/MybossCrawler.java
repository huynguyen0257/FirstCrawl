package huyng.crawler;

import huyng.constant.AppConstant;
import huyng.dto.Category;
import huyng.thread.BaseThread;

import javax.servlet.ServletContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MybossCrawler extends BaseCrawler implements Runnable {

    private String url;
    private String categoryName;
    protected Category category = null;

    public MybossCrawler(ServletContext context, String url, String categoryName) {
        super(context);
        this.url = url;
        this.categoryName = categoryName;
    }

    @Override
    public void run() {
//        category = createCategory(categoryName); trong DAO
        category = new Category("1",categoryName);
        if (category == null){
            Logger.getLogger(MybossCrawler.class.getName()).log(Level.SEVERE,null,new Exception("Error: Category null"));
            return;
        }
        BufferedReader reader = null;
        try{
            reader = getBufferedREaderForURL(url);
            String line ="";
            String document = "";
            boolean isStart = false;
            boolean isEnding = false;
            int divCounter = 0;
            int divOpen = 0, divClose = 0;
            while ((line = reader.readLine()) != null){
                if (line.contains("<div id=\"phantrang\"")){
                    isStart = true;
                }
                if (isStart){
                    document += line;
                    if (line.contains("</div>")) break;
                }
            }
            try{
                synchronized (BaseThread.getInstance()){
                    while (BaseThread.isSuspended()) BaseThread.getInstance().wait();
                }
            } catch (InterruptedException e) {
                Logger.getLogger(MybossCrawler.class.getName()).log(Level.SEVERE,null,e);
            }

            int lastPage = getLastPage(document);

            for (int i = 0; i < lastPage; i++) {
                String pageUrl = url +"?page="+(i+1);
                Thread pageCrawlingThread = new Thread(new MyBossEachPageCrawler(this.getContext(),pageUrl,category));
                pageCrawlingThread.start();
                try{
                    synchronized (BaseThread.getInstance()){
                        while (BaseThread.isSuspended()) BaseThread.getInstance().wait();
                    }
                } catch (InterruptedException e) {
                    Logger.getLogger(MybossCrawler.class.getName()).log(Level.SEVERE,null,e);
                }
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    private int getLastPage(String document) throws UnsupportedEncodingException, XMLStreamException {
        document = document.trim();
        XMLEventReader eventReader = parseStringToXMLEventReader(document);
        String tagName = "";
        String link = "";
        while (eventReader.hasNext()){
            XMLEvent event = (XMLEvent) eventReader.next();
            if (event.isStartElement()){
                StartElement startElement = event.asStartElement();
                tagName = startElement.getName().getLocalPart();
                if ("a".equals(tagName)){
                    Attribute attrHref = startElement.getAttributeByName(new QName("href"));
                    link = attrHref == null ? "" : attrHref.getValue();
                }
            }//End if start element
        }//end While
        if (link != null && !link.isEmpty()){
            String regex = "[0-9]+$";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(link);
            if (matcher.find()){
                String result = matcher.group();
                try{
                    int number = Integer.parseInt(result);
                    return number;
                }catch (NumberFormatException e){
                    e.printStackTrace();
                }
            }
        }
        return 1;
    }
}

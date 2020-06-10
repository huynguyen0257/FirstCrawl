package huyng.crawler;

import huyng.constant.AppConstant;
import huyng.dto.Category;
import huyng.dto.Product;
import huyng.thread.BaseThread;
import huyng.thread.MybossThread;

import javax.servlet.ServletContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

class run{
    public static void main(String[] args) {
        MyBossEachPageCrawler a = new MyBossEachPageCrawler(null,"https://www.myboss.vn/tay-cam-choi-game-c14",null);
        a.run();
    }
}

public class MyBossEachPageCrawler extends BaseCrawler implements Runnable{

    private String url;
    private Category category;
    private List<Product> products;

    public MyBossEachPageCrawler(ServletContext context, String url, Category category) {
        super(context);
        this.url = url;
        this.category = category;
        this.products = new ArrayList<>();
    }

    @Override
    public void run() {
        BufferedReader reader = null;
        try{
            reader = getBufferedREaderForURL(url);
            String line = "";
            String document = "<document>";
            boolean isStart = false;
            boolean isEnding = false;
            int divCouter = 0;
            int divOpen = 0, divClose = 0;
            while ((line = reader.readLine()) != null){
                if (line.contains("<div class=\" listproduct view_grid\">")){
                    isStart = true;
                }
                if(line.contains("<div id=\"phantrang\" class=\"phantrang_news") || line.contains("<!-- end container-vina -->")) break;
                if (isStart) {
                    if (line.contains("&") && line.contains("<a href=\"/site")) {
                        line = line.replace("&","");
                        document += line;
                    }else{
                        document += line;
                    }
                }
            }
            document = document.replace("</div>\t</div></div>","");
            document += "</document>";
            try{
                synchronized (BaseThread.getInstance()){
                    while (BaseThread.isSuspended()){
                        BaseThread.getInstance().wait();
                    }
                }
            } catch (InterruptedException e) {
                Logger.getLogger(MybossCrawler.class.getName()).log(Level.SEVERE,null,e);
            }
            stAXparserForEachPage(document);
        } catch (IOException ex) {
            Logger.getLogger(MybossCrawler.class.getName()).log(Level.SEVERE,null,ex);
        }catch (XMLStreamException ex){
            Logger.getLogger(MyBossEachPageCrawler.class.getName()).log(Level.SEVERE,null,ex);
        }
    }

    private void stAXparserForEachPage(String document) throws UnsupportedEncodingException, XMLStreamException {
        document = document.trim();
        XMLEventReader eventReader = parseStringToXMLEventReader(document);
        Map<String,String> categories = new HashMap<String,String>();
        String detailLink = "";
        String imgLink = "";
        String price = "";
        String productName = "";
        boolean isStart = false;
        int count = 0;
        try{
            while (eventReader.hasNext()){
                count++;
                String tagName = "";
                    XMLEvent event = (XMLEvent) eventReader.next();
                    if (event.isStartElement()){
                        StartElement startElement = event.asStartElement();
                        tagName = startElement.getName().getLocalPart();
                        if(startElement.toString().contains("<div class='col-xs-6")){
                            isStart = true;
                        }else if("a".equals(tagName) && isStart){
                            event = (XMLEvent) eventReader.next(); //mote to img tag
                            startElement = event.asStartElement(); //get img tag
                            Attribute attrSrc = startElement.getAttributeByName(new QName("src"));
                            imgLink = attrSrc == null ? "" : attrSrc.getValue(); //get img link

                            //move to tag <a> contains product name
                            while (detailLink == null || detailLink.isEmpty()){
                                event = eventReader.nextTag();
                                if (event.isStartElement()) {
                                    startElement = event.asStartElement();
                                    if(startElement.getName().getLocalPart().equals("a")){
                                        Attribute attrHref = startElement.getAttributeByName(new QName("href"));
                                        detailLink = attrHref == null ? "" : attrHref.getValue(); //get detail link
                                    }
                                }
                            }

                            event = (XMLEvent) eventReader.next();
                            Characters character = event.asCharacters();
                            productName = character.getData().trim();

                            eventReader.nextTag();
                            eventReader.next();
                            eventReader.nextTag();
                            eventReader.next();
                            eventReader.nextTag();
                            event = (XMLEvent) eventReader.next(); //move to <span> tag
                            character = event.asCharacters();
                            price = character.getData().trim(); //get price

                            if (!detailLink.isEmpty()){
                                detailLink = AppConstant.urlMyboss + detailLink;
                            }
                            if (!imgLink.isEmpty()){
                                imgLink = AppConstant.urlMyboss +"/"+ imgLink;
                            }
                            isStart = false;
                            try{
                                price = price.replaceAll("\\D+","");
                                BigInteger realPrice = new BigInteger(price);
    //                        String categoryId = this.category.getCategoryId();
    //                        var product = new Product(new Long(1))
                                Product product = new Product(new Long(1),productName,realPrice,imgLink,"1",true,detailLink);
                                products.add(product);
                                detailLink = "";
    //                        ProductDAO.getInstance().saveProductWhenCrawling(product);
                            }catch (NumberFormatException ex){
                                Logger.getLogger(MybossCrawler.class.getName()).log(Level.SEVERE,null,ex);
                            }
                        }
                    }

            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("======================================"+count);
        }
        products.forEach(p -> System.out.println(p.getProductName() + "\t\t" + p.getProductPrice()));
    }
}

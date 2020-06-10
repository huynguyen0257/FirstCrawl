package huyng.crawler;

import com.sun.deploy.association.utility.AppConstants;
import huyng.constant.AppConstant;
import jdk.nashorn.internal.ir.WhileNode;

import javax.naming.Context;
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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;



public class MybossCategoriesCrawler extends BaseCrawler {

    public MybossCategoriesCrawler(ServletContext context) {
        super(context);
    }

    public Map<String,String> getCategories(String url){
        BufferedReader reader = null;
        try{
            reader = getBufferedREaderForURL(url);
            String line ="";
            String document = "";
            boolean isStart = false;
            boolean isFound = false;
            while ((line = reader.readLine()) != null){
                if (isStart && line.contains("</li>")) break;
                if (isStart) document += line.trim();
                if (isFound && line.contains("</h3>")) isStart = true;
                if (line.contains("<a href=\"/thiet-bi-choi-game-Ca1.html\"")) isFound = true;
            }
            return stAXParserForCategories(document);
//            System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$DOCUMENT: "+ document);
        }catch (UnsupportedEncodingException ex){
            Logger.getLogger(BaseCrawler.class.getName()).log(Level.SEVERE,null,ex);
        }catch (IOException ex){
            Logger.getLogger(BaseCrawler.class.getName()).log(Level.SEVERE,null,ex);
        }catch (XMLStreamException ex){
            Logger.getLogger(MybossCategoriesCrawler.class.getName()).log(Level.SEVERE,null,ex);
        }finally {
            try{
                if (reader !=null) reader.close();
            }catch (IOException ex){
                Logger.getLogger(BaseCrawler.class.getName()).log(Level.SEVERE,null,ex);
            }
        }
        return null;
    }

    private Map<String, String> stAXParserForCategories(String document) throws UnsupportedEncodingException,XMLStreamException{
        document = document.trim();
        XMLEventReader eventReader = parseStringToXMLEventReader(document);
        Map<String,String> categories = new HashMap<>();
        while (eventReader.hasNext()){
            XMLEvent event = (XMLEvent) eventReader.next(); // lấy thẻ con tiếp theo của root hiện hành
            //<div class="submenut"><a href="loa-gaming-c44">Loa Gaming</a></div>
            // sẽ lấy event = <div class="submenut"> -> event = <a href="loa-gaming-c44"> -> event = Loa Gaming(là text) -> event = </a> -> evnt = </div>
            if (event.isStartElement()){
                //lấy ra tagName=a trong <a ... >
                StartElement startElement = event.asStartElement();
                String tagName = startElement.getName().getLocalPart();
                if ("a".equals(tagName)){
                    Attribute attrHref = startElement.getAttributeByName(new QName("href")); //lấy thuộc tính href trong a
                    String link = AppConstant.urlMyboss +"/"+ attrHref.getValue();//Lấy giá trị của thuộc tính href => tạo link category tiếp theo
                    event = (XMLEvent) eventReader.next();
                    Characters character = event.asCharacters();
                    categories.put(link, character.getData());
                }
            }
        }
        return categories;
    }
}

package config;

import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Created by awaigand on 21.04.2015.
 */
public class ConfigHandler {

    public static final String CONFIG_FILE_NAME = "jchat-config.xml";
    public static final String IP_ATTRIBUTE_NAME = "IP";
    public static final String SERVERS_ELEMENT_NAME = "servers";
    public static final String ROOT_ELEMENT_NAME = "JChat";
    public static final String SERVER_ELEMENT_NAME = "server";
    public static final String PORT_ATTRIBUTE_NAME = "PORT";
    public static final String SINCE_ELEMENT_NAME = "since";
    public static final String SINCE_XPATH = "//" + ROOT_ELEMENT_NAME + "/" + SINCE_ELEMENT_NAME;
    public static String username;
    public static String password;
    public static String currentSince;

    private Document document;

    private Document getDocument() {
        return document;
    }

    private void setDocument(Document document) {
        this.document = document;
    }

    public void writeSinceTime() throws IOException{
        Element ele = (Element) document.selectSingleNode(SINCE_XPATH);
        ele.setText(currentSince);
        XMLWriter writer = new XMLWriter(
                new FileWriter(CONFIG_FILE_NAME)
        );
        writer.write( document );
        writer.close();
    }

    private Document readConfigFromFile() throws DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(CONFIG_FILE_NAME);
        return document;
    }

    public ConfigHandler() throws DocumentException {
        setDocument(readConfigFromFile());

        Node node = document.selectSingleNode(SINCE_XPATH);
        currentSince = node.getText();
    }


    public static String getIPFromServerNode(Node server){
        return server.valueOf("@"+IP_ATTRIBUTE_NAME);
    }

    public static String getPortFromServerNode(Node server){
        return server.valueOf("@"+PORT_ATTRIBUTE_NAME);
    }

    public java.util.Iterator getServerIterator(){
        List servers = document.selectNodes("//" + SERVER_ELEMENT_NAME);
        return servers.iterator();
    }

    private static void addServerToElement(Element serversElement, String ip, String port ){
        Element singleServer = serversElement.addElement(SERVER_ELEMENT_NAME);
        singleServer.addAttribute(IP_ATTRIBUTE_NAME,ip);
        singleServer.addAttribute(PORT_ATTRIBUTE_NAME,port);
    }

    private static Document getDefaultConfigDocument(){
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement(ROOT_ELEMENT_NAME);
        Element servers = root.addElement(SERVERS_ELEMENT_NAME);
        Element since = root.addElement(SINCE_ELEMENT_NAME);

        addServerToElement(servers, "193.196.7.76", "8080");
        since.setText(new DateTime(DateTimeZone.UTC).toString(ISODateTimeFormat.dateTime()));

        return document;
    }

    public static boolean checkIfConfigExists(){
        File f = new File(CONFIG_FILE_NAME);
        return f.exists() && !f.isDirectory();
    }

    public static void writeDefaultConfig()throws IOException {

        Document document = getDefaultConfigDocument();
        XMLWriter writer = new XMLWriter(
                new FileWriter(CONFIG_FILE_NAME)
        );
        writer.write( document );
        writer.close();

    }



}

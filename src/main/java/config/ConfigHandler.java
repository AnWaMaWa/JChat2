package config;

import org.dom4j.*;
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
 * Handles everything related to the config file, as well as username and password.
 * Uses dom4j for writing and reading config file.
 * .properties file was not used for convenience  reasons.
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
    public static String currentSince; //ISO_8601 formatted server time since last received message

    private Document document;

    private Document getDocument() {
        return document;
    }

    private void setDocument(Document document) {
        this.document = document;
    }

    /**
     * Writes the currentSince time to the config file
     * This is used to show all messages since the last time the user received a message, i.e. during the time
     * the user was offline.
     * Currently only being called on program exit, which can be a problem if the users pc crashes or similar.
     * @throws IOException
     */
    public void writeSinceTime() throws IOException {
        Element ele = (Element) document.selectSingleNode(SINCE_XPATH);
        ele.setText(currentSince);
        XMLWriter writer = new XMLWriter(
                new FileWriter(CONFIG_FILE_NAME)
        );
        writer.write(document);
        writer.close();
    }

    private Document readConfigFromFile() throws DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(CONFIG_FILE_NAME);
        return document;
    }

    /**
     * Loads the config file into memory and reads the last time a user has received a message.
     * @throws DocumentException
     */

    public ConfigHandler() throws DocumentException {
        setDocument(readConfigFromFile());
        Node node = document.selectSingleNode(SINCE_XPATH);
        currentSince = node.getText();
    }


    /**
     * Returns the IP Attribute of a given server node
     * @param server
     * @return
     */
    public static String getIPFromServerNode(Node server) {
        return server.valueOf("@" + IP_ATTRIBUTE_NAME);
    }

    /**
     * Returns the PORT Attribute of a given server node.
     * @param server
     * @return
     */

    public static String getPortFromServerNode(Node server) {
        return server.valueOf("@" + PORT_ATTRIBUTE_NAME);
    }

    /**
     * Returns an iterator about all server nodes.
     * @return
     */

    public java.util.Iterator getServerIterator() {
        List servers = document.selectNodes("//" + SERVER_ELEMENT_NAME);
        return servers.iterator();
    }

    /**
     * Adds a server node to the given element. Used for writing new servers to config file.
     * @param serversElement Servers Element, which holds all server nodes
     * @param ip IP of new server node
     * @param port Port of new server node
     */
    private static void addServerToElement(Element serversElement, String ip, String port) {
        Element singleServer = serversElement.addElement(SERVER_ELEMENT_NAME);
        singleServer.addAttribute(IP_ATTRIBUTE_NAME, ip);
        singleServer.addAttribute(PORT_ATTRIBUTE_NAME, port);
    }

    /**
     * Prepares a default document with the main server and currentSince set to the current time.
     * @return
     */
    private static Document getDefaultConfigDocument() {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement(ROOT_ELEMENT_NAME);
        Element servers = root.addElement(SERVERS_ELEMENT_NAME);
        Element since = root.addElement(SINCE_ELEMENT_NAME);

        addServerToElement(servers, "193.196.7.76", "8080");
        since.setText(getCurrentUTCTimeAsISO());

        return document;
    }


    private static String getCurrentUTCTimeAsISO() {
        return getCurrentUTCTime().toString(ISODateTimeFormat.dateTime());
    }

    /**
     * Currently returns the user pcs current time as UTC ISO8601, which can lead to issues.
     * SHOULD return the UTC time of the CouchDB server in future releases, which use the same NTP Node.
     * Alternatively, use NTP Client to get NTP time instead of CouchDB server time.
     * @return ISO8601 Formatted DateTime String UTC Time Zone (e.g. 2015-04-23T14:16:14.964Z )
     */
    public static DateTime getCurrentUTCTime(){
        return new DateTime(DateTimeZone.UTC);
    }

    public static boolean checkIfConfigExists() {
        File f = new File(CONFIG_FILE_NAME);
        return f.exists() && !f.isDirectory();
    }

    public static void writeDefaultConfig() throws IOException {

        Document document = getDefaultConfigDocument();
        XMLWriter writer = new XMLWriter(
                new FileWriter(CONFIG_FILE_NAME)
        );
        writer.write(document);
        writer.close();

    }


}

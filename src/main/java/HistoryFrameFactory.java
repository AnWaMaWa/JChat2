import couchdb.DBClientWrapper;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by awaigand on 22.04.2015.
 */
public class HistoryFrameFactory {
    DBClientWrapper dbc;
    DateTimeFormatter toJsonGMT;
    public HistoryFrameFactory(DBClientWrapper dbc){
        this.dbc = dbc;
        toJsonGMT = ISODateTimeFormat.dateTime();
    }

    public HistoryFrame buildHistoryWindow(String jsonTimeSince){
        HistoryFrame historyFrame = new HistoryFrame("History since "+jsonTimeSince, dbc, jsonTimeSince);
        historyFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        historyFrame.pack();
        return historyFrame;
    }


    public HistoryFrame buildHistoryWindow(DateTime AnyZone){

        return buildHistoryWindow(toJsonGMT.print(AnyZone));
    }
}

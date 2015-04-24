import couchdb.DBClientWrapper;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import javax.swing.*;

/**
 * Created by awaigand on 22.04.2015.
 */
public class HistoryFrameFactory {
    DBClientWrapper dbc;
    DateTimeFormatter toJsonGMT;

    public HistoryFrameFactory(DBClientWrapper dbc) {
        this.dbc = dbc;
        toJsonGMT = ISODateTimeFormat.dateTime();
    }

    public HistoryFrame buildHistoryWindow(String jsonTimeSince) {
        HistoryFrame historyFrame = new HistoryFrame("History since " + jsonTimeSince, dbc, jsonTimeSince);
        historyFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        historyFrame.pack();
        return historyFrame;
    }


    public HistoryFrame buildHistoryFrame(DateTime AnyZone) {

        return buildHistoryWindow(toJsonGMT.print(AnyZone));
    }
}

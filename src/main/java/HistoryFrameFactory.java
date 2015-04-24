import couchdb.DBClientWrapper;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import javax.swing.*;

/**
 * Factory class, based on GOF Factory Pattern, for creating HistoryFrames, which are used to display history.
 * The frames themselves use the DBClientWrapper to get the history.
 * Created by awaigand on 22.04.2015.
 */
public class HistoryFrameFactory {
    DBClientWrapper dbc;
    DateTimeFormatter toJsonGMT = ISODateTimeFormat.dateTime();

    public HistoryFrameFactory(DBClientWrapper dbc) {
        this.dbc = dbc;
    }

    /**
     * Returns an HistoryFrame which displays the message history since the time represented by the jsonTimeSince string.
     * @param jsonTimeSince Time String in ISO 8601 Format in UTC
     * @return A History Frame displaying message history
     */
    public HistoryFrame buildHistoryFrame(String jsonTimeSince) {
        HistoryFrame historyFrame = new HistoryFrame("History since " + jsonTimeSince, dbc, jsonTimeSince);
        historyFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        historyFrame.pack();
        return historyFrame;
    }

    /**
     * This function can be called with joda-time DateTime in any format and timezone, which is converted
     * to ISO 8601 in UTC
     * @param anyZoneAndFormat a joda-time Date Time which can be in any zone or format
     * @return
     */
    public HistoryFrame buildHistoryFrame(DateTime anyZoneAndFormat) {
        return buildHistoryFrame(toJsonGMT.print(anyZoneAndFormat));
    }
}

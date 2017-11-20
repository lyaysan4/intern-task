package csv;

import com.google.common.collect.Collections2;
import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvCustomBindByPosition;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class Output {

    @CsvCustomBindByPosition(converter = UnixTimeBeanField.class, position = 0)
    private Instant unixTime;

    @CsvBindByPosition(position = 1)
    private String idUser;

    @CsvBindByPosition(position = 2)
    private String url;

    @CsvBindByPosition(position = 3)
    private Long time;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy").withLocale(Locale.ENGLISH).withZone(ZoneId.of("+0"));

    public String getShortDate() {
        return formatter.format(unixTime);
    }

    public void setUnixTime(Instant unixTime) {
        this.unixTime = unixTime;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Output() {
    }

    public Output(Instant unixTime, String idUser, String url, Long time) {
        this.unixTime = unixTime;
        this.idUser = idUser;
        this.url = url;
        this.time = time;
    }

    public List<Output> divideByDays() {
        DateTimeZone tz = DateTimeZone.forID("GMT");
        DateTime firstTime = new DateTime(unixTime.getEpochSecond() * 1000).withZone(tz);
        DateTime secondTime = new DateTime((unixTime.getEpochSecond() + time) * 1000).withZone(tz);
        List<Output> dividedOutputs = new ArrayList<>();

        if (!firstTime.minusMillis(firstTime.getMillisOfDay()).equals(secondTime.minus(secondTime.getMillisOfDay()))) {
            long firstInterval = 24 * 60 * 60L - firstTime.getSecondOfDay();
            dividedOutputs.add(new Output(unixTime, idUser, url, firstInterval));
            int days = (int) (time - (24 * 60 * 60 - firstTime.getSecondOfDay())) / (24 * 60 * 60);
            for (int i = 0; i < days; i++) {
                dividedOutputs.add(new Output(unixTime.plusSeconds(firstInterval + i * 24 * 60 * 60L), idUser, url, 24 * 60 * 60L));
            }
            dividedOutputs.add(new Output(unixTime.plusSeconds(firstInterval + days * 24 * 60 * 60), idUser, url, (long) secondTime.getSecondOfDay()));
        } else
            dividedOutputs.add(new Output(unixTime, idUser, url, time));

        return dividedOutputs;
    }

    public static Output avgTime(Output output, Collection<Output> list) {
        double asDouble = list.stream().mapToLong(out -> out.time).average().orElse(0);
        return new Output(null, output.idUser, output.url, (long) asDouble);
    }

    @Override
    public String toString() {
        return idUser + "," + url + "," + time;
    }

    public Collection<Output> getSame(List<Output> outputs) {
        return Collections2.filter(outputs, this::equals);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Output output = (Output) o;

        return idUser.equals(output.idUser) && url.equals(output.url);
    }

    @Override
    public int hashCode() {
        int result = idUser.hashCode();
        result = 31 * result + url.hashCode();
        return result;
    }
}

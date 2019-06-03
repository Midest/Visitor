package me.midest.model.time;

import me.midest.model.TimeInterval;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.*;

public class TimePeriodTest {

    @Test
    public void containsTest(){
        TimePeriod tp = new TimePeriod();
        assertFalse( tp.contains( LocalDate.now(), TimeInterval.create( "00:00-23:59" ) ) );
        tp.addPeriodical( Periodical.create( "10:00-12:00", "10.02.18", "05.02.18-20.02.18", PeriodUnit.DAYS, 2 ));
        assertFalse( tp.contains( LocalDate.of( 2018, 2, 11 ), TimeInterval.create( "10:00-12:00" )));
        assertTrue( tp.contains( LocalDate.of( 2018, 2, 12 ), TimeInterval.create( "10:00-12:00" )));
        assertTrue( tp.contains( LocalDate.of( 2018, 2, 12 ), TimeInterval.create( "09:00-11:00" )));
        assertFalse( tp.contains( LocalDate.of( 2018, 2, 12 ), TimeInterval.create( "08:00-09:00" )));
        tp.addPeriodical( Periodical.create( "08:00-12:00", "10.02.18", "05.02.18-20.02.18", PeriodUnit.DAYS, 2 ));
        assertTrue( tp.contains( LocalDate.of( 2018, 2, 12 ), TimeInterval.create( "08:00-09:00" )));

        tp.clear();
        assertFalse( tp.contains( LocalDate.of( 2018, 2, 12 ), TimeInterval.create( "09:00-11:00" )));
        tp.addInterval( Interval.create( "10:00-12:00", "02.01.19-04.05.19" ) );
        assertFalse( tp.contains( LocalDate.of( 2018, 2, 12 ), TimeInterval.create( "09:00-11:00" )));
        assertFalse( tp.contains( LocalDate.of( 2019, 4, 12 ), TimeInterval.create( "09:00-09:30" )));
        assertTrue( tp.contains( LocalDate.of( 2019, 4, 12 ), TimeInterval.create( "09:00-10:30" )));
    }

}

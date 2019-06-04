package me.midest.model.time;

import me.midest.model.TimeInterval;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.*;

public class IntervalTest {

    @Test
    public void anyDay(){
        Interval i = Interval.create( "10:00-12:00", null );
        assertTrue( i.contains( LocalDate.now(), TimeInterval.create( "09:00-11:00" )));
        assertTrue( i.contains( LocalDate.now(), TimeInterval.create( "11:00-12:00" )));
        assertFalse( i.contains( LocalDate.now(), TimeInterval.create( "09:00-10:00" )));
    }

}

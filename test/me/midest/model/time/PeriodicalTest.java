package me.midest.model.time;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class PeriodicalTest {

    @Test
    public void _stringRepresentation(){
        Periodical i = new Periodical();
        System.out.println( i );
        i.setDateFrom( LocalDate.now());
        System.out.println( i );
        i.setDateTo( LocalDate.now().minus( 5, ChronoUnit.DAYS ) );
        System.out.println( i );
        i.setStart( LocalTime.now() );
        System.out.println( i );
        i.setFirst( LocalDate.now().minus( 3, ChronoUnit.WEEKS ));
        System.out.println( i );
        i.setFirst( LocalDate.now().minus( 3, ChronoUnit.DAYS ));
        System.out.println( i );
        i.setDuration( 4 );
        System.out.println( i );
        i.setUnit( PeriodUnit.DAYS );
        System.out.println( i );
    }

}

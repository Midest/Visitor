package me.midest.logic.coupling;

import me.midest.model.Lesson;
import me.midest.model.TimeInterval;
import me.midest.model.Tutor;
import me.midest.model.Visit;
import org.junit.Test;
import static org.junit.Assert.*;

import java.time.LocalDate;
import java.util.Arrays;

public class VisitsValueTest {
    private TimeInterval ti = TimeInterval.create( "09:00-10:40" );

    @Test
    public void simpleTest(){
        Tutor t1 = new Tutor( "Иванов И.И." );
        Tutor t2 = new Tutor( "Петров И.И." );
        Lesson l1 = new Lesson( "Математика ПЗ-6, а.906 НМ-59", Lesson.SourceType.TUTOR );
        Lesson l2 = new Lesson( "Алгебра лекция-1, а.905 НМ-43", Lesson.SourceType.TUTOR );
        l1.setTutor( t1 );
        l2.setTutor( t2 );
        l1.setDate( LocalDate.of( 2018, 9,3 ) );
        l2.setDate( LocalDate.of( 2018, 12,25 ) );
        l1.setTime( ti );
        l2.setTime( ti );
        Visit v1 = new Visit( t2, l1 );
        Visit v2 = new Visit( t1, l2 );
        double maxL = 708.0;

        double value = VisitsValue.count( Arrays.asList( v1, v2 ));
        assertEquals( maxL, value, 0.01 );
    }

}

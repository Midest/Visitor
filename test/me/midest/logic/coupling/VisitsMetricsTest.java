package me.midest.logic.coupling;

import me.midest.model.*;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.Assert.*;

public class VisitsMetricsTest {
    private TimeInterval ti = TimeInterval.create( "09:00-10:40" );
    private Visit v21,v31,v32,v23,v23_2;
    private Long v21_31, v21_23, v21_32,v23_23_2;
    @Before
    public void setUp(){
        Tutor t1 = new Tutor( "Иванов И.И." );
        Tutor t2 = new Tutor( "Петров И.И." );
        Tutor t3 = new Tutor( "Сидоров И.И." );
        Lesson l1 = new Lesson( "Математика ПЗ-6, а.906 НМ-59", Lesson.SourceType.TUTOR );
        Lesson l2 = new Lesson( "Алгебра лекция-1, а.905 НМ-43", Lesson.SourceType.TUTOR );
        Lesson l3 = new Lesson( "Геометрия сем.-6, а.904 НМ-22", Lesson.SourceType.TUTOR );
        Lesson l3_2 = new Lesson( "Геометрия ПЗ-6, а.904 НМ-22", Lesson.SourceType.TUTOR );
        l1.setTutor( t1 );
        l2.setTutor( t2 );
        l3.setTutor( t3 );
        l3_2.setTutor( t3 );
        l1.setDate( LocalDate.of( 2018, 5,1 ) );
        l2.setDate( LocalDate.of( 2018, 5,2 ) );
        l3.setDate( LocalDate.of( 2018, 5,4 ) );
        l3_2.setDate( LocalDate.of( 2018, 5,4 ) );
        l1.setTime( ti );
        l2.setTime( ti );
        l3.setTime( ti );
        l3_2.setTime( ti );
        v21 = new Visit( t2, l1 );
        v31 = new Visit( t3, l1 );
        v32 = new Visit( t3, l2 );
        v23 = new Visit( t2, l3 );
        v23_2 = new Visit( t2, l3_2 );
        v21_31 = Long.valueOf( VISITOR_WEIGHT );
        v21_23 = Long.valueOf( LESSON_TYPE_WEIGHT + DISCIPLINE_WEIGHT + TUTOR_WEIGHT + GROUP_WEIGHT + DATE_WEIGHT_MULTIPLIER * 3 );
        v21_32 = Long.valueOf( VISITOR_WEIGHT + LESSON_TYPE_WEIGHT + DISCIPLINE_WEIGHT + TUTOR_WEIGHT + GROUP_WEIGHT + DATE_WEIGHT_MULTIPLIER * 1 );
        v23_23_2 = Long.valueOf( LESSON_TYPE_WEIGHT );
    }

    @Test
    public void valuesTest(){
        assertEquals( v21_31, VisitsMetrics.evaluate( v21, v31 ));
        assertEquals( v21_23, VisitsMetrics.evaluate( v21, v23 ));
        assertEquals( v21_32, VisitsMetrics.evaluate( v21, v32 ));
        assertEquals( v23_23_2, VisitsMetrics.evaluate( v23, v23_2 ));
    }

    @Test
    public void leastTest(){
        assertEquals( v23_23_2, VisitsMetrics.evaluateLeast( v23_2, Arrays.asList( v21, v31, v32, v23 ) ) );
    }


    private static final Long SECONDS_PER_DAY = 24*60*60L;

    private static final Integer TUTOR_WEIGHT = 500;
    private static final Integer DISCIPLINE_WEIGHT = 50;
    private static final Integer LESSON_TYPE_WEIGHT = 25;
    private static final Integer VISITOR_WEIGHT = 15;
    private static final Integer GROUP_WEIGHT = 5;
    private static final Integer DATE_WEIGHT_MULTIPLIER = 1;
}

package me.midest.logic.coupling;

import me.midest.model.Tutor;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class PeriodCouplingTest {

    PeriodCoupling coupler;

    @Before
    public void setUp(){
        coupler = new PeriodCoupling();

        // Внешние аудитории
        Set<String> outerRooms = new HashSet<>();
        //outerRooms.add( "филиал" );

        // Ненужные типы занятий
        Set<String> unwantedTypes = new HashSet<>();
        unwantedTypes.add( "конт.раб" );
        unwantedTypes.add( "зачет" );
        unwantedTypes.add( "зач._оц" );
        unwantedTypes.add( "экзамен" );
        unwantedTypes.add( "экз." );
        unwantedTypes.add( "вх.кнтрль" );
        unwantedTypes.add( "входной контроль" );

        // Проверяющие
        Set<Tutor> visitors = new HashSet<>();

        // Посещаемые
        Set<Tutor> toVisit = new HashSet<>();
        toVisit.addAll( visitors );

        // Лишние
        Set<Tutor> outerTutors = new HashSet<>();


        coupler.setOuterRooms( outerRooms );
        coupler.setOuterTutors( outerTutors );
        coupler.setPossibleVisitors( visitors );
        coupler.setToVisit( toVisit );
        coupler.setUnwantedTypes( unwantedTypes );
    }

    @Test
    public void testReader(){
        
    }

}

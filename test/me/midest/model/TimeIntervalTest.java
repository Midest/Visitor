package me.midest.model;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalTime;

import static org.junit.Assert.*;

public class TimeIntervalTest {

    private TimeInterval ti, tiWO,
            tiCovering, tiCovered, tiLCovering, tiRCovering,
            tiLEquality, tiREquality, tiEquality,
            tiLTangency, tiRTangency, tiLeft, tiRight;

    @Before
    public void setUp(){
        ti = TimeInterval.create( "10:00-15:00" );
        tiWO = TimeInterval.create( "15:00-10:00" ); // wrong order;
        tiCovering = TimeInterval.create( "09:30-15:30" ); // covering
        tiCovered = TimeInterval.create( "10:30-14:30" ); // covered
        tiLCovering = TimeInterval.create( "09:30-14:30" ); // left covering
        tiRCovering = TimeInterval.create( "10:30-15:30" ); // right covering
        tiLEquality = TimeInterval.create( "10:00-15:30" ); // left equality
        tiREquality = TimeInterval.create( "09:30-15:00" ); // right equality
        tiEquality = TimeInterval.create( "10:00-15:00" ); // equality
        tiLTangency = TimeInterval.create( "09:00-10:00" ); // left tangency
        tiRTangency = TimeInterval.create( "15:00-16:00" ); // right tangency
        tiLeft = TimeInterval.create( "09:00-09:30" ); // left
        tiRight = TimeInterval.create( "15:30-16:00" ); // right
    }

    @Test
    public void creationTest(){
        assertEquals( LocalTime.of( 10,0 ), ti.getStart() );
        assertEquals( LocalTime.of( 15,0 ), ti.getEnd() );

        assertEquals( LocalTime.of( 10,0 ), tiWO.getStart() );
        assertEquals( LocalTime.of( 15,0 ), tiWO.getEnd() );
    }

    @Test
    public void overlapTest(){
        assertTrue( ti.overlaps( tiWO ));
        assertTrue( tiWO.overlaps( ti ));
        assertTrue( ti.overlaps( tiCovering ));
        assertTrue( tiCovering.overlaps( ti ));
        assertTrue( ti.overlaps( tiCovered ));
        assertTrue( tiCovered.overlaps( ti ));
        assertTrue( ti.overlaps( tiLCovering ));
        assertTrue( tiLCovering.overlaps( ti ));
        assertTrue( ti.overlaps( tiRCovering ));
        assertTrue( tiRCovering.overlaps( ti ));
        assertTrue( ti.overlaps( tiLEquality ));
        assertTrue( tiLEquality.overlaps( ti ));
        assertTrue( ti.overlaps( tiREquality ));
        assertTrue( tiREquality.overlaps( ti ));
        assertTrue( ti.overlaps( tiEquality ));
        assertTrue( tiEquality.overlaps( ti ));

        // No overlap
        assertFalse( ti.overlaps( tiLTangency ));
        assertFalse( tiLTangency.overlaps( ti ));
        assertFalse( ti.overlaps( tiRTangency ));
        assertFalse( tiRTangency.overlaps( ti ));
        assertFalse( ti.overlaps( tiLeft ));
        assertFalse( tiLeft.overlaps( ti ));
        assertFalse( ti.overlaps( tiRight ));
        assertFalse( tiRight.overlaps( ti ));
    }

    @Test
    public void equalityTest(){
        // Strict
        assertEquals( ti, tiWO );
        assertTrue( ti.strictEquals( tiWO ) );
        assertTrue( tiWO.strictEquals( ti ) );
        assertEquals( ti, tiEquality );
        assertTrue( ti.strictEquals( tiEquality ) );
        assertTrue( tiEquality.strictEquals( ti ) );

        // Not strict
        assertEquals( ti, tiCovering );
        assertFalse( ti.strictEquals( tiCovering ) );
        assertFalse( tiCovering.strictEquals( ti ) );
        assertEquals( ti, tiCovered );
        assertFalse( ti.strictEquals( tiCovered ) );
        assertFalse( tiCovered.strictEquals( ti ) );
        assertEquals( ti, tiLCovering );
        assertFalse( ti.strictEquals( tiLCovering ) );
        assertFalse( tiLCovering.strictEquals( ti ) );
        assertEquals( ti, tiRCovering );
        assertFalse( ti.strictEquals( tiRCovering ) );
        assertFalse( tiRCovering.strictEquals( ti ) );
        assertEquals( ti, tiLEquality );
        assertFalse( ti.strictEquals( tiLEquality ) );
        assertFalse( tiLEquality.strictEquals( ti ) );
        assertEquals( ti, tiREquality );
        assertFalse( ti.strictEquals( tiREquality ) );
        assertFalse( tiREquality.strictEquals( ti ) );

        // Not equals
        assertNotEquals( ti, tiLTangency );
        assertNotEquals( ti, tiRTangency );
        assertNotEquals( ti, tiLeft );
        assertNotEquals( ti, tiRight );
    }

    @Test
    public void hashTest(){
        assertEquals( ti.hashCode(), tiEquality.hashCode() );
        assertEquals( ti, tiEquality );
        assertTrue( ti.strictEquals( tiEquality ) );

        // overlap equality
        assertEquals( ti.hashCode(), tiRCovering.hashCode() );
        assertEquals( ti, tiRCovering );
        assertFalse( ti.strictEquals( tiRCovering ) );
    }

}

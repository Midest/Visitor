package me.midest.model;

import org.junit.Test;
import static org.junit.Assert.*;

public class TutorTest {

    @Test
    public void equality(){
        Tutor t1 = new Tutor("Иванов Иван Иваныч");
        Tutor t2 = new Tutor("Иванов Иван Иваныч");
        assertEquals( t1, t2 );
    }

}

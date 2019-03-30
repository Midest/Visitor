package me.midest.logic.files;

import me.midest.model.FixedVisit;
import me.midest.model.TimeInterval;
import me.midest.model.Tutor;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class TxtReader {

    public static List<Tutor> readTutors( String fileName ){
        return readLines( fileName, Tutor.class );
    }

    public static List<FixedVisit> readFixedVisits( String fileName ){
        return readLines( fileName, FixedVisit.class );
    }

    private static <T> List<T> readLines( String fileName, Class<T> clazz ){
        List<T> list = new ArrayList<>();
        try (Stream<String> stream = Files.lines( Paths.get(fileName), Charset.forName("utf8"))) {
            stream.forEach( line -> list.add( processLine( line, clazz )));
        } catch ( Exception e ){
            e.printStackTrace();
        }
        return list;
    }

    private static <T> T processLine( String line, Class<T> clazz ){
        if( clazz.equals( Tutor.class )) {
            String[] l = line.split( "\\|" );
            Tutor t = new Tutor( l[0], Tutor.Status.byName( l[1] ), l[2].equals( "1" ), l[3].equals( "1" ) );
            if( l.length > 4 )
                t.setDepartment( l[5] );
            if( l.length > 5 )
                t.setRegalia( l[5] );
            return (T) t;
        }
        else if( clazz.equals( FixedVisit.class )) {
            String[] l = line.split( "," );
            Tutor t = new Tutor( l[0] );
            Tutor v = new Tutor( l[1] );
            TimeInterval ti = new TimeInterval( l[2] );
            LocalDate date = LocalDate.parse( l[3] );
            FixedVisit visit = new FixedVisit( t, v, ti, date );
            return (T) visit;
        }
        return null;
    }

}

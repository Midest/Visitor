package me.midest.logic.files;

import me.midest.model.FixedVisit;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class TxtWriter {

    public static void writeFixedVisits( String fileName, List<FixedVisit> list ){
        Path path = Paths.get( fileName );
        try ( final BufferedWriter writer = Files.newBufferedWriter(path)) {
            for( FixedVisit v : list )
                writer.write( toLineString( v ) );
        } catch( Exception e ) {
            e.printStackTrace();
        }
    }

    private static String toLineString( FixedVisit v ) {
        return v.getTutor().getName() + ',' +
                v.getVisitor().getName() + ',' +
                v.getTime() + ',' +
                v.getDate() + "\r\n";
    }

}

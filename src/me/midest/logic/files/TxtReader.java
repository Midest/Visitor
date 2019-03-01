package me.midest.logic.files;

import me.midest.model.Tutor;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class TxtReader {

    public static List<Tutor> readTutors(String fileName ){
        List<Tutor> list = new ArrayList<>();
        try (Stream<String> stream = Files.lines( Paths.get(fileName), Charset.forName("utf8"))) {
            stream.forEach( line -> {
                String[] l = line.split( "\\|" );
                list.add( new Tutor( l[0], Tutor.Status.byName( l[1] ), l[2].equals( "1" ), l[3].equals( "1" )));
            });
        } catch ( Exception e ){
            e.printStackTrace();
        }
        return list;
    }

}

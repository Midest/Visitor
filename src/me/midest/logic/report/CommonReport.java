package me.midest.logic.report;

import me.midest.model.Lesson;
import me.midest.model.SheetStructure;
import me.midest.model.SimplePair;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonReport implements Report {

    /**
     * @param lessons пары, из которых нужно выбирать
     * @param paramName название параметра
     * @param params регулярные выражения для параметра
     * @return
     */
    @Override
    public HSSFWorkbook generate( HSSFWorkbook template, Collection<Lesson> lessons, ParamTypes paramName, String... params ) {
        SheetStructure baseStructure = new SheetStructure( template.getSheetAt( 0 ));

        Set<String> names = new HashSet<>();
        Map<String, Set<Lesson>> resultLessons = new HashMap<>();
        Map<String, Set<LocalDate>> resultDates = new HashMap<>();
        String name;
        // Собираем списки
        for( String regexp : params ){
            Pattern p = Pattern.compile( regexp );
            for( Lesson l : lessons ){
                switch( paramName ){
                    case DISCIPLINE: name = l.getDiscipline(); break;
                    case GROUP: name = l.getGroup(); break;
                    case ROOM: name = l.getRoom(); break;
                    case DATE: name = l.getDate().toString(); break;
                    default: name = null;
                }
                Sheet s = null;
                if( !names.contains( name )){
                    Matcher m = p.matcher( name );
                    if( m.matches()){
                        s = template.cloneSheet( 0 );
                        template.setSheetName( template.getSheetIndex( s ), name );
                        names.add( name );
                    }
                }
                else{
                    s = template.getSheet( name );
                }
                if( s != null ){
                    resultLessons.putIfAbsent( name, new HashSet<>() );
                    resultLessons.get( name ).add( l );
                    resultDates.putIfAbsent( name, new HashSet<>() );
                    resultDates.get( name ).add( l.getDate());
                    System.out.println( l );
                }
            }
        }
        // Заполняем заголовки
        for( int i = 0; i < template.getNumberOfSheets(); i++ ){
            Sheet s = template.getSheetAt( i );
            name = s.getSheetName();
            if( !resultLessons.containsKey( name ))
                continue;
            Lesson l = resultLessons.get( name ).iterator().next();
            if( paramName == ParamTypes.GROUP ) {
                s.getHeader().setCenter( s.getHeader().getCenter().replaceAll( "группы", "группы " + name ).replaceAll( "2016/17", getYears( l.getDate())));
                s.getFooter().setLeft( s.getFooter().getLeft().replaceAll( "Группа", "Группа " + name ) );
            }
        }
        // Заполняем расписание
        for( String n : names ){
            Sheet s = template.getSheet( n );
            SheetStructure sheetStructure = baseStructure.clone();
            List<LocalDate> dates = new ArrayList<>( resultDates.get( n ));
            sheetStructure.fillDateRows( dates );
            for( LocalDate date : dates ) {
                SimplePair<Integer, Integer> rowCol = sheetStructure.getDateAddress( date );
                Cell cell = s.getRow( rowCol.getFirst() ).getCell( rowCol.getSecond() );
                cell.setCellValue( date.format( dtf ) ); // TODO применение excel-форматирования к результату
            }
            for( Lesson lesson : resultLessons.get( n )){
                SimplePair<Integer, Integer> rowCol = sheetStructure.getAddress( lesson );
                Cell cell = s.getRow( rowCol.getFirst() ).getCell( rowCol.getSecond() );
                cell.setCellValue( formattedLessonString( lesson, paramName ));
            }


        }
        System.out.println( names );
        if( template.getNumberOfSheets() > 1 )
            template.removeSheetAt( 0 );
        return template;
    }

    private String formattedLessonString( Lesson l, ParamTypes sheetType ){
        switch( sheetType ){
            case DISCIPLINE: return l.getGroup() + "\n" + l.getName() + "," + l.getRoom() + "\n" + l.getTutor();
            case ROOM: return l.getDiscipline() + "\n" + l.getName() + "," + l.getGroup() + "\n" + l.getTutor();
            case GROUP: return l.getDiscipline() + "\n" + l.getName() + "," + l.getRoom() + "\n" + l.getTutor();
            default: return l.getDiscipline() + "\n" + l.getGroup() + "," + l.getRoom() + "\n" + l.getTutor();
        }
    }

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern( "dd.MM.yyyy" );
    private static final int mil = 2000;
    private String getYears( LocalDate date ) {
        String years;
        if( date.getMonth().compareTo( Month.AUGUST ) == 1 )
            years = date.getYear() + "/" + ( date.getYear() + 1 - mil );
        else
            years = ( date.getYear() - 1 ) + "/" + ( date.getYear() - mil );
        return years;
    }

}

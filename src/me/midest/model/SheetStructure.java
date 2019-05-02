package me.midest.model;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

public class SheetStructure {
    private Map<DayOfWeek, Set<Integer>> dayOfWeekRows;
    private Map<TimeInterval, Set<Integer>> timeIntervalRows;
    private Map<LocalDate, Integer> dateCols;
    private Integer firstLessonColumnIndex = 0;
    private Sheet sheet;

    public SheetStructure( Sheet sheet ){
        this.sheet = sheet;
        createMaps();
        processSheet( sheet );
    }

    public SimplePair<Integer, Integer> getAddress( Lesson lesson ){
        Set<Integer> rows = new HashSet<>( dayOfWeekRows.get( lesson.getDate().getDayOfWeek() ));
        int row = -1;
        for( int index : timeIntervalRows.get( lesson.getTime() )){
            if( rows.contains( index )) {
                row = index;
                break;
            }
        }
        if( row == -1 )
            throw new RuntimeException( "Неверный шаблон" );
        int col = dateCols.get( lesson.getDate() );
        return new SimplePair<>( row, col );
    }

    public SimplePair<Integer, Integer> getDateAddress( LocalDate date ){
        Set<Integer> rows = new HashSet<>( dayOfWeekRows.get( date.getDayOfWeek() ));
        for( TimeInterval ti : timeIntervalRows.keySet()) if( ti != TimeInterval.EMPTY )
            rows.removeAll( timeIntervalRows.get( ti ));
        if( rows.size() != 1 )
            throw new RuntimeException( "Неверный шаблон" );
        int row = rows.iterator().next();
        int col = dateCols.get( date );
        return new SimplePair<>( row, col );
    }

    public void fillDateRows( List<LocalDate> dates ){
        Collections.sort( dates );
        LocalDate first = dates.get( 0 );
        for( LocalDate date : dates ){
            int colIndex = dateColIndex( first, date );
            dateCols.put( date, colIndex );
        }
    }
    private static final Long SECONDS_PER_DAY = 24*60*60L;

    private int dateColIndex( LocalDate first, LocalDate date ){
        long days = Duration.between( first.atStartOfDay(), date.atStartOfDay() )
                .abs().getSeconds() / SECONDS_PER_DAY;
        int wholeWeeks = (int)(days/7);
        int shift = first.getDayOfWeek().getValue() + days % 7 > 7 ? 1 : 0;
        return firstLessonColumnIndex + wholeWeeks + shift;
    }

    @Override
    public SheetStructure clone(){
        SheetStructure structure = new SheetStructure( sheet );
        if( !this.dateCols.isEmpty())
            structure.dateCols.putAll( this.dateCols );
        return structure;
    }

    private void createMaps() {
        dayOfWeekRows = new HashMap<>();
        for( DayOfWeek dow : DayOfWeek.values())
            dayOfWeekRows.put( dow, new HashSet<Integer>());
        timeIntervalRows = new HashMap<>();
        timeIntervalRows.put( TimeInterval.EMPTY, new HashSet<Integer>());
        dateCols = new HashMap<>();
    }

    /**
     * Правила составления шаблона для корректной обработки:
     * <ul>
     * <li>Указан день недели, время начала-конца пар.</li>
     * <li>На каждый день недели одна пустая ячейка в столбце времени</li>
     * </ul>
     * @param sheet
     */
    private void processSheet( Sheet sheet ) {
        Iterator<Row> rowIterator = sheet.rowIterator();
        Set<Integer> timeRows = new HashSet<>();
        Integer dowColIndex = -1;
        DayOfWeek currentDOW = null;
        while( rowIterator.hasNext()){
            Row row = rowIterator.next();
            Iterator<Cell> cellIterator = row.cellIterator();
            while( cellIterator.hasNext()){
                Cell cell = cellIterator.next();
                String value = cell.getStringCellValue().trim().toLowerCase();
                int colIndex = cell.getColumnIndex();
                if( daysOfWeek.containsKey( value )) {
                    if( dowColIndex == -1 ) {
                        dowColIndex = cell.getColumnIndex();
                    }
                    currentDOW = daysOfWeek.get( value );
                    if( colIndex >= firstLessonColumnIndex )
                        firstLessonColumnIndex = colIndex + 1;
                }
                else try{
                    TimeInterval ti = TimeInterval.create( value );
                    timeIntervalRows.putIfAbsent( ti, new HashSet<Integer>() );
                    timeIntervalRows.get( ti ).add( row.getRowNum());
                    timeRows.add( row.getRowNum());
                    if( colIndex >= firstLessonColumnIndex )
                        firstLessonColumnIndex = colIndex + 1;
                } catch( ArrayIndexOutOfBoundsException e ){}
            }
            if( currentDOW != null ) {
                dayOfWeekRows.get( currentDOW ).add( row.getRowNum() );
                if( timeRows.add( row.getRowNum() ))
                    timeIntervalRows.get( TimeInterval.EMPTY ).add( row.getRowNum());
            }
        }
    }

    private static Map<String, DayOfWeek> daysOfWeek = new HashMap<>();
    static {
        daysOfWeek.put( "понедельник", DayOfWeek.MONDAY );
        daysOfWeek.put( "вторник", DayOfWeek.TUESDAY );
        daysOfWeek.put( "среда", DayOfWeek.WEDNESDAY );
        daysOfWeek.put( "четверг", DayOfWeek.THURSDAY );
        daysOfWeek.put( "пятница", DayOfWeek.FRIDAY );
        daysOfWeek.put( "суббота", DayOfWeek.SATURDAY );
        daysOfWeek.put( "воскресенье", DayOfWeek.SUNDAY );
    }
}

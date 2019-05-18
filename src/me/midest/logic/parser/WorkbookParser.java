package me.midest.logic.parser;

import me.midest.model.Lesson;
import me.midest.model.Lesson.SourceType;
import me.midest.model.Term;
import me.midest.model.TimeInterval;
import me.midest.model.Tutor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import java.time.LocalDate;
import java.util.*;

public class WorkbookParser {

    /** Вспомогательная карта привязки к столбцам дат. */
    private Map<Integer, LocalDate> currentDates;
    private TimeInterval currentPairTime;
    private Term result;
    private Integer year;
    private Set<Tutor> tutors;

    public WorkbookParser( String year ){
        this.year = Integer.valueOf( year );
        result = new Term();
        currentDates = new HashMap<>();
        tutors = new HashSet<>();
    }

    public void parse( Workbook wb, SourceType sourceType ) throws Exception {
        for( int i = 0; i < wb.getNumberOfSheets(); i++ ){
            result.putLessons( parseSheet( wb.getSheetAt( i ), sourceType ));
        }
    }

    public Term getResult() {
        return result;
    }

    /**
     * Получение всех пар на листе.
     * Примечание: столбец B содержит время пары, со столбца
     * C начинаются перечисления дат и пар.
     * @param sheet лист
     * @return список всех пар на листе
     */
    private List<Lesson> parseSheet( Sheet sheet, SourceType sourceType ) throws Exception {
        List<Lesson> allLessons = new ArrayList<>();
        //TODO разбить сцепленные ячейки (кроме 1 столбца), скопировав текст во все
        int mergedCount = sheet.getNumMergedRegions();
        Map<CellRangeAddress, String> ranges = new HashMap<>();
        // Записываем все области объединенных ячеек
        for( int i = 0; i < mergedCount; i++ ){
            CellRangeAddress mergedRange = sheet.getMergedRegion( i );
            if( mergedRange.getFirstColumn() < getExcelColumnIndex( "C" )) continue;
            ranges.put( mergedRange, null );
        }
        // Удаляем с листа все области объединенных ячеек
        for( int i = mergedCount-1; i >= 0; i-- ) {
            if( sheet.getMergedRegion( i ).getFirstColumn() < getExcelColumnIndex( "C" )) continue;
            sheet.removeMergedRegion( i );
        }

        Iterator<Row> rowIterator = sheet.rowIterator();
        Cell cell = null;
        try {
            while( rowIterator.hasNext() ) {
                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();
                // Первые даты
                if( currentDates.size() == 0 ) while( cellIterator.hasNext() ) {
                    cell = cellIterator.next();
                    if( cell.getColumnIndex() > getExcelColumnIndex( "B" ) ) {
                        String cellValue = cell.getStringCellValue();
                        // Если непусто
                        if( cellValue != null && cellValue.length() > 0 ) try {
                            String[] datum = cellValue.split( "\\s" );
                            currentDates.put( cell.getColumnIndex(), LocalDate.of( year,
                                    months.get( datum[1].trim() ), Integer.valueOf( datum[0] ) ) );
                        } catch( Exception e ) {
                            currentDates.clear();
                        }
                    }
                }
                // Остальная часть листа
                else while( cellIterator.hasNext() ) {
                    cell = cellIterator.next();
                    if( cell.getColumnIndex() == getExcelColumnIndex( "A" ) ) {
                        continue;
                    } else if( cell.getColumnIndex() == getExcelColumnIndex( "B" ) ) {
                        String cellValue = cell.getStringCellValue();
                        if( cellValue != null && cellValue.length() > 0 ) try {
                            currentPairTime = TimeInterval.create( cellValue );
                        } catch( Exception e ) {
                            currentPairTime = null;
                        }
                        else {
                            currentPairTime = null;
                            currentDates.clear();
                        }
                    } else {
                        // Проверка значения объединенных ячеек
                        for( CellRangeAddress range : ranges.keySet() ) {
                            if( inRange( cell, range )){
                                if( ranges.get( range ) == null )
                                    ranges.put( range, cell.getStringCellValue());
                                else
                                    cell.setCellValue( ranges.get( range ));
                            }
                        }
                        // Значение
                        String cellValue = cell.getStringCellValue();
                        if( cellValue != null && cellValue.length() > 0 ) {
                            if( currentPairTime == null ) {
                                try {
                                    String[] datum = cellValue.split( "\\s" );
                                    currentDates.put( cell.getColumnIndex(), LocalDate.of( year,
                                            months.get( datum[1].trim() ), Integer.valueOf( datum[0] ) ) );
                                } catch( Exception e ) {
                                    currentDates.clear();
                                }
                            } else {
                                if( currentDates.get( cell.getColumnIndex() ) == null )
                                    throw new Exception();
                                Lesson lesson = new Lesson( cellValue, sourceType );
                                Tutor tutor;
                                String group;
                                switch( sourceType ) {
                                    case TUTOR: {
                                        tutor = new Tutor( sheet.getSheetName() );
                                        group = lesson.getGroup();
                                        break;
                                    }
                                    case GROUP: {
                                        tutor = lesson.getTutor();
                                        group = sheet.getSheetName();
                                        break;
                                    }
                                    default: {
                                        tutor = null;
                                        group = null;
                                    }
                                }
                                for( Tutor t : tutors )
                                    if( t.equals( tutor ) ) {
                                        tutor = t;
                                        break;
                                    }
                                lesson.setDate( currentDates.get( cell.getColumnIndex() ) );
                                lesson.setTime( currentPairTime );
                                lesson.setTutor( tutor );
                                lesson.setGroup( group );
                                allLessons.add( lesson );
                            }
                        }
                    }
                }
            }
        } catch( Exception e ){
            System.out.println( sheet.getSheetName() + ": "
                    + ( cell == null ? "" : ( cell.getRowIndex() + ";" + cell.getColumnIndex())) );
            throw e;
        }
        return allLessons;
    }

    /**
     * Проверка, что ячейка входит в диапазон.
     * @param cell ячейка
     * @param range диапазон
     * @return <code>true</code>, если ячейка входит в диапазон
     */
    private static boolean inRange( Cell cell, CellRangeAddress range ){
        int col = cell.getColumnIndex();
        int row = cell.getRowIndex();
        return col >= range.getFirstColumn() && col <= range.getLastColumn()
                && row >= range.getFirstRow() && row <= range.getLastRow();
    }

    private static final Map<String, Integer> months = new HashMap<>();
    static {
        months.put( "января", 1 ); months.put( "февраля", 2 ); months.put( "марта", 3 );
        months.put( "апреля", 4 ); months.put( "мая", 5 ); months.put( "июня", 6 );
        months.put( "июля", 7 ); months.put( "августа", 8 ); months.put( "сентября", 9 );
        months.put( "октября", 10 ); months.put( "ноября", 11 ); months.put( "декабря", 12 );
    }

    private static final char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final char[] numbers = "0123456789".toCharArray();

    /**Получение номера колонки документа MS Ecxel по ее имени.
     * @param name имя колонки.
     * @return <b>null</b>, если недопустимое имя ячейки.
     */
    public static int getExcelColumnIndex( String name ){
        // В закоментированном варианте нет проверки
        // на допустимость имени столбца.
        // return CellReference.convertColStringToIndex( name );
        int[] t = getExcelCellAddress( name + "1" );
        return ( t == null? -1 : t[0] ) ;
    }

    /**Получение адреса ячейки документа MS Excel по ее имени.
     * @param name имя ячейки.
     * @return Массив из двух значений — индексов столбца и строки;
     * <b>null</b>, если недопустимое имя ячейки.
     */
    public static int[] getExcelCellAddress( String name ) {
        char[] nameChars = name.toCharArray();
        int caret = name.length();
        CARET: for( int i = 0; i < nameChars.length; i++ ){
            for( int j = 0; j < numbers.length; j++ ){
                if( numbers[j] == nameChars[i] ){
                    caret = i;
                    break CARET;
                }
            }
        }
        if( caret > 2 || caret == name.length()){
            return null;
        }
        else if( caret == 2 ){
            if( nameChars[0] > 'I' ){
                return null;
            }
            else if( nameChars[0] == 'I' && nameChars[1] > 'V' ){
                return null;
            }
        }

        int indexX = 0;
        for( int i = 0; i < caret; i++ ){
            for( int j = 0; j < alphabet.length; j++ ){
                if( alphabet[j] == nameChars[i] ){
                    int pow = caret-i-1;
                    int plus = (j+1)*(int)Math.pow( alphabet.length, pow );
                    indexX+= plus; break;
                }
            }
        }
        indexX--;

        int indexY = Integer.valueOf( name.substring( caret ));
        indexY--;

        return new int[]{ indexX, indexY };
    }

    public void updateTutors( Set<Tutor> tutors ) {
        this.tutors.addAll( tutors );
    }
}

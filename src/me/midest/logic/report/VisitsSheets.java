package me.midest.logic.report;

import me.midest.logic.files.WorkbookReader;
import me.midest.model.Lesson;
import me.midest.model.SimplePair;
import me.midest.model.Tutor;
import me.midest.model.Visit;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Dmitry on 31.03.2016.
 */
public class VisitsSheets {

    private int bossVisits = 0;
    private int otherVisits = 0;
    private WorkbookReader reader;

    public VisitsSheets(){
        reader = new WorkbookReader();
        columnShift = WorkbookReader.getExcelColumnIndex( Fields.END.getColumn() )
                - WorkbookReader.getExcelColumnIndex( Fields.START.getColumn() ) + 1;
    }

    public HSSFWorkbook generate( Collection<Visit> visits ) throws IOException {
        Workbook workbook = reader.read( new File( TEMPLATE_NAME ) );
        if( workbook instanceof HSSFWorkbook ) {
            HSSFWorkbook wb = ( HSSFWorkbook )workbook;
            fill( wb, visits );
            return wb;
        }
        return null;
    }

    private void fill( HSSFWorkbook template, Collection<Visit> visits ) {
        List<Visit> visitsList = new ArrayList<>( visits );
        Collections.sort( visitsList, VisitsDatesComparator.comparator );
        calculateVisitsCounts( visitsList );
        createBlanks( template.getSheet( SHEET_NAME_BOSS ), bossVisits );
        createBlanks( template.getSheet( SHEET_NAME_OTHER ), otherVisits );
        setPrintArea( template, template.getSheetIndex( SHEET_NAME_BOSS ), bossVisits );
        setPrintArea( template, template.getSheetIndex( SHEET_NAME_OTHER ), otherVisits );
        putData( template, visitsList );
    }

    private void putData( HSSFWorkbook template, List<Visit> visits ) {
        Sheet sheetBoss = template.getSheet( SHEET_NAME_BOSS );
        Sheet sheetOther = template.getSheet( SHEET_NAME_OTHER );
        int indexBoss = -1;
        int indexOther = -1;
        for( int i = 0; i < visits.size(); i++ ){
            Sheet sheet;
            int index;
            Visit v = visits.get( i );
            boolean isBoss = v.getVisitor().isBoss();
            if( isBoss ){
                sheet = sheetBoss;
                index = ++indexBoss;
            }
            else{
                sheet = sheetOther;
                index = ++indexOther;
            }
            setCellsValues( sheet, i, index, v,
                    Fields.DATE_TIME, Fields.FACULTY, Fields.GROUP, Fields.ROOM,
                    Fields.DISCIPLINE, Fields.LESSON_TYPE, Fields.LESSON_THEME,
                    Fields.TUTOR_REGALIA, Fields.VISITOR_REGALIA, Fields.NUMBER,
                    isBoss ? Fields.TUTOR_POSITION : Fields.TUTOR_POSITION_OTHER,
                    isBoss ? Fields.TUTOR_NAME : Fields.TUTOR_NAME_OTHER );
            if( !isBoss )
                setCellsValues( sheet, i, index, v,
                        Fields.VISITOR_POSITION_OTHER, Fields.VISITOR_NAME_OTHER );
        }
    }

    private void setCellsValues( Sheet sheet, int indexOverall, int indexForSheet, Visit v, Fields... fields ){
        for( Fields f : fields )
            setCellValue( sheet, indexOverall, indexForSheet, v, f );
    }

    private void setCellValue( Sheet sheet, int indexOverall, int indexForSheet, Visit v, Fields field ) {
        SimplePair<Integer, Integer> address = WorkbookReader.getExcelCellAddress( field.getCell() );
        Cell cell = sheet.getRow( address.getSecond() ).getCell( address.getFirst() + indexForSheet * columnShift );
        Lesson l = v.getVisit();
        switch( field ){
            case DATE_TIME: cell.setCellValue( l.getDate().format( dtf ) + " " + l.getTime() ); break;
            case FACULTY: cell.setCellValue( getFacultyByGroup( l.getGroup() )); break;
            case GROUP: cell.setCellValue( l.getGroup()); break;
            case ROOM: cell.setCellValue( getRoomFullName( l.getRoom() )); break;
            case DISCIPLINE: cell.setCellValue( l.getDiscipline()); break;
            case LESSON_TYPE: cell.setCellValue( getLessonTypeName( l.getType() )); break;
            case LESSON_THEME: cell.setCellValue( l.getName()); break;
            case TUTOR_REGALIA: cell.setCellValue( getFullRegalia( l.getTutor() )); break;
            case VISITOR_REGALIA: cell.setCellValue( getFullRegalia( v.getVisitor() )); break;
            case VISITOR_POSITION_OTHER: cell.setCellValue( getPosition( v.getVisitor())); break;
            case VISITOR_NAME_OTHER: cell.setCellValue( v.getVisitor().getReverseInitialsName()); break;
            case TUTOR_POSITION:
            case TUTOR_POSITION_OTHER: cell.setCellValue( getPosition( l.getTutor())); break;
            case TUTOR_NAME:
            case TUTOR_NAME_OTHER: cell.setCellValue( l.getTutor().getReverseInitialsName()); break;
            case NUMBER: cell.setCellValue( indexOverall + 1 ); break;
            default: break;
        }
    }

    private String getPosition( Tutor tutor ) {
        return tutor.getWeight().getStatus()
                + ( tutor.getDepartment().isEmpty() ? "" : " " + tutor.getDepartment()) ;
    }

    private String getFullRegalia( Tutor tutor ) {
        return tutor.getWeight().getStatus()
                + ( tutor.fromBosses() ? " " + tutor.getDepartment() : "" )
                + ( tutor.getRegalia().isEmpty() ? "" : ( ", " + tutor.getRegalia()) )
                + ( "\n" )
                + tutor.getName();
    }

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern( "dd.MM.yyyy" );
    private static final int FACULTY_CAPTURE_GROUP = 1;
    private static final Pattern npGroups = Pattern.compile( "НП([1-3])\\d{2}" );
    private static final Pattern krGroups = Pattern.compile( "К\\d{2}" );
    private static String getFacultyByGroup( String group ){
        Matcher iksi = npGroups.matcher( group );
        if( iksi.find()) switch( Integer.valueOf( iksi.group( FACULTY_CAPTURE_GROUP ) ) ){
            //TODO Учесть вариант нескольких групп с разных факультетов
            case 1: return "НАиО";
            case 2: return "";
            case 3: return "";
            default: return "";
        }
        Matcher kr = krGroups.matcher( group );
        if( kr.find())
            return "КР";
        return "";
    }
    private static String getLessonTypeName( String type ){
        switch( type ){
            case "ПЗ": return "практическое занятие";
            case "лекция": return type;
            case "семинар": return type;
            case "сем.": return "семинар";
            case "зачет": return type;
            case "экзамен": return type;
            case "лаб.раб": return "лабораторная работа";
            case "входной контроль": return "практическое занятие";
            case "выходной контроль": return "практическое занятие";
            case "вх.кнтрль": return "практическое занятие";
            case "конт.раб": return "контрольная работа";
            case "зачет_оц": return "зачет с оценкой";
        }
        return type;
    }
    private static final Pattern rooms = Pattern.compile( "\\d{3}" );
    private static String getRoomFullName( String room ){
        String roomString = room.replaceFirst( "а\\.", "" );
        int str = roomString.indexOf( "кор" );
        switch( roomString ){
            case "ММ":
            case "Орд": return roomString;
        }
        if( roomString.contains( "лаб" ))
            return roomString.replace( '_', ' ' );
        if( str != -1 )
            str += 3;
        Matcher m = rooms.matcher( roomString );
        if( m.find())
            return "1-" + ( str == - 1 ? '3' : roomString.charAt( str )) + "-" + m.group();
        return roomString;
    }

    private void createBlanks( Sheet s, int visitsCount ) {
        for( int i = 1; i < visitsCount; i++ ){
            Iterator<Row> it = s.rowIterator();
            while( it.hasNext()){
                Row row = it.next();
                for( int j = 0; j < columnShift; j++ ){
                    Cell cell = row.getCell( j );
                    int index = j + columnShift * i;
                    Cell cell2 = row.createCell( index, cell.getCellType() );
                    s.setColumnWidth( index, s.getColumnWidth( j ) );
                    copyCell( cell, cell2 );
                }
                if( row.getRowNum() == WorkbookReader.getExcelRowIndex( Fields.HEADER1.getRow())
                        || row.getRowNum() == WorkbookReader.getExcelRowIndex( Fields.HEADER2.getRow()) ){
                    s.addMergedRegion( new CellRangeAddress( row.getRowNum(), row.getRowNum(), i * columnShift, (i+1) * columnShift - 1 ));
                }

            }
        }
    }

    private void copyCell( Cell from, Cell to ) {
        to.setCellStyle( from.getCellStyle() );
        to.getCellStyle().cloneStyleFrom( from.getCellStyle() ); // use styles
        to.getCellStyle().setWrapText( true );
        to.setCellComment( from.getCellComment() );
        switch (from.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                to.setCellValue(from.getRichStringCellValue());
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if ( DateUtil.isCellDateFormatted( from )) {
                    to.setCellValue( from.getDateCellValue() );
                } else {
                    to.setCellValue( from.getNumericCellValue() );
                }
                break;
            case Cell.CELL_TYPE_BOOLEAN:
                to.setCellValue( from.getBooleanCellValue() );
                break;
            case Cell.CELL_TYPE_FORMULA:
                to.setCellValue( from.getCellFormula() );
                break;
            default:
        }
    }

    private void setPrintArea( HSSFWorkbook book, int sheetIndex, int visitCount ) {
        String endCol = WorkbookReader.getExcelColumnName(
                WorkbookReader.getExcelColumnIndex( Fields.END.getColumn() ) + (visitCount-1) * columnShift );
        book.setPrintArea( sheetIndex,
                getRange( Fields.START.getRow(), Fields.START.getColumn(),
                        Fields.END.getRow(), endCol ) );
    }

    private String getRange( String startRow, String startCol, String endRow, String endCol ) {
        return '$' + startCol + '$' + startRow + ':' + '$' + endCol + '$' + endRow;
    }

    private void calculateVisitsCounts( Collection<Visit> visitsList ) {
        bossVisits = 0;
        otherVisits = 0;
        for( Visit v : visitsList )
            if( v.getVisitor().isBoss())
                bossVisits++;
            else
                otherVisits++;
    }

    private static Integer columnShift = 2;
    private static final String TEMPLATE_NAME = "template_visit_report.xls";
    private static final String SHEET_NAME_BOSS = "Начальник";
    private static final String SHEET_NAME_OTHER = "Остальные";
    private  enum Fields {
        HEADER1 ( "A", "1" ), // Заголовок 1 (на 2 ячейки)
        HEADER2 ( "A", "13" ), // Заголовок 2 (на 2 ячейки)

        DATE_TIME ( "B", "2" ), // Дата, время
        FACULTY ( "B", "3" ), // Факультет
        GROUP ( "B", "4" ), // Группа
        ROOM ( "B", "5" ), // Аудитория
        DISCIPLINE ( "B", "6" ), // Дисциплина
        LESSON_TYPE ( "B", "7" ), // Тип занятия
        LESSON_THEME ( "B", "8" ), // Тема занятия
        TUTOR_REGALIA ( "B", "9" ), // Преподаватель
        VISITOR_REGALIA ( "B", "10" ), // Посещающий

        TUTOR_POSITION ( "A", "37" ), // Должность преподаватля
        TUTOR_NAME ( "B", "38" ), // Имя

        VISITOR_POSITION_OTHER ( "A", "28" ), // Должность посещающего (не начальника)
        VISITOR_NAME_OTHER ( "B", "29" ), // Имя посещающего (не начальника)

        TUTOR_POSITION_OTHER ( "A", "33" ), // Должность преподаватля (посещает не начальник)
        TUTOR_NAME_OTHER ( "B", "34" ), // Имя (посещает не начальник)

        NUMBER ( "A", "41" ), // Номер посещения по списку

        START( "A", "1" ), // Начальная ячейка
        END( "B", "41" ), // Конечная ячейка
        ;

        private String cell;
        private String column;
        private String row;
        Fields( String col, String row ){
            this.cell = col + row;
            this.column = col;
            this.row = row;
        }

        public String getRow() {
            return row;
        }

        public String getColumn() {
            return column;
        }

        public String getCell() {
            return cell;
        }

    }

    public static class VisitsDatesComparator implements Comparator<Visit>{

        public final static VisitsDatesComparator comparator = new VisitsDatesComparator();

        @Override
        public int compare( Visit o1, Visit o2 ) {
            int compareDates = o1.getVisit().getDate().compareTo( o2.getVisit().getDate());
            return compareDates == 0 ?
                    o1.getVisit().getTime().compareTo( o2.getVisit().getTime() ) : compareDates;
        }

    }

}

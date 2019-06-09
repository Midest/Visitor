package me.midest.logic.report;

import me.midest.logic.files.WorkbookReader;
import me.midest.model.Lesson;
import me.midest.model.SimplePair;
import me.midest.model.Tutor;
import me.midest.model.Visit;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Класс для создания листов контроля.
 * @version 20190609
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

    public Workbook generate( Collection<Visit> visits ) throws IOException {
        Workbook workbook = reader.read( new File( TEMPLATE_NAME ) );
        if( workbook instanceof HSSFWorkbook || workbook instanceof XSSFWorkbook ){
            fill( workbook, visits );
            return workbook;
        }
        return null;
    }

    private void fill( Workbook template, Collection<Visit> visits ) {
        List<Visit> visitsList = new ArrayList<>( visits );
        Collections.sort( visitsList, VisitsDatesComparator.comparator );
        calculateVisitsCounts( visitsList );
        createBlanks( template.getSheet( SHEET_NAME_BOSS ), bossVisits );
        createBlanks( template.getSheet( SHEET_NAME_OTHER ), otherVisits );
        setPrintArea( template, template.getSheetIndex( SHEET_NAME_BOSS ), bossVisits );
        setPrintArea( template, template.getSheetIndex( SHEET_NAME_OTHER ), otherVisits );
        putData( template, visitsList );
    }

    private void putData( Workbook template, List<Visit> visits ) {
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
            case TUTOR_REGALIA: cell.setCellValue( getShortRegalia( l.getTutor() )); break;
            case VISITOR_REGALIA: cell.setCellValue( getShortRegalia( v.getVisitor() )); break;
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
                + ( tutor.getTitles().isEmpty() ? "" : ( ", " + tutor.getTitles()) )
                + ( "\n" )
                + tutor.getName();
    }

    private String getShortRegalia( Tutor tutor ){
        return tutor.getTitles();
    }

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern( "dd.MM.yyyy" );
    private static final int FACULTY_CAPTURE_GROUP = 1;
    private static final Pattern npGroups = Pattern.compile( "НП-([1-3])\\d" );
    private static final Pattern krGroups = Pattern.compile( "К\\d{2}" );
    private static String getFacultyByGroup( String group ){
        Matcher np = npGroups.matcher( group );
        if( np.find()) switch( Integer.valueOf( np.group( FACULTY_CAPTURE_GROUP ) ) ){
            case 1: return "Факультет №1";
            case 2: return "Факультет №2";
            case 3: return "Факультет №3";
            default: return "";
        }
        Matcher k = krGroups.matcher( group );
        if( k.find())
            return "Факультет №4";
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
    private static String getRoomFullName( String room ){
        return room.replaceFirst( "а\\.", "" );
    }

    private void createBlanks( Sheet s, int visitsCount ) {
        List<CellRangeAddress> merged = new ArrayList<>( s.getMergedRegions());
        for( int i = 1; i < visitsCount; i++ ){
            Iterator<Row> it = s.rowIterator();
            while( it.hasNext()){
                Row row = it.next();
                for( int j = 0; j < columnShift; j++ ){
                    Cell cell = row.getCell( j );
                    if( cell == null ){
                        System.out.println( "В шаблоне листов контроля ячейка ("
                                + (row.getRowNum()+1) + ";" + (j+1) + ") не существует. Создаю." );
                        cell = row.createCell( j );
                    }
                    int index = j + columnShift * i;
                    Cell cell2 = row.createCell( index, cell.getCellTypeEnum());
                    s.setColumnWidth( index, s.getColumnWidth( j ) );
                    copyCell( cell, cell2 );
                }
            }
            for( CellRangeAddress a : merged ){
                s.addMergedRegion( new CellRangeAddress( a.getFirstRow(), a.getLastRow(),
                        a.getFirstColumn() + i * columnShift, a.getLastColumn() + i * columnShift ));
            }
        }
    }

    private void copyCell( Cell from, Cell to ) {
        to.setCellStyle( from.getCellStyle() );
        to.getCellStyle().cloneStyleFrom( from.getCellStyle() ); // use styles
        to.getCellStyle().setWrapText( true );
        to.setCellComment( from.getCellComment() );
        switch (from.getCellTypeEnum()) {
            case STRING:
                to.setCellValue(from.getRichStringCellValue());
                break;
            case NUMERIC:
                if ( DateUtil.isCellDateFormatted( from )) {
                    to.setCellValue( from.getDateCellValue() );
                } else {
                    to.setCellValue( from.getNumericCellValue() );
                }
                break;
            case BOOLEAN:
                to.setCellValue( from.getBooleanCellValue() );
                break;
            case FORMULA:
                to.setCellValue( from.getCellFormula() );
                break;
            default:
        }
    }

    private void setPrintArea( Workbook book, int sheetIndex, int visitCount ) {
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
    private static final String TEMPLATE_PREF = "ext/";
    private static final String TEMPLATE_NAME = TEMPLATE_PREF + "template_visit_report.xlsx";
    private static final String SHEET_NAME_BOSS = "Начальник";
    private static final String SHEET_NAME_OTHER = "Остальные";
    private  enum Fields {
        DATE_TIME ( "G", "7" ), // Дата, время
        FACULTY ( "E", "3" ), // Факультет
        GROUP ( "C", "8" ), // Группа
        ROOM ( "L", "10" ), // Аудитория
        DISCIPLINE ( "D", "9" ), // Дисциплина
        LESSON_TYPE ( "D", "10" ), // Тип занятия
        LESSON_THEME ( "C", "11" ), // Тема занятия

        TUTOR_REGALIA ( "G", "29" ), // Преподаватель
        VISITOR_REGALIA ( "G", "27" ), // Посещающий

        TUTOR_POSITION ( "D", "29" ), // Должность преподаватля
        TUTOR_NAME ( "P", "29" ), // Имя

        VISITOR_POSITION_OTHER ( "D", "27" ), // Должность посещающего (не начальника)
        VISITOR_NAME_OTHER ( "P", "27" ), // Имя посещающего (не начальника)

        TUTOR_POSITION_OTHER ( "D", "29" ), // Должность преподаватля (посещает не начальник)
        TUTOR_NAME_OTHER ( "P", "29" ), // Имя (посещает не начальник)

        NUMBER ( "B", "37" ), // Номер посещения по списку

        START( "A", "1" ), // Начальная ячейка
        END( "R", "37" ), // Конечная ячейка
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

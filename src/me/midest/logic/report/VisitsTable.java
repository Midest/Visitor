package me.midest.logic.report;

import me.midest.logic.report.VisitsSheets.VisitsDatesComparator;
import me.midest.model.TimeInterval;
import me.midest.model.Visit;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class VisitsTable {

    public Workbook generate( Collection<Visit> visits, Booktype booktype ) {
        Workbook wb;
        switch( booktype ){
            case XLSX: wb = new XSSFWorkbook(); break;
            case XLS:
            default: wb = new HSSFWorkbook(); break;
        }
        fill( wb, visits );
        return wb;
    }

    private void fill( Workbook template, Collection<Visit> visits ) {
        List<Visit> visitsList = new ArrayList<>( visits );
        Collections.sort( visitsList, VisitsDatesComparator.comparator );
        createTable( template );
        putData( template, visitsList );
    }

    private void putData( Workbook template, List<Visit> visitsList ) {
        Sheet s = template.getSheetAt( 0 );
        int rowIndex = 0;
        for( Visit v : visitsList ){
            Row r = s.createRow( rowIndex++ );
            int cellIndex = 0;
            // Номер
            Cell cell = r.createCell( cellIndex++ );
            cell.setCellValue( rowIndex + "." );
            // Проверяющий
            cell = r.createCell( cellIndex++ );
            {
                cell.setCellValue( v.getVisitor().getName() );
            }
            // Дата
            cell = r.createCell( cellIndex++ );
            {
                LocalDate date = v.getVisit().getDate();
                cell.setCellValue( dtf1.format( date ) + "\n" + dtf2.format( date ));
            }
            // Время
            cell = r.createCell( cellIndex++ );
            {
                TimeInterval time = v.getVisit().getTime();
                cell.setCellValue( dtfT.format( time.getStart()) + "-\n" + dtfT.format( time.getEnd()));
            }
            // Дисциплина
            cell = r.createCell( cellIndex++ );
            {
                cell.setCellValue( v.getVisit().getDiscipline());
            }
            // Занятие
            cell = r.createCell( cellIndex++ );
            {
                cell.setCellValue( v.getVisit().getName());
            }
            // Преподаватель
            cell = r.createCell( cellIndex++ );
            {
                cell.setCellValue( v.getVisit().getTutor().getName());
            }
            // Группа
            cell = r.createCell( cellIndex++ );
            {
                cell.setCellValue( v.getVisit().getGroup());
            }
            // Аудитория
            cell = r.createCell( cellIndex++ );
            {
                cell.setCellValue( getRoomFullName( v.getVisit().getRoom()));
            }
        }
    }
    private static final DateTimeFormatter dtf1 = DateTimeFormatter.ofPattern( "dd.MM." );
    private static final DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern( "yyyy" );
    private static final DateTimeFormatter dtfT = DateTimeFormatter.ofPattern( "HH.mm" );

    private static String getRoomFullName( String room ){
        return room;
    }

    private void createTable( Workbook template ) {
        template.createSheet( "Список" );
    }

    public enum Booktype {
        XLS, XLSX
    }

}

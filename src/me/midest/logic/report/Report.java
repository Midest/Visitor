package me.midest.logic.report;

import me.midest.model.Lesson;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.util.Collection;

public interface Report {

    Workbook generate( HSSFWorkbook template, Collection<Lesson> lessons, ParamTypes paramName, String... params );

    enum ParamTypes{
        DISCIPLINE ( "template_schedule_discipline.xls" ),
        GROUP ( "template_schedule_group.xls" ),
        ROOM ( "template_schedule_room.xls" ),
        @Deprecated // В стандартный шаблон не разместишь
        DATE ( "template_schedule_date.xls" ),
        ;

        private String fileName;
        private File file;

        ParamTypes( String fileName ){
            this.fileName = fileName;
            this.file = new File( fileName );
        }

        public String file(){
            return file.exists() ? fileName : defaultFileName;
        }

        private static final String defaultFileName = "template_schedule_plain.xls";
    }

}

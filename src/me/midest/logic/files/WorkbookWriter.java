package me.midest.logic.files;

import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class WorkbookWriter {

    public WorkbookWriter(){}

    public void write( File xls, Workbook wb ) throws IOException {
        if( wb == null )
            return;
        OutputStream out = new FileOutputStream( xls );
        wb.write( out );
        out.close();
    }

}

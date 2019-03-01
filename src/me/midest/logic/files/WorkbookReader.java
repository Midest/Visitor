package me.midest.logic.files;

import me.midest.model.SimplePair;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class WorkbookReader {

    public WorkbookReader(){}

    public Workbook read( File xls ) throws IOException {
        InputStream inp = new FileInputStream( xls );
        Workbook wb;
        try {
            wb = WorkbookFactory.create( inp );
            inp.close();
        } catch( InvalidFormatException e ) {
            throw new IOException( e );
        }
        return wb;
    }

    private static final char[] alph = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final char[] nums = "0123456789".toCharArray();

    /**
     * Получение имени ячейки по индексам строки и столбца.
     * @param row индекс строки
     * @param col индекс столбца
     * @return Именной адрес ячейки.
     */
    public static String getExcelCellName( int row, int col ){
        String c = CellReference.convertNumToColString( col );
        String r = String.valueOf( row+1 );
        return c+r;
    }

    /**
     * Получение номера колонки документа MS Ecxel по ее имени.
     * @param name имя колонки.
     * @return <b>null</b>, если недопустимое имя ячейки.
     */
    public static int getExcelColumnIndex( String name ){
        // В закоментированном варианте нет проверки
        // на допустимость имени столбца.
        // return CellReference.convertColStringToIndex( name );
        SimplePair<Integer, Integer> t = getExcelCellAddress( name + "1" );
        return ( t == null? -1 : t.getFirst() ) ;
    }

    public static int getExcelRowIndex( String rowNum ){
        SimplePair<Integer, Integer> t = getExcelCellAddress( "A" + rowNum );
        return ( t == null? -1 : t.getSecond() );
    }

    public static String getExcelColumnName( int col ){
        return CellReference.convertNumToColString( col );
    }

    /**
     * Получение адреса ячейки документа MS Excel по ее имени.
     * @param name имя ячейки.
     * @return Массив из двух значений — индексов столбца и строки;
     * <b>null</b>, если недопустимое имя ячейки.
     */
    public static SimplePair<Integer, Integer> getExcelCellAddress( String name ) {
        char[] nameChars = name.toCharArray();
        int caret = name.length();
        CARET: for( int i = 0; i < nameChars.length; i++ ){
            for( int j = 0; j < nums.length; j++ ){
                if( nums[j] == nameChars[i] ){
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
            for( int j = 0; j < alph.length; j++ ){
                if( alph[j] == nameChars[i] ){
                    int pow = caret-i-1;
                    int plus = (j+1)*(int)Math.pow( alph.length, pow );
                    indexX+= plus; break;
                }
            }
        }
        indexX--;

        int indexY = Integer.valueOf( name.substring( caret ));
        indexY--;

        return new SimplePair<>( indexX, indexY );
    }

}

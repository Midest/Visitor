package me.midest.model.time;

import me.midest.model.TimeInterval;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Interval extends TimeInterval {

    protected static final DateTimeFormatter df = DateTimeFormatter.ofPattern( "dd.MM.yy" );

    private LocalDate dateFrom;
    private LocalDate dateTo;

    public static Interval create( String bounds, String fromTo ){
        Interval i = new Interval();
        if( bounds != null ) i.setBounds( bounds );
        if( fromTo != null ) {
            String from = fromTo.substring( 0, fromTo.length() / 2 );
            String to = fromTo.substring( fromTo.length() / 2 + 1 );
            try {
                i.setDateFrom( LocalDate.parse( from, df ) );
                i.setDateTo( LocalDate.parse( to, df ) );
            } catch( DateTimeException e ) {
                i.setDateFrom( LocalDate.parse( from ) );
                i.setDateTo( LocalDate.parse( to ) );
            }
        }
        return i;
    }

    /**
     * Содержится ли указанный период в данном интервале.
     * Если интервал или переданные параметры пустые, возвращает <code>false</code>.
     * @param date дата
     * @param timeInterval временной интревал
     * @return содержится ли указанный интервал в данном
     */
    boolean contains( LocalDate date, TimeInterval timeInterval ){
        if(( !hasDate() && !hasTime()) || ( date == null && timeInterval == null ))
            return false;
        return ( !hasTime() || timeInterval == null || overlaps( timeInterval ))
                && ( !hasDate() || date == null || (
                        !date.isBefore( dateFrom ) && !date.isAfter( dateTo ))
                    );
    }

    public LocalDate getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom( LocalDate dateFrom ){
        this.dateFrom = dateFrom;
        normalize();
    }

    public LocalDate getDateTo() {
        return dateTo;
    }

    public void setDateTo( LocalDate dateTo ) {
        this.dateTo = dateTo;
        normalize();
    }

    public void setDates( LocalDate from, LocalDate to ){
        this.dateFrom = from;
        this.dateTo = to;
        normalize();
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() &&
                ( dateFrom == null || dateTo == null );
    }

    protected boolean hasDate(){
        return dateFrom != null;
    }

    /**
     * {@inheritDoc}
     * <br/><br/>
     * Исправление неверного порядка дат в интервале. Если есть только одно значение даты,
     * второе приравнивается к нему.
     */
    @Override
    protected void normalize() {
        super.normalize();
        if( dateFrom != null && dateTo != null
                && dateFrom.isAfter( dateTo )){
            LocalDate t = dateTo;
            dateTo = dateFrom;
            dateFrom = t;
        }
        if( dateFrom == null && dateTo != null )
            dateFrom = dateTo;
        if( dateTo == null && dateFrom != null )
            dateTo = dateFrom;
    }

    /**
     * Проверяет равенство объектов. Объекты равны, если
     * переданный не <code>null</code>, того же класса и
     * выполнено равенство по всем полям времени и дат.
     * @param other объект для сравнения
     * @return <code>true</code>, если объекты равны
     */
    @Override
    public boolean equals( Object other ) {
        if( this == other ) return true;
        if( other == null ) return false;
        if( !(Interval.class.equals( other.getClass())))
            return false;
        final Interval that = (Interval)other;
        return equals( that );
    }

    protected boolean equals( Interval that ){
        if( !this.strictEquals( that ))
            return false;
        if( this.hasDate() &&
                ( !dateFrom.equals( that.dateFrom ) || !dateTo.equals( that.dateTo )))
            return false;
        if( !this.hasDate() && that.hasDate())
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return super.hashCode()
                + ( dateFrom == null ? 0 : 19 * dateFrom.hashCode())
                + ( dateTo == null ? 0 : 29 * dateTo.hashCode());
    }

    @Override
    public String toString() {
        return ( hasDate() ?
                df.format( dateFrom ) +
                    ( dateFrom.equals( dateTo ) ?
                        " " : "-" + df.format( dateTo ) + " " )
                : "" )
                + ( hasTime() ? super.toString() : "" );
    }
}

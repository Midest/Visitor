package me.midest.model.time;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс для описания периодических интервалов времени в течение дня.
 * Начиная с {@link #first некоей даты} повторяем в пределах отрезка
 * [{@link #dateFrom дата начала}, {@link #dateTo дата конца}]
 * каждые {@link #duration сколько-то} {@link #unit временных единиц}.
 * Если есть указание временного периода [{@link #start начало}, {@link #end конец}],
 * то повторяется он. Если нет, повторяем весь день (без указания времени).
 */
public class Periodical extends Interval {

    private LocalDate first;
    private PeriodUnit unit;
    private int duration;

    public static Periodical create( String bounds, String first, String fromTo,
                                     PeriodUnit unit, int duration ){
        Periodical p = new Periodical();
        if( bounds != null ) p.setBounds( bounds );
        if( first != null ) try {
            p.setFirst( LocalDate.parse( first, df ));
        } catch( DateTimeException e ) {
            p.setFirst( LocalDate.parse( first ));
        }
        if( fromTo != null ) {
            String from = fromTo.substring( 0, fromTo.length() / 2 );
            String to = fromTo.substring( fromTo.length() / 2 + 1 );
            try {
                p.setDateFrom( LocalDate.parse( from, df ) );
                p.setDateTo( LocalDate.parse( to, df ) );
            } catch( DateTimeException e ) {
                p.setDateFrom( LocalDate.parse( from ) );
                p.setDateTo( LocalDate.parse( to ) );
            }
        }
        if( unit != null ) p.setUnit( unit );
        if( duration > 0 ) p.setDuration( duration );
        return p;
    }

    public void setFirst( LocalDate first ) {
        this.first = first;
        normalize();
    }

    public void setUnit( PeriodUnit unit ) {
        this.unit = unit;
    }

    public void setDuration( int duration ) {
        this.duration = duration;
    }

    /**
     * Получение интервалов в соответствии с имеющимися параметрами.
     * Если нет дат, отрицательная периодичность или не указана
     * единица времени для периоды, возвращает <code>null</code>.
     * @return список интервалов, соответствующих этому экземпляру {@link Periodical}.
     */
    public List<Interval> getIntervals(){
        normalize();
        if( !hasDate() || duration < 0 || unit == null )
            return null;
        List<Interval> intervals = new ArrayList<>();
        LocalDate date = first;
        while( !date.isAfter( getDateTo())) {
            Interval i = new Interval();
            if( hasTime()){
                i.setStart( this.getStart() );
                i.setEnd( this.getEnd() );
            }
            i.setDateFrom( date );
            intervals.add( i );
            date = date.plus( duration, unit.getChronoUnit());
            if( duration == 0 ) break;
        }
        return intervals;
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && ( first == null || unit == null || duration < 0);
    }

    public boolean noDateBounds(){
        return first.equals( getDateFrom())
                && first.equals( getDateTo());
    }

    /**
     * {@inheritDoc}
     * <br/><br/>
     * Если первая {@link #setFirst(LocalDate) дата} установлена, а интервал нет,
     * то приравниваем его начало и конец к этой дате. Если дата не установлена, а
     * интервал есть, приравниваем ее к его началу. Если есть дата и интервал,
     * но дата выходит за его границы, переносим дату на начало интервала.
     */
    @Override
    protected void normalize() {
        super.normalize();
        if( getDateFrom() == null && first != null )
            setDateFrom( first );
        if( first == null && getDateFrom() != null )
            first = getDateFrom();
        if( hasDate() && ( first.isBefore( getDateFrom() ) || first.isAfter( getDateTo())))
            first = getDateFrom();
    }

    /**
     * {@inheritDoc}
     * Должны быть равны также {@link #setDuration(int) продолжительность}
     * и {@link #setUnit(PeriodUnit) единица продолжительности}.
     * @param other объект для сравнения
     * @return <code>true</code>, если объекты равны
     */
    @Override
    public boolean equals( Object other ) {
        if( this == other ) return true;
        if( other == null ) return false;
        if( !(other instanceof Periodical )) return false;
        final Periodical that = (Periodical)other;
        if( !super.equals( other ))
            return false;
        if( this.hasDate() && !this.first.equals( that.first ))
            return false;
        if( this.first == null && that.first != null )
            return false;
        if( this.duration != that.duration )
            return false;
        if( this.unit == null && that.unit != null )
            return false;
        if( this.unit != null && !this.unit.equals( that.unit ))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return super.hashCode()
                + ( first == null ? 0 : 31 * first.hashCode())
                + ( unit == null ? 0 : 37 * unit.hashCode())
                + 41 * duration;
    }

    @Override
    public String toString() {
        return isEmpty() ? "Пустое" :
                super.toString().trim() + " ("//"\n("
                + ( first == null ? "" : "с " + df.format( first ))
                + ( duration < 0 ? "" :
                        ( unit == null ? "" : " каждый " + duration + " " + unit.toString()))
                + ")";
    }
}

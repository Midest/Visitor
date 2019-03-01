package me.midest.model;

import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.Set;

public class TimeInterval implements Comparable<TimeInterval> {

    private static Set<TimeInterval> intervalList = new LinkedHashSet<>();

    public static void clear(){
        intervalList.clear();
    }

    private LocalTime start;
    private LocalTime end;

    public LocalTime getStart() {
        return start;
    }

    public void setStart( LocalTime start ) {
        this.start = start;
    }

    public LocalTime getEnd() {
        return end;
    }

    public void setEnd( LocalTime end ) {
        this.end = end;
    }

    public static final TimeInterval EMPTY = new TimeInterval();
    private TimeInterval(){}

    public TimeInterval( String bounds ){
        String bound[] = bounds.trim().replaceAll( "[\\s-]+", " " ).split( " " );
        String first[] = bound[0].split( ":" );
        String second[] = bound[1].split( ":" );
        start = LocalTime.of( Integer.valueOf( first[0] ), Integer.valueOf( first[1] ) );
        end = LocalTime.of(  Integer.valueOf( second[0] ), Integer.valueOf( second[1] )  );
        intervalList.add( this );
    }

    public boolean overlaps( TimeInterval that ){
        return !( ( this == EMPTY || that == EMPTY )
                || ( this.start.isBefore( that.start ) && !this.end.isAfter( that.start ))
                || ( !this.start.isBefore( that.end ) && this.end.isAfter( that.end )));
    }

    /**
     * Два объекта {@link TimeInterval} равны, если {@link #overlaps(TimeInterval) пересекаются}.
     * @param other объект для сравнения
     * @return
     */
    @Override
    public boolean equals( Object other ) {
        if( this == other ) return true;
        if( other == null ) return false;
        if( !(other instanceof TimeInterval )) return false;
        final TimeInterval that = (TimeInterval)other;
        return this.overlaps( that );
    }

    public boolean strictEquals( TimeInterval that ){
        return that != null
                && this.getStart().equals( that.getStart())
                && this.getEnd().equals( that.getEnd());
    }

    @Override
    public int compareTo( TimeInterval that ) {
        return this.start.equals( that.start ) ?
                this.end.compareTo( that.end ) : this.start.compareTo( that.start );
    }

    @Override
    public int hashCode() {
        Integer i = 0;
        for( TimeInterval ti : intervalList ) {
            i++;
            if( ti.equals( this ) )
                return i * TimeInterval.class.getName().hashCode();
        }
        return (intervalList.size()+1) * TimeInterval.class.getName().hashCode();
    }

    @Override
    public String toString() {
        return start + "-" + end;
    }
}

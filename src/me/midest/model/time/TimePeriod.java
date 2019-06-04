package me.midest.model.time;

import me.midest.model.TimeInterval;

import java.time.LocalDate;
import java.util.*;

public class TimePeriod {

    private Set<Interval> intervals = new HashSet<>();
    private Set<Periodical> periodicals = new HashSet<>();

    public TimePeriod(){}

    public void add( Interval i ){
        if( i == null )
            return;
        if( i instanceof Periodical)
            addPeriodical( (Periodical)i );
        else addInterval( i );
    }

    public void addInterval( LocalDate from, LocalDate to, TimeInterval time ){
        Interval i = new Interval();
        i.setDateFrom( from );
        i.setDateTo( to );
        i.setStart( time.getStart());
        i.setEnd( time.getEnd());
        addInterval( i );
    }

    public void addInterval( Interval interval ){
        intervals.add( interval );
    }

    public void addPeriodical( Periodical periodical ){
        periodicals.add( periodical );
    }

    public void removeInterval( Interval interval ){
        intervals.remove( interval );
    }

    public void removePeriodical( Periodical periodical ){
        periodicals.remove( periodical );
    }

    public void clear(){
        intervals.clear();
        periodicals.clear();
    }

    public boolean contains( LocalDate date, TimeInterval timeInterval, LocalDate defaultFrom, LocalDate defaultTo ){
        setBoundsToUnboundPeriodicals( defaultFrom, defaultTo );
        return contains( date, timeInterval );
    }

    public boolean contains( LocalDate date, TimeInterval timeInterval ){
        convertPeriodicals();
        for( Interval i : intervals )
            if( i.contains( date, timeInterval ))
                return true;
        return false;
    }

    public Set<Interval> getIntervals() {
        return intervals;
    }

    public Set<Periodical> getPeriodicals() {
        return periodicals;
    }

    public Collection<Interval> getAllAsIntervals(){
        convertPeriodicals();
        return intervals;
    }

    private void setBoundsToUnboundPeriodicals( LocalDate from, LocalDate to ){
        for( Periodical p : periodicals )
            if( p.noDateBounds())
                p.setDates( from, to );
    }

    private void convertPeriodicals(){
        for( Periodical p : periodicals )
            intervals.addAll( p.getIntervals());
    }

}

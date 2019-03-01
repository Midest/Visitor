package me.midest.model;

import java.time.LocalDate;

public class FixedVisit {

    private Tutor tutor;
    private Tutor visitor;
    private TimeInterval time;
    private LocalDate date;

    public FixedVisit( Tutor tutor, Tutor visitor, TimeInterval time, LocalDate date ){
        this.tutor = tutor;
        this.visitor = visitor;
        this.time = time;
        this.date = date;
    }

    public Tutor getTutor() {
        return tutor;
    }

    public Tutor getVisitor() {
        return visitor;
    }

    public TimeInterval getTime() {
        return time;
    }

    public LocalDate getDate() {
        return date;
    }

    @Override
    public boolean equals( Object other ) {
        if( this == other ) return true;
        if( other == null ) return false;
        if( !(other instanceof FixedVisit)) return false;
        final FixedVisit that = (FixedVisit)other;
        if( !compare( this.getVisitor(), that.getVisitor()))
            return false;
        if( this.getTutor() == null || that.getTutor() == null )
            return false;
        if( !compare( this.getDate(), that.getDate()))
            return false;
        if( !compare( this.getTime(), that.getTime()))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = 14;
        int hcInt = 0;
        hcInt =	( getTutor() != null ? getTutor().hashCode() : 0 );
        result = 31 * result + hcInt;
        hcInt =	( getVisitor() != null ? getVisitor().hashCode() : 0 );
        result = 31 * result + hcInt;
        hcInt = ( getDate() != null ? getDate().hashCode() : 0 );
        result = 31 * result + hcInt;
        hcInt = ( getTime() != null ? getTime().hashCode() : 0 );
        result = 31 * result + hcInt;
        return result;
    }

    private boolean compare( Object o1, Object o2 ){
        if( o1 != null ) return o1.equals(o2);
        else return o2 == null;
    }
}

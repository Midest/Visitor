package me.midest.model.time;

import java.time.temporal.ChronoUnit;

public enum PeriodUnit {
    DAYS( "день", ChronoUnit.DAYS ),
    WEEKS( "неделя", ChronoUnit.WEEKS ),
    MONTHS( "месяц", ChronoUnit.MONTHS );

    private String text;
    private ChronoUnit chronoUnit;
    PeriodUnit( String text, ChronoUnit chronoUnit ){
        this.text = text;
        this.chronoUnit = chronoUnit;
    }

    public ChronoUnit getChronoUnit() {
        return chronoUnit;
    }

    public static PeriodUnit byText( String text ){
        if( text == null )
            return null;
        for( PeriodUnit p : values())
            if( p.toString().equals( text ))
                return p;
        return null;
    }

    @Override
    public String toString() {
        return text;
    }
}

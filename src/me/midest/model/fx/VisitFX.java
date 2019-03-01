package me.midest.model.fx;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import me.midest.logic.coupling.VisitsMetrics;
import me.midest.model.EqualsGradations;
import me.midest.model.Lesson;
import me.midest.model.Visit;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class VisitFX implements EqualsGradations {

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern( "yyyy.MM.dd" );

    private Visit visit;
    private StringProperty visitor;
    private StringProperty date;
    private StringProperty time;
    private StringProperty discipline;
    private StringProperty type;
    private StringProperty room;
    private StringProperty group;
    private StringProperty tutor;
    private IntegerProperty metrics;
    private StringProperty optionals;

    public VisitFX( Visit visit ){
        this.visit = visit;
        this.visitor = new SimpleStringProperty( visit.getVisitor().toString());
        Lesson lesson = visit.getVisit();
        this.date = new SimpleStringProperty( lesson.getDate().format( dtf ) );
        this.time = new SimpleStringProperty( lesson.getTime().toString() );
        this.discipline = new SimpleStringProperty( lesson.getDiscipline() );
        this.type = new SimpleStringProperty( lesson.getType());
        this.room = new SimpleStringProperty( lesson.getRoom() );
        this.group = new SimpleStringProperty( lesson.getGroup() );
        this.tutor = new SimpleStringProperty( lesson.getTutor().toString() );
        this.metrics = new SimpleIntegerProperty( 0 );
        StringBuilder opt = new StringBuilder( visit.getOptionalRulesSatisfaction().toString() );
        for( int i = opt.length(); i < 7; i++ ){
            opt.insert( 0, "0" );
        }
        this.optionals = new SimpleStringProperty( new StringBuilder()
                .append( opt.toString(), 0, 1 )
                .append( opt.toString(), 2,  5 )
                .reverse().toString() );
    }

    public StringProperty visitorProperty() {
        return visitor;
    }

    public StringProperty dateProperty() {
        return date;
    }

    public StringProperty timeProperty() {
        return time;
    }

    public StringProperty disciplineProperty() {
        return discipline;
    }

    public StringProperty typeProperty() {
        return type;
    }

    public StringProperty roomProperty() {
        return room;
    }

    public StringProperty groupProperty() {
        return group;
    }

    public StringProperty tutorProperty() {
        return tutor;
    }

    public IntegerProperty metricsProperty() {
        return metrics;
    }

    public StringProperty optionalsProperty(){
        return optionals;
    }

    public Visit getVisit() {
        return visit;
    }

    public void evaluateMetrics( Collection<Visit> list ) {
        this.metricsProperty().setValue( VisitsMetrics.evaluateLeast( this.getVisit(), list ));
    }

    public void evaluateMetricsFX( Collection<VisitFX> list ) {
        evaluateMetricsFX( list, (VisitFX[]) null );
    }

    public void evaluateMetricsFX( Collection<VisitFX> list, VisitFX... excludes ) {
        List<Visit> visits = new ArrayList<>();
        List<VisitFX> excludesList = excludes == null ?
                new ArrayList<>() : Arrays.asList( excludes );
        list.forEach( v  -> {
            if( !excludesList.contains( v ))
                visits.add( v.getVisit());
        });
        this.metricsProperty().setValue( VisitsMetrics.evaluateLeast( this.getVisit(), visits ) );
    }

    @Override
    public boolean equals( Object other ) {
        if( this == other ) return true;
        if( other == null ) return false;
        if( !(other instanceof VisitFX)) return false;
        final VisitFX that = (VisitFX)other;
        return this.getVisit().equals( that.getVisit());
    }

    /**
     * На случай, когда нужно более точное сравнение посещений.
     * @param other
     * @return
     * @see Visit#deepEquals(Object)
     */
    public boolean deepEquals( Object other ) {
        if( this == other ) return true;
        if( other == null ) return false;
        if( !(other instanceof VisitFX)) return false;
        final VisitFX that = (VisitFX)other;
        return this.getVisit().deepEquals( that.getVisit());
    }

}

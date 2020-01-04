package me.midest.model.fx;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import me.midest.model.Tutor;
import me.midest.model.Tutor.Status;
import me.midest.model.time.TimePeriod;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TutorFX {

    private Tutor tutor;
    private StringProperty name;
    private SimpleStringProperty weight;
    private SimpleStringProperty titles;

    private BooleanProperty visitor;
    private BooleanProperty visitee;

    private TimePeriod unwantedIntervals;
    private TimePeriod unsuitableIntervals;

    private Set<TutorFX> allowedVisitorsSet;
    private ObservableList<TutorFX> allowedVisitors;

    public TutorFX( Tutor tutor ){
        this.tutor = tutor;
        this.name = new SimpleStringProperty( tutor.getName());
        this.weight = new SimpleStringProperty( tutor.getWeight().getStatus() );
        this.titles = new SimpleStringProperty( tutor.getTitles() );
        this.visitor = new SimpleBooleanProperty( tutor.isVisitor() );
        this.visitee = new SimpleBooleanProperty( tutor.isVisitee() );
        this.visitor.addListener( e -> tutor.setVisitor( visitor.get() ) );
        this.visitee.addListener( e -> tutor.setVisitee( visitee.get() ) );
        this.unwantedIntervals = new TimePeriod();
        this.unsuitableIntervals = new TimePeriod();

        this.allowedVisitorsSet = new HashSet<>();
        this.allowedVisitors = FXCollections.observableArrayList();
        allowedVisitors.addListener( (ListChangeListener<TutorFX>) c -> {  // выкидываем повторения TODO подумать над лучшим решением
            while( c.next()) {
                if( c.wasAdded() ) {
                    List<? extends TutorFX> added = c.getAddedSubList();
                    added.forEach( t -> {
                        if( !allowedVisitorsSet.add( t ) )
                            allowedVisitors.remove( t );
                    } );
                }
                if( c.wasRemoved() )
                    allowedVisitorsSet.removeAll( c.getRemoved());
            }
        });
    }

    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty weightProperty() {
        return weight;
    }

    public StringProperty titlesProperty() {
        return titles;
    }

    public ObservableList<TutorFX> getAllowedVisitors() {
        return allowedVisitors;
    }

    public void setWeight( Status weight ) {
        this.weight.set( weight.getStatus());
        this.tutor.setWeight( weight );
    }

    public void setTitles( String titles ) {
        this.titles.set( titles );
        this.tutor.setTitles( titles );
    }

    public BooleanProperty visitorProperty() {
        return visitor;
    }

    public BooleanProperty visiteeProperty() {
        return visitee;
    }

    public Tutor getTutor() {
        return tutor;
    }

    public TimePeriod getUnwantedIntervals() {
        return unwantedIntervals;
    }

    public TimePeriod getUnsuitableIntervals() {
        return unsuitableIntervals;
    }

    @Override
    public boolean equals( Object other ) {
        if( this == other ) return true;
        if( other == null ) return false;
        if( !(other instanceof TutorFX)) return false;
        final TutorFX that = (TutorFX)other;
        return this.getTutor().equals( that.getTutor());
    }

    @Override
    public int hashCode() {
        return this.getTutor().hashCode();
    }

}

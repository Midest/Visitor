package me.midest.model.fx;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import me.midest.model.Tutor;
import me.midest.model.Tutor.Status;

public class TutorFX {

    private Tutor tutor;
    private StringProperty name;
    private SimpleStringProperty weight;

    private BooleanProperty visitor;
    private BooleanProperty visitee;

    public TutorFX( Tutor tutor ){
        this.tutor = tutor;
        this.name = new SimpleStringProperty( tutor.getName());
        this.weight = new SimpleStringProperty( tutor.getWeight().getStatus() );
        this.visitor = new SimpleBooleanProperty( tutor.isVisitor() );
        this.visitee = new SimpleBooleanProperty( tutor.isVisitee() );
        this.visitor.addListener( e -> tutor.setVisitor( visitor.get() ) );
        this.visitee.addListener( e -> tutor.setVisitee( visitee.get() ) );
    }

    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty weightProperty() {
        return weight;
    }

    public void setWeight( Status weight ) {
        this.weight.set( weight.getStatus());
        this.tutor.setWeight( weight );
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

    @Override
    public boolean equals( Object other ) {
        if( this == other ) return true;
        if( other == null ) return false;
        if( !(other instanceof TutorFX)) return false;
        final TutorFX that = (TutorFX)other;
        return this.getTutor().equals( that.getTutor());
    }
}

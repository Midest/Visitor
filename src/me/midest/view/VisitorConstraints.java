package me.midest.view;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import me.midest.model.fx.TutorFX;
import me.midest.view.editors.TutorFXListCell;

import java.util.*;

public class VisitorConstraints {

    @FXML
    private ListView<TutorFX> visitors;

    @FXML
    private ListView<TutorFX> visitees;

    @FXML
    private void initialize() {
        updateList( visitors );
        updateList( visitees );
    }

    void setTutors( Collection<TutorFX> allTutors ){
        visitees.getItems().clear();
        visitors.getItems().clear();
        List<TutorFX> tutors = new ArrayList<>(allTutors);
        tutors.sort( Comparator.comparing( t -> t.getTutor().getName()));
        tutors.forEach( t -> {
            if( t.visitorProperty().get()) visitors.getItems().add( t );
            if( t.visiteeProperty().get()) visitees.getItems().add( t );
        } );
    }

    public Collection<TutorFX> getSelectedVisitors() {
        return visitors.getSelectionModel().getSelectedItems();
    }

    public Collection<TutorFX> getSelectedVisitees() {
        return visitees.getSelectionModel().getSelectedItems();
    }

    private void updateList( ListView<TutorFX> list ) {
        list.getSelectionModel().setSelectionMode( SelectionMode.MULTIPLE );
        list.setCellFactory( l -> new TutorFXListCell());
    }

}

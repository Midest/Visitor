package me.midest.view.controls;

import javafx.scene.Node;

public class FXUtils {

    public static Object getController( Node node ) {
        Object controller = null;
        while( controller == null && node != null ) {
            controller = node.getProperties().get( "controller" );
            node = node.getParent();
        }
        return controller;
    }

}

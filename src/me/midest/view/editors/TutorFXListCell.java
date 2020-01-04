package me.midest.view.editors;

import javafx.scene.control.ListCell;
import me.midest.model.fx.TutorFX;

public class TutorFXListCell extends ListCell<TutorFX> {

    @Override
    protected void updateItem( TutorFX item, boolean empty ) {
        super.updateItem( item, empty );
        setText( item != null ? item.getTutor().getName() : "" );
    }

}

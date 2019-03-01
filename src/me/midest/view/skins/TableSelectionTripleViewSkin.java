/**
 * Copyright (c) 2018 Dmitrii Molkov
 * Copyright (c) 2014, 2018 ControlsFX
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package me.midest.view.skins;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.Pair;
import me.midest.view.controls.TableSelectionTripleView;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Derived from ControlsFX ListSelectionView class.
 */
public class TableSelectionTripleViewSkin<T> extends SkinBase<TableSelectionTripleView<T>> {

    private GridPane gridPane;
    private HBox horizontalButtonBoxSecond;
    private HBox horizontalButtonBoxTarget;
    private VBox verticalButtonBoxSecond;
    private VBox verticalButtonBoxTarget;

    private Button copyToSecond;
    private Button removeFromSecond;
    private Button moveToTarget;
    private Button moveToTargetAll;
    private Button moveToSourceAll;
    private Button moveToSource;
    private TableView<T> sourceTableView;
    private TableView<T> secondTableView;
    private TableView<T> targetTableView;

    public TableSelectionTripleViewSkin( TableSelectionTripleView<T> view ) {
        super( view );

        sourceTableView = requireNonNull( createSourceTableView(),
                "source list view can not be null");
        sourceTableView.setId( "source-list-view" );
        sourceTableView.setItems( view.getSourceItems() );

        secondTableView = requireNonNull( createSecondTableView(),
                "second list view can not be null");
        secondTableView.setId( "second-list-view" );
        secondTableView.setItems( view.getSecondItems() );

        targetTableView = requireNonNull( createTargetTableView(),
                "target list view can not be null");
        targetTableView.setId( "target-list-view" );
        targetTableView.setItems( view.getTargetItems() );

        gridPane = createGridPane();
        gridPane.setVgap( 10.0 );
        gridPane.setHgap( 10.0 );
        gridPane.setPadding( new Insets( 10 ) );
        Orientation orientation = getSkinnable().getOrientation();
        if( orientation == Orientation.HORIZONTAL ){
            verticalButtonBoxSecond = createVerticalButtonBoxSecond();
            verticalButtonBoxTarget = createVerticalButtonBoxTarget();
        } else {
            horizontalButtonBoxSecond = createHorizontalButtonBoxSecond();
            horizontalButtonBoxTarget = createHorizontalButtonBoxTarget();
        }
        updateButtons();

        getChildren().add(gridPane);

        InvalidationListener updateListener = o -> updateView();

        view.sourceHeaderProperty().addListener(updateListener);
        view.sourceFooterProperty().addListener(updateListener);
        view.secondHeaderProperty().addListener(updateListener);
        view.secondFooterProperty().addListener(updateListener);
        view.targetHeaderProperty().addListener(updateListener);
        view.targetFooterProperty().addListener(updateListener);

        view.sourceHeaderProperty().getValue().setStyle( "-fx-font-size: 1.2em;-fx-font-weight: bold;" );
        view.secondHeaderProperty().getValue().setStyle( "-fx-font-size: 1.2em;-fx-font-weight: bold;" );
        view.targetHeaderProperty().getValue().setStyle( "-fx-font-size: 1.2em;-fx-font-weight: bold;" );

        updateView();

        view.orientationProperty().addListener(observable -> updateView());
    }

    private GridPane createGridPane() {
        GridPane gridPane = new GridPane();
        gridPane.getStyleClass().add("grid-pane");

        return gridPane;
    }

    // Constraints used when view's orientation is HORIZONTAL
    private void setHorizontalViewConstraints() {
        gridPane.getColumnConstraints().clear();
        gridPane.getRowConstraints().clear();

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setFillWidth( true );
        col1.setHgrow( Priority.ALWAYS );
        col1.setMaxWidth( Double.MAX_VALUE );
        col1.setPrefWidth( 200 );

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setFillWidth( true );
        col2.setHgrow( Priority.NEVER );

        ColumnConstraints col3 = new ColumnConstraints();
        col3.setFillWidth( true );
        col3.setHgrow( Priority.ALWAYS );
        col3.setMaxWidth( Double.MAX_VALUE );
        col3.setPrefWidth( 200 );

        gridPane.getColumnConstraints().addAll( col1, col2, col3 );

        RowConstraints row1 = new RowConstraints();
        row1.setFillHeight( true );
        row1.setVgrow( Priority.NEVER );

        RowConstraints row2 = new RowConstraints();
        row2.setMaxHeight( Double.MAX_VALUE );
        row2.setPrefHeight( 50 );
        row2.setVgrow( Priority.SOMETIMES );

        RowConstraints row3 = new RowConstraints();
        row3.setFillHeight( true );
        row3.setVgrow( Priority.NEVER );

        RowConstraints row4 = new RowConstraints();
        row4.setFillHeight( true );
        row4.setVgrow( Priority.NEVER );

        RowConstraints row5 = new RowConstraints();
        row5.setMaxHeight( Double.MAX_VALUE );
        row5.setPrefHeight( 200 );
        row5.setVgrow( Priority.SOMETIMES );

        gridPane.getRowConstraints().addAll(row1, row2, row3, row4, row5);
    }

    // Constraints used when view's orientation is VERTICAL
    private void setVerticalViewConstraints() {
        gridPane.getColumnConstraints().clear();
        gridPane.getRowConstraints().clear();

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setFillWidth(true);
        col1.setHgrow( Priority.SOMETIMES );
        col1.setMaxWidth( Double.MAX_VALUE );
        col1.setPrefWidth( 200 );

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setFillWidth(true);
        col2.setHgrow( Priority.SOMETIMES );
        col2.setMaxWidth( Double.MAX_VALUE );
        col2.setPrefWidth( 200 );

        gridPane.getColumnConstraints().addAll( col1, col2 );

        RowConstraints row1 = new RowConstraints();
        row1.setFillHeight( true );
        row1.setVgrow( Priority.NEVER );

        RowConstraints row2 = new RowConstraints();
        row2.setMaxHeight( Double.MAX_VALUE );
        row2.setPrefHeight( 200 );
        row2.setVgrow( Priority.ALWAYS );

        RowConstraints row3 = new RowConstraints();
        row3.setFillHeight( true );
        row3.setVgrow( Priority.NEVER );

        RowConstraints row4 = new RowConstraints();
        row4.setFillHeight( true );
        row4.setVgrow( Priority.NEVER );

        RowConstraints row5 = new RowConstraints();
        row5.setFillHeight(true);
        row5.setVgrow(Priority.NEVER);

        RowConstraints row6 = new RowConstraints();
        row6.setMaxHeight(Double.MAX_VALUE);
        row6.setPrefHeight(200);
        row6.setVgrow(Priority.ALWAYS);

        RowConstraints row7 = new RowConstraints();
        row7.setFillHeight(true);
        row7.setVgrow(Priority.NEVER);


        gridPane.getRowConstraints().addAll(row1, row2, row3, row4, row5, row6, row7);
    }


    // Used when view's orientation is HORIZONTAL
    private VBox createVerticalButtonBoxSecond() {
        VBox box = new VBox(5);
        box.setFillWidth( true );

        FontAwesome fontAwesome = new FontAwesome();
        copyToSecond = new Button("",
                fontAwesome.create(FontAwesome.Glyph.COPY));
        removeFromSecond = new Button("",
                fontAwesome.create(FontAwesome.Glyph.REMOVE));

        box.getChildren().addAll(copyToSecond, removeFromSecond);

        return box;
    }

    // Used when view's orientation is HORIZONTAL
    private VBox createVerticalButtonBoxTarget() {
        VBox box = new VBox(5);
        box.setFillWidth( true );

        GlyphFont fontAwesome = GlyphFontRegistry.font( FontAwesome.class.getSimpleName() );
        moveToTarget = new Button("",
                fontAwesome.create(FontAwesome.Glyph.ANGLE_RIGHT));
        moveToTargetAll = new Button("",
                fontAwesome.create(FontAwesome.Glyph.ANGLE_DOUBLE_RIGHT));

        moveToSource = new Button("",
                fontAwesome.create(FontAwesome.Glyph.ANGLE_LEFT));
        moveToSourceAll = new Button("",
                fontAwesome.create(FontAwesome.Glyph.ANGLE_DOUBLE_LEFT));

        box.getChildren().addAll(moveToTarget, moveToTargetAll, moveToSource,
                moveToSourceAll);

        return box;
    }

    // Used when view's orientation is VERTICAL
    private HBox createHorizontalButtonBoxSecond() {
        HBox box = new HBox(5);
        box.setFillHeight( true );

        FontAwesome fontAwesome = new FontAwesome();
        copyToSecond = new Button("",
                fontAwesome.create(FontAwesome.Glyph.COPY));
        removeFromSecond = new Button("",
                fontAwesome.create(FontAwesome.Glyph.REMOVE));

        box.getChildren().addAll(copyToSecond, removeFromSecond);

        return box;
    }

    // Used when view's orientation is VERTICAL
    private HBox createHorizontalButtonBoxTarget() {
        HBox box = new HBox(5);
        box.setFillHeight(true);

        FontAwesome fontAwesome = new FontAwesome();
        moveToTarget = new Button("",
                fontAwesome.create(FontAwesome.Glyph.ANGLE_DOWN));
        moveToTargetAll = new Button("",
                fontAwesome.create(FontAwesome.Glyph.ANGLE_DOUBLE_DOWN));

        moveToSource = new Button("",
                fontAwesome.create(FontAwesome.Glyph.ANGLE_UP));
        moveToSourceAll = new Button("",
                fontAwesome.create(FontAwesome.Glyph.ANGLE_DOUBLE_UP));

        box.getChildren().addAll(moveToTarget, moveToTargetAll, moveToSource,
                moveToSourceAll);

        return box;
    }

    private void updateButtons() {

        copyToSecond.getStyleClass().add("copy-to-second");
        removeFromSecond.getStyleClass().add("remove-from-second");
        moveToTarget.getStyleClass().add("move-to-target-button");
        moveToTargetAll.getStyleClass().add( "move-to-target-all-button" );
        moveToSource.getStyleClass().add( "move-to-source-button" );
        moveToSourceAll.getStyleClass().add("move-to-source-all-button");

        copyToSecond.setMaxWidth( Double.MAX_VALUE );
        removeFromSecond.setMaxWidth( Double.MAX_VALUE );
        moveToTarget.setMaxWidth( Double.MAX_VALUE );
        moveToTargetAll.setMaxWidth( Double.MAX_VALUE );
        moveToSource.setMaxWidth(Double.MAX_VALUE);
        moveToSourceAll.setMaxWidth(Double.MAX_VALUE);

        getSourceTableView().itemsProperty().addListener(
                it -> bindCopyRemoverButtonsToDataModel());

        getSecondTableView().itemsProperty().addListener(
                it -> bindCopyRemoverButtonsToDataModel() );

        getSourceTableView().itemsProperty().addListener(
                it -> bindMoveAllButtonsToDataModel() );

        getTargetTableView().itemsProperty().addListener(
                it -> bindMoveAllButtonsToDataModel() );

        getSourceTableView().selectionModelProperty().addListener(
                it -> bindMoveButtonsToSelectionModel());

        getTargetTableView().selectionModelProperty().addListener(
                it -> bindMoveButtonsToSelectionModel());

        bindCopyRemoverButtonsToDataModel();
        bindMoveButtonsToSelectionModel();
        bindMoveAllButtonsToDataModel();

        copyToSecond.setOnAction( evt -> copyToSecond() );

        removeFromSecond.setOnAction( evt -> removeFromSecond() );

        moveToTarget.setOnAction( evt -> moveToTarget() );

        moveToTargetAll.setOnAction( evt -> moveToTargetAll() );

        moveToSource.setOnAction( evt -> moveToSource() );

        moveToSourceAll.setOnAction( evt -> moveToSourceAll() );
    }

    private void bindCopyRemoverButtonsToDataModel() {
        copyToSecond.disableProperty().bind(
                Bindings.isEmpty( getSourceTableView().getItems()));

        removeFromSecond.disableProperty().bind(
                Bindings.isEmpty( getSecondTableView().getItems()));
    }

    private void bindMoveAllButtonsToDataModel() {
        moveToTargetAll.disableProperty().bind(
                Bindings.isEmpty( getSourceTableView().getItems()));

        moveToSourceAll.disableProperty().bind(
                Bindings.isEmpty( getTargetTableView().getItems()));
    }

    private void bindMoveButtonsToSelectionModel() {
        moveToTarget.disableProperty().bind(
                Bindings.isEmpty( getSourceTableView().getSelectionModel()
                        .getSelectedItems()));

        moveToSource.disableProperty().bind(
                Bindings.isEmpty( getTargetTableView().getSelectionModel()
                        .getSelectedItems()));
    }

    private void updateView() {
        gridPane.getChildren().clear();

        Node sourceHeader = getSkinnable().getSourceHeader();
        Node secondHeader = getSkinnable().getSecondHeader();
        Node targetHeader = getSkinnable().getTargetHeader();
        Node sourceFooter = getSkinnable().getSourceFooter();
        Node secondFooter = getSkinnable().getSecondFooter();
        Node targetFooter = getSkinnable().getTargetFooter();

        TableView<T> sourceList = getSourceTableView();
        TableView<T> secondList = getSecondTableView();
        TableView<T> targetList = getTargetTableView();

        StackPane stackPane = new StackPane();
        stackPane.setAlignment( Pos.CENTER );

        StackPane stackPane2 = new StackPane();
        stackPane2.setAlignment( Pos.CENTER );


        Orientation orientation = getSkinnable().getOrientation();

        if (orientation == Orientation.HORIZONTAL) {
            setHorizontalViewConstraints();
            if (sourceHeader != null) {
                gridPane.add(sourceHeader, 0, 0);
            }

            if (secondHeader != null) {
                gridPane.add(secondHeader, 2, 0);
            }

            if (targetHeader != null) {
                gridPane.add(targetHeader, 2, 3);
            }

            if (sourceList != null) {
                gridPane.add(sourceList, 0, 1, 1, 4);
            }

            if (secondList != null) {
                gridPane.add(secondList, 2, 1);
            }

            if (targetList != null) {
                gridPane.add(targetList, 2, 4);
            }

            if (sourceFooter != null) {
                gridPane.add(sourceFooter, 0, 5);
            }

            if (secondFooter != null) {
                gridPane.add(secondFooter, 2, 2);
            }

            if (targetFooter != null) {
                gridPane.add(targetFooter, 2, 5);
            }

            stackPane.getChildren().add( verticalButtonBoxSecond );
            gridPane.add(stackPane, 1, 1);

            stackPane2.getChildren().add( verticalButtonBoxTarget );
            gridPane.add(stackPane2, 1, 4);

        } else {
            setVerticalViewConstraints();

            if (sourceHeader != null) {
                gridPane.add(sourceHeader, 0, 0);
            }

            if (secondHeader != null) {
                gridPane.add(secondHeader, 0, 4);
            }

            if (targetHeader != null) {
                gridPane.add(targetHeader, 1, 4);
            }

            if (sourceList != null) {
                gridPane.add(sourceList, 0, 1, 2, 1);
            }

            if (secondList != null) {
                gridPane.add(secondList, 0, 5);
            }

            if (targetList != null) {
                gridPane.add(targetList, 1, 5);
            }

            if (sourceFooter != null) {
                gridPane.add(sourceFooter, 0, 2);
            }

            if (secondFooter != null) {
                gridPane.add(secondFooter, 0, 6);
            }

            if (targetFooter != null) {
                gridPane.add(targetFooter, 1, 6);
            }

            stackPane.getChildren().add( horizontalButtonBoxSecond );
            gridPane.add(stackPane, 0, 3);

            stackPane2.getChildren().add( horizontalButtonBoxTarget );
            gridPane.add(stackPane2, 1, 3);


        }
    }


    private void copyToSecond() {
        copy( getSourceTableView(), getSecondTableView() );
        getSourceTableView().getSelectionModel().clearSelection();
    }

    private void removeFromSecond() {
        remove( getSecondTableView() );
        getSecondTableView().getSelectionModel().clearSelection();
    }

    private void moveToTarget() {
        move( getSourceTableView(), getTargetTableView() );
        getSourceTableView().getSelectionModel().clearSelection();
    }

    private void moveToTargetAll() {
        move( getSourceTableView(), getTargetTableView(), new ArrayList<>(
                getSourceTableView().getItems()));
        getSourceTableView().getSelectionModel().clearSelection();
    }

    private void moveToSource() {
        move( getTargetTableView(), getSourceTableView());
        getTargetTableView().getSelectionModel().clearSelection();
    }

    private void moveToSourceAll() {
        move( getTargetTableView(), getSourceTableView(), new ArrayList<>(
                getTargetTableView().getItems()));
        getTargetTableView().getSelectionModel().clearSelection();
    }

    private void move( TableView<T> viewA, TableView<T> viewB ) {
        List<T> selectedItems = new ArrayList<>( viewA.getSelectionModel()
                .getSelectedItems());
        move( viewA, viewB, selectedItems );
    }

    private void move( TableView<T> viewA, TableView<T> viewB, List<T> items ) {
        for( T item : items ) if( item != null ) { // Хрен знает, почему, но в selectedItems появляются null при активном использовании
            viewA.getItems().remove(item);
            viewB.getItems().add(item);
        }
    }

    private void copy( TableView<T> viewA, TableView<T> viewB ) {
        List<T> selectedItems = new ArrayList<>( viewA.getSelectionModel()
                .getSelectedItems());
        copy( viewB, selectedItems );
    }

    private void copy( TableView<T> view, List<T> items ) {
        for( T item : items ) if( item != null && !view.getItems().contains( item ) ) { // Хрен знает, почему, но в selectedItems появляются null при активном использовании
            view.getItems().add(item);
        }
    }

    private void remove( TableView<T> view ) {
        List<T> selectedItems = new ArrayList<>( view.getSelectionModel()
                .getSelectedItems());
        remove( view, selectedItems );
    }

    private void remove( TableView<T> view, List<T> items ) {
        for( T item : items ) if( item != null ) { // Хрен знает, почему, но в selectedItems появляются null при активном использовании
            view.getItems().remove(item);
        }
    }



    /**
     * Returns the source list view (shown on the left-hand side).
     *
     * @return the source list view
     */
    public final TableView<T> getSourceTableView() {
        return sourceTableView;
    }

    public final TableView<T> getSecondTableView() {
        return secondTableView;
    }

    /**
     * Returns the target list view (shown on the right-hand side).
     *
     * @return the target list view
     */
    public final TableView<T> getTargetTableView() {
        return targetTableView;
    }

    /**
     * Creates the {@link ListView} instance used on the left-hand side as the
     * source list. This method can be overridden to provide a customized list
     * view control.
     *
     * @return the source list view
     */
    protected TableView<T> createSourceTableView(){
        return createTableView( true );
    }

    protected TableView<T> createSecondTableView(){
        return createTableView( true );
    }

    /**
     * Creates the {@link ListView} instance used on the right-hand side as the
     * target list. This method can be overridden to provide a customized list
     * view control.
     *
     * @return the target list view
     */
    protected TableView<T> createTargetTableView(){
        return createTableView( true );
    }

    private TableView<T> createTableView( boolean withMetrics ){
        TableView<T> tableView = new TableView<>();
        tableView.setPlaceholder( new Label("Нет данных для отображения"));
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.getColumns().addAll(
                createCols( new Pair<>( "visitor", "Посещающий"), new Pair<>( "date", "Дата" ),
                        new Pair<>( "time", "Время" ), new Pair<>( "discipline", "Дисципилина" ),
                        new Pair<>( "room", "Аудитория" ), new Pair<>( "group", "Группа" ),
                        new Pair<>( "tutor", "Преподаватель" ),
                        new Pair<>( "type", "Вид" ),
                        new Pair<>( "optionals", "Правила"),
                        withMetrics ? new Pair<>( "metrics", "Балл" ) : null ) );
        tableView.getSelectionModel().setSelectionMode( SelectionMode.MULTIPLE );
        return tableView;
    }

    private List<TableColumn<T,String>> createCols( Pair<String,String>... properties ) {
        List<TableColumn<T,String>> columns = new ArrayList<>( properties.length );
        for( Pair<String,String> property : properties ) if( property != null ) {
            TableColumn<T,String> col = new TableColumn<>();
            col.setText( property.getValue() );
            col.setCellValueFactory( new PropertyValueFactory<>( property.getKey() ) );
            columns.add( col );
        }
        return columns;
    }
}

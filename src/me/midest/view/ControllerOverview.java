package me.midest.view;

import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.util.Pair;
import me.midest.Main;
import me.midest.logic.coupling.TermCoupling;
import me.midest.logic.coupling.TheoreticalVisitsValue;
import me.midest.logic.coupling.VisitsValue;
import me.midest.logic.files.TxtReader;
import me.midest.logic.files.TxtWriter;
import me.midest.logic.files.WorkbookWriter;
import me.midest.logic.report.VisitsTable;
import me.midest.model.FixedVisit;
import me.midest.model.Lesson;
import me.midest.model.Tutor;
import me.midest.model.Visit;
import me.midest.model.fx.TutorFX;
import me.midest.model.fx.VisitFX;
import me.midest.model.time.Interval;
import me.midest.model.time.PeriodUnit;
import me.midest.model.time.Periodical;
import me.midest.model.time.TimePeriod;
import me.midest.view.controls.TableSelectionTripleView;
import me.midest.view.skins.TableSelectionTripleViewSkin;
import org.apache.poi.ss.usermodel.Workbook;
import org.controlsfx.control.ToggleSwitch;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.Wizard.LinearFlow;
import org.controlsfx.dialog.WizardPane;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.tools.ValueExtractor;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


public class ControllerOverview {

    private Main main;
    private VisitsTable visitsTableService;
    private WorkbookWriter workbookWriterService;

    @FXML
    private TableSelectionTripleView<VisitFX> visitsTables;
    @FXML
    private TableView<TutorFX> tutorsTable;
    @FXML
    private TextField tutorNameField;
    @FXML
    private ComboBox<Tutor.Status> tutorWeightCombo;
    @FXML
    private CheckBox tutorVisitorCheck;
    @FXML
    private CheckBox tutorVisiteeCheck;
    @FXML
    private TextField tutorTitlesField;
    @FXML
    private Button loadSchedule;
    @FXML
    private Button generateSchedule;
    @FXML
    private Button optimizeSchedule;
    @FXML
    private Button computeRating;
    @FXML
    private Text ratingLabel;
    @FXML
    private Text maxRatingLabel;
    @FXML
    private Text ratioRatingLabel;
    @FXML
    private Text checkLabelSecond;
    @FXML
    private Text checkLabelTarget;
    @FXML
    private Label openedFile;
    @FXML
    private Button saveSchedule;
    @FXML
    private Button saveTutor;
    @FXML
    private Button removeUnwantedInterval;
    @FXML
    private Button removeUnsuitableInterval;
    @FXML
    private ListView<Interval> unwantedIntervalList;
    @FXML
    private ListView<Interval> unsuitableIntervalList;
    @FXML
    private Button addInterval;

    final FileChooser xlsFileChooser = new FileChooser();
    final FileChooser allFileChooser = new FileChooser();

    @FXML
    private void initialize() {
        visitsTableService = new VisitsTable();
        workbookWriterService = new WorkbookWriter();
        xlsFileChooser.setInitialDirectory( new File( System.getProperty( "user.dir" ) ));
        xlsFileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("файлы Excel", "*.xls", "*.xlsx"));
        allFileChooser.setInitialDirectory( new File( System.getProperty( "user.dir" ) ));

        List<TableColumn<TutorFX, String>> list = createCols(
                new Pair<>( "name", "Имя" ),
                new Pair<>( "weight", "Должность" ),
                new Pair<>( "titles", "Звания, степени" )  );
        tutorsTable.getColumns().addAll( list );
        List<TableColumn<TutorFX, Boolean>> list2 = createCols(
                new Pair<>( "visitor", "Проверяющий" ),
                new Pair<>( "visitee", "Посещаемый" ) );
            list2.forEach( c -> c.setCellFactory( col -> new CheckBoxTableCell<>() ) );
        tutorsTable.getColumns().addAll( list2 );
        tutorsTable.getSelectionModel().selectedItemProperty().addListener(
                ( observable, oldValue, newValue ) -> showTutorDetails( newValue ) );
        tutorNameField.setEditable( false );
        tutorWeightCombo.setItems( FXCollections.observableArrayList( Tutor.Status.values() ));
        loadSchedule.setOnAction( e -> {
            xlsFileChooser.setTitle( "Выберите файл расписания занятий преподавателей" );
            File selectedFile = xlsFileChooser.showOpenDialog( main.getPrimaryStage() );
            if (selectedFile != null) {
                main.loadSchedule( selectedFile.getAbsolutePath() );
                openedFile.setText( selectedFile.getAbsolutePath() );
                xlsFileChooser.setInitialDirectory( selectedFile.getParentFile() );
                computeRating();
            }
        });

        generateSchedule.setOnAction( e -> {
            try {
                main.generateSchedule();
                if( main.isInitialized()) computeRating();
            } catch( Exception ex ){
                ex.printStackTrace();
            }
        });
        optimizeSchedule.setOnAction( e -> main.optimize() );
        computeRating.setOnAction( e -> { if( main.isInitialized()) computeRating(); });
        saveSchedule.setOnAction( e -> { if( !visitsTables.getTargetItems().isEmpty()) {
            xlsFileChooser.setTitle( "Сохранить итоговое расписание как" );
            File selectedFile = xlsFileChooser.showSaveDialog( main.getPrimaryStage() );
            if( selectedFile != null ) {
                xlsFileChooser.setInitialDirectory( selectedFile.getParentFile() );
                List<Visit> result = visitsTables.getTargetItems().parallelStream().map( VisitFX::getVisit ).collect( Collectors.toList() );
                Workbook wb = null;
                if( selectedFile.getName().endsWith( ".xls" ) ) {
                    wb = visitsTableService.generate( result, VisitsTable.Booktype.XLS );
                } else if( selectedFile.getName().endsWith( ".xlsx" ) ) {
                    wb = visitsTableService.generate( result, VisitsTable.Booktype.XLSX );
                }
                try {
                    workbookWriterService.write( selectedFile, wb );
                } catch( IOException e1 ) {
                    new Alert( Alert.AlertType.ERROR, "Проблема при сохранении файла:\n" + e1.getLocalizedMessage() ).show();
                }
            }
        }
        } );

        saveTutor.setOnAction( e -> {
            TutorFX selected = tutorsTable.getSelectionModel().getSelectedItem();
            if( selected != null ){
                selected.setWeight( tutorWeightCombo.getSelectionModel().getSelectedItem());
                selected.setTitles( tutorTitlesField.getText());
                selected.visitorProperty().set( tutorVisitorCheck.isSelected());
                selected.visiteeProperty().set( tutorVisiteeCheck.isSelected());
                TimePeriod tp = selected.getUnwantedIntervals();
                tp.clear();
                unwantedIntervalList.getItems().forEach( tp::add );
                tp = selected.getUnsuitableIntervals();
                tp.clear();
                unsuitableIntervalList.getItems().forEach( tp::add );
                //FIXME
            }
        });

        FontAwesome fa = new FontAwesome();
        removeUnsuitableInterval.setGraphic( fa.create( FontAwesome.Glyph.REMOVE ) );
        removeUnwantedInterval.setGraphic( fa.create( FontAwesome.Glyph.REMOVE ) );
        unsuitableIntervalList.getSelectionModel().setSelectionMode( SelectionMode.MULTIPLE );
        unwantedIntervalList.getSelectionModel().setSelectionMode( SelectionMode.MULTIPLE );
        removeUnsuitableInterval.setOnAction( e ->
                unsuitableIntervalList.getItems().removeAll(
                        unsuitableIntervalList.getSelectionModel().getSelectedItems())
        );
        removeUnwantedInterval.setOnAction( e ->
                unwantedIntervalList.getItems().removeAll(
                        unwantedIntervalList.getSelectionModel().getSelectedItems())
        );

        ValueExtractor.addValueExtractor( n -> n instanceof Spinner, ta -> ((Spinner)ta).getValue());
        ValueExtractor.addValueExtractor( n -> n instanceof ToggleSwitch, ta -> ((ToggleSwitch)ta).isSelected());
        // Для разового не нужен период, временная единица и длина промежутка, необязательно время
        // Для повторяющегося необязательны все параметры кроме частоты повторов ИЛИ периода
        addInterval.setOnAction( e -> {
            final WizardPane page1 = new WizardPane();
            final BorderPane pane1 = _createFixedSizePane();
            page1.setHeaderText( "Выберите параметры интервала для добавления" );
            page1.setContent( pane1 );
            pane1.setCenter( _buildPage( "AddIntervalPage1" ));

            final WizardPane page2 = new WizardPane();
            final BorderPane pane2 = _createFixedSizePane();
            page2.setHeaderText( "Настройте параметры интервала для добавления" );
            page2.setContent( pane2 );
            pane2.setCenter( _buildPage( "AddIntervalPage2" ));

            // Поскольку Wizard не собирает настройки с последней страницы, делаем на ней превью
            final WizardPane dummyPage = new WizardPane();
            final BorderPane pane3 = _createFixedSizePane();
            dummyPage.setHeaderText( "Нажмите кнопку для завершения" );
            dummyPage.setContent( pane3 );

            final Wizard wizard = new Wizard();
            wizard.setFlow( new LinearFlow( page1, page2, dummyPage ));
            wizard.setTitle( "Добавление интервала" );

            wizard.getSettings().addListener( (MapChangeListener<String, Object>) change -> {
                if( _fieldId( change.getKey() ) >= 0 )
                    ((BorderPane)dummyPage.getContent())
                            .setCenter( _buildIntervalPreview( wizard.getSettings() ) );
            } );
            wizard.getSettings().addListener( (MapChangeListener<String, Object>) change -> {
                // Костыль, зависит от названия элементов.
                // Можно заменить на branchingWizard
                int controlId = _fieldId( change.getKey() );
                switch( controlId ) { // TODO replace this workaround
                    case 5: {
                        _setEnableNode( page2, controlId, !(Boolean)change.getValueAdded());
                        break;
                    }
                    case 7: case 8: {
                        _setEnableNode( page2, controlId, (Boolean)change.getValueAdded());
                        break;
                    }
                    case 6: case 9:{
                        _setEnableNode( page2, controlId, (Boolean)change.getValueAdded()
                                && _getBoundBoolSetting( controlId, wizard.getSettings() ));
                        break;
                    }
                }
            } );
            wizard.showAndWait().ifPresent( result -> {
                if( result == ButtonType.FINISH ) {
                    ObservableMap<String,Object> s = wizard.getSettings();
                    Interval i = _buildInterval( s );
                    boolean isUnsuitable = (boolean) Optional
                            .ofNullable( s.get( _intervalFields[12] ))
                            .orElse( true );
                    if( i != null && !i.isEmpty()) {
                        if( isUnsuitable && !unsuitableIntervalList.getItems().contains( i ))
                            unsuitableIntervalList.getItems().add( i );
                        else if( !isUnsuitable && !unwantedIntervalList.getItems().contains( i ))
                            unwantedIntervalList.getItems().add( i );
                    }
                }
            });
        } );
    }

    private BorderPane _createFixedSizePane(){
        BorderPane p = new BorderPane();
        p.setPrefWidth( _INTERVAL_PAGE_WIDTH );
        p.setPrefHeight( _INTERVAL_PAGE_HEIGHT );
        return p;
    }

    /*
     * Блок вспомогательных штук для работы с Wizard без дополнительных контроллеров
     */
    private static final String[] _intervalFields = {"timeStart","timeEnd","firstPicker",
            "dateFromPicker", "dateToPicker", "radioSingle", "radioPeriodical",
            "checkInterval", "checkDate", "checkDateBoundaries",
            "durationSpinner","timeUnitCheck","unsuitableInt"};
    private static final int[][] _dependentIds = {{},{},{},{},{},{10,11},{3,4},{0,1},{2},{3,4}};
    private static int _fieldId( String field ){
        String s;
        for( int i = 0; i < _intervalFields.length; i++ ) {
            s = _intervalFields[i];
            if( s.equals( field ) ) return i;
        }
        return -1;
    }
    private void _setEnableNode( Pane parent, int controlId, boolean value ){
        for( int i : _dependentIds[controlId] )
            _findNodeById( parent, i ).setDisable( !value );
    }
    private Node _findNodeById( Pane parent, int i ){
        String key = _intervalFields[i];
        return _findChildNodeByNameRecursively( parent.getChildren(), key );
    }
    private Node _findChildNodeByNameRecursively( List<Node> nodes, String name ){
        for( Node n : nodes ){
            if( n.getId() != null && n.getId().equals( name ))
                return n;
            if( n instanceof Pane ){
                Node n2 = _findChildNodeByNameRecursively( ((Pane) n).getChildren(), name );
                if( n2 != null ) return n2;
            }
        }
        return null;
    }
    private boolean _getBoundBoolSetting( int id, Map<String, Object> settings ){
        switch( id ){
            case 9: return _getBoolSetting( _intervalFields[6], settings );
            case 6: return _getBoolSetting( _intervalFields[9], settings );
            default: return true;
        }
    }
    private boolean _getBoolSetting( String key, Map<String, Object> settings  ){
        Boolean val = (Boolean) settings.get( key );
        return val == null ? true : val;
    }

    private static final DateTimeFormatter _dtf = DateTimeFormatter.ofPattern( "dd.MM.YY" );

    private Node _buildIntervalPreview( ObservableMap<String, Object> s ) {
        StringBuilder text = new StringBuilder( "Добавляем" );
        boolean isSingle = s.containsKey( _intervalFields[5] ) && (boolean)s.get( _intervalFields[5] );
        boolean isPeriodical = s.containsKey( _intervalFields[6] ) && (boolean)s.get( _intervalFields[6] );
        boolean checkTime = s.containsKey( _intervalFields[7] ) && (boolean)s.get( _intervalFields[7] );
        boolean checkFirstDate = s.containsKey( _intervalFields[8] ) && (boolean)s.get( _intervalFields[8] );
        boolean checkDates = s.containsKey( _intervalFields[9] ) && (boolean)s.get( _intervalFields[9] );
        boolean hasUnit = s.containsKey( _intervalFields[11] );
        if( isSingle )
            text.append( " разовый" );
        else if( isPeriodical )
            text.append( " повторяющийся" );
        if( isPeriodical && hasUnit ){
            Object unitText = s.get( _intervalFields[11] );
            PeriodUnit unit = PeriodUnit.byText( unitText.toString());
            text
                    .append( "\n" )
                    .append( unit.equals( PeriodUnit.WEEKS ) ?
                            "каждую " : "каждый ")
                    .append( s.get( _intervalFields[10] ))
                    .append( " " )
                    .append( unitText );
            if( unit.equals( PeriodUnit.WEEKS ))
                text.setCharAt( text.length()-1, 'ю' );
        }
        text.append( " интервал\n" );
        if( checkTime ) {
            String timeStart = (String) s.get( _intervalFields[0] );
            String timeEnd = (String) s.get( _intervalFields[1] );
            if( timeStart != null && timeEnd != null ) text
                    .append( timeStart )
                    .append( "-" )
                    .append( timeEnd )
                    .append( " " );
        }
        if( checkFirstDate ) {
            LocalDate first = (LocalDate)s.get( _intervalFields[2] );
            if( first != null ) text
                    .append( "начиная с " )
                    .append( first.format( _dtf ) )
                    .append( " " );
        }
        if( checkDates && isPeriodical ) {
            LocalDate dateFrom = ((LocalDate) s.get( _intervalFields[3] ));
            LocalDate dateTo = ((LocalDate) s.get( _intervalFields[4] ));
            if( dateFrom != null && dateTo != null ) text
                    .append( checkTime || checkFirstDate ? "\n" : "" )
                    .append( "в промежутке " )
                    .append( dateFrom.format( _dtf ) )
                    .append( "-" )
                    .append( dateTo.format( _dtf ) )
                    .append( " " );
        }
        text.append( "\n\n" );
        Label n = new Label( text.toString());
        n.setStyle( "-fx-font-size:19;" );
        n.setAlignment( Pos.CENTER );
        n.setTextAlignment( TextAlignment.CENTER );
        return n;
    }
    private Interval _buildInterval( ObservableMap<String, Object> s ) {
        boolean isSingle = s.containsKey( _intervalFields[5] ) && (boolean)s.get( _intervalFields[5] );
        boolean isPeriodical = s.containsKey( _intervalFields[6] ) && (boolean)s.get( _intervalFields[6] );
        boolean checkTime = s.containsKey( _intervalFields[7] ) && (boolean)s.get( _intervalFields[7] );
        boolean checkFirstDate = s.containsKey( _intervalFields[8] ) && (boolean)s.get( _intervalFields[8] );
        boolean checkDates = s.containsKey( _intervalFields[9] ) && (boolean)s.get( _intervalFields[9] );
        boolean hasUnit = s.containsKey( _intervalFields[11] );
        Interval i = null;
        Periodical p = null;
        if( isSingle )
            i = new Interval();
        else if( isPeriodical )
            i = p = new Periodical();
        if( i == null )
            return null;
        if( isPeriodical && hasUnit ){
            Object unitText = s.get( _intervalFields[11] );
            PeriodUnit unit = PeriodUnit.byText( unitText.toString());
            p.setUnit( unit );
            p.setDuration( Integer.parseInt( s.get( _intervalFields[10] ).toString()));
        }
        if( checkTime ) {
            String timeStart = (String) s.get( _intervalFields[0] );
            String timeEnd = (String) s.get( _intervalFields[1] );
            i.setStart( LocalTime.parse( timeStart ));
            i.setEnd( LocalTime.parse( timeEnd ));
        }
        if( checkFirstDate ) {
            LocalDate first = (LocalDate)s.get( _intervalFields[2] );
            i.setDateFrom( first );
        }
        if( checkDates && isPeriodical ) {
            LocalDate dateFrom = ((LocalDate) s.get( _intervalFields[3] ));
            LocalDate dateTo = ((LocalDate) s.get( _intervalFields[4] ));
            p.setDates( dateFrom, dateTo );
        }
        return i;
    }

    private Node _buildPage( String fxml ) {
        try {
            return FXMLLoader
                    .<Parent>load( getClass().getResource( "/view/" + fxml + ".fxml" ) );
        } catch( Exception e ){
            return new Text( "Не удалось загрузить страницу" );
        }
    }

    private void computeRating(){
        recalculateMetrics();
        calculateRating();
        checkSchedules();
        validateLabels( true );
    }

    private void checkSchedules() {
        ObservableList<VisitFX> second = visitsTables.getSecondItems();
        ObservableList<VisitFX> target = visitsTables.getTargetItems();
        List<Tutor> tutors = tutorsTable.getItems().parallelStream()
                .map( TutorFX::getTutor )
                .collect( Collectors.toList());
        List<Visit> visitsSecond = second.parallelStream()
                .map( VisitFX::getVisit )
                .collect( Collectors.toList());
        List<Visit> visitsTarget = target.parallelStream()
                .map( VisitFX::getVisit )
                .collect( Collectors.toList());
        String secondFeasibility = main.getChecker().feasibilityCheck( visitsSecond ).toString();
        String secondCompleteness = main.getChecker().completenessCheck( visitsSecond, tutors,
                TermCoupling.VISITS_PER_TUTOR, TermCoupling.MIN_VISITS_PER_BOSS ).toString();
        String targetFeasibility = main.getChecker().feasibilityCheck( visitsTarget ).toString();
        String targetCompleteness = main.getChecker().completenessCheck( visitsTarget, tutors,
                TermCoupling.VISITS_PER_TUTOR, TermCoupling.MIN_VISITS_PER_BOSS ).toString();
        checkLabelSecond.setText( secondFeasibility + ". " + secondCompleteness + ". " );
        checkLabelTarget.setText( targetFeasibility + ". " + targetCompleteness + ". " );
    }

    private void recalculateMetrics() {
        ObservableList<VisitFX> source = visitsTables.getSourceItems();
        ObservableList<VisitFX> second = visitsTables.getSecondItems();
        ObservableList<VisitFX> target = visitsTables.getTargetItems();
        source.forEach( ( v ) -> v.evaluateMetricsFX( target ));
        second.forEach( ( v ) -> v.evaluateMetricsFX( second, v ));
        target.forEach( ( v ) -> v.evaluateMetricsFX( target, v ));
    }

    private void showTutorDetails( TutorFX tutor ) {
        boolean isNull = tutor == null;
        tutorNameField.setText( isNull ? "" : tutor.nameProperty().get() );
        tutorWeightCombo.setValue( isNull ? tutorWeightCombo.getItems().get( 0 ) : tutor.getTutor().getWeight() );
        tutorVisitorCheck.setSelected( !isNull && tutor.visitorProperty().get() );
        tutorVisiteeCheck.setSelected( !isNull && tutor.visiteeProperty().get() );
        tutorTitlesField.setText( isNull ? "" : tutor.titlesProperty().get() );
        unwantedIntervalList.getItems().clear();
        if( !isNull ) {
            unwantedIntervalList.getItems().addAll( tutor.getUnwantedIntervals().getIntervals() );
            unwantedIntervalList.getItems().addAll( tutor.getUnwantedIntervals().getPeriodicals() );
        }
        unsuitableIntervalList.getItems().clear();
        if( !isNull ) {
            unsuitableIntervalList.getItems().addAll( tutor.getUnsuitableIntervals().getIntervals() );
            unsuitableIntervalList.getItems().addAll( tutor.getUnsuitableIntervals().getPeriodicals() );
        }

        tutorNameField.disableProperty().set( isNull );
        tutorWeightCombo.disableProperty().set( isNull );
        tutorVisitorCheck.disableProperty().set( isNull );
        tutorVisiteeCheck.disableProperty().set( isNull );
        tutorTitlesField.disableProperty().set( isNull );
        saveTutor.disableProperty().set( isNull );
        addInterval.disableProperty().set( isNull );
    }

    private <S,T> List<TableColumn<S,T>> createCols( Pair<String,String>... properties ) {
        List<TableColumn<S,T>> columns = new ArrayList<>( properties.length );
        for( Pair<String,String> property : properties ) if( property != null )
            columns.add( createCol( property ));
        return columns;
    }

    private <S,T> TableColumn<S,T> createCol( Pair<String,String> property ){
        TableColumn<S,T> col = new TableColumn<>();
        col.setText( property.getValue() );
        col.setCellValueFactory( new PropertyValueFactory<>( property.getKey()));
        return col;
    }

    public void loadValues( ObservableList<VisitFX> source, ObservableList<VisitFX> second,
                            ObservableList<VisitFX> target, ObservableList<TutorFX> tutors ){
        visitsTables.setItems( source, second, target );
        tutorsTable.setItems( tutors );
        calculateRating();
        recalculateMetrics();
        target.addListener((ListChangeListener<VisitFX>) c -> {
            c.next();
            if( c.getAddedSize()!= 0 || c.getRemovedSize() != 0 )
                validateLabels( false );
        });
        source.addListener( (ListChangeListener<VisitFX>) c -> {
            c.next();
            if( c.getAddedSize()!= 0 || c.getRemovedSize() != 0 )
                validateLabels( false );
        } );
        second.addListener( (ListChangeListener<VisitFX>) c -> {
            c.next();
            if( c.getAddedSize()!= 0 || c.getRemovedSize() != 0 )
                validateLabels( false );
        } );
        initActions();
    }

    private void initActions() {
        TableSelectionTripleViewSkin<VisitFX> view = ( (TableSelectionTripleViewSkin<VisitFX>) visitsTables.getSkin() );
        view.setLoadFileToSecondAction( () -> {
            if( main.getSource().isEmpty() && main.getTarget().isEmpty())
                return null;
            allFileChooser.setTitle( "Выберите файл фиксированных посещений" );
            File selectedFile = allFileChooser.showOpenDialog( main.getPrimaryStage() );
            List<VisitFX> result = new ArrayList<>();
            if( selectedFile != null ){
                allFileChooser.setInitialDirectory( selectedFile.getParentFile() );
                List<FixedVisit> fixed = TxtReader.readFixedVisits( selectedFile.getAbsolutePath() );

                /* Собираем текущие данные */
                List<Tutor> tutorsList = main.getTutors()
                        .parallelStream().map( TutorFX::getTutor ).collect( Collectors.toList() );
                Set<Lesson> lessons = main.getSource()
                        .parallelStream().map( v -> v.getVisit().getVisit() ).collect( Collectors.toSet() );
                lessons.addAll( main.getTarget()
                        .parallelStream().map( v -> v.getVisit().getVisit() ).collect( Collectors.toSet() ));

                /* Ищем */
                Tutor v = null;
                Visit vis;
                VisitFX vfx;
                for( FixedVisit f : fixed ) {
                    for( Tutor t : tutorsList ) if( t.equals( f.getVisitor())){
                        v = t;
                        break;
                    }
                    if( v != null ) for( Lesson l : lessons ) {
                        if( l.getTime().equals( f.getTime() )
                                && l.getDate().equals( f.getDate() )
                                && l.getTutor().equals( f.getTutor() ) ) {
                            vis = new Visit( v, l );
                            vfx = new VisitFX( vis );
                            boolean added = false;
                            for( VisitFX mvfx : main.getSource()) if( mvfx.equals( vfx )
                                    && mvfx.getVisit().getVisit().getTutor().equals( l.getTutor() )){
                                result.add( mvfx );
                                added = true;
                                break;
                            }
                            if( !added ) for( VisitFX mvfx : main.getTarget()) if( mvfx.equals( vfx )
                                    && mvfx.getVisit().getVisit().getTutor().equals( l.getTutor() )){
                                result.add( mvfx );
                            }
                        }
                    }
                }
                return result;
            }
            return null;
        } );
        view.setSaveFileFromSecondAction( event -> {
            if( main.getSecond().isEmpty())
                event.consume();
            allFileChooser.setTitle( "Выберите файл для фиксированных посещений" );
            File selectedFile = allFileChooser.showSaveDialog( main.getPrimaryStage() );
            List<FixedVisit> result = main.getSecond().stream()
                    .map( v -> new FixedVisit(
                            v.getVisit().getVisit().getTutor(),
                            v.getVisit().getVisitor(),
                            v.getVisit().getVisit().getTime(),
                            v.getVisit().getVisit().getDate()))
                    .collect( Collectors.toList());
            if( selectedFile != null ) {
                allFileChooser.setInitialDirectory( selectedFile.getParentFile() );
                TxtWriter.writeFixedVisits( selectedFile.getAbsolutePath(), result );
            }
        });
    }

    private void validateLabels( boolean valid ){
        String css = valid ? "-fx-fill: #000000" : "-fx-fill: #ff0000";
        ratingLabel.setStyle( css );
        maxRatingLabel.setStyle( css );
        ratioRatingLabel.setStyle( css );
        checkLabelSecond.setStyle( css );
        checkLabelTarget.setStyle( css );
    }

    private void calculateRating() {
        List<Visit> visits = new ArrayList<>();
        visitsTables.getTargetItems().forEach( v -> visits.add( v.getVisit()));
        double rating = VisitsValue.count( visits );
        double maxRating = maxRating();
        ratingLabel.setText( String.format( "%.2f", rating ));
        maxRatingLabel.setText( String.format( "%.2f", maxRating ) );
        ratioRatingLabel.setText( String.format( "%.2f | %.2f", rating / maxRating, maxRating / rating ) );
    }

    private double maxRating() {
        if( visitsTables.getSourceItems().size() + visitsTables.getTargetItems().size() < 2 )
            return 0;
        int bossVisitors = 0;
        int otherVisitors = 0;
        for( TutorFX tut : tutorsTable.getItems() ){
            if( tut.getTutor().isVisitor()){
                if( tut.getTutor().isBoss()) bossVisitors++;
                else otherVisitors++;
            }
        }
        Set<String> disciplines = collect( o -> o.getVisit().getVisit().getDiscipline() );
        Set<String> lessonTypes = collect( o -> o.getVisit().getVisit().getType() );
        Set<String> groups = collect( o -> o.getVisit().getVisit().getType() );

        LocalDate first = LocalDate.MAX;
        LocalDate last = LocalDate.MIN;
        Visit v;
        LocalDate date;
        for( VisitFX vis : visitsTables.getSourceItems()){
            v = vis.getVisit();
            date = v.getVisit().getDate();
            if( date.isBefore( first ))
                first = date;
            if( date.isAfter( last ))
                last = date;
        }
        for( VisitFX vis : visitsTables.getTargetItems()){
            v = vis.getVisit();
            date = v.getVisit().getDate();
            if( date.isBefore( first ))
                first = date;
            if( date.isAfter( last ))
                last = date;
        }
        return TheoreticalVisitsValue.upperBound( first, last, bossVisitors, otherVisitors,
                visitsTables.getTargetItems().size(), disciplines.size(), lessonTypes.size(), groups.size() );
    }

    private Set<String> collect( Function<VisitFX,String> mapper ){
        Set<String> strings = visitsTables.getSourceItems()
                .parallelStream()
                .map( mapper )
                .collect( Collectors.toSet() );
        strings.addAll( visitsTables.getTargetItems()
                .parallelStream()
                .map( mapper )
                .collect( Collectors.toSet() ));
        return strings;
    }

    public void setMain( Main main ) {
        this.main = main;
    }

    private static final int _INTERVAL_PAGE_WIDTH = 440;
    private static final int _INTERVAL_PAGE_HEIGHT = 230;

}

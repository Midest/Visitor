package me.midest.view;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Pair;
import me.midest.Main;
import me.midest.logic.coupling.PeriodCoupling;
import me.midest.logic.coupling.VisitsValue;
import me.midest.logic.files.WorkbookWriter;
import me.midest.logic.report.VisitsTable;
import me.midest.model.Tutor;
import me.midest.model.Visit;
import me.midest.model.fx.TutorFX;
import me.midest.model.fx.VisitFX;
import me.midest.view.controls.TableSelectionTripleView;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
    private Text checkLabelSecond;
    @FXML
    private Text checkLabelTarget;
    @FXML
    private Label openedFile;
    @FXML
    private Button saveSchedule;
    @FXML
    private Button saveTutor;

    final FileChooser fileChooser = new FileChooser();

    @FXML
    private void initialize() {
        visitsTableService = new VisitsTable();
        workbookWriterService = new WorkbookWriter();
        fileChooser.setInitialDirectory( new File( System.getProperty( "user.dir" ) ));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("файлы Excel", "*.xls", "*.xlsx"));

        List<TableColumn<TutorFX, String>> list = createCols( new Pair<>( "name", "Имя" ), new Pair<>( "weight", "Должность" ) );
        tutorsTable.getColumns().addAll( list );
        List<TableColumn<TutorFX, Boolean>> list2 = createCols(  new Pair<>( "visitor", "Проверяющий" ), new Pair<>( "visitee", "Посещаемый" ) );
            list2.forEach( c -> c.setCellFactory( col -> new CheckBoxTableCell<>() ) );
        tutorsTable.getColumns().addAll( list2 );
        tutorsTable.getSelectionModel().selectedItemProperty().addListener(
                ( observable, oldValue, newValue ) -> showTutorDetails( newValue ) );
        tutorNameField.setEditable( false );
        tutorWeightCombo.setItems( FXCollections.observableArrayList( Tutor.Status.values() ));
        loadSchedule.setOnAction( e -> {
            fileChooser.setTitle( "Выберите файл расписания занятий преподавателей" );
            File selectedFile = fileChooser.showOpenDialog( main.getPrimaryStage() );
            if (selectedFile != null) {
                main.loadSchedule( selectedFile.getAbsolutePath() );
                openedFile.setText( selectedFile.getAbsolutePath() );
                fileChooser.setInitialDirectory( selectedFile.getParentFile() );
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
            fileChooser.setTitle( "Сохранить итоговое расписание как" );
            File selectedFile = fileChooser.showSaveDialog( main.getPrimaryStage() );
            if( selectedFile != null ) {
                fileChooser.setInitialDirectory( selectedFile.getParentFile() );
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
                selected.visitorProperty().set( tutorVisitorCheck.isSelected());
                selected.visiteeProperty().set( tutorVisiteeCheck.isSelected());
            }
        });
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
        String secondCompleteness = main.getChecker().completenessCheck( visitsSecond, tutors, PeriodCoupling.VISITS_PER_TUTOR, PeriodCoupling.MIN_VISITS_PER_BOSS ).toString();
        String targetFeasibility = main.getChecker().feasibilityCheck( visitsTarget ).toString();
        String targetCompleteness = main.getChecker().completenessCheck( visitsTarget, tutors, PeriodCoupling.VISITS_PER_TUTOR, PeriodCoupling.MIN_VISITS_PER_BOSS ).toString();
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
        tutorNameField.setText( tutor == null? "" : tutor.nameProperty().get() );
        tutorWeightCombo.setValue( tutor == null? tutorWeightCombo.getItems().get( 0 ) : tutor.getTutor().getWeight() );
        tutorVisitorCheck.setSelected( tutor != null && tutor.visitorProperty().get() );
        tutorVisiteeCheck.setSelected( tutor != null && tutor.visiteeProperty().get() );
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
    }

    private void validateLabels( boolean valid ){
        String css = valid ? "-fx-fill: #000000" : "-fx-fill: #ff0000";
        ratingLabel.setStyle( css );
        checkLabelSecond.setStyle( css );
        checkLabelTarget.setStyle( css );
    }

    private void calculateRating() {
        List<Visit> visits = new ArrayList<>();
        visitsTables.getTargetItems().forEach( v -> visits.add(v.getVisit()));
        double rating = VisitsValue.count( visits );
        ratingLabel.setText( String.format( "%.2f", rating ));
    }

    public void setMain( Main main ) {
        this.main = main;
    }
}

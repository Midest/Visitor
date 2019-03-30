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
import me.midest.view.controls.TableSelectionTripleView;
import me.midest.view.skins.TableSelectionTripleViewSkin;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
}

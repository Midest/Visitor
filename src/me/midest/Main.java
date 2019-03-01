package me.midest;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import me.midest.logic.coupling.PeriodCoupling;
import me.midest.logic.coupling.ScheduleChecker;
import me.midest.logic.coupling.ScheduleOptimizer;
import me.midest.logic.files.TxtReader;
import me.midest.logic.files.WorkbookReader;
import me.midest.logic.parser.WorkbookParser;
import me.midest.model.*;
import me.midest.model.fx.TutorFX;
import me.midest.model.fx.VisitFX;
import me.midest.view.ControllerOverview;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main extends Application {

    private boolean initialized = false;

    private Stage primaryStage;
    private BorderPane rootLayout;
    private ControllerOverview controller;

    private PeriodCoupling coupler;
    private WorkbookParser parser;
    private ScheduleChecker checker;
    private ScheduleOptimizer optimizer;

    private ObservableList<VisitFX> source;
    private ObservableList<VisitFX> second;
    private ObservableList<VisitFX> target;
    private ObservableList<TutorFX> tutors;

    public static void safeRemoval( ObservableList<VisitFX> listFrom, Collection<VisitFX> toRemove ) {
        listFrom.removeIf( visitFX -> contains( visitFX, toRemove ));
    }
    public static void safeRemoval( Collection<Visit> listFrom, Collection<Visit> toRemove ) {
        listFrom.removeIf( visitFX -> contains( visitFX, toRemove ));
    }

    private static boolean contains( EqualsGradations v, Collection<? extends EqualsGradations> list ){
        Iterator<? extends EqualsGradations> it = list.iterator();
        if( v == null ){
            while( it.hasNext())
                if( it.next() == null )
                    return true;
        } else {
            while( it.hasNext())
                if( v.deepEquals( it.next()))
                    return true;
        }
        return false;
    }

    public Main(){
        source = FXCollections.observableArrayList();
        second = FXCollections.observableArrayList();
        target = FXCollections.observableArrayList();
        tutors = FXCollections.observableArrayList();
    }

    public boolean isInitialized() {
        return initialized;
    }

    public ScheduleChecker getChecker() {
        return checker;
    }

    public ObservableList<VisitFX> getSource() {
        return source;
    }

    public ObservableList<VisitFX> getSecond() {
        return second;
    }

    public ObservableList<VisitFX> getTarget() {
        return target;
    }


    public void loadSchedule( String fileName ) {
        try {
            clearLists();
            String year = deriveYear( fileName );
            Term term = deriveTerm( fileName );
            initServices( fileName, year, term );
            generateSchedule();

        } catch( Exception e ){ e.printStackTrace();}
    }

    private static final Pattern YEAR_REG = Pattern.compile( ".*(20\\d{2}).*" );
    private String deriveYear( String fileName ) {
        int index = fileName.lastIndexOf( "/" );
        String file = fileName.substring( index == -1 ? 0 : index );
        Matcher m = YEAR_REG.matcher( file );
        return m.find() ? m.group(1) : String.valueOf( Calendar.getInstance().get( Calendar.YEAR ));
    }
    private Term deriveTerm( String fileName ) {
        int index = fileName.lastIndexOf( "/" );
        String file = fileName.substring( index == -1 ? 0 : index ).toLowerCase();
        for( Term t : Term.values())
            for( String key : t.getKeys())
                if( file.contains( key ))
                    return t;

        int month = Calendar.getInstance().get( Calendar.MONTH );
        if( month > 4 && month < 10 )
            return Term.ОСЕНЬ;
        else
            return Term.ВЕСНА;
    }

    /**
     * Генерация расписания посещений.
     * @throws Exception
     */
    public void generateSchedule() throws Exception {
        if( !isInitialized()) return;

        List<Tutor> tutorsList = tutors.parallelStream().map( TutorFX::getTutor ).collect( Collectors.toList());

        // Задаем фиксированные посещения
        Collection<Visit> fixed = second.parallelStream().map( VisitFX::getVisit ).collect( Collectors.toList());
        coupler.setFixedVisits( fixed );
        setCouplerTutors( tutorsList );

        // Генерируем расписание
        Collection<Visit> rawResult = coupler.generateSchedule( parser.getResult(), true );

        // Оптимизируем расписание
        List<Visit> result = optimizer.optimize( rawResult, tutorsList, checker, fixed,
                PeriodCoupling.VISITS_PER_TUTOR, PeriodCoupling.MIN_VISITS_PER_BOSS );

        // Очищаем списки
        source.clear();
        target.clear();

        // Перегоняем в визуалочку расписание
        Set<VisitFX> resultFX = result
                .parallelStream().map( VisitFX::new )
                .collect( Collectors.toCollection( HashSet::new ));
        resultFX.parallelStream().forEach( v -> v.evaluateMetricsFX( resultFX, v ));

        // Собираем все возможные посещения в один список
        Map<LocalDate, List<Visit>> allMap = coupler.getPossibleVisits();
        List<Visit> all = new ArrayList<>();
        allMap.values().forEach( all::addAll );

        // Перегоняем в визуалочку все возможные посещения
        List<VisitFX> allFX = all
                .parallelStream().map( VisitFX::new )
                .collect( Collectors.toCollection( ArrayList::new ));
        allFX.parallelStream().forEach( v -> v.evaluateMetricsFX( resultFX ));

        // Передаем в интерфейс
        source.addAll( allFX );
        target.addAll( resultFX );
        safeRemoval( source, target );
    }

    public void optimize(){
        if( !isInitialized()) return;

        Collection<Visit> fixed = second.parallelStream().map( VisitFX::getVisit ).collect( Collectors.toList());
        List<Visit> visits = target.parallelStream().map( VisitFX::getVisit ).collect( Collectors.toList());
        List<Tutor> tutorsList = tutors.parallelStream().map( TutorFX::getTutor ).collect( Collectors.toList());
        List<Visit> newResult = optimizer.optimize( visits, tutorsList, checker, fixed,
                PeriodCoupling.VISITS_PER_TUTOR, PeriodCoupling.MIN_VISITS_PER_BOSS );
        safeRemoval( visits, newResult );
        List<VisitFX> toMove = visits.parallelStream().map( VisitFX::new ).collect( Collectors.toList());
        safeRemoval( target, toMove );
        source.addAll( toMove );
    }

    private void clearLists() {
        source.clear();
        second.clear();
        target.clear();
        tutors.clear();
        initialized = false;
    }

    /**
     * Создает сервисы, парсит файл с расписаниями (и файл с преподавателями).
     * @param fileName имя (с указанием семестра: "весна", "осень")
     * @param year календарный год
     * @param term семестр
     * @throws Exception
     */
    private void initServices( String fileName, String year, Term term ) throws Exception {
        initialized = false;
        TimeInterval.clear();
        coupler = new PeriodCoupling();
        checker = new ScheduleChecker();
        optimizer = new ScheduleOptimizer();

        Set<String> outerRooms = new HashSet<>();
        outerRooms.add( "филиал" );
        outerRooms.add( "фил" );

        Set<String> unwantedTypes = new HashSet<>();
        unwantedTypes.add( "конт.раб" );
        unwantedTypes.add( "зачет" );
        unwantedTypes.add( "зач._оц" );
        unwantedTypes.add( "экзамен" );
        unwantedTypes.add( "экз." );
        unwantedTypes.add( "тест" );

        Set<Tutor> people = new HashSet<>();
        Set<Tutor> outerTutors = new HashSet<>();

        parser = new WorkbookParser( year );

        File book = new File( fileName );
        if( term.hasKey( book.getName()))
            if( book.getName().contains("xls")) {
                String abs = book.getAbsolutePath();
                people.addAll( TxtReader.readTutors(abs.substring(0, abs.lastIndexOf(".")).concat("_преподаватели.txt")));
                parser.updateTutors(people);
                parser.parse(new WorkbookReader().read(book), Lesson.SourceType.TUTOR);
            }

        coupler.setOuterRooms( outerRooms );
        coupler.setOuterTutors( outerTutors );
        coupler.setUnwantedTypes( unwantedTypes );

        // FX
        for( Tutor t : people )
            tutors.add( new TutorFX( t ) );
        initialized = true;
    }

    public void setCouplerTutors( Collection<Tutor> people ) {
        Set<Tutor> visitors = new HashSet<>();
        Set<Tutor> toVisit = new HashSet<>();
        for (Tutor t : people) {
            if( t.isVisitor()) visitors.add(t);
            if( t.isVisitee()) toVisit.add(t);
        }
        coupler.setPossibleVisitors( visitors );
        coupler.setToVisit( toVisit );
    }

    private enum Term{
        ВЕСНА( "весна" ),
        ОСЕНЬ( "осень" );

        private Set<String> keys = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        Term( String... keys ){
            this.keys.addAll( Arrays.asList( keys ));
        }

        public Set<String> getKeys() {
            return keys;
        }

        public boolean hasKey( String string ){
            for( String k : keys )
                if( string.contains(k))
                    return true;
            return false;
        }
    }

    @Override
    public void start( Stage primaryStage ) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle( "Visitor" );
        this.primaryStage.setMinHeight( 750 );
        this.primaryStage.setMinWidth( 1250 );
        initRootLayout();
        showOverview();
        primaryStage.show();
    }

    private void initRootLayout() {
        try {
            rootLayout = new BorderPane();

            // Отображаем сцену, содержащую корневой макет.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.setMaximized( true );
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showOverview(){
        try {
            // Загружаем сведения.
            FXMLLoader loader = new FXMLLoader();
            URL url = getClass().getResource("/view/Overview.fxml");
            loader.setLocation(url);
            AnchorPane overview = loader.load();

            // Помещаем сведения в центр корневого макета.
            rootLayout.setCenter( overview );

            // Даём контроллеру доступ к главному приложению.
            controller = loader.getController();
            controller.setMain( this );
            controller.loadValues( source, second, target, tutors );
        } catch( IOException e ) {
            e.printStackTrace();
        }
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch( args );
    }

}

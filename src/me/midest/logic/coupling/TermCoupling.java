package me.midest.logic.coupling;

import javafx.util.Pair;
import me.midest.model.*;

import javax.swing.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class TermCoupling {

    /** Минимум посещений начальнику. */
    public static final int MIN_VISITS_PER_BOSS = 10;
    /** Минимальное число посещений каждым преподавателем, кроме начальников. */
    public static final int VISITS_PER_TUTOR = 1;

    /** Расписание. */
    private Term term;
    /** Все возможные посещения всех пар по датам. */
    private Map<LocalDate, List<Visit>> possibleVisits;
    /** Список всех, кто может посещать, включая начальников. */
    private Set<Tutor> possibleVisitors;
    /** Список тех, кого надо посетить. */
    private Set<Tutor> toVisit;
    /** Список аудиторий, пары в которых не учитываются. */
    private Set<String> outerRooms;
    /** Список преподавателей, которые не относятся к кафедре (и не посещаются). */
    private Set<Tutor> outerTutors;
    /** Список типов занятий, которые нежелательно посещать */
    private Set<String> unwantedTypes;
    /** Список тех, для чьих пар не удалось найти возможностей посещения. */
    private Set<Tutor> withoutPossibleVisits;

    // Счетчики для составления расписания
    /** Посещений каждым. */
    private Map<Tutor, Integer> visitorVisits;
    /** Посещенные преподаватели. */
    private Set<Tutor> visitedTutors;
    /** Даты пар итогового расписания посещений. */
    private Set<Pair<Tutor, LocalDate>> tutorDate;
    private List<Visit> result;
    private Set<Lesson> resultLessons;
    private Map<Tutor, Set<Lesson>> byTutor;

    private Set<FixedVisit> fixedVisits;

    public TermCoupling(){
        possibleVisits = new HashMap<>();

        possibleVisitors = new HashSet<>();
        toVisit = new HashSet<>();

        outerRooms = new HashSet<>();
        outerTutors = new HashSet<>();

        withoutPossibleVisits = new HashSet<>();

        visitorVisits = new HashMap<>();
        visitedTutors = new HashSet<>();
        tutorDate = new HashSet<>();
        result = new ArrayList<>();
        resultLessons = new HashSet<>();

        fixedVisits = new HashSet<>();
    }

    public Term getTerm() {
        return term;
    }

    public Map<LocalDate, List<Visit>> getPossibleVisits() {
        return possibleVisits;
    }

    public void setPossibleVisitors( Set<Tutor> possibleVisitors ) {
        this.possibleVisitors = possibleVisitors;
    }

    public void setToVisit( Set<Tutor> toVisit ) {
        this.toVisit = toVisit;
    }

    public void setOuterRooms( Set<String> outerRooms ) {
        this.outerRooms = outerRooms;
    }

    public void setOuterTutors( Set<Tutor> outerTutors ) {
        this.outerTutors = outerTutors;
    }

    public void setUnwantedTypes( Set<String> unwantedTypes ) {
        this.unwantedTypes = unwantedTypes;
    }

    public void setFixedVisits( FixedVisit... fixedVisits ) {
        this.fixedVisits.clear();
        this.fixedVisits.addAll( Arrays.asList( fixedVisits ));
    }

    public void setFixedVisits( Collection<Visit> visits ){
        this.fixedVisits.clear();
        this.fixedVisits.addAll( visits.parallelStream()
                .map( v -> new FixedVisit( v.getVisit().getTutor(), v.getVisitor(), v.getVisit().getTime(), v.getVisit().getDate()))
                .collect( Collectors.toList()));
    }

     /**
     * Автоматическое заполнение список посещающих и посещаемых, если они пусты.
     */
    private void checkTutorsLists() {
        if( toVisit.isEmpty()) {
            Iterator<Tutor> tutors = term.getByTutors().keySet().iterator();
            while( tutors.hasNext() ) {
                Tutor tutor = tutors.next();
                if( !outerTutors.contains( tutor ))
                    toVisit.add( tutor );
            }
        }
        if( possibleVisitors.isEmpty()){
            Iterator<Tutor> tutors = term.getByTutors().keySet().iterator();
            while( tutors.hasNext() ) {
                Tutor tutor = tutors.next();
                if( !outerTutors.contains( tutor ))
                    possibleVisitors.add( tutor );
            }
        }
        withoutPossibleVisits.clear();
        withoutPossibleVisits.addAll( toVisit );
    }

    /**
     * Составление расписания посещений.
     * @return расписание в виде множества
     * @throws Exception
     * @param term
     * @param withWeekends
     */
    public Collection<Visit> generateSchedule( Term term, boolean withWeekends ) throws Exception{
        clearLists();
        removeTutorsWithNoLessons( term );
        findAllPossibleVisitsForPeriod( term, withWeekends );
        fillTempSets();
        fillFixedVisits();
        List<Visit> all = new ArrayList<>();
        for( List<Visit> v : possibleVisits.values())
            all.addAll( v );
        printStatistics( all );
        return processAllPossibleVisits( all );
    }

    private void printStatistics( List<Visit> possibleVisits ) {
        System.out.println( "--------------------" );
        System.out.println( "Всего посещений: " + possibleVisits.size() );
        int conflictCount = 0;
        int conflictVisitorCount = 0;
        int conflictTutorCount = 0;
        for( int i = 0 ; i < possibleVisits.size() - 1 ; i++ ){
            for( int j = i+1; j < possibleVisits.size(); j++ ){
                Visit v1 = possibleVisits.get( i );
                Visit v2 = possibleVisits.get( j );
                LocalDate d1 = v1.getVisit().getDate();
                LocalDate d2 = v2.getVisit().getDate();
                TimeInterval ti1 = v1.getVisit().getTime();
                TimeInterval ti2 = v2.getVisit().getTime();
                Tutor t1 = v1.getVisit().getTutor();
                Tutor t2 = v2.getVisit().getTutor();
                Tutor vi1 = v1.getVisitor();
                Tutor vi2 = v2.getVisitor();
                if( d1.equals( d2 ) && ti1.equals( ti2 )) {
                    if( vi1.equals( vi2 )) conflictVisitorCount++;
                    if( t1.equals( t2 )) conflictTutorCount++;
                }
                conflictCount = conflictTutorCount+conflictVisitorCount;
            }
        }
        System.out.println( "Конфликтов: " + conflictCount + " (посещающий: " + conflictVisitorCount + "; преподаватель: " + conflictTutorCount + ")" );
        List<Lesson> lessons = possibleVisits.parallelStream().map( Visit::getVisit ).collect( Collectors.toList());
        Set<String> disciplines = lessons.parallelStream().map( Lesson::getDiscipline ).collect( Collectors.toSet());
        Set<String> types = lessons.parallelStream().map( Lesson::getType ).collect( Collectors.toSet());
        Set<String> groups = lessons.parallelStream().map( Lesson::getGroup ).collect( Collectors.toSet());
        System.out.println( "Дисциплин: " + disciplines.size() );
        System.out.println( "Типов: " + types.size() );
        System.out.println( "Групп: " + groups.size() );
        final Map<String, Integer> optionals = new HashMap<>();
        possibleVisits.forEach( v -> {
            String opt = v.getOptionalRulesSatisfaction().toString();
            for( int i = opt.length(); i < 7; i++ ){
                opt = "0" + opt;
            }
            String optional = new StringBuilder()
                    .append( opt, 0, 1 )
                    .append( opt, 2, 5 )
                    .reverse().toString();
            Integer value = optionals.get( optional );
            optionals.put( optional, value != null ? value+1 : 1 );
        } );
        System.out.println( optionals );

    }

    private void clearLists() {
        result.clear();
        visitorVisits.clear();
        visitedTutors.clear();
        resultLessons.clear();
        tutorDate.clear();
    }

    private void removeTutorsWithNoLessons( Term term ) {
        Set<Tutor> toRemove = new HashSet<>();
        toVisit.stream().forEach( tutor -> {
            if( term.getByTutors().get( tutor ) == null
                    || term.getByTutors().get( tutor ).isEmpty()) toRemove.add( tutor );
        });
        toVisit.removeAll( toRemove );
    }

    /**
     * Добавление в расписание фиксированных посещений.
     */
    private void fillFixedVisits() {
        for( FixedVisit fv : fixedVisits ){
            for( Lesson l : term.getAllLessons()){
                if( l.getTutor().equals( fv.getTutor())
                        && l.getDate().equals( fv.getDate())
                        && l.getTime().strictEquals( fv.getTime())) {
                    l.setTutor( fv.getTutor() );
                    addVisitToSchedule( createVisit( fv.getVisitor(), l ) );  //FIXME может processVisit()
                                                                            //т.к. пока нет проверок?
                }
            }
        }
    }

    /**
     * Составление всех возможных вариантов посещений.
     * @param term пары некоторого {@link Term периода}
     * @param withWeekend рассматривать ли пары в выходные дни
     */
    protected void findAllPossibleVisitsForPeriod( Term term, boolean withWeekend ){
        this.term = term;
        checkTutorsLists();
        possibleVisits.clear();
        for( Map.Entry<LocalDate, Set<Lesson>> entry : term.getByDates().entrySet())
            possibleVisits.put( entry.getKey(), findAllPossibleVisits( entry.getValue(), withWeekend ) );
    }

    /**
     * Заполнение вспомогательных карт.
     */
    private void fillTempSets() {
        for( Tutor visitor : possibleVisitors )
            visitorVisits.putIfAbsent( visitor, 0 );
    }

    /**
     * По списку пар очередного дня составляем список
     * всех возможных посещений.
     * @param lessons список пар дня
     * @return список всех возможных посещений
     */
    private List<Visit> findAllPossibleVisits( Set<Lesson> lessons, boolean withWeekend ) {
        byTutor = new HashMap<>();
        List<Visit> visits = new ArrayList<>();
        // Разбиваем пары по проводящим их преподавателям,
        // оставляя только тех, кто участвует
        for( Lesson lesson : lessons ){
            Tutor tutor = lesson.getTutor();
            if( toVisit.contains( tutor ) || possibleVisitors.contains( tutor )) {
                byTutor.putIfAbsent( tutor, new HashSet<>());
                byTutor.get( tutor ).add( lesson );
            }
        }
        possibleVisitors.forEach( tutor -> byTutor.putIfAbsent( tutor, new HashSet<>() ) );
        // Проходим по всем парам
        for( Lesson lesson : lessons ) if( toVisit.contains( lesson.getTutor() )){
            Tutor tutor = lesson.getTutor();
            if( dateIsOk( lesson.getDate(), withWeekend ))  // Проверка дат
                for( Tutor visitor : byTutor.keySet()){
                    if( possibleVisitors.contains( visitor )
                            && !visitor.equals( tutor )){ // Если преподаватель подходит...
                        if( !hasLesson( visitor, byTutor.get( visitor ), lesson )){ // ...и у него нет пары...
                            addVisit( visits, visitor, lesson, byTutor );
                        }
                    }
                }
        }
        return visits;
    }

    /**
     * Проверка, подходит ли дата. Проверяет день недели.
     * @param date дата
     * @param withWeekend подходят ли выходные
     * @return <code>true</code>, если дата подходит
     */
    private boolean dateIsOk( LocalDate date, boolean withWeekend ) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return withWeekend || // Если с выходными...
                (  !dayOfWeek.equals( DayOfWeek.SATURDAY ) // ...или без
                    && !dayOfWeek.equals( DayOfWeek.SUNDAY ));
    }

    /** Проверка ограничений по датам для посещающих */
    private boolean dateForVisitorIsOk( Tutor visitor, Lesson lesson ) {
        return true;
    }

    /**
     * Проверка, в одном ли месте пары у проверяющего и проверяемого.
     * Если у посещающего нет пар, то считается, что в одном.
     * @param lesson пара проверяемого
     * @param visitorLessons пары посещающего
     * @return в одном ли здании пары
     */
    private boolean atSamePlace( Lesson lesson, Set<Lesson> visitorLessons ){
        if( visitorLessons.isEmpty())
            return true;
        // Проверяем, в главном или нет посещаемая пара...
        String outer = null;
        for( String outerRoom : outerRooms )
            if( lesson.getRoom().contains( outerRoom )) {
                outer = outerRoom;
                break;
            }

        // ...и в главном или нет пары посещающего
        boolean found = false;
        for( Lesson visitorLesson : visitorLessons )
            for( String outerRoom : outerRooms ){
                if( visitorLesson.getRoom().contains( outerRoom )) {
                    found = true;
                    // Если не в главном, но в одном месте у обоих
                    if( outerRoom.equals( outer ))
                        return true;
                }
                if( found ) break;
            }
        // Если в главном у обоих
        if( outer == null && !found )
            return true;
        return false;
    }

    /**
     * Есть ли пара у посещающего в определенное время.
     * @param visitor посещающий
     * @param visitorLessons список всех пар посещающего
     * @param lesson пара (для даты и времени проведения)
     * @return <code>true</code>, если есть пара
     */
    private boolean hasLesson( Tutor visitor, Set<Lesson> visitorLessons, Lesson lesson ){
        Lesson temp = lesson.copy();
        temp.setTutor( visitor );
        return visitorLessons.contains( temp );
    }

    /**
     * Добавление посещения в список и убирание посещенного из
     * {@link #withoutPossibleVisits списка непосещенных}. Здесь же
     * в посещение вносится информация о соответствии опциональным правилам.
     * @param visits список посещений
     * @param visitor посещающий
     * @param lesson пара
     * @param lessonsForWorkRule
     */
    private void addVisit( List<Visit> visits, Tutor visitor, Lesson lesson,
                           Map<Tutor, Set<Lesson>> lessonsForWorkRule ) {
        Visit visit = new Visit( visitor, lesson );
        calculateOptionalSatisfaction( visit, lessonsForWorkRule );
        visits.add( visit );
        withoutPossibleVisits.remove( lesson.getTutor() );
    }

    /**
     * Подсчет и внесение информации в посещение о соответствии опциональным правилам.
     * @param visit посещение
     * @param lessonsForWorkRule
     */
    private void calculateOptionalSatisfaction( Visit visit,
                                                Map<Tutor, Set<Lesson>> lessonsForWorkRule ) {
        Tutor visitor = visit.getVisitor();
        Lesson lesson = visit.getVisit();
        Tutor tutor = lesson.getTutor();
        boolean isOuterRoom = isOuterRoom( lesson.getRoom());
        if( dateForVisitorIsOk( visitor, lesson ))
            visit.satisfyOptionalRule( OptionalRules.Rules.DATE_FOR_VISITOR_IS_OK.priority());
        if( !isOuterRoom || visitor.isOuterBoss())
            visit.satisfyOptionalRule( OptionalRules.Rules.BOSS_OUTER_VISIT.priority());
        if( typeIsOk( lesson.getName() ))
            visit.satisfyOptionalRule( OptionalRules.Rules.LESSON_TYPE.priority() );
        if( visitor.canVisit( tutor ))
            visit.satisfyOptionalRule( OptionalRules.Rules.TUTOR_STATUS.priority() );
        if( !tutor.isBoss() )
            visit.satisfyOptionalRule( OptionalRules.Rules.DO_NOT_VISIT_BOSS.priority() );
        if( atSamePlace( lesson, lessonsForWorkRule.get( visitor ) ))
            visit.satisfyOptionalRule( OptionalRules.Rules.SAME_PLACE.priority() );
        if( !lessonsForWorkRule.get( visitor ).isEmpty())
            visit.satisfyOptionalRule( OptionalRules.Rules.VISITOR_WORKS.priority() );
    }

    private boolean isOuterRoom( String room ) {
        for( String outerRoom : outerRooms )
            if( room.contains( outerRoom ))
                return true;
        return false;
    }

    /**
     * Отбор посещений для расписания посещений из всех возможных
     * вариантов с учетом ограничений.
     * @param visits список всех возможных посещений
     * @return расписание посещений в виде множества
     */
    private Collection<Visit> processAllPossibleVisits( List<Visit> visits ){
        List<LessonCount> lessonCounts = countPossibleVisitedLessons( visits, true );

        boolean needMore = true;
        boolean added;
        // Проходим по списку преподавателей, пары которых нужно посетить
        while( needMore ) {
            added = false;
            Iterator<LessonCount> counters = lessonCounts.iterator();
            while( counters.hasNext() ) {
                boolean wasAdded = processTutorPossibleVisits( counters.next(), visits );
                if( wasAdded )
                    added = true;
            }
            needMore = checkIfNotReady();
            if( !added ) break;
        }

        // Проходим по оставшимся без посещений, не учитывая число посещений у посещающих
        if( needMore ) System.out.println( "Второй подход к поиску" );
        while( needMore ){
            Set<Tutor> notVisited = new HashSet<>( toVisit );
            notVisited.removeAll( visitedTutors );

            added = false;
            Iterator<LessonCount> counters = lessonCounts.iterator();
            while( counters.hasNext() ) {
                LessonCount c = counters.next();
                if( notVisited.contains( c.getTutor())) {
                    boolean wasAdded = processTutorPossibleVisits( c, visits, false );
                    if( wasAdded )
                        added = true;
                }
            }
            needMore = checkIfNotReady();
            if( !added ) break;
        }

        // Это уже, вероятно, не понадобится
        if( needMore ) System.out.println( "Третий подход к поиску" );
        while( needMore ){
            added = false;
            Set<Tutor> notVisited = new HashSet<>( toVisit );
            notVisited.removeAll( visitedTutors );
            for( Tutor tutor : notVisited ){
                boolean wasAdded = processTutorNotVisited( tutor, possibleVisitors );
                if( wasAdded )
                    added = true;
            }
            needMore = checkIfNotReady();
            if( !added ) break;
        }
        return result;
    }

    /**
     * Подсчет числа возможных для посещения пар у каждого преподавателя.
     * @param visits список посещений
     * @param withNulls заносить ди тех, у кого нет ни одной пары для посещения
     * @return список двоек (преподаватель, количество пар)
     */
    private List<LessonCount> countPossibleVisitedLessons( List<Visit> visits, boolean withNulls ){
        Set<Lesson> visitedLessons = new HashSet<>(  );
        for( Visit v : visits )
            visitedLessons.add( v.getVisit() );

        // Заполняем вспомогательную карту
        Map<Tutor, LessonCount> visited = new HashMap<>();
        Iterator<Lesson> iterator = visitedLessons.iterator();
        while( iterator.hasNext() ){
            Tutor tutor = iterator.next().getTutor();
            visited.putIfAbsent( tutor, new LessonCount( tutor, 0 ) );
            visited.get( tutor ).increase();
        }

        // Список подходит, потому что не будет повторяющихся значений
        List<LessonCount> counters = new ArrayList<>();
        for( LessonCount count : visited.values())
            counters.add( count );

        // Добавляем тех, у кого ноль возможных посещений
        if( withNulls ) for( Tutor tutor : withoutPossibleVisits ) {
            LessonCount counter = new LessonCount( tutor, 0 );
            if( !counters.contains( counter ))
                counters.add( counter );
        }

        // Сортируем по возрастанию
        Collections.sort( counters );
        return counters;
    }

    // Если нет пересекающихся посещений, то должно было добавиться на предыдущем этапе,
    // а если есть, то равные посещения, и в Set не добавится. Поэтому заменил Set на List для result
    private boolean processTutorNotVisited( Tutor tutor, Set<Tutor> possibleVisitors ){
        // Список начальников
        Set<Tutor> bosses = new LinkedHashSet<>();
        for( Tutor t : possibleVisitors )
            if( t.isOuterBoss()) bosses.add( t );
        for( Tutor t : possibleVisitors )
            if( t.fromBosses()) bosses.add( t );

        // Заполняем начальниками без проверки наличия у них посещений
        Set<Lesson> lessons = term.getByTutors().get( tutor );
        for( Lesson lesson : lessons ){
            for( Tutor visitor : bosses ){
                // Нет проверки на наличие посещений у посещающего, что логично,
                // учитывая, что это последняя попытка добавить.
                // Значит помешало добавленное посещение.
                // Его можно будет убрать при оптимизации.
                if( !hasLesson( visitor, term.getByTutors().get( visitor ), lesson )) {
                    return addVisitToSchedule( createVisit( visitor, lesson ));
                }
            }
        }
        return false;
    }

    private boolean processTutorPossibleVisits( LessonCount counter, List<Visit> allVisits ) {
        return processTutorPossibleVisits( counter, allVisits, true );
    }

    /**
     * Поиск очередной пары, которую можно добавить в расписание посещений,
     * среди всех вариантов посещения пар данного преподавателя.
     * @param counter количество пар преподавателя, которые можно посетить
     * @param allVisits все возможные варианты посещений
     * @return были ли добавлены пары
     */
    private boolean processTutorPossibleVisits( LessonCount counter, List<Visit> allVisits, boolean checkVisitorVisitsCount ) {
        Tutor tutor = counter.getTutor();

        if( counter.isNill()) {
            JOptionPane.showMessageDialog( null, counter.getTutor().getName() + ": никто не может посетить",
                    "Проблема", JOptionPane.ERROR_MESSAGE );
            return false;
        }

        // Выбираем только посещения данного преподавателя
        List<Visit> tutorVisits = allVisits.parallelStream().filter( v -> v.getVisit().getTutor().equals( tutor )).collect( Collectors.toList());

        // Находим все посещения пар преподавателя
        List<OptionalRules.Rules> rules = new ArrayList<>( Arrays.asList( OptionalRules.Rules.values()));
        OptionalRules.Rules[] rulesArray = OptionalRules.Rules.values();
        for( int j = 0; j < rulesArray.length+1; j++ ){
            // Заполняем список приоритетов правил
            int[] priorities = new int[rules.size()];
            for( int i = 0; i < rules.size(); i++ )
                priorities[i] = rules.get( i ).priority();
            // Проходим по всем посещениям преподавателя, которые удовлетворяют правилам
            Pair<Visit, Long> bestPossible = null;
            for( Visit v : tutorVisits ) if( v.satisfiesOptionalRules( priorities )) {
                bestPossible = processVisit( v, bestPossible, checkVisitorVisitsCount );
            }
            if( bestPossible != null ) {
                addVisitToSchedule( bestPossible.getKey() );
                return true;
            } else{
                if( j < rulesArray.length )
                    rules.remove( rulesArray[j] );
            }
        }
        return false;
    }

    private boolean typeIsOk( String name ) {
        for( String type : unwantedTypes )
            if( name.toLowerCase().contains( type.toLowerCase()))
                return false;
        return true;
    }

    private Pair<Visit, Long> processVisit( Visit v, Pair<Visit, Long> bestPossible, boolean checkVisitorVisitsCount ) {
        if( checkVisitorVisitsCount && !visitorHasVisitsLeft( v.getVisitor() ))
            return bestPossible;

        Long metrics = Long.MAX_VALUE;
        for( Visit resultVisit : result )
            metrics = Math.min( metrics, VisitsMetrics.evaluate( resultVisit, v ));

        if( !alreadyVisitedThatDay( v.getVisit().getTutor(), v.getVisit().getDate() )
                && !result.contains( v ) && !resultLessons.contains( v.getVisit())
                && ( bestPossible == null || metrics > bestPossible.getValue() ))
            return new Pair<>( v, metrics );
        else
            return bestPossible;
    }

    /**
     * Проверка того, что преподавателя уже посетили в данный день.
     * @param tutor преподаватель
     * @param date дата
     * @return <code>true</code>, если посетили
     */
    private boolean alreadyVisitedThatDay( Tutor tutor, LocalDate date ){
        return tutorDate.contains( new Pair<>( tutor, date ));
    }

    private boolean addVisitToSchedule( Visit visit ){
        Lesson lesson = visit.getVisit();
        Tutor tutor = lesson.getTutor();
        Tutor visitor = visit.getVisitor();

        if( result.add( visit )) {
            Integer perVisitor = visitorVisits.get( visitor );
            if( perVisitor == null ) perVisitor = 0;
            resultLessons.add( visit.getVisit() );
            tutorDate.add( new Pair<>( tutor, visit.getVisit().getDate() ) );
            if( dateForVisitorIsOk( visitor, visit.getVisit() ))
                visitorVisits.put( visitor, perVisitor + 1 );
            visitedTutors.add( tutor );
            return true;
        }
        else return false;
    }

    /**
     * Проверка выполненности всех минимальных условий готовности расписания.
     * @return <code>true</code>, если не готово
     */
    private boolean checkIfNotReady(){
        // По посещаемым
        Set<Tutor> notVisited = new HashSet<>( toVisit );
        notVisited.removeAll( visitedTutors );
        if( !notVisited.isEmpty())
            return true;
        // По посещениям
        Set<Tutor> notVisit = new HashSet<>( possibleVisitors );
        for( Tutor key : visitorVisits.keySet())
            if( visitCountIsOk( key, visitorVisits.get( key ) ))
                notVisit.remove( key );
        if( !notVisit.isEmpty())
            return true;
        return false;
    }

    private boolean visitCountIsOk( Tutor tutor, Integer count ){
        return tutor.fromBosses() ?
                count >= MIN_VISITS_PER_BOSS :
                count >= VISITS_PER_TUTOR;
    }

    /**
     * Создание посещения с подсчетом выполнимости дополнительных правил.
     * @return
     */
    private Visit createVisit( Tutor visitor, Lesson lesson){
        Visit v = new Visit( visitor, lesson );
        calculateOptionalSatisfaction( v, byTutor );
        return v;
    }

    /**
     * Посещающему еще нужны посещения.
     * @param visitor посещающий
     * @return <code>true</code>, если нужны посещения
     */
    private boolean visitorHasVisitsLeft( Tutor visitor ){
        Integer perVisitor = visitorVisits.get( visitor );
        return visitor.fromBosses() ?
                perVisitor < MIN_VISITS_PER_BOSS : perVisitor < VISITS_PER_TUTOR;
    }

}
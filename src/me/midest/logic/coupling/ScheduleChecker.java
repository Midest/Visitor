package me.midest.logic.coupling;

import me.midest.model.Tutor;
import me.midest.model.Visit;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ScheduleChecker {

    /**
     * Проверяем допустимость расписания. Конфликты по совпадению одной из троек:
     * 1. Посещающий - дата - время.
     * 2. Преподаватель - дата - время.
     * @param visits расписание
     * @return <code>true</code>, если расписание не содержит конфликтов.
     */
    public Feasibility feasibilityCheck( Collection<Visit> visits ){
        int visitorClashCount;
        int tutorClashCount;
        for( Visit v1 : visits ){
            visitorClashCount = 0;
            tutorClashCount = 0;
            for( Visit v2 : visits ) {
                if( v1.equals( v2 ) ) visitorClashCount++;
                if( v1.getVisit().equals( v2.getVisit() )) tutorClashCount++;
            }
            if( visitorClashCount > 1 && tutorClashCount > 1 )
                return Feasibility.CLASH_ALL;
            else if( visitorClashCount > 1 )
                return Feasibility.CLASH_VISITOR;
            else if( tutorClashCount > 1 )
                return Feasibility.CLASH_TUTOR;
        }
        return Feasibility.NO_CLASH;
    }

    public Completeness completenessCheck( Collection<Visit> visits, Collection<Tutor> tutors,
                                                 int visitsPerVisitor, int visitsPerBoss ){
        Map<Tutor, Integer> visitsCount = tutors.stream()
                .filter( Tutor::isVisitor )
                .collect( Collectors.toMap( t -> t, t -> 0 ));
        List<Tutor> notVisited = tutors.stream()
                .filter( Tutor::isVisitee )
                .collect( Collectors.toList());
        // Считаем
        for( Visit v : visits ){
            Integer count = visitsCount.get( v.getVisitor() );
            visitsCount.put( v.getVisitor(), ++count );
            notVisited.remove( v.getVisit().getTutor());
        }
        boolean EvV = notVisited.isEmpty();
        for( Map.Entry<Tutor, Integer> e : visitsCount.entrySet()){
            if(( e.getKey().fromBosses() && e.getValue() < visitsPerBoss )
                    || ( !e.getKey().fromBosses() && e.getValue() < visitsPerVisitor ))
                return EvV ? Completeness.EVERYONE_VISITED : Completeness.NOTHING;
        }
        return EvV ? Completeness.FULL : Completeness.ENOUGH_VISITS;
    }

    public boolean isFeasible( Feasibility status ){
        return Feasibility.NO_CLASH.equals( status );
    }

    public boolean isComplete( Completeness status ){
        return Completeness.FULL.equals( status );
    }

    public enum Feasibility{
        NO_CLASH( "Нет конфликтов" ),
        CLASH_VISITOR( "Одновременные посещения у проверяющего" ),
        CLASH_TUTOR( "Несколько проверяющих на занятие" ),
        CLASH_ALL( "Одновременные посещения у проверяющего и несколько проверяющих на занятие" );

        private String text;
        Feasibility( String text ){
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    public enum Completeness{
        FULL( "Все посещены, проверяющим достаточно посещений" ),
        EVERYONE_VISITED( "Все посещены, проверяющим недостаточно посещений" ),
        ENOUGH_VISITS( "Не все посещены, проверяющим достаточно посещений" ),
        NOTHING( "Не все посещены, проверяющим недостаточно посещений" );

        private String text;
        Completeness( String text ){
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

}

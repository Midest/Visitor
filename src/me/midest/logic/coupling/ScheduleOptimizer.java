package me.midest.logic.coupling;

import me.midest.model.Tutor;
import me.midest.model.Visit;

import java.util.*;

public class ScheduleOptimizer {

    private ScheduleChecker checker;
    private int visitsPerTutor;
    private int minVisitsPerBoss;

    public List<Visit> optimize( Collection<Visit> visits, Collection<Tutor> tutors, ScheduleChecker checker,
                          Collection<Visit> fixed,
                          int visitsPerTutor, int minVisitsPerBoss ){
        List<Visit> schedule = new ArrayList<>( visits );
        this.checker = checker;
        this.visitsPerTutor = visitsPerTutor;
        this.minVisitsPerBoss = minVisitsPerBoss;

        while( removeRedundant( schedule, tutors, fixed )){}
        return schedule;
    }

    private boolean isOk( Collection<Visit> visits, Collection<Tutor> tutors ) {
        return checker.isFeasible( checker.feasibilityCheck( visits ))
                && checker.isComplete( checker.completenessCheck( visits, tutors, visitsPerTutor, minVisitsPerBoss ));
    }

    private boolean removeRedundant( Collection<Visit> visits, Collection<Tutor> tutors, Collection<Visit> fixed ){
        // Находим худшее по метрике посещение
        Visit redundant = findWorstRedundant( visits, tutors, fixed );
        if( redundant == null )
            return false;

        // Рейтинг текущего расписания
        double currentValue = VisitsValue.count( visits );

        // Выкидываем худшее лишнее и пересчитываем рейтинг
        List<Visit> wOut = new ArrayList<>( visits );
        wOut.remove( redundant );
        double newValue = VisitsValue.count( wOut );
        if( newValue > currentValue ) {
            visits.remove( redundant );
            return true;
        }
        return false;
    }

    private Visit findWorstRedundant( Collection<Visit> visits, Collection<Tutor> tutors, Collection<Visit> fixed ) {
        final Map<Visit, Long> redundant = new HashMap<>();
        visits.forEach( v -> {
            if( !fixed.contains( v )) {
                List<Visit> wOut = new ArrayList<>( visits );
                wOut.remove( v );
                if( isOk( wOut, tutors ) )
                    redundant.put( v, VisitsMetrics.evaluateLeast( v, wOut ) );
            }
        } );
        Optional<Map.Entry<Visit, Long>> visit = redundant.entrySet().stream().min( Comparator.comparingLong( Map.Entry::getValue ));
        return visit.map( Map.Entry::getKey ).orElse( null );
    }

}

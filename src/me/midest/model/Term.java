package me.midest.model;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;

public class Term {

    private Map<Tutor, Set<Lesson>> byTutors;
    private Map<LocalDate, Set<Lesson>> byDates;
    private Set<Lesson> allLessons;

    private Set<Month> months;

    public Term(){
        byTutors = new HashMap<>();
        byDates = new HashMap<>();
        allLessons = new HashSet<>();
        months = new HashSet<>();
    }

    public Map<Tutor, Set<Lesson>> getByTutors() {
        return byTutors;
    }

    public Map<LocalDate, Set<Lesson>> getByDates() {
        return byDates;
    }

    public Set<Lesson> getAllLessons() {
        return allLessons;
    }

    public Set<Month> getMonths() {
        return months;
    }

    public Integer getMonthCount() {
        return months.size();
    }

    public void putLesson( Lesson lesson ){
        Tutor tutor = lesson.getTutor();
        LocalDate date = lesson.getDate();
        if( tutor == null || date == null )
            return;
        // По преподавателю
        if( byTutors.get( tutor ) == null )
            byTutors.put( tutor, new HashSet<>());
        Set<Lesson> list = byTutors.get( tutor );
        list.add( lesson );
        // По дате
        if( byDates.get( date ) == null )
            byDates.put( date, new HashSet<>());
        list = byDates.get( date );
        list.add( lesson );
        allLessons.add( lesson );
    }

    public void putLessons( List<Lesson> lessons ){
        for( Lesson lesson : lessons )
            putLesson( lesson );
        updateMonths();
    }

    private void updateMonths() {
        Iterator<LocalDate> dates = byDates.keySet().iterator();
        while( dates.hasNext())
            months.add( dates.next().getMonth());
    }

    public Set<Lesson> getTutorLessonsByDate( Tutor tutor, LocalDate date ) {
        Set<Lesson> set = new HashSet<>();
        for( Lesson lesson : byTutors.get( tutor )) if( lesson.getDate().equals( date ))
            set.add( lesson );
        return set;
    }
}

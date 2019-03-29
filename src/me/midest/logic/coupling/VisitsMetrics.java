package me.midest.logic.coupling;

import me.midest.model.Lesson;
import me.midest.model.Visit;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class VisitsMetrics {

    /**
     * Список со значениями метрики для пар посещений.
     */
    private static Map<Integer, Long> hashes = new HashMap<>();

    /**
     * Хэш пары посещений. Зависит от их строкового представления,
     * поскольку при использовании моего {@link Visit#hashCode()},
     * даже при попытке расширить его суммой с хэшом всех нужных параметров,
     * случаются коллизии. А так их вроде нет.
     * @param first первое посещение
     * @param second второе посещение
     * @return числовой хэш пары
     * @see Visit#toString()
     */
    private static int hash( Visit first, Visit second ){
        return first.toString().hashCode() + 31 * second.toString().hashCode();
    }

    /**
     * Нахождение максимального различия между координатами.
     * @param first посещение один
     * @param second посещение два
     * @return максимум разностей координат
     */
    public static Long evaluateMax( Visit first, Visit second ){
        Lesson l1 = first.getVisit();
        Lesson l2 = second.getVisit();
        Integer max = l1.getTutor().equals( l2.getTutor() ) ? 0 : Weight.TUTOR.getWeight();
        max = Math.max( max, l1.getDiscipline().equals( l2.getDiscipline()) ? 0 : Weight.DISCIPLINE.getWeight() );
        max = Math.max( max, l1.getType().equals( l2.getType()) ? 0 : Weight.LESSON_TYPE.getWeight() );
        max = Math.max( max, first.getVisitor().equals( second.getVisitor() ) ? 0 : Weight.VISITOR.getWeight() );
        max = Math.max( max, l1.getGroup().equals( l2.getGroup()) ? 0 : Weight.GROUP.getWeight() );
        Long maxL = Long.valueOf( max );
        Long days = Duration.between( l1.getDate().atStartOfDay(), l2.getDate().atStartOfDay() )
                .abs().getSeconds() / SECONDS_PER_DAY;
        maxL = Math.max( maxL, DATE_WEIGHT_MULTIPLIER * days );
        return maxL;
    }

    /**
     * Нахождение суммы различий с весами между координатами.
     * @param first посещение один
     * @param second посещение два
     * @return сумма разностей (с весами) координат
     */
    public static Long evaluate( Visit first, Visit second ){
        Integer hash = hash( first, second );
        Long maxL = hashes.get( hash );
        if( maxL != null )
            return maxL;

        Lesson l1 = first.getVisit();
        Lesson l2 = second.getVisit();
        Integer max = l1.getTutor().equals( l2.getTutor() ) ? 0 : Weight.TUTOR.getWeight();
        max += l1.getDiscipline().equals( l2.getDiscipline()) ? 0 : Weight.DISCIPLINE.getWeight();
        max += l1.getType().equals( l2.getType()) ? 0 : Weight.LESSON_TYPE.getWeight();
        max += first.getVisitor().equals( second.getVisitor() ) ? 0 : Weight.VISITOR.getWeight();
        max += l1.getGroup().equals( l2.getGroup()) ? 0 : Weight.GROUP.getWeight();
        maxL = Long.valueOf( max );
        Long days = Duration.between( l1.getDate().atStartOfDay(), l2.getDate().atStartOfDay() )
                .abs().getSeconds() / SECONDS_PER_DAY;
        maxL += DATE_WEIGHT_MULTIPLIER * days;
        hashes.put( hash, maxL );
        return maxL;
    }

    /**
     * Нахождение минимального значения метрики у данного
     * посещения с остальными переданными.
     * @param visit данное посещение
     * @param all список для сравнения
     * @return минимальное значение метрики
     */
    public static Long evaluateLeast( Visit visit, Collection<Visit> all ){
        Long metrics = Long.MAX_VALUE;
        for( Visit resultVisit : all )
            metrics = Math.min( metrics, VisitsMetrics.evaluate( resultVisit, visit ));
        return metrics;
    }

    private static final Long SECONDS_PER_DAY = 24*60*60L;
    private static final Integer DATE_WEIGHT_MULTIPLIER = 1;

    enum Weight{
        TUTOR(500),
        DISCIPLINE(50),
        LESSON_TYPE(25),
        VISITOR(15),
        GROUP(5)
        ;

        private int weight;
        Weight( int weight ){
            this.weight = weight;
        }

        public int getWeight(){
            return weight;
        }
    }

}

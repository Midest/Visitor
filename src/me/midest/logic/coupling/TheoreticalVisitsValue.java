package me.midest.logic.coupling;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static me.midest.model.OptionalRules.Rules;
import static me.midest.logic.coupling.VisitsMetrics.Weight;

/**
 * Рассчет верхней границы значения целевой функции для имеющихся данных.
 */
public class TheoreticalVisitsValue {

    /**
     * Вычисление верхней границы значения целевой функции.
     * @param first самая ранняя возможная дата для посещения
     * @param last самая поздняя возможная дата для посещения
     * @param visitsCount число посещений
     * @param
     * @return значение верхней границы целевой функции
     */
    public static double upperBound( LocalDate first, LocalDate last,
                                     int bossVisitors, int otherVisitors, int visitsCount,
                                     int uniqueDisciplines, int uniqueLessonTypes, int uniqueGroups ){
        return ( maxWeight( between( first, last )) * binomial( visitsCount, 2 )
                    - daysCut( between( first, last ), visitsCount )
                    - visitorsCut( Weight.VISITOR.getWeight(), bossVisitors, otherVisitors, visitsCount )
                    - otherParamCut( Weight.DISCIPLINE.getWeight(), uniqueDisciplines, visitsCount )
                    - otherParamCut( Weight.LESSON_TYPE.getWeight(), uniqueLessonTypes, visitsCount )
                    - otherParamCut( Weight.GROUP.getWeight(), uniqueGroups, visitsCount ) )
                / binomial( visitsCount, 2 ) + maxR( visitsCount );
    }

    /**
     * Грубая прикидка верхней границы значения целевой функции.
     * @param first самая ранняя возможная дата для посещения
     * @param last самая поздняя возможная дата для посещения
     * @param visitsCount число посещений
     * @return значение верхней границы целевой функции
     */
    public static double roughEstimate( LocalDate first, LocalDate last, int visitsCount ){
        return maxWeight( between( first, last )) + maxR( visitsCount );
    }

    private static int between( LocalDate first, LocalDate last ) {
        return (int) ChronoUnit.DAYS.between( first, last );
    }

    /**
     * Максимальная разница между значениями параметров пары посещений.
     * @return максимальный вес разницы параметров посещений в целевой функции
     */
    private static int maxWeight( int days ){
        int maxWeight = 0;
        for( Weight w : Weight.values())
            maxWeight+= w.getWeight();
        return maxWeight + days;
    }

    /**
     * Максимальная выполнимость дополнительных правил у посещений, умноженная на
     * коэффициент дельта и деленная на число посещений.
     * @return максимальный вес выполнимости дополнительных правил в целевой функции для данного числа посещений
     */
    private static double maxR( int visitsCount ){
        int maxRules = 0;
        for( Rules r : Rules.values())
            maxRules+= r.value();
        return ( VisitsValue.delta() * maxRules ) / visitsCount;
    }

    /**
     * Значение снижения верхней границы целевой функции по параметру "дней между".
     * @param days дней между крайними посещениями
     * @param visitsCount число посещений
     * @return значение снижения верхней границы целевой функции для данной разницы дней и числа посещений
     */
    private static double daysCut( int days, int visitsCount ){
        return days * ( binomial( visitsCount - (visitsCount/2), 2 ) + binomial( visitsCount/2, 2 ) );
    }

    /**
     * Значение снижения верхней границы целевой функции по посещающий.
     * @param weight вес параметра
     * @param bossVisitors число посещающих руководителей
     * @param otherVisitors число остальных посещающих
     * @param visitsCount число посещений
     * @return значение снижения верхней границы целевой функции для данного числа посещающих
     */
    private static double visitorsCut( int weight, int bossVisitors, int otherVisitors, int visitsCount ){
        int vpt = TermCoupling.VISITS_PER_TUTOR;
        int vpb = TermCoupling.MIN_VISITS_PER_BOSS;
        if( vpb < vpt )
            return 0;
        int minVisits = bossVisitors * vpb + otherVisitors * vpt;
        if( visitsCount < minVisits )
            return 0;
        int excessVisits = visitsCount - minVisits;
        if( excessVisits == 0 )
            return weight * ( bossVisitors * binomial( vpb, 2 ) + otherVisitors * binomial( vpt, 2 ) );
        int otherFiller = (vpb-vpt) * otherVisitors;
        if( excessVisits < otherFiller ){
                int l = excessVisits % otherVisitors;
                int p = excessVisits / otherVisitors;
                return weight * ( bossVisitors * binomial( vpb, 2 )
                        + l * binomial( vpt + p + 1, 2 )
                        + ( otherVisitors - l ) * binomial( vpt + p, 2 ) );
        }
        else{
            int allFiller = excessVisits - otherFiller;
            int allVisitors = bossVisitors + otherVisitors;
            if( allFiller == 0 )
                return weight * allVisitors * binomial( vpb, 2 );
            else{
                int l = allFiller % allVisitors;
                int p = allFiller / allVisitors;
                return weight * ( l * binomial( vpb + p + 1, 2 )
                        + ( allVisitors - l ) * binomial( vpb + p, 2 ) );
            }

        }
    }

    /**
     * Значение снижения верхней границы целевой функции по стандартным параметрам.
     * @param weight вес параметра
     * @param uniqueValuesCount число уникальных значений параметра
     * @param visitsCount число посещений
     * @return значение снижения верхней границы целевой функции
     */
    private static double otherParamCut( int weight, int uniqueValuesCount, int visitsCount ){
        if( weight == 0 ) return 0;
        int i = uniqueValuesCount;
        int m = visitsCount;
        int l = m % i;
        return weight * ( binomial( m/i, 2 ) * (i-l) + binomial( m/i + 1, 2 ) * l  );
    }

    /**
     * Число сочетаний из {@code n} по {@code k}.
     * @param n всего
     * @param k выбор
     * @return число сочетаний
     */
    private static long binomial( int n, int k ){
        if( n < k )
            return 0L;
        long b = 1L;
        if( k > n-k )
            k = n-k;
        for( int i = 1, t = n; i < k + 1; i++, t-- )
            b = b * t / i;
        return b;
    }

}

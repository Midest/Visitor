package me.midest.logic.coupling;

import me.midest.model.Visit;

import java.util.Collection;

import static me.midest.model.OptionalRules.Rules;

public class VisitsValue {

    /**
     * Считает и возвращает значение целевой функции для данного списка посещений.
     * @param visits список посещений (с указанной выполнимостью дополнительных правил)
     * @return значение целевой функции для переданного списка
     */
    public static double count( Collection<? extends Visit> visits ){
        if( visits.size() < 2 )
            return 0;
        int vSize = visits.size();
        int vPairsSize = vSize * ( vSize - 1 ) / 2;
        double L = allPairsSum( visits )/ vPairsSize;
        double R = rulesSum( visits ) / vSize;
        return L + ( DELTA * R / vSize );
    }

    private static long rulesSum( Collection<? extends Visit> visits ) {
        long sum = 0L;
        for( Visit v : visits )
            for( Rules r : Rules.values())
                if( v.satisfiesOptionalRules( r.priority()))
                    sum+= r.value();
        return sum;
    }

    private static long allPairsSum(Collection<? extends Visit> visits ) {
        int k = visits.size();
        Visit[] visitsArray = new Visit[k];
        visitsArray = visits.toArray( visitsArray );
        long sum = 0L;
        for( int i = 0; i < k-1; i++ )
            for( int j = i+1; j < k; j++ )
                sum += VisitsMetrics.evaluate(visitsArray[i], visitsArray[j]);
        return sum;
    }

    private static final int DELTA = 15;

}

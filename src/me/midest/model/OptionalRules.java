package me.midest.model;

public class OptionalRules {

    private int ruleSatisfaction;

    public OptionalRules(){
        this.ruleSatisfaction = 0;
    }

    public void add( int rulePriority ){
        ruleSatisfaction = ruleSatisfaction | getRule( rulePriority );
    }

    public boolean satisfies( int rulePriority ){
        return ( ruleSatisfaction | getRule( rulePriority )) == ruleSatisfaction;
    }

    public boolean satisfies( int... rulePriorities ){
        for( int priority : rulePriorities )
            if( !satisfies( priority ))
                return false;
        return true;
    }

    public static int ONE = 0b1;

    public static OptionalRules get( int... priorities ){
        OptionalRules rules = new OptionalRules();
        for( int p : priorities )
            rules.add( p );
        return rules;
    }

    public static int getRule( int rulePriority ){
        return ONE << rulePriority;
    }

    @Override
    public String toString() {
        return Integer.toBinaryString( ruleSatisfaction );
    }

    /**
     * Список опциональных правил с их приоритетом.
     */
    public enum Rules{
        VISITOR_WORKS(6, 50),
        SAME_PLACE(5, 0),
        DO_NOT_VISIT_BOSS(4, 75),
        TUTOR_STATUS(3, 100),
        LESSON_TYPE(2, 150),
        DATE_FOR_VISITOR_IS_OK(1, 0),
        BOSS_OUTER_VISIT(0, 0),
        ;

        private int priority;
        private int value;

        public int priority() {
            return priority;
        }

        public int value() {
            return value;
        }

        Rules( int priority, int value ){
            this.priority = priority;
            this.value = value;
        }

    }

}

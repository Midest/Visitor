package me.midest.model;

public class Visit implements EqualsGradations {

    private Tutor visitor;
    private Lesson visit;
    private OptionalRules optionalRulesSatisfaction;

    public Visit( Tutor visitor, Lesson visit ){
        optionalRulesSatisfaction = new OptionalRules();
        setVisitor( visitor );
        setVisit( visit );
    }

    public OptionalRules getOptionalRulesSatisfaction() {
        return optionalRulesSatisfaction;
    }

    public Tutor getVisitor() {
        return visitor;
    }

    public void setVisitor( Tutor visitor ) {
        this.visitor = visitor;
    }

    public Lesson getVisit() {
        return visit;
    }

    public void setVisit( Lesson visit ) {
        this.visit = visit;
    }

    public void satisfyOptionalRule( int rulePriority ){
        optionalRulesSatisfaction.add( rulePriority );
    }

    public boolean satisfiesOptionalRules( int... rulePriority ) {
        return optionalRulesSatisfaction.satisfies( rulePriority );
    }

    @Override
    public boolean equals( Object other ) {
        if( this == other ) return true;
        if( other == null ) return false;
        if( !(other instanceof Visit)) return false;
        final Visit that = (Visit)other;
        if( !compare( this.getVisitor(), that.getVisitor()))
            return false;
        if( this.getVisit() == null || that.getVisit() == null )
            return false;
        if( !compare( this.getVisit().getDate(), that.getVisit().getDate()))
            return false;
        if( !compare( this.getVisit().getTime(), that.getVisit().getTime()))
            return false;
        return true;
    }

    /**
     * На случай, когда нужно точное сравнение времени
     * - {@link TimeInterval#strictEquals(TimeInterval)}
     * и урока - {@link Lesson#deepEquals(Object)}.
     * @param other
     * @return
     */
    public boolean deepEquals( Object other ) {
        if( this == other ) return true;
        if( other == null ) return false;
        if( !(other instanceof Visit)) return false;
        final Visit that = (Visit)other;
        return this.equals( that )
                && this.getVisit().getTime().strictEquals( that.getVisit().getTime())
                && this.getVisit().deepEquals( that.getVisit() );
    }

    @Override
    public int hashCode() {
        int result = 14;
        int hcInt = 0;
        hcInt =	( getVisitor() != null ? getVisitor().hashCode() : 0 );
        result = 29 * result + hcInt;
        if( getVisit() != null ) {
            hcInt = ( getVisit().getDate() != null ? getVisit().getDate().hashCode() : 0 );
                result = 29 * result + hcInt;
            hcInt = ( getVisit().getTime() != null ? getVisit().getTime().hashCode() : 0 );
                result = 29 * result + hcInt;
        }
        return result;
    }

    private boolean compare( Object o1, Object o2 ){
        if( o1 != null ) return o1.equals(o2);
        else return o2 == null;
    }

    @Override
    public String toString() {
        return visitor + " может посетить " + visit;
    }
}

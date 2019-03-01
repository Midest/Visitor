package me.midest.model;

public class LessonCount implements Comparable<LessonCount> {

    Tutor tutor;
    Integer count;

    public LessonCount( Tutor tutor, Integer count ){
        this.tutor = tutor;
        this.count = count;
    }

    public Integer getCount() {
        return count;
    }

    public Tutor getTutor() {
        return tutor;
    }

    @Override
    public int compareTo( LessonCount that ) {
        return this.count.compareTo( that.count ) ;
    }

    public void increase() {
        count++;
    }


    public boolean isNill() {
        return getCount() == 0 ;
    }

    @Override
    public boolean equals( Object obj ) {
        if( this == obj )
            return true;
        if( obj instanceof LessonCount ) {
            LessonCount that = (LessonCount) obj;
            if( tutor != null ? !tutor.equals( that.tutor ) : that.tutor != null )
                return false;
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return tutor.hashCode();
    }
}

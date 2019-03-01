package me.midest.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lesson {

    private LocalDate date;
    private TimeInterval time;

    private String name = "";
    /** Тип генерируется на основе названия. */
    private String type = "";
    private String discipline;
    private String group;
    private String room = "";

    private Tutor tutor;

    private Lesson(){}

    private static Pattern pattern = Pattern.compile( "([^:]+):?\\s([а-яА-Я._]+\\.?-[\\d.]+)[,\\s]\\s?а(?:уд)?\\.\\s?([^\\s]+)\\s(?:гр\\.\\s)?(.+)" );
    private static Pattern pattern2 = Pattern.compile( "([^,]+)\\r?[\\s\\n]([^\\s,]+),(.*)\\r?[\\s\\n](.*)" );
    public Lesson( String text, SourceType type ){
        if( type == SourceType.TUTOR ) try{
            Matcher m = pattern.matcher( text );
            m.matches();
            setDiscipline( m.group( 1 ).trim());
            setName( m.group( 2 ).trim());
            setRoom( m.group( 3 ).trim());
            setGroup( m.group( 4 ).trim());
        } catch( Exception e ){
            String[] rows = text.split( "\n" );
            if( rows.length > 1 ){
                ArrayList<String> real = new ArrayList<>();
                for( String row : rows ){
                    if( !row.contains( "(*)" )) real.add( row );
                }
                if( real.size() == 1 ){
                    Matcher m = pattern.matcher( real.get( 0 ) );
                    m.matches();
                    setDiscipline( m.group( 1 ).trim());
                    setName( m.group( 2 ).trim());
                    setRoom( m.group( 3 ).trim());
                    setGroup( m.group( 4 ).trim());
                }
                else System.out.println( "ОШИБКА ОБРАБОТКИ: " + text + "\n" );
            }
            else System.out.println( "ОШИБКА ОБРАБОТКИ: " + text + "\n" );
        }
        else if( type == SourceType.ROOM ) try{
            Matcher m = pattern2.matcher( text );
            m.matches();
            setDiscipline( m.group( 1 ).trim());
            setName( m.group( 2 ).trim());
            setGroup( m.group( 3 ).trim() );
            setTutor( new Tutor( m.group( 4 ).trim() ) );
        } catch( Exception e ){
            //System.out.println( "ОШИБКА ОБРАБОТКИ: " + text + "\n" );
        }
        else throw new IllegalArgumentException( type.name() );
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate( LocalDate date ) {
        this.date = date;
    }

    public TimeInterval getTime() {
        return time;
    }

    public void setTime( TimeInterval time ) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public void setName( String name ) {
        this.name = name;
        this.type = name.substring( 0, name.indexOf( '-' ));
    }

    public String getDiscipline() {
        return discipline;
    }

    public void setDiscipline( String discipline ) {
        this.discipline = discipline;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup( String group ) {
        this.group = group;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom( String room ) {
        this.room = "а." + room;
    }

    public Tutor getTutor() {
        return tutor;
    }

    public void setTutor( Tutor tutor ) {
        this.tutor = tutor;
    }

    private void set( Lesson lesson ){
        setTime( lesson.getTime() );
        setDate( lesson.getDate() );
        setDiscipline( lesson.getDiscipline() );
        setName( lesson.getName() );
        setRoom( lesson.getRoom() );
        setGroup( lesson.getGroup() );
        setTutor( lesson.getTutor() );
    }

    public Lesson copy(){
        Lesson copy = new Lesson();
        copy.set( this );
        return copy;
    }

    @Override
    public boolean equals( Object other ) {
        if( this == other ) return true;
        if( other == null ) return false;
        if( !(other instanceof Lesson)) return false;
        final Lesson that = (Lesson)other;
        if( !compare( this.tutor, that.tutor ))
            return false;
        if( !compare( this.date, that.date ))
            return false;
        if( !compare( this.time, that.time ))
            return false;
        return true;
    }

    public boolean deepEquals( Object other ) {
        if( this == other ) return true;
        if( other == null ) return false;
        if( !(other instanceof Lesson)) return false;
        final Lesson that = (Lesson)other;
        if( !compare( this.tutor, that.tutor ))
            return false;
        if( !compare( this.date, that.date ))
            return false;
        if( !compare( this.time, that.time ))
            return false;
        if( !compare( this.type, that.type ))
            return false;
        if( !compare( this.name, that.name ))
            return false;
        if( !compare( this.group, that.group ))
            return false;
        if( !compare( this.room, that.room))
            return false;
        if( !compare( this.discipline, that.discipline ))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = 14;
        int hcInt = 0;
        hcInt =	( getTutor() != null ? getTutor().hashCode() : 0 );
            result = 29 * result + hcInt;
        hcInt =	( getDate() != null ? getDate().hashCode() : 0 );
            result = 29 * result + hcInt;
        hcInt =	( getTime() != null ? getTime().hashCode() : 0 );
            result = 29 * result + hcInt;
        return result;
    }

    private boolean compare( Object o1, Object o2 ){
        if( o1 != null ) return o1.equals(o2);
        else return o2 == null;
    }

    @Override
    public String toString() {
        return date + ", " + time.getStart() + " - " + discipline + " - " + name + " - " + room + " - " + group +  " (" + tutor + ")";
    }

    public enum SourceType{
        TUTOR, GROUP, ROOM
    }
}

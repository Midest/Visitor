package me.midest.model;

public class Tutor implements Comparable<Tutor>, CharSequence{

    private String name;
    private String initialsName;
    private String reverseName;
    private Status weight;
    private boolean outerBoss;
    private boolean special;
    private String regalia = "";
    private String department = "";
    private boolean visitor;
    private boolean visitee;

    /**
     * Имя должно начинаться с заглавной буквы. Все до первой заглавной буквы отбрасывается.
     * @param name имя
     */
    public Tutor( String name ){
        this( name, Status.ПРЕПОДАВАТЕЛЬ );
        if( Character.isLowerCase( name.charAt( 0 ))){
            int i = 0;
            do {} while( !Character.isUpperCase( name.charAt( ++i )));
            name = name.substring( i );
            this.name = name;
        }
    }

    public Tutor( String name, Status weight ){
        this.name = name;
        reverseName( name );
        this.weight = weight;
        this.outerBoss = false;
        this.special = false;
    }

    public Tutor( String name, Status weight, boolean visitor, boolean visitee ){
        this( name, weight );
        this.visitor = visitor;
        this.visitee = visitee;
    }

    public static final Tutor EMPTY = new Tutor( "", Status.ПРЕПОДАВАТЕЛЬ );

    /**
     * Берет Ф.И.О. и возвращает И.О.Ф.
     * Варианты написания исходной строки (в скобках результат):
     * <ul>
     *     <li>Иванов И. И. (И.И. Иванов)</li>
     *     <li>Иванов И.И. (И.И. Иванов)</li>
     *     <li>Иванов (Иванов)</li>
     * </ul>
     * @param name
     * @return
     */
    protected void reverseName( String name ) {
        String[] nameParts = name.trim().split( "\\s" );
        switch( nameParts.length ){
            case 1: {
                initialsName = nameParts[0];
                reverseName = nameParts[0];
                return;
            }
            case 2: {
                String initial = nameParts[1].length() == 1 ? nameParts[1] :
                        nameParts[1].length() == 2 && nameParts[1].endsWith(".") ? nameParts[1] :
                                nameParts[1].charAt(0) + ".";
                initialsName = nameParts[0] + " " + initial;
                reverseName = initial + " " + nameParts[0];
                return;
            }
            case 3: {
                String initial1 = nameParts[1].length() == 1 ? nameParts[1] :
                        nameParts[1].length() == 2 && nameParts[1].endsWith(".") ? nameParts[1] :
                                nameParts[1].charAt(0) + ".";
                String initial2 = nameParts[2].length() == 1 ? nameParts[2] :
                        nameParts[2].length() == 2 && nameParts[2].endsWith(".") ? nameParts[2] :
                                nameParts[2].charAt(0) + ".";
                initialsName = nameParts[0] + " " + initial1 + initial2;
                reverseName = initial1 + initial2 + " " + nameParts[0];
                return;
            }
            default: return;
        }
    }

    public Tutor( String name, Status weight, String regalia ){
        this( name, weight, regalia, "кафедры" );
    }

    public Tutor( String name, Status weight, String regalia, String department ){
        this( name, weight );
        setRegalia( regalia );
        setDepartment( department );

    }

    public String getName() {
        return name;
    }

    public String getReverseInitialsName() {
        return reverseName;
    }


    public boolean isVisitor() {
        return visitor;
    }

    public void setVisitor(boolean visitor) {
        this.visitor = visitor;
    }

    public boolean isVisitee() {
        return visitee;
    }

    public void setVisitee(boolean visitee) {
        this.visitee = visitee;
    }

    public Status getWeight() {
        return weight;
    }

    public void setWeight(Status weight) {
        this.weight = weight;
    }

    public String getRegalia() {
        return regalia;
    }

    public void setRegalia( String regalia ) {
        this.regalia = regalia;
    }

    public String getDepartment() {
        return department;
    }

    /**
     * Особый ли преподаватель. Особого преподавателя
     * посещают только начальники.
     * @return
     */
    public boolean isSpecial() {
        return special;
    }

    /**
     * Установка флага особенности. Особого преподавателя
     * посещают только начальники.
     */
     public void setSpecial( boolean special ) {
        this.special = special;
    }

    /**
     * В родительном падеже.
     * @param department
     */
    public void setDepartment( String department ) {
        this.department = department;
    }

    public boolean fromBosses() {
        return getWeight().compareTo( Status.ЗАМЗАВЕДУЮЩЕГО ) >= 0;
    }

    public boolean isBoss() {
        return getWeight().equals( Status.ЗАВЕДУЮЩИЙ );
    }

    public void setOuterBoss( boolean outerBoss ) {
        this.outerBoss = outerBoss;
    }

    public boolean isOuterBoss() {
        return outerBoss;
    }

    public boolean canVisit( Tutor tutor ) {
        return getWeight().compareTo( tutor.getWeight() ) >= 0
                && ( !tutor.isSpecial() || isBoss());
    }

    @Override
    public int length() {
        return name.length();
    }

    @Override
    public char charAt( int index ) {
        return name.charAt( index );
    }

    @Override
    public CharSequence subSequence( int start, int end ) {
        return name.subSequence( start, end );
    }

    @Override
    public int compareTo( Tutor o ) {
        return weight.compareTo( o.weight );
    }

    @Override
    public boolean equals( Object obj ) {
        if( this == obj ) return true;
        if( obj instanceof Tutor ){
            Tutor that = (Tutor)obj;
            return name != null ? name.equals( that.name ) : that.name == null;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public boolean contains( String s ) {
        return name.contains( s );
    }

    public boolean contains( Tutor s ) {
        return s != null && name != null && name.contains( s.getName() );
    }

    @Override
    public String toString() {
        return initialsName;
    }

    public enum Status {
        // Если нужны будут несколько одноуровневых, нужно
        // вводить переменную с типом и допконструктор
        АСПИРАНТ ( "Аспирант" ),
        НАУЧНЫЙ_СОТРУДНИК ( "Научный сотрудник" ),
        АССИСТЕНТ( "Ассистент" ),
        ПРЕПОДАВАТЕЛЬ ( "Преподаватель" ),
        СТАРШИЙ_ПРЕПОДАВАТЕЛЬ ( "Старший преподаватель" ),
        ДОЦЕНТ ( "Доцент" ),
        ПРОФЕССОР ( "Профессор" ),
        //ОСОБЫЙ, //FIXME сделать в tutor другое поле?
        ЗАМЗАВЕДУЮЩЕГО( "Заместитель заведующего" ),
        ЗАВЕДУЮЩИЙ( "Заведующий" ),;

        private String status;
        Status( String status ){
            this.status = status;
        }

        public static Status byName( String name ){
            for( Status s : values()) if( s.getStatus().equalsIgnoreCase(name))
                return s;
            return null;
        }

        public String getStatus() {
            return status;
        }

        @Override
        public String toString() {
            return getStatus();
        }
    }


}

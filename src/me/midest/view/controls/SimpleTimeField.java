package me.midest.view.controls;

import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.scene.control.IndexRange;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.regex.Pattern;

/**
 * Текстовое поле ввода времени в простом 24-часовом (HH:mm) формате (00:00-23:59).
 * @author Midest
 * @version 201905
 */
public class SimpleTimeField extends TextField {

        protected enum Unit {
            HOURS, MINUTES
        }

        private final Pattern timePattern;
        private final ReadOnlyIntegerWrapper hours;
        private final ReadOnlyIntegerWrapper minutes;

        public SimpleTimeField() {
            this( "00:00" );
        }

        public SimpleTimeField( String time ) {
            super( time );
            timePattern = Pattern.compile( "([01]?[0-9]|2[0-3]):[0-5][0-9]" );
            if( !validate( time ))
                throw new IllegalArgumentException( "Invalid time: " + time );
            hours = new ReadOnlyIntegerWrapper( this, "hours" );
            minutes = new ReadOnlyIntegerWrapper( this, "minutes" );
            hours.bind( new TimeUnitBinding( Unit.HOURS ) );
            minutes.bind( new TimeUnitBinding( Unit.MINUTES ));

            this.addEventFilter( KeyEvent.KEY_TYPED, inputEvent -> {
                int c = SimpleTimeField.this.getCaretPosition();
                if( c > 4 )
                    inputEvent.consume();
                else if( !"1234567890:".contains( inputEvent.getCharacter().toLowerCase() ))
                    inputEvent.consume();
            } );
            this.addEventFilter( KeyEvent.KEY_RELEASED, inputEvent -> {
                boolean withMinutes = false;
                if( SimpleTimeField.this.getText() != null
                        && SimpleTimeField.this.getText().length() == 5
                        && SimpleTimeField.this.getText().indexOf( ":" ) == 2 ) {
                    withMinutes = true;
                }
                int c = SimpleTimeField.this.getCaretPosition();
                if( ( c == 2 && withMinutes )
                        && ( inputEvent.getCode() != KeyCode.LEFT && inputEvent.getCode() != KeyCode.BACK_SPACE ) ) {
                    SimpleTimeField.this.forward();
                    inputEvent.consume();
                }
            } );
        }

        public ReadOnlyIntegerProperty hoursProperty() {
            return hours.getReadOnlyProperty();
        }
        public int getHours() {
            return hours.get();
        }
        public ReadOnlyIntegerProperty minutesProperty() {
            return minutes.getReadOnlyProperty();
        }
        public int getMinutes() {
            return minutes.get();
        }

        @Override
        public void appendText( String text ) {
            // Всегда константная длина, нельзя добавлять
        }

        @Override
        public boolean deleteNextChar() {
            boolean success = false;
            final IndexRange selection = getSelection();
            if( selection.getLength() > 0 ) {
                int selectionEnd = selection.getEnd();
                this.deleteText( selection );
                this.positionCaret( selectionEnd );
                success = true;
            } else {
                int caret = this.getCaretPosition();
                if( caret != 2 ) {
                    StringBuilder currentText = new StringBuilder( this.getText());
                    setText( currentText.replace( caret, caret, "0" ).toString());
                    success = true;
                }
                this.positionCaret( Math.min( caret + 1, this.getText().length()));
            }
            return success;
        }

        @Override
        public boolean deletePreviousChar() {
            boolean success = false;
            final IndexRange selection = getSelection();
            if( selection.getLength() > 0 ) {
                int selectionStart = selection.getStart();
                this.deleteText( selection );
                this.positionCaret( selectionStart );
                success = true;
            } else {
                int caret = this.getCaretPosition();
                if( caret != 3 ){
                    String currentText = this.getText();
                    setText( currentText.substring( 0, caret - 1 ) + "0" + currentText.substring( caret ));
                    success = true;
                }
                this.positionCaret( Math.max( caret - 1, 0 ));
            }
            return success;
        }

        @Override
        public void deleteText( IndexRange range ) {
            this.deleteText( range.getStart(), range.getEnd());
        }

        @Override
        public void deleteText( int begin, int end ) {
            StringBuilder builder = new StringBuilder( this.getText());
            for( int i = begin; i < end; i++ )
                builder.replace( i, i+1, "0" );
            builder.replace( 2, 3, ":" );
            this.setText( builder.toString());
        }

        @Override
        public void insertText( int index, String text ) {
            StringBuilder candidateBuilder = new StringBuilder( this.getText());
            candidateBuilder.replace( index, index + text.length(), text );
            String candidate = candidateBuilder.toString();
            if( validate( candidate ))
                this.setText( candidate );
            this.positionCaret( index + text.length());
        }

        @Override
        public void replaceSelection( String replacement ) {
            IndexRange selection = this.getSelection();
            if( selection.getLength() == 0 ) {
                this.insertText( selection.getStart(), replacement );
            } else {
                this.replaceText( selection.getStart(), selection.getEnd(), replacement );
            }
        }

        @Override
        public void replaceText( IndexRange range, String text ) {
            this.replaceText( range.getStart(), range.getEnd(), text );
        }

        @Override
        public void replaceText( int begin, int end, String text ) {
            if( begin == end ) {
                this.insertText( begin, text );
            } else {
                int replaceLength = end - begin;
                int textLength = text.length();
                {
                    StringBuilder builder = new StringBuilder( this.getText());
                    if( textLength > replaceLength ) builder.replace( begin, end, text.substring( 0, replaceLength ));
                    else{
                        builder.replace( begin, begin+textLength, text );
                        for( int i = begin+textLength; i < end; i++ )
                            if( i != 2 ) builder.replace( i, i+1, "0" );
                    }
                    String testText = builder.toString();
                    if( validate( testText ))
                        this.setText( testText );
                    this.positionCaret( textLength > replaceLength ? end : begin+textLength );
                }
            }
        }

        private boolean validate( String time ) {
            if( !timePattern.matcher( time ).matches() )
                return false;
            String[] tokens = time.split( ":" );
            try {
                int hours = Integer.parseInt( tokens[0] );
                int mins = Integer.parseInt( tokens[1] );
                if( hours < 0 || hours > 23 ) {
                    return false;
                }
                if( mins < 0 || mins > 59 ) {
                    return false;
                }
                return true;
            } catch ( NumberFormatException nfe ) {
                System.out.println( nfe.getMessage());
                return false;
            }
        }

        private final class TimeUnitBinding extends IntegerBinding {

            final Unit unit;

            TimeUnitBinding( Unit unit ) {
                this.bind( textProperty());
                this.unit = unit;
            }

            @Override
            protected int computeValue() {
                String token = getText().split( ":" )[unit.ordinal()];
                return Integer.parseInt( token );
            }

    }

}

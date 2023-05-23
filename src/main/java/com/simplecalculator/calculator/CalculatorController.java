package com.simplecalculator.calculator;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;

public class CalculatorController {


    private StringBuilder numberString = new StringBuilder();
    private double[] numbers = new double[4];   // need the history of (up to) 3 numbers
    private String[] operations = new String[3]; // need the history of (up to) 3 operations
    private int state;                          // keep the track of operations pending & operation priority (see simplify method)
    @FXML
    private TextField display;
    @FXML
    private BorderPane borderPane;

    public void initialize() {

        fullClear();

        borderPane.requestFocus();
        // usually, the keyPressed action is handled on the scene level,
        // but I want to keep the control on the Controller level, so applying
        // the action on borderPane instead of scene
        borderPane.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {

                switch (keyEvent.getCode()) {
                    case DIGIT0, NUMPAD0 -> button0click();
                    case DIGIT1, NUMPAD1 -> button1click();
                    case DIGIT2, NUMPAD2 -> button2click();
                    case DIGIT3, NUMPAD3 -> button3click();
                    case DIGIT4, NUMPAD4 -> button4click();
                    case DIGIT5, NUMPAD5 -> button5click();
                    case DIGIT6, NUMPAD6 -> button6click();
                    case DIGIT7, NUMPAD7 -> button7click();
                    case DIGIT8, NUMPAD8 -> button8click();
                    case DIGIT9, NUMPAD9 -> button9click();
                    case DECIMAL -> buttonDotClick();
                    case PLUS -> buttonPlusClicked();
                    case MINUS -> buttonMinusClicked();
                    case ASTERISK, X -> buttonTimesClicked();
                    case SLASH -> buttonDivideClicked();
                    case EQUALS, ENTER -> buttonEqualsClick();
                    case C -> fullClear();
                    default -> System.out.println("Unimplemented key pressed " + keyEvent.getCode());
                }

            }
        });
    }

    // clear display and memory
    public void fullClear() {

        clearDisplay();

        operations[0] = "none";
        operations[1] = "none";
        numbers[0] = 0.0;
        numbers[1] = 0.0;
        numbers[2] = 0.0;
        state = 0;
    }

    // clear display, not memory
    public void clearDisplay() {

        if (this.numberString != null) {
            this.numberString.delete(0, this.numberString.length());
        }
        this.numberString.append("0");
        display.setText(this.numberString.toString());
    }

    // ============================================================================= //
    //                                 OPERATIONS                                    //
    // ============================================================================= //

    public void operationClicked(String operation) {

        numbers[state] = getCurrentNumber();
        this.operations[state] = operation;
        state++;

        clearDisplay();
        simplify();
    }

    public void buttonPlusClicked(){
        operationClicked("plus");
    }

    // minus is more complex as it has to handle negative numbers
    public void buttonMinusClicked(){

        if (this.numberString.toString().equals("0")) {              // handle negative numbers
            this.numberString.delete(0, this.numberString.length());
            this.numberString.append("-0");
            display.setText(numberString.toString());
        } else {
            operationClicked("minus");
        }
    }

    public void buttonTimesClicked(){
        operationClicked("times");
    }

    public void buttonDivideClicked(){
        operationClicked("divide");
    }

    // With 4 operations and 2 levels of priority, it is sufficient to keep track of last 3 numbers and last 3 operations
    public void simplify() {

        if (state==2) {
            if (operations[1].equals("plus") || operations[1].equals("minus")) { // under these circumstances (low priority operations follows), I can process the numbers[0] and numbers[1] into numbers[0] (perform operations[0])

                performOperation0();

            } else if ((operations[1].equals("times") || operations[1].equals("divide")) && (operations[0].equals("times") || operations[0].equals("divide"))) {

                performOperation0();

            }
        } else if (state == 3) {
            if (operations[2].equals("times") || operations[2].equals("divide")) {
                if ((operations[0].equals("plus") || operations[0].equals("minus")) && (operations[1].equals("times") || operations[1].equals("divide"))) {

                    // priority low-high-high -> I can process numbers[1] and numbers[2] into numbers[1] (perform operations[1])
                    performOperation1();

                } else {
                    throw new RuntimeException("Unexpected event"); // may happen if another (new) operations will not be properly implemented
                }
            } else if (operations[2].equals("plus") || operations[2].equals("minus")) {
                if ((operations[0].equals("plus") || operations[0].equals("minus")) && (operations[1].equals("times") || operations[1].equals("divide"))) {

                    // priority low-high-low -> I can process numbers[0 to 3] into numbers[0] (perform operations[0] and operations[1])
                    performOperation1();
                    performOperation0();

                } else {
                    throw new RuntimeException("Unexpected event"); // may happen if another (new) operations will not be properly implemented
                }
            }
        }
    }

    private void performOperation0() {

        numbers[0] = calculate(numbers[0], numbers[1], operations[0]);
        numbers[1] = 0.0;
        operations[0] = operations[1];
        operations[1] = "none";
        state--;
    }

    public void performOperation1() {

        numbers[1] = calculate(numbers[1], numbers[2], operations[1]);
        numbers[2] = 0.0;
        operations[1] = operations[2];
        operations[2] = "none";
        state--;
    }

    public double calculate(double number1, double number2, String operation) {

        double result = 0.0;

        switch (operation) {
            case "none" -> throw new RuntimeException("No operations to perform");
            case "plus" -> result = number1 + number2;
            case "minus" -> result = number1 - number2;
            case "times" -> result = number1 * number2;
            case "divide" -> result = number1 / number2;
            case "default" -> throw new RuntimeException("Undefined operations");
        }

        return result;

    }
    
    public void buttonEqualsClick() {
        
        double result = 0.0;

        if (state == 0) {
            result = getCurrentNumber();
        } else if (state == 1) {
            numbers[1] = getCurrentNumber();
            performOperation0();
            result = numbers[0];
        } else if (state == 2) {
            numbers[2] = getCurrentNumber();
            performOperation1();
            performOperation0();
            result = numbers[0];
        } else {
            throw new RuntimeException("Unable to get the result, does the simplify method works right?");
        }

        operations[0] = "none";
        operations[1] = "none";
        operations[2] = "none";
        numbers[1] = 0.0;
        numbers[2] = 0.0;

        this.numberString.delete(0, this.numberString.length());
        this.numberString.append(String.format("%1.6f", result));
        display.setText(this.numberString.toString());
    }

    // ============================================================================= //
    //                       BUTTONS FOR NUMBER CONSTRUCTION                         //
    // ============================================================================= //

    // obtain the currently constructed number
    public double getCurrentNumber() {

        double currentNumber = Double.parseDouble(this.numberString.toString());
        return currentNumber;
    }

    // gradually construct the current number
    public void digitButton(String number) {

        if (this.numberString.toString().equals("0")) {
            this.numberString.delete(0, this.numberString.length());
        } else if (this.numberString.toString().equals("-0")) {
            this.numberString.delete(1, this.numberString.length());
        }
        this.numberString.append(number);
        display.setText(this.numberString.toString());
    }

    public void buttonDotClick() {

        if (this.numberString.toString().contains(".")) {
            // the number already contains a decimal dot
        } else {
            this.numberString.append(".");
        }
        display.setText(this.numberString.toString());
    }

    public void button0click() {
        digitButton("0");
    }
    public void button1click() {
        digitButton("1");
    }
    public void button2click() {
        digitButton("2");
    }
    public void button3click() {
        digitButton("3");
    }
    public void button4click() {
        digitButton("4");
    }
    public void button5click() {
        digitButton("5");
    }
    public void button6click() {
        digitButton("6");
    }
    public void button7click() {
        digitButton("7");
    }
    public void button8click() {
        digitButton("8");
    }
    public void button9click() {
        digitButton("9");
    }

}
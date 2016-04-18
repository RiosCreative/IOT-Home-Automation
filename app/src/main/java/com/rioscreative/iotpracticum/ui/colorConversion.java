package com.rioscreative.iotpracticum.ui;

public class colorConversion {

    public String mRGBValue;
    public String mRed;
    public String mGreen;
    public String mBlue;
    public int mRedValue;
    public int mGreenValue;
    public int mBlueValue;


    public colorConversion(String colorString){
        // colorString is hex format RGB color.
        // set mRed, mGreen, mBlue, mRedValue, mGreenValue, and mBlueValue
        mRGBValue = colorString;
        char[] colorValues = colorString.toCharArray();

        mRed = "" + colorValues[0] + colorValues[1];
        mGreen = "" + colorValues[2] + colorValues[3];
        mBlue = "" + colorValues[4] + colorValues[5];
        mRedValue = convertToDec(mRed);
        mGreenValue = convertToDec(mGreen);
        mBlueValue = convertToDec(mBlue);
    }

    public colorConversion(int red, int green, int blue){
        // If red, green, and blue are provided 0-255 integer values
        mRedValue = red;
        mGreenValue = green;
        mBlueValue = blue;
        mRed = convertToHex(red);
        mGreen = convertToHex(green);
        mBlue = convertToHex(blue);
        mRGBValue = mRed + mGreen + mBlue;
    }

    public String getRGBvalue() {
        return mRGBValue;
    }
    public void setRGBvalue(String colorString) {
        mRGBValue = colorString;
    }

    public int getRedValue() {
        return mRedValue;
    }
    public void setRedValue(int red) {
        mRedValue = red;
    }

    public int getGreenValue() {
        return mGreenValue;
    }
    public void setGreenValue(int green) {
        mGreenValue = green;
    }

    public int getBlueValue() {
        return mBlueValue;
    }
    public void setBlueValue(int blue) {
        mBlueValue = blue;
    }

    public int convertToDec(String colorString){
        char[] colorValues = colorString.toCharArray();
        int firstValue = letterNumConverter(colorValues[0])*16;
        int secondValue = letterNumConverter(colorValues[1]);
        int colorValue = firstValue + secondValue;
        return colorValue;
    }

    public int letterNumConverter(char letter){
        if (letter == '0') {return 0;}
        else if (letter == '1') {return 1;}
        else if (letter == '2') {return 2;}
        else if (letter == '3') {return 3;}
        else if (letter == '4') {return 4;}
        else if (letter == '5') {return 5;}
        else if (letter == '6') {return 6;}
        else if (letter == '7') {return 7;}
        else if (letter == '8') {return 8;}
        else if (letter == '9') {return 9;}
        else if (letter == 'A' || letter == 'a') {return 10;}
        else if (letter == 'B' || letter == 'b') {return 11;}
        else if (letter == 'C' || letter == 'c') {return 12;}
        else if (letter == 'D' || letter == 'd') {return 13;}
        else if (letter == 'E' || letter == 'e') {return 14;}
        else if (letter == 'F' || letter == 'f') {return 15;}
        // If the character is something crazy, return a crazy value so it gets noticed --> 255
        return 255;
    }

    public String convertToHex(int colorInteger){
        int tensPlaceNumber = colorInteger / 16;
        int onesPlaceNumber = colorInteger % 16;
        // Convert number value to letter value if > 10
        String tensString = numLetterConversion(tensPlaceNumber);
        String onesString = numLetterConversion(onesPlaceNumber);
        String colorValue = tensString + onesString;
        return colorValue;
    }

    private static String numLetterConversion(int value) {
        String letterValue = new String();
        if (value < 10) {
            return Integer.toString(value);
        }
        else if (value == 10) {
            letterValue = "A";
        }
        else if (value == 11) {
            letterValue = "B";
        }
        else if (value == 12) {
            letterValue = "C";
        }
        else if (value == 13) {
            letterValue = "D";
        }
        else if (value == 14) {
            letterValue = "E";
        }
        else if (value == 15) {
            letterValue = "F";
        }
        return letterValue;
    }

}

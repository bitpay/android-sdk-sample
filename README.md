=Music Store App

This project is a sample app to test the capabilities of the [BitPay Android SDK](https://github.com/bitpay/android-sdk).

## Setup your token
Create a new token on [My Account > API Tokens](https://test.bitpay.com/api-tokens). We store the token as a string resource in [this file](https://github.com/bitpay/android-sdk-sample/blob/master/app/src/main/res/values/strings.xml).

    <string name="token">KUW8nvHZpbqG8xbDvtXYVL</string>

## Building the project

    gradle build

## Installing to a device

    gradle install

## Run it

    gradle run

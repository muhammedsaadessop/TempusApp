package com.example.tempus

data class Errors(
    val loginError: String = "INCORRECT EMAIL OR PASSWORD",
    val regEmailError: String = "EMAIL ALREADY IN USE",
    val validationError: String = " please check credentials",
    val emailValidationEmptyError: String = "Email cant be empty",
    val noNullsPassWord: String = " PASSWORD CANT BE EMPTY",
    val illegalCharacterHash: String = "# Cant be used in Emails",
    val passwordCantBeEmpty: String = "Password field cant be empty",
    val confirmPasswordCantBeEmpty: String = "Confirm Password field cant be empty",
    val passwordNotMatch: String = "Passwords don't match",
    val passwordTooShort: String = " Password does not meet the required length",
    val confirmPasswordTooShort: String = " Confirm Password does not meet the required length",
    val newSignInRequired: String = " Please Sign in again",
    val notYourUsername: String = "no username detected , please enter a valid username",
    val emptyUserName: String = "Username Field is empty!",
    val emptyCat: String = "Category Field is empty!",
    val noCat: String = "No categories have been made!",
    val noDetailsEntered: String = "Please Enter your Details",
    val emptyTaskName: String = " Please enter a Task name",
    val emptyDesc: String = " Please enter a description",
    val startTimeNotChosen: String = "Please enter a Start time",
    val endTimeNotChosen: String = "Please enter a Start time",
    val noStartDate: String = "Please Enter A Start date",
    val noEndDate: String = "Please Enter A Start date",
    val noMinGoal: String = " Please enter a Minimum Goal",
    val noMaxGoal: String = "Please enter a Max Goal",
    val noFName: String = " no First Name Entered",
    val noSName: String = "No Surname Entered",
    val InvalidCharacter: String = "Invalid Character in password or username!",
    val EmptyBreakName: String = "Break name cant be empty!",
    val MinCantBeEmpty: String = " Minutes is empty!",
val TimesCantBeSame:String = "Start and End Time cant be the same ",
val CatNewNameEmpty:String ="New Category name cant be empty"


)

data class Messages
    (
    val confirmedLogin: String = "user details verified!",
    val deleteConfirmation: String = "Account has been deleted"


)

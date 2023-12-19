package com.example.tempus


data class TaskStorage(
    val taskName: String,
    val categoryName: String,
    val description: String,
    val starTime: String,
    val endTime: String,
    val duration: String,
    val minGoal: String,
    val maxGoal: String,
    val dateAdded: String,
    val imageURL: String,
    val tabID: String,
    val completedHours: String,
    val breakDurations: String,
    val completedBreak:String,
    val timeRemaining: String,
    val userIdTask: String

)

data class CategoryStorage
    (
    val categoryID: String,
    val totalHours: String,
    val totalTimeCompleted:Int,
    val userIdCat: String
)

data class User(
    val firstname: String,
    val surname: String,
    val displayname: String,
    val emailaddress: String,
    val password: String,
    val confirmkey: String,
    val securityQ:String,
    val securityAnswer:String,
    val userid: String

)

data class BreakStorage(
    val breakName:String,
    val breakTask:String,
    val breakDuration:String,
    val userIdBreaks:String

)
data class SecurityQuestions(
    val questionOne:String,
    val questionTwo:String,
    val questionThree:String,
    val questionFour:String,
    val questionFive:String


)

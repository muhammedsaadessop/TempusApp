# Tempus_Timesheet_Kotlin
VERSION: 1.0.0
added firebase code for tasks 
added image code to upload
added new code to get download url 
fixed image view 
fixed permissions issue
added storage code 
added firebase for task page
changed app name
fixed gradle issue 
changed to shared account
tested in api 33
tested on api 32
added validation for photo data 
added validation for edit page 
user needs to enter name and surname for data to populate 
added user id to users
fixed permission issues for api 32,33 and 34
31 and lower remains untested 
permissions move to on app load
added email validations 
added pop ups 
added dialogue boxes 
added unfinished forgot password method
stopped at 21:17 11/06/2023

VERSION 2.0.0
started at 8:30 12/6/2023
added an error data class for handling errors 
tested ai 30-34 android 11-14
minor bugs on 12
added notifications 
changed app name 
added pop up window 
changing toast to crouton as its light weight for common errors
added user icon
limited notify to specific situations
preload data once user is verified 
changed target api to 30
added persisted login 
need clarification if ok to log user automatically in
added valid error 
added document name change to userid on validation
normalised ids
fixed loop issue
added dialogue check
almost finished edit details 
lots of bugs fixed
added notify permissions
ended at 21:00 12/6/2023

VERSION:2.0.1
started t 9:30 13/6/2023
ran into gradle issues
upgraded to flamingo 
new assets dynamic icons and colours 
fixed again user loop issue 
fixed user notifications
ended 00:36 14/6/2023

VERSION 2.0.2:
started at 9:30 14/06/2023
added colour to buttons 
found a button solution for dialog's 
upgraded all plug ins to new versions
upgraded gradle 
fixed issue where clicking ok on empty dialogue would wipe firebase
beta dialog is ready for broad implementation
beta notification is ready for broad implementation
croutons are ready for replacing toast front end needs to decide on a design
fixed dialog works with no issues
edit user now works and can update user details
sign up button colour wont change 
all part 2 except date and total hours work well 
added custom buttons
added custom possible layouts
need to implement error handling and comments
app is ready for beta 2
ended at 17:45 14/06/2023

VERSION 2.0.3:
started at 8:55 15/06/2023
added KH UI for settings
added logout code for logout button
added total hours logic 
added progress bar to hide preload clocked at 4.8 seconds
added caching to thread for layout
pages to cache: reg,login,home,account setting and tasks due to heavy data use
smooth transition implemented 
icon fixed
app start up matches loading screen
planned optimisation of image background loading
planned optimisation of strings
found git website for custom stuff 
removed animation from stats and half of home 

ended 23:10 15/06/2023

VERSION 2.0.4:
started 9:00 16/06/2023
add new code to places left out 
added override transition 
moved data loading to splash screen
fixed hours
fixed calculation
fixed image issue
added more error handling
first apk generated 
added filtering by user selectable period
added filtering by task name no more index position
elements on recycler view tied to task names instead 
created work schedule 
solidified positions
ended at 22:16 16/06/2023

VERSION: 2.0.5:
added collapsable categories 
added clickable tasks under categories 
linked pages to tasks 
total hour break down can be seen
made image in task page editable need to add download option 
added tabs for display
fixed back button for registration page 
removed auto comments
added sub task layout
josh has partially worked the graph
londa has got caching working partially 
biometrics planned for implementation 
Beta 2 Patch 3 released 

Patch 4:

added ability to delete user data under settings 
added ability to delete user account 
added ability for the server to remove deleted accounts without needing to close the app
added ability to change email and password in security authentication 
added security checks for changed details and deleted accounts or outdated tokens 
added front end graph
added formatting tool for code
added bottom up shortcut
add new stark code
tested tablayout as an option
kiolin updated front end
fixed error class for errors 
fixed crashing on null time
code has been formatted
redesigned database names
redesigned code names to be clearer
moved persistent login to registration as well to speed up loading 
users can no longer remain in app after devalidation 
details captured type messages will not show except on success 
removed unused files

Patch 5:
completed graph 
added graph colours
added fonts
added size
added data 
added customisation
added swipe to delete 
added breaks on top of allocated hours bar 
added final customisation for graph
added timer code
added fixed timer code
added back button code 
added progress bar category code 
added interval code
added new form code
added privacy policy resources 
added edit task file 
added updated timer code
moved timer to task page
fixed verified database crash 
fixed swipe to delete 

Patch 6: 
to be done implement the above pages 

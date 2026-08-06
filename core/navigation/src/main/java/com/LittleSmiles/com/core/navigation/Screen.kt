package com.LittleSmiles.com.core.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Menu : Screen("menu")
    object Settings : Screen("settings")
    object ParentalHub : Screen("parental_hub")
    
    // Learning Activities
    object Colors : Screen("colors")
    object Shapes : Screen("shapes")
    object Animals : Screen("animals")
    object Routines : Screen("routines")
    object Opposites : Screen("opposites")
    object Letters : Screen("letters")
    object Numbers : Screen("numbers")
    object Tracing : Screen("tracing")
    object BodyParts : Screen("body_parts")
    object Emotions : Screen("emotions")
    object Matching : Screen("matching")
    object Drawing : Screen("drawing")
}

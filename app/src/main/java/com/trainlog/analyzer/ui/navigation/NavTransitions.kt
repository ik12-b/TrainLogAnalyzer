package com.trainlog.analyzer.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.navigation.NavBackStackEntry

private const val DURATION = 320
private val Ease = FastOutSlowInEasing

/** Forward: slide from right + fade */
fun slideEnter(): EnterTransition =
    slideInHorizontally(
        animationSpec = tween(DURATION, easing = Ease),
        initialOffsetX = { it / 5 }
    ) + fadeIn(animationSpec = tween(DURATION, easing = Ease))

fun slideExit(): ExitTransition =
    slideOutHorizontally(
        animationSpec = tween(DURATION, easing = Ease),
        targetOffsetX = { -it / 8 }
    ) + fadeOut(animationSpec = tween(DURATION, easing = Ease))

/** Back: reverse of forward */
fun slidePopEnter(): EnterTransition =
    slideInHorizontally(
        animationSpec = tween(DURATION, easing = Ease),
        initialOffsetX = { -it / 8 }
    ) + fadeIn(animationSpec = tween(DURATION, easing = Ease))

fun slidePopExit(): ExitTransition =
    slideOutHorizontally(
        animationSpec = tween(DURATION, easing = Ease),
        targetOffsetX = { it / 5 }
    ) + fadeOut(animationSpec = tween(DURATION, easing = Ease))

/** Modal-style: slide up from bottom (form, import, compare) */
fun modalEnter(): EnterTransition =
    slideInVertically(
        animationSpec = tween(DURATION, easing = Ease),
        initialOffsetY = { it / 4 }
    ) + fadeIn(animationSpec = tween(DURATION, easing = Ease)) +
        scaleIn(
            initialScale = 0.96f,
            animationSpec = tween(DURATION, easing = Ease)
        )

fun modalExit(): ExitTransition =
    slideOutVertically(
        animationSpec = tween(DURATION, easing = Ease),
        targetOffsetY = { it / 6 }
    ) + fadeOut(animationSpec = tween(DURATION, easing = Ease)) +
        scaleOut(
            targetScale = 0.96f,
            animationSpec = tween(DURATION, easing = Ease)
        )

fun modalPopEnter(): EnterTransition =
    fadeIn(animationSpec = tween(DURATION, easing = Ease)) +
        scaleIn(initialScale = 0.98f, animationSpec = tween(DURATION, easing = Ease))

fun modalPopExit(): ExitTransition =
    slideOutVertically(
        animationSpec = tween(DURATION, easing = Ease),
        targetOffsetY = { it / 3 }
    ) + fadeOut(animationSpec = tween(DURATION, easing = Ease))

/** Tab switch Home ↔ Lab: cross-fade only */
fun tabEnter(): EnterTransition =
    fadeIn(animationSpec = tween(280, easing = Ease))

fun tabExit(): ExitTransition =
    fadeOut(animationSpec = tween(280, easing = Ease))

/** Splash → Home: soft fade + scale */
fun splashExit(): ExitTransition =
    fadeOut(animationSpec = tween(400, easing = Ease)) +
        scaleOut(targetScale = 1.04f, animationSpec = tween(400, easing = Ease))

fun homeFromSplashEnter(): EnterTransition =
    fadeIn(animationSpec = tween(450, easing = Ease)) +
        scaleIn(initialScale = 0.97f, animationSpec = tween(450, easing = Ease))

/** Default transitions applied at NavHost level */
val defaultEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideEnter()
}
val defaultExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideExit()
}
val defaultPopEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slidePopEnter()
}
val defaultPopExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slidePopExit()
}

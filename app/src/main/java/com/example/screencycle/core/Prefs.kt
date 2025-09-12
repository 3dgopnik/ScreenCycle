package com.example.screencycle.core

import android.content.Context
import android.content.SharedPreferences

object Prefs {
    private const val NAME = "screen_cycle_prefs"
    private const val KEY_PLAY = "play_minutes"
    private const val KEY_REST = "rest_minutes"
    private const val KEY_PACKAGES = "packages"
    private const val KEY_RUNNING = "running"

    private fun sp(ctx: Context): SharedPreferences = ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun setPlayMinutes(ctx: Context, m: Int) = sp(ctx).edit().putInt(KEY_PLAY, m).apply()
    fun setRestMinutes(ctx: Context, m: Int) = sp(ctx).edit().putInt(KEY_REST, m).apply()
    fun getPlayMinutes(ctx: Context) = sp(ctx).getInt(KEY_PLAY, 30)
    fun getRestMinutes(ctx: Context) = sp(ctx).getInt(KEY_REST, 30)

    fun setPackages(ctx: Context, list: Set<String>) = sp(ctx).edit().putStringSet(KEY_PACKAGES, list).apply()
    fun getPackages(ctx: Context): Set<String> = sp(ctx).getStringSet(KEY_PACKAGES, emptySet()) ?: emptySet()

    fun setRunning(ctx: Context, b: Boolean) = sp(ctx).edit().putBoolean(KEY_RUNNING, b).apply()
    fun isRunning(ctx: Context) = sp(ctx).getBoolean(KEY_RUNNING, false)
}

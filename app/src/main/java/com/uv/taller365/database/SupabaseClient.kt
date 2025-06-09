package com.uv.taller365.database

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://jbfdwlkyvckmfsfhufei.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImpiZmR3bGt5dmNrbWZzZmh1ZmVpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDkzODg4MzgsImV4cCI6MjA2NDk2NDgzOH0.4M-wLg1gW8EqoW-ZESgoL4mSBprsrZXlRxSkf3Wh81A",
    ) {
        install(Storage)
    }
}



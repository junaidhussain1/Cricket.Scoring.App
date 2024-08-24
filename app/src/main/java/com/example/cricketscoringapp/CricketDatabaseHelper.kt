package com.example.cricketscoringapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// SQLite helper class
class CricketDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "cricketPlayers.db"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "players"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = "CREATE TABLE $TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_NAME TEXT)"
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun addPlayer(name: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_NAME, name)
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    // Add method to update player
//    fun updatePlayer(playerId: Int, newName: String): Int {
//        val db = writableDatabase
//        val contentValues = ContentValues().apply {
//            put("name", newName)
//        }
//        return db.update("players", contentValues, "id = ?", arrayOf(playerId.toString()))
//    }

    // Add method to delete player
    fun deletePlayer(playerId: Int): Int {
        val db = writableDatabase
        return db.delete("players", "id = ?", arrayOf(playerId.toString()))
    }

    // Update getAllPlayers method to return Player objects
    fun getAllPlayers(): List<Player> {
        val players = mutableListOf<Player>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM players", null)
        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            players.add(Player(id, name))
        }
        cursor.close()
        return players
    }

    fun playerAlreadyExists(playerName: String): Boolean {
        val db = readableDatabase
        val query = "SELECT 1 FROM players WHERE name = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(playerName))

        val exists = cursor.moveToFirst() // returns true if the query returned a row, false otherwise
        cursor.close()
        return exists
    }
}

package com.example.cricketscoringapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// SQLite helper class
class CricketDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        //Database name
        const val DATABASE_NAME = "cricket.db"
        const val DATABASE_VERSION = 1

        //Table Names
        const val TABLE_PLAYERS = "players"
        const val TABLE_TEAMS = "teams"
        const val TABLE_MATCHES = "matches"

    }

    // SQL statements to create tables
    private val createPLAYERSTABLE = """
        CREATE TABLE $TABLE_PLAYERS (
            name TEXT PRIMARY KEY
        )
    """

    private val createMATCHESTABLE = """
        CREATE TABLE $TABLE_MATCHES (
            match_id TEXT PRIMARY KEY,
            isFinished INTEGER,
            isSynced INTEGER
        )
    """

    private val createTEAMSTABLE = """
        CREATE TABLE $TABLE_TEAMS (
            match_id TEXT,
            team_id INTEGER,
            player_name TEXT,
            isCaptain INTEGER,
            isKeeper INTEGER,
            PRIMARY KEY (match_id, player_name)
        )
    """

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(createPLAYERSTABLE)
        db?.execSQL(createMATCHESTABLE)
        db?.execSQL(createTEAMSTABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_PLAYERS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_MATCHES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_TEAMS")
        onCreate(db)
    }

    fun addPlayer(name: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("name", name)
        db.insert(TABLE_PLAYERS, null, values)
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
    fun deletePlayer(name: String): Int {
        val db = writableDatabase
        return db.delete(TABLE_PLAYERS, "name = ?", arrayOf(name))
    }

    // Update getAllPlayers method to return Player objects
    fun getAllPlayers(): List<Player> {
        val players = mutableListOf<Player>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_PLAYERS", null)
        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            players.add(Player(name))
        }
        cursor.close()
        return players
    }

    fun playerAlreadyExists(playerName: String): Boolean {
        val db = readableDatabase
        val query = "SELECT 1 FROM $TABLE_PLAYERS WHERE name = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(playerName))

        val exists = cursor.moveToFirst() // returns true if the query returned a row, false otherwise
        cursor.close()
        return exists
    }

//    fun addTeamPlayer(player) {
//        val db = this.writableDatabase
//        val values = ContentValues()
//        values.put("player_name", name)
//        values.put("isCaptain", name)
//        values.put("isKeeper", name)
//        db.insert(TABLE_PLAYERS, null, values)
//        db.close()
//    }

    //Use this to get unique univeral identifier to use for Match ID
    //val uuid = UUID.randomUUID().toString()
}

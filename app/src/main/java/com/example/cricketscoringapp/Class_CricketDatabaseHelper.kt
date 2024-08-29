package com.example.cricketscoringapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.UUID

// SQLite helper class
class CricketDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        //Database name
        const val DATABASE_NAME = "cricket.db"
        const val DATABASE_VERSION = 2

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

    private fun addMatch(matchId: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("match_id", matchId)
        values.put("isFinished",0)
        values.put("isSynced",0)
        db.insert(TABLE_MATCHES, null, values)
        db.close()
    }

    fun deleteMatch(matchId: String) {
        val db = writableDatabase
        db.delete(TABLE_TEAMS, "match_id = ?", arrayOf(matchId))
        db.delete(TABLE_MATCHES, "match_id = ?", arrayOf(matchId))
    }

    fun getMatchId(): String {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_MATCHES WHERE isFinished = 0 LIMIT 1", null)
        val matchId:String

        if (cursor.moveToFirst()) {
            matchId = cursor.getString(cursor.getColumnIndexOrThrow("match_id"))
            return matchId
        } else {
            //Use this to get unique universal identifier to use for Match ID
            matchId = UUID.randomUUID().toString()
            addMatch(matchId)
        }
        cursor.close()
        return matchId
    }

    //Delete team players except captain
    fun clearTeam(matchId: String, teamId: Int): Int {
        val db = writableDatabase
        return db.delete(TABLE_TEAMS, "match_id = ? AND team_id = ? AND isCaptain = 0", arrayOf(matchId,teamId.toString()))
    }

    //includeCaptain = 0 (team players except captain). includeCaptain = 1 (all team players)
    fun getTeamPlayers(matchId: String, teamId: Int, includeCaptain: Int): List<Player> {
        val players = mutableListOf<Player>()
        val db = readableDatabase
        var query = "SELECT * FROM $TABLE_TEAMS WHERE match_id = ? AND team_id = ?"
        if (includeCaptain == 0) {
            query += "AND isCaptain = 0"
        }
        val cursor = db.rawQuery(query, arrayOf(matchId,teamId.toString()))
        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow("player_name"))
            players.add(Player(name))
        }
        cursor.close()
        return players
    }

    fun getTeamForPlayer(matchId: String,playerName: String) : Int? {
        val db = readableDatabase
        val query = "SELECT team_id FROM $TABLE_TEAMS WHERE match_id = ? AND player_name = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId,playerName))
        var teamId: Int? = null

        if (cursor.moveToFirst()) {
            teamId = cursor.getInt(0)
        }

        cursor.close()
        return teamId
    }

    fun addTeamPlayer(matchId: String, teamId: Int, playerName: String, isCaptain: Int, isKeeper: Int) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("match_id", matchId)
        values.put("team_id", teamId)
        values.put("player_name", playerName)
        values.put("isCaptain",isCaptain)
        values.put("isKeeper",isKeeper)
        db.insert(TABLE_TEAMS, null, values)
        db.close()
    }

    fun removeTeamPlayer(matchId: String, teamId: Int, playerName: String): Int {
        val db = writableDatabase
        return db.delete(TABLE_TEAMS, "match_id = ? AND team_id = ? AND player_name = ?", arrayOf(matchId,teamId.toString(),playerName))
    }

    // Add method to update player
//    fun updateTeamPlayer(matchId: String, teamId: Int, playerName: String): Int {
//        val db = writableDatabase
//        val contentValues = ContentValues().apply {
//            put("name", newName)
//        }
//        return db.update("players", contentValues, "id = ?", arrayOf(playerId.toString()))
//    }

    fun isTeamPlayer(matchId: String, teamId: Int, playerName: String) : Boolean {
        val db = readableDatabase
        val query = "SELECT 1 FROM $TABLE_TEAMS WHERE match_id = ? AND team_id =? AND player_name = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId,teamId.toString(),playerName))

        val exists = cursor.moveToFirst() // returns true if the query returned a row, false otherwise
        cursor.close()
        return exists
    }

    fun getTeamSize(matchId: String, teamId: Int) : Int {
        val db = readableDatabase
        val query = "SELECT COUNT(*) FROM $TABLE_TEAMS WHERE match_id = ? AND team_id =?"
        val cursor = db.rawQuery(query, arrayOf( matchId,teamId.toString()))

        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
    }

    fun getCaptain(matchId: String, teamId: Int) : String {
        val db = readableDatabase
        val query = "SELECT player_name FROM $TABLE_TEAMS WHERE match_id = ? AND team_id = ? AND isCaptain = 1 LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId,teamId.toString()))

        val captainName = if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndexOrThrow("player_name"))
        } else {
            ""
        }
        cursor.close()
        return captainName
    }
}

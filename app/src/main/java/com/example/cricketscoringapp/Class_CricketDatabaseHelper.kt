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
        const val DATABASE_VERSION = 3

        //Table Names
        const val TABLE_PLAYERS = "players"
        const val TABLE_MATCHES = "matches"
        const val TABLE_TEAMS = "teams"
        const val TABLE_BATTINGSTATS = "battingstats"
        const val TABLE_BOWLINGSTATS = "bowlingstats"
        const val TABLE_BOWLINGSTATSSUMMARY = "bowlingsummary"
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
            first_batting_team_captain TEXT,
            winning_team_captain TEXT,
            losing_team_captain TEXT,
            is_synced INTEGER
        )
    """

    private val createTEAMSTABLE = """
        CREATE TABLE $TABLE_TEAMS (
            match_id TEXT,
            team_id INTEGER,
            player_name TEXT,
            is_captain INTEGER,
            PRIMARY KEY (match_id, player_name)
        )
    """

    private val createBATTINGSTATS = """
        CREATE TABLE $TABLE_BATTINGSTATS (
            match_id TEXT,
            team_id INTEGER,
            batting_order INTEGER,
            player_name TEXT,
            batting_turn INTEGER,
            batting_status TEXT,
            runs INTEGER,
            balls INTEGER,
            fours INTEGER,
            sixes INTEGER,
            PRIMARY KEY (match_id, batting_order)
        )
    """

    private val createBOWNLINGSTATS = """
        CREATE TABLE $TABLE_BOWLINGSTATS (
            match_id TEXT,
            team_id INTEGER,
            bowling_order INTEGER,
            player_name TEXT,
            bowling_turn INTEGER,
            bowling_status TEXT,
            keeper_name TEXT,
            over REAL,
            maiden INTEGER,
            runs INTEGER,
            wickets INTEGER,
            PRIMARY KEY (match_id, bowling_order)
        )
    """

    private val createBOWNLINGSTATSSUMMARY = """
        CREATE TABLE $TABLE_BOWLINGSTATSSUMMARY (
            match_id TEXT,
            team_id INTEGER,
            player_name TEXT,
            over REAL,
            maiden INTEGER,
            runs INTEGER,
            wickets INTEGER,
            PRIMARY KEY (match_id, player_name)
        )
    """
    
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(createPLAYERSTABLE)
        db?.execSQL(createMATCHESTABLE)
        db?.execSQL(createTEAMSTABLE)
        db?.execSQL(createBATTINGSTATS)
        db?.execSQL(createBOWNLINGSTATS)
        db?.execSQL(createBOWNLINGSTATSSUMMARY)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_PLAYERS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_MATCHES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_TEAMS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_BATTINGSTATS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_BOWLINGSTATS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_BOWLINGSTATSSUMMARY")
        onCreate(db)
    }

    fun addPlayer(name: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("name", name)
        db.insert(TABLE_PLAYERS, null, values)
        db.close()
    }

    fun deletePlayer(name: String): Int {
        val db = writableDatabase
        return db.delete(TABLE_PLAYERS, "name = ?", arrayOf(name))
    }

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

    fun getMatchId(): String {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_MATCHES WHERE winning_team_captain = ? AND losing_team_captain = ? LIMIT 1", arrayOf("",""))
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

    private fun addMatch(matchId: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("match_id", matchId)
        values.put("first_batting_team_captain","")
        values.put("winning_team_captain","")
        values.put("losing_team_captain","")
        values.put("is_synced",0)
        db.insert(TABLE_MATCHES, null, values)
        db.close()
    }

    fun updateMatch(matchId: String, firstBattingTeamCaptain: String) : Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("first_batting_team_captain", firstBattingTeamCaptain)
        }
        val whereClause = "match_id = ?"
        val whereArgs = arrayOf(matchId)

        return db.update(TABLE_MATCHES, contentValues, whereClause, whereArgs)
    }

    fun updateMatch(matchId: String, winningTeamCaptain: String, losingTeamCaptain: String) : Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("winning_team_captain", winningTeamCaptain)
            put("losing_team_captain", losingTeamCaptain)
        }
        val whereClause = "match_id = ?"
        val whereArgs = arrayOf(matchId)

        return db.update(TABLE_MATCHES, contentValues, whereClause, whereArgs)
    }

    fun deleteMatch(matchId: String) {
        val db = writableDatabase
        db.delete(TABLE_TEAMS, "match_id = ?", arrayOf(matchId))
        db.delete(TABLE_BATTINGSTATS, "match_id = ?", arrayOf(matchId))
        db.delete(TABLE_BOWLINGSTATS, "match_id = ?", arrayOf(matchId))
        db.delete(TABLE_BOWLINGSTATSSUMMARY, "match_id = ?", arrayOf(matchId))
        db.delete(TABLE_MATCHES, "match_id = ?", arrayOf(matchId))
    }

    fun updateMatch(matchId: String, isSynced: Int) : Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("is_synced", isSynced)
        }
        return db.update(TABLE_MATCHES, contentValues, "match_id = ?", arrayOf(matchId))
    }

    //Delete team players except captain
//    fun clearTeam(matchId: String, teamId: Int): Int {
//        val db = writableDatabase
//        return db.delete(TABLE_TEAMS, "match_id = ? AND team_id = ? AND is_captain = 0", arrayOf(matchId,teamId.toString()))
//    }

    fun getTeamPlayers(matchId: String, teamId: Int, includeCaptain: Int): List<Player> {
        val players = mutableListOf<Player>()
        val db = readableDatabase
        var query = "SELECT * FROM $TABLE_TEAMS WHERE match_id = ? AND team_id = ?"
        if (includeCaptain == 0) {
            query += "AND is_captain = 0"
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

    fun addTeamPlayer(matchId: String, teamId: Int, playerName: String, isCaptain: Int) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("match_id", matchId)
        values.put("team_id", teamId)
        values.put("player_name", playerName)
        values.put("is_captain",isCaptain)
        db.insert(TABLE_TEAMS, null, values)
        db.close()
    }

    fun removeTeamPlayer(matchId: String, teamId: Int, playerName: String): Int {
        val db = writableDatabase
        return db.delete(TABLE_TEAMS, "match_id = ? AND team_id = ? AND player_name = ?", arrayOf(matchId,teamId.toString(),playerName))
    }

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

    fun getCaptainForTeam(matchId: String, teamId: Int) : String {
        val db = readableDatabase
        val query = "SELECT player_name FROM $TABLE_TEAMS WHERE match_id = ? AND team_id = ? AND is_captain = 1 LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId,teamId.toString()))

        val captainName = if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndexOrThrow("player_name"))
        } else {
            ""
        }
        cursor.close()
        return captainName
    }

    fun addBattingStats(matchId: String,
                        teamId: Int,
                        playerName: String,
                        battingTurn: Int,
                        battingStatus: String) {
        val db = writableDatabase
        val values = ContentValues()
        values.put("match_id", matchId)
        values.put("team_id", teamId)
        values.put("batting_order",getNextBattingOrderNo(matchId))
        values.put("player_name", playerName)
        values.put("batting_turn", battingTurn)
        values.put("batting_status", battingStatus)
        db.insert(TABLE_BATTINGSTATS,null,values)
        db.close()
    }

    fun updateBattingStats(matchId: String,
                           teamId: Int,
                           battingOrder: Int,
                           battingStatus: String) : Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("batting_status", battingStatus)
        }
        val whereClause = "match_id = ? AND team_id = ? AND batting_order = ?"
        val whereArgs = arrayOf(matchId, teamId.toString(), battingOrder.toString())

        return db.update(TABLE_BATTINGSTATS, contentValues, whereClause, whereArgs)
    }

    fun updateBattingStats(matchId: String,
                           teamId: Int,
                           battingOrder: Int,
                           runs: Int,
                           balls: Int,
                           fours: Int,
                           sixes: Int) : Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("runs", runs)
            put("balls", balls)
            put("fours", fours)
            put("sixes", sixes)
        }
        val whereClause = "match_id = ? AND team_id = ? AND batting_order = ?"
        val whereArgs = arrayOf(matchId, teamId.toString(), battingOrder.toString())

        return db.update(TABLE_BATTINGSTATS, contentValues, whereClause, whereArgs)
    }

    private fun getNextBattingOrderNo(matchId: String): Int {
        val db = readableDatabase
        val query = "SELECT MAX(batting_order) AS max_battingorder FROM $TABLE_BATTINGSTATS"
        val cursor = db.rawQuery(query, null)

        cursor.use { // Auto-close the cursor after use
            if (it.moveToFirst()) {
                return it.getInt(it.getColumnIndexOrThrow("max_battingorder")) + 1
            }
        }
        // If no records found, return 1
        return 1
    }
}

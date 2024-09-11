package com.example.cricketscoringapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

// SQLite helper class
class CricketDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        //Database name
        const val DATABASE_NAME = "cricket.db"
        const val DATABASE_VERSION = 8

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
            match_date TEXT,
            first_batting_team_captain TEXT,
            second_batting_team_captain TEXT,
            winning_team_captain TEXT,
            is_started INTEGER,
            is_synced INTEGER
        )
    """

    private val createTEAMSTABLE = """
        CREATE TABLE $TABLE_TEAMS (
            match_id TEXT,
            team_id INTEGER,
            player_name TEXT,
            is_captain INTEGER,
            is_midbowler INTEGER,
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
            noballs INTEGER,
            wides INTEGER,
            byes INTEGER,
            legbyes INTEGER,
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
            noballs INTEGER,
            wides INTEGER,
            byes INTEGER,
            legbyes INTEGER,
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
        val cursor = db.rawQuery("SELECT match_id FROM $TABLE_MATCHES WHERE winning_team_captain = ? LIMIT 1", arrayOf(""))
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
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd") // Define the format
        val formattedDate = currentDate.format(formatter)
        values.put("match_id", matchId)
        values.put("match_date",formattedDate)
        values.put("first_batting_team_captain","")
        values.put("second_batting_team_captain","")
        values.put("winning_team_captain","")
        values.put("is_started",0)
        values.put("is_synced",0)
        db.insert(TABLE_MATCHES, null, values)
        db.close()
    }

    fun updateMatchCaptain(matchId: String, whichTeam: Int, captain: String) : Int {
        val db = writableDatabase
        val batsman = if (whichTeam == 2) {
            "second_batting_team_captain"
        } else {
            "first_batting_team_captain"
        }
        val contentValues = ContentValues().apply {
            put(batsman, captain)
        }
        val whereClause = "match_id = ?"
        val whereArgs = arrayOf(matchId)

        return db.update(TABLE_MATCHES, contentValues, whereClause, whereArgs)
    }

    fun updateMatchIsStarted(matchId: String) : Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("is_started", 1)
        }
        val whereClause = "match_id = ?"
        val whereArgs = arrayOf(matchId)

        return db.update(TABLE_MATCHES, contentValues, whereClause, whereArgs)
    }

    fun getBattingTeamCaptain(matchId: String, whichTeam: Int) : String {
        val db = readableDatabase

        val batsman = if (whichTeam == 2) {
            "second_batting_team_captain"
        } else {
            "first_batting_team_captain"
        }

        val query = "SELECT $batsman FROM $TABLE_MATCHES WHERE match_id = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId))

        val captainName = if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndexOrThrow(batsman))
        } else {
            ""
        }
        cursor.close()
        return captainName
    }

    fun deleteMatch(matchId: String) {
        val db = writableDatabase
        db.delete(TABLE_TEAMS, "match_id = ?", arrayOf(matchId))
        db.delete(TABLE_BATTINGSTATS, "match_id = ?", arrayOf(matchId))
        db.delete(TABLE_BOWLINGSTATS, "match_id = ?", arrayOf(matchId))
        db.delete(TABLE_BOWLINGSTATSSUMMARY, "match_id = ?", arrayOf(matchId))
        db.delete(TABLE_MATCHES, "match_id = ?", arrayOf(matchId))
    }

    fun updateMatchWinner(matchId: String, winningTeamCaptain: String) : Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("winning_team_captain", winningTeamCaptain)
        }
        val whereClause = "match_id = ?"
        val whereArgs = arrayOf(matchId)

        return db.update(TABLE_MATCHES, contentValues, whereClause, whereArgs)
    }

    fun updateMatch(matchId: String, isSynced: Int) : Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("is_synced", isSynced)
        }
        return db.update(TABLE_MATCHES, contentValues, "match_id = ?", arrayOf(matchId))
    }

    fun getMatches() : List<Match> {
        val matches = mutableListOf<Match>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_MATCHES", null)
        while (cursor.moveToNext()) {
            val matchId = cursor.getString(cursor.getColumnIndexOrThrow("match_id"))
            val firstBattingTeamCaptain = cursor.getString(cursor.getColumnIndexOrThrow("first_batting_team_captain"))
            val secondBattingTeamCaptain = cursor.getString(cursor.getColumnIndexOrThrow("second_batting_team_captain"))
            val winningTeamCaptain = cursor.getString(cursor.getColumnIndexOrThrow("winning_team_captain"))
            val isStarted = cursor.getString(cursor.getColumnIndexOrThrow("is_started")).toBoolean()
            val isSynced = cursor.getString(cursor.getColumnIndexOrThrow("is_synced")).toBoolean()
            matches.add(Match(matchId,firstBattingTeamCaptain,secondBattingTeamCaptain,winningTeamCaptain,isStarted,isSynced))
        }
        cursor.close()
        return matches
    }

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

    fun addTeamPlayer(matchId: String, teamId: Int, playerName: String, isCaptain: Int, isMidBowler: Int) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("match_id", matchId)
        values.put("team_id", teamId)
        values.put("player_name", playerName)
        values.put("is_captain",isCaptain)
        values.put("is_midbowler",isMidBowler)
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
        val query = "SELECT MAX(batting_order) AS max_batting_order FROM $TABLE_BATTINGSTATS WHERE match_id = ?"
        val cursor = db.rawQuery(query, arrayOf(matchId))

        cursor.use { // Auto-close the cursor after use
            if (it.moveToFirst()) {
                return it.getInt(it.getColumnIndexOrThrow("max_batting_order")) + 1
            }
        }
        // If no records found, return 1
        return 1
    }

    fun getBatsmanByStatus(matchId: String, battingStatus: String) : String {
        val db = readableDatabase
        val query = "SELECT player_name FROM $TABLE_BATTINGSTATS WHERE match_id = ? AND batting_status = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId,battingStatus))

        val playerName = if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndexOrThrow("player_name"))
        } else {
            ""
        }
        cursor.close()
        return playerName
    }

    fun addBowlingStats(matchId: String,
                        teamId: Int,
                        playerName: String,
                        bowlingTurn: Int,
                        bowlingStatus: String) {
        val db = writableDatabase
        val values = ContentValues()
        values.put("match_id", matchId)
        values.put("team_id", teamId)
        values.put("bowling_order",getNextBowlingOrderNo(matchId))
        values.put("player_name", playerName)
        values.put("bowling_turn", bowlingTurn)
        values.put("bowling_status", bowlingStatus)
        values.put("keeper_name","")
        db.insert(TABLE_BOWLINGSTATS,null,values)
        db.close()
    }

    fun updateBowlingStats(matchId: String,
                           bowlingStatus: String) : Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("bowling_status", bowlingStatus)
        }
        val whereClause = "match_id = ? AND bowling_status = ?"
        val whereArgs = arrayOf(matchId, "bowling")

        return db.update(TABLE_BOWLINGSTATS, contentValues, whereClause, whereArgs)
    }

    fun updateBowlingStatsKeeper(matchId: String,
                           teamId: Int,
                           bowlingOrder: Int,
                           keeperName: String) : Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("keeper_name", keeperName)
        }
        val whereClause = "match_id = ? AND team_id = ? AND bowling_order = ?"
        val whereArgs = arrayOf(matchId, teamId.toString(), bowlingOrder.toString())

        return db.update(TABLE_BOWLINGSTATS, contentValues, whereClause, whereArgs)
    }

    fun updateBowlingStats(matchId: String,
                           teamId: Int,
                           bowlingOrder: Int,
                           over: Double,
                           maiden: Int,
                           runs: Int,
                           wickets: Int,
                           noballs: Int,
                           wides: Int,
                           byes: Int,
                           legbyes: Int) : Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("over", over)
            put("maiden", maiden)
            put("runs", runs)
            put("wickets", wickets)
            put("noballs", noballs)
            put("wides",wides)
            put("byes",byes)
            put("legbyes",legbyes)
        }
        val whereClause = "match_id = ? AND team_id = ? AND bowling_order = ?"
        val whereArgs = arrayOf(matchId, teamId.toString(), bowlingOrder.toString())

        return db.update(TABLE_BOWLINGSTATS, contentValues, whereClause, whereArgs)
    }

    private fun getNextBowlingOrderNo(matchId: String): Int {
        val db = readableDatabase
        val query = "SELECT MAX(bowling_order) AS max_bowling_order FROM $TABLE_BOWLINGSTATS WHERE match_id = ?"
        val cursor = db.rawQuery(query, arrayOf(matchId))

        cursor.use { // Auto-close the cursor after use
            if (it.moveToFirst()) {
                return it.getInt(it.getColumnIndexOrThrow("max_bowling_order")) + 1
            }
        }
        // If no records found, return 1
        return 1
    }

    fun getCurrentBowler(matchId: String) : String {
        val db = readableDatabase
        val query = "SELECT player_name FROM $TABLE_BOWLINGSTATS WHERE match_id = ? AND bowling_status = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId,"bowling"))

        val playerName = if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndexOrThrow("player_name"))
        } else {
            ""
        }
        cursor.close()
        return playerName
    }

    fun getCurrentKeeper(matchId: String) : String {
        val db = readableDatabase
        val query = "SELECT keeper_name FROM $TABLE_BOWLINGSTATS WHERE match_id = ? AND bowling_status = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId,"bowling"))

        val playerName = if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndexOrThrow("keeper_name"))
        } else {
            ""
        }
        cursor.close()
        return playerName
    }

    fun getBowlingStats(matchId: String, teamId: Int) : List<Bowler> {
        val bowlers = mutableListOf<Bowler>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_BOWLINGSTATS where match_id = ? AND team_id = ?", arrayOf(matchId,teamId.toString()))
        while (cursor.moveToNext()) {
            val playerName = cursor.getString(cursor.getColumnIndexOrThrow("player_name"))
            val overs: String? = cursor.getString(cursor.getColumnIndexOrThrow("over"))
            if (overs != null) {
                bowlers.add(Bowler(playerName,overs.toDouble()))
            }
        }
        cursor.close()
        return bowlers
    }

    fun getFullyBattedAlreadyPlayers(matchId: String, teamId: Int) : List<Player> {
        val players = mutableListOf<Player>()
        val db = readableDatabase
        val query = "SELECT player_name FROM $TABLE_BATTINGSTATS WHERE match_id = ? AND team_id = ? AND batting_turn = 2 AND batting_status = 'out'"
        val cursor = db.rawQuery(query, arrayOf(matchId,teamId.toString()))
        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow("player_name"))
            players.add(Player(name))
        }
        cursor.close()
        return players
    }

    fun getIsMatchStarted(matchId: String): Boolean {
        val db = readableDatabase
        val query = "SELECT 1 FROM $TABLE_MATCHES WHERE match_id = ? AND is_started = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId,"1"))

        val exists = cursor.moveToFirst() // returns true if the query returned a row, false otherwise
        cursor.close()
        return exists
    }
}

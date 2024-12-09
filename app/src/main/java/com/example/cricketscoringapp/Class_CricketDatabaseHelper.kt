package com.example.cricketscoringapp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

// SQLite helper class
class CricketDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        //Database name
        const val DATABASE_NAME = "cricket.db"
        const val DATABASE_VERSION = 15

        //Table Names
        const val TABLE_PLAYERS = "players"
        const val TABLE_MATCHES = "matches"
        const val TABLE_TEAMS = "teams"
        const val TABLE_BATTINGSTATS = "battingstats"
        const val TABLE_BOWLINGSTATS = "bowlingstats"
        const val VIEW_MATCHSTATS = "vwmatchstats"
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
            first_batting_team_striker TEXT,
            first_batting_team_nonstriker TEXT,
            second_batting_team_captain TEXT,
            second_batting_team_bowler TEXT,
            second_batting_team_keeper TEXT,
            winning_team_captain TEXT,
            is_started INTEGER,
            is_finished INTEGER,
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
            dotballs INTEGER,
            wicket_description TEXT,
            wicket_type TEXT,
            wicket_bowler TEXT,
            wicket_fielder TEXT,
            PRIMARY KEY (match_id, batting_order)
        )
    """

    private val createBOWLINGSTATS = """
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
            fours INTEGER,
            sixes INTEGER,
            dotballs INTEGER,
            over_record TEXT,
            PRIMARY KEY (match_id, bowling_order)
        )
    """

    private val createMATCHSTATSVIEW = """
    CREATE VIEW $VIEW_MATCHSTATS AS
    SELECT 
        tm.match_id,
        tm.team_id,
        tm.player_name,
        CASE 
            WHEN tm.is_captain = 1 THEN 'YES'  
            ELSE ''                          
        END AS is_captain,
        (SELECT COUNT(*) 
            FROM $TABLE_BATTINGSTATS bs 
            WHERE tm.player_name = bs.wicket_fielder
            AND tm.match_id = bs.match_id
            AND bs.wicket_type LIKE 'caught%'
        ) AS catches,
        (SELECT COUNT(*) 
            FROM $TABLE_BATTINGSTATS bs 
            WHERE tm.player_name = bs.wicket_fielder
            AND tm.match_id = bs.match_id
            AND bs.wicket_type = 'stumped'
        ) AS stumpings,
        (SELECT COUNT(*) 
            FROM $TABLE_BATTINGSTATS bs 
            WHERE tm.player_name = bs.wicket_fielder
            AND tm.match_id = bs.match_id
            AND bs.wicket_type = 'run out'
        ) AS runOuts,
        inning1.runs AS firstInningsRunsScored,
        inning1.balls AS firstInningsBallsFaced,
        inning1.fours AS firstInningsFours,
        inning1.sixes AS firstInningsSixes,
        inning1.dotballs AS firstInningDotBalls,
        CASE 
            WHEN inning1.batting_status = 'out' THEN 'Out'
            WHEN inning1.batting_status = 'not out' THEN 'Not out'
            ELSE 'DNB'
        END AS firstInningBattingStatus,
        inning1.wicket_type AS firstInningHowOut,
        inning1.wicket_bowler AS firstInningBowler,
        CASE 
            WHEN inning1.wicket_type = 'caught' THEN inning1.wicket_fielder  
            ELSE ''                          
        END AS firstInningCaughtBy,
        CASE 
            WHEN inning1.wicket_type = 'run out' THEN inning1.wicket_fielder  
            ELSE ''                          
        END AS firstInningRunOutBy,
        inning2.runs AS secondInningsRunsScored,
        inning2.balls AS secondInningsBallsFaced,
        inning2.fours AS secondInningsFours,
        inning2.sixes AS secondInningsSixes,
        inning2.dotballs AS secondInningDotBalls,
        CASE 
            WHEN inning2.batting_status = 'out' THEN 'Out'
            WHEN inning2.batting_status = 'not out' THEN 'Not out'
            ELSE 'DNB'
        END AS secondInningBattingStatus,
        inning2.wicket_type AS secondInningHowOut,
        inning2.wicket_bowler AS secondInningBowler,
        CASE 
            WHEN inning2.wicket_type = 'caught' THEN inning2.wicket_fielder  
            ELSE ''                          
        END AS secondInningCaughtBy,
        CASE 
            WHEN inning2.wicket_type = 'run out' THEN inning2.wicket_fielder  
            ELSE ''                          
        END AS secondInningRunOutBy,
        '' AS mBowler,
        SUM(bowling.over) AS oversBowled,
        SUM(bowling.runs) AS runsConceded,
        SUM(bowling.wickets) AS wickets,
        SUM(bowling.maiden) AS maidens,
        SUM(bowling.sixes) AS sixes,                
        SUM(bowling.fours) AS fours,
        SUM(bowling.dotballs) AS dotballs,
        SUM(bowling.wides) AS wides,
        SUM(bowling.noballs) AS noballs,
        '' AS winLossTie
    FROM 
        $TABLE_TEAMS tm
    LEFT JOIN $TABLE_BATTINGSTATS inning1 
        ON inning1.match_id = tm.match_id 
        AND inning1.team_id = tm.team_id
        AND inning1.player_name = tm.player_name
        AND inning1.batting_turn = 1
    LEFT JOIN $TABLE_BATTINGSTATS inning2 
        ON inning2.match_id = tm.match_id 
        AND inning2.team_id = tm.team_id
        AND inning2.player_name = tm.player_name
        AND inning2.batting_turn = 2
    LEFT JOIN $TABLE_BOWLINGSTATS bowling 
        ON bowling.match_id = tm.match_id 
        AND bowling.team_id = tm.team_id
        AND bowling.player_name = tm.player_name
    GROUP BY 
        tm.match_id,tm.team_id,tm.player_name
    ORDER BY tm.team_id, tm.is_captain DESC
"""

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(createPLAYERSTABLE)
        db?.execSQL(createMATCHESTABLE)
        db?.execSQL(createTEAMSTABLE)
        db?.execSQL(createBATTINGSTATS)
        db?.execSQL(createBOWLINGSTATS)
        db?.execSQL(createMATCHSTATSVIEW)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_PLAYERS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_MATCHES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_TEAMS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_BATTINGSTATS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_BOWLINGSTATS")

        db?.execSQL("DROP VIEW IF EXISTS $VIEW_MATCHSTATS")

        onCreate(db)
    }

    //GET FUNCTIONS ********************************************************************************

    private fun getNextBattingOrderNo(matchId: String): Int {
        val db = readableDatabase
        val query = "SELECT MAX(batting_order) AS max_batting_order FROM $TABLE_BATTINGSTATS WHERE match_id = ?"
        val cursor = db.rawQuery(query, arrayOf(matchId))

        cursor.use { // Auto-close the cursor after use
            if (it.moveToFirst()) {
                return it.getIntOrZero("max_batting_order") + 1
            }
        }
        // If no records found, return 1
        return 1
    }

    private fun getNextBattingTurnNo(matchId: String, playerName: String): Int {
        val db = readableDatabase
        val query = "SELECT batting_turn FROM $TABLE_BATTINGSTATS WHERE match_id = ? AND player_name = ?"
        val cursor = db.rawQuery(query, arrayOf(matchId,playerName))

        cursor.use { // Auto-close the cursor after use
            if (it.moveToFirst()) {
                return it.getIntOrZero("batting_turn") + 1
            }
        }
        // If no records found, return 1
        return 1
    }

    private fun getNextBowlingOrderNo(matchId: String): Int {
        val db = readableDatabase
        val query = "SELECT MAX(bowling_order) AS max_bowling_order FROM $TABLE_BOWLINGSTATS WHERE match_id = ?"
        val cursor = db.rawQuery(query, arrayOf(matchId))

        cursor.use { // Auto-close the cursor after use
            if (it.moveToFirst()) {
                return it.getIntOrZero("max_bowling_order") + 1
            }
        }
        // If no records found, return 1
        return 1
    }

    private fun getNextBowlingTurnNo(matchId: String, playerName: String): Int {
        val db = readableDatabase
        val query = "SELECT bowling_turn FROM $TABLE_BOWLINGSTATS WHERE match_id = ? AND player_name = ?"
        val cursor = db.rawQuery(query, arrayOf(matchId,playerName))

        cursor.use { // Auto-close the cursor after use
            if (it.moveToFirst()) {
                return it.getIntOrZero("bowling_turn") + 1
            }
        }
        // If no records found, return 1
        return 1
    }

    fun getAllPlayers(): List<Player> {
        val players = mutableListOf<Player>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_PLAYERS ORDER BY name", null)
        while (cursor.moveToNext()) {
            val name = cursor.getStringOrEmpty("name")
            players.add(Player(name))
        }
        cursor.close()
        return players
    }

    fun getMatchId(): String {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT match_id FROM $TABLE_MATCHES WHERE is_finished = ? LIMIT 1", arrayOf("0"))
        val matchId:String

        if (cursor.moveToFirst()) {
            matchId = cursor.getStringOrEmpty("match_id")
            return matchId
        } else {
            //Use this to get unique universal identifier to use for Match ID
            matchId = UUID.randomUUID().toString()
            addMatch(matchId)
        }
        cursor.close()
        return matchId
    }

    fun getBattingTeamCaptain(matchId: String, whichTeam: Int) : String {
        val db = readableDatabase

        val batsman = if (whichTeam == 1) {
            "first_batting_team_captain"
        } else {
            "second_batting_team_captain"
        }

        val query = "SELECT $batsman FROM $TABLE_MATCHES WHERE match_id = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId))

        val captainName = if (cursor.moveToFirst()) {
            cursor.getStringOrEmpty(batsman)
        } else {
            ""
        }
        cursor.close()
        return captainName
    }

    fun getFirstBattingTeamStriker(matchId: String) : String {
        val db = readableDatabase
        val query = "SELECT first_batting_team_striker FROM $TABLE_MATCHES WHERE match_id = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId))

        val player = if (cursor.moveToFirst()) {
            cursor.getStringOrEmpty("first_batting_team_striker")
        } else {
            ""
        }
        cursor.close()
        return player
    }

    fun getFirstBattingTeamNonStriker(matchId: String) : String {
        val db = readableDatabase
        val query = "SELECT first_batting_team_nonstriker FROM $TABLE_MATCHES WHERE match_id = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId))

        val player = if (cursor.moveToFirst()) {
            cursor.getStringOrEmpty("first_batting_team_nonstriker")
        } else {
            ""
        }
        cursor.close()
        return player
    }

    fun getSecondBattingTeamBowler(matchId: String) : String {
        val db = readableDatabase
        val query = "SELECT second_batting_team_bowler FROM $TABLE_MATCHES WHERE match_id = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId))

        val player = if (cursor.moveToFirst()) {
            cursor.getStringOrEmpty("second_batting_team_bowler")
        } else {
            ""
        }
        cursor.close()
        return player
    }

    fun getSecondBattingTeamKeeper(matchId: String) : String {
        val db = readableDatabase
        val query = "SELECT second_batting_team_keeper FROM $TABLE_MATCHES WHERE match_id = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId))

        val player = if (cursor.moveToFirst()) {
            cursor.getStringOrEmpty("second_batting_team_keeper")
        } else {
            ""
        }
        cursor.close()
        return player
    }

    fun getMatches() : List<Match> {
        val matches = mutableListOf<Match>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_MATCHES", null)
        while (cursor.moveToNext()) {
            val matchId = cursor.getStringOrEmpty("match_id")
            val firstBattingTeamCaptain = cursor.getStringOrEmpty("first_batting_team_captain")
            val secondBattingTeamCaptain = cursor.getStringOrEmpty("second_batting_team_captain")
            val winningTeamCaptain = cursor.getStringOrEmpty("winning_team_captain")
            val isStarted = cursor.getIntOrZero("is_started") == 1
            val isFinished = cursor.getIntOrZero("is_finished") == 1
            val isSynced = cursor.getIntOrZero("is_synced") == 1
            matches.add(Match(matchId,firstBattingTeamCaptain,secondBattingTeamCaptain,winningTeamCaptain,isStarted,isFinished,isSynced))
        }
        cursor.close()
        return matches
    }

    fun getMatchStats(matchId: String): List<UploadRow> {
        val matchStats = mutableListOf<UploadRow>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $VIEW_MATCHSTATS WHERE match_id = ? ORDER BY team_id", arrayOf(matchId))
        while (cursor.moveToNext()) {
            matchStats.add(
                UploadRow(
                    cursor.getStringOrEmpty("player_name"),
                    cursor.getStringOrEmpty("is_captain"),
                    cursor.getIntOrZero("catches"),
                    cursor.getIntOrZero("stumpings"),
                    cursor.getIntOrZero("runOuts"),
                    cursor.getIntOrZero("firstInningsRunsScored"),
                    cursor.getIntOrZero("firstInningsBallsFaced"),
                    cursor.getIntOrZero("firstInningsFours"),
                    cursor.getIntOrZero("firstInningsSixes"),
                    cursor.getIntOrZero("firstInningDotBalls"),
                    cursor.getStringOrEmpty("firstInningBattingStatus"),
                    cursor.getStringOrEmpty("firstInningHowOut"),
                    cursor.getStringOrEmpty("firstInningBowler"),
                    cursor.getStringOrEmpty("firstInningCaughtBy"),
                    cursor.getStringOrEmpty("firstInningRunOutBy"),
                    cursor.getIntOrZero("secondInningsRunsScored"),
                    cursor.getIntOrZero("secondInningsBallsFaced"),
                    cursor.getIntOrZero("secondInningsFours"),
                    cursor.getIntOrZero("secondInningsSixes"),
                    cursor.getIntOrZero("secondInningDotBalls"),
                    cursor.getStringOrEmpty("secondInningBattingStatus"),
                    cursor.getStringOrEmpty("secondInningHowOut"),
                    cursor.getStringOrEmpty("secondInningBowler"),
                    cursor.getStringOrEmpty("secondInningCaughtBy"),
                    cursor.getStringOrEmpty("secondInningRunOutBy"),
                    cursor.getStringOrEmpty("mBowler"),
                    cursor.getDoubleOrZero("oversBowled"),
                    cursor.getIntOrZero("runsConceded"),
                    cursor.getIntOrZero("wickets"),
                    cursor.getIntOrZero("maidens"),
                    cursor.getIntOrZero("sixes"),
                    cursor.getIntOrZero("fours"),
                    cursor.getIntOrZero("dotballs"),
                    cursor.getIntOrZero("wides"),
                    cursor.getIntOrZero("noballs"),
                    cursor.getStringOrEmpty("winLossTie")
                )
            )
        }
        cursor.close()
        return matchStats
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
            val name = cursor.getStringOrEmpty("player_name")
            players.add(Player(name))
        }
        cursor.close()
        return players
    }

    fun getTeamForPlayer(matchId: String,playerName: String) : Int {
        val db = readableDatabase
        val query = "SELECT team_id FROM $TABLE_TEAMS WHERE match_id = ? AND player_name = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId,playerName))
        val teamId = if (cursor.moveToFirst()) {
            cursor.getIntOrZero("team_id")
        } else {
            0
        }

        cursor.close()
        return teamId
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
            cursor.getStringOrEmpty("player_name")
        } else {
            ""
        }
        cursor.close()
        return captainName
    }

    fun getBatsmanByStatus(matchId: String, battingStatus: String) : BatsmanStats {
        val db = readableDatabase
        // Initialize with default values
        var batsmanStats = BatsmanStats(
            name = mutableStateOf(value = ""),
            runs = mutableIntStateOf(value = 0),
            balls = mutableIntStateOf(value = 0),
            fours = mutableIntStateOf(value = 0),
            sixes = mutableIntStateOf(value = 0),
            dotballs = mutableIntStateOf(value = 0),
            wicketDescription = mutableStateOf(value = ""),
            active = mutableStateOf(value = false)
        )

        val query = "SELECT * FROM $TABLE_BATTINGSTATS WHERE match_id = ? AND batting_status = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId,battingStatus))

        while (cursor.moveToNext()) {
            val playerName: String = cursor.getStringOrEmpty("player_name")
            val runs: Int = cursor.getIntOrZero("runs")
            val balls: Int = cursor.getIntOrZero("balls")
            val fours: Int = cursor.getIntOrZero("fours")
            val sixes: Int = cursor.getIntOrZero("sixes")
            val dotballs: Int = cursor.getIntOrZero("dotballs")
            val wicketDescription: String = cursor.getStringOrEmpty("wicket_description")

            // Assign the retrieved values to batsmanStats
            batsmanStats = BatsmanStats(
                name = mutableStateOf(value = playerName),
                runs = mutableIntStateOf(value = runs),
                balls = mutableIntStateOf(value = balls),
                fours = mutableIntStateOf(value = fours),
                sixes = mutableIntStateOf(value = sixes),
                dotballs = mutableIntStateOf(value = dotballs),
                wicketDescription = mutableStateOf(value = wicketDescription),
                active = mutableStateOf(value = battingStatus == "striker")
            )
        }
        cursor.close()
        return batsmanStats
    }

    fun getCurrentBowler(matchId: String) : String {
        val db = readableDatabase
        val query = "SELECT player_name FROM $TABLE_BOWLINGSTATS WHERE match_id = ? AND bowling_status = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId,"bowling"))

        val playerName = if (cursor.moveToFirst()) {
            cursor.getStringOrEmpty("player_name")
        } else {
            ""
        }
        cursor.close()
        return playerName
    }

    fun getCurrentBowlerStats(matchId: String) : BowlerStats {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_BOWLINGSTATS WHERE match_id = ? AND bowling_status = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId, "bowling"))

        return if (cursor.moveToFirst()) {
            BowlerStats(
                name = mutableStateOf(cursor.getStringOrEmpty("player_name")),
                over = mutableDoubleStateOf(cursor.getDoubleOrZero("over")),
                maiden = mutableIntStateOf(cursor.getIntOrZero("maiden")),
                runs = mutableIntStateOf(cursor.getIntOrZero("runs")),
                wickets = mutableIntStateOf(cursor.getIntOrZero("wickets")),
                noballs = mutableIntStateOf(cursor.getIntOrZero("noballs")),
                wides = mutableIntStateOf(cursor.getIntOrZero("wides")),
                byes = mutableIntStateOf(cursor.getIntOrZero("byes")),
                legbyes = mutableIntStateOf(cursor.getIntOrZero("legbyes")),
                fours = mutableIntStateOf(cursor.getIntOrZero("fours")),
                sixes = mutableIntStateOf(cursor.getIntOrZero("sixes")),
                dotballs = mutableIntStateOf(cursor.getIntOrZero("dotballs")),
                keepername = mutableStateOf(cursor.getStringOrEmpty("keeper_name")),
                overrecord = mutableStateOf(cursor.getStringOrEmpty("over_record"))
            )
        } else {
            BowlerStats()  // Return an empty/default BowlerStats if no data found
        }.also {
            cursor.close()
        }
    }

    fun getConsolidatedBowlerStats(matchId: String,bowlerName: String): BowlerStats {
        val db = readableDatabase
        val query = """
            SELECT 
                player_name, 
                SUM(over) AS total_overs, 
                SUM(maiden) AS total_maidens, 
                SUM(runs) AS total_runs, 
                SUM(wickets) AS total_wickets, 
                SUM(noballs) AS total_noballs, 
                SUM(wides) AS total_wides, 
                SUM(byes) AS total_byes, 
                SUM(legbyes) AS total_legbyes, 
                SUM(fours) AS total_fours, 
                SUM(sixes) AS total_sixes,
                SUM(dotballs) AS total_dotballs
            FROM 
                $TABLE_BOWLINGSTATS
            WHERE 
                match_id = ? AND player_name = ?
            GROUP BY 
                player_name
            """
        val cursor = db.rawQuery(query, arrayOf(matchId,bowlerName))

        return if (cursor.moveToFirst()) {
            BowlerStats(
                name = mutableStateOf(cursor.getStringOrEmpty("player_name")),
                over = mutableDoubleStateOf(cursor.getDoubleOrZero("total_overs")),
                maiden = mutableIntStateOf(cursor.getIntOrZero("total_maidens")),
                runs = mutableIntStateOf(cursor.getIntOrZero("total_runs")),
                wickets = mutableIntStateOf(cursor.getIntOrZero("total_wickets")),
                noballs = mutableIntStateOf(cursor.getIntOrZero("total_noballs")),
                wides = mutableIntStateOf(cursor.getIntOrZero("total_wides")),
                byes = mutableIntStateOf(cursor.getIntOrZero("total_byes")),
                legbyes = mutableIntStateOf(cursor.getIntOrZero("total_legbyes")),
                fours = mutableIntStateOf(cursor.getIntOrZero("total_fours")),
                sixes = mutableIntStateOf(cursor.getIntOrZero("total_sixes")),
                dotballs = mutableIntStateOf(cursor.getIntOrZero("total_dotballs")),
                keepername = mutableStateOf(""),
                overrecord = mutableStateOf("")
            )
        } else {
            BowlerStats()  // Return an empty/default BowlerStats if no data found
        }.also {
            cursor.close()
        }
    }

    fun getLastBowler(matchId: String, teamId: Int) : String {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_BOWLINGSTATS WHERE match_id = ? AND team_id = ? AND bowling_status = ? ORDER BY bowling_order DESC LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId,teamId.toString(),"bowled"))
        var lastBowler = ""
        if (cursor.moveToFirst()) {
            lastBowler = cursor.getStringOrEmpty("player_name")
        }
        cursor.close()
        return lastBowler
    }

    fun getLastKeeper(matchId: String, teamId: Int) : String {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_BOWLINGSTATS WHERE match_id = ? AND team_id = ? AND bowling_status = ? ORDER BY bowling_order DESC LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId,teamId.toString(),"bowling"))
        var lastKeeper = ""
        if (cursor.moveToFirst()) {
            lastKeeper = cursor.getStringOrEmpty("keeper_name")
        }
        cursor.close()
        return lastKeeper
    }

    fun getCurrentKeeper(matchId: String) : String {
        val db = readableDatabase
        val query = "SELECT keeper_name FROM $TABLE_BOWLINGSTATS WHERE match_id = ? AND bowling_status = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId,"bowling"))

        val playerName = if (cursor.moveToFirst()) {
            cursor.getStringOrEmpty("keeper_name")
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
            val playerName = cursor.getStringOrEmpty("player_name")
            val overs: String = cursor.getStringOrEmpty("over")
            bowlers.add(Bowler(playerName,overs.toDouble()))
        }
        cursor.close()
        return bowlers
    }

    fun getBatsmanStats(matchId: String, playerName: String): BatsmanStats {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_BATTINGSTATS WHERE match_id = ? AND player_name = ? ORDER BY batting_order DESC",
            arrayOf(matchId, playerName)
        )

        val batsmanStats = cursor.use {
            if (it.moveToNext()) {
                BatsmanStats(
                    name = mutableStateOf(playerName),
                    runs = mutableIntStateOf(it.getIntOrZero("runs")),
                    balls = mutableIntStateOf(it.getIntOrZero("balls")),
                    fours = mutableIntStateOf(it.getIntOrZero("fours")),
                    sixes = mutableIntStateOf(it.getIntOrZero("sixes")),
                    dotballs = mutableIntStateOf(it.getIntOrZero("dotballs")),
                    wicketDescription =  mutableStateOf(it.getStringOrEmpty("wicket_description")),
                    active = mutableStateOf(it.getStringOrEmpty("batting_status") == "striker")
                )
            } else {
                // Return default values if no stats are found
                BatsmanStats(
                    name = mutableStateOf(playerName),
                    runs = mutableIntStateOf(0),
                    balls = mutableIntStateOf(0),
                    fours = mutableIntStateOf(0),
                    sixes = mutableIntStateOf(0),
                    dotballs = mutableIntStateOf(0),
                    wicketDescription = mutableStateOf(""),
                    active = mutableStateOf(false)
                )
            }
        }

        return batsmanStats
    }

    fun getTeamBattingStats(matchId: String, teamId: Int): List<BatsmanStats> {
        val batsmen = mutableListOf<BatsmanStats>() // Initialize the list to store batsman stats
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_BATTINGSTATS WHERE match_id = ? AND team_id = ? ORDER BY batting_order",
            arrayOf(matchId, teamId.toString())
        )

        cursor.use {  // Ensure the cursor is properly closed after use
            while (it.moveToNext()) {  // Iterate through all records in the cursor
                val batsmanStats = BatsmanStats(
                    name = mutableStateOf(it.getStringOrEmpty("player_name")),
                    runs = mutableIntStateOf(it.getIntOrZero("runs")),
                    balls = mutableIntStateOf(it.getIntOrZero("balls")),
                    fours = mutableIntStateOf(it.getIntOrZero("fours")),
                    sixes = mutableIntStateOf(it.getIntOrZero("sixes")),
                    dotballs = mutableIntStateOf(it.getIntOrZero("dotballs")),
                    wicketDescription = mutableStateOf(it.getStringOrEmpty("wicket_description")),
                    active = mutableStateOf(it.getStringOrEmpty("batting_status") == "striker")
                )
                batsmen.add(batsmanStats) // Add each batsman stats object to the list
            }
        }

        return batsmen  // Return the list of BatsmanStats
    }

    fun getTeamBowlingStats(matchId: String, teamId: Int): List<BowlerStats> {
        val bowlers = mutableListOf<BowlerStats>() // Initialize the list to store batsman stats
        val db = readableDatabase
        val query = """
            SELECT 
                player_name, 
                SUM(over) AS total_overs, 
                SUM(maiden) AS total_maidens, 
                SUM(runs) AS total_runs, 
                SUM(wickets) AS total_wickets, 
                SUM(noballs) AS total_noballs, 
                SUM(wides) AS total_wides, 
                SUM(byes) AS total_byes, 
                SUM(legbyes) AS total_legbyes, 
                SUM(fours) AS total_fours, 
                SUM(sixes) AS total_sixes,
                SUM(dotballs) AS total_dotballs
            FROM 
                $TABLE_BOWLINGSTATS
            WHERE 
                match_id = ? AND team_id = ?
            GROUP BY 
                player_name
            """
        val cursor = db.rawQuery(query, arrayOf(matchId,teamId.toString()))

        cursor.use {  // Ensure the cursor is properly closed after use
            while (it.moveToNext()) {  // Iterate through all records in the cursor
                val bowlerStats = BowlerStats(
                    name = mutableStateOf(it.getStringOrEmpty("player_name")),
                    over = mutableDoubleStateOf(it.getDoubleOrZero("total_overs")),
                    maiden = mutableIntStateOf(it.getIntOrZero("total_maidens")),
                    runs = mutableIntStateOf(it.getIntOrZero("total_runs")),
                    wickets = mutableIntStateOf(it.getIntOrZero("total_wickets")),
                    noballs = mutableIntStateOf(it.getIntOrZero("total_noballs")),
                    wides = mutableIntStateOf(it.getIntOrZero("total_wides")),
                    fours = mutableIntStateOf(it.getIntOrZero("total_fours")),
                    sixes = mutableIntStateOf(it.getIntOrZero("total_sixes")),
                    dotballs = mutableIntStateOf(it.getIntOrZero("total_dotballs")),
                    byes = mutableIntStateOf(it.getIntOrZero("total_byes")),
                    legbyes = mutableIntStateOf(it.getIntOrZero("total_legbyes"))
                )
                bowlers.add(bowlerStats) // Add each bowler stats object to the list
            }
        }

        return bowlers  // Return the list of BowlerStats
    }

    fun getFullyBattedAlreadyPlayers(matchId: String, teamId: Int) : List<Player> {
        val players = mutableListOf<Player>()
        val db = readableDatabase
        val query = "SELECT player_name FROM $TABLE_BATTINGSTATS WHERE match_id = ? AND team_id = ? AND batting_turn = 2 AND batting_status = 'out'"
        val cursor = db.rawQuery(query, arrayOf(matchId,teamId.toString()))
        while (cursor.moveToNext()) {
            val name = cursor.getStringOrEmpty("player_name")
            players.add(Player(name))
        }
        cursor.close()
        return players
    }

    fun getBattingCount(matchId: String, playerName: String) : Int {
        val db = readableDatabase
        val query = "SELECT COUNT(*) AS outCount FROM $TABLE_BATTINGSTATS WHERE match_id = ? AND player_name = ? AND batting_status = ?"
        val cursor = db.rawQuery(query, arrayOf(matchId,playerName,"out"))

        val battingCount = if (cursor.moveToFirst()) {
            cursor.getInt(0)
        } else {
            0
        }
        cursor.close()
        return battingCount
    }

    fun getIsMatchStarted(matchId: String): Boolean {
        val db = readableDatabase
        val query = "SELECT is_started FROM $TABLE_MATCHES WHERE match_id = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId))

        val isStarted = if (cursor.moveToFirst())  {
            cursor.getIntOrZero("is_started") == 1
        } else {
            false
        }
        cursor.close()
        return isStarted
    }

    fun getDateForMatch(matchId: String): String {
        val db = readableDatabase
        val query = "SELECT match_date FROM $TABLE_MATCHES WHERE match_id = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId))

        val matchDate = if (cursor.moveToFirst())  {
            cursor.getStringOrEmpty("match_date")
        } else {
            ""
        }
        cursor.close()
        return matchDate
    }

    fun getTeamStats(matchId: String, currentTeamId: Int, captainName: String): TeamStats {
        val otherTeamId = if (currentTeamId == 1) 2 else 1
        // Initialize with default values
        val teamStats: TeamStats

        var overs = 0.0
        var extras = 0
        var runs = 0
        var wickets = 0

        val db = readableDatabase
        var query = "SELECT SUM(over) AS overs FROM $TABLE_BOWLINGSTATS WHERE match_id = ? AND team_id = ? LIMIT 1"
        var cursor = db.rawQuery(query, arrayOf(matchId,otherTeamId.toString()))

        if (cursor.moveToNext()) {
            overs = cursor.getDoubleOrZero("overs")
        }
        cursor.close()

        query = "SELECT SUM(byes + legbyes + wides + noballs) AS extras FROM $TABLE_BOWLINGSTATS WHERE match_id = ? AND team_id = ? LIMIT 1"
        cursor = db.rawQuery(query, arrayOf(matchId,otherTeamId.toString()))

        if (cursor.moveToNext()) {
            extras = cursor.getIntOrZero("extras")
        }
        cursor.close()

        query = "SELECT SUM(runs) AS runs FROM $TABLE_BATTINGSTATS WHERE match_id = ? AND team_id = ? LIMIT 1"
        cursor = db.rawQuery(query, arrayOf(matchId,currentTeamId.toString()))

        if (cursor.moveToNext()) {
            runs = cursor.getIntOrZero("runs")
        }
        cursor.close()

        query = "SELECT COUNT(*) AS wickets FROM $TABLE_BATTINGSTATS WHERE match_id = ? AND team_id = ? AND batting_status = ? LIMIT 1"
        cursor = db.rawQuery(query, arrayOf(matchId,currentTeamId.toString(),"out"))

        if (cursor.moveToNext()) {
            wickets = cursor.getIntOrZero("wickets")
        }
        cursor.close()

        query = "SELECT COUNT(*) AS wickets FROM $TABLE_BATTINGSTATS WHERE match_id = ? AND team_id = ? AND batting_status = ? LIMIT 1"
        cursor = db.rawQuery(query, arrayOf(matchId,currentTeamId.toString(),"striker"))

        val active = if (cursor.moveToFirst()) {
            cursor.getInt(0) > 0  // Check if the count is greater than 0
        } else {
            false
        }
        cursor.close()

        teamStats =  TeamStats(
            name = mutableStateOf(captainName),
            overs = mutableDoubleStateOf(overs),
            inningScore = mutableIntStateOf(runs + extras + (-3 * wickets)),
            inningWickets = mutableIntStateOf(wickets),
            active = mutableStateOf(active)
        )
        return teamStats

    }

    fun getTeamWickets(matchId: String, teamId: Int) : Int {
        val db = readableDatabase
        val query = "SELECT COUNT(*) AS wickets FROM $TABLE_BATTINGSTATS WHERE match_id = ? AND team_id = ? AND batting_status = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId,teamId.toString(),"out"))

        val wickets = if (cursor.moveToFirst()) {
            cursor.getIntOrZero("wickets")
        } else {
            0
        }
        cursor.close()
        return wickets
    }

    fun getTeamBatters(matchId: String, teamId: Int) : Int {
        val db = readableDatabase
        val query = "SELECT COUNT(*) AS batters FROM $TABLE_BATTINGSTATS WHERE match_id = ? AND team_id = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId,teamId.toString()))

        val wickets = if (cursor.moveToFirst()) {
            cursor.getIntOrZero("batters")
        } else {
            0
        }
        cursor.close()
        return wickets
    }

    fun getTeamOversBowled(matchId: String, teamId: Int): Double {
        val db = readableDatabase
        val query = "SELECT SUM(over) AS overs FROM $TABLE_BOWLINGSTATS WHERE match_id = ? AND team_id = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId,teamId.toString()))

        val overs = if (cursor.moveToFirst()) {
            cursor.getDoubleOrZero("overs")
        } else {
            0.0
        }
        cursor.close()
        return overs
    }

    fun getBowlersOversBowled(matchId: String, bowlingTeamId: Int, bowlerName: String): String {
        val db = readableDatabase
        val query = "SELECT SUM(over) AS overs FROM $TABLE_BOWLINGSTATS WHERE match_id = ? AND team_id = ? AND player_name = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId,bowlingTeamId.toString(),bowlerName))
        var oversBowled = ""
        if (cursor.moveToFirst()) {
            oversBowled = cursor.getDoubleOrZero("overs").toString()
        }
        cursor.close()
        return oversBowled
    }

    fun getStriker(matchId: String) : String {
        val db = readableDatabase
        val query = "SELECT player_name FROM $TABLE_BATTINGSTATS WHERE match_id = ? AND batting_status = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId,"striker"))

        val playerName = if (cursor.moveToFirst()) {
            cursor.getStringOrEmpty("player_name")
        } else {
            ""
        }
        cursor.close()
        return playerName
    }

    fun getNonStriker(matchId: String) : String {
        val db = readableDatabase
        val query = "SELECT player_name FROM $TABLE_BATTINGSTATS WHERE match_id = ? AND batting_status = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId,"non-striker"))

        val playerName = if (cursor.moveToFirst()) {
            cursor.getStringOrEmpty("player_name")
        } else {
            ""
        }
        cursor.close()
        return playerName
    }

    fun getBowler(matchId: String) : String {
        val db = readableDatabase
        val query = "SELECT player_name FROM $TABLE_BOWLINGSTATS WHERE match_id = ? AND bowling_status = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId,"bowling"))

        val playerName = if (cursor.moveToFirst()) {
            cursor.getStringOrEmpty("player_name")
        } else {
            ""
        }
        cursor.close()
        return playerName
    }

    fun getKeeper(matchId: String) : String {
        val db = readableDatabase
        val query = "SELECT keeper_name FROM $TABLE_BOWLINGSTATS WHERE match_id = ? AND bowling_status = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId,"bowling"))

        val playerName = if (cursor.moveToFirst()) {
            cursor.getStringOrEmpty("keeper_name")
        } else {
            ""
        }
        cursor.close()
        return playerName
    }

    fun getDBVersion() : Int {
        return DATABASE_VERSION
    }

    //DELETE FUNCTIONS *****************************************************************************

    fun deletePlayer(name: String): Int {
        val db = writableDatabase
        return db.delete(TABLE_PLAYERS, "name = ?", arrayOf(name))
    }

    fun deleteMatch(matchId: String) {
        val db = writableDatabase
        db.delete(TABLE_TEAMS, "match_id = ?", arrayOf(matchId))
        db.delete(TABLE_BATTINGSTATS, "match_id = ?", arrayOf(matchId))
        db.delete(TABLE_BOWLINGSTATS, "match_id = ?", arrayOf(matchId))
        db.delete(TABLE_MATCHES, "match_id = ?", arrayOf(matchId))
    }

    fun deleteBatsman(matchId: String, playerName: String): Int {
        val dbRead = readableDatabase
        val cursor = dbRead.rawQuery(
            "SELECT * FROM $TABLE_BATTINGSTATS WHERE match_id = ? AND player_name = ? ORDER BY batting_order DESC LIMIT 1",
            arrayOf(matchId, playerName)
        )

        if (cursor.moveToNext()) {
            val battingOrder: Int = cursor.getInt(cursor.getColumnIndexOrThrow("batting_order"))
            cursor.close()

            val dbWrite = writableDatabase
            return dbWrite.delete(TABLE_BATTINGSTATS, "match_id = ? AND player_name = ? AND batting_order = ?", arrayOf(matchId,playerName,battingOrder.toString()))
        }

        return 0
    }

    fun deleteCurrentBowler(matchId: String) : Int {
        val currentBowler = getCurrentBowler(matchId)
        val db = writableDatabase
        return db.delete(TABLE_BOWLINGSTATS, "player_name = ? AND bowling_status = ?", arrayOf(currentBowler,"bowling"))
    }

    fun deleteTeamPlayer(matchId: String, teamId: Int, playerName: String): Int {
        val db = writableDatabase
        return db.delete(TABLE_TEAMS, "match_id = ? AND team_id = ? AND player_name = ?", arrayOf(matchId,teamId.toString(),playerName))
    }

    //ADD FUNCTIONS ********************************************************************************

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
        values.put("is_finished",0)
        values.put("is_synced",0)
        db.insert(TABLE_MATCHES, null, values)
        db.close()
    }

    fun addPlayer(name: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("name", name)
        db.insert(TABLE_PLAYERS, null, values)
        db.close()
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

    fun addBattingStats(matchId: String, teamId: Int, playerName: String, battingStatus: String) {
        val db = writableDatabase
        val values = ContentValues()
        values.put("match_id", matchId)
        values.put("team_id", teamId)
        values.put("batting_order",getNextBattingOrderNo(matchId))
        values.put("player_name", playerName)
        values.put("batting_turn", getNextBattingTurnNo(matchId,playerName))
        values.put("batting_status", battingStatus)
        db.insert(TABLE_BATTINGSTATS,null,values)
        db.close()
    }

    fun addBowlingStats(matchId: String, teamId: Int, bowlerName: String, keeperName: String, bowlingStatus: String) {
        val db = writableDatabase
        val values = ContentValues()
        values.put("match_id", matchId)
        values.put("team_id", teamId)
        values.put("bowling_order",getNextBowlingOrderNo(matchId))
        values.put("player_name", bowlerName)
        values.put("bowling_turn", getNextBowlingTurnNo(matchId,bowlerName))
        values.put("bowling_status", bowlingStatus)
        values.put("keeper_name",keeperName)
        db.insert(TABLE_BOWLINGSTATS,null,values)
        db.close()
    }

    //UPDATE FUNCTIONS *****************************************************************************

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

    fun updateMatchIsFinished(matchId: String, winningTeamCaptain: String) : Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("is_started", 0)
            put("is_finished", 1)
            put("winning_team_captain", winningTeamCaptain)
        }
        val whereClause = "match_id = ?"
        val whereArgs = arrayOf(matchId)

        return db.update(TABLE_MATCHES, contentValues, whereClause, whereArgs)
    }

    fun updateMatchOpeningStriker(matchId: String, player: String) : Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("first_batting_team_striker", player)
        }
        val whereClause = "match_id = ?"
        val whereArgs = arrayOf(matchId)

        return db.update(TABLE_MATCHES, contentValues, whereClause, whereArgs)
    }

    fun updateMatchOpeningNonStriker(matchId: String, player: String) : Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("first_batting_team_nonstriker", player)
        }
        val whereClause = "match_id = ?"
        val whereArgs = arrayOf(matchId)

        return db.update(TABLE_MATCHES, contentValues, whereClause, whereArgs)
    }

    fun updateMatchOpeningBowler(matchId: String, player: String) : Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("second_batting_team_bowler", player)
        }
        val whereClause = "match_id = ?"
        val whereArgs = arrayOf(matchId)

        return db.update(TABLE_MATCHES, contentValues, whereClause, whereArgs)
    }

    fun updateMatchOpeningKeeper(matchId: String, player: String) : Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("second_batting_team_keeper", player)
        }
        val whereClause = "match_id = ?"
        val whereArgs = arrayOf(matchId)

        return db.update(TABLE_MATCHES, contentValues, whereClause, whereArgs)
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

    fun updateBattingStats(matchId: String, playerName: String, existingBattingStatus: String, newBattingStatus: String) : Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("batting_status", newBattingStatus)
        }
        val whereClause = "match_id = ? AND player_name = ? AND batting_status = ?"
        val whereArgs = arrayOf(matchId, playerName, existingBattingStatus)

        return db.update(TABLE_BATTINGSTATS, contentValues, whereClause, whereArgs)
    }

    fun updateBattingStats(matchId: String, newBattingStatus: String, batsmanStats: BatsmanStats, wicketDescription: String, wicketType: String, wicketBowler: String, wicketFielder: String) : Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("batting_status", newBattingStatus)
            put("runs", batsmanStats.runs.value)
            put("balls", batsmanStats.balls.value)
            put("fours", batsmanStats.fours.value)
            put("sixes", batsmanStats.sixes.value)
            put("dotballs",batsmanStats.dotballs.value)
            put("wicket_description",wicketDescription)
            put("wicket_type",wicketType)
            put("wicket_bowler",wicketBowler)
            put("wicket_fielder",wicketFielder)
        }
        val whereClause = "match_id = ? AND batting_status = ?"
        val whereArgs = arrayOf(matchId, "striker")

        return db.update(TABLE_BATTINGSTATS, contentValues, whereClause, whereArgs)
    }

    fun updateBowlingStats(matchId: String, bowlingStatus: String) : Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("bowling_status", bowlingStatus)
        }
        val whereClause = "match_id = ? AND bowling_status = ?"
        val whereArgs = arrayOf(matchId, "bowling")

        return db.update(TABLE_BOWLINGSTATS, contentValues, whereClause, whereArgs)
    }

    fun updateBowlingStatsKeeper(matchId: String, teamId: Int, keeperName: String) : Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("keeper_name", keeperName)
        }
        val whereClause = "match_id = ? AND team_id = ? AND bowling_status = ?"
        val whereArgs = arrayOf(matchId, teamId.toString(), "bowling")

        return db.update(TABLE_BOWLINGSTATS, contentValues, whereClause, whereArgs)
    }

    fun updateBowlingStats(matchId: String, bowlerStats: BowlerStats) : Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("over", bowlerStats.over.value)
            put("maiden", bowlerStats.maiden.value)
            put("runs", bowlerStats.runs.value)
            put("wickets", bowlerStats.wickets.value)
            put("noballs", bowlerStats.noballs.value)
            put("wides",bowlerStats.wides.value)
            put("byes",bowlerStats.byes.value)
            put("legbyes",bowlerStats.legbyes.value)
            put("fours",bowlerStats.fours.value)
            put("sixes",bowlerStats.sixes.value)
            put("dotballs",bowlerStats.dotballs.value)
            put("over_record",bowlerStats.overrecord.value)
        }
        val whereClause = "match_id = ? AND bowling_status = ?"
        val whereArgs = arrayOf(matchId, "bowling")

        return db.update(TABLE_BOWLINGSTATS, contentValues, whereClause, whereArgs)
    }

    fun updateStriker(matchId: String,playerName: String) : Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("player_name", playerName)
        }
        val whereClause = "match_id = ? AND batting_status = ?"
        val whereArgs = arrayOf(matchId, "striker")

        return db.update(TABLE_BATTINGSTATS, contentValues, whereClause, whereArgs)
    }

    fun updateNonStriker(matchId: String,playerName: String) : Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("player_name", playerName)
        }
        val whereClause = "match_id = ? AND batting_status = ?"
        val whereArgs = arrayOf(matchId, "non-striker")

        return db.update(TABLE_BATTINGSTATS, contentValues, whereClause, whereArgs)
    }

    fun updateBowler(matchId: String, playerName: String) : Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("player_name", playerName)
        }
        val whereClause = "match_id = ? AND bowling_status = ?"
        val whereArgs = arrayOf(matchId, "bowling")

        return db.update(TABLE_BOWLINGSTATS, contentValues, whereClause, whereArgs)
    }

    fun updateKeeper(matchId: String, playerName: String) : Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("keeper_name", playerName)
        }
        val whereClause = "match_id = ? AND bowling_status = ?"
        val whereArgs = arrayOf(matchId, "bowling")

        return db.update(TABLE_BOWLINGSTATS, contentValues, whereClause, whereArgs)
    }

    //IS FUNCTIONS *********************************************************************************

    fun isPlayerAlreadyExists(playerName: String): Boolean {
        val db = readableDatabase
        val query = "SELECT 1 FROM $TABLE_PLAYERS WHERE name = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(playerName))

        val exists = cursor.moveToFirst() // returns true if the query returned a row, false otherwise
        cursor.close()
        return exists
    }

    fun isTeamPlayer(matchId: String, teamId: Int, playerName: String) : Boolean {
        val db = readableDatabase
        val query = "SELECT 1 FROM $TABLE_TEAMS WHERE match_id = ? AND team_id =? AND player_name = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId,teamId.toString(),playerName))

        val exists = cursor.moveToFirst() // returns true if the query returned a row, false otherwise
        cursor.close()
        return exists
    }

    // Helper extension functions to simplify cursor operations ************************************
    private fun Cursor.getStringOrEmpty(columnName: String): String {
        return getString(getColumnIndexOrThrow(columnName)) ?: ""
    }

    private fun Cursor.getIntOrZero(columnName: String): Int {
        return getInt(getColumnIndexOrThrow(columnName))
    }

    private fun Cursor.getDoubleOrZero(columnName: String): Double {
        return getDouble(getColumnIndexOrThrow(columnName))
    }
}

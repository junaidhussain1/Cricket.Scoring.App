package com.example.cricketscoringapp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

// SQLite helper class
class CricketDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        //Database name
        const val DATABASE_NAME = "cricket.db"
        const val DATABASE_VERSION = 27

        //Table Names
        const val TABLE_PLAYERS = "players"
        const val TABLE_MATCHES = "matches"
        const val TABLE_TEAMS = "teams"
        const val TABLE_BATTINGSTATS = "battingstats"
        const val TABLE_BOWLINGSTATS = "bowlingstats"
        const val TABLE_PARTNERSHIPS = "partnerships"
        const val TABLE_MATCHSNAPSHOT = "matchsnapshot"

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
            no_of_overs_aside INTEGER,
            no_of_players_aside INTEGER,
            side_wall_rule INTEGER,
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
            overvalue REAL,
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

    private val createPARTNERSHIP = """
        CREATE TABLE $TABLE_PARTNERSHIPS (
            match_id TEXT,
            team_id INTEGER,
            wicket_number INTEGER,
            batsman1_name TEXT,
            batsman2_name TEXT,
            runs INTEGER DEFAULT 0,
            balls INTEGER DEFAULT 0,
            PRIMARY KEY (match_id, team_id, wicket_number)
        )
    """

    private val createMATCHSNAPSHOT = """
        CREATE TABLE $TABLE_MATCHSNAPSHOT (
            match_id TEXT,
            entry_no INTEGER,
            state TEXT,
            PRIMARY KEY (match_id, entry_no)
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
        (SELECT player_name 
            FROM $TABLE_TEAMS tm2 
            WHERE tm.match_id = tm2.match_id
            AND tm.team_id = tm2.team_id
            AND tm2.is_captain = 1 
        ) AS captainname,
        (SELECT winning_team_captain 
            FROM $TABLE_MATCHES tma 
            WHERE tm.match_id = tma.match_id 
        ) AS winningcaptain,
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
            WHEN inning1.wicket_type IN ('caught', 'caught behind') THEN inning1.wicket_fielder  
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
            WHEN inning2.wicket_type IN ('caught', 'caught behind') THEN inning2.wicket_fielder  
            ELSE ''                          
        END AS secondInningCaughtBy,
        CASE 
            WHEN inning2.wicket_type = 'run out' THEN inning2.wicket_fielder  
            ELSE ''                          
        END AS secondInningRunOutBy,
        '' AS mBowler,
        SUM(bowling.overvalue) AS oversBowled,
        SUM(bowling.runs) AS runsConceded,
        SUM(bowling.wickets) AS wickets,
        SUM(bowling.maiden) AS maidens,
        SUM(bowling.sixes) AS sixes,                
        SUM(bowling.fours) AS fours,
        SUM(bowling.dotballs) AS dotballs,
        SUM(bowling.wides) AS wides,
        SUM(bowling.noballs) AS noballs,
        CASE
            WHEN (
                SELECT player_name 
                FROM $TABLE_TEAMS tm2 
                WHERE tm.match_id = tm2.match_id
                  AND tm.team_id = tm2.team_id
                  AND tm2.is_captain = 1
            ) = (
                SELECT winning_team_captain
                FROM $TABLE_MATCHES tma
                WHERE tm.match_id = tma.match_id
            )
            THEN 'Win'
            ELSE 'Loss'
        END AS winLossTie
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
        db?.execSQL(createMATCHSNAPSHOT)
        db?.execSQL(createPARTNERSHIP)
        db?.execSQL(createMATCHSTATSVIEW)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        //db?.execSQL("DROP TABLE IF EXISTS $TABLE_PLAYERS")
        //db?.execSQL("DROP TABLE IF EXISTS $TABLE_MATCHES")
        //db?.execSQL("DROP TABLE IF EXISTS $TABLE_TEAMS")
        //db?.execSQL("DROP TABLE IF EXISTS $TABLE_BATTINGSTATS")
        //db?.execSQL("DROP TABLE IF EXISTS $TABLE_BOWLINGSTATS")

        //db?.execSQL("DROP VIEW IF EXISTS $VIEW_MATCHSTATS")
        //db?.execSQL(createMATCHSTATSVIEW)
        //onCreate(db)

        if (oldVersion < 27) {
            db?.execSQL(createPARTNERSHIP)
        }
    }

    //GET FUNCTIONS ********************************************************************************

    private fun getNextBattingOrderNo(matchId: String): Int {
        val db = readableDatabase
        val query =
            "SELECT MAX(batting_order) AS max_batting_order FROM $TABLE_BATTINGSTATS WHERE match_id = ?"
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
        val query =
            "SELECT batting_turn FROM $TABLE_BATTINGSTATS WHERE match_id = ? AND player_name = ?"
        val cursor = db.rawQuery(query, arrayOf(matchId, playerName))

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
        val query =
            "SELECT MAX(bowling_order) AS max_bowling_order FROM $TABLE_BOWLINGSTATS WHERE match_id = ?"
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
        val query =
            "SELECT bowling_turn FROM $TABLE_BOWLINGSTATS WHERE match_id = ? AND player_name = ?"
        val cursor = db.rawQuery(query, arrayOf(matchId, playerName))

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
        val cursor = db.rawQuery(
            "SELECT match_id FROM $TABLE_MATCHES WHERE is_finished = ? LIMIT 1",
            arrayOf("0")
        )
        val matchId: String

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

    fun getBattingTeamCaptain(matchId: String, whichTeam: Int): String {
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

    fun getFirstBattingTeamStriker(matchId: String): String {
        val db = readableDatabase
        val query =
            "SELECT first_batting_team_striker FROM $TABLE_MATCHES WHERE match_id = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId))

        val player = if (cursor.moveToFirst()) {
            cursor.getStringOrEmpty("first_batting_team_striker")
        } else {
            ""
        }
        cursor.close()
        return player
    }

    fun getFirstBattingTeamNonStriker(matchId: String): String {
        val db = readableDatabase
        val query =
            "SELECT first_batting_team_nonstriker FROM $TABLE_MATCHES WHERE match_id = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId))

        val player = if (cursor.moveToFirst()) {
            cursor.getStringOrEmpty("first_batting_team_nonstriker")
        } else {
            ""
        }
        cursor.close()
        return player
    }

    fun getFirstBattingTeamCaptain(matchId: String): String {
        val db = readableDatabase
        val query =
            "SELECT first_batting_team_captain FROM $TABLE_MATCHES WHERE match_id = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId))

        val player = if (cursor.moveToFirst()) {
            cursor.getStringOrEmpty("first_batting_team_captain")
        } else {
            ""
        }
        cursor.close()
        return player
    }

    fun getSecondBattingTeamCaptain(matchId: String): String {
        val db = readableDatabase
        val query =
            "SELECT second_batting_team_captain FROM $TABLE_MATCHES WHERE match_id = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId))

        val player = if (cursor.moveToFirst()) {
            cursor.getStringOrEmpty("second_batting_team_captain")
        } else {
            ""
        }
        cursor.close()
        return player
    }

    fun getSecondBattingTeamBowler(matchId: String): String {
        val db = readableDatabase
        val query =
            "SELECT second_batting_team_bowler FROM $TABLE_MATCHES WHERE match_id = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId))

        val player = if (cursor.moveToFirst()) {
            cursor.getStringOrEmpty("second_batting_team_bowler")
        } else {
            ""
        }
        cursor.close()
        return player
    }

    fun getSecondBattingTeamKeeper(matchId: String): String {
        val db = readableDatabase
        val query =
            "SELECT second_batting_team_keeper FROM $TABLE_MATCHES WHERE match_id = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId))

        val player = if (cursor.moveToFirst()) {
            cursor.getStringOrEmpty("second_batting_team_keeper")
        } else {
            ""
        }
        cursor.close()
        return player
    }

    fun getMatches(): List<Match> {
        val matches = mutableListOf<Match>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_MATCHES ORDER BY match_date DESC ", null)
        while (cursor.moveToNext()) {
            val matchId = cursor.getStringOrEmpty("match_id")
            val firstBattingTeamCaptain = cursor.getStringOrEmpty("first_batting_team_captain")
            val secondBattingTeamCaptain = cursor.getStringOrEmpty("second_batting_team_captain")
            val winningTeamCaptain = cursor.getStringOrEmpty("winning_team_captain")
            val noOfOversAside = cursor.getIntOrZero("no_of_overs_aside")
            val noOfPlayersAside = cursor.getIntOrZero("no_of_players_aside")
            val sideWallRule = cursor.getIntOrZero("side_wall_rule")
            val isStarted = cursor.getIntOrZero("is_started") == 1
            val isFinished = cursor.getIntOrZero("is_finished") == 1
            val isSynced = cursor.getIntOrZero("is_synced") == 1
            matches.add(
                Match(
                    matchId,
                    firstBattingTeamCaptain,
                    secondBattingTeamCaptain,
                    winningTeamCaptain,
                    noOfOversAside,
                    noOfPlayersAside,
                    sideWallRule,
                    isStarted,
                    isFinished,
                    isSynced
                )
            )
        }
        cursor.close()
        return matches
    }

    fun getMatch(matchId: String): Match? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_MATCHES WHERE match_id = ?",
            arrayOf(matchId)
        )

        var match: Match? = null

        if (cursor.moveToFirst()) {
            match = Match(
                matchId = cursor.getStringOrEmpty("match_id"),
                firstBattingTeamCaptain = cursor.getStringOrEmpty("first_batting_team_captain"),
                secondBattingTeamCaptain = cursor.getStringOrEmpty("second_batting_team_captain"),
                winningTeamCaptain = cursor.getStringOrEmpty("winning_team_captain"),
                noOfOversAside = cursor.getIntOrZero("no_of_overs_aside"),
                noOfPlayersAside = cursor.getIntOrZero("no_of_players_aside"),
                sideWallRule = cursor.getIntOrZero("side_wall_rule"),
                isStarted = cursor.getIntOrZero("is_started") == 1,
                isFinished = cursor.getIntOrZero("is_finished") == 1,
                isSynced = cursor.getIntOrZero("is_synced") == 1
            )
        }

        cursor.close()
        return match
    }


    fun getMatchStats(matchId: String): List<UploadRow> {
        val matchStats = mutableListOf<UploadRow>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $VIEW_MATCHSTATS WHERE match_id = ? ORDER BY team_id",
            arrayOf(matchId)
        )
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
        val cursor = db.rawQuery(query, arrayOf(matchId, teamId.toString()))
        while (cursor.moveToNext()) {
            val name = cursor.getStringOrEmpty("player_name")
            players.add(Player(name))
        }
        cursor.close()
        return players
    }

    fun getTeamForPlayer(matchId: String, playerName: String): Int {
        val db = readableDatabase
        val query =
            "SELECT team_id FROM $TABLE_TEAMS WHERE match_id = ? AND player_name = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId, playerName))
        val teamId = if (cursor.moveToFirst()) {
            cursor.getIntOrZero("team_id")
        } else {
            0
        }

        cursor.close()
        return teamId
    }

    fun getTeamSize(matchId: String, teamId: Int): Int {
        val db = readableDatabase
        val query = "SELECT COUNT(*) FROM $TABLE_TEAMS WHERE match_id = ? AND team_id =?"
        val cursor = db.rawQuery(query, arrayOf(matchId, teamId.toString()))

        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
    }

    fun getCaptainForTeam(matchId: String, teamId: Int): String {
        val db = readableDatabase
        val query =
            "SELECT player_name FROM $TABLE_TEAMS WHERE match_id = ? AND team_id = ? AND is_captain = 1 LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId, teamId.toString()))

        val captainName = if (cursor.moveToFirst()) {
            cursor.getStringOrEmpty("player_name")
        } else {
            ""
        }
        cursor.close()
        return captainName
    }

    fun getBatsmanByStatus(matchId: String, battingStatus: String): BatsmanStats {
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

        val query =
            "SELECT * FROM $TABLE_BATTINGSTATS WHERE match_id = ? AND batting_status = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId, battingStatus))

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

    fun getCurrentBowler(matchId: String): String {
        val db = readableDatabase
        val query =
            "SELECT player_name FROM $TABLE_BOWLINGSTATS WHERE match_id = ? AND bowling_status = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId, "bowling"))

        val playerName = if (cursor.moveToFirst()) {
            cursor.getStringOrEmpty("player_name")
        } else {
            ""
        }
        cursor.close()
        return playerName
    }

    fun getCurrentBowlerStats(matchId: String): BowlerStats {
        val db = readableDatabase
        val query =
            "SELECT * FROM $TABLE_BOWLINGSTATS WHERE match_id = ? AND bowling_status = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId, "bowling"))

        return if (cursor.moveToFirst()) {
            BowlerStats(
                name = mutableStateOf(cursor.getStringOrEmpty("player_name")),
                over = mutableDoubleStateOf(cursor.getDoubleOrZero("overvalue")),
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

    fun getConsolidatedBowlerStats(matchId: String, bowlerName: String): BowlerStats {
        val db = readableDatabase
        val query = """
            SELECT 
                player_name, 
                SUM(overvalue) AS total_overs, 
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
        val cursor = db.rawQuery(query, arrayOf(matchId, bowlerName))

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

//    fun getLastBowler(matchId: String, teamId: Int) : String {
//        val db = readableDatabase
//        val query = "SELECT * FROM $TABLE_BOWLINGSTATS WHERE match_id = ? AND team_id = ? AND bowling_status = ? ORDER BY bowling_order DESC LIMIT 1"
//        val cursor = db.rawQuery(query, arrayOf(matchId,teamId.toString(),"bowled"))
//        var lastBowler = ""
//        if (cursor.moveToFirst()) {
//            lastBowler = cursor.getStringOrEmpty("player_name")
//        }
//        cursor.close()
//        return lastBowler
//    }

    fun getLastKeeper(matchId: String, teamId: Int): String {
        val db = readableDatabase
        val query =
            "SELECT * FROM $TABLE_BOWLINGSTATS WHERE match_id = ? AND team_id = ? AND bowling_status = ? ORDER BY bowling_order DESC LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId, teamId.toString(), "bowling"))
        var lastKeeper = ""
        if (cursor.moveToFirst()) {
            lastKeeper = cursor.getStringOrEmpty("keeper_name")
        }
        cursor.close()
        return lastKeeper
    }

    fun getCurrentKeeper(matchId: String): String {
        val db = readableDatabase
        val query =
            "SELECT keeper_name FROM $TABLE_BOWLINGSTATS WHERE match_id = ? AND bowling_status = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId, "bowling"))

        val playerName = if (cursor.moveToFirst()) {
            cursor.getStringOrEmpty("keeper_name")
        } else {
            ""
        }
        cursor.close()
        return playerName
    }

    fun getBowlingStats(matchId: String, teamId: Int): List<BowlerStats> {
        val bowlers = mutableListOf<BowlerStats>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_BOWLINGSTATS where match_id = ? AND team_id = ?",
            arrayOf(matchId, teamId.toString())
        )
        cursor.use {  // Ensure the cursor is properly closed after use
            while (it.moveToNext()) {  // Iterate through all records in the cursor
                val bowlerStats = BowlerStats(
                    name = mutableStateOf(it.getStringOrEmpty("player_name")),
                    overrecord = mutableStateOf(it.getStringOrEmpty("over_record"))
                )
                bowlers.add(bowlerStats) // Add each bowler stats object to the list
            }
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
                    wicketDescription = mutableStateOf(it.getStringOrEmpty("wicket_description")),
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
                SUM(overvalue) AS total_overs, 
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
        val cursor = db.rawQuery(query, arrayOf(matchId, teamId.toString()))

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

    fun getFullyBattedAlreadyPlayers(matchId: String, teamId: Int): List<Player> {
        val players = mutableListOf<Player>()
        val db = readableDatabase
        val query =
            "SELECT player_name FROM $TABLE_BATTINGSTATS WHERE match_id = ? AND team_id = ? AND batting_turn = 2 AND batting_status = 'out'"
        val cursor = db.rawQuery(query, arrayOf(matchId, teamId.toString()))
        while (cursor.moveToNext()) {
            val name = cursor.getStringOrEmpty("player_name")
            players.add(Player(name))
        }
        cursor.close()
        return players
    }

    fun getBattingCount(matchId: String, playerName: String): Int {
        val db = readableDatabase
        val query =
            "SELECT COUNT(*) AS outCount FROM $TABLE_BATTINGSTATS WHERE match_id = ? AND player_name = ? AND batting_status = ?"
        val cursor = db.rawQuery(query, arrayOf(matchId, playerName, "out"))

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

        val isStarted = if (cursor.moveToFirst()) {
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

        val matchDate = if (cursor.moveToFirst()) {
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
        var query =
            "SELECT SUM(overvalue) AS overs FROM $TABLE_BOWLINGSTATS WHERE match_id = ? AND team_id = ? LIMIT 1"
        var cursor = db.rawQuery(query, arrayOf(matchId, otherTeamId.toString()))

        if (cursor.moveToNext()) {
            overs = cursor.getDoubleOrZero("overs")
        }
        cursor.close()

        query =
            "SELECT SUM(byes + legbyes + wides + noballs) AS extras FROM $TABLE_BOWLINGSTATS WHERE match_id = ? AND team_id = ? LIMIT 1"
        cursor = db.rawQuery(query, arrayOf(matchId, otherTeamId.toString()))

        if (cursor.moveToNext()) {
            extras = cursor.getIntOrZero("extras")
        }
        cursor.close()

        query =
            "SELECT SUM(runs) AS runs FROM $TABLE_BATTINGSTATS WHERE match_id = ? AND team_id = ? LIMIT 1"
        cursor = db.rawQuery(query, arrayOf(matchId, currentTeamId.toString()))

        if (cursor.moveToNext()) {
            runs = cursor.getIntOrZero("runs")
        }
        cursor.close()

        query =
            "SELECT COUNT(*) AS wickets FROM $TABLE_BATTINGSTATS WHERE match_id = ? AND team_id = ? AND batting_status = ? LIMIT 1"
        cursor = db.rawQuery(query, arrayOf(matchId, currentTeamId.toString(), "out"))

        if (cursor.moveToNext()) {
            wickets = cursor.getIntOrZero("wickets")
        }
        cursor.close()

        query =
            "SELECT COUNT(*) AS wickets FROM $TABLE_BATTINGSTATS WHERE match_id = ? AND team_id = ? AND batting_status = ? LIMIT 1"
        cursor = db.rawQuery(query, arrayOf(matchId, currentTeamId.toString(), "striker"))

        val active = if (cursor.moveToFirst()) {
            cursor.getInt(0) > 0  // Check if the count is greater than 0
        } else {
            false
        }
        cursor.close()

        teamStats = TeamStats(
            name = mutableStateOf(captainName),
            overs = mutableDoubleStateOf(overs),
            inningScore = mutableIntStateOf(runs + extras + (-3 * wickets)),
            inningWickets = mutableIntStateOf(wickets),
            active = mutableStateOf(active)
        )
        return teamStats

    }

    fun getTeamWickets(matchId: String, teamId: Int): Int {
        val db = readableDatabase
        val query =
            "SELECT COUNT(*) AS wickets FROM $TABLE_BATTINGSTATS WHERE match_id = ? AND team_id = ? AND batting_status = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId, teamId.toString(), "out"))

        val wickets = if (cursor.moveToFirst()) {
            cursor.getIntOrZero("wickets")
        } else {
            0
        }
        cursor.close()
        return wickets
    }

    fun getTeamBatters(matchId: String, teamId: Int): Int {
        val db = readableDatabase
        val query =
            "SELECT COUNT(*) AS batters FROM $TABLE_BATTINGSTATS WHERE match_id = ? AND team_id = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId, teamId.toString()))

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
        val query =
            "SELECT SUM(overvalue) AS overs FROM $TABLE_BOWLINGSTATS WHERE match_id = ? AND team_id = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId, teamId.toString()))

        val overs = if (cursor.moveToFirst()) {
            cursor.getDoubleOrZero("overs")
        } else {
            0.0
        }
        cursor.close()
        return overs
    }

//    fun getBowlersOversBowled(matchId: String, bowlingTeamId: Int, bowlerName: String): String {
//        val db = readableDatabase
//        val query = "SELECT SUM(overvalue) AS overs FROM $TABLE_BOWLINGSTATS WHERE match_id = ? AND team_id = ? AND player_name = ? LIMIT 1"
//        val cursor = db.rawQuery(query, arrayOf(matchId,bowlingTeamId.toString(),bowlerName))
//        var oversBowled = ""
//        if (cursor.moveToFirst()) {
//            oversBowled = cursor.getDoubleOrZero("overs").toString()
//        }
//        cursor.close()
//        return oversBowled
//    }

    fun getStriker(matchId: String): String {
        val db = readableDatabase
        val query =
            "SELECT player_name FROM $TABLE_BATTINGSTATS WHERE match_id = ? AND batting_status = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId, "striker"))

        val playerName = if (cursor.moveToFirst()) {
            cursor.getStringOrEmpty("player_name")
        } else {
            ""
        }
        cursor.close()
        return playerName
    }

    fun getNonStriker(matchId: String): String {
        val db = readableDatabase
        val query =
            "SELECT player_name FROM $TABLE_BATTINGSTATS WHERE match_id = ? AND batting_status = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId, "non-striker"))

        val playerName = if (cursor.moveToFirst()) {
            cursor.getStringOrEmpty("player_name")
        } else {
            ""
        }
        cursor.close()
        return playerName
    }

    fun getBowler(matchId: String): String {
        val db = readableDatabase
        val query =
            "SELECT player_name FROM $TABLE_BOWLINGSTATS WHERE match_id = ? AND bowling_status = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId, "bowling"))

        val playerName = if (cursor.moveToFirst()) {
            cursor.getStringOrEmpty("player_name")
        } else {
            ""
        }
        cursor.close()
        return playerName
    }

    fun getKeeper(matchId: String): String {
        val db = readableDatabase
        val query =
            "SELECT keeper_name FROM $TABLE_BOWLINGSTATS WHERE match_id = ? AND bowling_status = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId, "bowling"))

        val playerName = if (cursor.moveToFirst()) {
            cursor.getStringOrEmpty("keeper_name")
        } else {
            ""
        }
        cursor.close()
        return playerName
    }

    fun getNoOfOversAside(matchId: String): Int {
        val db = readableDatabase
        val query = "SELECT no_of_overs_aside FROM $TABLE_MATCHES WHERE match_id = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId))

        val noOfOversAside = if (cursor.moveToFirst()) {
            cursor.getIntOrZero("no_of_overs_aside")
        } else {
            0
        }
        cursor.close()
        return noOfOversAside
    }

    fun getNoOfPlayersAside(matchId: String): Int {
        val db = readableDatabase
        val query = "SELECT no_of_players_aside FROM $TABLE_MATCHES WHERE match_id = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId))

        val noOfPlayersAside = if (cursor.moveToFirst()) {
            cursor.getIntOrZero("no_of_players_aside")
        } else {
            0
        }
        cursor.close()
        return noOfPlayersAside
    }

    fun getSideWallRule(matchId: String): Int {
        val db = readableDatabase
        val query = "SELECT side_wall_rule FROM $TABLE_MATCHES WHERE match_id = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(matchId))

        val sideWallRule = if (cursor.moveToFirst()) {
            cursor.getIntOrZero("side_wall_rule")
        } else {
            0
        }
        cursor.close()
        return sideWallRule
    }

    fun getBallByBallHistory(matchId: String, teamId: Int): List<BallEvent> {
        val ballEvents = mutableListOf<BallEvent>()
        val db = this.readableDatabase

        val query = """
            SELECT player_name, overvalue, over_record, 
                   (COALESCE(noballs, 0) + COALESCE(wides, 0) + COALESCE(byes, 0) + COALESCE(legbyes, 0)) AS extras
            FROM $TABLE_BOWLINGSTATS 
            WHERE match_id = ? AND team_id = ? AND over_record IS NOT NULL AND over_record != ''
            ORDER BY bowling_order
            """

        val cursor = db.rawQuery(query, arrayOf(matchId,teamId.toString()))

        cursor.use {
            var cumulativeOvers = 0.0
            var totalRuns = 0
            var totalWickets = 0

            while (it.moveToNext()) {
                val bowlerName = it.getStringOrEmpty("player_name")
                val overValue = it.getDoubleOrZero("overvalue")
                val overRecord = it.getStringOrEmpty("over_record")
                val extras = it.getIntOrZero("extras")

                val baseOverNumber = kotlin.math.ceil(cumulativeOvers).toInt()

                if (overRecord.isNotEmpty()) {
                    val balls = overRecord.split("|")
                    var actualBallNumber = 1
                    var overRuns = 0

                    balls.forEachIndexed { index, ballString ->
                        val parts = ballString.split(",")
                        if (parts.size >= 2) {
                            val action = parts.getOrElse(0) { "" }
                            val batsmanName = parts.getOrElse(1) { "" }
                            val newBatsmanName = parts.getOrElse(2) { "" }
                            val fielderName = parts.getOrElse(3) { "unknown" }
                            val nonStrikerName = parts.getOrElse(4) { "unknown" }

                            val excludedValuesFromBallsBalled = setOf("W","W+1","W+2","NB","NB+1","NB+2","NB+3","NB+4","NB+6","NBL1","NBL2","NBL3","NBB1","NBB2","NBB3","WKRONB","WKROW","WKSTW")
                            val isInvalidBall = action in excludedValuesFromBallsBalled

                            // Calculate runs from this ball
                            val runsFromBall = when {
                                action.startsWith("WK") -> {
                                    totalWickets++
                                    -3
                                }

                                action.startsWith("W+") -> {
                                    val runs = action.removePrefix("W+").toIntOrNull() ?: 0
                                    1 + runs // Wide + additional runs
                                }

                                action.startsWith("W") -> 1 // Just a wide
                                action.startsWith("NB+") -> {
                                    val runs = action.removePrefix("NB+").toIntOrNull() ?: 0
                                    1 + runs // No ball + additional runs
                                }

                                action.startsWith("NB") -> 1 // Just a no ball
                                action.startsWith("LBW") -> -2 //-2 for an LBW warning
                                action.startsWith("LB") -> action.removePrefix("LB").toIntOrNull()
                                    ?: 0

                                action.startsWith("B") -> action.removePrefix("B").toIntOrNull()
                                    ?: 0

                                else -> action.toIntOrNull() ?: 0
                            }

                            overRuns += runsFromBall
                            totalRuns += runsFromBall

                            val overDisplay = String.format(
                                Locale.UK,
                                "%d.%d",
                                baseOverNumber,
                                actualBallNumber
                            )

                            val displayResult = when {
                                action == "0" -> "•"
                                action.startsWith("WK") -> getWicketDescription(
                                    action,
                                    bowlerName,
                                    fielderName
                                )

                                action == "W" -> "Wide"
                                action.startsWith("W+") -> action.replace("W+", "Wide + ")
                                action == "NB" -> "No-ball"
                                action.startsWith("NB+") -> action.replace("NB+", "No-ball + ")
                                action == "LBW" -> "LBW (-2 Runs)"
                                action == "LB" -> "Leg-bye"
                                action.startsWith("LB") -> action.replace("LB", "Leg-byes = ")
                                action == "B" -> "Bye"
                                action.startsWith("B") -> action.replace("B", "Byes =  ")
                                else -> action
                            }

                            ballEvents.add(
                                BallEvent(
                                    over = overDisplay,
                                    bowler = bowlerName,
                                    batsman = batsmanName,
                                    nonStriker = nonStrikerName,
                                    result = action,
                                    resultText = displayResult
                                )
                            )

                            if (!isInvalidBall) {
                                actualBallNumber++
                            }
                        }
                    }

                    // Add over summary if it's a complete over
                    if (overValue >= 1.0) {
                        ballEvents.add(
                            BallEvent(
                                over = (baseOverNumber + 1).toString(),
                                bowler = "",
                                batsman = "",
                                nonStriker = "",
                                result = "",
                                resultText = "",
                                isOverSummary = true,
                                overRuns = overRuns,
                                overExtras = extras,
                                totalScore = "$totalRuns/$totalWickets"
                            )
                        )
                    }
                }

                cumulativeOvers += overValue
            }
        }

        return ballEvents
    }

    fun getDBVersion(): Int {
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
        db.delete(TABLE_MATCHSNAPSHOT, "match_id = ?", arrayOf(matchId))
        db.delete(TABLE_PARTNERSHIPS, "match_id = ?", arrayOf(matchId))
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
            return dbWrite.delete(
                TABLE_BATTINGSTATS,
                "match_id = ? AND player_name = ? AND batting_order = ?",
                arrayOf(matchId, playerName, battingOrder.toString())
            )
        }

        return 0
    }

    fun deleteCurrentBowler(matchId: String): Int {
        val currentBowler = getCurrentBowler(matchId)
        val db = writableDatabase
        return db.delete(
            TABLE_BOWLINGSTATS,
            "player_name = ? AND bowling_status = ?",
            arrayOf(currentBowler, "bowling")
        )
    }

    fun deleteTeamPlayer(matchId: String, teamId: Int, playerName: String): Int {
        val db = writableDatabase
        return db.delete(
            TABLE_TEAMS,
            "match_id = ? AND team_id = ? AND player_name = ?",
            arrayOf(matchId, teamId.toString(), playerName)
        )
    }

    //ADD FUNCTIONS ********************************************************************************

    private fun addMatch(matchId: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd") // Define the format
        val formattedDate = currentDate.format(formatter)
        values.put("match_id", matchId)
        values.put("match_date", formattedDate)
        values.put("first_batting_team_captain", "")
        values.put("second_batting_team_captain", "")
        values.put("winning_team_captain", "")
        values.put("no_of_overs_aside", 12)
        values.put("no_of_players_aside", 6)
        values.put("side_wall_rule", 1)
        values.put("is_started", 0)
        values.put("is_finished", 0)
        values.put("is_synced", 0)
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

    fun addTeamPlayer(
        matchId: String,
        teamId: Int,
        playerName: String,
        isCaptain: Int,
        isMidBowler: Int
    ) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("match_id", matchId)
        values.put("team_id", teamId)
        values.put("player_name", playerName)
        values.put("is_captain", isCaptain)
        values.put("is_midbowler", isMidBowler)
        db.insert(TABLE_TEAMS, null, values)
        db.close()
    }

    fun addBattingStats(matchId: String, teamId: Int, playerName: String, battingStatus: String) {
        val db = writableDatabase
        val values = ContentValues()
        values.put("match_id", matchId)
        values.put("team_id", teamId)
        values.put("batting_order", getNextBattingOrderNo(matchId))
        values.put("player_name", playerName)
        values.put("batting_turn", getNextBattingTurnNo(matchId, playerName))
        values.put("batting_status", battingStatus)
        db.insert(TABLE_BATTINGSTATS, null, values)
        db.close()
    }

    fun addBowlingStats(
        matchId: String,
        teamId: Int,
        bowlerName: String,
        keeperName: String,
        bowlingStatus: String
    ) {
        val db = writableDatabase
        val values = ContentValues()
        values.put("match_id", matchId)
        values.put("team_id", teamId)
        values.put("bowling_order", getNextBowlingOrderNo(matchId))
        values.put("player_name", bowlerName)
        values.put("bowling_turn", getNextBowlingTurnNo(matchId, bowlerName))
        values.put("bowling_status", bowlingStatus)
        values.put("keeper_name", keeperName)
        db.insert(TABLE_BOWLINGSTATS, null, values)
        db.close()
    }

    //UPDATE FUNCTIONS *****************************************************************************
    fun updateOversAside(matchId: String, oversAside: Int): Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("no_of_overs_aside", oversAside)
        }
        val whereClause = "match_id = ?"
        val whereArgs = arrayOf(matchId)

        return db.update(TABLE_MATCHES, contentValues, whereClause, whereArgs)
    }

    fun updatePlayersAside(matchId: String, playersAside: Int): Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("no_of_players_aside", playersAside)
        }
        val whereClause = "match_id = ?"
        val whereArgs = arrayOf(matchId)

        return db.update(TABLE_MATCHES, contentValues, whereClause, whereArgs)
    }

    fun updateSideWallRule(matchId: String, sideWallRule: Int): Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("side_wall_rule", sideWallRule)
        }
        val whereClause = "match_id = ?"
        val whereArgs = arrayOf(matchId)

        return db.update(TABLE_MATCHES, contentValues, whereClause, whereArgs)
    }

    fun updateTeamID(matchId: String, fromTeamId: Int, toTeamId: Int): Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("team_id", toTeamId)
        }
        val whereClause = "match_id = ? AND team_id = ?"
        val whereArgs = arrayOf(matchId,fromTeamId.toString())

        return db.update(TABLE_TEAMS, contentValues, whereClause, whereArgs)
    }

    fun updateMatchCaptain(matchId: String, whichTeam: Int, captain: String): Int {
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

    fun updateMatchIsStarted(matchId: String): Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("is_started", 1)
        }
        val whereClause = "match_id = ?"
        val whereArgs = arrayOf(matchId)

        return db.update(TABLE_MATCHES, contentValues, whereClause, whereArgs)
    }

    fun updateMatchIsFinished(matchId: String, winningTeamCaptain: String): Int {
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

    fun updateMatchOpeningStriker(matchId: String, player: String): Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("first_batting_team_striker", player)
        }
        val whereClause = "match_id = ?"
        val whereArgs = arrayOf(matchId)

        return db.update(TABLE_MATCHES, contentValues, whereClause, whereArgs)
    }

    fun updateMatchOpeningNonStriker(matchId: String, player: String): Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("first_batting_team_nonstriker", player)
        }
        val whereClause = "match_id = ?"
        val whereArgs = arrayOf(matchId)

        return db.update(TABLE_MATCHES, contentValues, whereClause, whereArgs)
    }

    fun updateMatchOpeningBowler(matchId: String, player: String): Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("second_batting_team_bowler", player)
        }
        val whereClause = "match_id = ?"
        val whereArgs = arrayOf(matchId)

        return db.update(TABLE_MATCHES, contentValues, whereClause, whereArgs)
    }

    fun updateMatchOpeningKeeper(matchId: String, player: String): Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("second_batting_team_keeper", player)
        }
        val whereClause = "match_id = ?"
        val whereArgs = arrayOf(matchId)

        return db.update(TABLE_MATCHES, contentValues, whereClause, whereArgs)
    }

//    fun updateMatchWinner(matchId: String, winningTeamCaptain: String) : Int {
//        val db = writableDatabase
//        val contentValues = ContentValues().apply {
//            put("winning_team_captain", winningTeamCaptain)
//        }
//        val whereClause = "match_id = ?"
//        val whereArgs = arrayOf(matchId)
//
//        return db.update(TABLE_MATCHES, contentValues, whereClause, whereArgs)
//    }
//
//    fun updateMatch(matchId: String, isSynced: Int) : Int {
//        val db = writableDatabase
//        val contentValues = ContentValues().apply {
//            put("is_synced", isSynced)
//        }
//        return db.update(TABLE_MATCHES, contentValues, "match_id = ?", arrayOf(matchId))
//    }

    fun updateMatchDate(matchId: String, matchDate: String): Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("match_date", matchDate)
        }
        val whereClause = "match_id = ?"
        val whereArgs = arrayOf(matchId)

        return db.update(TABLE_MATCHES, contentValues, whereClause, whereArgs)
    }

    fun updateBattingStats(
        matchId: String,
        playerName: String,
        existingBattingStatus: String,
        newBattingStatus: String
    ): Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("batting_status", newBattingStatus)
        }
        val whereClause = "match_id = ? AND player_name = ? AND batting_status = ?"
        val whereArgs = arrayOf(matchId, playerName, existingBattingStatus)

        return db.update(TABLE_BATTINGSTATS, contentValues, whereClause, whereArgs)
    }

    fun updateBattingStats(
        matchId: String,
        newBattingStatus: String,
        batsmanStats: BatsmanStats,
        wicketDescription: String,
        wicketType: String,
        wicketBowler: String,
        wicketFielder: String
    ): Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("batting_status", newBattingStatus)
            put("runs", batsmanStats.runs.value)
            put("balls", batsmanStats.balls.value)
            put("fours", batsmanStats.fours.value)
            put("sixes", batsmanStats.sixes.value)
            put("dotballs", batsmanStats.dotballs.value)
            put("wicket_description", wicketDescription)
            put("wicket_type", wicketType)
            put("wicket_bowler", wicketBowler)
            put("wicket_fielder", wicketFielder)
        }
        val whereClause = "match_id = ? AND batting_status = ?"
        val whereArgs = arrayOf(matchId, "striker")

        return db.update(TABLE_BATTINGSTATS, contentValues, whereClause, whereArgs)
    }

    fun updateBowlingStats(matchId: String, bowlingStatus: String): Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("bowling_status", bowlingStatus)
        }
        val whereClause = "match_id = ? AND bowling_status = ?"
        val whereArgs = arrayOf(matchId, "bowling")

        return db.update(TABLE_BOWLINGSTATS, contentValues, whereClause, whereArgs)
    }

    fun updateBowlingStatsKeeper(matchId: String, teamId: Int, keeperName: String): Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("keeper_name", keeperName)
        }
        val whereClause = "match_id = ? AND team_id = ? AND bowling_status = ?"
        val whereArgs = arrayOf(matchId, teamId.toString(), "bowling")

        return db.update(TABLE_BOWLINGSTATS, contentValues, whereClause, whereArgs)
    }

    fun updateBowlingStats(matchId: String, bowlerStats: BowlerStats): Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("overvalue", bowlerStats.over.value)
            put("maiden", bowlerStats.maiden.value)
            put("runs", bowlerStats.runs.value)
            put("wickets", bowlerStats.wickets.value)
            put("noballs", bowlerStats.noballs.value)
            put("wides", bowlerStats.wides.value)
            put("byes", bowlerStats.byes.value)
            put("legbyes", bowlerStats.legbyes.value)
            put("fours", bowlerStats.fours.value)
            put("sixes", bowlerStats.sixes.value)
            put("dotballs", bowlerStats.dotballs.value)
            put("over_record", bowlerStats.overrecord.value)
        }
        val whereClause = "match_id = ? AND bowling_status = ?"
        val whereArgs = arrayOf(matchId, "bowling")

        return db.update(TABLE_BOWLINGSTATS, contentValues, whereClause, whereArgs)
    }

    fun updateStriker(matchId: String, playerName: String): Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("player_name", playerName)
        }
        val whereClause = "match_id = ? AND batting_status = ?"
        val whereArgs = arrayOf(matchId, "striker")

        return db.update(TABLE_BATTINGSTATS, contentValues, whereClause, whereArgs)
    }

    fun updateNonStriker(matchId: String, playerName: String): Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("player_name", playerName)
        }
        val whereClause = "match_id = ? AND batting_status = ?"
        val whereArgs = arrayOf(matchId, "non-striker")

        return db.update(TABLE_BATTINGSTATS, contentValues, whereClause, whereArgs)
    }

    fun updateBowler(matchId: String, playerName: String): Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("player_name", playerName)
        }
        val whereClause = "match_id = ? AND bowling_status = ?"
        val whereArgs = arrayOf(matchId, "bowling")

        return db.update(TABLE_BOWLINGSTATS, contentValues, whereClause, whereArgs)
    }

    fun updateKeeper(matchId: String, playerName: String): Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("keeper_name", playerName)
        }
        val whereClause = "match_id = ? AND bowling_status = ?"
        val whereArgs = arrayOf(matchId, "bowling")

        return db.update(TABLE_BOWLINGSTATS, contentValues, whereClause, whereArgs)
    }

    fun saveSnapshot(matchId: String) {
        val db = writableDatabase

        // Read current batting stats for this match
        val battingStats = mutableListOf<Map<String, Any?>>()
        db.rawQuery("SELECT * FROM $TABLE_BATTINGSTATS WHERE match_id = ?", arrayOf(matchId)).use { cursor ->
            while (cursor.moveToNext()) {
                val row = mutableMapOf<String, Any?>()
                cursor.columnNames.forEach { col ->
                    row[col] = when (cursor.getType(cursor.getColumnIndexOrThrow(col))) {
                        Cursor.FIELD_TYPE_INTEGER -> cursor.getLong(cursor.getColumnIndexOrThrow(col))
                        Cursor.FIELD_TYPE_FLOAT   -> cursor.getDouble(cursor.getColumnIndexOrThrow(col))
                        Cursor.FIELD_TYPE_STRING  -> cursor.getString(cursor.getColumnIndexOrThrow(col))
                        else                      -> null
                    }
                }
                battingStats.add(row)
            }
        }

        // Read current bowling stats for this match
        val bowlingStats = mutableListOf<Map<String, Any?>>()
        db.rawQuery("SELECT * FROM $TABLE_BOWLINGSTATS WHERE match_id = ?", arrayOf(matchId)).use { cursor ->
            while (cursor.moveToNext()) {
                val row = mutableMapOf<String, Any?>()
                cursor.columnNames.forEach { col ->
                    row[col] = when (cursor.getType(cursor.getColumnIndexOrThrow(col))) {
                        Cursor.FIELD_TYPE_INTEGER -> cursor.getLong(cursor.getColumnIndexOrThrow(col))
                        Cursor.FIELD_TYPE_FLOAT   -> cursor.getDouble(cursor.getColumnIndexOrThrow(col))
                        Cursor.FIELD_TYPE_STRING  -> cursor.getString(cursor.getColumnIndexOrThrow(col))
                        else                      -> null
                    }
                }
                bowlingStats.add(row)
            }
        }

        //Read the current partnership stats for this match
        val partnerships = mutableListOf<Map<String, Any?>>()
        db.rawQuery("SELECT * FROM $TABLE_PARTNERSHIPS WHERE match_id = ?", arrayOf(matchId)).use { cursor ->
            while (cursor.moveToNext()) {
                val row = mutableMapOf<String, Any?>()
                cursor.columnNames.forEach { col ->
                    row[col] = when (cursor.getType(cursor.getColumnIndexOrThrow(col))) {
                        Cursor.FIELD_TYPE_INTEGER -> cursor.getLong(cursor.getColumnIndexOrThrow(col))
                        Cursor.FIELD_TYPE_FLOAT -> cursor.getDouble(cursor.getColumnIndexOrThrow(col))
                        Cursor.FIELD_TYPE_STRING -> cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                col
                            )
                        )

                        else -> null
                    }
                }
                partnerships.add(row)
            }
        }


        // Serialise to JSON
        val state = MatchState(battingStats, bowlingStats, partnerships)
        val json = Gson().toJson(state)

        // Get next entry_no for this match
        val nextEntryNo = db.rawQuery(
            "SELECT COALESCE(MAX(entry_no), 0) + 1 FROM $TABLE_MATCHSNAPSHOT WHERE match_id = ?",
            arrayOf(matchId)
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.getInt(0) else 1
        }

        // Save snapshot
        val contentValues = ContentValues().apply {
            put("match_id", matchId)
            put("entry_no", nextEntryNo)
            put("state", json)
        }
        db.insert(TABLE_MATCHSNAPSHOT, null, contentValues)
    }

    fun undoLastBall(matchId: String): Boolean {
        val db = writableDatabase

        // Get the last entry_no for this match
        val lastEntryNo = db.rawQuery(
            "SELECT MAX(entry_no) FROM $TABLE_MATCHSNAPSHOT WHERE match_id = ?",
            arrayOf(matchId)
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.getInt(0) else return false
        }

        // Delete the most recent snapshot (current state)
        db.delete(TABLE_MATCHSNAPSHOT, "match_id = ? AND entry_no = ?", arrayOf(matchId, lastEntryNo.toString()))

        // Fetch what is now the last snapshot (previous state)
        val stateJson = db.rawQuery(
            "SELECT state FROM $TABLE_MATCHSNAPSHOT WHERE match_id = ? ORDER BY entry_no DESC LIMIT 1",
            arrayOf(matchId)
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else return false
        }

        // Deserialise the state
        val state = Gson().fromJson(stateJson, MatchState::class.java)

        db.beginTransaction()
        try {
            // Restore batting stats
            db.delete(TABLE_BATTINGSTATS, "match_id = ?", arrayOf(matchId))
            state.battingStats.forEach { row ->
                val cv = ContentValues()
                row.forEach { (col, value) ->
                    when (value) {
                        is Long   -> cv.put(col, value)
                        is Double -> cv.put(col, value)
                        is String -> cv.put(col, value)
                        null      -> cv.putNull(col)
                    }
                }
                db.insert(TABLE_BATTINGSTATS, null, cv)
            }

            // Restore bowling stats
            db.delete(TABLE_BOWLINGSTATS, "match_id = ?", arrayOf(matchId))
            state.bowlingStats.forEach { row ->
                val cv = ContentValues()
                row.forEach { (col, value) ->
                    when (value) {
                        is Long   -> cv.put(col, value)
                        is Double -> cv.put(col, value)
                        is String -> cv.put(col, value)
                        null      -> cv.putNull(col)
                    }
                }
                db.insert(TABLE_BOWLINGSTATS, null, cv)
            }

            // Restore the partnership stats
            db.delete(TABLE_PARTNERSHIPS, "match_id = ?", arrayOf(matchId))
            state.partnerships.forEach { row ->
                val cv = ContentValues()
                row.forEach { (col, value) ->
                    when (value) {
                        is Long   -> cv.put(col, value)
                        is Double -> cv.put(col, value)
                        is String -> cv.put(col, value)
                        null      -> cv.putNull(col)
                    }
                }
                db.insert(TABLE_PARTNERSHIPS, null, cv)
            }


            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }

        return true
    }

    //IS FUNCTIONS *********************************************************************************

    fun isPlayerAlreadyExists(playerName: String): Boolean {
        val db = readableDatabase
        val query = "SELECT 1 FROM $TABLE_PLAYERS WHERE name = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(playerName))

        val exists =
            cursor.moveToFirst() // returns true if the query returned a row, false otherwise
        cursor.close()
        return exists
    }

//    fun isTeamPlayer(matchId: String, teamId: Int, playerName: String) : Boolean {
//        val db = readableDatabase
//        val query = "SELECT 1 FROM $TABLE_TEAMS WHERE match_id = ? AND team_id =? AND player_name = ? LIMIT 1"
//        val cursor = db.rawQuery(query, arrayOf(matchId,teamId.toString(),playerName))
//
//        val exists = cursor.moveToFirst() // returns true if the query returned a row, false otherwise
//        cursor.close()
//        return exists
//    }

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

    fun importCSVToDatabase(uri: Uri, context: Context, importType: String) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)

            if (inputStream == null) {
                Toast.makeText(context, "Cannot open file", Toast.LENGTH_SHORT).show()
                return
            }

            val reader = BufferedReader(InputStreamReader(inputStream))

            val db = this.writableDatabase

            db.beginTransaction() // For better performance
            try {
                // Read and skip header line
                //reader.readLine()

                // Process each line
                reader.forEachLine { line ->
                    if (line.isNotBlank()) {
                        val columns = parseCSVLine(line)

                        when (importType) {
                            "players" -> importPlayerData(columns,db, TABLE_PLAYERS)
                            "matches" -> importMatchData(columns, db,TABLE_MATCHES)
                            "teams" -> importTeamData(columns, db,TABLE_TEAMS)
                            "bowling" -> importBowlingData(columns, db,TABLE_BOWLINGSTATS)
                            "batting" -> importBattingData(columns, db,TABLE_BATTINGSTATS)
                            else -> {
                                Toast.makeText(context, "Unknown import type", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                }

                db.setTransactionSuccessful()
                Toast.makeText(context, "CSV imported successfully!", Toast.LENGTH_SHORT).show()

            } finally {
                db.endTransaction()
            }

            reader.close()
            inputStream.close()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun insertPartnership(matchId: String, teamId: Int, batsman1: String, batsman2: String) {
        val db = writableDatabase

        // Derive next wicket number automatically
        val nextWicketNumber = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_PARTNERSHIPS WHERE match_id = ? AND team_id = ?",
            arrayOf(matchId, teamId.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.getInt(0) + 1 else 1
        }

        val cv = ContentValues().apply {
            put("match_id", matchId)
            put("team_id", teamId)
            put("wicket_number", nextWicketNumber)
            put("batsman1_name", batsman1)
            put("batsman2_name", batsman2)
            put("runs", 0)
            put("balls", 0)
        }
        db.insert(TABLE_PARTNERSHIPS, null, cv)
    }

    fun updatePartnership(matchId: String, teamId: Int, runsToAdd: Int, ballsToAdd: Int) {
        val db = writableDatabase
        db.execSQL("""
        UPDATE $TABLE_PARTNERSHIPS 
        SET runs = runs + $runsToAdd, balls = balls + $ballsToAdd
        WHERE match_id = ? AND team_id = ? 
        AND wicket_number = (SELECT MAX(wicket_number) FROM $TABLE_PARTNERSHIPS WHERE match_id = ? AND team_id = ?)
    """, arrayOf(matchId, teamId.toString(), matchId, teamId.toString()))
    }

    fun getPartnerships(matchId: String, teamId: Int): List<Partnership> {
        val partnerships = mutableListOf<Partnership>()
        val db = readableDatabase
        db.rawQuery("""
        SELECT wicket_number, batsman1_name, batsman2_name, runs, balls 
        FROM $TABLE_PARTNERSHIPS 
        WHERE match_id = ? AND team_id = ?
        ORDER BY wicket_number ASC
    """, arrayOf(matchId, teamId.toString())).use { cursor ->
            while (cursor.moveToNext()) {
                partnerships.add(
                    Partnership(
                        wicketNumber = cursor.getInt(0),
                        batsman1Name = cursor.getString(1),
                        batsman2Name = cursor.getString(2),
                        runs = cursor.getInt(3),
                        balls = cursor.getInt(4)
                    )
                )
            }
        }
        return partnerships
    }
}

// Helper functions for different import types
private fun importPlayerData(columns: List<String>, db: SQLiteDatabase, tableName: String) {
    val values = ContentValues().apply {
        put("name", columns.getOrNull(0)?.trim() ?: "")
    }
    db.insert(tableName, null, values)
}
private fun importTeamData(columns: List<String>, db: SQLiteDatabase, tableName: String) {
    val values = ContentValues().apply {
        put("match_id", columns.getOrNull(0)?.trim() ?: "")
        put("team_id", columns.getOrNull(1)?.trim() ?: "")
        put("player_name", columns.getOrNull(2)?.trim() ?: "")
        put("is_captain", columns.getOrNull(3)?.trim()?.toIntOrNull() ?: 0)  // Changed to 0
        put("is_midbowler", columns.getOrNull(4)?.trim()?.toIntOrNull() ?: 0)  // Changed to 0
    }
    db.insert(tableName, null, values)
}

private fun importMatchData(columns: List<String>, db: SQLiteDatabase, tableName: String) {
    val values = ContentValues().apply {
        put("match_id", columns.getOrNull(0)?.trim() ?: "")
        put("match_date", columns.getOrNull(1)?.trim() ?: "")
        put("first_batting_team_captain", columns.getOrNull(2)?.trim() ?: "")
        put("first_batting_team_striker", columns.getOrNull(3)?.trim() ?: "")
        put("first_batting_team_nonstriker", columns.getOrNull(4)?.trim() ?: "")
        put("second_batting_team_captain", columns.getOrNull(5)?.trim() ?: "")
        put("second_batting_team_bowler", columns.getOrNull(6)?.trim() ?: "")
        put("second_batting_team_keeper", columns.getOrNull(7)?.trim() ?: "")
        put("winning_team_captain", columns.getOrNull(8)?.trim() ?: "")
        put("no_of_overs_aside", columns.getOrNull(9)?.trim()?.toIntOrNull() ?: 0)  // Changed to 0
        put("no_of_players_aside", columns.getOrNull(10)?.trim()?.toIntOrNull() ?: 0)  // Changed to 0
        put("side_wall_rule", columns.getOrNull(11)?.trim()?.toIntOrNull() ?: 0)  // Changed to 0
        put("is_started", columns.getOrNull(12)?.trim()?.toIntOrNull() ?: 0)  // Changed to 0
        put("is_finished", columns.getOrNull(13)?.trim()?.toIntOrNull() ?: 0)  // Changed to 0
        put("is_synced", columns.getOrNull(14)?.trim()?.toIntOrNull() ?: 0)  // Changed to 0
    }
    db.insert(tableName, null, values)
}

private fun importBowlingData(columns: List<String>, db: SQLiteDatabase, tableName: String) {
    val values = ContentValues().apply {
        put("match_id", columns.getOrNull(0)?.trim() ?: "")
        put("team_id", columns.getOrNull(1)?.trim() ?: "")
        put("bowling_order", columns.getOrNull(2)?.trim()?.toIntOrNull() ?: 0)
        put("player_name", columns.getOrNull(3)?.trim() ?: "")
        put("bowling_turn", columns.getOrNull(4)?.trim()?.toIntOrNull() ?: 0)
        put("bowling_status", columns.getOrNull(5)?.trim() ?: "")
        put("keeper_name", columns.getOrNull(6)?.trim() ?: "")
        put("overvalue", columns.getOrNull(7)?.trim()?.toDoubleOrNull() ?: 0.0)
        put("maiden", columns.getOrNull(8)?.trim()?.toIntOrNull() ?: 0)
        put("runs", columns.getOrNull(9)?.trim()?.toIntOrNull() ?: 0)
        put("wickets", columns.getOrNull(10)?.trim()?.toIntOrNull() ?: 0)
        put("noballs", columns.getOrNull(11)?.trim()?.toIntOrNull() ?: 0)
        put("wides", columns.getOrNull(12)?.trim()?.toIntOrNull() ?: 0)
        put("byes", columns.getOrNull(13)?.trim()?.toIntOrNull() ?: 0)
        put("legbyes", columns.getOrNull(14)?.trim()?.toIntOrNull() ?: 0)
        put("fours", columns.getOrNull(15)?.trim()?.toIntOrNull() ?: 0)
        put("sixes", columns.getOrNull(16)?.trim()?.toIntOrNull() ?: 0)
        put("dotballs", columns.getOrNull(17)?.trim()?.toIntOrNull() ?: 0)
        put("over_record", columns.getOrNull(18)?.trim() ?: "")
    }
    db.insert(tableName, null, values)
}

private fun importBattingData(columns: List<String>, db: SQLiteDatabase, tableName: String) {
    val values = ContentValues().apply {
        put("match_id", columns.getOrNull(0)?.trim() ?: "")
        put("team_id", columns.getOrNull(1)?.trim() ?: "")
        put("batting_order", columns.getOrNull(2)?.trim()?.toIntOrNull() ?: 0)
        put("player_name", columns.getOrNull(3)?.trim() ?: "")
        put("batting_turn", columns.getOrNull(4)?.trim()?.toIntOrNull() ?: 0)
        put("batting_status", columns.getOrNull(5)?.trim() ?: "")
        put("runs", columns.getOrNull(6)?.trim()?.toIntOrNull() ?: 0)
        put("balls", columns.getOrNull(7)?.trim()?.toIntOrNull() ?: 0)
        put("fours", columns.getOrNull(8)?.trim()?.toIntOrNull() ?: 0)
        put("sixes", columns.getOrNull(9)?.trim()?.toIntOrNull() ?: 0)
        put("dotballs", columns.getOrNull(10)?.trim()?.toIntOrNull() ?: 0)
        put("wicket_description", columns.getOrNull(11)?.trim() ?: "")
        put("wicket_type", columns.getOrNull(12)?.trim() ?: "")
        put("wicket_bowler", columns.getOrNull(13)?.trim() ?: "")
        put("wicket_fielder", columns.getOrNull(14)?.trim() ?: "")
    }
    db.insert(tableName, null, values)
}

private fun parseCSVLine(line: String): List<String> {
    val result = mutableListOf<String>()
    var current = StringBuilder()
    var inQuotes = false

    for (i in line.indices) {
        val char = line[i]
        when {
            char == '"' -> {
                // Toggle quotes state
                inQuotes = !inQuotes
            }
            char == ',' && !inQuotes -> {
                // Only split on commas outside quotes
                result.add(current.toString())
                current = StringBuilder()
            }
            else -> {
                current.append(char)
            }
        }
    }
    // Add the last field
    result.add(current.toString())

    return result
}

package com.main;

import java.util.ArrayList;
import java.util.Scanner;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import com.backend.models.Competition;
import com.backend.models.Game;
import com.backend.models.School;
import com.backend.models.SchoolDuration;
import com.backend.models.SchoolInteger;
import com.backend.models.SkillsCompetition;
import com.backend.models.Tournament;
import com.backend.models.GameEvent.GameEvent;
import com.backend.models.enums.GameTypeEnum;
import com.backend.models.optaplanner.GameProcess;
import com.backend.models.optaplanner.TournamentScoreCalculator;
import com.backend.models.optaplanner.TournamentSolution;
import com.backend.models.optaplanner.TournamentSolver;
import com.framework.helpers.Database;
import com.framework.models.Essentials;

public class TournamentSetup 
{
	public static void main(String[] args) 
	{
		Scanner keyboard = new Scanner(System.in);
		setupCompetitions(keyboard);
		setupSchedule(keyboard);
		keyboard.close();
	}
	
	public static void setupCompetitions(Scanner keyboard)
	{
		System.out.println("Do you want to setup competitions and skill competitions?");
    	
    	String valueEntered = "";
    	while(!(valueEntered.toLowerCase().equals("y") || valueEntered.toLowerCase().equals("n")))
    	{
    		valueEntered = keyboard.next();
    	}
    	
    	if(valueEntered.toLowerCase().equals("y"))
    	{
    		try(Essentials essentials = new Essentials(new Database(Database.DatabaseType.PRODUCTION), null, null, null, null))
    		{
    			ArrayList<School> schools = School.getSchools(essentials);

    			Competition competition = new Competition(
    					null,
    					new ArrayList<School>(),
    					new ArrayList<School>(),
    					new ArrayList<School>(),
    					new ArrayList<School>(),
    					new ArrayList<School>(),
    					new ArrayList<School>(),
    					new ArrayList<School>(),
    					new ArrayList<School>());
    			
    			ArrayList<SchoolInteger> pickBalls = new ArrayList<SchoolInteger>();
    			ArrayList<SchoolDuration> twoActuatorChanged = new ArrayList<SchoolDuration>();
    			ArrayList<SchoolDuration> twoTargetHits = new ArrayList<SchoolDuration>();
    			
    			for(School school : schools)
    			{
    				pickBalls.add(new SchoolInteger(school, 0));
    				// Initialize to 99 minutes.
    				twoActuatorChanged.add(new SchoolDuration(school, new Duration(99 * 60 * 1000)));
    				twoTargetHits.add(new SchoolDuration(school, new Duration(99 * 60 * 1000)));

    				competition.kiosk.add(school);
    				competition.programming.add(school);
    				competition.robotConstruction.add(school);
    				competition.robotDesign.add(school);
    				competition.sportsmanship.add(school);
    				competition.video.add(school);
    				competition.websiteDesign.add(school);
    				competition.websiteJournalism.add(school);
    			}
    			essentials.database.dropCollection(SkillsCompetition.class);
    			essentials.database.dropCollection(Competition.class);
    			
    			SkillsCompetition skillsCompetition = new SkillsCompetition(
    					null,
						pickBalls, 
						twoTargetHits,
						twoActuatorChanged);
    			essentials.database.save(skillsCompetition);
    			essentials.database.save(competition);
    		}
    	}
	}
	
	public static void setupSchedule(Scanner keyboard)
	{
		try(Essentials essentials = new Essentials(new Database(Database.DatabaseType.PRODUCTION), null, null, null, null))
		{
			ArrayList<School> schools = School.getSchools(essentials);
			if((schools.size() * Tournament.GAME_PER_SCHOOL) % (Tournament.SCHOOLS_PER_TEAM * 2) != 0 )
			{
	        	System.out.println("There's no possible solution. Please change Tournament.GAME_PER_SCHOOL value.");
	        	return;
			}
			
			Duration TIME_BETWEEN_GAMES = new Duration(5 * 60 * 1000);
			DateTime[] RoundStartHour = new DateTime[Tournament.BLOCK_NUMBERS];
			RoundStartHour[0] = new DateTime(2015, 2, 12, 18, 30);
			RoundStartHour[1] = new DateTime(2015, 2, 13, 9, 0);
			RoundStartHour[2] = new DateTime(2015, 2, 13, 13, 30);
			RoundStartHour[3] = new DateTime(2015, 2, 13, 18, 30);
			
			TournamentSolution solvedTournament = TournamentSolver.solve(essentials, Tournament.GAME_PER_SCHOOL / (Tournament.SCHOOLS_PER_TEAM * 2), Tournament.SCHOOLS_PER_TEAM * 2, "com/backend/models/optaplanner/TournamentSolverConfig.xml");
			if(solvedTournament != null)
			{
				solvedTournament.outputTournamentSolution();
				
				if(solvedTournament != null)
		        {
		        	System.out.println("Do you want to save this schedule? (y/n)\nWARNING! This will erase all previously saved games.");
		        	
		        	String valueEntered = "";
		        	while(!(valueEntered.toLowerCase().equals("y") || valueEntered.toLowerCase().equals("n")))
		        	{
		        		valueEntered = keyboard.next();
		        	}
		        	
		        	if(valueEntered.toLowerCase().equals("y"))
		        	{
		        		essentials.database.dropCollection(Game.class);
		        		ArrayList<GameProcess> games = solvedTournament.getGames();
		        		int blockStart = 0;
		        		for(int currentGame = 0; currentGame < games.size(); currentGame++)
		        		{
		        			int blockNumber = TournamentScoreCalculator.getBlockForIndex(currentGame, games.size());
		        			if(TournamentScoreCalculator.isStartOfBlock(currentGame, games.size()))
		        			{
		        				blockStart = currentGame;
		        			}
		        			int currentGameInBlock = currentGame - blockStart;
		        			Duration delta = new Duration(currentGameInBlock * Game.getGameLength().getMillis() + currentGameInBlock * TIME_BETWEEN_GAMES.getMillis()); 
		        			Game game = new Game(null, 
		        					currentGame + 1,
		        					RoundStartHour[blockNumber].plus(delta),
		        					GameTypeEnum.PRELIMINARY, 
		        					games.get(currentGame).getBlueTeam(), 
		        					games.get(currentGame).getYellowTeam(), 
		        					new ArrayList<GameEvent>(),
		        					(currentGame == 0) ? true : false
		        					);
		        			
		        			essentials.database.save(game);
		        		}
		        	}
		        }
			}
		}
	}
}